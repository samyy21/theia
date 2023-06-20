package com.paytm.pgplus.theia.nativ.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.ObjectMapperUtil;
import com.paytm.pgplus.cache.enums.MerchantInfoRequest;
import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.cache.model.MerchantInfoResponse;
import com.paytm.pgplus.cache.model.VendorDetail;
import com.paytm.pgplus.cache.model.VendorParentDetails;
import com.paytm.pgplus.enums.SplitMethod;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.ShippingInfo;
import com.paytm.pgplus.facade.acquiring.models.SplitPayInfo;
import com.paytm.pgplus.facade.common.model.Money;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.ConnectionUtil;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.mappingserviceclient.service.IVendorInfoService;
import com.paytm.pgplus.models.MultiCurrency;
import com.paytm.pgplus.models.SplitInfoData;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.enums.AcquiringPG2ServiceUrl;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.splitSettlement.SplitCommandInfo;
import com.paytm.pgplus.theia.models.splitSettlement.SplitSettlementRequestBody;
import com.paytm.pgplus.theia.models.splitSettlement.SplitSettlementResponseBody;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitRequest;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitRequestBody;
import com.paytm.pgplus.theia.nativ.model.postTransactionSplit.PostTransactionSplitResponseBody;
import com.paytm.pgplus.theia.nativ.service.IPostTransactionSplitService;
import com.paytm.pgplus.theia.utils.SplitSettlementHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.PARTNER_ID;

