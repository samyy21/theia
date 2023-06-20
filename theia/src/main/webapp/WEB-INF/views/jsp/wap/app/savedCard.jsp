<div data-role="content" data-theme="i" id="savedcard-container" <c:if test="${'5' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
				<li>${requestScope.validationErrors['INVALID_CVV']}</li>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for
				Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" method="post" action="submitTransaction" data-ajax="false">
			<div  data-role="fieldcontain" class="mt5 mb10" id="card">
				<label class="ui-input-text">Select your card</label>
				<c:set var="savedCardList" value="${cardInfo.merchantViewSavedCardsList}"></c:set>
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
						<%-- <c:when test="${'maestro' eq card.cardType}">
							<c:set var="cardImagePrefix" value="maestro" />
						</c:when> --%>
					</c:choose>
					
					<c:set var="checkedid" value="" />
					<c:if test="${card.cardId eq requestScope.SCARD_ID }">
					   <c:set var="checkedid" value="checked" />
					</c:if>
					<c:if test="${empty requestScope.SCARD_ID && 0 == count.index}">
					   <c:set var="checkedid" value="checked" />
					</c:if>
					
					<div class="pleft10" id="${card.cardId}-item">
						<input  type="radio" <c:if test="${'checked' eq  checkedid }">checked</c:if> class="styled" name="savedCardId"  value="${card.cardId}"/>
					</div>
					
					<div class="label" id="deleteDiv">
						<label style="margin-left: 10px">Use ${card.cardNumber}</label>
						<c:if test="${!empty cardImagePrefix}">&nbsp;
							<img src="images/wap/${cardImagePrefix}-card.jpg" title="" alt=""/>
						</c:if>
						<a href="#" onclick='$("#overlay").popup("open")' id="${card.cardId}" style="float: right; margin-right: 5px">
							<img src="images/wap/delete.png" alt="Delete" title="Delete" />
						</a> 
					</div>
				</c:forEach>
			</div>
			
			<input type="hidden" name="txnMde" value="SC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
<!-- 			<input type="hidden" name="txnMode" value="CC" /> -->
			<input type="hidden" name="deletCardId" id="deletCardId"/>
			
			<div data-role="fieldcontain" class="padd-t" style="margin-bottom: 50px">
				<label for="creditCard" class="ui-input-text">CVV/Secure Code</label>
      			<input autocomplete="off" type="tel" name="cvvNumber" maxlength="4">
			</div>
			
			<div data-role="footer" data-id="foo1" data-position="fixed">
		  		<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
		</form>
	</section>
</div>
 <div id="overlay" data-role="popup" data-overlay-theme="a" data-position-to="window">
    <div class="blue-text">Please note </div>
   <p style="padding:10px 15px;">Are you sure you want to delete this card information?</p>
    <a href="#" id="deletCardButton" data-selected="mobile" data-transition="slide" data-role="button" class="ui-btn ui-btn-text">Delete</a>
    <a href="#" id="deleteCardClose" data-selected="mobile" data-transition="slide" data-role="button" class="ui-btn ui-btn-text">Cancel</a>
</div>
<script>
	$('#deletCardButton').live('click', function() {
		var cardId = $("#deletCardId").val();
		$("#overlay").popup("close");
		window.location.href = "DeleteCardDetails?savedCardId=" + cardId + "&ajax=no";
	}
	);
</script>