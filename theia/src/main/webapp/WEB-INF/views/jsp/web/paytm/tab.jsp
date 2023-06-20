<ul class="nav nav-tabs" id="verTabs" data-showtabs="${sessionScope.promoPaymentModes}">
	<c:if test="${'1' eq saveCardEnabled}">
		<li <c:if test="${5 eq paymentType}">class = "active"</c:if>><a href="#scContent" data-toggle="tab" tabindex="-1" id="savedcard"><div class="img img-arrow-r"></div>Saved Cards</a></li>
	</c:if>
	<c:if test="${1 eq dcEnabled}">
		<li <c:if test="${2 eq paymentType || empty paymentType}">class = "active"</c:if>><a href="#dcContent" data-toggle="tab" tabindex="-1" id="debitCard"><div class="img img-arrow-r"></div>Debit Card</a></li>
	</c:if>
	<c:if test="${1 eq ccEnabled}">
		<li <c:if test="${1 eq paymentType}">class = "active"</c:if>><a href="#ccContent" data-toggle="tab" tabindex="-1" id="creditCard"><div class="img img-arrow-r"></div>Credit Card</a></li>
	</c:if>
	<c:if test="${1 eq netBankingEnabled}">
		<li <c:if test="${3 eq paymentType}">class = "active"</c:if>><a href="#nbContent" data-toggle="tab" tabindex="-1" id="netBanking"><div class="img img-arrow-r"></div>Net Banking</a></li>
	</c:if>
	<c:if test="${1 eq atmEnabled}">
		<li <c:if test="${8 eq paymentType}">class = "active"</c:if>><a href="#atmContent" data-toggle="tab" tabindex="-1" id="atmBanking" class="nonPromoMode"><div class="img img-arrow-r"></div>ATM Card</a></li>
	</c:if>
	<c:if test="${1 eq impsEnabled}">
		<li <c:if test="${6 eq paymentType}">class = "active"</c:if>><a href="#impsContent" data-toggle="tab" tabindex="-1" id="impsBanking" class="nonPromoMode"><div class="img img-arrow-r"></div>IMPS</a></li>
	</c:if>
	
	<c:if test="${1 eq cashcardEnabled}">
		<li <c:if test="${10 eq paymentType}">class = "active"</c:if>><a href="#cashCardContent" data-toggle="tab" tabindex="-1" class="nonPromoMode"><div class="img img-arrow-r"></div>Cash Card</a></li>
	</c:if>
	
	<c:if test="${1 eq codEnabled}">
		<li <c:if test="${12 eq paymentType}">class = "active"</c:if>><a href="#codContent" data-toggle="tab" tabindex="-1"><div class="img img-arrow-r"></div>COD</a></li>
	</c:if>
	<c:if test="${1 eq rewardsEnabled}">
		<li <c:if test="${11 eq paymentType}">class = "active"</c:if>><a href="#rewardsContent" data-toggle="tab" tabindex="-1"><div class="img img-arrow-r"></div>Rewards</a></li>
	</c:if>
	
</ul>