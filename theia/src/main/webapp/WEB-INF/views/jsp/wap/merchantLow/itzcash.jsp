<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${10 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading">Cash Card</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading">Cash Card</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="CASHCARD" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
	
	<p>
		<select id="itzSelect" class="mt5">
	    		<option value="-1">Select</option>
	            <option id="ITZ" value="ITZ"  selected="selected">ITZ CASH</option>
		</select>
	</p>
	<p class="pt7">
    	<label>Itz account no.</label><br />
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_ITZCARD"]}</div>' var="errorText"></c:set>
		</c:if>
    	<input type="text" name="itzCashNumber" size="25" maxlength="12" id="itzCashNumber" class="mt5 ${cssClass}"/>
    	${errorText}
    	
    </p>
    
    <p class="pt7">
    	<label>Password</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PWD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_PWD"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="password" name="itzPwd" maxlength="20" id="itzPwd" size="12" class="mt5 ${cssClass}"/><br />
		${errorText}
    </p>
    
    
    <p class="pt7"><input type="submit" value="${submitBtnText}" class="blue-btn" /></p> 
    
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
		_paq.push(['trackEvent', 'Payment Mode', 'itzcash']);
	} catch(e){}
</script>
</c:otherwise>
</c:choose>
</c:if>
