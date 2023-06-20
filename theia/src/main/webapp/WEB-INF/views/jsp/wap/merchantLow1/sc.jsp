<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${5 eq paymentType}">
		<div class="heading">Saved Details</div>
		<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
			<c:if test="${txnConfig.addMoneyFlag}">
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
	<div id ="no-saved-card-left" style="display:none; margin: 10px; color: #f81;"> Please select another payment method to proceed as there are no other saved card left</div>
		<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()" class="${isSCAvailable ? '' : 'hide'}">
			<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
			<input type="hidden" name="txnMode" value="CC" />
			<input type="hidden" name="txn_Mode" value="SC" />
			<input type="hidden" name="txnMde" value="SC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="walletAmount" value="0" />
			<input type="hidden" name = "cvvNumber" id = "cvvNum"/>
			<c:if test="${txnConfig.addMoneyFlag}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
			<c:if test="${txnConfig.addMoneyFlag}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
			</c:if>
		<c:forEach var="card" varStatus="status" items="${savedCardList}">
			
			<c:if test="${card.paymentMode ne 'IMPS'}">
		
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
			<c:set var="cardNumber" value="${card.cardNumber} "></c:set>
			<c:set var="inputLabel" value="CVV"></c:set>
			<c:if test="${isIMPS}">
				<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
				<c:set var="inputLabel" value="OTP"></c:set>
			</c:if>
	
			<c:set var="cardState" value="promo-valid-card"></c:set>
			<c:set var="isCCDCPromo" value="${(fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'CC') ne -1) || (fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'DC') ne -1)}"/>
			<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && isCCDCPromo && txnInfo.promoCodeValid eq false && empty param.showAll}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="row-dot sc-row ${cardState}" data-firstsixdigits="${card.firstSixDigit}">
				<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
				<label>${cardNumber}</label>
				<span class="${card.cardScheme}-sc sc-icon" style="float:none;"></span>
				<%-- <c:if test="${!empty cardImagePrefix}">
					<img src="${ptm:stResPath()}images/wap/merchantLow/${cardImagePrefix}.png" alt="" title="" />
				</c:if>--%>
				<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">Delete</a>
				
				<div class="pad cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
					<c:if test="${!card.iDebitCard}">
					<p class="pad10 cvv-box">
						<label>CVV</label><br /> 
						<c:remove var = "cssClass"/><c:remove var = "errorText"/>
						<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV'] }">
							<c:set value = "error" var="cssClass"></c:set>
							<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
						</c:if>
						<input type="password" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/>
						<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
						${errorText}
					</p>
					</c:if>
					<c:if test="${card.cardType eq 'SBIME'}">
        				<p class="pad10">If your Maestro Card does not have CVV, skip the field</p>		
					</c:if>
					<%-- <c:if test="${entityInfo.iDebitEnabled && card.instId eq 'ICICI' && card.paymentMode eq 'DEBIT_CARD'}">
					<div class="pt7">
						  <!-- For Icici Debit card Only -->
               			 <div id="idebitSavedCard" class="mt10">
                        
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
					</div>
					</c:if>--%>
					<script>
						console.log(' for card id , idebit flag', '${card.cardId}', '${card.iDebitCard}');
					</script>
					<c:if test="${card.iDebitCard}">
						<div class="pt7 ml20">
							  <!-- For Icici Debit card Only -->
							  	
	               			 <div id="idebitSavedCard" class="mt10">
	                        
		                        <div class="idebitOption"> 
		                        <label>
		                        <input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit-${card.cardId}" value="Y" checked onclick ="scOptionBasedCVVCheck('Y')">
		                         <span class ="fl" style="margin-left: 3px;">Use ATM PIN</span>
	                   			<span class="idebit-help small">Enter your ATM PIN in the next step</span>
		                         </label></div>
		                         <div class="clear"></div>
		                         <div class="idebitOption">
		                        <label>
		                        <input type="radio"  class = "pcb checkbox paymentIdebit fl" name="isIciciIDebit-${card.cardId}" value="N" onclick ="scOptionBasedCVVCheck('N')">
		                         <span class ="fl" style="margin-left: 3px;">Use 3D Secure PIN or OTP</span>
	                			<span class="idebit-help small">Enter 3D secure PIN/OTP in the next step</span>
		                         </label>
		                        </div>
	                		</div>
	                		<div class ="clear"></div>
	                		<p class="pad10 cvv-box hide">
								<label>${inputLabel}</label><br /> 
								<c:remove var = "cssClass"/><c:remove var = "errorText"/>
								<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
									<c:set value = "error" var="cssClass"></c:set>
									<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
								</c:if>
								<input type="password" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/>
								<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
								${errorText}
							</p>
	                		<!-- For Icici Debit card Only -->
						</div>
					</c:if>
					<c:if test ="${card.cardSchemeLowSuccessRate ||  card.issuerLowSuccessRate}">
						<div id="warningDiv" class = "failure" style="width: 91%;">
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
					<p class="pt7">
						<input type="submit" value="${submitBtnText}" class="blue-btn" />
					</p>
					<div class="pt7">
						<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
						<div class="fl small secure-text">Your card details are secured via 128 Bit encryption by Verisign</div>
						<div class="clear"></div>
					</div>
				</div>
			</div>
			<c:if test="${!status.last}">
				<div class="dotted"></div>
			</c:if>
			
			</c:if>
		</c:forEach>
		</form>
		
		<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit" onsubmit = "return submitForm()" class="${isIMPSAvailable ? '' : 'hide'}">
			<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="txn_Mode" value="SC" />
			<input type="hidden" name="txnMde" value="IMPS" />
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
			<c:set var="cardNumber" value="${card.cardNumber} | ${!empty card.instId ? card.instId : 'Saved Card'}"></c:set>
			<c:set var="inputLabel" value="CVV"></c:set>
			<c:if test="${isIMPS}">

				<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
				<c:set var="inputLabel" value="OTP"></c:set>
			</c:if>
	
			<c:set var="cardState" value="promo-valid-card"></c:set>
			<c:set var="isCCDCPromo" value="${(fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'CC') ne -1) || (fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'DC') ne -1)}"/>
			<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode && isCCDCPromo && txnInfo.promoCodeValid eq false}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="row-dot sc-row ${cardState}" data-firstsixdigits="${card.firstSixDigit}">
				<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
				<label>${cardNumber}</label><label> : IMPS</label>

				<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">Delete</a>
				
				<div class="pad cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
					<c:if test="${isIMPS}">               				
            			<div class="pad10">MMID : ${card.expiryDate}</div>
            		</c:if>
					<p class="pad10">
						<label>OTP</label><br /> 
						<c:remove var = "cssClass"/><c:remove var = "errorText"/>
						<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_OTP']}">
							<c:set value = "error" var="cssClass"></c:set>
							<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_OTP"]}</span>' var="errorText"></c:set>
						</c:if>
						<input type="password" name="otp" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/>
						<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
						${errorText}
					</p>
					<p class="pt7">
						<input type="submit" value="${submitBtnText}" class="blue-btn" />
					</p>
					<div class="pt7">
						<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
						<div class="fl small secure-text">Your card details are secured via 128 Bit encryption by Verisign</div>
						<div class="clear"></div>
					</div>
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
		if(cardId) {
			document.getElementById(prefix + cardId + suffix).style.display = "block";
			openedCardId = prefix + cardId;
			document.getElementById(prefix + cardId).checked = true;
		} else {
			var firstElem =  document.getElementsByName("savedCardId")[0];
			openedCardId = firstElem.getAttribute('id');
			firstElem.checked = true;
			document.getElementById(openedCardId + suffix).style.display = "block";
		}
		
		 function showSavedCard(obj, id) {
			 if(openedCardId != null) {
				 
				 document.getElementById(openedCardId + suffix).style.display = "none";
				 document.getElementById(openedCardId).checked=false;
			 }
			 openedCardId = id;
			 document.getElementById(id + suffix).style.display = "block";
			 document.getElementById(id).checked=true;
			 
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
		</c:if>
