<div class = 'tab-pane fade <c:if test="${1 == paymentType}">in active</c:if>' id = "ccContent">
	<h3> Enter credit card details</h3>
	<form autocomplete="off" name="creditcard-form" method="post" action="submitTransaction" id="card">
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="txn_Mode" value="CC" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<input type="hidden" name="walletAmount" id="walletAmountCC" value="0" />
		<ul>
        	<li>
            	<label>Card Number</label>
            	<p class="cd">
					<input autocomplete="off" type="text" name="" data-promocodes="${sessionScope.promoBinList}" class="c cardInput" id="cn" data-type="cc" type="text" size="16" maxlength="19" style="width: 278px">
					<input type="hidden" name="cardNumber" value="">
				</p>
               <c:if test="${promocodeType  eq 'DISCOUNT' && '1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
               	<c:set var="promoError" value="1"/> 
               </c:if>	
                <div class="error clear promo-code-error" style="width:250px;<c:if test='${empty promoError}'>display:none</c:if>">
					${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg} 
				</div>
                <c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD']}</div>
					<script type="text/javascript">
						$("#cn").addClass("error1");
					</script>
				</c:if>
				
			</li>
			
            <li class="ml10">
     	    	<label for = "ccExpMonth">Expiry Date</label>
               	<div class="mt5">
               	 	<div class="fl" id = "ccExpMonthWrapper">
	                	<select class="combobox" id="ccExpMonth" name="expiryMonth" style="width: 80px;">
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
               		
               		<div class="fl ml10" id = "ccExpYearWrapper">
               			<select class="combobox" id="ccExpYear" name="expiryYear" style="width: 80px;">
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
               </div>
                <c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
					<script type="text/javascript">
						$("#ccExpMonthWrapper").addClass("error1");
						$("#ccExpYearWrapper").addClass("error1");
					</script>
				</c:if>
            </li>
            
            <li class="ml10" id = "ccCvvWrapper">
                <label>CVV/Security Code</label>
                <p> 
                	<%-- dummy input to trick browser to not save cc number --%> 
                	<input type="text" name="" class="hide" autocomplete="off">
                	<input class="width40" autocomplete="off" type="password" name="cvvNumber" id="ccCvvBox" maxlength="4">
                </p>
                <c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
					<div class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CVV']}</div>
					<script type="text/javascript">
						$("#ccCvvBox").addClass("error1");
					</script>
				</c:if>
                <div class="clear"></div>
                <a href="#" id="clueTipBoxCC" class="clueTipBox" title="The last 3 digit printed on the <br />signature panel on the back<br /> of your credit card.|"></a>
                <a href="#" id="clueTipBoxAmex" class="clueTipBox" title="Four digits code printed at <br/>top of Amex logo on your card.|"></a>
            </li>
		</ul>
		<c:if test="${1 eq saveCardOption}">
	        <div class="clear"></div>
	        <div id = "ccStoreCardWrapper">
	        <div class="fl" id="ccSaveCardLabel">
	        	<input type="checkbox"  class = "pcb" name="storeCardFlag" checked="checked">
	        </div>
	        <label for="card1" class="save fl mt6 ml10">Save this card for faster checkout</label>
	        </div>
        </c:if>
        <p class="clear">
           	<input name="" type="submit" class="gry-btn" value="Proceed Securely" id = "ccSubmit">
           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
        </p>
        <div class="secure">
        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
        	<span><i>Your card details are secured via 128 Bit encryption by<br/>Verisign</i></span>
        </div>
	</form>
</div>