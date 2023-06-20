<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${2 eq paymentType}">
<div class="heading">Debit Card</div>

<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()" id="DC-form">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="DC" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
	<input type="hidden" name="savedCardId" id="savedCardId" value="" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
	<p class="error">
    	<label>Card No.</label><br />
    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
		
		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && txnInfo.txnMode eq 'DC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
		
		<input id="card-number" type="tel" maxlength="19" name="cardNumber" class="mt5 ${cssClass}" value="${!empty cardNumber ? cardNumber : defaultCardNumber}" onblur="onCardNumberBlur('dc')" onkeydown="onCardDown(false)"/>
		
    	${errorText}
    	
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
    </p>
    
    <p class="pt7">
    	<div class="fl">
            <label>Month</label><br />
             <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
              <c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<c:set value = "error" var="cssClass"></c:set>
				<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
			</c:if>
             <select name="expiryMonth" id="dcMonth" class="expSelect mt5 ${cssClass}">
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
        	<label>Year</label><br />
           <select name="expiryYear" id="dcYear" class="expSelect width mt5 ${cssClass}">
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
    	<label>CVV</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="password" id="normalOptionCVVTxt" maxlength="4" size="5" name="cvvNumber" class ="cvv-input ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/> <img src="${ptm:stResPath()}images/wap/paytmAppLow/cvv.png" alt="CVV" title="CVV" />
        ${errorText}
    </p>
     <p class="small mb10 pt7">
    	If your Maestro Card does not have Expiry Date/CVV, skip these fields
    </p>  
    <%--For ICICI idebit card 
		 check if ICICI idebit is true--%>
<%-- 		<c:if test="${entityInfo.iDebitEnabled}"> --%>
			<div id="idebitPayOption" class="mt20 hide">
            	
                <div class="idebitOption" style="margin-left:0;">
                   	<label>
                   		<input type="radio" class="checkbox idebitDC" name="isIciciIDebit"  value="Y" checked onclick ="optionBasedCVVCheck(true)">
                   			Use ATM PIN
                   	</label>
                </div>
                <div class="idebitOption" >
                	<label>
                		<input type="radio" class="checkbox idebitDC"  name="isIciciIDebit"  value="N" onclick ="optionBasedCVVCheck(true)">
                			Use 3D Secure PIN or OTP
                	</label>
            	</div>
            	<div class = "clear"></div>
            	<p class="pt7 cvv hide" id ="idebitOptionCVV">
			    	<label>CVV</label><br />
			    	<%--Commenting the code for validation error, as in page refresh we don't preserve values
			    		Hence, the error message will always be displayed on the top --%>
			    	<%--<c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
			    	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<c:set value = "error" var="cssClass"></c:set>
						<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
					</c:if>  --%>
			        <input type="password" id="idebitOptionCVVTxt" maxlength="4" size="5" name="cvvNumber" class ="cvv-input ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/> <img src="${ptm:stResPath()}images/wap/paytmAppLow/cvv.png" alt="CVV" title="CVV" />
			        ${errorText}
			    </p>
        	</div>
<%--         </c:if> 	--%>
		<%--End of For ICICI idebit card --%>
    <div id="warningDiv" class = "failure" style="display: none"><span id="errorMsg"></span></div> 
    <c:if test="${saveCardOption}">
	    <p class="pt7">
	    	<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> Save this card for future checkout
	    </p>
    </c:if>
    
    <p class="pt7"><input type="submit" value="${submitBtnText}" class="blue-btn dc-blue-btn" /></p> 
    
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
        <div class="fl small secure-text"> Your card details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
</div>
</form>

<script>
	onCardDown(false);
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'dc']);
	} catch(e){}
</script>
</c:if>
