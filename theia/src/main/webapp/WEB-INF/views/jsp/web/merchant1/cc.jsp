<div class = 'card content ${1 eq paymentType ? "active" : ""}' id = "cc-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your payment details are secured via<br/>128 Bit encryption by Verisign</i></span>
	        </div>
		</div>
		<div class="clear"></div>
	</div>
	
	<form autocomplete="off" name="creditcard-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card" class="cc-form validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<input type="hidden" name="walletAmount" id="walletAmountCC" value="0" />

		<c:if test="${cardInfo.cardStoreMandatory}">
			<input type="hidden" name="storeCardFlag"  value="on" />
		</c:if>
		<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		
		<ul class="grid">
        	<li class="mb20 card-wrapper">
            	<label class="mb10" for="cardNumber">ENTER CREDIT CARD NUMBER</label>
            	<c:set var="ccErrorInputClass"></c:set>
            	<c:if test="${'1' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD']}">
            		<c:set var="ccErrorInputClass">error1</c:set>
            	</c:if>
            	
            	<p class="cd">
            		
            		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && txnInfo.txnMode eq 'CC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
            		<c:set var="defaultCardNumberFormatted" value="${ defaultCardNumber ne ''  ? sessionScope.paymentInfo.formattedCardNumber : '' }"></c:set>
            		<c:if test="${retryPaymentInfo.retryCardNumber ne '' && retryPaymentInfo.paymentMode eq 'cc'}">
            			<c:set var="defaultCardNumberFormatted" value="${retryPaymentInfo.retryCardNumber}"></c:set>
            		</c:if>
					<input autocomplete="off" type="text" name="" class="ccCardNumber ${ccErrorInputClass} text-input large-input c cardInput type-tel card-field-selector" id="cn" size="16" maxlength="19" style="width: 278px" data-type="cc" 
					value="${defaultCardNumberFormatted}"> 
					<input type="hidden" name="cardNumber" value="${retryPaymentInfo.cardNumberWithoutFormatting}" class="required">
				</p>
                
                
				
				<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
					<c:if test="${ '1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
	               		<c:set var="promoError" value="1"/> 
	               	</c:if>
				</c:if>

				<div class="error error2 promo-code-error <c:if test='${empty promoError && empty ccErrorInputClass}'>hide</c:if>">
						
					<c:if test='${!empty promoError}'>
						${requestScope.validationErrors['INVALID_PROMO_DETAILS']}
					</c:if>
					
					<c:if test="${ccErrorInputClass eq 'error1'}">
						${requestScope.validationErrors['INVALID_CARD']}
						${requestScope.validationErrors['INVALID_BIN_DETAILS']}
						${requestScope.validationErrors['INVALID_INTERNATIONAL_CARD']}
						${requestScope.validationErrors['INVALID_CARDSCHEME']}
					</c:if>  
					
				</div>
				
			</li>
			
            <li class="fl expiry-wrapper">
     	    	<label class="mb10 ccExpMonth ccExpYear" for = "ccExpMonth">EXPIRY DATE</label>
               	<div class="mb10">
               		<c:set var="ccDatesErrorClass"></c:set>
               		<c:if test="${'1' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
               			<c:set var="ccDatesErrorClass">error1</c:set>
               		</c:if>
               	 	<div class="fl" id = "ccExpMonthWrapper">
	                	<select class="ccExpMonth ${ccDatesErrorClass} combobox required" id="ccExpMonth" name="expiryMonth" style="width: 80px;">
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
               			<select class="ccExpYear ${ccDatesErrorClass} combobox required" id="ccExpYear" name="expiryYear" style="width: 80px;">
              				<option value="0">YY</option>
                            
                            
							<c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}">${i}</option>
							  
							</c:forEach>
                            
                	</select>
               	 </div>
               	 <div class="clear"></div>
               </div>
                <c:if test="${ccDatesErrorClass eq 'error1'}">
					<div class="error error2 clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
				</c:if>
            </li>
            
            <li class="ml10 fr" id = "ccCvvWrapper">
            	<c:set var="ccCvvErrorClass"></c:set>
            	<c:if test="${paymentType eq 1 && errorsPresent && !empty requestScope.validationErrors['INVALID_CVV']}">
            		<c:set var="ccCvvErrorClass">error1</c:set>
            	</c:if>
                <div class="cvv-block">
                	<label class="mb10" for="cvvNumber">CVV/SECURITY CODE</label>
                	<%-- dummy input to trick browser to not save cc number --%> 
                	<input type="text" name="" class="f-hide" autocomplete="off">
                	<input class="ccCvvBox ${ccCvvErrorClass} text-input small-input required type-tel" autocomplete="off" type="password" name="cvvNumber" id="ccCvvBox" maxlength="4">
                	<div class="clear"></div>
                	<c:if test="${ccCvvErrorClass eq 'error1'}">
					<div class="error error2 mt10">${requestScope.validationErrors['INVALID_CVV']}</div>
				</c:if>
                </div>
                <div class="clue-box hide">
					<div class="cc-cvv-clue default-clue ui-cluetip hide mt10">
	                	The last 3 digit printed on the signature panel on the back of your credit card.
	                </div>
	                <div class="cc-cvv-clue amex-clue ui-cluetip hide mt10">
	                	Four digits code printed at <br/>top of Amex logo on your card.
	                </div>
	            </div>
            </li>
		</ul>
		
		<div id="warningDiv" class="hide clear">
			<div id="errorMsg" class="mt10"></div>
		</div>
		
		<div class="storeCardWrapper">
		<c:if test="${saveCardOption}">
	        <div id = "ccStoreCardWrapper" class="fl mt20">
	        <div class="fl" id="ccSaveCardLabel">
	        	<input type="checkbox"  class="pcb checkbox" name="storeCardFlag" checked="checked">
	        </div>
	        <label for="card1" class="save fl mt8">Save this card for faster checkout</label>
	        </div>
        </c:if>
        	<div class="clear"></div>
        </div>
        <div class="mt20">
        	<div class="btn-submit ${submitBtnClass} fl">
	           	<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id = "ccSubmit">
	         </div>
	         <a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
	         <div class="clear"></div>
        </div>

	</form>
	<c:if test="${themeInfo.subTheme eq 'bob'}">
   		<span class="mt10 b show" style="color:#fe5d27;">This transaction will be processed by<img src="images/web/merchant/bob_logo.png" style="position: relative;top: 13px;display: inline;left: 10px;"></span>
   	</c:if>
</div>