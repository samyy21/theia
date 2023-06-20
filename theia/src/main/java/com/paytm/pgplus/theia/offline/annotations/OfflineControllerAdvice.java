package com.paytm.pgplus.theia.offline.annotations;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * Created by rahulverma on 15/9/17.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OfflineControllerAdvice {
}
