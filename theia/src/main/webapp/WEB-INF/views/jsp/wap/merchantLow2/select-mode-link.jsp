<c:choose>
	<c:when test="${5 eq paymentType}">
		<c:set var="selectedMode" value="Saved Card"/>
	</c:when>
	<c:when test="${12 eq paymentType}">
		<c:set var="selectedMode" value="COD"/>
	</c:when>
	<c:when test="${2 eq paymentType}">
		<c:set var="selectedMode" value="Debit Card"/>
	</c:when>
	<c:when test="${10 eq paymentType}">
		<c:set var="selectedMode" value="Cash Card"/>
	</c:when>
	<c:when test="${6 eq paymentType}">
		<c:set var="selectedMode" value="IMPS"/>
	</c:when>
	<c:when test="${8 eq paymentType}">
		<c:set var="selectedMode" value="ATM Card"/>
	</c:when>
	<c:when test="${3 eq paymentType}">
		<c:set var="selectedMode" value="Net Banking"/>
	</c:when>
	<c:when test="${1 eq paymentType}">
		<c:set var="selectedMode" value="Credit Card"/>
	</c:when>
	<c:when test="${11 eq paymentType}">
		<c:set var="selectedMode" value="Rewards"/>
	</c:when>
	<c:when test="${13 eq paymentType}">
		<c:set var="selectedMode" value="EMI"/>
	</c:when>
</c:choose>