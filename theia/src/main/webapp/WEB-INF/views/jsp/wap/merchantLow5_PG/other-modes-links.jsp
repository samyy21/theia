<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- in case of only wallet, append useWallet querystring to other mode links --%>
<c:set var="useWalletQuerystring" value=""></c:set>
<c:if test="${paymentType eq 7}">
	<c:set var="useWalletQuerystring" value="&use_wallet=0"></c:set>
</c:if>
<style>

	.bottom-link{
		background: #efefef !important;
		padding: 7px 11px !important;
		text-align: center;
		font-size: 14px;
	}
	.bottom-link img{ display: none;}

</style>
<%-- commented one && fn:length(cardInfo.savedCardsList) > 0 --%>
<c:if test="${(5 ne paymentType || !empty ppi) && saveCardEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=SC${useWalletQuerystring}&${queryStringForSession}" id="SC-link" class="pay-mode-link">
<span class="bottom-link">
	Saved Details
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(15 ne paymentType || !empty ppi) &&  upiEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=UPI${useWalletQuerystring}&${queryStringForSession}" id="UPI-link" class="pay-mode-link">
<span class="bottom-link">
        BHIM UPI
        <img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(2 ne paymentType || !empty ppi) && (dcEnabled || ccEnabled)}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=DC${useWalletQuerystring}&${queryStringForSession}" id="DC-link" class="pay-mode-link">
<span class="bottom-link">
	 Credit / Debit / ATM Card
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<%--<c:if test="${(1 ne paymentType || !empty ppi) && ccEnabled}">--%>
	<%--<a href="/theia/jsp/wap/merchantLow5/${formName}.jsp?txn_Mode=CC${useWalletQuerystring}&${queryStringForSession}" id="CC-link" class="pay-mode-link">--%>
<%--<span class="bottom-link">--%>
	<%--Credit Card--%>
	<%--<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />--%>
<%--</span>--%>
	<%--</a>--%>
<%--</c:if>--%>

<c:if test="${(3 ne paymentType || !empty ppi) && netBankingEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=NB${useWalletQuerystring}&${queryStringForSession}" id="NB-link" class="pay-mode-link">
<span class="bottom-link">
	Net Banking 
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(13 ne paymentType || !empty wallet) && emiEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=EMI${useWalletQuerystring}&${queryStringForSession}" id="EMI-link" class="pay-mode-link">
<span class="bottom-link">
	EMI
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(12 ne paymentType || !empty wallet) && codEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=COD${useWalletQuerystring}&${queryStringForSession}" id="COD-link class="pay-mode-link" <c:if test="${usePaytmCash && txnConfig.codHybridAllowed ne true}"> style="display:none;"</c:if>>
<span class="bottom-link">
	Cash On Delivery (COD)
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(8 ne paymentType || !empty ppi) && atmEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=ATM${useWalletQuerystring}&${queryStringForSession}" id="ATM-link" class="pay-mode-link">
<span class="bottom-link">
	ATM Card
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(6 ne paymentType ||  !empty ppi) && impsEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=IMPS${useWalletQuerystring}&${queryStringForSession}" id="IMPS-link" class="pay-mode-link">
<span class="bottom-link">
	IMPS
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(10 ne paymentType || !empty ppi) && cashcardEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=CASHCARD${useWalletQuerystring}&${queryStringForSession}" id="CASHCARD-link" class="pay-mode-link">
<span class="bottom-link">
	Cash Card
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${(11 ne paymentType || !empty wallet) && rewardsEnabled && themeInfo.subTheme ne 'ccdc'}">
	<a href="/theia/jsp/wap/merchantLow5_PG/${formName}.jsp?txn_Mode=REWARDS${useWalletQuerystring}&${queryStringForSession}" id="REWARDS-link" class="pay-mode-link">
<span class="bottom-link-last">
	Rewards
	<img src="${ptm:stResPath()}images/wap/merchantLow5/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>
