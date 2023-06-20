<div id="tab-wrapper">
	<div class="blue-bar">Select payment mode</div>
	<div data-role="fieldcontain" class="ui-field-contain ui-body ui-br mb20">
		<select name="select-choice-0" id="pmodeSelect">
			<c:if test="${saveCardEnabled}">
	      		<option value="savedcard" <c:if test="${'5' eq paymentType }">selected=selected</c:if>>Saved Card</option>
	      	</c:if>
	      	
	      	<c:if test="${ccEnabled}">
	   			<option value="creditcard" <c:if test="${'1' eq paymentType }">selected=selected</c:if>>Credit Card</option>
	      	</c:if>
	      	
	    	<c:if test="${dcEnabled ||atmEnabled}">
	      		<option value="debitcard" <c:if test="${'2' == paymentType  || '8' == paymentType}">selected=selected</c:if>>Debit Card</option>
	      	</c:if>
	     
	     	<c:if test="${netBankingEnabled}">
	      		<option value="netbanking" <c:if test="${'3' eq paymentType }">selected=selected</c:if>>Net Banking</option>
	      	</c:if>
	       
	      	<c:if test="${impsEnabled}">
	      		<option value="imps" <c:if test="${'6' eq paymentType }">selected=selected</c:if>>IMPS</option>
	      	</c:if>
	      
	      	<c:if test="${walletEnabled}">
	      		<option value="ppi" <c:if test="${'7' eq paymentType }">selected=selected</c:if>>Wallet</option>
	      	</c:if>
	      
	        <c:if test="${telcoEnabled}">
	      		<option value="telco" <c:if test="${'9' eq paymentType }">selected=selected</c:if>>Operator Billing</option>
	      	</c:if>
	      	
	      	<c:if test="${cashcardEnabled}">
	      		<option value="itz" <c:if test="${'10' eq paymentType }">selected=selected</c:if>>ITZ CASH</option>
	      	</c:if>
		</select>
	</div>
</div>