<div id="creditcard-container" <c:if test="${'1' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="creditcard-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="txn_Mode" value="CC" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
		<div class="query">
			<div class="margin-auto1">
			</div>
			<div class="gray">
				<div class="fl width615">
					<div>
						<div class="fl">
							<label>Card Number</label>
							<input autocomplete="off" type="text" id="cardNumber" name="cardNumber" class="FI" maxlength="16" value="${param.cardNumber}" />
						</div>
						<div class="fl mt6 ml6">
							<img id="masterImg" title="Master card" src="images/web/master.png">
							<img id="visaImg" title="Visa"  src="images/web/visa.png">
							<c:if test="${1 eq amexEnabled}">
								<img id="amexImg" title="Amex"  src="images/web/amex.png">
							</c:if>
						</div>
						<div class="clear"></div>
					</div>
					<c:if test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
						<div class="error">${requestScope.validationErrors['INVALID_CARD']}</div>
					</c:if>
					<div class="mt6">
						<label>Expiry date</label>
						<select name="expiryMonth">
							<option value="0">month</option>
							<c:forEach var="loopVar" begin="1" end="12" step="1">
								<c:set var="selected" value="" />
								<fmt:formatNumber var="formattedLoopVar" value="${loopVar}" pattern="00" />
								<c:if test="${formattedLoopVar eq param.ccExpiryMonth}">
									<c:set var="selected">selected="selected"</c:set>
								</c:if>
								<option value="${formattedLoopVar}" ${selected}>${formattedLoopVar}</option>
							</c:forEach>
						</select>
						<select name="expiryYear">
							<option value="0">year</option>
							<c:forEach var="loopVar" begin="${year}" end="2049" step="1">
								<c:set var="selected" value="" />
								<c:if test="${loopVar eq param.ccExpiryYear}">
									<c:set var="selected">selected="selected"</c:set>
								</c:if>
								<option value="${loopVar}" ${selected}>${loopVar}</option>
							</c:forEach>
						</select>
						
						<div class="clear"></div>
					</div>
					<c:if test="${'1' == paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<div class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
					</c:if>
					<div class="clear"></div>
					<div class="cc-sec mt6" <c:if test="${'AMEX' == cardType}">style="display:none"</c:if>>
						<label  class="fl">CVV</label>
						<input autocomplete="off" type="password" name="cvvNumber" class="fl padd3 width127" size="15" maxlength="3"/>
						
						<div class="clear"></div>
					</div>
					<div class="amex-sec mt6" <c:if test="${'AMEX' != cardType}">style="display:none"</c:if>>
						<label class="fl">Security Code</label>
						<input autocomplete="off" type="password" name="secureCode" class="fl padd3 width127" size="4" maxlength="4" />
					</div>
					<div class="clear"></div>
					<c:if test="${'1' == paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<div class="error">${requestScope.validationErrors['INVALID_CVV']}</div>
					</c:if>
					<div class="clear"></div>
					<c:if test="${1 eq saveCardOption}">
						 <div class="save-card-option ml137">
							<input value="N" name="storeCardFlag" type="hidden" />
							<span class="gry">Save this card for future transaction.</span>
						</div>
					</c:if>
                    <div class="clear"></div>
				</div>
				
				<div class="fr width-180 amex-sec ac" style="display: none;">
					<div class="fr lock left">
						<div class="fl">
							<img title="Secured" alt="Secured" src="images/web/lock.png">
						</div>
						<div class="fl widthh">
							<span class="gry">Your card details are secured via 128 Bit encryption by Verisign.</span>
						</div>
						<div class="clear"></div>
					</div>
					<div class="clear"></div>
					<div class="blue ml10" style="margin:0;">
						<img style="margin-bottom:5px;" title="Security code" alt="Security code" src="images/web/sc.png">
						<br>
						What is Security Code?
						<br>
						<span class="gry">Secure code is a four digits code printed at top of Amex logo on your card.</span>
						</div>
					<div class="clear"></div>
				</div>
				
				<div class="fr width-180 cc-sec ac" style="display: block;">
					<div class="fr lock left">
						<div class="fl">
							<img title="Secured" alt="Secured" src="images/web/lock.png">
						</div>
						<div class="fl widthh">
							<span class="gry">Your card details are secured via 128 Bit encryption by Verisign.</span>
						</div>
						<div class="clear"></div>
					</div>
					<div class="clear"></div>
				
					<div class="blue ml10" style="margin:0;">
						<img style="margin-bottom:5px;" title="CVV" alt="CVV" src="images/web/cvv-no.png">
						<br>
						What is CVV?
						<br>
						<span class="gry">CVV Number is the last 3 digit printed on the signature panel on the back of your credit card</span>
					</div>
					<div class="clear"></div>
				</div>

				<div class="clear"></div>
			</div>
			<!--Right end-->
			<div class="padd-button">
				<div class="margin">
					<input type="submit" class="submit" title="Pay Now" value="Pay Now" />
					<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
				</div>
				<div class="margin italics">(You will be directed to your bank's 3D-Verification page)</div>
				<div class="clear"></div>
			</div>
		</div>
	</form>
</div>