<div  data-savedcards="" class='tab-pane fade <c:if test="${5 eq paymentType}">in active</c:if>' id="scContent">
	<h3> Select from card below</h3>
	<form autocomplete="off" name="savecard-form" method="post" class="form-horizontal" action="submitTransaction" id="card">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="CC" />
		<input type="hidden" name="txn_Mode" value="CC" />
		<input type="hidden" name="txnMde" value="SC" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="savedCardId" id="savedCardId" value=""/>
		<input type="hidden" name="cvvNumber" id="scCvvBox" value="">
		<input type="hidden" name="walletAmount" id="walletAmountSC" value="0" />
		
		<c:forEach var="card"  varStatus="status" items="${cardInfo.savedCardsList}">
			<c:choose>
				<c:when test="${'MASTER' eq card.cardType}">
					<c:set var="cardImagePrefix" value="master-sc" />
				</c:when>
				<c:when test="${'VISA' eq card.cardType}">
					<c:set var="cardImagePrefix" value="visa-sc" />
				</c:when>
				<c:when test="${'DINERS' eq card.cardType}">
					<c:set var="cardImagePrefix" value="diners-sc" />
				</c:when>
			</c:choose>
			
			<c:set var="cardState" value=""></c:set>
			<c:if test="${!empty sessionScope.promoCode && txnInfo.promoCodeValid eq false}">
				<c:set var="cardState" value="hide"></c:set>
			</c:if>
			
			<div class="control-group ${cardState}">
           		<div class="controls" id="one">
             		<div class="input-prepend fl">
               			<span class="add-on">
                 			<label class="radio cvvRadio" id="${card.cardIdentifier}">
                   				<input type="radio" name="cno">
                 			</label>
               			</span>
               			<input id="prependedradio-${card.cardIdentifier}" data-firstsixdigits="${card.firstSixDigit}" name="prependedradio" class="savedCardLabel input-xlarge ${cardImagePrefix}" disabled="disabled" type="text" value="${card.cardNumber}" style="position: relative;">
               			<span class="deleteCard" cardId="${card.cardIdentifier}" id="delete-${card.cardIdentifier}">
							<a href="#" class="blue">x</a>
						</span>
             		</div>
             		
             		<div class="saveCardCvvDiv" id="cvvDiv-${card.cardIdentifier}" style="display: none;">
		               	<div class="fl ml15 mt24">
		                   <label>CVV/Security Code</label>
		                   <p> <input type="password" id="cvv-${card.cardIdentifier}" class="width40 scCvvInput" maxlength="3" value="" autofocus></p>
		                   
		                   <c:if test="${paymentType == '5' && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
								<div class="scCvvError error clear">${requestScope.validationErrors['INVALID_CVV']}</div>
								<script type="text/javascript">
									$("#cvv-" + "${card.cardIdentifier}").addClass("error1");
								</script>
							</c:if>
		               	</div>
	             
	               		<div class="fl" id="cvv-sec">
	                  		<div class="fl"><div class="img img-cvv" alt="CVV" title="CVV"></div></div>
	                      	<div class="fl secure1">The last 3 digit printed on the <br />signature panel on the back<br /> of your credit card.</div>
	                      	<div class="clear"></div>
	              		</div> 
              		</div>
           		</div>
            </div>
		</c:forEach>
				
		<div class="clear"></div>
			
		<p class="clear">
           	<input name="" type="button" class="gry-btn" value="Proceed Securely" id="scSubmit" disabled="disabled">
           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
        </p>
        <div class="secure">
        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
        </div>
	</form>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#scCvvBox")[0].value = "";
			var saveCard = "${SCARD_ID}";
			if(saveCard == null || saveCard == "NULL" || saveCard== "null" || saveCard == "") {
				saveCard = $(".cvvRadio")[0].id;
			}
			
			$("#" + saveCard).children().attr("checked", "checked");
			$("#savedCardId")[0].value = saveCard;
			$("#cvvDiv-" + saveCard).show();
		
			$("#" + saveCard).parent().addClass("savedCardSelect");
			$("#prependedradio-" + saveCard).addClass("savedCardSelect1");
			$("#prependedradio-" + saveCard).closest(".control-group").find(".scCvvInput").addClass("savedCardSelect1").focus();
			
		});
	</script>
</div>