<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${5 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading ml15 mt20" style="padding-left: 5px;" >Saved Cards</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
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
		<div class="heading ml15 mt20" style="padding-left: 5px;">Saved Cards</div>
		<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()" class="${isSCAvailable ? '' : 'hide'}">
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
			
			<c:if test="${card.paymentMode ne 'IMPS'}">
		
			<c:choose>
				<c:when test="${'MASTER' eq card.cardType}">
					<c:set var="cardImagePrefix" value="master" />
				</c:when>
				<c:when test="${'VISA' eq card.cardType}">
					<c:set var="cardImagePrefix" value="visa" />
				</c:when>
				<c:when test="${'AMEX' eq card.cardType}">
					<c:set var="cardImagePrefix" value="amex" />
				</c:when>
				<c:when test="${'SBIME' eq card.cardType}">
					<c:set var="cardImagePrefix" value="maestro" />
				</c:when>
			</c:choose>
			
			<c:set var="isIMPS" value="false"></c:set>
			<c:set var="cardNumber" value="${card.cardNumber}"></c:set>
			<c:set var="cardBankName" value ="${!empty card.bankName ? card.bankName : 'Saved Card'}"></c:set>
			<c:set var="inputLabel" value="CVV"></c:set>
			<c:if test="${isIMPS}">
				<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
				<c:set var="inputLabel" value="OTP"></c:set>
			</c:if>
	
			<c:set var="cardState" value="promo-valid-card"></c:set>
			<c:if test="${!empty txnInfo.promoCodeResponse.promoResponseCode && txnInfo.promoCodeValid eq false && empty param.showAll}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="row-dot sc-row ${cardState} mt10" data-firstsixdigits="${card.firstSixDigit}" style="border: 1px solid #ebebeb;border-radius: 2px;">
				<div class="radio-item" style="margin-bottom: 10px;">
					<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'${card.cardId}')" />
					
					<label id="label_${card.cardId}" for="cvv_${card.cardId}" style="color: #999;font-size: 16px;">${cardNumber}</label>
					<span class="${card.cardScheme}-sc sc-icon" style="float:none; padding-right: 40px;"></span>
					<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard">&nbsp;</a>
					<div class="bank-name-sc" id="bank_${card.cardId}" style="color:#999;">
						<span>${cardBankName}</span>
					</div>
				</div>
