<c:if test="${('5' eq paymentType) }">
	<%if(request.getParameter("CARD_TYPE")==null || (!request.getParameter("CARD_TYPE").equals("AMEX"))){ %>
		<div class="divider"></div>
		<div class="form-container">
			<form autocomplete="off" name="creditcard-form" method="post" action="/payment/request/submit">
				<div style="float: left;width: 100%; margin-bottom: 15px">
					<ul style="margin-top: 0px; padding-left: 5px">
						<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
						<c:forEach var="card" items="${savedCardList}" varStatus="count">
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
							
							<c:set var="checkedid" value="" />
							<c:if test="${card.cardId eq requestScope.SCARD_ID }">
							   <c:set var="checkedid" value="checked" />
							</c:if>
							<c:if test="${empty requestScope.SCARD_ID && 0 == count.index}">
							   <c:set var="checkedid" value="checked" />
							</c:if>
							
							<li id="${card.cardId}-item">
								<div>
									<input  type="radio" <c:if test="${'checked' eq  checkedid }">checked</c:if> class="styled" name="savedCardId"  value="${card.cardId}"/>
								</div>
								<span style="color: #333333; font: 400 15px 'Open Sans',Arial,sans-serif; padding-top: 10px; margin-left: 34px;width:86%;display: block;">Use ${card.cardNumber} 
								<c:if test="${!empty cardImagePrefix}">&nbsp;<img src="images/wap/${cardImagePrefix}-card.jpg" alt="" title=""/></c:if>
								<a href="DeleteCardDetails?savedCardId=${card.cardId}&ajax=no" class="delete" >
									<img src="images/wap/delete.png" alt="Delete" title="Delete" />
								</a>
								</span>
							</li>
							
						</c:forEach>
					</ul>
				</div>
				<p>
					<label>CVV/Secure Code</label><input type="tel"  placeholder="Enter" name="cvvNumber"	maxlength="4" />
				</p>
				<c:if test="${'5' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
							<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
				</c:if>
				<input type="hidden" name="txnMde" value="SC" />
				<input type="hidden" name="channelId" value="WAP" />
				<input type="hidden" name="AUTH_MODE" value="3D" />
				<input type="hidden" name="txnMode" value="CC" />
						
				<div class="mt10 margin">
					 <input	name="Submit" type="submit" value="Pay Now" class="button" />
				</div>
				<div class="margin mb15">
					<input id="cancelButton" name="cancleBtn" onClick="cancelTxn();" type="button" class="cancel" value="Cancel" />
				</div>
			</form>
		</div>
	<%} %>
</c:if>