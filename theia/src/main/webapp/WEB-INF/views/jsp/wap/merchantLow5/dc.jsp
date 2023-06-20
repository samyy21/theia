<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${2 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
	<div class="heading">Debit Card</div>
		<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading">Debit Card</div>

<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm(this)" id="DC-form">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" id="DCtxnMode" name="txnMode" value="DC" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
	<input type="hidden" name="savedCardId" id="savedCardId" value="" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${cardInfo.cardStoreMandatory}">
		<input type="hidden" name="storeCardFlag"  value="on" />
	</c:if>
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
	<p class="error">
    	<label for="cardNumber">Card No.</label><br />
    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
		
		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && txnInfo.txnMode eq 'DC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
		
		<input id="card-number" type="tel" maxlength="23" name="cardNumber" class="mt5 ${cssClass}" value="${!empty cardNumber ? cardNumber : defaultCardNumber}" onblur="onCardNumberBlur('dc')" onkeydown="pressCardSpace(event);" onkeyup="onCardDown(false,event); removeFieldError(null, this)"/>

    	${errorText}
    	 <div id="warningDiv" class = "clear failure" style="display: none; width: 80%; margin-left: 0;"><span id="errorMsg"></span></div>
		<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoResponseCode}">
			<c:if test="${ '2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
              		<c:set var="promoError" value="1"/> 
              	</c:if>
			
			<div id="wrong-promo-card-msg" class="error-txt <c:if test='${empty promoError}'>hide</c:if>">
				
				<c:if test='${!empty promoError}'>
					${requestScope.validationErrors['INVALID_PROMO_DETAILS']}
				</c:if> 
			</div>
		</c:if>
	<div id="luhnError" class="luhnError hide error">Please enter a valid card number</div>
    </p>
	<div id="cvv_expiry_block">
    <p class="pt7">
    	<div class="fl">
            <label for="expiryMonth">Month</label><br />
             <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
              <c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<c:set value = "error" var="cssClass"></c:set>
				<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
			</c:if>
             <select name="expiryMonth" id="dcMonth" class="expSelect mt5 ${cssClass}" onchange="removeFieldError(null, this)">
	                <option value="0">MM</option>
                    <option value="01" ${01 == expiryMonth ? 'selected="selected"':'' }>01</option>
                    <option value="02" ${02 == expiryMonth ? 'selected="selected"':'' }>02</option>
                    <option value="03" ${03 == expiryMonth ? 'selected="selected"':'' }>03</option>
                    <option value="04" ${04 == expiryMonth ? 'selected="selected"':'' }>04</option>
                    <option value="05" ${05 == expiryMonth ? 'selected="selected"':'' }>05</option>
                    <option value="06" ${06 == expiryMonth ? 'selected="selected"':'' }>06</option>
                    <option value="07" ${07 == expiryMonth ? 'selected="selected"':'' }>07</option>
                    <option value="08" ${08 == expiryMonth ? 'selected="selected"':'' }>08</option>
                    <option value="09" ${08 == expiryMonth ? 'selected="selected"':'' }>09</option>
                    <option value="10" ${10 == expiryMonth ? 'selected="selected"':'' }>10</option>
                    <option value="11" ${11 == expiryMonth ? 'selected="selected"':'' }>11</option>
                    <option value="12" ${12 == expiryMonth ? 'selected="selected"':'' }>12</option>
              	</select>
        </div>
        <div class="fl ml20">
        	<label for="expiryYear">Year</label><br />
           <select name="expiryYear" id="dcYear" class="expSelect width mt5 ${cssClass}" onchange="removeFieldError(null, this)">
            		<option value="0">YY</option>
                   <c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}" ${i == expiryYear ? 'selected="selected"':'' }>${i}</option>
					</c:forEach>
            	</select>
        </div>
        <div class="clear"></div>
        ${errorText}
    </p>
    
    <p class="pt7 cvv" id ="normalOptionCVV">
    	<label for="cvvNumber">CVV</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="tel" id="normalOptionCVVTxt" maxlength="4" size="5" name="cvvNumber" class ="mask cvv-input ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0]); removeFieldError(null, this)"/> <img id="defaultCvvIcon" src="${ptm:stResPath()}images/wap/paytmAppLow/cvv.png" alt="CVV" title="CVV" /><img id="amexCvvIcon" class="hide" src="${ptm:stResPath()}images/wap/paytmAppLow/security-code.png" alt="Security Code" title="Amex Cvv" />
        ${errorText}
    </p>
     <p class="small mb10 hide pt7" id="maestro_msg">
    	If your Maestro Card does not have Expiry Date/CVV, skip these fields
    </p> 
    
	</div>
    <%--For ICICI idebit card 
		 check if ICICI idebit is true--%>
