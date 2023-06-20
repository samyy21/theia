package com.paytm.pgplus.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.constants.ETestResponseValidation;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;

/**
 * Created by Naman on 24/05/17.
 */
public class ResponseUtils {

    public static void validateTestResponse(GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean,
            String expectedResult) throws IOException {

        @SuppressWarnings("unchecked")
        Map<String, String> responseMap = (Map<String, String>) RequestUtils.mapJsonToObject(expectedResult, Map.class);

        for (Map.Entry<String, String> entry : responseMap.entrySet()) {

            ETestResponseValidation validation = ETestResponseValidation.getTestResponseChecks(entry.getKey());
            if (null != validation) {
                validateEachResponseParam(workFlowResponseBean, validation, entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateEachResponseParam(GenericCoreResponseBean<WorkFlowResponseBean> genericResponseBean,
            ETestResponseValidation validation, Object validationValue) {

        Integer value = 0;
        ArrayList<String> expectedArrayList = null;
        Boolean expectedBoolean = null;

        switch (validation) {

        case SUCCESSFULLY_PROCESSED:

            expectedBoolean = Boolean.valueOf(validationValue.toString());

            if (expectedBoolean) {
                Assert.assertTrue(genericResponseBean.isSuccessfullyProcessed(),
                        "IsSuccessfullyProcessed is Expected To Be True");
            } else {
                Assert.assertFalse(genericResponseBean.isSuccessfullyProcessed(),
                        "IsSuccessfullyProcessed is Expected To Be False");
            }

            return;

        case USER_DETAILS_NOT_NULL:

            Assert.assertTrue(validateUserDetails(genericResponseBean.getResponse().getUserDetails()),
                    "UserDetails Validation Failed");
            return;

        case MERCHANT_SAVED_CARD:

            value = Integer.valueOf(validationValue.toString());

            Assert.assertTrue(!genericResponseBean.getResponse().getUserDetails().getMerchantViewSavedCardsList()
                    .isEmpty(), "Merchant saved card list is empty");

            Assert.assertTrue(
                    genericResponseBean.getResponse().getUserDetails().getMerchantViewSavedCardsList().size() == value,
                    "Number of Merchant saved Card List is Not equals " + value);
            return;

        case ADD_AND_PAY_SAVED_CARD:

            value = Integer.valueOf(validationValue.toString());

            if (value != 0) {

                Assert.assertTrue(!genericResponseBean.getResponse().getUserDetails().getAddAndPayViewSavedCardsList()
                        .isEmpty(), "Add and Pay saved card list is empty");

                Assert.assertTrue(genericResponseBean.getResponse().getUserDetails().getAddAndPayViewSavedCardsList()
                        .size() == value, "Number of Add and Pay saved Card List is Not equals " + value);
            } else {

                Assert.assertTrue(
                        CollectionUtils.isEmpty(genericResponseBean.getResponse().getUserDetails()
                                .getAddAndPayViewSavedCardsList()), "Saved Card "
                                + "List is expected to be null or empty");

            }

            return;

        case MERCHANT_PAY_METHODS:

            Assert.assertNotNull(genericResponseBean.getResponse().getMerchnatViewResponse().getPayMethodViews(),
                    "Merchant Pay Method are expected to be Non Null");

            expectedArrayList = (ArrayList<String>) validationValue;

            Assert.assertTrue(
                    validatePayModes(genericResponseBean.getResponse().getMerchnatViewResponse().getPayMethodViews(),
                            expectedArrayList), "Merchant PayModes Are Not as Expected ::{}" + validationValue);
            return;

        case ADD_AND_PAY_PAY_METHODS:

            expectedArrayList = (ArrayList<String>) validationValue;

            if (expectedArrayList.size() != 0) {

                Assert.assertNotNull(genericResponseBean.getResponse().getAddAndPayViewResponse().getPayMethodViews(),
                        "AndAndPay Pay Method are expected to be Non Null");
                Assert.assertTrue(
                        validatePayModes(genericResponseBean.getResponse().getAddAndPayViewResponse()
                                .getPayMethodViews(), expectedArrayList), "Merchant PayModes Are Not as Expected ::{}"
                                + validationValue);
            } else {

                Assert.assertNull(genericResponseBean.getResponse().getAddAndPayViewResponse(),
                        "AndAndPay Pay Method are expected to be Null");
            }

            return;

        case PAYMENT_DONE:

            Assert.assertTrue(Boolean.valueOf(validationValue.toString()) == genericResponseBean.getResponse()
                    .isPaymentDone(), "Payment Done is expected to be " + Boolean.valueOf(validationValue.toString()));

            return;

        case TRANS_ID_NOT_BLANK:

            Assert.assertTrue(StringUtils.isNotBlank(genericResponseBean.getResponse().getTransID()),
                    "Trans ID is Excepted to be Not Blank");
            return;

        case QUERY_PAYMENT_STATUS:

            QueryPaymentStatus queryPaymentStatus = genericResponseBean.getResponse().getQueryPaymentStatus();

            Assert.assertNotNull(queryPaymentStatus, "PayResultQuery is expected to be non null");

            String paymentStatus = queryPaymentStatus.getPaymentStatusValue();

            if ("SUCCESS".equals(validationValue.toString())) {
                Assert.assertEquals(paymentStatus, "SUCCESS",
                        "Payment status is expected to be SUCCESS in PayResultQuery and found to be ::" + paymentStatus);
            } else {
                Assert.assertEquals(paymentStatus, "FAIL",
                        "Payment status is expected to be FAIL in PayResultQuery and found to be ::" + paymentStatus);
            }

            return;

        case QUERY_TRANSACTION_STATUS:

            QueryTransactionStatus queryTransactionStatus = genericResponseBean.getResponse()
                    .getQueryTransactionStatus();

            Assert.assertNotNull(queryTransactionStatus, "queryByAcquirementId is expected to be non null");

            String transactionStatus = queryTransactionStatus.getStatusDetailType();

            if ("SUCCESS".equals(validationValue.toString())) {
                Assert.assertEquals(transactionStatus, "SUCCESS",
                        "Transaction status is expected to be SUCCESS in queryByAcquirementId and found to be ::"
                                + transactionStatus);
            } else {
                Assert.assertEquals(transactionStatus, "FAIL",
                        "Transaction status is expected to be FAIL in queryByAcquirementId and found to be ::"
                                + transactionStatus);
            }

            return;

        case ASSERT_NULL_RESPONSE:

            expectedBoolean = Boolean.valueOf(validationValue.toString());

            if (expectedBoolean) {

                Assert.assertNull(genericResponseBean.getResponse(), "Null Response is expected");
                Assert.assertTrue(StringUtils.isNotBlank(genericResponseBean.getFailureDescription()),
                        "Failure description was expected to be not blank");
                Assert.assertTrue(StringUtils.isNotBlank(genericResponseBean.getFailureMessage()),
                        "Failure message was expected to be not blank");
            } else {
                Assert.assertNotNull(genericResponseBean.getResponse(), "Null Response is expected");
            }

            return;

        case BULK_FEE_CONSULT:

            expectedArrayList = (ArrayList<String>) validationValue;

            Assert.assertTrue(
                    validateFeeConsultConditions(genericResponseBean.getResponse().getConsultFeeResponse(),
                            expectedArrayList), "Bulk fee consult validation failed");

            return;

        }
    }

    private static boolean validateFeeConsultConditions(ConsultFeeResponse consultFeeResponse,
            ArrayList<String> validationValue) {

        if (validationValue != null && validationValue.size() > 0) {

            if (consultFeeResponse.getConsultDetails().size() == validationValue.size()) {

                Map<EPayMethod, ConsultDetails> consultDetailsMap = consultFeeResponse.getConsultDetails();

                for (String payMethodString : validationValue) {

                    EPayMethod ePayMethod = EPayMethod.getPayMethodByMethod(payMethodString);

                    if (consultDetailsMap.get(ePayMethod) == null) {
                        return false;
                    }

                }

                return true;
            }
        }

        return false;
    }

    private static boolean validatePayModes(List<PayMethodViewsBiz> payMethodViewsBizList,
            ArrayList<String> validationValue) {

        boolean result = true;

        if (validationValue.size() < 1 || validationValue.size() != payMethodViewsBizList.size()) {
            return false;
        }

        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizList) {

            String payMethod = payMethodViewsBiz.getPayMethod();

            if (StringUtils.isBlank(payMethod) || !validationValue.contains(payMethod)) {
                return false;
            }

        }

        return result;
    }

    private static boolean validateUserDetails(UserDetailsBiz userDetails) {

        boolean result = true;

        if (null == userDetails || isBlank(userDetails.getEmail()) || isBlank(userDetails.getInternalUserId())
                || isBlank(userDetails.getMobileNo()) || isBlank(userDetails.getPayerAccountNumber())
                || isBlank(userDetails.getUserId()) || isBlank(userDetails.getUserName())
                || isBlank(userDetails.getUserToken())) {
            result = false;
        }

        return result;
    }

    private static boolean isBlank(String string) {
        return StringUtils.isBlank(string);
    }
}
