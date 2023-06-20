package com.paytm.pgplus.workflow;

import static com.paytm.pgplus.constants.Constants.FIXED_LOCAL_REDIS_PORT;
import static com.paytm.pgplus.constants.Constants.REDIS_CENTOS_PATH;
import static com.paytm.pgplus.constants.EApiResponseKeys.ADD_AND_PAY_CONSULT_PAYVIEW;
import static com.paytm.pgplus.constants.EApiResponseKeys.BULK_CHARGE_FEE_CONSULT;
import static com.paytm.pgplus.constants.EApiResponseKeys.CONSULT_PAYVIEW;
import static com.paytm.pgplus.constants.EApiResponseKeys.CREATE_ORDER;
import static com.paytm.pgplus.constants.EApiResponseKeys.CREATE_ORDER_AND_PAY;
import static com.paytm.pgplus.constants.EApiResponseKeys.CREATE_TOPUP;
import static com.paytm.pgplus.constants.EApiResponseKeys.FETCH_USER_DETAILS;
import static com.paytm.pgplus.constants.EApiResponseKeys.PAY_RESULT_QUERY;
import static com.paytm.pgplus.constants.EApiResponseKeys.QUERY_BY_ACQUIREMENT_ID;
import static com.paytm.pgplus.constants.EApiResponseKeys.WALLET_CONSULT;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.payment.utils.PGPlusWalletDecisionMaker;
import com.paytm.pgplus.biz.core.user.service.impl.SavedCardsImpl;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.constants.EApiResponseKeys;
import com.paytm.pgplus.facade.common.model.AlipayRequest;
import com.paytm.pgplus.facade.enums.AlipayServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.fund.models.request.ConsultWalletLimitsRequest;
import com.paytm.pgplus.facade.fund.models.response.ConsultWalletLimitsResponse;
import com.paytm.pgplus.facade.fund.services.impl.TopupImpl;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.user.helper.AuthenticationFixedResponses;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.impl.AuthenticationImpl;
import com.paytm.pgplus.facade.utils.AlipayApiClient;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.httpclient.config.ClientContextContainer;
import com.paytm.pgplus.looperclient.exception.LooperException;
import com.paytm.pgplus.looperclient.servicehandler.impl.LooperRequestHandlerImpl;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.requestidclient.IdManager;
import com.paytm.pgplus.requestidclient.enums.Groups;
import com.paytm.pgplus.requestidclient.models.SubscriberMetaInfo;
import com.paytm.pgplus.utils.MockUtils;
import com.paytm.pgplus.utils.PropertiesUtil;
import com.paytm.pgplus.utils.RequestUtils;
import com.paytm.pgplus.utils.ResponseUtils;

import mockit.Mock;
import mockit.MockUp;

import com.github.fppt.jedismock.RedisServer;

/**
 * Created by Naman on 19/05/17.
 */
