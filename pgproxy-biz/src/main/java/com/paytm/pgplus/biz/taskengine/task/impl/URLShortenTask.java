package com.paytm.pgplus.biz.taskengine.task.impl;

import com.paytm.pgplus.biz.taskengine.common.TaskName;
import com.paytm.pgplus.biz.taskengine.task.AbstractTask;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.urlshortner.IUrlShortnerService;
import com.paytm.pgplus.facade.urlshortner.model.request.ShortUrlRequest;
import com.paytm.pgplus.facade.urlshortner.model.response.ShortUrlAPIResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("urlShortenTask")
public class URLShortenTask extends AbstractTask<WorkFlowRequestBean, WorkFlowTransactionBean, WorkFlowResponseBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLShortenTask.class);

    @Autowired
    IUrlShortnerService urlShortnerService;

    @Override
    protected GenericCoreResponseBean<?> doBizExecute(WorkFlowRequestBean input, WorkFlowTransactionBean transBean,
            WorkFlowResponseBean response) {

        ShortUrlRequest shortUrlRequest = new ShortUrlRequest(input.getAppInvokeURL(),
                Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.THEIA_SHORTEN_URL_TTL, "15")));
        ShortUrlAPIResponse shortUrlAPIResponse;
        try {
            shortUrlAPIResponse = urlShortnerService.shortUrl(shortUrlRequest);
            if (shortUrlAPIResponse == null
                    || (shortUrlAPIResponse != null && StringUtils.isBlank(shortUrlAPIResponse.getShortUrl())))
                return new GenericCoreResponseBean<ShortUrlAPIResponse>("Short URL received is empty");
        } catch (FacadeCheckedException e) {
            LOGGER.info("Error while calling shorten URL service : {} ", e);
            return new GenericCoreResponseBean<ShortUrlAPIResponse>(
                    "Error while communicating with Shorten URL Service");
        }
        response.setShortUrlAPIResponse(shortUrlAPIResponse);
        return new GenericCoreResponseBean<>(shortUrlAPIResponse);
    }

    @Override
    public TaskName getTaskName() {
        return TaskName.FETCH_SHORTEN_URL;
    }

    @Override
    public boolean isMandatory(WorkFlowRequestBean inputBean, WorkFlowTransactionBean transBean) {
        return true;
    }

    @Override
    public int getMaxExecutionTime() {
        return Integer.valueOf(ConfigurationUtil.getProperty(BizConstant.SHORTEN_URL_TIME, "200"));
    }
}
