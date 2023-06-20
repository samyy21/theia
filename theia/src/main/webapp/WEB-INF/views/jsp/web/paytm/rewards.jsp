<div class = 'tab-pane fade <c:if test="${11 == paymentType}">in active</c:if>' id = "rewardsContent">

	<form autocomplete="off" name="rewards-form" method="post" action="submitTransaction" id="rewards">
		<input type="hidden" name="txnMode" value="REWARDS" />
		<input type="hidden" name="txn_Mode" value="REWARDS" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="OTP" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<input type="hidden" name="walletAmount" id="walletAmountRewards" value="0" />
		<c:choose>
			<c:when test="${showRewardsOTP eq 1}">
				<input type="hidden" name="action" value="OTP_INPUT" id="rewardsAction"/>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="action" value="CARD_INPUT" id="rewardsAction"/>
			</c:otherwise>
		</c:choose>
		<c:set var="showRewardsOTP" value="${requestScope.showRewardsOTP}"></c:set>
		<div class="rewards-card-form" <c:if test="${showRewardsOTP eq 1}">style="display:none"</c:if>>
		<ul>
			<li class="rewards-program mb15">
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
			</li>
        	<li>
            	<label>Card Number</label>
            	<p class="cd">
					<input autocomplete="off" type="text" name="cardNumber" class="d cardInput" id="rewardsCardNo" type="text" size="19" maxlength="19" style="width: 278px">
				</p>
                <c:if test="${'11' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD']}</div>
					<script type="text/javascript">
						$("#rewardsCardNo").addClass("error1");
					</script>
				</c:if>
				
			</li>
            <li class="">
                <label>Mobile Number</label>
                <div>
                	<input type="text" name="mobileNo" size="25" maxlength="10" id="rewardsMobile" class="fl"/>
                	<span class="fl rewards-otp-msg">Enter your registered mobile number to get One-time Password (OTP).</span>
                	<div class="clear"></div>
                </div>
                <c:if test="${paymentMode == 'REWARDS' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['MOBILE_NO']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['MOBILE_NO']}</div>
					<script type="text/javascript">
						$("#rewardsMobile").addClass("error1");
					</script>
				</c:if>
            </li>
        </ul>
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
        <p class="clear">
           	<input name="" type="submit" class="gry-btn" value="Check available points" id = "rewardsSubmit">
           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
        </p>
		</div>  
		
		<div class="rewards-otp-form"  <c:if test="${showRewardsOTP ne 1}">style="display:none"</c:if>>
       	<ul>
       		<li>
       			
       		</li>
            <li class="">
                <label>Enter OTP</label>
                <div>
                	<input type="text" name="otp" size="6" maxlength="6" id="rewardsOTP" class="fl"/>
                	<a href="#" class="fl btn-change-rewards-details">Change details</a>
                	<div class="clear"></div>	
                </div>
                <c:if test="${paymentMode == 'REWARDS' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_OTP']}</div>
					<script type="text/javascript">
						$("#rewardsOTP").addClass("error1");
					</script>
				</c:if>
            </li>
		</ul>
		<c:if test="${showRewardsOTP eq 1}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}.
			</div>
		</c:if>
        <p class="clear">
           	<input name="" type="submit" class="gry-btn" value="Proceed Securely" id = "rewardsOTPSubmit">
           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
        </p>
		</div>
        <div class="secure">
        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
        	<span><i>Your card details are secured via 128 Bit encryption by<br/>Verisign</i></span>
        </div>
	</form>
</div>