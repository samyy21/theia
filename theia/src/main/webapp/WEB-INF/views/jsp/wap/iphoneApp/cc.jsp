<c:if test="${'1' eq paymentType}">
	<div class="form-container">
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="/payment/request/submit">
			<p class="mt15">
				<label>Card Number </label><br />  <input autocomplete="off"  type="tel"
					name="cardNumber" maxlength="19" /> <br /> 
			</p>
			<c:if
				test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
				<p class="error">${requestScope.validationErrors['INVALID_CARD']}</p>
			</c:if>
				
			<p class="mt15">
				<label>Expiry date (MMYY)</label> <br />
				<input name="ccExpiryMonthYear" type="tel" maxlength="4"> <br />
				<span class="sm-txt">
					<em> (Optional for Maestro Cards) </em> 
				</span>
			</p>
			<c:if
				test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<p class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</p>
			</c:if>
			
			<p class="mt15">
				<label>CVV Number</label> <br />
				<input autocomplete="off"  type="tel" name="cvvNumber" maxlength="4" /> <br />
				<span class="sm-txt"><em> (Optional for Maestro Cards) </em> </span>
			</p>
			<c:if
				test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
				<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
			</c:if>
			
			<c:if test="${saveCardOption}">
	        	<div class="pad mt5" id="card">        
		        	<div ><input  name="storeCardFlag"	type="checkbox" class="styled" value="Y"/></div>
		        	<div class="label"><label for="card" style="font-size: 15px">Save this card for future transactions</label></div>
		  		</div>
			</c:if>
	
			<input type="hidden" name="txnMode" value="CC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			
			<div class="margin">
				 <input	name="Submit" type="submit" value="Pay Now" class="button" />
			</div> 
			<div class="margin mb15">
				<input id="cancelButton" name="cancleBtn" onClick="cancelTxn();" type="button" class="cancel" value="Cancel" />
			</div>
		</form>
	</div>
</c:if>