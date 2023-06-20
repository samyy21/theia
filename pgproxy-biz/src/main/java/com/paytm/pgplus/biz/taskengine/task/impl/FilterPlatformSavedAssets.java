package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayCardOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.enums.BinConfigAttributesEnum;
import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.integration.enums.SupportRegion;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by charu on 11/05/20.
 */

/**
 * Temporary task created to filter save assets returned from platform. Filters
 * international card,
 */

@Service("filterPlatformSavedAssets")
public class FilterPlatformSavedAssets extends
        AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    @Autowired
    FF4JUtil ff4JUtil;

    @Override
    protected GenericCoreResponseBean<Boolean> doBizExecute(WorkFlowRequestBean input,
            WorkFlowTransactionBean transBean, WorkFlowResponseBean response) {
        if (transBean.getMerchantLiteViewConsult() != null) {
            filteringInternationalCards(transBean.getMerchantLiteViewConsult());
        }

        if (transBean.getAddAndPayLiteViewConsult() != null) {
            filteringInternationalCards((transBean.getAddAndPayLiteViewConsult()));
        }
        return new GenericCoreResponseBean<>(true);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FILTER_PLATFROM_SAVED_ASSETS;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return false;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.parseInt(ConfigurationUtil.getProperty(BizConstant.FILTER_PLATFROM_SAVED_ASSETS_EXECUTION_TIME,
                "50"));
    }

    @Override
    public boolean isRunnable(WorkFlowRequestBean input, WorkFlowTransactionBean transBean) {
        String userId = null;
        if (transBean.getUserDetails() != null) {
            userId = transBean.getUserDetails().getUserId();
        } else if (input.getUserDetailsBiz() != null) {
            userId = input.getUserDetailsBiz().getUserId();
        }
        return ff4JUtil.filterPlatformSavedAssets() && ff4JUtil.fetchSavedCardFromPlatform(input, userId);
    }

    private void filteringInternationalCards(LitePayviewConsultResponseBizBean consultResponseBizBean) {
        for (PayMethodViewsBiz payMethodViewsBiz : consultResponseBizBean.getPayMethodViews()) {
            if (EPayMethod.DEBIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())
                    || EPayMethod.CREDIT_CARD.getMethod().equals(payMethodViewsBiz.getPayMethod())) {
                Map<String, List<String>> payOptionSupportCountriesMap = new HashMap<>();
                for (PayChannelOptionViewBiz payChannelOptionView : payMethodViewsBiz.getPayChannelOptionViews()) {
                    if (payChannelOptionView.isEnableStatus()) {
                        List<String> supportIssuingCountries = new ArrayList<>();
                        supportIssuingCountries.addAll(payChannelOptionView.getSupportCountries());
                        payOptionSupportCountriesMap.put(payChannelOptionView.getPayOption(), supportIssuingCountries);
                    }
                }
                if (CollectionUtils.isNotEmpty(payMethodViewsBiz.getPayCardOptionViews())) {
                    Iterator iterator = payMethodViewsBiz.getPayCardOptionViews().iterator();
                    while (iterator.hasNext()) {
                        PayCardOptionViewBiz payCardOptionView = (PayCardOptionViewBiz) iterator.next();
                        String cardKey = payCardOptionView.getPayMethod() + "_" + payCardOptionView.getCardScheme();
                        String indian = payCardOptionView.getExtendInfo() != null ? payCardOptionView.getExtendInfo()
                                .get(BinConfigAttributesEnum.INDIAN.name()) : null;
                        if ((StringUtils.isEmpty(indian) || "false".equals(indian))
                                && !payOptionSupportCountriesMap.get(cardKey).contains(SupportRegion.INTL.name())) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
}
