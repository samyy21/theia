<c:if test="${5 eq paymentType}">
		<div class="heading">Saved Cards</div>
		<form autocomplete="off" method="post" action="" onsubmit = "submitForm()">
			<input type="hidden" name="txnMode" value="CC" />
			<input type="hidden" name="txn_Mode" value="SC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="walletAmount" value="0" />
			<input type="hidden" name = "cvvNumber" id = "cvvNum"/>
			<c:if test="${isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
			<c:if test="${txnConfig.addMoneyFlag}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
			</c:if>
		<c:forEach var="card" varStatus="status" items="${savedCardList}">
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
			</c:choose>
	
			<c:set var="cardState" value="promo-valid-card"></c:set>
			<c:if test="${!empty sessionScope.promoCode && txnInfo.promoValid eq false}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="row-dot sc-row ${cardState}" data-firstsixdigits="${card.firstSixDigit}">
				<input type="radio" name="savedCardId" value="${card.cardId}" id = "cvv_${card.cardId}" onclick="showSavedCard(this,'cvv_${card.cardId}')" />
				<label>${card.cardNumber}</label>
				<c:if test="${!empty cardImagePrefix}">
					<img src="/images/wap/paytmAppLow/${cardImagePrefix}.png" alt="" title="" />
				</c:if>
				<a href="DeleteCardDetails?savedCardId=${card.cardId}&ajax=no" class="deleteCard">Delete</a>
				
				<div class="pad cvv" style = "display:none" id = "cvv_${card.cardId}_pad">
					<p class="pad10">
						<label>CVV</label><br /> 
						<c:remove var = "cssClass"/><c:remove var = "errorText"/>
						<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV'] && card.cardId eq requestScope.SCARD_ID}">
							<c:set value = "error" var="cssClass"></c:set>
							<c:set value = '<br/><span class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</span>' var="errorText"></c:set>
						</c:if>
						<input type="password" maxlength="3" size="9" id = "cvv_${card.cardId}_input" class = "${cssClass}" onkeypress="onKeypressCvvNumber(arguments[0])" />
						<img src="/images/wap/paytmAppLow/cvv.png" alt="CVV" title="CVV" />
						${errorText}
					</p>
					<p class="pt7 ml20">
						<input type="submit" value="${submitBtnText}" class="blue-btn" />
					</p>
					<div class="pt7">
						<div class="fl image"><img src="/images/wap/paytmAppLow/lock.png" alt="" title="" /></div>
						<div class="fl small">Your card details are secured via 128 Bit encryption by Verisign</div>
						<div class="clear"></div>
					</div>
				</div>
			</div>
			<c:if test="${!status.last}">
				<div class="dotted"></div>
			</c:if>
		</c:forEach>
		</form>
		<script type = "text/javascript">
		var cardId = '${empty requestScope.SCARD_ID ? false : requestScope.SCARD_ID}';
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
			 }
			 openedCardId = id;
			 document.getElementById(id + suffix).style.display = "block";
			 
			 
		 }
		 
		 function setCVV() {
			 document.getElementById("cvvNum").value = document.getElementById(openedCardId + "_input").value;
		 }
		</script>
		</c:if>