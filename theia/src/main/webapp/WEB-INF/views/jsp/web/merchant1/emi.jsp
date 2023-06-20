<div class = 'card content ${13 eq paymentType ? "active" : ""}' id = "emi-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
				<div class="img img-lock fl" alt="Secure" title="Secure"></div>
				<span><i>Your payment details are secured via<br />128
						Bit encryption by Verisign
				</i></span>
			</div>
		</div>
		<div class="clear"></div>
	</div>
	<div class="fl">
		<form autocomplete="off" class="emi-form validated" name="emi-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="EMI" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
			<input type="hidden" name="walletAmount" value="0" />
			<input type="hidden" name="emiBankName" value="" />
			
			
			<c:if test="${isAddMoneyAvailable  && isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			<c:if test="${!empty param.emiBankName}">
				<c:set var="emiSelectedBank" value="${param.emiBankName}" scope="session" />
				<c:if test="${param.emi_bank eq 'none'}">
					<c:remove var="emiSelectedPlanId" scope="session"></c:remove>
				</c:if>
			</c:if>
			
			<%-- For retry only --%>
			<c:if test="${retryPaymentInfo.paymentMode eq 'emi' && retryPaymentInfo.emiBankName ne '' && 
			              retryPaymentInfo.emiPlanId ne ''}">
				<c:set var="emiSelectedBank" value="${retryPaymentInfo.emiBankName}" />
				<c:set var="emiPlanIdSelected" value="${retryPaymentInfo.emiPlanId}" /> 
			</c:if>
			<%-- End --%>
			
			<c:if test="${!empty param.emi_plan_id}">
				<c:set var="emiSelectedPlanId" value="${param.emi_plan_id}" scope="session"></c:set>
			</c:if>
			<c:set var="emiInfoList" value="${entityInfo.completeEMIInfoList}" ></c:set>
			<c:if test="${existAddMoneyTab}">
				<c:set var="emiInfoList" value="${entityInfo.addCompleteEMIInfoList}" ></c:set>
			</c:if>
			<%-- loop on bank maps --%>
			<c:forEach var="i" begin="1" end="2">
				<c:if test="${i eq 1}">
					<c:set var="emiBankMap" value="${emiInfoList}" />
				</c:if>
				<c:if test="${i eq 2}">
					<%--TODO: to be changed to hybrid later --%>
					<c:set var="emiBankMap" value="${entityInfo.hybridEMIInfoList}" />
				</c:if>
			
				<div class="emi-bank-map emi-bank-map-${i} ${i eq 2 ? ' hide' : ''}">
				
					<c:set var="emiAvailable" value="true" />
					<c:if test="${empty emiBankMap}">
						<div class="alert mt10">
							EMI is not available for this transaction
						</div>
						<c:set var="emiAvailable" value="false"></c:set>
					</c:if>
		
					<c:if test="${emiAvailable}">
						<label>SELECT YOUR BANK</label><br>
						<select class="emiBankSelect ${!empty emiSelectedBank ? 'selected' : '' }">
							<option id="select-option" value="none">Select</option>
							<c:forEach var="bank" items="${emiBankMap}">
						    	<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${emiSelectedBank eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
							</c:forEach>
						</select>
							
						
						<%-- loop on banks --%>
						<c:forEach var="bank" items="${emiBankMap}">
								<div class='${bank.bankName}-bank emi-bank-plans hide'>
									<label class="mt20 mb10">SELECT EMI PLAN</label>
									
									<ul id="${bank.bankName}-emi-plans" class="grid emi-plans">
										<%-- loop on plans --%>
										<c:forEach var="emiPlan" items="${bank.emiInfo}">
										
										    <li>
										    	<div>
										    	
											    	<input type="radio" name="emiPlanId" value="${emiPlan.planId}" <c:if test="${emiPlanIdSelected eq emiPlan.planId}">checked='checked'</c:if> 
											    	class="fl checkbox">
											    	<input type="hidden" value="${emiPlan.aggregator}" class="isAggregator"/>
											    	<input type="hidden" value="${emiPlan.ajaxRequired}" class="isAjaxRequired"/>
													<span class="emi-month medium b">${emiPlan.ofMonths} Months</span>
													<br>
												</div>
												<div class="emi-back">
													<span class="emi-amt">Rs ${emiPlan.emiAmount}</span>
													<br>
													<span class="emi-interest">
														<span class="b">${emiPlan.interestRate}%</span> 
														p.a
													</span>
												</div>
											</li>
										</c:forEach>
									</ul>
								</div>
						</c:forEach>
					</c:if>
				</div>

			</c:forEach>



			<div class="card-details-wrapper mt20 hide">
				<ul class="grid">
		        	<li class="mb20 card-wrapper">
		            	<label class="mb10" for="cardNumber">ENTER CREDIT CARD NUMBER</label>
		            	<c:set var="ccErrorInputClass"></c:set>
		            	<c:if test="${'13' eq paymentType && errorsPresent  && !empty requestScope.validationErrors['INVALID_CARD']}">
		            		<c:set var="ccErrorInputClass">error1</c:set>
		            	</c:if>
		            	<p class="cd">
		            		<c:set var="defaultCardNumberFormatted" value=""></c:set>
		            		<c:set var="CardNumberWithoutFormatting" value=""></c:set>
		            		<c:if test="${retryPaymentInfo.retryCardNumber ne '' && retryPaymentInfo.paymentMode eq 'emi'}">
            			   		<c:set var="defaultCardNumberFormatted" value="${retryPaymentInfo.retryCardNumber}"></c:set>
            			   		<c:set var="CardNumberWithoutFormatting" value="${retryPaymentInfo.cardNumberWithoutFormatting}"></c:set>
            		  		 </c:if>
							<%-- <input autocomplete="off"  class="ccCardNumber ${ccErrorInputClass} text-input large-input c cardInput type-tel" id="cn" type="text" size="16" maxlength="19" style="width: 278px" data-type="cc" 
							value="${defaultCardNumberFormatted}" />
							<input type="hidden" name="cardNumber" value="${CardNumberWithoutFormatting} class="required" />
							 --%>
							<input autocomplete="off" type="text" name="" class="emiCCCardNumber ${ccErrorInputClass} text-input large-input c cardInput type-tel card-field-selector" id="cn" size="16" maxlength="19" style="width: 278px" data-type="cc" 
								value="${defaultCardNumberFormatted}"> 
							<input type="hidden" name="cardNumber" value="${retryPaymentInfo.cardNumberWithoutFormatting}" class="required">
					
							<div id="cardInvalidmsg" class="error hide clear">Please Enter the valid card number.</div>
						</p>
		                
		                <c:if test="${ccErrorInputClass eq 'error1'}">
							<div class="error error2 clear">${requestScope.validationErrors['INVALID_CARD']}</div>
						</c:if>
						
					</li>
					
		            <li class="fl expiry-wrapper">
		     	    	<label class="mb10 ccExpMonth ccExpYear" for = "ccExpMonth">EXPIRY DATE</label>
		               	<div class="mb10">
		               		<c:set var="ccDatesErrorClass"></c:set>
		               		<c:if test="${'13' eq paymentType && errorsPresent  && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
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
		            	<c:if test="${paymentType eq 13 && errorsPresent && !empty requestScope.validationErrors['INVALID_CVV']}">
		            		<c:set var="ccCvvErrorClass">error1</c:set>
		            	</c:if>
		                <div class="cvv-block">
		                	<label class="mb10" for="cvvNumber">CVV/SECURITY CODE</label> 
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
		           		<input name="" type="submit" class="gry-btn btn-normal" value="Pay now" id = "emiSubmit">	          
		           	</div>
		           	<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
		           	<div class="clear"></div>
		        </div>
		    </div>
	       
		</form>
	</div>
	
    <div class="clear"></div>
</div>