package com.paytm.pgplus.biz.core.wallet.service;

import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowTransactionBean;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

public interface IWalletQRCodeService {

    GenericCoreResponseBean<QRCodeDetailsResponse> fetchQRCodeDetails(WorkFlowTransactionBean workFlowTransBean);

    QRCodeInfoBaseResponse getQRCodeInfoByQrCodeId(String qrCodeId);

    String getQRDisplayNameByQrCodeId(String qrCodeId);

}
