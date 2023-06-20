package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EPayMethodGroup;
import com.paytm.pgplus.common.util.PayMethodUtility;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.theia.helper.MobileMaskHelper;
import com.paytm.pgplus.theia.nativ.enums.PayModeGroupSequenceEnum;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayOption;
import com.paytm.pgplus.theia.nativ.utils.PayModeOrderUtil;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_ONLINE_PAYMODE_GROUPS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIVE_PAYMODE_GROUPS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.*;

@Service
public class NativePayviewConsultRequestProcessorV5 extends NativePayviewConsultRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayviewConsultRequestProcessorV5.class);

    @Autowired
    private FF4JUtil ff4JUtil;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private MobileMaskHelper mobileMaskHelper;

    @Override
    protected NativeCashierInfoResponse postProcess(NativeCashierInfoContainerRequest request,
            CashierInfoContainerRequest serviceRequest, WorkFlowResponseBean workFlowResponse) throws Exception {
        CashierInfoRequest cashierInfoRequest = serviceRequest.getCashierInfoRequest();
        NativeCashierInfoRequest nativeCashierInfoRequest = request.getNativeCashierInfoRequest();
        NativeCashierInfoResponse nativeCashierInfoResponse = super.postProcess(request, serviceRequest,
                workFlowResponse);

        NativeCashierInfoResponseBody response = nativeCashierInfoResponse.getBody();
        String merchantPayModeGroupOrdering = enhancedCashierPageServiceHelper.getMerchantPayModeGroupSequence(
                cashierInfoRequest.getHead().getMid(),
                nativeCashierInfoRequest.getBody().getPaymodeSequenceEnum() == null ? PayModeGroupSequenceEnum.DEFAULT
                        : PayModeGroupSequenceEnum.valueOf(nativeCashierInfoRequest.getBody().getPaymodeSequenceEnum()
                                .name()), nativeCashierInfoRequest.getBody().isOfflineFlow() ? NATIVE_PAYMODE_GROUPS
                        : NATIVE_ONLINE_PAYMODE_GROUPS);

        if (StringUtils.isNotBlank(merchantPayModeGroupOrdering)) {
            Map<String, Integer> groupNameIndexMap = new HashMap<>();
            String[] payModeGroups = merchantPayModeGroupOrdering.split(",");
            for (int index = 0; index < payModeGroups.length; index++)
                groupNameIndexMap.put(payModeGroups[index], index);

            Map<String, EPayMethod> ePayMethodMap = PayMethodUtility.getEPayMethodMap();

            Map<String, Object> groupedMerchantPayOption = getGroupedPayOptions(response.getMerchantPayOption(),
                    ePayMethodMap, groupNameIndexMap);
            Map<String, Object> groupedAddMoneyPayOption = getGroupedPayOptions(response.getAddMoneyPayOption(),
                    ePayMethodMap, groupNameIndexMap);

            Map<String, Integer> groupPriorities = enhancedCashierPageServiceHelper
                    .getGroupPrioritybyPayMethodPriority(cashierInfoRequest.getHead().getMid(),
                            nativeCashierInfoRequest.getBody().getPaymodeSequenceEnum(), nativeCashierInfoRequest
                                    .getBody().getPaymodeSequence());

            response.setGroupPayOptionsPriorities(groupPriorities);
            response.setGroupedMerchantPayOption(groupedMerchantPayOption);
            response.setGroupedAddMoneyPayOption(groupedAddMoneyPayOption);
        }

        if (ff4JUtil.isFeatureEnabled(UN_GROUPED_PAYMODES_DISABLED, cashierInfoRequest.getHead().getMid())) {
            LOGGER.info("Disabling old payModes for mid {}", cashierInfoRequest.getHead().getMid());
            response.setMerchantPayOption(null);
            response.setAddMoneyPayOption(null);
        }

        addUserInfoInResponse(cashierInfoRequest, response);

        LOGGER.info("Native response returned for grouped fetchPaymentOptions is: {}", response);
        return nativeCashierInfoResponse;
    }

    public Map<String, Object> getGroupedPayOptions(PayOption payOption, Map<String, EPayMethod> ePayMethodMap,
            Map<String, Integer> groupNameIndexMap) {

        Map<String, Object> groupedPayOptions = new HashMap<>();

        if (!ObjectUtils.notEqual(payOption, null)) {
            return groupedPayOptions;
        }

        if (CollectionUtils.isNotEmpty(payOption.getPayMethods())) {
            List<PayMethod> payMethods = payOption.getPayMethods();

            payMethods = new ArrayList<>(payMethods);
            payMethods.sort((method1, method2) -> {
                if (method1.getPriority() == null)
                    return 1;
                if (method2.getPriority() == null)
                    return -1;
                return Integer.parseInt(method1.getPriority()) - Integer.parseInt(method2.getPriority());
            });

            for (PayMethod payMethod : payMethods) {
                EPayMethod ePayMethod = ePayMethodMap.get(payMethod.getPayMethod());
                if (ePayMethod == null)
                    continue;
                EPayMethodGroup ePayMethodGroup = ePayMethod.getMethodGroup();
                if (ePayMethodGroup == null)
                    ePayMethodGroup = EPayMethodGroup.OTHER_OPTIONS;
                if (!groupNameIndexMap.containsKey(ePayMethodGroup.getGroupName()))
                    continue;
                List<PayMethod> methodList = (List<PayMethod>) groupedPayOptions.getOrDefault(
                        ePayMethodGroup.getDisplayName(), new ArrayList<>());
                payMethod.setPriority(String.valueOf(methodList.size() + 1));
                methodList.add(payMethod);
                groupedPayOptions.put(ePayMethodGroup.getDisplayName(), methodList);
            }
        }

        putInMap(EPayMethodGroup.SAVED_CARD, payOption.getSavedInstruments(), groupNameIndexMap, groupedPayOptions);
        putInMap(EPayMethodGroup.SAVED_VPA, payOption.getUserProfileSarvatra(), groupNameIndexMap, groupedPayOptions);
        putInMap(EPayMethodGroup.UPI_PROFILE, payOption.getUpiProfileV4(), groupNameIndexMap, groupedPayOptions);
        putInMap(EPayMethodGroup.SAVED_MANDATE_BANK, payOption.getSavedMandateBanks(), groupNameIndexMap,
                groupedPayOptions);

        groupedPayOptions = groupedPayOptions.entrySet().stream()
                .sorted(new PayModeOrderUtil.PayModeGroupComparator(groupNameIndexMap))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        return groupedPayOptions;
    }

    private void putInMap(EPayMethodGroup ePayMethodGroup, Object value, Map<String, Integer> groupNameIndexMap,
            Map<String, Object> groupedPayOptions) {
        if (!groupNameIndexMap.containsKey(ePayMethodGroup.getGroupName()) || value == null)
            return;
        groupedPayOptions.put(ePayMethodGroup.getDisplayName(), value);
    }

    private void addUserInfoInResponse(CashierInfoRequest cashierInfoRequest, NativeCashierInfoResponseBody response) {
        if (response.getUserDetails() == null
                && ff4JUtil
                        .isFeatureEnabled(RETURN_USER_INFO_IN_V5_FPO_RESPONSE, cashierInfoRequest.getHead().getMid())) {
            UserInfo userInfo = cashierInfoRequest.getBody().getUserInfo();
            if (userInfo != null
                    && StringUtils.isNotBlank(userInfo.getMobile())
                    && ff4JUtil.isFeatureEnabled(MASK_MOBILE_ON_CASHIER_PAGE_ENABLED, cashierInfoRequest.getHead()
                            .getMid())) {
                String mobileNo = userInfo.getMobile();
                userInfo.setMobile(mobileMaskHelper.getMaskedNumber(mobileNo));
            }
            response.setUserInfo(userInfo);
        }
    }

}
