package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.response.BaseResponseBody;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchQRPaymentDetailsResponseBody extends BaseResponseBody {

    private final static long serialVersionUID = -5902112396707061221L;

    private QRCodeInfoBaseResponse qrCodeInfoBaseResponse;

    private NativeCashierInfoResponseBody nativeCashierInfoResponseBody;

    private Object channelPaymentDetails;

    @JsonGetter(value = "qrInfo")
    public QRCodeInfoBaseResponse getQrCodeInfoBaseResponse() {
        return qrCodeInfoBaseResponse;
    }

    public void setQrCodeInfoBaseResponse(QRCodeInfoBaseResponse qrCodeInfoBaseResponse) {
        this.qrCodeInfoBaseResponse = qrCodeInfoBaseResponse;
    }

    @JsonGetter(value = "paymentOptions")
    public NativeCashierInfoResponseBody getNativeCashierInfoResponse() {
        return nativeCashierInfoResponseBody;
    }

    public void setNativeCashierInfoResponse(NativeCashierInfoResponseBody nativeCashierInfoResponseBody) {
        this.nativeCashierInfoResponseBody = nativeCashierInfoResponseBody;
    }

    @JsonGetter(value = "channelPaymentDetails")
    public Object getChannelPaymentDetails() {
        return channelPaymentDetails;
    }

    public void setChannelPaymentDetails(Object channelPaymentDetails) {
        this.channelPaymentDetails = channelPaymentDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchQRPaymentDetailsResponseBody{");
        sb.append("qrInfo=").append(qrCodeInfoBaseResponse);
        sb.append(", merchantPayOption=").append(nativeCashierInfoResponseBody);
        sb.append('}');
        return sb.toString();
    }

}
