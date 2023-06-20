<div data-role="collapsible" id="rewards" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>Rewards</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<form autocomplete="off" name="rewards-form" method="post" action="/payment/request/submit" id="rewards" data-ajax="false">
			<input type="hidden" name="txnMode" value="REWARDS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
			<input type="hidden" name="walletAmount" id="walletAmountCC" value="0" />
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
			<p>
				<div class="ui-select">
				<div data-corners="true" data-shadow="true" data-iconshadow="true" data-wrapperels="span" data-icon="dwn-arw" data-iconpos="right" data-theme="c" class="ui-btn ui-shadow ui-btn-corner-all ui-btn-icon-right ui-btn-up-c">
					<span class="ui-btn-inner">
						<span class="ui-btn-text"><span>Select Reward Program</span></span>
						<span class="ui-icon ui-icon-dwn-arw ui-icon-shadow">&nbsp;</span>
					</span>
					<select class="" id = "rewardsProgram" name="bankCode" data-size="10">
						<option value="-1">Select Reward Program</option>
						<option value="REWARDS">PNB Rewardz</option>
						<option value="REWARDS">Federal Bank Utsav Rewards</option>
						<option value="REWARDS">Union Rewardz</option>
						<option value="REWARDS">Krur Vysya Bank - Anmol Rewardz</option>
						<option value="REWARDS">CBI Anmol Rewardz</option>
						<option value="REWARDS">BOI - Star Rewardz</option>
						<option value="REWARDS">State Bank Group - Freedom Rewardz</option>
					</select>
				</div>
				</div>
			</p>
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input autocomplete="off" type="text" name="cardNumber" class="ui-input-text ui-body-c" id="rewardsCardNo" size="19" maxlength="19" placeholder="Card Number">
				</div>
				<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<div class="error">${requestScope.validationErrors['INVALID_CARD']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#rewardsCardNo").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
			
			
			<p>
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
					<input type="text" name="mobileNo" size="25" maxlength="10" class="ui-input-text ui-body-c" id="rewardsMobile" placeholder="Mobile" />
				</div>
				<div class="rewards-msg">
			 		Enter valid registered mobile number to get One-time Password (OTP).
			 	</div>
		        <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['MOBILE_NO']}">
					<div class="error">${requestScope.validationErrors['MOBILE_NO']}</div>
					<script type="text/javascript">
						$(document).ready(function() {
							$("#rewardsMobile").parent().addClass("error");
						});
					</script>
				</c:if>
			</p>
			<c:if test="${requestScope.rewardsInsufficientBalance}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}. You have insufficient points for this order, select another option to pay.
			</div>
			</c:if>
			<c:if test="${requestScope.rewardsInvalidCard}">
		 	<div class="alert alert-danger fl">
				You have entered invalid card number.
			</div>
			</c:if>
			<c:if test="${requestScope.rewardsInvalidMobile}">
		 	<div class="alert alert-danger fl">
				Enter valid registered mobile number to get One-time Password (OTP).
			</div>
			</c:if>
			 <c:if test="${requestScope.rewardsError}">
		 	<div class="alert alert-danger fl">
				Request failed due to technical error at bank. Retry with different payment mode.
			</div>
		</c:if>
			  <div class="load-btn">
	  			<input type="submit"  class="submitButton" value="Check available points" data-icon="ldr" data-iconpos="right">
	  		</div>
			</div>
			<div class="rewards-otp-form"  <c:if test="${showRewardsOTP ne 1}">style="display:none"</c:if>>
			<p>
			 	<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
			 		<input type="tel" name="otp" id="otp" placeholder="OTP" maxlength="6" class="ui-input-text ui-body-c"/>
			 	</div>
			 	<div class="rewards-msg">
			 		<a href="#" class="btn-change-rewards-details">Change details</a>
			 	</div>
		         <c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
					<div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#otp").parent().addClass("error");
						});
					</script>
		 		</c:if>
			 </p>
	 		<c:if test="${showRewardsOTP eq 1}">
			<div class="alert alert-info fl">
			Available Points ${requestScope.rewardsBalancePoints}. Cash redeemable ${requestScope.rewardsBalanceAmount}.
			</div>
			</c:if>
		 	<div class="load-btn">
	  			<input type="submit"  class="submitButton" value="Proceed securely" data-icon="ldr" data-iconpos="right">
	  		</div>
			 </div>
		</form>
	</div>
</div>