package com.paytm.pgplus.theia.logging;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.logging.ExtendedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.THEIA_STACKTRACE_LIMIT;

@Component("exceptionLogUtils")
public class ExceptionLogUtils {

    @Autowired
    private Ff4jUtils ff4jUtil;

    private static Ff4jUtils ff4jUtils;

    @PostConstruct
    private void init() {
        ff4jUtils = this.ff4jUtil;
    }

    private static int limitLines = 5;
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ExceptionLogUtils.class);

    public static Throwable limitLengthOfStackTrace(Throwable e) {
        try {
            limitLines = Integer.parseInt(ff4jUtils.getPropertyAsStringWithDefault(THEIA_STACKTRACE_LIMIT, "5"));
            if (limitLines == 0) {
                return e;
            }
            if (e != null && e.getStackTrace() != null && e.getStackTrace().length > limitLines) {
                e.setStackTrace(Arrays.copyOfRange(e.getStackTrace(), 0, limitLines));
            }
            return e;
        } catch (Exception e1) {
            EXT_LOGGER.info("Exception while fetching length of stack trace : {} ", e1.getMessage());
            return e;
        }
    }
}
