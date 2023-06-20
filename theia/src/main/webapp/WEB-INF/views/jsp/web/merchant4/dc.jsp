<div class = 'card content ${2 eq paymentType ? "active" : ""}' id = "dc-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="clear"></div>
	</div>

	<form autocomplete="off" name="creditcard-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card" class="dc-form validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="DC" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<input type="hidden" name="walletAmount" id="walletAmountDC" value="0" />
		<c:if test="${cardInfo.cardStoreMandatory}">
			<input type="hidden" name="storeCardFlag"  value="on" />
		</c:if>
		<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		
		<ul class="grid">
        	<li class="mb20 card-wrapper">
            	<label for="cardNumber" class="mb10">ENTER DEBIT CARD NUMBER</label>
            	<c:set var="dcErrorInputClass"></c:set>
            	<c:if test="${'2' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD']}">
            	</c:if>
            	
            	
            	
            	<p class="cd">
            	
            		<c:set var="defaultCardNumber" value="${!empty sessionScope.paymentInfo && txnInfo.txnMode eq 'DC' ? sessionScope.paymentInfo.cardNumber : '' }"></c:set>
            		<c:set var="defaultCardNumberFormatted" value="${ defaultCardNumber ne ''  ? sessionScope.paymentInfo.formattedCardNumber : '' }"></c:set>
            		<c:if test="${retryPaymentInfo.retryCardNumber ne '' && retryPaymentInfo.paymentMode eq 'dc'}">
            			<c:set var="defaultCardNumberFormatted" value="${retryPaymentInfo.retryCardNumber}"></c:set>
            		</c:if>
					<input class="dcCardNumber ${dcErrorInputClass} d text-input large-input cardInput type-tel card-field-selector" autocomplete="off" name="" id="cn1" type="text" maxlength="23" style="width: 278px" data-type="dc" 
					value="${defaultCardNumberFormatted}">
					<input type="hidden" name="cardNumber" value="${retryPaymentInfo.cardNumberWithoutFormatting}" class="required">
				</p>
                
                <c:if test="${dcErrorInputClass eq 'error1'}">
					<div id="dcCCnoId" class="error error2 clear" >
						${requestScope.validationErrors['INVALID_CARD']}
						${requestScope.validationErrors['INVALID_BIN_DETAILS']}
						${requestScope.validationErrors['INVALID_INTERNATIONAL_CARD']}
						${requestScope.validationErrors['INVALID_CARDSCHEME']}
					</div>
				</c:if>
				
				<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
					<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
	               		<c:set var="promoError" value="1"/> 
	               	</c:if>
					
					<div class="error promo-code-error <c:if test='${empty promoError}'>hide</c:if>">
						
						<c:if test='${!empty promoError}'>
							${requestScope.validationErrors['INVALID_PROMO_DETAILS']}
						</c:if> 
					</div>
					
				</c:if>
				<div id="luhnError" class="luhnError hide error" style="float:right;">Please enter a valid card number</div>
                
			</li>
			<li>
				<div id="warningDiv" class="hide clear" style="margin-top: -15px; margin-bottom: 15px;    width: 410px !important;line-height: 1.45;">
					<div id="errorMsg" class="mt10"></div>
				</div>
			</li>
            <li class="fl expiry-wrapper" style="overflow:hidden; clear:both;">
     	    	<label class="mb10 dcExpMonth dcExpYear" for = "dcExpMonth">EXPIRY DATE</label>
               	<div class="mb10">
               		<c:set var="dcDatesErrorClass"></c:set>
               		<c:if test="${'2' eq paymentType && errorsPresent && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
               			<c:set var="dcDatesErrorClass">error1</c:set>
               		</c:if>
               	 	<div class="fl" id = "dcExpMonthWrapper">
                	<select class="dcExpMonth ${dcDatesErrorClass} combobox required" id="dcExpMonth" name="expiryMonth" style="width: 80px;">
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
               		
               	 	<div class="fl ml10" id = "dcExpYearWrapper">
               			<select class="dcExpYear ${dcDatesErrorClass} combobox required" id = "dcExpYear" name="expiryYear" style="width: 80px;">
             				<option value="0">YY</option>
                           <c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}">${i}</option>
							  
							</c:forEach>
                	</select>
               	 </div>
               	 <div class="clear"></div>
               </div>
                <c:if test="${dcDatesErrorClass eq 'error1'}">
					<div id="dcexId" class="error error2 clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
				</c:if>
            </li>
            
            <li class="ml10 fl relative" id = "dcCvvWrapper">
                <c:set var="dcCvvErrorClass"></c:set>
                <c:if test="${paymentType eq 2 && errorsPresent && !empty requestScope.validationErrors['INVALID_CVV']}">
                	<c:set var="dcCvvErrorClass">error1</c:set>
                </c:if>
                <div class="cvv-block">
                	<label for="cvvNumber" class="mb10">CVV</label>
                	<%-- dummy input to trick browser to not save cc number --%>
                	<input type="text" name="" class="f-hide" autocomplete="off">
                	<input class="dcCvvBox ${dcCvvErrorClass} text-input small-input width40 required type-tel" autocomplete="off" type="password" name="cvvNumber" id="dcCvvBox" maxlength="4">
                	<div class="clear"></div>
                	<c:if test="${dcCvvErrorClass eq 'error1'}">
						<div id="dcCvvId" class="error error2 mt10">${requestScope.validationErrors['INVALID_CVV']}</div>
					</c:if>
                </div>
                
                <div class="cvv-clue-box hide">
	                <div class="dc-cvv-clue default-clue ui-cluetip hide mt10">
	                	The last 3 digit printed on the signature panel on the back of your debit card.
	                </div>
					<div class="dc-cvv-clue amex-clue ui-cluetip hide mt10">
						Four digits code printed at <br/>top of Amex logo on your card.
					</div>
	            </div>
            </li>
		</ul>
		
		<div class="clear"></div>
		<%--For ICICI idebit card 
		 check if ICICI idebit is true--%>
