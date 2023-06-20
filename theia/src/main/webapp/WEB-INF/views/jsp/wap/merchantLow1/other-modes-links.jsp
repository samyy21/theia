<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%-- in case of only wallet, append useWallet querystring to other mode links --%>
<c:set var="useWalletQuerystring" value=""></c:set>
<c:if test="${paymentType eq 7}">
	<c:set var="useWalletQuerystring" value="&use_wallet=0"></c:set>
</c:if>

<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	<%--<c:if test="${walletInfo.walletEnabled}"> --%>
	<a href="#" id="paytmWallet" class="pay-mode-link hide" onclick="showAuthView('login'); return false;">
		<span class="bottom-link">
		
			Paytm
			<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
		</span>
	</a>
	<%-- </c:if>  --%>
</c:if>

<c:if test="${saveCardEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=SC${useWalletQuerystring}&${queryStringForSession}" id="SC-link" class="pay-mode-link">
<span class="bottom-link ${5 eq paymentType ? 'b' : ''}">
	Saved Card
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${dcEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=DC${useWalletQuerystring}&${queryStringForSession}" id="DC-link" class="pay-mode-link">
<span class="bottom-link ${2 eq paymentType ? 'b' : ''}">
	Debit Card
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${ccEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=CC${useWalletQuerystring}&${queryStringForSession}" id="CC-link" class="pay-mode-link">
<span class="bottom-link ${1 eq paymentType ? 'b' : ''}">
	Credit Card
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${netBankingEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=NB${useWalletQuerystring}&${queryStringForSession}" id="NB-link" class="pay-mode-link">
<span class="bottom-link ${3 eq paymentType ? 'b' : ''}">
	Net Banking 
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>


<c:if test="${emiEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=EMI${useWalletQuerystring}&${queryStringForSession}" id="EMI-link" class="pay-mode-link">
<span class="bottom-link-last ${13 eq paymentType ? 'b' : ''}">
	EMI
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>
<c:if test="${codEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=COD${useWalletQuerystring}&${queryStringForSession}" id="COD-link" class="pay-mode-link" <c:if test="${usePaytmCash && !txnConfig.codHybridAllowed}"> style="display:none;"</c:if>>
<span class="bottom-link ${12 eq paymentType ? 'b' : ''}">
	Cash On Delivery (COD)
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>


<c:if test="${atmEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=ATM${useWalletQuerystring}&${queryStringForSession}" id="ATM-link" class="pay-mode-link">
<span class="bottom-link ${8 eq paymentType ? 'b' : ''}">
	ATM Card
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${impsEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=IMPS${useWalletQuerystring}&${queryStringForSession}" id="IMPS-link" class="pay-mode-link">
<span class="bottom-link ${6 eq paymentType ? 'b' : ''}">
	IMPS
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>

<c:if test="${cashcardEnabled}">

	<a href="/theia/jsp/wap/merchantLow1/${formName}.jsp?txn_Mode=CASHCARD${useWalletQuerystring}&${queryStringForSession}" id="CASHCARD-link" class="pay-mode-link">
<span class="bottom-link ${10 eq paymentType ? 'b' : ''}">
	Cash Card
	<img src="${ptm:stResPath()}images/wap/merchantLow2/arrow.gif" align="right" alt="" title="" />
</span>
	</a>
</c:if>