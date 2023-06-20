
<%-- in case of only wallet, append useWallet querystring to other mode links --%>
<c:set var="useWalletQuerystring" value="" />
<c:if test="${paymentType eq 7}">
	<c:set var="useWalletQuerystring" value="&use_wallet=0"></c:set>
</c:if>

 
<c:if test="${(5 ne paymentType || !empty ppi) && saveCardEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=SC${useWalletQuerystring}" id="SC-link" class="pay-mode-link">
<span class="bottom-link">
	Saved Card
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a> 
</c:if>

<c:if test="${(12 ne paymentType || !empty wallet) && codEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=COD${useWalletQuerystring}">
<span class="bottom-link">
	Cash On Delivery (COD)
	<img src="/images/wap/paytmAppLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(2 ne paymentType || !empty ppi) && dcEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=DC${useWalletQuerystring}" id="DC-link" class="pay-mode-link">
<span class="bottom-link">
	Debit Card
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a> 
</c:if>

<c:if test="${(1 ne paymentType || !empty ppi) && ccEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=CC${useWalletQuerystring}" id="CC-link" class="pay-mode-link">
<span class="bottom-link">
	Credit Card
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a> 
</c:if>

<c:if test="${(3 ne paymentType || !empty ppi) && netBankingEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=NB${useWalletQuerystring}" id="NB-link" class="pay-mode-link">
<span class="bottom-link">
	Net Banking 
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(8 ne paymentType || !empty ppi) && atmEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=ATM${useWalletQuerystring}" id="ATM-link" class="pay-mode-link">
<span class="bottom-link">
	ATM Card
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(6 ne paymentType ||  !empty ppi) && impsEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=IMPS${useWalletQuerystring}" id="IMPS-link" class="pay-mode-link">
<span class="bottom-link">
	IMPS
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(10 ne paymentType || !empty ppi) && cashcardEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=CASHCARD${useWalletQuerystring}" id="CASHCARD-link" class="pay-mode-link">
<span class="bottom-link">
	Cash Card
	<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(11 ne paymentType || !empty wallet) && rewardsEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=REWARDS${useWalletQuerystring}">
<span class="bottom-link-last">
	Rewards
	<img src="/images/wap/paytmAppLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>

<c:if test="${(13 ne paymentType || !empty wallet) && emiEnabled}">
<a href="/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=EMI${useWalletQuerystring}">
<span class="bottom-link">
	EMI
	<img src="/images/wap/paytmAppLow/arrow.gif" align="right" alt="" title="" />
</span>
</a>
</c:if>