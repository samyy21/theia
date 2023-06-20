<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${!txnInfo.onus}">
<div class="logo" style="background: #ffffff;">
    <div id="headerIdea">
        <div class="container">
            <div class="fl">
                <c:if test="${themeInfo.subTheme eq 'bigBasket'}">
                    <img src="${ptm:stPath()}merchantLogo/bigBasket.png" style="margin:8px 0px;" alt="${merchInfo.merchantName}" height="35" />
                </c:if>
                <c:if test="${themeInfo.subTheme ne 'bigBasket'}">
                    <c:choose>
                        <c:when test="${merchInfo.useNewImagePath eq true}">
                            <img src="${merchInfo.merchantImage}" style="margin:8px 0px;" alt="${merchInfo.merchantName}" height="35" />
                        </c:when>
                        <c:otherwise>
                            <img src="${ptm:stPath()}merchantLogo/${merchInfo.merchantImage}" style="margin:8px 0px;" alt="${merchInfo.merchantName}" height="35" />
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>

            <div class="row fr" style="text-align: center;
    font-size: 18px;
    font-weight: bold;
    line-height: 27px; border-bottom: #bfbfbf solid 1px;">
                <c:choose>
                    <c:when test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges}">
                        <div class="fl">Amount to be added</div>
                    </c:when>
                    <c:otherwise>
                        <%--<div class="fl">Total amount to be paid</div>--%>
                        <span >Amount:</span>
                    </c:otherwise>
                </c:choose>
                <span ><span class="WebRupee">Rs</span> <span id = "totalAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span></span>
                <div class="clear"></div>
                <div class="clear" style="font-size: 11px;
    color: #666;
    font-weight: normal;
    margin-top: -6px;">
                    <span >OrderId: ${txnInfo.orderId}</span>

                </div>

            </div>
            <%--<div class="fr">--%>
                <%--<img src="${ptm:stResPath()}images/web/paytm_logo.png" style="margin:16px 0px;" alt="" height="27" />--%>
            <%--</div>--%>
            <div class="clear"></div>
        </div>
        <div class="clear"></div>
    </div>
</div>
</c:if>