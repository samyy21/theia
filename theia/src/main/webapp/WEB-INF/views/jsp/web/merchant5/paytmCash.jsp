<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="card paytmcash-card">
	<div class="blur-overlay"></div>
    <div >
		<ul class="grid">
	        <li>
				<div id="pc" class="fl green-tick">
			  		<input type="checkbox" class="pcb checkbox" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
				</div>
				<div class="fl ml10 text-box">
		          	<label for="pc" class="text mb5 mt6 b">Paytm</label>
		          	<div class="bal grey-text small" id="yourBal" style = "display:none">
		          		 Available Balance
		          		<span class="text b">
		          			<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span>
		          		</span>
		          		
		          	</div>
		          	<div class="clear"></div>
					<div class="bal grey-text small mt6 hide" id="remBal">Remaining balance <span class="text b"><span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span></span></div>
				</div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	        <li class="fr balance-used-box hide">
	        	<div class="large b mt6">
	        		 <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
	        		<span id="balance-used"></span>
	        	</div>
	        </li>
      	</ul>
	</div>

    <div class="clear"></div>
    
    <form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden"  name="txnMode" value="PPI" />
		<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
		<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
	    <div class="fullWalletDeduct ml50 mt20">
	    	<c:choose>
	      		<c:when test="${sessionScope.isPaymentOtpRequired eq 'Y'}">
		    	<div class="forOtp mt20" >
		    		<div class="row">
		    			<h4 class="fl mt20">Enter OTP</h4>
		    			<input type="text" name="otp" class="text-input lg-input" id="otpno" maxlength="6" style="margin-left:20px;" />
		    			<p style="margin-left:262px; display:none;" class="mt10 errorlbl"><span class="red-text">Enter Valid OTP</span></p>
		    		</div>
		    		 <div class="clear"></div>
		    		<div class="row mt10">
		    		<c:set var="otpuserMobNo" value="******${fn:substring(loginInfo.user.mobileNumber, 6, 10)}"></c:set>
			    		<p class="fl">One Time Password (OTP) has been sent to mobile ${otpuserMobNo }.<br>
			    		please enter and click on pay now to complete payment.</p>
			    		<a href="javascript:void(0)" class="fr link blueLink" id="resendOtp">Resend OTP?</a>
		    		</div>
		    		
		    		<div class="btn-submit fl" style="margin-left:130px;">
						<input class="btn-normal" type="submit" id="otpsubmit" value="Pay now" name="">
					</div>

					<div class="clear"></div>
		    		
		    	</div>
	    	</c:when>
	    	<c:otherwise>
		    	<div class="btn-submit fl">
					<input class="btn-normal" type="submit" value="Pay now" name="">
				</div>

				<div class="clear"></div>
			</c:otherwise>
			</c:choose>
	    </div>
    </form>
    <div class="clear"></div>
</div>