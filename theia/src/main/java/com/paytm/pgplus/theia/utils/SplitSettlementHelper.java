package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.enums.EnumCurrency;
import com.paytm.pgplus.facade.acquiring.models.Goods;
import com.paytm.pgplus.facade.acquiring.models.SplitCommandInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SplitSettlementHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitSettlementHelper.class);

    public static SplitSettlementInfoData convertSplitCommandInfoListToSplitSettlementInfoData(
            List<SplitCommandInfo> splitCommandInfoList) {
        SplitSettlementInfoData splitSettlementInfoData = new SplitSettlementInfoData();
        splitSettlementInfoData.setSplitMethod(splitCommandInfoList.get(0).getSplitMethod());
        List<SplitInfoData> splitInfoDataList = new ArrayList<>();
        for (SplitCommandInfo splitCommandInfo : splitCommandInfoList) {
            SplitInfoData splitInfoData = new SplitInfoData();
            splitInfoData.setMid(splitCommandInfo.getPaytmMerchantId());
            splitInfoData.setExtendInfo(splitCommandInfo.getExtendInfo());
            if (splitCommandInfo.getAmount() != null) {
                splitInfoData.setAmount(new Money(EnumCurrency.getEnumByCurrency(splitCommandInfo.getAmount()
                        .getCurrency().getCurrency()), AmountUtils.getTransactionAmountInRupee(splitCommandInfo
                        .getAmount().getValue())));
            } else {
                splitInfoData.setPercentage(splitCommandInfo.getPercentage());
            }
            splitInfoData.setGoods(convertGoodsToGoodsInfoList(splitCommandInfo.getGoods()));
            splitInfoData.setShippingInfo(convertFacadeShippingInfoToShippingInfoList(splitCommandInfo
                    .getShippingInfo()));
            splitInfoDataList.add(splitInfoData);
        }
        splitSettlementInfoData.setSplitInfo(splitInfoDataList);

        return splitSettlementInfoData;
    }

    public static List<ShippingInfo> convertFacadeShippingInfoToShippingInfoList(
            List<com.paytm.pgplus.facade.acquiring.models.ShippingInfo> shippingInfoList) {
        List<ShippingInfo> returnShippingInfoList = null;
        if (CollectionUtils.isNotEmpty(shippingInfoList)) {
            returnShippingInfoList = new ArrayList<>(shippingInfoList.size());
            for (com.paytm.pgplus.facade.acquiring.models.ShippingInfo shippingInfo : shippingInfoList) {
                ShippingInfo returnShippingInfo = new ShippingInfo();
                returnShippingInfo.setAddress1(shippingInfo.getAddress1());
                returnShippingInfo.setAddress2(shippingInfo.getAddress2());
                returnShippingInfo.setCarrier(shippingInfo.getCarrier());
                returnShippingInfo.setChargeAmount(new Money(EnumCurrency.getEnumByCurrency(shippingInfo
                        .getChargeAmount().getCurrency().getCurrency()), shippingInfo.getChargeAmount().getValue()));
                returnShippingInfo.setCityName(shippingInfo.getCityName());
                returnShippingInfo.setCountryName(shippingInfo.getCountryName());
                returnShippingInfo.setEmail(shippingInfo.getEmail());
                returnShippingInfo.setFirstName(shippingInfo.getFirstName());
                returnShippingInfo.setLastName(shippingInfo.getLastName());
                returnShippingInfo.setMerchantShippingId(shippingInfo.getMerchantShippingId());
                returnShippingInfo.setMobileNo(shippingInfo.getMobileNo());
                returnShippingInfo.setStateName(shippingInfo.getStateName());
                returnShippingInfo.setTrackingNo(shippingInfo.getTrackingNo());
                returnShippingInfo.setZipCode(shippingInfo.getZipCode());
                returnShippingInfoList.add(returnShippingInfo);
            }
        }
        return returnShippingInfoList;
    }

    public static List<GoodsInfo> convertGoodsToGoodsInfoList(List<Goods> goodsList) {
        List<GoodsInfo> returnGoodsInfoList = null;
        if (CollectionUtils.isNotEmpty(goodsList)) {
            returnGoodsInfoList = new ArrayList<>(goodsList.size());
            for (Goods goods : goodsList) {
                GoodsInfo goodsInfo = new GoodsInfo();
                goodsInfo.setDescription(goods.getDescription());
                goodsInfo.setCategory(goods.getCategory());
                goodsInfo.setMerchantGoodsId(goods.getMerchantGoodsId());
                goodsInfo.setMerchantShippingId(goods.getMerchantShippingId());
                goodsInfo.setPrice(new Money(EnumCurrency.getEnumByCurrency(goods.getPrice().getCurrency()
                        .getCurrency()), goods.getPrice().getValue()));
                goodsInfo.setQuantity(goods.getQuantity());
                goodsInfo.setSnapshotUrl(goods.getSnapshotUrl());
                goodsInfo.setUnit(goods.getUnit());
                try {
                    goodsInfo.setExtendInfo(JsonMapper.mapJsonToObject(goods.getExtendInfo(), ExtendInfo.class));
                } catch (FacadeCheckedException e) {
                    LOGGER.info("Exception in convertGoodsToGoodsInfoList: {}", e);
                }
                returnGoodsInfoList.add(goodsInfo);
            }
        }
        return returnGoodsInfoList;
    }

    public static List<Goods> createGoodsListFromGoodsInfoList(List<GoodsInfo> goods) throws FacadeCheckedException {
        List<Goods> goodsList = null;
        if (CollectionUtils.isNotEmpty(goods)) {
            goodsList = new ArrayList<>(goods.size());
            for (GoodsInfo good : goods) {
                Goods.GoodsBuilder builder = new Goods.GoodsBuilder(good.getDescription(),
                        new com.paytm.pgplus.facade.common.model.Money(good.getPrice().getCurrency().getCurrency(),
                                good.getPrice().getValue()), good.getQuantity());
                Goods goodsObject = builder.merchantGoodsId(good.getMerchantGoodsId())
                        .merchantShippingId(good.getMerchantShippingId()).category(good.getCategory())
                        .unit(good.getUnit()).snapshotUrl(good.getSnapshotUrl())
                        .extendInfo(JsonMapper.mapObjectToJson(good.getExtendInfo())).build();
                goodsList.add(goodsObject);
            }
        }
        return goodsList;
    }

    public static List<com.paytm.pgplus.facade.acquiring.models.ShippingInfo> createFacadeShippingListFromShippingInfoList(
            List<com.paytm.pgplus.models.ShippingInfo> shippingInfos) throws FacadeCheckedException {
        List<com.paytm.pgplus.facade.acquiring.models.ShippingInfo> shippingInfoList = null;
        if (CollectionUtils.isNotEmpty(shippingInfos)) {
            shippingInfoList = new ArrayList<com.paytm.pgplus.facade.acquiring.models.ShippingInfo>(
                    shippingInfos.size());
            for (com.paytm.pgplus.models.ShippingInfo shippingInfo : shippingInfos) {
                com.paytm.pgplus.facade.acquiring.models.ShippingInfo.ShipingInfoBuilder builder = new com.paytm.pgplus.facade.acquiring.models.ShippingInfo.ShipingInfoBuilder(
                        shippingInfo.getMerchantShippingId(), shippingInfo.getCountryName(),
                        shippingInfo.getStateName(), shippingInfo.getCityName(), shippingInfo.getAddress1(),
                        shippingInfo.getFirstName(), shippingInfo.getLastName(), shippingInfo.getZipCode());
                builder.trackingNo(shippingInfo.getTrackingNo());
                builder.carrier(shippingInfo.getCarrier());
                builder.chargeAmount(new com.paytm.pgplus.facade.common.model.Money(shippingInfo.getChargeAmount()
                        .getCurrency().getCurrency(), shippingInfo.getChargeAmount().getValue()));
                builder.address2(shippingInfo.getAddress2());
                builder.mobileNo(shippingInfo.getMobileNo());
                builder.email(shippingInfo.getEmail());
                com.paytm.pgplus.facade.acquiring.models.ShippingInfo shippingInfoObject = builder.build();
                shippingInfoList.add(shippingInfoObject);
            }
        }
        return shippingInfoList;
    }

    public static SplitSettlementInfo createSplitSettlementInfoFromSplitSettlementInfoData(
            SplitSettlementInfoData splitSettlementInfoData) {
        if (splitSettlementInfoData != null) {
            SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo();
            splitSettlementInfo.setSplitMethod(splitSettlementInfoData.getSplitMethod());
            List<SplitInfoData> splitInfoDataList = splitSettlementInfoData.getSplitInfo();
            if (splitInfoDataList != null && splitInfoDataList.size() > 0) {
                List<SplitInfo> splitInfoList = new ArrayList<>(splitSettlementInfoData.getSplitInfo().size());
                for (SplitInfoData splitInfoData : splitInfoDataList) {
                    SplitInfo splitInfo = new SplitInfo();
                    splitInfo.setAmount(splitInfoData.getAmount());
                    splitInfo.setMid(splitInfoData.getMid());
                    splitInfo.setPercentage(splitInfoData.getPercentage());
                    splitInfoList.add(splitInfo);
                }
                splitSettlementInfo.setSplitInfo(splitInfoList);
            }
            return splitSettlementInfo;
        }
        return null;
    }
}
