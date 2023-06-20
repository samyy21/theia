<div data-role="collapsible" id="saveCard" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>Saved Cards</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<form autocomplete="off" name="savecard-form" method="post" action="/payment/request/submit" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="CC" />
			<input type="hidden" name="txnMde" value="SC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="savedCardId" id="savedCardId" value=""/>
			<input type="hidden" name="cvvNumber" id="scCvvBox" value="">
			<input type="hidden" name="walletAmount" id="walletAmountSC" value="0" />
			
			<div id="cards">
			<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
			<c:if test="${txnConfig.addMoneyFlag}">
				<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
			</c:if>
				<c:forEach var="card"  varStatus="status" items="${savedCardList}">
					<c:choose>
						<c:when test="${'MASTER' eq card.cardType}">
							<c:set var="cardImagePrefix" value="master" />
						</c:when>
						<c:when test="${'VISA' eq card.cardType}">
							<c:set var="cardImagePrefix" value="visa" />
						</c:when>
						<c:when test="${'DINERS' eq card.cardType}">
							<c:set var="cardImagePrefix" value="diners" />
						</c:when>
					</c:choose>
					
					<c:set var="cardState" value="promo-valid-card"></c:set>
					<c:if test="${!empty sessionScope.promoCode && txnInfo.promoValid}">
						<c:set var="cardState" value="hide"></c:set>
					</c:if>
					
					<div class="scContainer ${cardState}" id="scContainer-${card.cardId}">
						<div class="radio fl saveRadio" id="${card.cardId}">
		                	<input type="radio" id="radio-${card.cardId}" value="pc2" name="1">
		                	<span class="radio-label" data-firstsixdigits="${card.firstSixDigit}">${card.cardNumber}
		                		<div class="${cardImagePrefix}-logo"></div>
		                	</span> 
		               	</div>
		                
		                <div class="txt12 deleteCard" cardId="${card.cardId}" id="delete-${card.cardId}" style="float:right; margin-top:-5px">
		                	<a href="#">
		                		<div class="delete-icon"></div>
		                	</a>
		                </div>
		               	
		               	<div class="clear"></div>
		                        
		                <div class="mb10 mt15 saveCardCvvDiv" id="cvvDiv-${card.cardId}" style="display: none;">
			                <div class="fl ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
			                	<input type="password" id="cvv-${card.cardId}" class="scCvvInput ui-input-text ui-body-c" placeholder="CVV" value="" maxlength="3" size="4" autofocus>
			                </div>
			                <div class="fl mt4">
			                	<div class="cvv-icon"></div>
			                </div>
			                <div class="fl cvv">The last 3 digit printed on the signature panel on the back of your credit card.</div>
			                <div class="clear"></div>
			                <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
								<div class="scCvvError error clear" style="width: 150px; margin-left: 5px">${requestScope.validationErrors['INVALID_CVV']}</div>
								<script type="text/javascript">
									$(document).ready(function(){
										$("#cvv-" + '${card.cardId}').parent().addClass("cvvError");
									});
								</script>
							</c:if>
		                </div>
		                
		                <div class="divider mb10"></div>
	                </div>
				</c:forEach>
			
			</div>
			<div class="load-btn">
			  	 
			  	 <input type=submit class="submitButton" value="Proceed Securely" id="scSubmit" data-icon="ldr" data-iconpos="right">
		  	</div>
		  </form>
		  
		  <script type="text/javascript">
			$(document).ready(function() {
				$("#scCvvBox")[0].value = "";
				var saveCard = "${SCARD_ID}";
				if(saveCard == null || saveCard == "NULL" || saveCard== "null" || saveCard == "") {
					saveCard = $(".saveRadio").first().addClass("checked").attr('id');
				}
				
				$("#" + saveCard).find('input').attr("checked", "checked");
				
				$("#savedCardId")[0].value = saveCard;
				$("#cvvDiv-" + saveCard).show();
				$("#scSubmit").attr("disabled", false);
			});
		</script>
	</div>
	
</div>