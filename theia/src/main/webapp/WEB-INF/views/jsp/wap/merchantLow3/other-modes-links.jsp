<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- in case of only wallet, append useWallet querystring to other mode links --%>
<c:set var="useWalletQuerystring" value=""></c:set>
<c:if test="${paymentType eq 7}">
	<c:set var="useWalletQuerystring" value="&use_wallet=0"></c:set>
</c:if>
<%-- commented one && fn:length(cardInfo.savedCardsList) > 0 --%>
<c:if test="${(5 ne paymentType || !empty ppi) && saveCardEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=SC${useWalletQuerystring}&${queryStringForSession}" id="SC-link" class="pay-mode-link">
<span class="bottom-link">
	Saved Cards
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(15 ne paymentType || !empty ppi) &&  upiEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=UPI${useWalletQuerystring}&${queryStringForSession}" id="UPI-link" class="pay-mode-link">
<span class="bottom-link">
        BHIM UPI
        <img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(2 ne paymentType || !empty ppi) && dcEnabled}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=DC${useWalletQuerystring}&${queryStringForSession}" id="DC-link" class="pay-mode-link">
<span class="bottom-link">
	Debit Card
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(1 ne paymentType || !empty ppi) && ccEnabled}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=CC${useWalletQuerystring}&${queryStringForSession}" id="CC-link" class="pay-mode-link">
<span class="bottom-link">
	Credit Card
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(3 ne paymentType || !empty ppi) && netBankingEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=NB${useWalletQuerystring}&${queryStringForSession}" id="NB-link" class="pay-mode-link">
<span class="bottom-link">
	Net Banking 
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(13 ne paymentType || !empty wallet) && emiEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=EMI${useWalletQuerystring}&${queryStringForSession}" id="EMI-link" class="pay-mode-link">
<span class="bottom-link">
	EMI
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(12 ne paymentType || !empty wallet) && codEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=COD${useWalletQuerystring}&${queryStringForSession}" id="COD-link class="pay-mode-link" <c:if test="${usePaytmCash && txnConfig.codHybridAllowed ne true}"> style="display:none;"</c:if>>
<span class="bottom-link">
	Cash On Delivery (COD)
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(8 ne paymentType || !empty ppi) && atmEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=ATM${useWalletQuerystring}&${queryStringForSession}" id="ATM-link" class="pay-mode-link">
<span class="bottom-link">
	ATM 
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(6 ne paymentType ||  !empty ppi) && impsEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=IMPS${useWalletQuerystring}&${queryStringForSession}" id="IMPS-link" class="pay-mode-link">
<span class="bottom-link">
	IMPS
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(10 ne paymentType || !empty ppi) && cashcardEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=CASHCARD${useWalletQuerystring}&${queryStringForSession}" id="CASHCARD-link" class="pay-mode-link">
<span class="bottom-link">
	Cash Card
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>

<c:if test="${(11 ne paymentType || !empty wallet) && rewardsEnabled && themeInfo.subTheme ne 'ccdc'}">
	<div class="pay-modes-link">
		<a href="/theia/jsp/wap/merchantLow3/${formName}.jsp?txn_Mode=REWARDS${useWalletQuerystring}&${queryStringForSession}" id="REWARDS-link" class="pay-mode-link">
<span class="bottom-link-last">
	Rewards
	<img src="/theia/resources/images/wap/merchantLow3/close-copy.png" class="mt7" align="right" alt="" title="" />
</span>
		</a>
	</div>
</c:if>