<%-- 		<c:if test="${entityInfo.iDebitEnabled}"> --%>
			<div id="idebitPayOption" class="mt20 hide">
            	
                <div class="idebitOption" style="margin-left:0;">
                   	<label>
                   		<input type="radio" class="checkbox idebitDC fl" name="isIciciIDebit"  value="Y" checked>
                   			<span class="idebit-heading" style="float:left; display:inline-block;margin-top: 7px; padding-left: 5px;">Use ATM PIN</span>
                   	</label>
                </div>
                <div class="clear"></div>
                <div class="idebitOption" >
                	<label>
                		<input type="radio" class="checkbox idebitDC fl"  name="isIciciIDebit"  value="N">
                			<span class="idebit-heading" style="float:left; display:inline-block;margin-top: 7px; padding-left: 5px;">Use 3D Secure PIN or OTP</span>
                	</label>
            	</div>
        	</div>
<%-- --         </c:if> -->--%>
		<%--End of For ICICI idebit card --%>
		<div class="clear"></div>
		
		
		<div id = "cardInfo" class="storeCardWrapper">
			<div class="txt12 opt mt10" id = "maestroOpt" style = "display:none;">If your Maestro Card does not have Expiry Date/CVV, skip these fields</div>
			<c:set var="condition" value="${!walletInfo.walletEnabled or loginInfo.loginFlag}" />
			<c:if test="${saveCardOption }">
		        <div id = "dcStoreCardWrapper" class = "fl mt20">
			        <div id="dcSaveCardLabel" class="fl">
			        	<input type="checkbox"  class = "pcb checkbox" name="storeCardFlag" checked="checked">
					</div>

				<c:if test="${isSubscriptionFlow eq true}">
						<label for="card1" class="save fl mt8">Card details will be saved for renewal transactions.</label>
				</c:if>
				<c:if test="${isSubscriptionFlow eq false}">

							<label for="card1" class="save fl mt8">Save this card for faster checkout</label>
					</c:if>
		         </div>
	        </c:if>
          	<div class="clear"></div>
          </div>
          <div class="clear"></div>
	      	<c:set var="submitBtnText" value="Pay now"></c:set>

        <div class="card-bin-disable mt10 hide clear">
            <div class="card-bin-disable-error-msg mt10"></div>
        </div>

        <div class="mt20">
        	<div class="btn-submit ${submitBtnClass} fl">
        	<button name="" type="submit" class="gry-btn fr btn-normal btn-submit" id = "dcSubmit"  data-txnmode="DC" onclick="pushGAData(this, 'pay_now_clicked')" >${submitBtnText}</button>
<!--            		<input name="" type="submit" class="gry-btn fr btn-normal" value="Pay now" id = "dcSubmit"> -->
           	</div>

        	<div class="clear"></div>
        </div>
        
	</form>
	<c:if test="${themeInfo.subTheme eq 'bob'}">
   		<span class="mt10 b show" style="color:#fe5d27;">This transaction will be processed by<img src="images/web/merchant/bob_logo.png" style="position: relative;top: 13px;display: inline;left: 10px;"></span>
   	</c:if>
</div>