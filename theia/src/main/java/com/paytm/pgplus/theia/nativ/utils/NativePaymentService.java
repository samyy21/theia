package com.paytm.pgplus.theia.nativ.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.models.GoodsInfo;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
import com.paytm.pgplus.theia.nativ.model.token.UpdateTransactionDetailRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("nativePaymentService")
public class NativePaymentService implements INativePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePaymentService.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativePaymentService.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private MerchantDataServiceImpl merchantDataService;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public InitiateTokenBody initiateTransaction(NativeInitiateRequest request) {
        InitiateTokenBody initiateTokenBody = nativeSessionUtil.createToken(request);
        return initiateTokenBody;
    }

    @Override
    public InitiateTokenBody initiateTransaction(InitiateTransactionRequest request) {
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        nativeInitiateRequest.setInitiateTxnReq(request);
        return nativeSessionUtil.createToken(nativeInitiateRequest);
    }

    @Override
    public String generateResponseChecksum(InitiateTransactionRequest request, InitiateTransactionResponse response)
            throws Exception {
        Map<String, String> checksumParamMap = getChecksumParams(request, response);
        return ValidateChecksum
                .getInstance()
                .getRespCheckSumValue(getMerchantKey(request.getBody().getMid(), request.getHead().getClientId()),
                        checksumParamMap).get("CHECKSUMHASH");
    }

    private Map<String, String> getChecksumParams(InitiateTransactionRequest request,
            InitiateTransactionResponse response) {
        Map<String, String> checksumParamMap = new HashMap<String, String>();
        checksumParamMap.put("requestType", request.getBody().getRequestType());
        checksumParamMap.put("mid", request.getBody().getMid());
        checksumParamMap.put("orderId", request.getBody().getOrderId());
        checksumParamMap.put("txnAmount", request.getBody().getTxnAmount().getValue());
        checksumParamMap.put("txnToken", response.getBody().getTxnToken());
        checksumParamMap.put("authenticated", String.valueOf(response.getBody().isAuthenticated()));
        return checksumParamMap;
    }

    private String getMerchantKey(String mid, String clientId) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }

    @Override
    public Boolean updateTransactionDetail(UpdateTransactionDetailRequest request) {

        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(request.getHead().getTxnToken());
        if (request.getBody().getTxnAmount() != null) {
            orderDetail.setTxnAmount(request.getBody().getTxnAmount());
        }
        if (request.getBody().getExtendInfo() != null) {
            orderDetail.setExtendInfo(request.getBody().getExtendInfo());
        }
        if (request.getBody().getGoods() != null) {
            orderDetail.setGoods(request.getBody().getGoods());
        }

        if (request.getBody().getShippingInfo() != null) {
            orderDetail.setShippingInfo(request.getBody().getShippingInfo());
        }

        try {
            String txnId = nativeSessionUtil.getTxnId(request.getHead().getTxnToken());
            if (StringUtils.isNotBlank(txnId)) {
                List<Goods> goodsList = null;
                List<ShippingInfo> shippingInfos = null;
                WorkFlowRequestBean workFlowRequestBean = createWorkFlowRequestBean(orderDetail, request, txnId);
                if (null != request.getBody().getGoods()) {
                    goodsList = getGoods(orderDetail.getGoods());
                }
                if (null != request.getBody().getShippingInfo()) {
                    shippingInfos = JsonMapper.convertValue(orderDetail.getShippingInfo(),
                            new TypeReference<List<ShippingInfo>>() {
                            });
                }
                workFlowHelper.modifyCreatedOrderInInitiateTxn(workFlowRequestBean, goodsList, shippingInfos);
            }
            return nativeSessionUtil.setOrderDetail(request.getHead().getTxnToken(), orderDetail);
        } catch (Exception ex) {
            LOGGER.error("Exception in setting updated order details {}", ex);
        }
        return false;
    }

    private List<Goods> getGoods(List<GoodsInfo> goodsInfos) throws FacadeCheckedException {
        List<Goods> goods = new ArrayList<>();
        for (int i = 0; i < goodsInfos.size(); i++) {
            Goods good = null;
            try {
                good = new Goods.GoodsBuilder(goodsInfos.get(i).getDescription(), JsonMapper.convertValue(goodsInfos
                        .get(i).getPrice(), new TypeReference<Money>() {
                }), goodsInfos.get(i).getQuantity()).category(goodsInfos.get(i).getCategory())
                        .merchantGoodsId(goodsInfos.get(i).getMerchantGoodsId())
                        .merchantShippingId(goodsInfos.get(i).getMerchantShippingId())
                        .snapshotUrl(goodsInfos.get(i).getSnapshotUrl()).unit(goodsInfos.get(i).getUnit())
                        .extendInfo(getExtendInfo(goodsInfos.get(i).getExtendInfo())).build();
            } catch (FacadeCheckedException e) {
                throw e;
            }
            goods.add(good);
        }
        return goods;
    }

    public static Map<String, String> getGoodsExtendeInfoMap(final ExtendInfo extendInfo) {
        try {
            Map<String, Object> map = extendInfo != null ? JsonMapper.convertValueIncludeTransient(extendInfo,
                    Map.class) : Collections.emptyMap();
            Map<String, String> flatMap = CommonUtils.flattenMap(map, null);
            return flatMap;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public String getExtendInfo(final ExtendInfo extendInfo) throws FacadeCheckedException {
        String extendInfos = null;
        try {
            Map<String, String> extendeInfoMap = getGoodsExtendeInfoMap(extendInfo);
            extendInfos = (extendeInfoMap != null) && !extendeInfoMap.isEmpty() ? JsonMapper
                    .mapObjectToJson(extendeInfoMap) : null;
        } catch (Exception ex) {
            throw ex;
        }
        return extendInfos;
    }

    private WorkFlowRequestBean createWorkFlowRequestBean(InitiateTransactionRequestBody orderDetail,
            UpdateTransactionDetailRequest request, String txnId) throws MappingServiceClientException {
        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = new WorkFlowRequestBean();
            workFlowRequestBean.setOrderID(orderDetail.getOrderId());
            workFlowRequestBean.setTransID(txnId);
            MerchantInfo merchantInfo = merchantDataService.getMerchantMappingData(orderDetail.getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantInfo :: {}", merchantInfo);
            workFlowRequestBean.setAlipayMID(merchantInfo.getAlipayId());
            workFlowRequestBean.setPaytmMID(orderDetail.getMid());
            workFlowRequestBean.setFullPg2TrafficEnabled(merchantPreferenceService.isFullPg2TrafficEnabled(orderDetail
                    .getMid()));
            workFlowRequestBean.setIs3pAddMoneyEnabled(merchantPreferenceService.is3pAddMoney(orderDetail.getMid(),
                    false));
            if (null != request.getBody().getTxnAmount()) {
                workFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(request.getBody()
                        .getTxnAmount().getValue()));
            }
        } catch (Exception ex) {
            LOGGER.error("Create order exception in creating Workflow bean for modifying {}", ex);
            throw ex;
        }
        return workFlowRequestBean;
    }

}