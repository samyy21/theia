<c:if test="${showQRCode && !loginInfo.loginFlag}">
<div id="scanNpay">
    <div class="scanBox">
        <div class="centerBox">
            <div class="headingData">Scan with Paytm app to pay</div>
            <div class="qrScanImage">
                <img src="data:image/png;base64,${txnInfo.qrDetails.path}" class="barcode" width="150" />
            </div>
            <div class="bottomScanBox">
                <span class="scanRupayIcon"><img src="${ptm:stResPath()}images/web/pay.png"/></span>
                <div class="scanAbbreviations">
                    <div class="b relative">
                        <span class="fs-small">Open Paytm app and tap on pay icon</span>
                        <a href="javascript:void(0)" class="pl5">Learn More</a>
                        <img class="learnmore-image hide" src="${ptm:stResPath()}images/web/scanpay-learnmore.png" width="400" height="400"/>
                    </div>
                    <div class="greyTxt info-txt">If you are not able to scan this QR code please update your Paytm app</div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
        <div class="clear"></div>
    </div>
    <div class="border-right"></div>
    <div class="scanBox" style="padding-top: 12px;">
        <img id="scanPayAuthIframeLoadingId" src="${ptm:stResPath()}images/spinner.gif" class="loading-image"/>
        <span class="or-divide">or</span>
        <iframe id="scanPayAuthLogin" src="" width="100%" height="100%" style="width:100%; box-sizing:border-box; padding-left: 57px;"></iframe>
        <div class="clear"></div>
    </div>
    <div class="clear"></div>
</div>
</c:if>