<%-- 		<c:if test="${entityInfo.iDebitEnabled}"> --%>
			
			<div id="idebitPayOption" class="mt20 hide">
            <div class="line-border" style="margin-left: 0; margin-top: 10px;"></div>	
                <div class="idebitOption" style="margin-left:0;margin-top: 10px;">
                   	<label>
                   		<input type="radio" class="checkbox idebitDC fl" name="isIciciIDebit"  value="Y" checked onclick ="optionBasedCVVCheck(true)">
                   			<span class ="fl" style="margin-left: 3px;">Use ATM PIN</span>
                   			<span class="idebit-help small">Enter your ATM PIN in the next step</span>
                   	</label>
                </div>
                <div class="clear"></div>
                <div class="idebitOption" >
                	<label>
                		<input type="radio" class="checkbox idebitDC fl"  name="isIciciIDebit"  value="N" onclick ="optionBasedCVVCheck(true)">
                			<span class ="fl" style="margin-left: 3px;">Use 3D Secure PIN or OTP</span>
                			<span class="idebit-help small">Enter 3D secure PIN/OTP in the next step</span>
                	</label>
            	</div>
            	<div class = "clear"></div>
            	<p class="pt7 cvv hide" id ="idebitOptionCVV">
			    	<label for="cvvNumber">CVV</label><br />
			    	<%--Commenting the code for validation error, as in page refresh we don't preserve values
			    		Hence, the error message will always be displayed on the top --%>
			    	<%--<c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
			    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<c:set value = "error" var="cssClass"></c:set>
						<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
					</c:if>  --%>
			        <input type="tel" id="idebitOptionCVVTxt" maxlength="4" size="5" name="cvvNumber" class ="mask cvv-input ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0]); removeFieldError(null, this)"/><img id="defaultCvvIcon" src="${ptm:stResPath()}images/wap/paytmAppLow/cvv.png" alt="CVV" title="CVV" /><img id="amexCvvIcon" class="hide" src="${ptm:stResPath()}images/wap/paytmAppLow/security-code.png" alt="Security Code" title="Amex Cvv" />
				${errorText}
			    </p>
        	</div>
<%-- --         </c:if> -- --%>
		<%--End of For ICICI idebit card --%>
   
     <div class="clear"></div>
    <c:if test="${saveCardOption}">
    	<c:choose>
    		<c:when test="${themeInfo.subTheme eq 'ccdc' }">
    			<p class="pt7">
    			<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y"
			    	 checked="checked" style="display:none"/>
			    	<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y"
					 checked="checked" disabled="disabled"/>

					 <c:if test="${isSubscriptionFlow eq true}">
					Card details will be saved for renewal transactions.
			</c:if>
			<c:if test="${isSubscriptionFlow eq false}">

						Save this card for faster checkout
				</c:if>
			    </p>
    		</c:when>
    		<c:when test="${themeInfo.subTheme ne 'ccdc' }">
    			<p class="pt7" id="dcCardSaved">
			    	<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y"
			    		${txnConfig.saveCardMandatory ? 'disabled="disabled"':''}
					 checked="checked"/>



					 <c:if test="${isSubscriptionFlow eq true}">
					Card details will be saved for renewal transactions.
			</c:if>
			<c:if test="${isSubscriptionFlow eq false}">

						Save this card for faster checkout
				</c:if>

					<c:if test="${isSubscription eq true}">
						<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked" style="display:none"/>
					</c:if>
			    </p>
    		</c:when>
    	</c:choose>
	    
    </c:if>
     <%--post convenince related --%>
      <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null &&  txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
     		 <c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" style="display:none;margin-right: 10px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
			</div>
	  </c:if>
	   <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null &&  txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
			<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
			</div>
	 </c:if>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.DC != null &&  txnConfig.paymentCharges.DC.totalConvenienceCharges ne '0.00'}">
			<div class="post-conv-inclusion" style="margin-right: 10px;" id="normal-post-con">
				<%@ include file="../../common/post-con/dc-postconv.jsp" %>
			</div>
	</c:if>

	<div class="card-bin-disable mt10 hide clear">
		<div class="card-bin-disable-error-msg mt5"></div>
	</div>

    <p class="pt7">
    	<button type="submit" id="btnSubmit" class="blue-btn dc-blue-btn"  data-txnmode="DC" onclick="pushGAData(this, 'pay_now_clicked')" >Pay Now</button>
    </p> 

</div>
</form>

<script>
    onCardDown(false);
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'dc']);
	} catch(e){}
</script>
</c:otherwise>
</c:choose>
</c:if>
