<div class='content sc-cards ${5 eq paymentType ? "active" : ""}' id="sc-card">
	<c:set var="isFirstChecked" value="false"></c:set>
	<!-- <h3> Select from card below</h3> -->
	<div class="savedCardSection">
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

			<c:if test="${card.paymentMode ne 'IMPS' && card.paymentMode ne 'UPI'}">
				<c:set var="isIMPS" value="false"/>
				<c:set var="cardNumber" value="${card.cardNumber}"/>
				<c:set var="inputLabel" value="ENTER CVV"/>
				
				<c:if test="${isIMPS}">
					<c:set var="cardNumber" value="${card.holderMobileNo}"/>
					<c:set var="inputLabel" value="ENTER OTP"/>
				</c:if>

			<div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
						<span class="convcardType hide">${card.cardType}</span>
						<span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
						<span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>	
						<span class="cardSchemeName hide">${card.cardScheme}</span>
						<span class="issuerName hide">${card.instId}</span>
						<span class="issuerIdebit hide">${card.iDebitCard}</span>
                        <div class="controls">
                            <div class="input-prepend grid">
                                <div class="cardHeading">
	              			<span class="add-on fl">
	                			<label class="radio cvvRadio">
	                  				<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}"
                                           data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}" test="card.count"  <c:if
                                            test="${!isFirstChecked}"> checked </c:if>
                                            <c:if test="${! empty txnConfig.paymentCharges}"> data-cType="${card.cardType}"  </c:if>
                                            />
	                  				<c:if test="${!isFirstChecked}">
                                        <c:set var="isFirstChecked" value="true"></c:set>
                                    </c:if>
	                			</label>
	              			</span>
							<div class="fl ml20">
	              				<c:if test="${!empty card.bankName}">
	              					<span class="fl mr10 mt6 lt-grey-text bankNM default">${card.bankName}</span>
	              					
	              				</c:if>
	              				<c:if test="${!empty card.paymentMode}">
	              				<input type="hidden" class="cardType" value="${card.paymentMode}"/>
	              				</c:if>
	              			</div>
	              			
	              			 <span class="deleteCard fr" cardId="${card.cardId}" id="delete-${card.cardId}">
								<a href="#" class="blue blue-text-2">Remove Card</a>
							</span>
                                </div>

                                <div class="sc-number">
                                    <c:if test="${!empty card.instId}">
                                        <div class="sc-icon ${card.cardScheme}-sc fl"></div>
                                    </c:if>
                                    <input id="prependedradio-${card.cardId}" name="prependedradio"
                                           class="savedCardLabel clear text-input fl" disabled="disabled" type="text" value="${cardNumber}"
                                           style="position: relative; width:100%;">

										<c:if test="${isIMPS}">
											<span class="fl mr10 mt6 lt-grey-text medium">cardNumber
												: ${card.cardNumber}</span>
										</c:if>

								

                                    <div class="saveCardCvvDiv fr" id="cvvDiv-${card.cardId}" style="display:none;">
                                        <ul class="">
                                            <li>
                                                <div class="ml15 mt24 mr10 fl">
                                                       <label for="submit-btn-" class="mb7 small"></label>

                                                    <c:if test="${card.cardScheme eq 'SBIME' || card.cardScheme eq 'MAESTRO'}">
                                                        <input type="password" id="cvv-${card.cardId}" disabled
                                                           class="scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
                                                           maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""
                                                           placeholder="Enter CVV">
                                                    </c:if>
                                                    <c:if test="${card.cardScheme ne 'SBIME' && card.cardScheme ne 'MAESTRO'}">
                                                        <input type="password" id="cvv-${card.cardId}"
                                                               class="scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
                                                               maxlength="${isIMPS ? '8' : '4'}" minlength="${isIMPS ? '6' : '3'}" value=""
                                                               placeholder="Enter CVV">
                                                    </c:if>

                                                    <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
                                                        <div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_CVV']}</div>
                                                        <script type="text/javascript">
                                                            $("#cvv-" + "${card.cardId}").addClass("error1");
                                                        </script>
                                                    </c:if>
                                                </div>

                                            </li>
                                        </ul>

                                    </div>


                                </div>
                                <div class="clear"></div>
                                <c:if test="${card.cardType eq 'SBIME'}">
                                    <p style="padding:5px;" class="txt12">If your Maestro Card does not have CVV, skip the field</p>
                                </c:if>
                            </div>


                        </div>
                    </div>

                </c:if>


            <c:if test="${card.paymentMode eq 'UPI'}">
                <c:set var="isIMPS" value="false"/>
                <c:set var="cardNumber" value="${card.cardNumber}"/>
                <c:set var="inputLabel" value="ENTER CVV"/>

                <c:if test="${isIMPS}">
                    <c:set var="cardNumber" value="${card.holderMobileNo}"/>
                    <c:set var="inputLabel" value="ENTER OTP"/>
                </c:if>

                <div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && !txnInfo.promoCodeValid ? 'hide' : ''}">
                    <span class="convcardType hide">${card.cardType}</span>
                    <span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
                    <span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>
                    <span class="cardSchemeName hide">${card.cardScheme}</span>
                    <span class="issuerName hide">${card.instId}</span>
                    <div class="controls">
                        <div class="input-prepend grid">
                            <div class="cardHeading">
	              			<span class="add-on fl">
	                			<label class="radio cvvRadio">
                                    <input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}"
                                           data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}" test="card.count"  <c:if
                                            test="${!isFirstChecked}"> checked </c:if>
                                            <c:if test="${! empty txnConfig.paymentCharges}"> data-cType="UPI"  </c:if>
                                            />
                                    <c:if test="${!isFirstChecked}">
                                        <c:set var="isFirstChecked" value="true"></c:set>
                                    </c:if>
                                </label>
	              			</span>
                                <div class="fl ml20">
                                    <c:if test="${!empty card.paymentMode}">
                                        <span class="fl mr10 mt6 lt-grey-text bankNM default">BHIM UPI</span>

                                    </c:if>
                                    <c:if test="${!empty card.paymentMode}">
                                        <input type="hidden" class="cardType" value="${card.paymentMode}"/>
                                    </c:if>
                                </div>

	              			 <span class="deleteCard fr" cardId="${card.cardId}" id="delete-${card.cardId}">
								<a href="#" class="blue blue-text-2">Remove VPA</a>
							</span>
                            </div>

                            <div class="sc-number">
                                <c:if test="${!empty card.paymentMode}">
                                    <div class="sc-icon ${card.paymentMode}-sc fl"></div>
                                </c:if>
                                <input id="prependedradio-${card.cardId}" name="prependedradio"
                                       class="savedCardLabel clear text-input fl" disabled="disabled" type="text" value="${cardNumber}"
                                       style="position: relative; width:100%;">

                                <c:if test="${isIMPS}">
											<span class="fl mr10 mt6 lt-grey-text medium">cardNumber
												: ${card.cardNumber}</span>
                                </c:if>


                            </div>
                            <div class="clear"></div>

                        </div>


                    </div>
                </div>

            </c:if>
            </c:forEach>





        <div class="clear"></div>
                <!-- For Icici Debit card Only -->
                <div id="idebitSavedCard" class="mt10 hide card" style="margin-left: 3%;">
                        
                        <div class="idebitOption"> <label>
                        <input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit" value="Y" checked>
                         <span class="idebit-heading" style="float:left; display:inline-block;margin-top: 7px; padding-left: 5px;">Use ATM PIN</span>
                         </label></div>
                         <div class="clear"></div>
                         <div class="idebitOption">
                        <label>
                        <input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit" value="N">
                         <span class="idebit-heading" style="float:left; display:inline-block;margin-top: 7px; padding-left: 5px;">Use 3D Secure PIN or OTP</span>
                         </label>
                        </div>
                </div>
                <!-- For Icici Debit card Only -->
          

            <div class="action-btns mt20 fl relative" style="margin-left:3%;" id="ccdcCards">
            	
                <div class="savedCardbtn ${submitBtnClass}">
                	<button name="submit-btn-${card.cardId}" type="submit"
                           class="gry-btn btn-submit btn-normal ${card.cardType eq 'SBIME' ? 'no-check' : ''}" id="scSubmit" data-txnmode="SC" onclick="pushGAData(this, 'pay_now_clicked')">Proceed Securely</button>
                   
                </div>
            </div>
            <div class="clear"></div>
        </form>

		<form autocomplete="off" name="savecard-form" method="post" class="form-horizontal validated impsForm" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
            <input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
            <input type="hidden" name="txnMode" value="IMPS"/>
            <input type="hidden" name="txnMde" value="IMPS"/>
            <input type="hidden" name="channelId" value="${channelInfo.channelID}"/>
            <input type="hidden" name="AUTH_MODE" value="OTP"/>
            <input type="hidden" name="savedCardId" id="savedCardId" value=""/>
            <input type="hidden" name="cvvNumber" id="scCvvBox" value="">
            <input type="hidden" name="walletAmount" id="walletAmountSC" value="0"/>
            <c:if test="${txnConfig.addMoneyFlag && isAddMoneyAvailable}">
                <input type="hidden" name="addMoney" value="1"/>
            </c:if>

            <c:forEach var="card" varStatus="status" items="${savedCardList}">

                <c:if test="${card.paymentMode eq 'IMPS'}">

                    <c:set var="isIMPS" value="true"></c:set>
                    <c:set var="cardNumber" value="${card.cardNumber}"></c:set>
                    <c:set var="inputLabel" value="ENTER CVV"></c:set>
                    <c:if test="${isIMPS}">
                        <c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
                        <c:set var="inputLabel" value="ENTER OTP"></c:set>
                    </c:if>

                    <div class="control-group  card ${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode  && !txnInfo.promoCodeValid ? 'hide' : ''}">

						<span class="issuerLowSuccessRate hide">${card.issuerLowSuccessRate}</span>
						<span class="cardSchemeLowSuccessRate hide">${card.cardSchemeLowSuccessRate}</span>
						<span class="cardSchemeName hide">${card.cardScheme}</span>
                        <!-- <div class="card-header small grey-text mb20">
                            <div class="fl">

                            </div>

                            <div class="clear"></div>
                        </div> -->


                        <div class="controls" id="one">


                            <div class="input-prepend grid">

                                <div class="cardHeading">
	              			<span class="add-on fl">
                 			<label class="radio cvvRadio">
                   				<input type="checkbox" name="cno" class="sc-checkbox checkbox" id="${card.cardId}"
                                       data-firstsixdigits="${card.firstSixDigit}" data-txnMode="${card.txnMode}" test="card.count"  <c:if
                                        test="${!isFirstChecked}"> checked </c:if>  />
                   				<c:if test="${!isFirstChecked}">
                                    <c:set var="isFirstChecked" value="true"></c:set>
                                </c:if>
                 			</label>
               			</span>


                                    <div class="fl ml20">
	              					<span class="fl mr10 mt6 lt-grey-text default">
							Your IMPS Mobile
						</span>
                                    </div>
	              			
	              			 <span class="deleteCard fr" cardId="${card.cardId}" id="delete-${card.cardId}">
								<a href="#" class="blue blue-text-2">Remove Card</a>
							</span>
                                </div>


                                <div class="sc-number fl" style="padding-bottom:0;">

                                    <input id="prependedradio-${card.cardId}" name="prependedradio"
                                           class="savedCardLabel clear text-input fl" disabled="disabled" type="text" value="${cardNumber}"
                                           style="position: relative;">

                                    <c:if test="${isIMPS}">
                                        <span class="fl mr10 mt6 lt-grey-text default">MMID : ${card.expiryDate}</span>
                                    </c:if>

                                        <%-- <span class="deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}">
                                         <a href="#" class="blue">x</a>
                                     </span> --%>
                                </div>
                                <div class="clear"></div>
                            </div>


                            <div class="saveCardCvvDiv mb10 mt10 ml20" id="cvvDiv-${card.cardId}" style="display:none;">
                                <ul class="grid">
                                    <li>
                                        <div class="ml15 mt24">
                                                <%-- <label for="submit-btn-${card.cardId}" class="mb7">${inputLabel}</label> --%>
                                            <div class="ml15 mt24 mr10 fl">
                                                <c:if test="${card.cardScheme eq 'SBIME' || card.cardScheme eq 'MAESTRO'}">
                                                    <input type="text" disabled name="otp" id="cvv-${card.cardId}"
                                                                                  class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
                                                                                  maxlength="${isIMPS ? '8' : '4'}"
                                                                                  minlength="${isIMPS ? '6' : '3'}"
                                                                                  placeholder="${inputLabel}" value="">
                                                </c:if>
                                                <c:if test="${card.cardScheme ne 'SBIME' && card.cardScheme ne 'MAESTRO'}">
                                                    <input type="text" name="otp" id="cvv-${card.cardId}"
                                                           class="width40 scCvvInput text-input ${isIMPS ? 'medium' : 'small'}-input type-tel"
                                                           maxlength="${isIMPS ? '8' : '4'}"
                                                           minlength="${isIMPS ? '6' : '3'}"
                                                           placeholder="${inputLabel}" value="">
                                                </c:if>
                                            </div>


                                            <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
                                                <div class="scCvvError error error2 clear">${requestScope.validationErrors['INVALID_OTP']}</div>
                                                <script type="text/javascript">
                                                    $("#cvv-" + "${card.cardId}").addClass("error1");
                                                </script>
                                            </c:if>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>

                </c:if>
            </c:forEach>

			<div id="warningDiv" class="hide clear  mt10" style="width: 91%; margin-left: 3%;">
				<div id="errorMsg" class="mt10"></div>
			</div>
            <div class="savedCardbtn hide fl mt20 ${submitBtnClass}" style="margin-left:3%; width:100%;" id="impsCardButton">
                <input name="submit-btn-${card.cardId}" type="submit"
                       class="gry-btn btn-normal ${card.cardType eq 'SBIME' ? 'no-check' : ''}" value="Proceed Securely" id="scSubmit" data-txnmode="SC" onclick="pushGAData(this, 'pay_now_clicked')">
            </div>
            <div class="clear"></div>

        </form>
    </div>
</div>