<%-- 				<c:if test="${!empty cardImagePrefix}"> --%>
<%-- 					<img src="${ptm:stResPath()}images/wap/merchantLow/${cardImagePrefix}.png" alt="" title="" /> --%>
<%-- 				</c:if> --%>
				
				
				<div class="pad cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
					
					<c:if test="${isIMPS}">               				
            			<div class="pad10">MMID : ${card.expiryDate}</div>
            		</c:if>
					<p class="pad10 cvv-box">
						<c:remove var = "cssClass"/><c:remove var = "errorText"/>
						<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
							<c:set value = "error" var="cssClass"></c:set>
							<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
						</c:if>
						<input type="password" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass} ml5" placeholder="CVV" onkeypress="onKeypressCvvNumber(arguments[0])"/>
						<img src="/theia/resources/images/wap/merchantLow3/c-v-v-example.png" alt="CVV" title="CVV" style="margin-left: -40px;"/>
						${errorText}
						
					</p>
					<c:if test="${card.cardType eq 'SBIME'}">
        				<p class="pad10">If your Maestro Card does not have CVV, skip the field</p>		
					</c:if>
					
				</div>
			</div>
			<div id="savedBtn_${card.cardId}_pad" style="display:none;">
				<c:if test="${entityInfo.iDebitEnabled && card.instId eq 'ICICI' && card.paymentMode eq 'DEBIT_CARD'}">
					<div class="pt7 ml15">
						  <!-- For Icici Debit card Only -->
               			 <div id="idebitSavedCard" class="mt10">
                        	<div class="radio-item" style="margin-bottom: 10px;">
	                        	<div class="idebitOption"> 
	                        	<input type="radio" id="idebitOption-1" class = "pcb checkbox paymentIdebit" name="isIciciIDebit-${card.cardId}" value="Y" checked>
	                         	<label for="idebitOption-1" style="font-size: 14px;">Use ATM PIN</label></div>
	                         </div>
	                         <div class="radio-item" style="margin-bottom: 10px;">
		                         <div class="idebitOption">
		                        	<input type="radio" id="idebitOption-2" class = "pcb checkbox paymentIdebit" name="isIciciIDebit-${card.cardId}" value="N">
		                        	<label for="idebitOption-2" style="font-size: 14px;">Use 3D Secure PIN or OTP</label>
		                        </div>
	                        </div>
                		</div>
                <!-- For Icici Debit card Only -->
					</div>
					</c:if>
				
					<c:if test ="${card.cardSchemeLowSuccessRate ||  card.issuerLowSuccessRate}">
						<div id="warningDiv" class = "failure low-success-rate" style="margin-left: 24px;margin-right: 25px; margin-bottom: 10px;">
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
					<c:if test="${card.cardType eq 'CREDIT_CARD'}">
						<div class="post-conv-inclusion" style="margin-left: 25px; margin-right:25px; height: 44px;">
							<%@ include file="../../common/post-con-withr/cc-postconv.jsp" %>
						</div>
					</c:if>
					<c:if test="${card.cardType eq 'DEBIT_CARD'}">
						<div class="post-conv-inclusion" style="margin-left: 25px; margin-right:25px; height: 44px;float: left;">
							<%@ include file="../../common/post-con-withr/dc-postconv.jsp" %>
						</div>
					</c:if>
				<p class="ml25 mr25" style="margin-right: 25px;">
					<button type="submit" class="blue-btn sc-special-btn"  style="width: 100%;">
						${submitBtnText}
					</button>
				</p>
				<div class="pt7 mt15" style="margin-left: 23px;margin-bottom: 25px;">
					<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
       				<div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        			<div class="fl lock-display"> We do not share your card details with anyone</div>
					<div class="clear"></div>
				</div>
			</div>
			<c:if test="${!status.last}">
				
			</c:if>
			
			</c:if>
		</c:forEach>
		</form>
		
		<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit" onsubmit = "return submitForm()" class="${isIMPSAvailable ? '' : 'hide'}">
			<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="txnMde" value="IMPS" />
			<input type="hidden" name="txn_Mode" value="IMPS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			<input type="hidden" name="walletAmount" value="0" />
			<input type="hidden" name = "cvvNumber" id = "cvvNum"/>
			<c:if test="${txnConfig.addMoneyFlag}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
		<c:forEach var="card" varStatus="status" items="${savedCardList}">
		
			<c:if test="${card.paymentMode eq 'IMPS'}">
			
			<c:choose>
				<c:when test="${'MASTER' eq card.cardType}">
					<c:set var="cardImagePrefix" value="master" />
				</c:when>
				<c:when test="${'VISA' eq card.cardType}">
					<c:set var="cardImagePrefix" value="visa" />
				</c:when>
				<c:when test="${'AMEX' eq card.cardType}">
					<c:set var="cardImagePrefix" value="amex" />
				</c:when>
				<c:when test="${'SBIME' eq card.cardType}">
					<c:set var="cardImagePrefix" value="maestro" />
				</c:when>
			</c:choose>
			
			<c:set var="isIMPS" value="true"></c:set>
			<c:set var="cardNumber" value="${card.cardNumber}"></c:set>
			<c:set var="cardBankName" value ="${!empty card.bankName ? card.bankName : 'Saved Card'}"></c:set>
			<c:set var="inputLabel" value="CVV"></c:set>
			<c:if test="${isIMPS}">
				<c:set var="cardNumber" value="${card.holderMobileNo}"></c:set>
				<c:set var="inputLabel" value="OTP"></c:set>
			</c:if>
	
			<c:set var="cardState" value="promo-valid-card"></c:set>
			<c:if test="${!empty txnInfo.promoCodeResponse.promoResponseCode && txnInfo.promoCodeValid eq false}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="row-dot sc-row ${cardState} mt10" data-firstsixdigits="${card.firstSixDigit}" style="border: 1px solid #ebebeb; border-radius: 2px;">
