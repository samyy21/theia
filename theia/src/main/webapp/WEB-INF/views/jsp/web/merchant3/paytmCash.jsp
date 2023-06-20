<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="paytmcash-card relative">
	<div class="blur-overlay"></div>
    <div >
		<ul class="grid">
	        <li>
				<div id="pc" class="fl green-tick">
			  		<input type="checkbox" class="pcb checkbox <c:if test="${true eq onlyWalletEnabled}"> f-hide</c:if>" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
				</div>
				<div class="fl ml10 mt6 text-box">
		          	
		          	<c:if test="${true eq onlyWalletEnabled}">
						<label for="pc" class="text mb5  medium sufficentTxt">Awesome! You have sufficient balance for this order</label>
					</c:if>
					<c:if test="${true ne onlyWalletEnabled}">
						<label for="pc" class="text mb5  medium">Pay using Paytm</label>
					</c:if>
		          		
		          		
		          	<div class="clear"></div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	</ul>
	</div>

	<div class="clear"></div>
	<div id="no-walletTextUpdate" class="notification no-walletTextUpdate alert hide fl" style="padding: 4px;font-weight: 400;border:none;color: #222;font-size: 12px;margin-top: 4px;padding-left: 10px;padding-right: 10px;background:#f9ffcf;margin-left: 0px;">You do not have sufficient paytm balance for this transaction</div>
	<div class="clear"></div>
</div>





<div class="card paytmcash-card">
	<div class="blur-overlay"></div>
    <div class="relative">
		<ul class="grid" style="padding-top:3px;">
	        <li id="leftTxt">
				
				<div class="fl  text-box relative">
				<div class="relative">
		          	<div class="bal grey-text medium" id="yourBal" style = "display:none">
		          		  Available Paytm Balance <span class="text b" id="rsdrightAmt">  <span class="${CURRENCY_CLASS}"> ${CURRENCY_TXT}</span> <span class = 'amt'></span>
		          		</span>
		          		
		          	</div>
		          	<div class="clear"></div>
					<div class="bal grey-text medium mt6 hide" id="remBal">Remaining balance <span class="text b"><span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class = 'amt'></span></span></div>
					
				<%--	<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
						<span class="arrow-up subWallets" id="showpaytmWallets"></span>
					</c:if> --%>
				</div>
				
				<%--
				<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
	          			
						<div id="WalletsWEBView">
							<div class="paymentCards hide" >
							
							 <ul class="paymentCradsList">
							 <c:forEach var="subwallets"  varStatus="status" items="${walletInfo.subWalletDetails }">
							 	<li><span class="wallet-icn"><img src="${ptm:stResPath()}images/web/bank/${subwallets.webLogo}" alt="wallet Icons" height="16"/></span>  ${subwallets.displayMessage} <strong><span class="WebRupee"> &#8377 </span>   <fmt:formatNumber value=" ${subwallets.subWalletBalance}" maxFractionDigits="2" /> </strong></li>
							 </c:forEach>
							 </ul>
							 <div class="clear"></div>
							</div>
						    <div class="clear"></div>
					    </div>
			   		</c:if>
					 		
			   		<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
						<div id="WalletsWAPView">
							<div class="paymentCards hide">
							
							 <ul class="paymentCradsList">
							 <c:forEach var="subwallets"  varStatus="status" items="${walletInfo.subWalletDetails }">
							 	<li><span class="wallet-icn"><img src="${ptm:stResPath()}images/web/bank/${subwallets.wapLogo}" alt="wallet Icons" height="16"/></span>  ${subwallets.displayMessage} <strong><span class="WebRupee"> &#8377 </span>   ${subwallets.subWalletBalance} </strong></li>
							 </c:forEach>
							 </ul>
							 <div class="clear"></div>
							</div>
						    <div class="clear"></div>
					    </div>
			   		</c:if>
					--%>
				
				</div>
	        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
	        </li>
	        <li class="fr balance-used-box hide" id="rightTxt">
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
	    <div class="fullWalletDeduct  mt20 relative">
			<c:if test="${walletInfo.areSubWalletsEnabled eq true}">
				<div class="foodWalletBorder clear"></div>
			</c:if>
	    	<c:choose>
	      		<c:when test="${txnInfo.isPaymentOTPRequired eq true}">
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