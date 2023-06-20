package com.paytm.pgplus.cashier.service.test.payment.initiate;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.workflow.AbstractCashierWorkflow;
import com.paytm.pgplus.cashier.workflow.PaymentWorkflow;
import com.paytm.pgplus.checksum.utils.MappingServiceUtil;

import mockit.Mock;
import mockit.MockUp;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//import com.github.fppt.jedismock.RedisServer;
import static com.paytm.pgplus.cashier.service.test.payment.initiate.CashierWorkFlowConstants.ConfigConstants.REDIS_CENTOS_PATH;
import static com.paytm.pgplus.cashier.service.test.payment.initiate.CashierWorkFlowConstants.ConfigConstants.CATALINA_BASE;

/**
 * Created by charuaggarwal on 7/7/17.
 */

@ContextConfiguration(locations = { "classpath:cashier-context-test.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseCashierWorkFlowTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseCashierWorkFlowTest.class);

    private static final int FIXED_LOCAL_REDIS_PORT = 6379;

    private static final Properties prop = new Properties();

    // private static RedisServer redisServer;

    @Autowired
    TestUtils testUtils;

    @BeforeClass
    public static void initailize() {

        String rmiServerHost = System.getProperty("java.rmi.server.hostname", null);
        if (rmiServerHost == null) {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }

        new MockUp<PaymentWorkflow>() {
            @Mock
            public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) {
                InitiatePaymentResponse response = new InitiatePaymentResponse();
                return response;
            }
        };

        new MockUp<AbstractCashierWorkflow>() {
            @Mock
            public CashierPaymentStatus fetchBankForm(String cashierRequestId) throws CashierCheckedException {
                return new CashierPaymentStatus(new CashierPaymentStatus.CashierPaymentStatusBuilder("", "", "", "",
                        "", ""));
            }
        };
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InputStream inputStream = BaseCashierWorkFlowTest.class.getClassLoader().getResourceAsStream(
                "properties/cashier_test.properties");
        prop.load(inputStream);

        System.setProperty(CATALINA_BASE, prop.getProperty(CATALINA_BASE));

        bootstrapInMemoryRedis();
    }

    private static void bootstrapInMemoryRedis() throws IOException {
        /*
         * String redisPath = prop.getProperty(REDIS_CENTOS_PATH);
         * 
         * LOG.info("Starting Redis server on port: " + FIXED_LOCAL_REDIS_PORT);
         * 
         * RedisExecProvider redisExecProvider =
         * RedisExecProvider.defaultProvider().override(OS.UNIX, redisPath);
         * redisServer = new RedisServer(redisExecProvider,
         * FIXED_LOCAL_REDIS_PORT); if (redisServer.isActive()) {
         * redisServer.stop(); }
         */

        // redisServer = RedisServer.newRedisServer();
        // redisServer.start();
    }

    @Before
    public void beforeTest() {

        testUtils.cleanUpRedisBeforeTest();
    }

    @AfterClass
    public static void tearAfterClass() throws Exception {
        // redisServer.stop();
    }

}