@Service(value = "postTransactionSplitService")
public class PostTransactionSplitServiceImpl implements IPostTransactionSplitService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostTransactionSplitServiceImpl.class);

    @Autowired
    @Qualifier("merchantDataServiceImpl")
    private IMerchantDataService merchantDataService;

    @Autowired
    @Qualifier("vendorInfoService")
    private IVendorInfoService vendorInfoService;

    public PostTransactionSplitResponseBody acquiringSplit(PostTransactionSplitRequest request)
            throws FacadeCheckedException {
        SplitSettlementRequestBody requestBody = transformPostTransactionSplitRequest(request);
        SplitSettlementResponseBody response = ConnectionUtil.execute(requestBody,
                AcquiringPG2ServiceUrl.ACQUIRING_SPLIT, SplitSettlementResponseBody.class);
        return transformPostTransactionSplitResponse(response);
    }

    private SplitSettlementRequestBody transformPostTransactionSplitRequest(PostTransactionSplitRequest request) {
        List<SplitCommandInfo> splitCommandInfoList = populateSplitCommandInfoList(request.getBody());
        try {
            if (CollectionUtils.isNotEmpty(splitCommandInfoList)) {
                for (SplitCommandInfo splitCommandInfo : splitCommandInfoList) {
                    // Swapping targetMerchantId and pplusMid
                    String targetMid = StringUtils.isNotBlank(splitCommandInfo.getTargetMerchantId()) ? splitCommandInfo
                            .getTargetMerchantId() : splitCommandInfo.getPplusMid();
                    splitCommandInfo.setTargetMerchantId(splitCommandInfo.getPplusMid());
                    // For pplusMid, setting PG mid(targetMerchantId) for
                    // directly onboarded and pplusMid for existing vendors
                    splitCommandInfo.setPplusMid(targetMid);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred at transformPostTransactionSplitRequest : {}", e.getMessage());
        }
        return new SplitSettlementRequestBody(request.getBody().getMid(), request.getBody().getOrderId(), request
                .getBody().getAcqId(), request.getHead().getRequestId(), "Theia", splitCommandInfoList);
    }

    private PostTransactionSplitResponseBody transformPostTransactionSplitResponse(SplitSettlementResponseBody response) {
        return new PostTransactionSplitResponseBody(new ResultInfo(response.getResultInfo().getResultStatus(), response
                .getResultInfo().getResultCode(), response.getResultInfo().getResultMsg()), response.getMid(),
                response.getOrderId(), response.getAcqId());
    }

    private List<SplitCommandInfo> populateSplitCommandInfoList(PostTransactionSplitRequestBody requestData) {

        SplitSettlementInfoData splitSettlementInfoData = requestData.getSplitSettlementInfoData();
        List<String> partnerIdList = new ArrayList<>();
        List<String> midList = new ArrayList<>();

        for (SplitInfoData splitInfoData : splitSettlementInfoData.getSplitInfo()) {
            if (StringUtils.isNotBlank(splitInfoData.getMid()))
                midList.add(splitInfoData.getMid());
            if (StringUtils.isNotBlank(splitInfoData.getPartnerId()))
                partnerIdList.add(splitInfoData.getPartnerId());
        }

        try {
            if (!midList.isEmpty()) {
                if (midList.size() != splitSettlementInfoData.getSplitInfo().size()) {
                    LOGGER.error("midList : {}, SplitInfo : {}", midList, splitSettlementInfoData.getSplitInfo());
                    throw new PaymentRequestValidationException("Child MIDs are missing in splitInfo");
                }
                MerchantInfoRequest merchantInfoRequest = new MerchantInfoRequest(midList);
                final MerchantInfoResponse merchantInfoList = merchantDataService
                        .getMerchantInfoList(merchantInfoRequest);
                return convertSplitSettlementsDetails(requestData.getSplitSettlementInfoData(), merchantInfoList);
            } else if (!partnerIdList.isEmpty()) {
                if (partnerIdList.size() != splitSettlementInfoData.getSplitInfo().size()) {
                    LOGGER.error("partnerIdList : {}, SplitInfo : {}", partnerIdList,
                            splitSettlementInfoData.getSplitInfo());
                    throw new PaymentRequestValidationException("Child partnerIds are missing in splitInfo");
                }
                final VendorParentDetails vendorParentDetails = vendorInfoService.getVendorSplitDetails(
                        requestData.getMid(), partnerIdList);
                LOGGER.info("Mapping response - VendorParentDetails :: {}", vendorParentDetails);
                LOGGER.info("isPartiallyMatched flag: " + vendorParentDetails.isPartiallyMatched());

                if (BooleanUtils.isTrue(vendorParentDetails.isPartiallyMatched())) {
                    LOGGER.error("Merchant data is not found for all given partnerIds from Mapping Service");
                    throw new PaymentRequestValidationException("Merchant data not found for all given partnerIds");
                }
                return convertSplitSettlementsDetails(requestData.getSplitSettlementInfoData(), vendorParentDetails);
            } else {
                throw new PaymentRequestValidationException("Mid or partnerId is missing from splitInfo data");
            }
        } catch (MappingServiceClientException | PaymentRequestValidationException e) {
            LOGGER.error("Invalid mid in splitInfo : {}", e.getMessage());
            throw new PaymentRequestValidationException(e.getMessage(), ResponseConstants.INVALID_MID);
        }
    }

    private List<SplitCommandInfo> convertSplitSettlementsDetails(SplitSettlementInfoData splitSettlementInfo,
            MerchantInfoResponse merchantInfoResponse) {
        List<SplitPayInfo> splitPayInfos = new ArrayList<>();
        Map<String, String> paytmAlipayMid = new HashMap<>();
        if (merchantInfoResponse != null && merchantInfoResponse.getMerchantInfoList() != null) {
            for (MerchantInfo merchantInfo : merchantInfoResponse.getMerchantInfoList()) {
                paytmAlipayMid.put(merchantInfo.getPaytmId(), merchantInfo.getAlipayId());
            }
            try {
                if (splitSettlementInfo.getSplitInfo() != null) {
                    List<SplitCommandInfo> splitCommandInfoList = new ArrayList<>();
                    MultiCurrency splitAmount = null;
                    Money money = null;
                    for (SplitInfoData splitInfo : splitSettlementInfo.getSplitInfo()) {
                        splitAmount = null;
                        money = null;
                        List<Goods> goodsList = SplitSettlementHelper.createGoodsListFromGoodsInfoList(splitInfo
                                .getGoods());
                        List<ShippingInfo> shippingInfosList = SplitSettlementHelper
                                .createFacadeShippingListFromShippingInfoList(splitInfo.getShippingInfo());
                        if (SplitMethod.AMOUNT.equals(splitSettlementInfo.getSplitMethod())) {
                            splitAmount = new MultiCurrency(splitInfo.getAmount().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                            money = new Money(splitInfo.getAmount().getCurrency().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                        }
                        SplitPayInfo splitPayInfo = new SplitPayInfo(splitSettlementInfo.getSplitMethod(),
                                paytmAlipayMid.get(splitInfo.getMid()), splitAmount, splitInfo.getPercentage());
                        SplitCommandInfo splitCommandInfo = new SplitCommandInfo.SplitCommandInfoBuilder(
                                splitSettlementInfo.getSplitMethod(), paytmAlipayMid.get(splitInfo.getMid()),
                                splitInfo.getMid(), money, splitInfo.getPercentage())
                                .setExtendInfo(splitInfo.getExtendInfo()).setGoods(goodsList)
                                .setShippingInfo(shippingInfosList).build();

                        splitPayInfos.add(splitPayInfo);
                        splitCommandInfoList.add(splitCommandInfo);
                    }
                    return splitCommandInfoList;
                }
            } catch (Exception e) {
                LOGGER.error("Error in converting splitSettlement info into splitPayInfo, SplitCommandInfoList : {} ",
                        e);
                throw new PaymentRequestValidationException(e, ResponseConstants.INVALID_REQUEST);
            }
        }
        return null;
    }

    private List<SplitCommandInfo> convertSplitSettlementsDetails(SplitSettlementInfoData splitSettlementInfo,
            VendorParentDetails vendorParentDetails) {
        List<SplitPayInfo> splitPayInfos = new ArrayList<>();
        Map<String, String> paytmAlipayMid = new HashMap<>();
        Map<String, String> partnerIdPaytmMid = new HashMap<>();
        if (vendorParentDetails != null && CollectionUtils.isNotEmpty(vendorParentDetails.getVendorDetails())) {
            try {
                for (VendorDetail vendorDetail : vendorParentDetails.getVendorDetails()) {
                    paytmAlipayMid.put(vendorDetail.getPaytmMerchantId(), vendorDetail.getAlipayMerchantId());
                    partnerIdPaytmMid.put(vendorDetail.getPartnerId(), vendorDetail.getPaytmMerchantId());
                }
                if (splitSettlementInfo.getSplitInfo() != null) {
                    List<SplitCommandInfo> splitCommandInfoList = new ArrayList<>();
                    MultiCurrency splitAmount = null;
                    Money money = null;
                    for (SplitInfoData splitInfo : splitSettlementInfo.getSplitInfo()) {
                        splitAmount = null;
                        money = null;
                        List<Goods> goodsList = SplitSettlementHelper.createGoodsListFromGoodsInfoList(splitInfo
                                .getGoods());
                        List<ShippingInfo> shippingInfosList = SplitSettlementHelper
                                .createFacadeShippingListFromShippingInfoList(splitInfo.getShippingInfo());
                        if (SplitMethod.AMOUNT.equals(splitSettlementInfo.getSplitMethod())) {
                            splitAmount = new MultiCurrency(splitInfo.getAmount().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                            money = new Money(splitInfo.getAmount().getCurrency().getCurrency(),
                                    AmountUtils.getTransactionAmountInPaise(splitInfo.getAmount().getValue()));
                        }

                        String childMid = null;
                        if (StringUtils.isNotBlank(splitInfo.getPartnerId())) {
                            childMid = partnerIdPaytmMid.get(splitInfo.getPartnerId());
                            Map<String, String> extendInfoMap = new HashMap<>();
                            if (splitInfo.getExtendInfo() != null) {
                                extendInfoMap = new ObjectMapper().readValue(splitInfo.getExtendInfo(), Map.class);
                            }
                            extendInfoMap.put(PARTNER_ID, splitInfo.getPartnerId());
                            splitInfo.setExtendInfo(ObjectMapperUtil.getObjectMapper()
                                    .writeValueAsString(extendInfoMap));
                        }

                        SplitPayInfo splitPayInfo = new SplitPayInfo(splitSettlementInfo.getSplitMethod(),
                                paytmAlipayMid.get(childMid), splitAmount, splitInfo.getPercentage());
                        SplitCommandInfo splitCommandInfo = new SplitCommandInfo.SplitCommandInfoBuilder(
                                splitSettlementInfo.getSplitMethod(), paytmAlipayMid.get(childMid), childMid, money,
                                splitInfo.getPercentage()).setExtendInfo(splitInfo.getExtendInfo()).setGoods(goodsList)
                                .setShippingInfo(shippingInfosList).build();

                        splitPayInfos.add(splitPayInfo);
                        splitCommandInfoList.add(splitCommandInfo);
                    }
                    return splitCommandInfoList;
                }
            } catch (Exception e) {
                LOGGER.error("Error in converting splitSettlement info into splitPayInfo, SplitCommandInfoList : {} ",
                        e);
                throw new PaymentRequestValidationException(e, ResponseConstants.INVALID_REQUEST);
            }
        }
        return null;
    }
}
