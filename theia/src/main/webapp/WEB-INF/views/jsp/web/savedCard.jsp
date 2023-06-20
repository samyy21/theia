<div id="savedcard-container" <c:if test="${'5' eq paymentType }">style="display:block"</c:if>>
	<form autocomplete="off" name="creditcard-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="txn_Mode" value="CC" />
		<input type="hidden" name="txnMde" value="SC" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="savedCardId" id="savedCardId" value="" />
		<div class="query">
			<div class="padd">
				<div class="fl">
					<ul class="save-card padd1" style="float: left;">
						<c:forEach var="card"  varStatus="status" items="${cardInfo.savedCardsList}">
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
							<li id="${card.cardIdentifier}-item">
								<span id="${card.cardIdentifier}">
									<div class="fl label">Use ${card.cardNumber} </div>
									<div class="fl ml10" style="width: 69px">
										<img class="cardPad"  src="images/web/${cardImagePrefix}.jpg" alt="" title=""/>
									</div>
									<div class="fl blue">
										<a href="#" style="text-decoration: underline;" class="blue" id="${card.cardIdentifier}">Delete</a>
									</div>
								</span>
							</li>
							<div class="clear"></div>
						</c:forEach>
					</ul>
				</div>
				<div class="clear"></div>
			</div>
			<div class="gray">
				<div class="fl width-650">
					<div class="cc-sec">
						<label class="fl ">CVV</label>
						<input autocomplete="off" type="password" name="cvvNumber" class="fl padd3 width127" size="15" maxlength="3"/><br/>
						<c:if test="${'5' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
							<div class="clear"></div>
							<div class="error">${requestScope.validationErrors['INVALID_CVV']}</div>
						</c:if>
						<div class="clear"></div>
					</div>
					<div class="clear"></div>
				</div>
				<div class="fr width-180 cvv-cc" style="margin-top:0px;">
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
	<script type="text/javascript">
		$(document).ready(function(){
		//$('ul.save-card span')[0].click();
			var obj = $('ul.save-card span')[0];
			obj.className = 'chek';
			var saveCard = obj.id;
			$("#savedCardId")[0].value=saveCard;
			$(".cc-sec").show();
			$(".cvv-cc").show();
		});
	</script>
</div>