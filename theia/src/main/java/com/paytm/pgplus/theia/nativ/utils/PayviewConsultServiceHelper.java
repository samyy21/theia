package com.paytm.pgplus.theia.nativ.utils;

import java.util.*;

import com.paytm.pgplus.biz.utils.ObjectMapperUtil;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoV2;
import com.paytm.pgplus.cache.model.PerfernceInfo;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.model.payview.PayChannelBase;
import com.paytm.pgplus.theia.offline.model.payview.PayMethod;
import com.paytm.pgplus.theia.offline.model.payview.SavedCard;
import com.paytm.pgplus.theia.offline.model.payview.SavedInstruments;
import com.paytm.pgplus.theia.offline.model.payview.SavedVPA;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponseBody;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PAY_WITH_PAYTM;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.THEME_PREFERENCE_DETAILS;

/**
 * Created by rahulverma on 31/10/17.
 */
@Component("payviewConsultServiceHelper")
public class PayviewConsultServiceHelper extends
        BasePayviewConsultServiceHelper<CashierInfoRequest, CashierInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayviewConsultServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(PayviewConsultServiceHelper.class);

    @Autowired
    @Qualifier("customBeanMapper")
    private ICustomBeanMapper<CashierInfoResponse> customBeanMapper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private IMerchantDataService merchantDataService;

    @Override
    public CashierInfoResponse transformResponse(WorkFlowResponseBean workFlowResponse, CashierInfoRequest serviceReq,
            CashierInfoRequest request) {
        CashierInfoResponse cashierInfoResponse = customBeanMapper.getCashierInfoResponse(workFlowResponse, request);
        filterDisabledPayMethods(cashierInfoResponse);
        filterDisabledSavedInstruments(cashierInfoResponse);
        trimResponse(request, cashierInfoResponse);
        return cashierInfoResponse;
    }

    private void trimResponse(CashierInfoRequest cashierInfoRequest, CashierInfoResponse cashierInfoResponse) {
        LOGGER.info("Trimming Response ...");
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null)
            return;
        List<InstrumentType> instrumentTypes = cashierInfoRequest.getBody().getInstrumentTypes();
        List<InstrumentType> savedInstrumentsTypes = cashierInfoRequest.getBody().getSavedInstrumentsTypes();

        int netbankingOptionSize = instrumentTypes.contains(InstrumentType.NB_TOP5) ? 5 : -1;

        CashierInfoResponseBody cashierInfoResponseBody = cashierInfoResponse.getBody();
        List<PayMethod> merchantPayMethods = cashierInfoResponseBody.getPayMethodViews().getMerchantPayMethods();
        List<PayMethod> addMoneyPayMethods = cashierInfoResponseBody.getPayMethodViews().getAddMoneyPayMethods();
        SavedInstruments merchantSavedInstruments = cashierInfoResponseBody.getPayMethodViews()
                .getMerchantSavedInstruments();
        SavedInstruments addMoneySavedInstruments = cashierInfoResponseBody.getPayMethodViews()
                .getAddMoneySavedInstruments();

        trimInstrumentTypes(instrumentTypes, merchantPayMethods);
        trimInstrumentTypes(instrumentTypes, addMoneyPayMethods);
        trimSavedInstrumentTypes(savedInstrumentsTypes, merchantSavedInstruments);
        trimSavedInstrumentTypes(savedInstrumentsTypes, addMoneySavedInstruments);

        trimPayChannelOptions(merchantPayMethods, netbankingOptionSize);
        trimPayChannelOptions(addMoneyPayMethods, netbankingOptionSize);

        LOGGER.info("Trimming Response Done");
        LOGGER.debug("Trimmed Response {}", cashierInfoResponse);
    }

    private void filterDisabledPayMethods(CashierInfoResponse cashierInfoResponse) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null)
            return;
        filterDisabledPayMethods(cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods());
        filterDisabledPayMethods(cashierInfoResponse.getBody().getPayMethodViews().getAddMoneyPayMethods());

    }

    private void filterDisabledSavedInstruments(CashierInfoResponse cashierInfoResponse) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null)
            return;
        filterDisabledSavedInstruments(cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments());
        filterDisabledSavedInstruments(cashierInfoResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments());

    }

    private void filterDisabledSavedInstruments(SavedInstruments savedInstruments) {
        if (savedInstruments == null)
            return;
        if (savedInstruments.getSavedCards() != null && !savedInstruments.getSavedCards().isEmpty()) {
            Iterator<SavedCard> savedCardIterator = savedInstruments.getSavedCards().iterator();
            if (savedCardIterator.hasNext()) {
                SavedCard savedCard = savedCardIterator.next();
                if (savedCard != null && savedCard.getIsDisabled() != null
                        && Boolean.parseBoolean(savedCard.getIsDisabled().getStatus())) {
                    savedCardIterator.remove();
                }
            }

        }
        if (savedInstruments.getSavedVPAs() != null && !savedInstruments.getSavedCards().isEmpty()) {
            Iterator<SavedVPA> savedVPAIterator = savedInstruments.getSavedVPAs().iterator();
            if (savedVPAIterator.hasNext()) {
                SavedVPA savedVPA = savedVPAIterator.next();
                if (savedVPA != null && savedVPA.getIsDisabled() != null
                        && Boolean.parseBoolean(savedVPA.getIsDisabled().getStatus())) {
                    savedVPAIterator.remove();
                }
            }
        }

    }

    private void filterDisabledPayMethods(List<PayMethod> payMethods) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (payMethod.getPayChannelOptions() != null) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (payChannelBase.getIsDisabled() != null
                            && Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        payChannelBaseIterator.remove();
                    }
                }
            }
            if (payMethod.getPayChannelOptions() == null
                    || payMethod.getPayChannelOptions().isEmpty()
                    || (payMethod.getIsDisabled() != null && Boolean
                            .parseBoolean(payMethod.getIsDisabled().getStatus()))) {
                payMethodIterator.remove();
            }
        }
    }

    private void trimInstrumentTypes(List<InstrumentType> instrumentTypes, List<PayMethod> payMethods) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Set<String> instrumentTypeSet = instrumentTypeListToPayMethodStringSet(instrumentTypes);
        if (instrumentTypeSet == null || instrumentTypeSet.contains(InstrumentType.ALL.getPayMethod()))
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (!instrumentTypeSet.contains(payMethod.getPayMethod())) {
                payMethodIterator.remove();
            }
        }
    }

    private void trimSavedInstrumentTypes(List<InstrumentType> savedInstrumentTypes, SavedInstruments savedInstruments) {
        if (savedInstruments == null)
            return;
        Set<String> savedInstrumentTypeSet = instrumentTypeListToPayMethodStringSet(savedInstrumentTypes);
        if (savedInstrumentTypeSet == null || savedInstrumentTypeSet.contains(InstrumentType.ALL.getPayMethod()))
            return;
        List<SavedCard> savedCards = savedInstruments.getSavedCards();
        List<SavedVPA> savedVPAs = savedInstruments.getSavedVPAs();

        trimSavedCard(savedInstrumentTypeSet, savedCards);
        trimSavedVPA(savedInstrumentTypeSet, savedVPAs);
    }

    private void trimSavedCard(Set<String> savedInstrumentTypeSet, List<SavedCard> savedCards) {
        if (savedInstrumentTypeSet == null || savedCards == null || savedCards.isEmpty())
            return;
        Iterator<SavedCard> savedCardIterator = savedCards.iterator();
        while (savedCardIterator.hasNext()) {
            SavedCard savedCard = savedCardIterator.next();
            if (!savedInstrumentTypeSet.contains(savedCard.getPayMethod())) {
                savedCardIterator.remove();
            }
        }
    }

    private void trimSavedVPA(Set<String> savedInstrumentTypeSet, List<SavedVPA> savedVPAs) {
        if (savedInstrumentTypeSet == null || savedVPAs == null || savedVPAs.isEmpty())
            return;
        Iterator<SavedVPA> savedVPAIterator = savedVPAs.iterator();
        while (savedVPAIterator.hasNext()) {
            SavedVPA savedVPA = savedVPAIterator.next();
            if (!savedInstrumentTypeSet.contains(savedVPA.getPayMethod())) {
                savedVPAIterator.remove();
            }
        }
    }

    private void trimPayChannelOptions(List<PayMethod> payMethods, int netbankingOptionSize) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                payMethod.setPayChannelOptions(Collections.emptyList());
            }
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod()) && netbankingOptionSize > 0
                    && netbankingOptionSize <= payMethod.getPayChannelOptions().size()) {
                // TODO:Assuming it is sorted
                payMethod.setPayChannelOptions(new ArrayList<PayChannelBase>(payMethod.getPayChannelOptions().subList(
                        0, netbankingOptionSize)));
            }
        }

    }

    private Set<String> instrumentTypeListToPayMethodStringSet(List<InstrumentType> instrumentTypes) {
        Set<String> instrumentTypeSet = new HashSet<String>();
        if (instrumentTypes == null)
            return instrumentTypeSet;
        for (InstrumentType instrumentType : instrumentTypes) {
            instrumentTypeSet.add(instrumentType.getPayMethod());
        }
        return instrumentTypeSet;
    }

    @Override
    public void trimConsultFeeResponse(CashierInfoResponse response) {

    }

    public String getCategoryForPWPMerchant(String mid) {

        PerfernceInfo merchantPreferenceInfoExt = null;
        String pwpCategory = null;

        try {
            merchantPreferenceInfoExt = merchantDataService.getMerchantPreferenceInfoExt(mid, PAY_WITH_PAYTM);
            EXT_LOGGER.customInfo("Mapping response - MerchantPreferenceInfoExt :: {}", merchantPreferenceInfoExt);

            if (merchantPreferenceInfoExt != null
                    && CollectionUtils.isNotEmpty(merchantPreferenceInfoExt.getMerchantPreferenceInfos())) {
                PerfernceInfo.MerchantPreferenceInfo merchantPreferenceInfo = merchantPreferenceInfoExt
                        .getMerchantPreferenceInfos().get(0);
                Map<String, String> prefValues = JsonMapper.mapJsonToObject(merchantPreferenceInfo.getPrefValue(),
                        Map.class);
                pwpCategory = prefValues.get("CATEGORY");
            }
        } catch (Exception e) {
            LOGGER.info("Could not get data from mapping service for PWP Category");
        }

        return StringUtils.isNotBlank(pwpCategory) ? pwpCategory : null;
    }

}