@ContextConfiguration(locations = { "classpath:biz-context-test.xml" })
public abstract class Base extends AbstractTestNGSpringContextTests implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(Base.class);

    protected static Properties properties = PropertiesUtil.getProperties();

    private static RedisServer redisServer;

    String createOrderResponseKey;
    String createTopupResponseKey;
    String createOrderAndPayResponseKey;
    String consultPayViewResponseKey;
    String addAndPayConsultPayViewResponseKey;
    String queryByAcquirementId;
    String payResultQueryResponseKey;
    String walletConsultResponse;
    String fetchUserDetailsResponseKey;
    String bulkChargeFeeConsult;

    @BeforeClass
    public static void setUp() throws FacadeCheckedException, IOException, IllegalAccessException {

        ClientContextContainer.initialize();

        // These are not production env values. Please ignore.
        final SubscriberMetaInfo subscriberMetaInfo = new SubscriberMetaInfo(5, 50, 2000, Groups.PGPROXY, 10L,
                TimeUnit.MILLISECONDS, properties.getProperty("http://52.76.10.37/requestidservice/id/fetch"));
        IdManager.initialize(subscriberMetaInfo);

        bootstrapInMemoryRedis();
    }

    @BeforeMethod
    public void setUpMocks() {

        new MockUp<AlipayApiClient>() {
            @SuppressWarnings("null")
            @Mock
            public String sendPostRequestToAlipay(final String url, final AlipayRequest alipayRequest)
                    throws FacadeCheckedException, FacadeUncheckedException {

                String responseKey = null;

                if (url.endsWith(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER.getFunctionUrl())) {

                    responseKey = createOrderResponseKey;
                } else if (url.endsWith(AlipayServiceUrl.ACQUIRING_ORDER_CREATEORDER_AND_PAY.getFunctionUrl())) {

                    responseKey = createOrderAndPayResponseKey;
                } else if (url.endsWith(AlipayServiceUrl.FUND_USER_TOPUP_FROM_MERCHANT.getFunctionUrl())) {

                    responseKey = createTopupResponseKey;
                } else if (url.endsWith(AlipayServiceUrl.PAYMENT_CASHIER_PAYVIEW_CONSULT.getFunctionUrl())) {

                    if (alipayRequest.getRequest().contains("51053000100000000001")) {

                        responseKey = addAndPayConsultPayViewResponseKey;
                    } else {
                        responseKey = consultPayViewResponseKey;
                    }

                } else if (url.endsWith(AlipayServiceUrl.ACQUIRING_ORDER_QUERY_BY_ACQUIREMENT_ID.getFunctionUrl())) {

                    responseKey = queryByAcquirementId;
                } else if (url.endsWith(AlipayServiceUrl.BOSS_CHARGE_FEE_BATCH_CONSULT.getFunctionUrl())) {

                    responseKey = bulkChargeFeeConsult;
                }

                if (responseKey.contains("Exception")) {
                    throw new FacadeCheckedException("Exception occurred while communicating with Alipay");
                }

                return MockUtils.fetchMockResponse(responseKey);
            }

        };

        new MockUp<TopupImpl>() {
            @Mock
            ConsultWalletLimitsResponse consultWalletLimits(final ConsultWalletLimitsRequest consultWalletLimitsRequest)
                    throws FacadeCheckedException, FacadeUncheckedException {

                if (walletConsultResponse.equals("Exception")) {
                    throw new FacadeCheckedException("Wallet Limit Consult Failed");
                } else if (walletConsultResponse.equals("wallet.consult.failure")) {
                    return ConsultWalletLimitsResponse.FAILURE;
                } else {
                    return ConsultWalletLimitsResponse.SUCCESS;
                }
            }
        };

        new MockUp<LooperRequestHandlerImpl>() {
            @Mock
            PayResultQueryResponse fetchPaymentStatus(final PayResultQueryRequest requestData, final Object eventSource)
                    throws LooperException, FacadeCheckedException {

                String responseJson = MockUtils.fetchMockResponse(payResultQueryResponseKey);

                return JsonMapper.mapJsonToObject(responseJson, PayResultQueryResponse.class);
            }
        };

        new MockUp<AuthenticationImpl>() {
            @Mock
            FetchUserDetailsResponse fetchUserDetails(final FetchUserDetailsRequest fetchUserDetailsRequest)
                    throws FacadeCheckedException, FacadeUncheckedException {

                if (fetchUserDetailsResponseKey.equals("fetchUserDetails.success")) {

                    String responseJson = MockUtils.fetchMockResponse(fetchUserDetailsResponseKey);
                    UserDetailsV2 userDetailsV2 = JsonMapper.mapJsonToObject(responseJson, UserDetailsV2.class);
                    UserDetails userDetails = new UserDetails(userDetailsV2);
                    return new FetchUserDetailsResponse(userDetails);
                } else if (fetchUserDetailsResponseKey.equals("fetchUserDetails.failure")) {
                    return AuthenticationFixedResponses.fetchUserDetailsFailureResponse();
                } else {
                    throw new FacadeCheckedException("Exception occurred while fetching user details");
                }
            }
        };

        new MockUp<SavedCardsImpl>() {

            @Mock
            GenericCoreResponseBean<List<CardBeanBiz>> fetchSavedCardsByUserId(String userID) {

                return MockUtils.fetchSavedCardsSuccessFully();
            }
        };

        new MockUp<PGPlusWalletDecisionMaker>() {

            @Mock
            private double fetchWalletBalance(final ConsultPayViewResponseBizBean consultPayViewResponseBean) {
                return 1l;
            }
        };
    }

    private static void bootstrapInMemoryRedis() throws IOException {
        LOG.info("Starting Redis server on port: " + FIXED_LOCAL_REDIS_PORT);
        redisServer = RedisServer.newRedisServer();
        redisServer.start();
    }

    @AfterClass
    public static void tearAfterClass() throws Exception {
        redisServer.stop();
    }

    final String setFetchExpectedResultKey(String testCase) {

        String resultJson = properties.getProperty(testCase.replaceFirst("Request", "Response"));

        return resultJson;
    }

    public void executeTestCase(IWorkFlow workFlow, String testCase) throws IOException, IllegalAccessException,
            NoSuchFieldException {

        WorkFlowRequestBean workFlowRequestBean = RequestUtils.createWorkFlowRequestBean(testCase, properties);

        GenericCoreResponseBean<WorkFlowResponseBean> genericResponse = null;
        genericResponse = workFlow.process(workFlowRequestBean);

        ResponseUtils.validateTestResponse(genericResponse, setFetchExpectedResultKey(testCase));
    }

    public void setALLSuccessResponseKeys() {

        setSuccessResponseKey(CREATE_ORDER, CONSULT_PAYVIEW, CREATE_ORDER_AND_PAY, CONSULT_PAYVIEW,
                ADD_AND_PAY_CONSULT_PAYVIEW, PAY_RESULT_QUERY, QUERY_BY_ACQUIREMENT_ID, FETCH_USER_DETAILS,
                WALLET_CONSULT, CREATE_TOPUP, BULK_CHARGE_FEE_CONSULT);
    }

    public void setSuccessResponseKey(EApiResponseKeys... apiResponseKeys) {

        for (EApiResponseKeys responseKey : apiResponseKeys) {

            switch (responseKey) {

            case CREATE_ORDER:
                createOrderResponseKey = "create.order.success";
                continue;

            case CONSULT_PAYVIEW:
                consultPayViewResponseKey = "cashier.consult.acquiring.success";
                continue;

            case CREATE_TOPUP:
                createTopupResponseKey = "create.topup.success";
                continue;

            case WALLET_CONSULT:
                walletConsultResponse = "wallet.consult.success";
                continue;

            case PAY_RESULT_QUERY:
                payResultQueryResponseKey = "pay.result.query.success.successTxn";
                continue;

            case FETCH_USER_DETAILS:
                fetchUserDetailsResponseKey = "fetchUserDetails.success";
                continue;

            case CREATE_ORDER_AND_PAY:
                createOrderAndPayResponseKey = "create.orderAndPay.success";
                continue;

            case QUERY_BY_ACQUIREMENT_ID:
                queryByAcquirementId = "querybyAcquirementid.success";
                continue;

            case ADD_AND_PAY_CONSULT_PAYVIEW:
                addAndPayConsultPayViewResponseKey = "cashier.consult.acquiring.success";
                continue;

            case BULK_CHARGE_FEE_CONSULT:
                bulkChargeFeeConsult = "bulk.charge.fee.consult.success";
                continue;

            }
        }
    }

    public void setFailureResponseKey(EApiResponseKeys... apiResponseKeys) {

        for (EApiResponseKeys responseKey : apiResponseKeys) {

            switch (responseKey) {

            case CREATE_ORDER:
                createOrderResponseKey = "create.order.failure";
                continue;

            case CONSULT_PAYVIEW:
                consultPayViewResponseKey = "cashier.consult.acquiring.failure";
                continue;

            case CREATE_TOPUP:
                createTopupResponseKey = "create.topup.failure";
                continue;

            case WALLET_CONSULT:
                walletConsultResponse = "wallet.consult.failure";
                continue;

            case PAY_RESULT_QUERY:
                payResultQueryResponseKey = "pay.result.query.failure";
                continue;

            case FETCH_USER_DETAILS:
                fetchUserDetailsResponseKey = "fetchUserDetails.failure";
                continue;

            case CREATE_ORDER_AND_PAY:
                createOrderAndPayResponseKey = "create.orderAndPay.failure";
                continue;

            case QUERY_BY_ACQUIREMENT_ID:
                queryByAcquirementId = "querybyAcquirementid.failure";
                continue;

            case ADD_AND_PAY_CONSULT_PAYVIEW:
                addAndPayConsultPayViewResponseKey = "cashier.consult.acquiring.failure";
                continue;

            case BULK_CHARGE_FEE_CONSULT:
                bulkChargeFeeConsult = "bulk.charge.fee.consult.failure";
                continue;

            }
        }
    }

    public void setExceptionResponseKey(EApiResponseKeys... apiResponseKeys) {

        for (EApiResponseKeys responseKey : apiResponseKeys) {

            switch (responseKey) {

            case CREATE_ORDER:
                createOrderResponseKey = "Exception";
                continue;

            case CONSULT_PAYVIEW:
                consultPayViewResponseKey = "Exception";
                continue;

            case CREATE_TOPUP:
                createTopupResponseKey = "Exception";
                continue;

            case WALLET_CONSULT:
                walletConsultResponse = "Exception";
                continue;

            case PAY_RESULT_QUERY:
                payResultQueryResponseKey = "Exception";
                continue;

            case FETCH_USER_DETAILS:
                fetchUserDetailsResponseKey = "Exception";
                continue;

            case CREATE_ORDER_AND_PAY:
                createOrderAndPayResponseKey = "Exception";
                continue;

            case QUERY_BY_ACQUIREMENT_ID:
                queryByAcquirementId = "Exception";
                continue;

            case ADD_AND_PAY_CONSULT_PAYVIEW:
                addAndPayConsultPayViewResponseKey = "Exception";
                continue;

            case BULK_CHARGE_FEE_CONSULT:
                bulkChargeFeeConsult = "Exception";
                continue;
            }
        }
    }

    public void setPayResultQuery_FailureTxn() {
        payResultQueryResponseKey = "pay.result.query.success.failureTxn";
    }
}
