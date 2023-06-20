<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${6 eq paymentType}">
<div class="heading ml15 mt20">IMPS</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="IMPS" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="OTP" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot ml20 mr20">
	<p>
<!--     	<label>Mobile No.</label><br /> -->
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MOBILE']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_MOBILE"]}</div>' var="errorText"></c:set>
		</c:if>
    	<input type = "text" name="mobileNo" id="mobileNo"  maxlength="10" class="mt5 ${cssClass} all-input" style="margin-bottom: 10px; width: 99%;" placeholder="Enter mobile number" />
    	${errorText}
    	
    </p>
    
    <p class="pt7" style="margin-bottom: 10px; margin-top: 13px;">
<!--     	<label>MMID</label><br /> -->
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_MMID']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_MMID"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="mmid" id="mmid" maxlength="7" class="mt5 ${cssClass} all-input" style="width: 99%;" placeholder="Enter MMID" /><br />
		${errorText}
        <a href="http://www.npci.org.in/merchant.aspx" target="_blank" class = "tips ml5">How to get MMID</a>
    </p>
    
    <p class="pt7" style="margin-bottom: 10px;">
<!--     	<label>OTP</label><br /> -->
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_OTP"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="otp" id="otp" maxlength="8" class="mt5 ${cssClass} all-input" placeholder="Enter OTP" style="width: 99%;"/><br />
        ${errorText}
        <a href="http://www.npci.org.in/merchant.aspx" target="_blank" class = "tips ml5">How to generate OTP</a>
    </p>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
			<div class="post-conv-inclusion">
				<%@ include file="../../common/post-con-withr/imps-postconv.jsp" %>
			</div>
	</c:if>
    <p class="pt7" style="margin-top: 20px; float: left; margin-bottom: 15px;">
    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> 
    	<label for="saveCard" class="saved-card-label">Save this Mobile No. for faster checkout</label>
    </p>
    
    <p class="pt7 clear"><button type="submit" class="blue-btn" >${submitBtnText}</button></p> 
    
    <!--Lock image-->
    <div class="pt7 mt15" style="margin-bottom: 20px;">
    	<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
        <div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        <div class="fl lock-display"> We do not share your card details with anyone</div>
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
