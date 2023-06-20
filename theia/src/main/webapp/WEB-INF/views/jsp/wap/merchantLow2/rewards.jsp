<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${11 eq paymentType}">
<div class="heading">Rewards</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="REWARDS" />
	<input type="hidden" name="txn_Mode" value="REWARDS" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="OTP" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${isAddMoneyAvailable}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
	<div class="row-dot">
	<c:choose>
			<c:when test="${showRewardsOTP}">
				<input type="hidden" name="action" value="OTP_INPUT" id="rewardsAction"/>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="action" value="CARD_INPUT" id="rewardsAction"/>
			</c:otherwise>
	</c:choose>
	<c:set var="showRewardsOTP" value="${requestScope.showRewardsOTP}"></c:set>
	<div class="rewards-card-form" <c:if test="${showRewardsOTP}">style="display:none"</c:if>>
	<p>
		<select class="selectpicker" id = "rewardsProgram" name="bankCode" data-size="10">
			<option value="-1">Select Reward Program</option>
			<option value="REWARDS">PNB Rewardz</option>
			<option value="REWARDS">Federal Bank Utsav Rewards</option>
			<option value="REWARDS">Union Rewardz</option>
			<option value="REWARDS">Krur Vysya Bank - Anmol Rewardz</option>
		    <option value="REWARDS">CBI Anmol Rewardz</option>
			<option value="REWARDS">BOI - Star Rewardz</option>
			<option value="REWARDS">State Bank Group - Freedom Rewardz</option>
		</select>
	</p>
	<br/>
	<p>
    	<label>Card Number</label><br />
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
    	<input type="text" name="cardNumber" class="mt5 ${cssClass}" id="rewardsCardNo" size="19" maxlength="19">
    	${errorText}
    	
    </p>
    <p class="pt7">
    	<label>Mobile</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['MOBILE_NO']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["MOBILE_NO"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="mobileNo" size="25" maxlength="10" class="mt5 ${cssClass}" id="rewardsMobile"/><br />
        <div class="small mt5">Enter your registered mobile number to get One-time Password (OTP).</div>
		${errorText}
    </p>
		<c:if test="${requestScope.rewardsInsufficientBalance eq 1}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}. You have insufficient points for this order, select another option to pay.
			</div>
		</c:if>
		<c:if test="${requestScope.rewardsInvalidCard eq 1}">
		 	<div class="alert alert-danger fl">
				You have entered invalid card number.
			</div>
		</c:if>
		<c:if test="${requestScope.rewardsInvalidMobile eq 1}">
		 	<div class="alert alert-danger fl">
				Enter valid registered mobile number to get One-time Password (OTP).
			</div>
	     </c:if>
		 <c:if test="${requestScope.rewardsError eq 1}">
		 	<div class="alert alert-danger fl">
				Request failed due to technical error at bank. Retry with different payment mode.
			</div>
		</c:if>
    <p class="pt7 mt5"><input type="submit" value="Check available points" class="blue-btn" /></p> 
    </div>
    
  <div class="rewards-otp-form"  <c:if test="${showRewardsOTP ne 1}">style="display:none"</c:if>>
    <p class="pt7">
    	<label>OTP</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_OTP"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="text" name="otp" id="otp" maxlength="6" class="mt5 ${cssClass}" /><br />
        <div class="small mt5">
        	<a href="javascript:void()" class="btn-change-rewards-details" onclick="showRewardsForm();return false">Change Details</a>
        </div>
        ${errorText}
    </p>
    <c:if test="${showRewardsOTP eq 1}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}.
			</div>
		</c:if>
    <p class="pt7 mt5"><input type="submit" value="${submitBtnText}" class="blue-btn" /></p> 
  </div>
  
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
		_paq.push(['trackEvent', 'Payment Mode', 'rewards']);
	} catch(e){}
</script>
</c:if>