<%-- 				<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" /> --%>
<%-- 				<label for ="cvv_${card.cardId}">${cardNumber}</label> --%>
				<div class="radio-item" style="margin-bottom: 10px;">
					<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'${card.cardId}')" />
					
					<label id="label_${card.cardId}" for="cvv_${card.cardId}" style="color: #999;">${cardNumber}<span> : IMPS</span></label>
<%-- 					<span class="${card.cardScheme}-sc sc-icon" style="float:none; padding-right: 40px;"></span> --%>
					<a href="javascript:void(0)" onclick = "deleteCard(${card.cardId})" class="deleteCard"></a>
					<div class="bank-name-sc" id="bank_${card.cardId}" style="color:#999;">
						<span>${cardBankName}</span>
					</div>
				</div>
				
				
				<div class="pad cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
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
						<input type="password" name="otp" maxlength="${isIMPS ? '8' : '4'}" size="9" id = "cvv_${card.cardId}_input" autocomplete="off" class ="cvv-input ${isIMPS ? 'medium-input' : ''} ${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])"/>
						<img src="${ptm:stResPath()}images/wap/merchantLow/cvv.png" alt="CVV" title="CVV" />
						${errorText}
					</p>
					
				</div>
			</div>
			<div id="savedBtn_${card.cardId}_pad" style="display:none;">
				<p class="pt7 ml25 mr10">
					<button type="submit" class="blue-btn sc-special-btn">
						${submitBtnText}
					</button>
				</p>
				<div class="pt7 mt15" style="margin-left: 23px;margin-bottom: 25px;">
					<div class="fl image"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
       				<div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        			<div class="fl lock-display"> We do not share your card details with anyone</div>
					<div class="clear"></div>
				</div>
			</div>
			
			
			</c:if>
		</c:forEach>
		</form>
		
		
		<script type = "text/javascript">
		var cardId = ${empty requestScope.SCARD_ID ? false : requestScope.SCARD_ID};
		var prefix = "cvv_";
		var suffix = "_pad";
		var prefixBtn="savedBtn_";
		if(cardId) {
			document.getElementById(prefix + cardId + suffix).style.display = "block";
			document.getElementById(prefixBtn + cardId + suffix).style.display = "block";
			openedCardId = prefix + cardId;
			document.getElementById(prefix + cardId).checked = true;
			document.getElementById("label_" +openedCardId.substring(4)).style.color ="#222";
			document.getElementById("label_" +openedCardId.substring(4)).style.fontWeight ="600";
			document.getElementById("bank_" +openedCardId.substring(4)).style.color ="#666";
		} else {
			var firstElem =  document.getElementsByName("savedCardId")[0];
			openedCardId = firstElem.getAttribute('id');
			firstElem.checked = true;
			document.getElementById(openedCardId + suffix).style.display = "block";
			document.getElementById(prefixBtn+openedCardId.substring(4) + suffix).style.display = "block";
			document.getElementById("label_" +openedCardId.substring(4)).style.color ="#222";
			document.getElementById("label_" +openedCardId.substring(4)).style.fontWeight ="600";
			document.getElementById("bank_" +openedCardId.substring(4)).style.color ="#666";
		}
		
		 function showSavedCard(obj, id) {
			 if(openedCardId != null) {
				 console.log('inside open card id,',openedCardId);
				 document.getElementById(openedCardId + suffix).style.display = "none";
				 document.getElementById(openedCardId).checked = false;
				 document.getElementById(prefixBtn+openedCardId.substring(4) + suffix).style.display = "none";
				 document.getElementById("label_" +openedCardId.substring(4)).style.color ="#999";
				 document.getElementById("label_" +openedCardId.substring(4)).style.fontWeight ="400";
				 document.getElementById("bank_" +openedCardId.substring(4)).style.color ="#666";
				
			 } 
			 openedCardId = prefix + id;
			 console.log('id,val',id,suffix);
			 document.getElementById(prefix + id + suffix).style.display = "block";
			 document.getElementById(prefixBtn + id + suffix).style.display = "block";
			 document.getElementById(prefix + id ).nextSibling.nextSibling.style.color ="#222";
			 document.getElementById(prefix + id ).nextSibling.nextSibling.style.fontWeight ="600";
			 document.getElementById("bank_" +id).style.color ="#666";
			 document.getElementById(prefix + id).checked=true;
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
	<div class="mt20"></div>
</c:if>
