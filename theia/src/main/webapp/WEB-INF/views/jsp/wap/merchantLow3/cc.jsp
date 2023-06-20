<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${1 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading ml15 mt20">Credit Card</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading ml15 mt20">Credit Card</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()" id="CC-form">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="CC" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${cardInfo.cardStoreMandatory}">
		<input type="hidden" name="storeCardFlag"  value="on" />
	</c:if>
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot ml20 mr20">
	<p>
	
<!--     	<label>Card No.</label><br /> -->
    	<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
		<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BIN_DETAILS']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_BIN_DETAILS"]}</div>' var="errorText"></c:set>
		</c:if>
		<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_INTERNATIONAL_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_INTERNATIONAL_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
		<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARDSCHEME']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARDSCHEME"]}</div>' var="errorText"></c:set>
		</c:if>
		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && txnInfo.txnMode eq 'CC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
		<div class="group-text">
		<input id="card-number" name="cardNumber" type="tel" maxlength="19" class="mt5 ${cssClass} all-input" placeholder="Card Number" value="${!empty cardNumber ? cardNumber : defaultCardNumber}" onkeydown="pressCardSpace(event);onCardDown(true);" onblur="onCardNumberBlur('cc')" style="width: 100%;"/>
		</div>
    	${errorText}
    	<div id="warningDiv" class = "failure clear  low-success-rate" style="display: none; margin: 10px 0px 10px 0;"><span id="errorMsg"></span></div>
    	<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoResponseCode}">
			<c:if test="${ '1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
              		<c:set var="promoError" value="1"/> 
              	</c:if>
			
			<div id="wrong-promo-card-msg" class="error-txt <c:if test='${empty promoError}'>hide</c:if>">
				
				<c:if test='${!empty promoError}'>
					${requestScope.validationErrors['INVALID_PROMO_DETAILS']}
				</c:if> 
			</div>
		</c:if>
    </p>
    <div class="date-cvv-div mt5">
    <p class="pt7">
    <label class="color-label">Expiry / Validity Date</label><br />
    	<div class="fl">
            
            <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
            <c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<c:set value = "error" var="cssClass"></c:set>
				<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
			</c:if>
            <select name="expiryMonth" class="expSelect mt5 ${cssClass} all-input color-fields special-input-select pr15 positionYExpYear" onchange="selectBoxColorChange(this);">
                 <option value="0" class="color-fields">MM</option>
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
            <br/>
            	${errorText}
        </div>
        <div class="fl ml20">
<!--         	<label>Year</label><br /> -->
            <select name="expiryYear" class="expSelect mt5 ${cssClass} all-input color-fields special-input-select pr15 positionYExpYear" onchange="selectBoxColorChange(this);">
               <option value="0" class="color-fields">YY</option>
                   <c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}" ${i == expiryYear ? 'selected="selected"':'' }>${i}</option>
					</c:forEach>
            </select>
        </div>
     
     
    
<!--     	<label>CVV/Security Code</label><br /> -->
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt" style="float:right;margin-right: 60px; clear:both;">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="password" placeholder="CVV" maxlength="4" size="5" name="cvvNumber" style="float: right;margin-right: 14px;font-size: 14px;" class ="ml20 cvv-input ${cssClass} mt7" onkeypress="onKeypressCvvNumber(arguments[0])"/> 
        <img src="/theia/resources/images/wap/merchantLow3/c-v-v-example.png" alt="Security Code" title="Security Code" class="ml50 cvv-after-img" />
        ${errorText}
    </p>
    </div>
   
     <c:if test="${saveCardOption}">
     	<c:choose>
    		<c:when test="${themeInfo.subTheme eq 'ccdc' }">
    			<p class="pt7 mt10" style="padding-top: 20px;">
    				<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked" style="display:none"/>
					<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked" disabled="disabled"/> <label for="saveCard" class="saved-card-label">Save this card for future payments</label>
			    </p>
    		</c:when>
    		<c:when test="${themeInfo.subTheme ne 'ccdc' }">
    			<p class="pt7 mt10 clear" id="ccCardSaved" style="padding-top: 20px;">
			    	<input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" ${txnConfig.saveCardMandatory ? 'disabled="disabled"':''}
			    	 checked="checked"/> <label for="saveCard" class="saved-card-label">Save this card for future payments</label>
			    </p>
    		</c:when>
    	</c:choose>
	    </c:if>
     <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
			<div class="post-conv-inclusion">
				<%@ include file="../../common/post-con-withr/cc-postconv.jsp" %>
			</div>
	</c:if>
    <p class="pt7 " style="margin-top: 22px;">
<%--     	<input type="submit" value="${submitBtnText}" class="blue-btn cc-blue-btn" /> --%>
		<button type="submit" class="blue-btn cc-blue-btn" style="padding-bottom: 15px;">
			${submitBtnText}
		</button>
    </p> 
    
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
	onCardDown(true);
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'cc']);
	} catch(e){}
</script>
</c:otherwise>
</c:choose>
</c:if>
