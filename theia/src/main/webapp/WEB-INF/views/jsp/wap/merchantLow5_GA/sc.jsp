<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${5 eq paymentType}">
	<c:choose>
		<c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
			<div class="heading">Saved Details</div>
			<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
		</c:when>
		<c:otherwise>

			<c:if test="${selectedModeType eq 'BANK'}">
			<c:set var="savedCardList"
				   value="${cardInfo.merchantViewSavedCardsList}"></c:set>
			</c:if>

			<c:if test="${txnConfig.addMoneyFlag && selectedModeType eq 'ADD_MONEY'}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
			</c:if>

			<c:set var="isSCAvailable" value="false"></c:set>
			<c:set var="isIMPSAvailable" value="false"></c:set>

			<c:forEach var="card" varStatus="status" items="${savedCardList}">

				<c:if test="${card.paymentMode eq 'IMPS'}">
					<c:set var="isIMPSAvailable" value="true"></c:set>
				</c:if>

				<c:if test="${card.paymentMode ne 'IMPS'}">
					<c:set var="isSCAvailable" value="true"></c:set>
				</c:if>

			</c:forEach>
			<c:set var="irctcData" value="${txnConfig.paymentCharges.CC}"></c:set>
			<fmt:setLocale value="en_US" scope="page"/>
			<fmt:parseNumber var="txnAmountInt" pattern="0.0" type="number" value="${txnInfo.txnAmount}" />
			<fmt:parseNumber var="conveFeeAmountInt" pattern="0.0" type="number" value="${irctcData.feeAmount}" />
			<fmt:parseNumber var="minAmountForAddMoneyVoucher" pattern="0.0" type="number" value="${txnConfig.minAmountForAddMoneyVoucher}" />


			<div class="heading">Saved Details</div>
			<div id ="no-saved-card-left" style="display:none; margin: 10px; color: #f81;"> Please select another payment method to proceed as there are no other saved card left</div>
			<form id="SC-form" autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm(this)" class="${isSCAvailable ? '' : 'hide'}">
				<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
				<input type="hidden" name="txnMode" value="CC" />
				<input type="hidden" name="txnMde" value="SC" />
				<input type="hidden" name="txn_Mode" value="SC" />
				<input type="hidden" name="channelId" value="WAP" />
				<input type="hidden" name="AUTH_MODE" value="3D" />
				<input type="hidden" name="walletAmount" value="0" />
				<input type="hidden" name = "cvvNumber" id = "cvvNum"/>
				<c:if test="${txnConfig.addMoneyFlag}">
					<input type="hidden" name="addMoney" value="1" />
				</c:if>

				<c:forEach var="card" varStatus="status" items="${savedCardList}">

					<c:if test="${card.paymentMode ne 'IMPS' && card.paymentMode ne 'UPI'}">
						<c:choose>
							<c:when test="${'MASTER' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="master" />
							</c:when>
							<c:when test="${'VISA' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="visa" />
							</c:when>
							<c:when test="${'AMEX' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="amex" />
							</c:when>
							<c:when test="${'SBIME' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="maestro" />
							</c:when>
						</c:choose>
						<c:set var="isIMPS" value="false"></c:set>
						<c:set var="cardNumber" value="${card.cardNumber} | ${!empty card.bankName ? card.bankName : 'Saved Card'}"></c:set>
						<c:set var="inputLabel" value="CVV"></c:set>
						<c:if test="${isIMPS}">
							<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
							<c:set var="inputLabel" value="OTP"></c:set>
						</c:if>

						<c:set var="cardState" value="promo-valid-card"></c:set>
						<c:if test="${!empty txnInfo.promoCodeResponse.promoResponseCode && txnInfo.promoCodeValid eq false && empty param.showAll}">
							<c:set var="cardState" value="hide"></c:set>
						</c:if>

						<div class="row-dot sc-row ${cardState}" onclick="onSaveCardTabClick(event,this)" data-firstsixdigits="${card.firstSixDigit}">


							<c:if test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges && conveFeeAmountInt > 0}">
								<!-- AddMONEY with CONVE FEE -->


								<div id="postConvCharge_${card.cardId}" class="postConvCharges" style="display:none; margin-bottom: 10px;font-size: 12px; padding:0 7px;">
									<c:if test="${txnAmountInt  >=  minAmountForAddMoneyVoucher }">
										You will be charged <strong><span class="WebRupee">Rs.</span> ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of  <strong><span class="WebRupee">Rs.</span> ${irctcData.feeAmount}.</strong> We will send Gift Card of <strong><span class="WebRupee">Rs.</span> ${irctcData.feeAmount}</strong> within <strong>24 hrs</strong> for your next purchase on Paytm. Adding money by Debit Card & Net Banking has no fee.
									</c:if>
									<c:if test="${txnAmountInt  < minAmountForAddMoneyVoucher }">
										You will be charged <strong><span class="WebRupee">Rs.</span> ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of <strong><span class="WebRupee">Rs.</span> ${irctcData.feeAmount}.</strong> Adding money by Debit Card & Net Banking has no fee.
									</c:if>
									<div class="clear"></div>
								</div>
								<!-- AddMONEY with CONVE FEE -->
							</c:if>

							<div id="CARDSCHEME" data-value="${card.cardScheme}"></div>
							<div id="BNKCODE" data-value="${card.instId}"></div>
							<div id="CURCARDTYPE" data-value="${card.txnMode}"></div>

							<input type="hidden" id="scCardType_${card.cardId}" value="${card.paymentMode}" class="scCardType" />

							<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
							<label>${cardNumber}</label>
							<span class="${card.cardScheme}-sc sc-icon" style="float:none;"></span>
								<%-- 				<c:if test="${!empty cardImagePrefix}"> --%>
								<%-- 					<img src="${ptm:stResPath()}images/wap/merchantLow/${cardImagePrefix}.png" alt="" title="" /> --%>
								<%-- 				</c:if> --%>
							<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">Delete</a>

							<div class="cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
								<c:if test="${entityInfo.iDebitEnabled && card.instId eq 'ICICI' && card.paymentMode eq 'DEBIT_CARD'}">
									<div class="line-border" style="width: 111%;margin-left: -20px;margin-right: 10px;"></div>
								</c:if>
								<c:if test="${isIMPS}">
									<div class="pad10">MMID : ${card.expiryDate}</div>
								</c:if>
								<c:if test="${!card.iDebitCard}">
									<p class="pad10 cvv-box">
										<label for="cvvBox">${inputLabel}</label><br />
										<c:remove var = "cssClass"/><c:remove var = "errorText"/>
										<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
											<c:set value = "error" var="cssClass"></c:set>
											<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
										</c:if>
										<c:if test="${card.cardScheme eq 'SBIME' || card.cardScheme eq 'MAESTRO'}">
											<input name="cvvBox" type="tel" disabled maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="mask cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0]); removeFieldError(null, this)"/>
										</c:if>
										<c:if test="${card.cardScheme ne 'SBIME' && card.cardScheme ne 'MAESTRO'}">
											<input name="cvvBox" type="tel" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="mask cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0]); removeFieldError(null, this)"/>
										</c:if>

										<!-- change for amex cvv icon -->
										<c:if test="${card.cardScheme eq 'AMEX'}">
											<img src="${ptm:stResPath()}images/wap/merchantLow/security-code.png" alt="CVV" title="AMEX_CVV" width="47" height="24"/>
										</c:if>

										<c:if test="${card.cardScheme ne 'AMEX'}">
											<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
										</c:if>
												${errorText}
									</p>
								</c:if>
								<c:if test="${card.cardType eq 'SBIME'}">
									<p class="pad10">If your Maestro Card does not have CVV, skip the field</p>
								</c:if>
								<script>
									console.log(' for card id , idebit flag', '${card.cardId}', '${card.iDebitCard}');
									console.log("card.iDebitCard => ${card.iDebitCard}");
								</script>
								<c:if test="${card.iDebitCard}">
									<div class="pt7 ml20">
										<!-- For Icici Debit card Only -->

										<div id="idebitSavedCard" class="mt10">

											<div class="idebitOption">
												<label>
													<input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit" value="Y" checked onclick ="scOptionBasedCVVCheck('Y')">
													<span class ="fl" style="margin-left: 3px;">Use ATM PIN</span>
													<span class="idebit-help small">Enter your ATM PIN in the next step</span>
												</label></div>
											<div class="clear"></div>
											<div class="idebitOption">

												<label>
													<input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit" value="N" onclick ="scOptionBasedCVVCheck('N')">
													<span class ="fl" style="margin-left: 3px;">Use 3D Secure PIN or OTP</span>
													<span class="idebit-help small">Enter 3D secure PIN/OTP in the next step</span>
												</label>
											</div>
										</div>
										<div class ="clear"></div>
										<p class="pad10 cvv-box hide">
											<label for="cvvBox">${inputLabel}</label><br />
											<c:remove var = "cssClass"/><c:remove var = "errorText"/>
											<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
												<c:set value = "error" var="cssClass"></c:set>
												<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
											</c:if>
											<input type="tel" name="cvvBox" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="mask cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0]); removeFieldError(null, this)"/>

											<!-- change for amex cvv icon -->
											<c:if test="${card.cardScheme eq 'AMEX'}">
												<img src="${ptm:stResPath()}images/wap/merchantLow/security-code.png" alt="CVV" title="AMEX_CVV" width="47" height="24"/>
											</c:if>

											<c:if test="${card.cardScheme ne 'AMEX'}">
												<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
											</c:if>
												${errorText}
										</p>
										<!-- For Icici Debit card Only -->
									</div>
								</c:if>
								<script>
									//console.log('success rate:','${card.cardSchemeLowSuccessRate}','${card.issuerLowSuccessRate}');
								</script>
								<c:if test ="${card.cardSchemeLowSuccessRate ||  card.issuerLowSuccessRate}">
									<div id="warningDiv" class = "clear failure" style="margin-left: 20px;width: 86%;margin-top: 10px; margin-bottom: 15px; float: left;">
							<span id="errorMsg">
								<%--Display div in case of low success error --%>
								<c:set var="lowSuccessErrorMessage" value="${messageInfo.lowPercentageMessage}" />
								<c:set var="cardSchemeCompleteName" value="${card.cardScheme} cards"/>
								<c:set var="cardInstitutionId" value="${card.instId} bank"/>
								<c:choose>
									<c:when test ="${card.cardSchemeLowSuccessRate}">
										${lowSuccessErrorMessage.replaceAll("@BANK @METHOD",cardSchemeCompleteName)}
									</c:when>

									<c:when test ="${card.issuerLowSuccessRate}">
										${lowSuccessErrorMessage.replaceAll("@BANK @METHOD",cardInstitutionId)}
									</c:when>
								</c:choose>
							</span>
									</div>
								</c:if>
								<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null && txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
									<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
									<div class="post-conv-inclusion" id="hybrid-post-con-${card.cardId}" style="display:none; margin-left: 20px; margin-right:10px; height: 44px;float: left;"
										 data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
										<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null && txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
									<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
									<div class="post-conv-inclusion" id="addnpay-post-con-${card.cardId}" style="display:none;margin-left: 20px; margin-right:10px; height: 44px;float: left;" data-post-bal = "Pay <span class='WebRupee'>Rs</span>  ${valueOfBalanceToBeDisplayed}">
										<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test="${card.cardType eq 'CREDIT_CARD' && txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.CC != null && txnConfig.paymentCharges.CC.totalConvenienceCharges ne '0.00'}">
									<div class="post-conv-inclusion" id="normal-post-con-${card.cardId}" style="margin-left: 20px; margin-right:10px; height: 44px;">
										<%@ include file="../../common/post-con/cc-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test="${card.cardType eq 'DEBIT_CARD' && txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.DC != null && txnConfig.paymentCharges.DC.totalConvenienceCharges ne '0.00'}">
									<div class="post-conv-inclusion" id="normal-post-con-${card.cardId}"  style="margin-left: 20px; margin-right:10px; height: 44px;float: left;">
										<%@ include file="../../common/post-con/dc-postconv.jsp" %>
									</div>
								</c:if>
								<p class="pt7 ml20">
									<button type="submit" class="blue-btn" id="btnSubmit-${card.cardId}"  data-txnmode="SC" onclick="pushGAData(this, 'pay_now_clicked')">Pay Now</button>
								</p>
							</div>
						</div>
						<c:if test="${!status.last}">
							<div class="dotted"></div>
						</c:if>

					</c:if>


					<c:if test="${card.paymentMode eq 'UPI'}">
						<c:choose>
							<c:when test="${'MASTER' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="master" />
							</c:when>
							<c:when test="${'VISA' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="visa" />
							</c:when>
							<c:when test="${'AMEX' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="amex" />
							</c:when>
							<c:when test="${'SBIME' eq card.cardScheme}">
								<c:set var="cardImagePrefix" value="maestro" />
							</c:when>
						</c:choose>
						<c:set var="isIMPS" value="false"></c:set>
						<c:set var="cardNumber" value="${card.cardNumber} | BHIM UPI"></c:set>
						<c:set var="inputLabel" value="CVV"></c:set>
						<c:if test="${isIMPS}">
							<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
							<c:set var="inputLabel" value="OTP"></c:set>
						</c:if>

						<c:set var="cardState" value="promo-valid-card"></c:set>
						<c:if test="${!empty txnInfo.promoCodeResponse.promoResponseCode && txnInfo.promoCodeValid eq false && empty param.showAll}">
							<c:set var="cardState" value="hide"></c:set>
						</c:if>

						<div class="row-dot sc-row ${cardState}" onclick="onSaveCardTabClick(event,this)" data-firstsixdigits="${card.firstSixDigit}">


							<c:if test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges && conveFeeAmountInt > 0}">
								<!-- AddMONEY with CONVE FEE -->


								<div id="postConvCharge_${card.cardId}" class="postConvCharges" style="display:none; margin-bottom: 10px;font-size: 12px; padding:0 7px;">
									<c:if test="${txnAmountInt  >=  minAmountForAddMoneyVoucher }">
										You will be charged <strong>Rs. ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of  <strong>Rs. ${irctcData.feeAmount}.</strong> We will send Gift Card of <strong>Rs. ${irctcData.feeAmount}</strong> within <strong>24 hrs</strong> for your next purchase on Paytm. Adding money by Debit Card & Net Banking has no fee.
									</c:if>
									<c:if test="${txnAmountInt  < minAmountForAddMoneyVoucher }">
										You will be charged <strong>Rs. ${irctcData.totalTransactionAmount}</strong> including Credit Card Fee of <strong>Rs. ${irctcData.feeAmount}.</strong> Adding money by Debit Card & Net Banking has no fee.
									</c:if>
									<div class="clear"></div>
								</div>
								<!-- AddMONEY with CONVE FEE -->
							</c:if>



							<input type="hidden" id="scCardType_${card.cardId}" value="${card.paymentMode}" class="scCardType" />

							<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
							<label>${cardNumber}</label>
							<span class="${card.paymentMode}-sc sc-icon" style="float:none;"></span>
								<%-- 				<c:if test="${!empty cardImagePrefix}"> --%>
								<%-- 					<img src="${ptm:stResPath()}images/wap/merchantLow/${cardImagePrefix}.png" alt="" title="" /> --%>
								<%-- 				</c:if> --%>
							<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">Delete</a>

							<div class="cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
								<c:if test="${entityInfo.iDebitEnabled && card.instId eq 'ICICI' && card.paymentMode eq 'DEBIT_CARD'}">
									<div class="line-border" style="width: 111%;margin-left: -20px;margin-right: 10px;"></div>
								</c:if>
								<c:if test="${isIMPS}">
									<div class="pad10">MMID : ${card.expiryDate}</div>
								</c:if>

								<script>
									console.log('success rate:','${card.cardSchemeLowSuccessRate}','${card.issuerLowSuccessRate}');
								</script>
								<c:if test ="${card.cardSchemeLowSuccessRate ||  card.issuerLowSuccessRate}">
									<div id="warningDiv" class = "clear failure" style="margin-left: 20px;width: 86%;margin-top: 10px; margin-bottom: 15px; float: left;">
							<span id="errorMsg">
								<%--Display div in case of low success error --%>
								<c:set var="lowSuccessErrorMessage" value="${messageInfo.lowPercentageMessage}" />
								<c:set var="cardSchemeCompleteName" value="${card.cardScheme} cards"/>
								<c:set var="cardInstitutionId" value="${card.instId} bank"/>
								<c:choose>
									<c:when test ="${card.cardSchemeLowSuccessRate}">
										${lowSuccessErrorMessage.replaceAll("@BANK @METHOD",cardSchemeCompleteName)}
									</c:when>

									<c:when test ="${card.issuerLowSuccessRate}">
										${lowSuccessErrorMessage.replaceAll("@BANK @METHOD",cardInstitutionId)}
									</c:when>
								</c:choose>
							</span>
									</div>
								</c:if>
								<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null && txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
									<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
									<div class="post-conv-inclusion" id="hybrid-post-con-${card.cardId}" style="display:none; margin-left: 20px; margin-right:10px; height: 44px;float: left;"
										 data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
										<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null && txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
									<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
									<div class="post-conv-inclusion" id="addnpay-post-con-${card.cardId}" style="display:none;margin-left: 20px; margin-right:10px; height: 44px;float: left;" data-post-bal = "Pay <span class='WebRupee'>Rs</span>  ${valueOfBalanceToBeDisplayed}">
										<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test="${card.cardType eq 'CREDIT_CARD' && txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.CC != null && txnConfig.paymentCharges.CC.totalConvenienceCharges ne '0.00'}">
									<div class="post-conv-inclusion" id="normal-post-con-${card.cardId}" style="margin-left: 20px; margin-right:10px; height: 44px;">
										<%@ include file="../../common/post-con/cc-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test="${card.cardType eq 'DEBIT_CARD' && txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.DC != null && txnConfig.paymentCharges.DC.totalConvenienceCharges ne '0.00'}">
									<div class="post-conv-inclusion" id="normal-post-con-${card.cardId}"  style="margin-left: 20px; margin-right:10px; height: 44px;float: left;">
										<%@ include file="../../common/post-con/dc-postconv.jsp" %>
									</div>
								</c:if>
								<c:if test="${card.cardType eq 'UPI' && txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.UPI != null && txnConfig.paymentCharges.UPI.totalConvenienceCharges ne '0.00'}">
									<div class="post-conv-inclusion" id="normal-post-con-${card.cardId}"  style="margin-left: 20px; margin-right:10px; height: 44px;float: left;">
										<%@ include file="../../common/post-con/upi-postconv.jsp" %>
									</div>
								</c:if>

								<p class="pt7 ml20">
									<button type="submit" class="blue-btn" id="btnSubmit-${card.cardId}"  data-txnmode="SC" onclick="pushGAData(this, 'pay_now_clicked')" >Pay Now</button>
								</p>
							</div>
						</div>
						<c:if test="${!status.last}">
							<div class="dotted"></div>
						</c:if>

					</c:if>



				</c:forEach>
			</form>

			<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit" onsubmit = "return submitForm(this)" class="${isIMPSAvailable ? '' : 'hide'}">
				<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
				<input type="hidden" name="txnMode" value="IMPS" />
				<input type="hidden" name="txnMde" value="IMPS" />
				<input type="hidden" name="txn_Mode" value="SC" />
				<input type="hidden" name="channelId" value="WAP" />
				<input type="hidden" name="AUTH_MODE" value="OTP" />
				<input type="hidden" name="walletAmount" value="0" />
				<input type="hidden" name = "cvvNumber" id = "cvvNum"/>
				<c:if test="${txnConfig.addMoneyFlag}">
					<input type="hidden" name="addMoney" value="1" />
				</c:if>
				<c:forEach var="card" varStatus="status" items="${savedCardList}">

					<c:if test="${card.paymentMode eq 'IMPS'}">
						<c:set var="isIMPS" value="true"></c:set>
						<c:set var="cardNumber" value="${card.cardNumber} | ${!empty card.bankName ? card.bankName : 'Saved Card'}"></c:set>
						<c:set var="inputLabel" value="CVV"></c:set>
						<c:if test="${isIMPS}">

							<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
							<c:set var="inputLabel" value="OTP"></c:set>
						</c:if>

						<c:set var="cardState" value="promo-valid-card"></c:set>
						<c:if test="${!empty txnInfo.promoCodeResponse.promoResponseCode && txnInfo.promoCodeValid eq false}">
							<c:set var="cardState" value="hide"></c:set>
						</c:if>

						<div class="row-dot sc-row ${cardState}" onclick="onSaveCardTabClick(event,this)" data-firstsixdigits="${card.firstSixDigit}">
							<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
							<label>${cardNumber}</label><label> : IMPS</label>

							<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">Delete</a>

							<div class="cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
								<c:if test="${isIMPS}">
									<div class="pad10">MMID : ${card.expiryDate}</div>
								</c:if>
								<p class="pad10">
									<label>${inputLabel}</label><br />
									<c:remove var = "cssClass"/><c:remove var = "errorText"/>
									<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP'] }">
										<c:set value = "error" var="cssClass"></c:set>
										<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_OTP"]}</span>' var="errorText"></c:set>
									</c:if>
									<input type="tel" name="otp" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="mask cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0])"/>

									<!-- change for amex cvv icon -->
									<c:if test="${card.cardScheme eq 'AMEX'}">
										<img src="${ptm:stResPath()}images/wap/merchantLow/security-code.png" alt="CVV" title="AMEX_CVV" width="47" height="24"/>
									</c:if>

									<c:if test="${card.cardScheme ne 'AMEX'}">
										<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
									</c:if>
											${errorText}
								</p>
								<p class="pt7 ml20">
									<button type="submit" class="blue-btn" id="btnSubmit-${card.cardId}"  data-txnmode="SC"  onclick="pushGAData(this, 'pay_now_clicked')" >Pay Now</button>
								</p>
							</div>
						</div>
						<c:if test="${!status.last}">
							<div class="dotted"></div>
						</c:if>

					</c:if>
				</c:forEach>
			</form>


			<script type = "text/javascript">
				var cardId = ${empty requestScope.SCARD_ID ? false : requestScope.SCARD_ID};
				var prefix = "cvv_";
				var suffix = "_pad";

                //to get default savedcard bank code and card scheme
                var firstBnkCode = document.getElementById("BNKCODE");
                var firstCardScheme = document.getElementById("CARDSCHEME");
                var firstCardType = document.getElementById("CURCARDTYPE");
                curcard = (firstBnkCode && firstBnkCode.getAttribute("data-value")) ? firstBnkCode.getAttribute("data-value") : '';
                curcardschme = (firstCardScheme && firstCardScheme.getAttribute("data-value")) ? firstCardScheme.getAttribute("data-value") : '';
                curcardtype = (firstCardType && firstCardType.getAttribute("data-value"))? firstCardType.getAttribute("data-value") : '' ;


                //post conv changes
				//calculation of addMoney flag
				// caluclation to be done when paytmcash box is ticked and post conv fee is
				var isPostConvEnabled = document.getElementById('payment-details').getAttribute('data-postconv-enabled');
				//console.log('inside post conv');
				if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
					if ($("#totalAmtVal")[0] && $("#totalAmtVal")[0] != undefined)
						var totalAmount = Number($("#totalAmtVal")[0].value);
					if ($("#totalWalletVal")[0] && $("#totalWalletVal")[0] != undefined)
						var walletBalance = Number($("#totalWalletVal")[0].value);
					var fullWallet = (walletBalance >= totalAmount);
					if ($('#config')[0] &&  $('#config')[0] != undefined)
						var isAddMoneyAvailable = $('#config')[0].getAttribute('data-addmoney') == 'true' ? true : false;
					var addMoney = !fullWallet && isAddMoneyAvailable;
				}

				if(cardId) {
					document.getElementById(prefix + cardId + suffix).style.display = "block";
					openedCardId = prefix + cardId;
					document.getElementById(prefix + cardId).checked = true;

					var scCheckboxBtn=document.getElementById(prefix + cardId);

					if(scCheckboxBtn){
						isIciciDebit(scCheckboxBtn);
					}

					if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
						// POST CONVE FEE FOR CC
						var scCardType=document.getElementById("scCardType_"+cardId);
						var openedCard=document.getElementById(openedCardId);
						if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
							if(document.getElementById("postConvCharge_"+openedCard.value)){
								if(scCardType.value=="CC"){
									document.getElementById("postConvCharge_"+openedCard.value).style.display = "block";
								}
								else{
									document.getElementById("postConvCharge_"+openedCard.value).style.display = "none";
								}
							}
						}
						//End of CONVE FEE FOR CC
					}

				} else {
					var firstElem =  document.getElementsByName("savedCardId")[0];

					if(firstElem){
						isIciciDebit(firstElem);
					}

					openedCardId = firstElem.getAttribute('id');
					firstElem.checked = true;
					document.getElementById(openedCardId + suffix).style.display = "block";

					if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
						// post conv fee column
						var idForPostConv = openedCardId.substring(4);
						// checked with wallet checked
						if($("#paytmCashCB")[0] != undefined && $("#paytmCashCB")[0].checked){
							// code of hybrid or addNPay

							$('#normal-post-con-' + idForPostConv).hide();

							if(addMoney){
								$('#addnpay-post-con-' + idForPostConv).show();
								if($('#addnpay-post-con-' + idForPostConv)[0])
									$('#btnSubmit-' + idForPostConv)[0].innerHTML = $('#addnpay-post-con-' + idForPostConv)[0].getAttribute('data-post-bal');
							} else {
								$('#hybrid-post-con-' + idForPostConv).show();
								if($('#hybrid-post-con-' + idForPostConv)[0])
									$('#btnSubmit-' + idForPostConv)[0].innerHTML = $('#hybrid-post-con-' + idForPostConv)[0].getAttribute('data-post-bal');
							}
							// changing pay now text in button

						} else{
							// code for normal fee
							$('#normal-post-con-' +idForPostConv).show();
							$('#hybrid-post-con-'+ idForPostConv).hide();
						}

						// POST CONVE FEE FOR CC

						var openedCard=document.getElementById(openedCardId);
						var scCardType=document.getElementById("scCardType_"+openedCard.value);

						if(document.getElementById("postConvCharge_"+openedCard.value)){
							if(scCardType.value=="CREDIT_CARD"){
								document.getElementById("postConvCharge_"+openedCard.value).style.display = "block";
							}
							else{
								document.getElementById("postConvCharge_"+openedCard.value).style.display = "none";
							}
						}
						//End of CONVE FEE FOR CC
					}
				}

				function showSavedCard(obj, id) {
					if(openedCardId != null) {

						document.getElementById(openedCardId + suffix).style.display = "none";
						document.getElementById(openedCardId).checked=false;
						if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
							// CC CHARGES
							var CC_ChargesTxt=document.getElementsByClassName("postConvCharges");
							if(CC_ChargesTxt.length > 0){
								for(var i=0;i<CC_ChargesTxt.length;i++){
									CC_ChargesTxt[i].style.display = "none";
								}

							}
						}
					}

					if(obj){
						isIciciDebit(obj);
					}

					openedCardId = id;
					document.getElementById(id + suffix).style.display = "block";
					document.getElementById(id).checked=true;
					if (isPostConvEnabled == 'true' || isPostConvEnabled == true) {
						// post conv fee column
						var idForPostConv = id.substring(4);
						// checked with wallet checked
						if($("#paytmCashCB")[0].checked){
							// code of hybrid or addNPay
							//TODO for addNPay
							$('#normal-post-con-' + idForPostConv).hide();
							$('#hybrid-post-con-' + idForPostConv).show();
							// changing pay now text in button
							if(addMoney){
								$('#addnpay-post-con-' + idForPostConv).show();
								if($('#addnpay-post-con-' + idForPostConv)[0])
									$('#btnSubmit-' + idForPostConv)[0].innerHTML = $('#addnpay-post-con-' + idForPostConv)[0].getAttribute('data-post-bal');
							} else {
								$('#hybrid-post-con-' + idForPostConv).show();
								if($('#hybrid-post-con-' + idForPostConv)[0])
									$('#btnSubmit-' + idForPostConv)[0].innerHTML = $('#hybrid-post-con-' + idForPostConv)[0].getAttribute('data-post-bal');
							}
						} else{
							// code for normal fee
							$('#normal-post-con-' +idForPostConv).show();
							$('#hybrid-post-con-'+ idForPostConv).hide();
						}

						// POST CONVE FEE FOR CC
						var scCardType=document.getElementById("scCardType_"+obj.value);
						if(document.getElementById("postConvCharge_"+obj.value)){
							if(scCardType.value=="CREDIT_CARD") {
								document.getElementById("postConvCharge_"+obj.value).style.display = "block";
							} else {
								document.getElementById("postConvCharge_"+obj.value).style.display = "none";
							}
						}


					}
					var scCardType=document.getElementById("scCardType_"+obj.value);
					var cvvBox=document.getElementById("cvvNum");
					if(scCardType.value=="UPI") {
						cvvBox.disabled=true;
					}
					else {
						cvvBox.disabled=false;
					}


				}

				function isIciciDebit(obj){
					if(obj){
						var cvvBox=obj.parentElement.querySelector(".cvv");
						if(cvvBox){
							var isIdebit=cvvBox.querySelector("input.paymentIdebit");
							if(isIdebit){
								var idebitInputs=cvvBox.getElementsByClassName("paymentIdebit");

								if(idebitInputs.length > 0){
									for(var i=0;i<idebitInputs.length;i++){
										idebitInputs[i].disabled = false;
									}

								}

							} else
							{
								var idebits=document.getElementsByClassName("paymentIdebit");
								if(idebits.length > 0){
									for(var i=0;i<idebits.length;i++){
										idebits[i].disabled = true;
									}

								}

							}
						}

					}
				}

				function setCVV() {
					document.getElementById("cvvNum").value = document.getElementById(openedCardId + "_input").value;
				}
			</script>
			<script>
				try {
					_paq.push(['trackEvent', 'Payment Mode', 'sc']);
				} catch(e){}
			</script>
		</c:otherwise>
	</c:choose>
</c:if>
