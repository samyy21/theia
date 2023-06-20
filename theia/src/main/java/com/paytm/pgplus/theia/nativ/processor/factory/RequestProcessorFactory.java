package com.paytm.pgplus.theia.nativ.processor.factory;

import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class RequestProcessorFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SuppressWarnings("rawtypes")
    public IRequestProcessor getRequestProcessor(RequestType type) {
        if (type == RequestType.NATIVE_PAY_VIEW_CONSULT) {
            return (IRequestProcessor) applicationContext.getBean("nativePayviewConsultRequestProcessor");
        } else if (type == RequestType.NATIVE_PAY_VIEW_CONSULT_V5) {
            return (IRequestProcessor) applicationContext.getBean("nativePayviewConsultRequestProcessorV5");
        } else if (type == RequestType.LOGIN_SEND_OTP_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeGenerateOtpRequestProcessor");
        } else if (type == RequestType.SUPERGW_SEND_OTP_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("superGwSendOtpRequestProcessor");
        } else if (type == RequestType.LOGIN_VALIDATE_OTP_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeValidateOtpRequestProcessor");
        } else if (type == RequestType.SUPERGW_VALIDATE_OTP_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("superGwValidateOtpRequestProcessor");
        } else if (type == RequestType.INITIATE_TRANSACTION_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeInitiateTransactionRequestProcessor");
        } else if (type == RequestType.NATIVE_BIN_DETAIL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeBinDetailRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_NB_PAY_CHANNEL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchNetBankingPayChannelRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_EMI_PAY_CHANNEL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchEmiPayChannelRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_EMI_DETAIL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchEmiDetailRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_BALANCE_INFO) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchBalanceInfoRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_VPA_DETAILS) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchVpaDetailsRequestProcessor");
        } else if (type == RequestType.FETCH_MERCHANT_USER_INFO) {
            return (IRequestProcessor) applicationContext.getBean("fetchMerchantUserInfoRequestProcessor");
        } else if (type == RequestType.FETCH_MERCHANT_INFO) {
            return (IRequestProcessor) applicationContext.getBean("fetchMerchantInfoRequestProcessor");
        } else if (type == RequestType.FETCH_PROMO_CODE_DETAIL) {
            return (IRequestProcessor) applicationContext.getBean("nativePromoCodeDetailRequestProcessor");
        } else if (type == RequestType.UPDATE_TRANSACTION_DETAIL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeUpdateTransactionDetailRequestProcessor");
        } else if (type == RequestType.FETCH_CARD_INDEX_NUMBER_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchCardIndexNumberRequestProcessor");
        } else if (type == RequestType.ENHANCED_LOGIN_VALIDATE_OTP_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("enhancedValidateOtpRequestProcessor");
        } else if (type == RequestType.NATIVE_FETCH_PCF_DETAILS) {
            return (IRequestProcessor) applicationContext.getBean("nativeFetchPcfDetailsRequestProcessor");
        } else if (type == RequestType.CHECK_EMI_ELIGIBILITY) {
            return (IRequestProcessor) applicationContext.getBean("checkEMIEligibilityProcessor");
        } else if (type == RequestType.ENHANCED_LOGOUT_USER_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("enhancedLogoutUserRequestProcessor");
        } else if (type == RequestType.NATIVE_KYC_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeKYCDetailRequestProcessor");
        } else if (type == RequestType.NATIVE_SUBSCRIPTION) {
            return (IRequestProcessor) applicationContext.getBean("nativeSubscriptionTransactionRequestProcessor");
        } else if (type == RequestType.NATIVE_AOA_SUBSCRIPTION) {
            return (IRequestProcessor) applicationContext.getBean("nativeAOASubscriptionTransactionRequestProcessor");
        } else if (type == RequestType.NATIVE_DIRECT_BANK_PAGE_RESEND_OTP) {
            return (IRequestProcessor) applicationContext.getBean("nativeDirectBankPageResendOtpProcessor");
        } else if (type == RequestType.NATIVE_DIRECT_BANK_PAGE_SUBMIT) {
            return (IRequestProcessor) applicationContext.getBean("nativeDirectBankPageSubmitProcessor");
        } else if (type == RequestType.NATIVE_DIRECT_BANK_PAGE_CANCEL) {
            return (IRequestProcessor) applicationContext.getBean("nativeDirectBankPageCancelProcessor");
        } else if (type == RequestType.VALIDATE_VPA_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeValidateVpaRequestProcessor");
        } else if (type == RequestType.SUPERGW_VALIDATE_VPA_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("superGwValidateVpaRequestProcessor");
        } else if (type == RequestType.FETCH_BIN_CARD_HASH_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeBinCardHashRequestProcessor");
        } else if (type == RequestType.TRANSACTION_STATUS) {
            return (IRequestProcessor) applicationContext.getBean("transactionStatusProcessor");
        } else if (type == RequestType.RISK_VERIFIER_DO_VIEW) {
            return (IRequestProcessor) applicationContext.getBean("riskVerifierDoViewRequestProcessor");
        } else if (type == RequestType.RISK_VERIFIER_DO_VERIFY) {
            return (IRequestProcessor) applicationContext.getBean("riskVerifierDoVerifyRequestProcessor");
        } else if (type == RequestType.VALIDATE_PRN_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeValidatePRNRequestProcessor");
        } else if (type == RequestType.NATIVE_GET_MERCHANT_EMI_DETAILS) {
            return (IRequestProcessor) applicationContext.getBean("nativeMerchantEmiDetailRequestProcessor");
        } else if (type == RequestType.FETCH_QR_PAYMENT_DETAILS) {
            return (IRequestProcessor) applicationContext.getBean("fetchQRPaymentDetailsRequestProcessor");
        } else if (type == RequestType.NATIVE_LOGOUT_USER) {
            return (IRequestProcessor) applicationContext.getBean("nativeLogoutUserProcessor");
        } else if (type == RequestType.DE_ENROLL_ONE_CLICK) {
            return (IRequestProcessor) applicationContext.getBean("deEnrollOneClickRequestProcessor");
        } else if (type == RequestType.VALIDATE_EMI_SUBVENTION) {
            return (IRequestProcessor) applicationContext.getBean("validateEmiSubventionProcessor");
        } else if (type == RequestType.BANKS_EMI_SUBVENTION) {
            return (IRequestProcessor) applicationContext.getBean("banksEmiSubventionProcessor");
        } else if (type == RequestType.TENURE_EMI_SUBVENTION) {
            return (IRequestProcessor) applicationContext.getBean("tenuresEmiSubventionProcessor");
        } else if (type == RequestType.NATIVE_CONSENT_API) {
            return (IRequestProcessor) applicationContext.getBean("nativeConsentRequestProcessor");
        } else if (type == RequestType.VALIDATE_CARD_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeValidateCardRequestProcessor");
        } else if (type == RequestType.CARD_ENROLLMENT_STATUS) {
            return (IRequestProcessor) applicationContext.getBean("cardEnrollmentStatusRequestProcessor");
        } else if (type == RequestType.CREATE_ACCESS_TOKEN_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("accessTokenRequestProcessor");
        } else if (type == RequestType.CARD_NUMBER_VALIDATION_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("nativeCardNumberValidationRequestProcessor");
        } else if (type == RequestType.SUPERGW_BIN_DETAIL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("superGwBinDetailRequestProcessor");
        } else if (type == RequestType.SUPERGW_PAY_VIEW_CONSULT) {
            return (IRequestProcessor) applicationContext.getBean("superGwPayviewConsultProcessor");
        } else if (type == RequestType.SUPERGW_FETCH_NB_PAY_CHANNEL_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("superGwNbDetailChannelRequestProcessor");
        } else if (type == RequestType.SEND_NOTIFICATION_APP_INVOKE) {
            return (IRequestProcessor) applicationContext.getBean("sendNotificationAppInvokeRequestProcessor");
        } else if (type == RequestType.FETCH_DCC_RATES) {
            return (IRequestProcessor) applicationContext.getBean("fetchDccRatesRequestProcessor");
        } else if (type == RequestType.FETCH_USER_PAYMENT_OFFERS) {
            return (IRequestProcessor) applicationContext.getBean("fetchUserPaymentOffersRequestProcessor");
        } else if (type == RequestType.SET_USER_PREFERENCE) {
            return (IRequestProcessor) applicationContext.getBean("setUserPreferenceRequestProcessor");
        } else if (type == RequestType.FETCH_CARD_TOKEN_DETAILS) {
            return (IRequestProcessor) applicationContext.getBean("fetchCardTokenDetailRequestProcessor");
        } else if (type == RequestType.FETCH_USER_PAYMODE_STATUS) {
            return (IRequestProcessor) applicationContext.getBean("fetchUserPayModeStatusRequestProcessor");
        } else if (type == RequestType.FETCH_MERCHANT_STATIC_CONFIG) {
            return (IRequestProcessor) applicationContext.getBean("fetchMerchantStaticConfigRequestProcessor");
        } else if (type == RequestType.FETCH_PSP_APPS) {
            return (IRequestProcessor) applicationContext.getBean("fetchPspAppsRequestProcessor");
        } else if (type == RequestType.IMEI_VALIDATE_API) {
            return (IRequestProcessor) applicationContext.getBean("nativeImeiValidationProcessor");
        } else if (type == RequestType.POST_TRANSACTION_SPLIT_REQUEST) {
            return (IRequestProcessor) applicationContext.getBean("postTransactionSplitRequestProcessor");
        }

        return null;
    }

    public enum RequestType {

        OFFLINE_PAY_VIEW_CONSULT, NATIVE_PAY_VIEW_CONSULT, NATIVE_PAY_VIEW_CONSULT_V5, LOGIN_SEND_OTP_REQUEST, LOGIN_VALIDATE_OTP_REQUEST, INITIATE_TRANSACTION_REQUEST, NATIVE_BIN_DETAIL_REQUEST, NATIVE_FETCH_NB_PAY_CHANNEL_REQUEST, NATIVE_FETCH_EMI_PAY_CHANNEL_REQUEST, NATIVE_FETCH_EMI_DETAIL_REQUEST, NATIVE_FETCH_BALANCE_INFO, NATIVE_FETCH_VPA_DETAILS, FETCH_MERCHANT_USER_INFO, FETCH_MERCHANT_INFO, FETCH_PROMO_CODE_DETAIL, UPDATE_TRANSACTION_DETAIL_REQUEST, FETCH_CARD_INDEX_NUMBER_REQUEST, ENHANCED_LOGIN_VALIDATE_OTP_REQUEST, NATIVE_FETCH_PCF_DETAILS, CHECK_EMI_ELIGIBILITY, ENHANCED_LOGOUT_USER_REQUEST, NATIVE_KYC_REQUEST, NATIVE_SUBSCRIPTION, NATIVE_AOA_SUBSCRIPTION, NATIVE_DIRECT_BANK_PAGE_RESEND_OTP, NATIVE_DIRECT_BANK_PAGE_SUBMIT, NATIVE_DIRECT_BANK_PAGE_CANCEL, VALIDATE_VPA_REQUEST, FETCH_BIN_CARD_HASH_REQUEST, RISK_VERIFIER_DO_VIEW, RISK_VERIFIER_DO_VERIFY, TRANSACTION_STATUS, VALIDATE_PRN_REQUEST, NATIVE_GET_MERCHANT_EMI_DETAILS, FETCH_QR_PAYMENT_DETAILS, DE_ENROLL_ONE_CLICK, NATIVE_LOGOUT_USER, VALIDATE_EMI_SUBVENTION, BANKS_EMI_SUBVENTION, TENURE_EMI_SUBVENTION, NATIVE_CONSENT_API, VALIDATE_CARD_REQUEST, CARD_ENROLLMENT_STATUS, CREATE_ACCESS_TOKEN_REQUEST, CARD_NUMBER_VALIDATION_REQUEST, SUPERGW_SEND_OTP_REQUEST, SUPERGW_VALIDATE_OTP_REQUEST, SUPERGW_VALIDATE_VPA_REQUEST, SUPERGW_BIN_DETAIL_REQUEST, SUPERGW_PAY_VIEW_CONSULT, SUPERGW_FETCH_NB_PAY_CHANNEL_REQUEST, SEND_NOTIFICATION_APP_INVOKE, FETCH_DCC_RATES, FETCH_USER_PAYMENT_OFFERS, SET_USER_PREFERENCE, FETCH_CARD_TOKEN_DETAILS, FETCH_USER_PAYMODE_STATUS, FETCH_MERCHANT_STATIC_CONFIG, FETCH_PSP_APPS, IMEI_VALIDATE_API, POST_TRANSACTION_SPLIT_REQUEST;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}