<div class="clear rowBox">
	
	
	<ul class="grid  fl" style="overflow:visible; width: 100%;">
	  <%-- <c:set var="merchantName" value="${fn:toUpperCase(merchInfo.merchantName)}"></c:set> --%>
	  <c:set var="merchantName" value="${merchInfo.merchantName}"></c:set>
	  
		<c:choose>
			<c:when test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges}">
			<c:set var="paymentLabel" value="Amount to be added :"></c:set>
			</c:when>
			<c:otherwise>
			<c:set var="paymentLabel" value="Total payment to be made :"></c:set>
			</c:otherwise>
			</c:choose>
		
		<li class="merchant-name mr10 titletext lightTxt" >
				<c:if test="${txnInfo.onus}">
						${paymentLabel}
				   </c:if>
				<c:if test="${!txnInfo.onus}">
					<span class="b offus-bold">Select options to pay </span>
					<span class="titletext WebRupee offus-bold" style="margin-left:5px;">Rs</span>
					<span id="totalAmountSpan" class="titletext offus-bold"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span>
					<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
			   </c:if>
			<br><br>
			<%@ include file="cartDetails.jsp" %>
		</li>
      <li class="fr b">
		    <c:if test="${txnInfo.onus}">
				<span class="titletext WebRupee" style="margin-left:5px;">Rs</span>
				<span id="totalAmountSpan" class="titletext"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span>
				<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
			</c:if>
			<c:if test="${!txnInfo.onus}">
					<a href="javascript:void(0)" class="cancel-txn" style="font-family: 'Open Sans';font-weight: 600;font-style: normal;font-stretch: normal;line-height: 1.67;letter-spacing: normal;
							text-align: center;color: #00b9f5;border: 1px solid #00b9f5;border-radius: 3px;padding: 10px 25px; margin-top:2px; display:inline-block;">
						Cancel Payment
					</a>
			</c:if>
      </li>
      <li class="clear"></li>
    </ul>
    <%--<%@ include file="cartDetails.jsp" %>--%>

    <div class="clear"></div>
    <c:set var="irctcData" value="${txnConfig.paymentCharges.CC}"></c:set>
    <fmt:parseNumber var="txnAmountInt" pattern="0.0" type="number" value="${txnInfo.txnAmount}" />
    <fmt:parseNumber var="conveFeeAmountInt" pattern="0.0" type="number" value="${irctcData.feeAmount}" />
    <fmt:parseNumber var="minAmountForAddMoneyVoucher" pattern="0.0" type="number" value="${txnConfig.minAmountForAddMoneyVoucher}" />

	<c:if test="${!empty txnConfig.paymentCharges && conveFeeAmountInt > 0}">
			<div class="topBorderPaytmCC mt20"></div>
	</c:if>
    <c:if test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges && conveFeeAmountInt > 0}">
	<!-- AddMONEY with CONVE FEE -->
	<div id="postConvCharge">
		<c:if test="${txnAmountInt  >=  minAmountForAddMoneyVoucher}">
		You will be charged <strong>Rs. ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of  <strong>Rs. ${irctcData.feeAmount}.</strong> We will send Gift Card of <strong>Rs. ${irctcData.feeAmount}</strong> within <strong>24 hrs</strong> for your next purchase on Paytm. Adding money by Debit Card & Net Banking has no fee.
		</c:if>
		<c:if test="${txnAmountInt  < minAmountForAddMoneyVoucher}">
		You will be charged <strong>Rs. ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of <strong>Rs. ${irctcData.feeAmount}.</strong> Adding money by Debit Card & Net Banking has no fee.
		</c:if>
	
	</div>



	<!-- AddMONEY with CONVE FEE -->
</c:if>   
    <div class="clear"></div>     
</div>