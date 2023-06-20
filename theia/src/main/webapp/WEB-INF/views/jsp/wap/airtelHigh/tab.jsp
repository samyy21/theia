<select name="select1" id="paymentModes" data-icon="dwn-arw">
	<c:if test="${dcEnabled}">
		<option value="debitcard" <c:if test="${'DC' == paymentMode }">selected=selected</c:if>>Debit card</option>
	</c:if>
	<c:if test="${ccEnabled}">
		<option value="creditcard" <c:if test="${'CC' == paymentMode }">selected=selected</c:if>>Credit card</option>
	</c:if>
	<c:if test="${netBankingEnabled}">
		<option value="netbanking" <c:if test="${'3' == paymentType }">selected=selected</c:if>>Net banking</option>
	</c:if>
	<c:if test="${atmEnabled}" >
		<option value="atm" <c:if test="${'8' == paymentType }">selected=selected</c:if>>ATM card</option>
	</c:if>
	<c:if test="${impsEnabled}">
		<option value="imps" <c:if test="${'6' == paymentType }">selected=selected</c:if>>IMPS</option>
	</c:if>
</select>