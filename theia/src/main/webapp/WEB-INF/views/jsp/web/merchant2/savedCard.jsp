<div class = 'content sc-cards ${5 eq paymentType ? "active" : ""}' id = "sc-card">

	<!-- <h3> Select from card below</h3> -->
	<form autocomplete="off" name="savecard-form" method="post" class="form-horizontal validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="txnMde" value="SC" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="savedCardId" id="savedCardId" value=""/>
		<input type="hidden" name="cvvNumber" id="scCvvBox" value="">
		<input type="hidden" name="walletAmount" id="walletAmountSC" value="0" />
		<c:if test="${txnConfig.addMoneyFlag && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		
		<c:set var="savedCardList" value="${cardInfo.merchantViewSavedCardsList}"/>
			<c:if test="${existAddMoneyTab}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"/>
			</c:if>
		
		<c:forEach var="card"  varStatus="status" items="${savedCardList}">

			<c:if test="${card.paymentMode ne 'IMPS'}">
			
				<c:set var="isIMPS" value="false"/>
				<c:set var="cardNumber" value="${card.cardNumber}"/>
				<c:set var="inputLabel" value="ENTER CVV"/>
			<div class="control-group  card" style="height:326px; border:none;">	
			<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
				<span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
				<span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>	
				<span class="cardSchemeName hide">${card.cardScheme}</span>
				<span class="issuerName hide">${card.instId}</span>
				<span class="issuerIdebit hide">${card.iDebitCard}</span>
				<c:if test="${!empty card.paymentMode}">
       				<input type="hidden" class="cardType" value="${card.paymentMode}"/>
          		</c:if>
				<div class="card-header small grey-text mt10 mr10">
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
							<a href="#" class="blue blue-text-2">Remove</a>
						</span>
					</div>
					<div class="clear"></div>
				</div>
           		<div class="controls" id="one">
             		<div class="input-prepend grid pd15 mb20">
               			<c:if test="${!isIMPS}">
	               			<span class="add-on fl">
	               				<div class="sc-icon ${card.cardScheme}-sc fr"></div>
	               				
	               				<c:if test="${!empty card.instId}">
									<span class="fl mr10 mt6 lt-grey-text medium bankNM">${card.instId}</span>
								</c:if>
	               				
	                 			<label class="radio cvvRadio" >
	                   				<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}" data-firstsixdigits="${card.firstSixDigit}" <c:if test="${status.count eq 1 }">checked="checked"</c:if> >
	                 			</label>
	               			</span>
	               		
						</c:if>
						<c:if test="${isIMPS}">
							<div class="sc-number fl">
	               				<label class="b mt10">IMPS MOBILE NUMBER</label>
							</div>
						</c:if>
						<div class="clear"></div>
						<input id="prependedradio-${card.cardId}" name="prependedradio" class="savedCardLabel text-input" disabled="disabled" type="text" value="${cardNumber}" style="position: relative;">
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
             		<div class="saveCardCvvDiv" id="cvvDiv-${card.cardId}">
		               	<ul class="grid">
		               		<li class="pd15">
				               	<div class="ml15 mt24 save-card-cvv">
				                   <label for="submit-btn-${card.cardId}" class="mb7">ENTER CVV</label>
				                   <p> <input type="password" id="cvv-${card.cardId}" class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel" maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""></p>
				                   
				                   <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
										<div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_CVV']}</div>
										<script type="text/javascript">
											$("#cvv-" + '${card.cardId}').addClass("error1");
										</script>
									</c:if>
									
									<c:if test="${card.cardScheme eq 'SBIME'}">
			           					<span>If your Maestro Card does not have CVV, skip the field</span>		
			           				</c:if>
				               	</div>
				            </li>
				            <%-- <c:if test="${isIMPS}">
				            	<li class="pd15">
					               	<div class="ml15 mt24">
					                   <label class="mb7">MMID</label>
					                   <p>${card.mmid}</p>
					               	</div>
					            </li>
				            </c:if> --%>
				            <li class="action-btns mt6">
				            	<div class="btn-submit fl ${submitBtnClass}">
					         		<input name="submit-btn-${card.cardId}" type="button" class="gry-btn ${card.cardType eq 'SBIME' ? 'no-check' : ''}" value="Pay now" id="scSubmit">
					           	</div>
								<div class="clear"></div>
					        </li>
				            
				       </ul>
		               	
              		</div>
           		</div>
            </div>
            </div>
            
            </c:if>
		</c:forEach>
			
		<div class="clear"></div>
	</form>

	<form autocomplete="off" name="savecard-form" method="post" class="form-horizontal validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="IMPS" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
		<input type="hidden" name="AUTH_MODE" value="OTP" />
		<input type="hidden" name="savedCardId" id="savedCardId" value=""/>
		<input type="hidden" name="cvvNumber" id="scCvvBox" value="">
		<input type="hidden" name="walletAmount" id="walletAmountSC" value="0" />
		<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>

		<c:forEach var="card" varStatus="status" items="${savedCardList}">

			<c:if test="${card.paymentMode eq 'IMPS'}">
			
			<c:set var="isIMPS" value="true"></c:set>
			<c:if test="${isIMPS}">
				<c:set var="cardNumber" value="${card.cardNumber}"></c:set>
				<c:set var="inputLabel" value="ENTER OTP"></c:set>
			</c:if>
			
			<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
				<div class="card-header small grey-text mt10 mr10">
					<!-- <div class="fl">
						Your Saved Card
					</div> -->
					<div class="fr">
						<span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
							<a href="#" class="blue blue-text-2">Remove</a>
						</span>
					</div>
					<div class="clear"></div>
				</div>
           		<div class="controls" id="one">
             		<div class="input-prepend grid pd15 mb20">
               			<c:if test="${!isIMPS}">
	               			<span class="add-on fl">
	                 			<label class="radio cvvRadio" >
	                   				<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}" data-firstsixdigits="${card.firstSixDigit}" <c:if test="${status.count eq 1 }">checked="checked"</c:if> >
	                 			</label>
	               			</span>
						</c:if>
						<c:if test="${isIMPS}">
							<div class="sc-number fl">
	               				<label class="b mt10">IMPS MOBILE NUMBER</label>
							</div>
						</c:if>
						<div class="clear"></div>
						<input id="prependedradio-${card.cardId}" name="prependedradio" class="savedCardLabel text-input" disabled="disabled" type="text" value="${cardNumber}" style="position: relative;">
					<div class="clear"></div>
             		</div>
             		
             		
             		<div class="saveCardCvvDiv" id="cvvDiv-${card.cardId}">
		               	<ul class="grid">
		               		<li class="pd15">
				               	<div class="ml15 mt24">
				                   <label for="submit-btn-${card.cardId}" class="mb7">ENTER OTP</label>
				                   <p> <input type="text" name="otp" id="cvv-${card.cardId}" class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel" maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""></p>
                   
				                   <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
										<div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_OTP']}</div>
										<script type="text/javascript">
											$("#cvv-" + '${card.cardId}').addClass("error1");
										</script>
									</c:if>
				               	</div>
				            </li>
				            <li class="action-btns mt6">
				            	<div class="btn-submit fl ${submitBtnClass}">
					         		<input name="submit-btn-${card.cardId}" type="button" class="gry-btn ${card.cardType eq 'SBIME' ? 'no-check' : ''}" value="Pay now" id="scSubmit">
					           	</div>
								<div class="clear"></div>
					        </li>
				       </ul>		               
              		</div>
           		</div>
            </div>
            
            </c:if>
		</c:forEach>
		
		<div class="clear"></div>
	</form>
</div>