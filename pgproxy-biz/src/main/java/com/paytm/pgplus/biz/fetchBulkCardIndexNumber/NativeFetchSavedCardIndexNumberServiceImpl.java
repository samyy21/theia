package com.paytm.pgplus.biz.fetchBulkCardIndexNumber;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.paytm.pgplus.biz.core.cachecard.service.ICacheCardInfoService;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.request.CacheCardRequestBean;
import com.paytm.pgplus.biz.fetchBulkCardIndexNumber.model.NativeCardIndexNumberResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.nativ.utils.NativeFetchBulkCardIndexNumberTask;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service("nativeFetchSavedCardIndexNumberServiceImpl")
public class NativeFetchSavedCardIndexNumberServiceImpl implements INativeFetchSavedCardIndexNumberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFetchSavedCardIndexNumberServiceImpl.class);

    @Autowired
    NativeFetchSavedCardIndexNumberHelper nativeFetchSavedCardIndexNumberHelper;

    @Autowired
    @Qualifier("cachecardinfoservice")
    ICacheCardInfoService cacheCardInfoService;

    private static ExecutorService taskExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("NativeFetchSavedCardIndexNumberTaskExecutor-thread-%d").build());

    @Override
    public GenericCoreResponseBean<Boolean> fetchCardIndexNotLoggedIn(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {
        MidCustIdCardBizDetails midCustIdCardBizDetails = workFlowTransactionBean.getMidCustIdCardBizDetails();
        List<CardBeanBiz> merchantSavedCardList = null;
        // List<NativeFetchCardIndexNumberTask>

        List<NativeFetchBulkCardIndexNumberTask> nativeFetchCardIndexNumberTasksList = new ArrayList<>();

        if (midCustIdCardBizDetails != null
                && !CollectionUtils.isEmpty(midCustIdCardBizDetails.getMerchantCustomerCardList())) {

            merchantSavedCardList = midCustIdCardBizDetails.getMerchantCustomerCardList();
            for (CardBeanBiz cardBeanBiz : merchantSavedCardList) {
                CacheCardRequestBean cacheCardRequestBean = nativeFetchSavedCardIndexNumberHelper
                        .mapSavedCardtoCacheCardRequestBean(cardBeanBiz);
                NativeFetchBulkCardIndexNumberTask nativeFetchCardIndexNumberTask = new NativeFetchBulkCardIndexNumberTask(
                        cacheCardRequestBean, workFlowTransactionBean);
                nativeFetchCardIndexNumberTask.setCacheCardInfoService(cacheCardInfoService);
                nativeFetchCardIndexNumberTask
                        .setNativeFetchSavedCardIndexNumberHelper(nativeFetchSavedCardIndexNumberHelper);
                nativeFetchCardIndexNumberTasksList.add(nativeFetchCardIndexNumberTask);
            }
        }
        try {
            if (!isEmpty(nativeFetchCardIndexNumberTasksList)) {
                List<Future<NativeCardIndexNumberResponse>> cacheCardResponseList = taskExecutor
                        .invokeAll(nativeFetchCardIndexNumberTasksList);
                int i = 0;
                for (Future<NativeCardIndexNumberResponse> cacheCardRequestBeanIterator : cacheCardResponseList) {
                    // fill the cardIndex number for all the cards
                    try {
                        NativeCardIndexNumberResponse nativeCardIndexNumberResponse = cacheCardRequestBeanIterator.get(
                                200, TimeUnit.MILLISECONDS);
                        String cardIndexNumber = null;
                        if (nativeCardIndexNumberResponse != null
                                && nativeCardIndexNumberResponse.getCacheCardResponseBody() != null) {
                            cardIndexNumber = nativeCardIndexNumberResponse.getCacheCardResponseBody().getCardIndexNo();
                        }
                        midCustIdCardBizDetails.getMerchantCustomerCardList().get(i).setCardIndexNo(cardIndexNumber);
                        i++;
                    } catch (TimeoutException e) {
                        LOGGER.error("timeout occurred for the current running thread", e);
                        return new GenericCoreResponseBean<>(false);
                    } catch (ExecutionException e) {
                        LOGGER.error("current working thread  interuppted ", e);
                        return new GenericCoreResponseBean<>(false);
                    }

                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("interuppted exception was called", e);
            return new GenericCoreResponseBean<>(false);
        } finally {
            LOGGER.info("task executor finished its execution");

        }
        return new GenericCoreResponseBean<>(true);

    }

    @Override
    public GenericCoreResponseBean<Boolean> fetchCardIndexLoggedIn(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowTransactionBean workFlowTransactionBean, WorkFlowResponseBean workFlowResponseBean) {

        List<CardBeanBiz> merchantSavedCardList = new ArrayList<>();
        List<NativeFetchBulkCardIndexNumberTask> nativeFetchCardIndexNumberTasksList = new ArrayList<>();

        merchantSavedCardList = workFlowResponseBean.getUserDetails().getMerchantViewSavedCardsList();
        if (merchantSavedCardList != null) {
            for (CardBeanBiz cardBeanBiz : merchantSavedCardList) {
                CacheCardRequestBean cacheCardRequestBean = nativeFetchSavedCardIndexNumberHelper
                        .mapSavedCardtoCacheCardRequestBean(cardBeanBiz);

                NativeFetchBulkCardIndexNumberTask nativeFetchCardIndexNumberTask = new NativeFetchBulkCardIndexNumberTask(
                        cacheCardRequestBean, workFlowTransactionBean);

                nativeFetchCardIndexNumberTask.setCacheCardInfoService(cacheCardInfoService);
                nativeFetchCardIndexNumberTask
                        .setNativeFetchSavedCardIndexNumberHelper(nativeFetchSavedCardIndexNumberHelper);
                nativeFetchCardIndexNumberTasksList.add(nativeFetchCardIndexNumberTask);
            }
        }

        try {
            List<Future<NativeCardIndexNumberResponse>> cacheCardResponseList = new ArrayList<>();
            if (!isEmpty(nativeFetchCardIndexNumberTasksList)) {
                cacheCardResponseList = taskExecutor.invokeAll(nativeFetchCardIndexNumberTasksList);

                int i = 0;
                for (Future<NativeCardIndexNumberResponse> cacheCardRequestBeanIterator : cacheCardResponseList) {
                    // fill the cardIndex number for all the cards
                    try {
                        // timeout time to be decided
                        CacheCardResponseBody cacheCardResponseBean = cacheCardRequestBeanIterator.get(200,
                                TimeUnit.MILLISECONDS).getCacheCardResponseBody();
                        if (cacheCardResponseBean != null) {
                            String cardIndexNumber = cacheCardResponseBean.getCardIndexNo();
                            merchantSavedCardList.get(i).setCardIndexNo(cardIndexNumber);
                        }
                        i++;
                    } catch (TimeoutException e) {
                        LOGGER.error("timeout occurred for the current running thread", e);
                        return new GenericCoreResponseBean<>(false);
                    } catch (ExecutionException e) {
                        LOGGER.error("current working thread  interuppted ", e);
                        return new GenericCoreResponseBean<>(false);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("interuppted exception was called", e);
            return new GenericCoreResponseBean<>(false);

        } finally {
            LOGGER.info("task executor finished its execution");
        }
        return new GenericCoreResponseBean<>(true);

    }

}
