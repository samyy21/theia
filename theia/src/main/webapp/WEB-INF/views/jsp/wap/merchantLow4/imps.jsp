<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${6 eq paymentType}">
<div class="heading">IMPS</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="IMPS" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="OTP" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
	<p>
    	<label>Mobile No.</label><br />
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_MOBILE"]}</div>' var="errorText"></c:set>
		</c:if>
    	<input type = "text" name="mobileNo" id="mobileNo"  maxlength="10" class="mt5 ${cssClass}" />
    	${errorText}
    	
    </p>
    
    <p class="pt7">
    	<label>MMID</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_MMID"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="mmid" id="mmid" maxlength="7" class="mt5 ${cssClass}" /><br />
		${errorText}
        <a href="http://www.npci.org.in/merchant.aspx" target="_blank" class = "tips">How to get MMID</a>
    </p>
    
    <p class="pt7">
    	<label>OTP</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_OTP"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="otp" id="otp" maxlength="8" class="mt5 ${cssClass}" /><br />
        ${errorText}
        <a href="http://www.npci.org.in/merchant.aspx" target="_blank" class = "tips">How to generate OTP</a>
    </p>
  
    <c:if test="${saveCardOption}">
    <p class="pt7">
    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> Save this Mobile No. for faster checkout
    </p>
    </c:if>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null}">
    	<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
		<div class="post-conv-inclusion" style="display:none;margin-right: 10px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
			<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
		</div>
	 </c:if>
	  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null}">
		<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
		<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
			<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
		</div>
	 </c:if>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.IMPS != null}">
		<div class="post-conv-inclusion" id="normal-post-con" style="margin-right: 10px;">
			<%@ include file="../../common/post-con/imps-postconv.jsp" %>
		</div>
	</c:if>
    <p class="pt7">
    	<button type="submit" id="btnSubmit" class="blue-btn">${submitBtnText}</button>
    </p> 
    
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
        <div class="fl small"> Your payment details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    
</div>
</form>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'imps']);
	} catch(e){}
</script>
</c:if>
