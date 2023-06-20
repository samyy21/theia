<c:if test="${1 eq paymentType}">
<div class="heading">Credit Card</div>
<form autocomplete="off" method="post" action="/payment/request/submit" onsubmit = "submitForm()">
	<input type="hidden" name="txnMode" value="CC" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${cardInfo.cardType}" />
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${isAddMoneyAvailable}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
	<p>
    	<label>Card No.</label><br />
    	<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
		</c:if>
    	<input id="card-number" name="cardNumber" type="tel" maxlength="16" class="mt5 ${cssClass}" onblur="onCardNumberBlur('cc')"/>
    	${errorText}
    	<c:if test="${!empty txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}">
    		<div id="wrong-promo-card-msg" class="error-txt hide">
				${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}
	    	</div>
    	</c:if>
    </p>
    
    <p class="pt7">
    	<div class="fl">
            <label>Month</label><br />
            <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
            <c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<c:set value = "error" var="cssClass"></c:set>
				<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
			</c:if>
            <select name="expiryMonth" class="expSelect mt5 ${cssClass}">
                 <option value="0">MM</option>
                 <option value="01">01</option>
                 <option value="02">02</option>
                 <option value="03">03</option>
                 <option value="04">04</option>
                 <option value="05">05</option>
                 <option value="06">06</option>
                 <option value="07">07</option>
                 <option value="08">08</option>
                 <option value="09">09</option>
                 <option value="10">10</option>
                 <option value="11">11</option>
                 <option value="12">12</option>
            </select>
        </div>
        <div class="fl ml20">
        	<label>Year</label><br />
            <select name="expiryYear" class="expSelect mt5 ${cssClass}">
               <option value="0">YY</option>
                    <option value="2014">2014</option>
                    <option value="2015">2015</option>
                    <option value="2016">2016</option>
                   	<option value="2017">2017</option>
                    <option value="2018">2018</option>
                    <option value="2019">2019</option>
                    <option value="2020">2020</option>
                    <option value="2021">2021</option>
                    <option value="2022">2022</option>
                   	<option value="2023">2023</option>
                   	<option value="2024">2024</option>
                  	<option value="2025">2025</option>
                    <option value="2026">2026</option>
                    <option value="2027">2027</option>
                    <option value="2028">2028</option>
                    <option value="2029">2029</option>
                    <option value="2030">2030</option>
                    <option value="2031">2031</option>
                    <option value="2032">2032</option>
                    <option value="2033">2033</option>
                    <option value="2034">2034</option>
                    <option value="2035">2035</option>
                    <option value="2036">2036</option>
                    <option value="2037">2037</option>
                    <option value="2038">2038</option>
                    <option value="2039">2039</option>
                    <option value="2040">2040</option>
                    <option value="2041">2041</option>
                    <option value="2042">2042</option>
                    <option value="2043">2043</option>
                    <option value="2044">2044</option>
                    <option value="2045">2045</option>
                    <option value="2046">2046</option>
                    <option value="2047">2047</option>
                    <option value="2048">2048</option>
                    <option value="2049">2049</option>
            </select>
        </div>
        <div class="clear"></div>
        ${errorText}
    </p>
    
    <p class="pt7 cvv">
    	<label>CVV/Security Code</label><br />
    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
    	<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
			<c:set value = "error" var="cssClass"></c:set>
			<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
		</c:if>
        <input type="password" maxlength="4" size="5" name="cvvNumber" class = "${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/> <img src="/images/wap/paytmAppLow/security-code.png" alt="Security Code" title="Security Code" />
        ${errorText}
    </p>
    <c:if test="${saveCardOption }">
	    <p class="pt7">
	    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> Save this card for future checkout
	    </p>
    </c:if>
    
    <p class="pt7 "><input type="submit" value="${submitBtnText}" class="blue-btn" /></p> 
    
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="/images/wap/paytmAppLow/lock.png" alt="" title="" /></div>
        <div class="fl small"> Your card details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    
</div>
</form>
</c:if>