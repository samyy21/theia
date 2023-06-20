<div class='content sc-cards ${5 eq paymentType ? "active" : ""}' id="sc-card">
	<c:set var="isFirstChecked" value="false"></c:set>
	<!-- <h3> Select from card below</h3> -->
	<form autocomplete="off" name="savecard-form" method="post" class="form-horizontal validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<c:if test="${txnConfig.addMoneyFlag && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		
		<c:set var="savedCardList" value="${cardInfo.merchantViewSavedCardsList}"/>
			<c:if test="${existAddMoneyTab}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"/>
			</c:if>
		
		<c:forEach var="card"  varStatus="status" items="${savedCardList}">

			<c:if test="${card.paymentMode ne 'IMPS' && card.paymentMode ne 'UPI'}">
				<c:set var="isIMPS" value="false"/>
				<c:set var="cardNumber" value="${card.cardNumber}"/>
				<c:set var="inputLabel" value="ENTER CVV"/>
				
				<c:if test="${isIMPS}">
					<c:set var="cardNumber" value="${card.holderMobileNo}"/>
					<c:set var="inputLabel" value="ENTER OTP"/>
				</c:if>

			<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
				<span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
				<span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>	
				<span class="cardSchemeName hide">${card.cardScheme}</span>
				<span class="issuerName hide">${card.instId}</span>
				<span class="issuerIdebit hide">${card.iDebitCard}</span>
				<c:if test="${!empty card.paymentMode}">
       				<input type="hidden" class="cardType" value="${card.paymentMode}"/>
          		</c:if>
				<div class="card-header small grey-text mb20">
					<div class="fl">
						<c:choose>
							<c:when test="${isIMPS}">
								Your IMPS Mobile
							</c:when>
							<c:otherwise>
								Your Saved Card
							</c:otherwise>
						</c:choose>
					</div>
					<div class="fr">
						<span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
							<a href="#" class="blue blue-text-2">Remove Card</a>
						</span>
					</div>
					<div class="clear"></div>

				</div>
           		<div class="controls" id="one">
             		<div class="input-prepend grid">
               			<span class="add-on fl">
                 			<label class="radio cvvRadio">
                   				<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}" data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}" test="card.count"  <c:if test="${!isFirstChecked}"> checked </c:if>  />
                   				<c:if test="${!isFirstChecked}">
                 					<c:set var="isFirstChecked" value="true"></c:set>
                 				</c:if>
                 			</label>
               			</span>
               			<div class="sc-number fl ml20">
							<c:if test="${!empty card.instId}">
									<span class="fl mr10 mt6 lt-grey-text medium bankNM">${card.instId}</span>
									<div class="sc-icon ${card.cardScheme}-sc fl">
										<%-- Need to set image of card name like VISA,MASTER etc --%>
										<%-- <c:choose>
											<c:when test="${'MASTER' eq card.cardScheme}">
												<!-- <c:set var="cardImagePrefix" value="master" /> -->
												<div class="fl ml10" style="width: 69px">
													<img class="cardPad" src="${pageContext.request.contextPath}/resources/images/web/master.jpg" alt="Master Card"
														title="Master" />
												</div>
											</c:when>
											<c:when test="${'VISA' eq card.cardScheme}">
												<div class="fl ml10" style="width: 69px">
													<img class="cardPad" src="${pageContext.request.contextPath}/resources/images/web/visa.jpg" alt="Visa"
														title="Visa" />
												</div>
											</c:when>
											<c:when test="${'AMEX' eq card.cardScheme}">
												<div class="fl ml10" style="width: 69px">
													<img class="cardPad" src="${pageContext.request.contextPath}/resources/images/web/amex.png" alt="Amex"
														title="Amex" />
												</div>
											</c:when>
											<c:when test="${'MAESTRO' eq card.cardScheme}">
												<div class="fl ml10" style="width: 69px">
													<img class="cardPad"
														src="${pageContext.request.contextPath}/resources/images/web/maestro.png"
														alt="Maestro" title="Maestro" />
												</div>
											</c:when>
										</c:choose> --%>
										<%-- End --%>
									</div>
								</c:if>

								<input id="prependedradio-${card.cardId}" name="prependedradio"
									class="savedCardLabel clear text-input fl" disabled="disabled"
									type="text" value="${cardNumber}" style="position: relative; width:100%;">

							<c:if test="${isIMPS}">				
               					<span class="fl mr10 mt6 lt-grey-text medium">cardNumber
										: ${card.cardNumber}</span>
               				</c:if>

           					<c:if test="${empty card.instId}">
           						<div class="sc-icon ${card.cardScheme}-sc fl"></div>
           					</c:if>
               				
						</div>
					<div class="clear"></div>
					</div>
					<!-- For Icici Debit card Only -->
					<c:if test="${card.iDebitCard}">
				                <div id="idebitSavedCard" class="idebitSavedCard mt10 ml50"  style="display:none;">
				                        
				                        <div class="idebitOption"> <label>
				                        <input type="radio"  class = "pcb checkbox paymentIdebit" name="isIciciIDebit" value="Y" checked>
				                         Use ATM PIN</label></div>
				                         <div class="idebitOption">
				                        <label>
				                        <input type="radio"  class = "pcb checkbox paymentIdebit" name="isIciciIDebit" value="N">
				                         Use 3D Secure PIN or OTP</label>
				                        </div>
				                </div>
				                <!-- For Icici Debit card Only -->
				      </c:if>
				      <div id="warningDiv-${card.cardId}" class="ml20 mb5 hide clear">
						<div id="errorMsg-${card.cardId}" class="mt10"></div>
					  </div>
					<div class="saveCardCvvDiv mb20 mt20 ml50"
						id="cvvDiv-${card.cardId}" style="display:none;">
						<ul class="">
							<li>
							<div class="ml15 mt24 mr10 fl save-card-cvv">
							<label for="submit-btn-${card.cardId}" class="mb7">${inputLabel}</label>

							<c:if test="${card.cardScheme eq 'SBIME' || card.cardScheme eq 'MAESTRO'}">
								<input type="password" id="cvv-${card.cardId}" disabled
										class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
										maxlength="${isIMPS ? '8' : '4'}"
										minlength="${isIMPS ? '6' : '3'}" value="">
							</c:if>
								<c:if test="${card.cardScheme ne 'SBIME' && card.cardScheme ne 'MAESTRO'}">
									<input type="password" id="cvv-${card.cardId}"
										   class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
										   maxlength="${isIMPS ? '8' : '4'}"
										   minlength="${isIMPS ? '6' : '3'}" value="">
								</c:if>

							<c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
										<div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_CVV']}</div>
										<script type="text/javascript">
											$("#cvv-" + '${card.cardId}').addClass("error1");
										</script>
									</c:if>
							</div>
							<div class="action-btns mt20 fl">
								<div class="btn-submit ${submitBtnClass}">
									<input name="submit-btn-${card.cardId}" type="submit"
										class="gry-btn btn-normal ${card.cardType eq 'SBIME' ? 'no-check' : ''}"
										value="Pay now" id="scSubmit">
								</div>
							</div>

							<div class="clear"></div>
							</li>
						</ul>
		               	
	             		<c:if test="${card.cardType eq 'SBIME'}">
           					<span>If your Maestro Card does not have CVV, skip the field</span>		
           				</c:if>
              		</div>
           		</div>
            </div>
            
            </c:if>


			<c:if test="${card.paymentMode eq 'UPI'}">
				<c:set var="isIMPS" value="false"/>
				<c:set var="cardNumber" value="${card.cardNumber}"/>

				<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
					<span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
					<span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>
					<span class="cardSchemeName hide">${card.cardScheme}</span>
					<span class="issuerName hide">${card.instId}</span>
					<span class="issuerIdebit hide">${card.iDebitCard}</span>
					<c:if test="${!empty card.paymentMode}">
						<input type="hidden" class="cardType" value="${card.paymentMode}"/>
					</c:if>

					<div class="card-header small grey-text mb20">
						<div class="fl">
							<c:choose>
								<c:when test="${isIMPS}">
									Your IMPS Mobile
								</c:when>
								<c:otherwise>
									Your Saved VPA
								</c:otherwise>
							</c:choose>
						</div>

						<div class="fr">
						<span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
							<a href="#" class="blue blue-text-2">Remove Card</a>
						</span>
						</div>

						<div class="clear"></div>

					</div>
					<div class="controls" id="one">
						<div class="input-prepend grid">
               			<span class="add-on fl">
                 			<label class="radio cvvRadio">
								<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}" data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}" test="card.count"  <c:if test="${!isFirstChecked}"> checked </c:if>  />
								<c:if test="${!isFirstChecked}">
									<c:set var="isFirstChecked" value="true"></c:set>
								</c:if>
							</label>
               			</span>
							<div class="sc-number fl ml20">

								<c:if test="${!empty card.paymentMode}">
									<span class="fl mr10 mt6 lt-grey-text medium bankNM">BHIM UPI</span>
									<div class="sc-icon ${card.paymentMode}-sc fl"></div>
								</c:if>
								<input id="prependedradio-${card.cardId}" name="prependedradio"
									   class="savedCardLabel clear text-input fl" disabled="disabled"
									   type="text" value="${cardNumber}" style="position: relative; width:100%;">

								<c:if test="${isIMPS}">
               					<span class="fl mr10 mt6 lt-grey-text medium">cardNumber
										: ${card.cardNumber}</span>
								</c:if>

							</div>
							<div class="clear"></div>
						</div>

						<div id="warningDiv-${card.cardId}" class="ml20 mb5 hide clear">
							<div id="errorMsg-${card.cardId}" class="mt10"></div>
						</div>
						<div class="saveCardCvvDiv mb20 mt24 ml50"
							 id="cvvDiv-${card.cardId}" style="display:none;">
							<ul class="">
								<li>

									<div class="action-btns  fl">
										<div class="btn-submit ${submitBtnClass}">
											<input name="submit-btn-${card.cardId}" type="submit"
												   class="gry-btn btn-normal ${card.cardType eq 'SBIME' ? 'no-check' : ''}"
												   value="Pay now" id="scSubmit">
										</div>
									</div>

									<div class="clear"></div>
								</li>
							</ul>
						</div>
					</div>
				</div>

			</c:if>






			<c:if test="${card.paymentMode eq 'IMPS'}">
			
			<c:set var="isIMPS" value="true"></c:set>
			<c:set var="cardNumber" value="${card.cardNumber}"></c:set>
			<c:set var="inputLabel" value="ENTER CVV"></c:set>
			
			<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
			<c:set var="inputLabel" value="ENTER OTP"></c:set>
			
			<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">

				<div class="card-header small grey-text mb20">
					<div class="fl">
						<c:choose>
							<c:when test="${isIMPS}">
								Your IMPS Mobile
							</c:when>
							<c:otherwise>
								Your Saved Card
							</c:otherwise>
						</c:choose>
					</div>
					<div class="fr">
						<span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
							<a href="#" class="blue blue-text-2">Remove Card</a>
						</span>
					</div>
					<div class="clear"></div>
				</div>
				<div class="controls" id="one">
					<div class="input-prepend grid">
						<span class="add-on fl">
							<label class="radio cvvRadio">
								<input type="checkbox" name="cno" class="sc-checkbox checkbox"
									id="${card.cardId}" data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}"
									   test="card.count"
								<c:if test="${!isFirstChecked}"> checked </c:if> />
								<c:if test="${!isFirstChecked}">
									<c:set var="isFirstChecked" value="true"></c:set>
								</c:if>
							</label>
						</span>
					<div class="sc-number fl ml20">
						<input id="prependedradio-${card.cardId}" name="prependedradio"
									class="savedCardLabel clear text-input fl" disabled="disabled"
									type="text" value="${cardNumber}" style="position: relative;">

								<c:if test="${isIMPS}">
									<span class="fl mr10 mt6 lt-grey-text medium">MMID
										: ${card.expiryDate}</span>
								</c:if>

								<%-- <span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
								<a href="#" class="blue">x</a>
							</span> --%>
						</div>
						<div class="clear"></div>
					</div>
					 
					<div class="saveCardCvvDiv mb20 mt20 ml50"
						id="cvvDiv-${card.cardId}" style="display:none;">
		               	<ul class="grid">
		               		<li>
				               	<div class="ml15 mt24">
				                   <label for="submit-btn-${card.cardId}" class="mb7">${inputLabel}</label>

									<c:if test="${card.cardScheme eq 'SBIME' || card.cardScheme eq 'MAESTRO'}">
				                   <p> <input type="text" name="otp" disabled id="cvv-${card.cardId}" class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel" maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""></p>
									</c:if>

									<c:if test="${card.cardScheme ne 'SBIME' && card.cardScheme ne 'MAESTRO'}">
										<p> <input type="text" name="otp"  id="cvv-${card.cardId}" class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel" maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""></p>
									</c:if>
                   
				                   <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
										<div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_OTP']}</div>
										<script type="text/javascript">
											$("#cvv-" + '${card.cardId}').addClass("error1");
										</script>
									</c:if>
				               	</div>
				            </li>
				            
				            
				            <li class="action-btns mt20 ml10">
				            	<div class="btn-submit fl ${submitBtnClass}">
					           		<input name="submit-btn-${card.cardId}" type="submit" class="gry-btn btn-normal ${card.cardType eq 'SBIME' ? 'no-check' : ''}" value="Pay now" id="scSubmit">
					           	</div>

								<div class="clear"></div>
					        </li>
				       </ul>		               
              		</div>
           		</div>
            </div>
            
            </c:if>
		</c:forEach>

		<c:choose>
			<c:when test="${isIMPS}">
       			<input type="hidden" name="txnMode" value="IMPS" />
				<input type="hidden" name="AUTH_MODE" value="OTP" />
    		</c:when>
			<c:otherwise>
        		<input type="hidden" name="txnMode" value="CC" />
				<input type="hidden" name="txnMde" value="SC" />
				<input type="hidden" name="AUTH_MODE" value="3D" />
    		</c:otherwise>
		</c:choose>
		
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="savedCardId" id="savedCardId" value=""/>
		<input type="hidden" name="cvvNumber" id="scCvvBox" value="">
		<input type="hidden" name="walletAmount" id="walletAmountSC" value="0" />
		<input type="hidden" name="storeCardFlag"  value="off" />
		
		<div class="clear"></div>
	</form>
</div>