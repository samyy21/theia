<div id="tab-wrapper">
	<ul class="mytabs">
		<c:if test="${true eq cardInfo.saveCardEnabled}">
			<li id="savedcard">
				<span <c:if test="${'5' eq paymentType }">class="act"</c:if>>Saved Cards</span>
			</li>
		</c:if>
		<c:if test="${true eq txnConfig.ccEnabled}">
			<li id="creditcard">
				<span <c:if test="${'1' == paymentType }">class="act"</c:if>>
					Credit Card
				</span>
			</li>
		</c:if>
		<c:if test="${true eq txnConfig.dcEnabled || true eq txnConfig.atmEnabled}">
			<li id="debitcard">
				<span <c:if test="${'2' == paymentType  || '8' == paymentType}">class="act"</c:if>>
					Debit Card
				</span>
			</li>
		</c:if>
		<c:if test="${true eq txnConfig.netBankingEnabled}">
			<li id="netbanking">
				<span <c:if test="${'3' == paymentType}">class="act"</c:if>>
					Net Banking
				</span>
			</li>
		</c:if>
		<c:if test="${true eq txnConfig.impsEnabled}">
			<li id="imps">
				<span <c:if test="${'6' == paymentType}">class="act"</c:if>>
					IMPS
				</span>
			</li>
		</c:if>
		<c:if test="${true eq walletInfo.walletEnabled}">
			<li id="ppi">
				<span <c:if test="${'7' == paymentType}">class="act"</c:if>>
					Wallet
				</span>
			</li>
		</c:if>
		<c:if test="${1 eq otherPaymentMethodEnabled}">
			<li id="other">
				<span <c:if test="${'4' == paymentType}">class="act"</c:if>>
					Cash Card
				</span>
			</li>
		</c:if>
		<c:if test="${1 eq txnConfig.cashcardEnabled}">
			<li id="itz">
				<span <c:if test="${'10' == paymentType}">class="act"</c:if>>
					ITZ CASH
				</span>
			</li>
		</c:if>
		
	</ul>
</div>