package com.paytm.pgplus.theia.test.util;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.CardInfo;
import com.paytm.pgplus.theia.sessiondata.DigitalCreditInfo;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.test.enums.ETestResponseValidation;

/**
 * @author kartik
 * @date 26-05-2017
 */
public class TestResponseUtil {

    private static final String TEST_CONTRAINT_FLAG = "Y";

    public static void validateTestResponse(PageDetailsResponse pageDetailsResponse, PaymentRequestBean requestData,
            ITheiaSessionDataService sessionDataService, TestResource testResource, String testResponseKey) {

        String expectedTestResponse = testResource.getTestProperties().getProperty(testResponseKey);
        @SuppressWarnings("unchecked")
        Map<String, String> responseConstraints = TestRequestUtil.mapJsonToObject(testResource.getObjectMapper(),
                expectedTestResponse, Map.class);

        // Check if conditions are met for every contraint
        for (Map.Entry<String, String> entry : responseConstraints.entrySet()) {
            ETestResponseValidation validation = ETestResponseValidation.getResponseValidationByName(entry.getKey());
            if (validation != null) {
                validateConstraint(validation, pageDetailsResponse, requestData, sessionDataService, entry.getValue());
            }
        }
    }

    private static void validateConstraint(ETestResponseValidation validation, PageDetailsResponse pageDetailsResponse,
            PaymentRequestBean requestData, ITheiaSessionDataService sessionDataService, String value) {
        boolean testFlag = TEST_CONTRAINT_FLAG.equalsIgnoreCase(value);

        switch (validation) {
        case BLANK_MERCHANT_RESPONSE:
            if (testFlag) {
                assertTrue(StringUtils.isBlank(pageDetailsResponse.getHtmlPage()),
                        "Merchant Response must be blank for default flow");
            } else {
                assertTrue(StringUtils.isNotBlank(pageDetailsResponse.getHtmlPage()),
                        "Merchant Response blank for failure test");
            }
            break;
        case LOGIN_FLAG_ENABLED:
            LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(requestData.getRequest(), false);
            assertNotNull(loginInfo, "Login info null for User logged in flow");
            assertNotNull(loginInfo.getUser(), "OAuthUserInfo null in session");
            if (testFlag) {
                assertTrue(loginInfo.isLoginFlag(), "User login flag not set true");
            } else {
                assertTrue(!loginInfo.isLoginFlag(), "User login flag not set false");
            }
            break;
        case SUCCESSFULLY_PROCESSED:
            if (testFlag) {
                assertTrue(pageDetailsResponse.isSuccessfullyProcessed(), "Processing failed");
            } else {
                assertTrue(!pageDetailsResponse.isSuccessfullyProcessed());
            }
            break;
        case SAVED_CARD_ENABLED:
            CardInfo cardInfo = sessionDataService.getCardInfoFromSession(requestData.getRequest(), false);
            if (testFlag) {
                assertNotNull(cardInfo, "CardInfo not present in session");
                if (cardInfo.isSaveCardEnabled()) {
                    assertTrue(CollectionUtils.isNotEmpty(cardInfo.getMerchantViewSavedCardsList()),
                            "SavedCards list found empty");
                }
            }
            break;
        case ADDNPAY_SAVED_CARD_ENABLED:
            CardInfo cardsInfo = sessionDataService.getCardInfoFromSession(requestData.getRequest(), false);
            if (testFlag) {
                assertNotNull(cardsInfo, "CardInfo not present in session");
                if (cardsInfo.isAddAndPayViewSaveCardEnabled()) {
                    assertTrue(CollectionUtils.isNotEmpty(cardsInfo.getAddAndPayViewCardsList()),
                            "AddnPay SavedCards list found empty");
                }
            }
            break;
        case REDIRECT_PAGE_IN_SESSION:
            if (testFlag) {
                String redirectPage = (String) requestData.getRequest().getSession(false).getAttribute("bankHTML");
                assertTrue(StringUtils.isNotBlank(redirectPage), "Redirect Page found null in session");
            }
            break;
        case DIGITAL_CREDIT_ENABLED:
            DigitalCreditInfo digitalCreditInfo = sessionDataService.getDigitalCreditInfoFromSession(
                    requestData.getRequest(), false);
            if (testFlag) {
                assertTrue(digitalCreditInfo.isDigitalCreditEnabled(), "DigitalCreditInfo not enabled!");
                assertTrue(StringUtils.isNotBlank(digitalCreditInfo.getExternalAccountNo()),
                        "DigitalCredit accountNo missing in session");
                assertTrue(StringUtils.isNotBlank(digitalCreditInfo.getLenderId()),
                        "DigitalCredit lenderId missing in session");
            } else {
                assertTrue(!digitalCreditInfo.isDigitalCreditEnabled(),
                        "DigitalCredit Enabled when user not logged in!");
            }
            break;
        case PROMO_CODE_ENABLED:
            TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest(), false);
            if (testFlag) {
                assertTrue(txnInfo.isPromoCodeValid(), "Promo Invalid");
                assertNotNull(txnInfo.getPromoCodeResponse(), "Promo Response null");
            }
            break;
        default:
            fail("Constraint does not match any validation type");
            break;
        }
    }

    @SuppressWarnings("unchecked")
    public static void validateTestResponseForSeamlessACS(WorkFlowResponseBean workflowResponseBean,
            TestResource testResource, String testResponseKey) throws FacadeCheckedException {
        Map<String, String> expectedResponse = JsonMapper.mapJsonToObject(
                testResource.getTestProperties().getProperty(testResponseKey), Map.class);
        Map<String, String> receivedResponse = JsonMapper.mapJsonToObject(
                JsonMapper.mapObjectToJson(workflowResponseBean.getSeamlessACSPaymentResponse()), Map.class);
        assertTrue(expectedResponse.size() == receivedResponse.size());
        for (Entry<String, String> entry : expectedResponse.entrySet()) {
            if (!"ACS_URL".equals(entry.getKey())) {
                assertTrue(entry.getValue().equals(receivedResponse.get(entry.getKey())));
            }
        }
    }
}
