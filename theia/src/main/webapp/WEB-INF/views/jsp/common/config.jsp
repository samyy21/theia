<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.Calendar" %>
<% pageContext.setAttribute("currentYear", Calendar.getInstance().get(Calendar.YEAR)); %>
<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate var="month" value="${now}" pattern="mm"/>
<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
<c:set var="merchantImage" value="${merchInfo.merchantImage}" />
<c:set var="merchantName" value="${merchInfo.merchantName}"/>

<c:set var="queryStringForSession" value="MID=${txnInfo.mid}&ORDER_ID=${txnInfo.orderId}&route=${jvmRoute}"/>


<%-- <c:if test="${not empty txnInfo.txnMode}"> --%>
<%-- <c:set var="queryStringForSession" value="txn_Mode=${txnInfo.txnMode}&MID=${txnInfo.mid}&ORDER_ID=${txnInfo.orderId}&route=${jvmRoute}"/> --%>
<%-- </c:if> --%>
<%--
| last user submit mode - ${param.txn_Mode}
| merchant pref - ${param.PAYMENT_TYPE_ID}
| user profiling - ${sessionScope.PAYMENT_TYPE_ID}
--%>
<c:set var="paymentTypeId" value="" />
<%-- mode chosen by user and submitted (set after submit) --%>
<c:if test="${!empty param.txn_Mode}">
	<c:set var="paymentTypeId" value="${param.txn_Mode}" />
</c:if>
<%-- get paymentType sent by merchant --%>
<c:if test="${!empty param.PAYMENT_TYPE_ID}">
	<c:set var="paymentTypeId" value="${param.PAYMENT_TYPE_ID}" />
	<%-- set paymentType in session --%>
	<c:set var="PAYMENT_TYPE_ID" value="${paymentTypeId}" scope="session"/>
</c:if>

<%-- get paymentType from user preference (also no user pref for subscription) --%>
<c:if test="${empty paymentTypeId && !txnConfig.subscriptionTxn}">
	<c:set var="paymentTypeId" value="${txnInfo.paymentTypeId}" />
</c:if>
<%-- default mode --%>
<c:set var="multiplePaymentModes" value="${fn:split(param.PAYMENT_TYPE_ID, ',')}" />
<c:if test="${fn:length(multiplePaymentModes) > 1}">
	<c:choose>
	<c:when test="${multiplePaymentModes[0] eq 'PPI'}">
		<c:set var="paymentTypeId" value="${multiplePaymentModes[1]}" />
	</c:when>
	<c:otherwise>
		<c:set var="paymentTypeId" value="${multiplePaymentModes[0]}" />
	</c:otherwise>
	</c:choose>
</c:if>
<c:choose>
	<c:when test="${'CC' == paymentTypeId}">
		<c:set var="paymentType" value="1" />
		<c:set var="cardType" value="" />
		<c:choose>
			<c:when test="${'AMEX' == param.CARD_TYPE}">
				<c:set var="cardType" value="2" />
			</c:when>
			<c:otherwise>
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:when test="${'DC' == paymentTypeId}">
		<c:set var="paymentType" value="2" />
	</c:when>
	<c:when test="${'NB' == paymentTypeId}">
		<c:set var="paymentType" value="3" />
	</c:when>
	<c:when test="${'OT' == paymentTypeId}">
		<c:set var="paymentType" value="4" />
	</c:when>
	<c:when test="${'SC' == paymentTypeId}">
		<c:set var="paymentType" value="5" />
	</c:when>
	<c:when test="${'IMPS' == paymentTypeId}">
		<c:set var="paymentType" value="6" />
	</c:when>
	<c:when test="${'PPI' == paymentTypeId}">
		<c:set var="paymentType" value="7" />
	</c:when>
	<c:when test="${'ATM' == paymentTypeId}">
		<c:set var="paymentType" value="8" />
	</c:when>
	<c:when test="${'Telco' == paymentTypeId}">
		<c:set var="paymentType" value="9" />
	</c:when>
	<c:when test="${'CASHCARD' == paymentTypeId}">
		<c:set var="paymentType" value="10" />
	</c:when>
	<c:when test="${'COD' == paymentTypeId}">
		<c:set var="paymentType" value="12" />
	</c:when>
	<c:when test="${'REWARDS' == paymentTypeId}">
		<c:set var="paymentType" value="11" />
	</c:when>
	<c:when test="${'EMI' == paymentTypeId}">
		<c:set var="paymentType" value="13" />
	</c:when>
	<c:when test="${'UPI' == paymentTypeId}">
		<c:set var="paymentType" value="15" />
	</c:when>
	<c:when test="${'PAYTM_DIGITAL_CREDIT' == paymentTypeId}">
		<c:set var="paymentType" value="16" />
	</c:when>
	<c:when test="${'PAYTM_PAYMENT_BANK' == paymentTypeId}">
		<c:set var="paymentType" value="17" />
	</c:when>
	<c:when test="${'UPI_PUSH' == paymentTypeId}">
		<c:set var="paymentType" value="18" />
	</c:when>


</c:choose>

<c:set var="saveCardEnabled" value="false"/>


		<c:choose>
			<c:when test="${themeInfo.subTheme eq 'ccdc' && !empty cardInfo.merchantViewSavedCardsList && (empty param.txn_Mode || param.txn_Mode eq 'SC') && ('CC' eq paymentTypeId || 'DC' eq paymentTypeId)}">
				<c:set var="paymentType" value="2" />
			</c:when>

			<c:when test="${(!empty cardInfo.merchantViewSavedCardsList || !empty cardInfo.addAndPayViewCardsList) && (empty txnInfo.txnMode && (empty param.txn_Mode || param.txn_Mode eq 'SC')) && ('CC' eq paymentTypeId || 'DC' eq paymentTypeId)}">
				<c:set var="paymentType" value="5" />
			</c:when>

		</c:choose>

<c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage && paymentType eq 5 && empty param.txn_Mode}">
	<c:set var="paymentType" value="16" />
</c:if>


<c:set var="saveCardOption" value="false"/>
<c:if test="${cardInfo.cardStoreOption}">
	<c:set var="saveCardOption" value="true"></c:set>
</c:if>
<c:set var="ccEnabled" value="${entityInfo.ccEnabled}" />
<c:set var="amexEnabled" value="false" />
<c:set var="dcEnabled" value="${entityInfo.dcEnabled}" />
<c:set var="atmEnabled" value="${entityInfo.atmEnabled}" />
<c:set var="netBankingEnabled" value="${entityInfo.netBankingEnabled}" />
<c:set var="impsEnabled" value="${entityInfo.impsEnabled}" />
<c:set var="walletEnabled" value="true" />

<c:set var="onlyWalletEnabled" value="${walletInfo.walletOnly}" />
<c:set var="cashcardEnabled" value="false" />
<c:set var="codEnabled" value="${entityInfo.codEnabled}" />
<c:set var="emiEnabled" value="${entityInfo.emiEnabled}" />
<c:set var="upiEnabled" value="${entityInfo.upiEnabled }" />
<c:set var="upiPushEnabled" value="${entityInfo.upiPushEnabled}" />



<c:set var="otherPaymentMethodEnabled" value="false" />
<%--<c:forEach items="${txnConfig.configuredChannels}" var="channelInfo">
	<c:if test="${('3D' eq channelInfo.authMode or 'SUBS' eq channelInfo.authMode) && 'CC' eq channelInfo.paymentType}">
		<c:set var="ccEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'PA3D' eq channelInfo.authMode && 'CC' eq channelInfo.paymentType}">
		<c:set var="ccEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'3D' eq channelInfo.authMode && 'DC' eq channelInfo.paymentType}">
		<c:set var="dcEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'NB' eq channelInfo.paymentType}">
		<c:set var="netBankingEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'USRPWD' eq channelInfo.paymentType}">
		<c:set var="netBankingEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'OTP' eq channelInfo.authMode && 'IMPS' eq channelInfo.paymentType}">
		<c:set var="impsEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'PPI' eq channelInfo.paymentType}">
		<c:set var="walletEnabled" value="true" />
	</c:if>
	
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'ATM' eq channelInfo.paymentType}">
		<c:set var="atmEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'3D' eq channelInfo.authMode && 'Telco' eq channelInfo.paymentType}">
		<c:set var="telcoEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'CASHCARD' eq channelInfo.paymentType}">
		<c:set var="cashcardEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'3D' eq channelInfo.authMode && 'COD' eq channelInfo.paymentType && txnConfig.codEnabled eq 'COD'}">
		<c:set var="codEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
		</c:if>
	<c:if test="${'OTP' eq channelInfo.authMode && 'REWARDS' eq channelInfo.paymentType}">
		<c:set var="rewardsEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'3D' eq channelInfo.authMode && 'EMI' eq channelInfo.paymentType}">
		<c:set var="emiEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	<c:if test="${'USRPWD' eq channelInfo.authMode && 'OFFLINE' eq channelInfo.paymentType}">
		<c:set var="chequeDDNeftEnabled" value="true" />
		<c:set var="onlyWalletEnabled" value="false" />
	</c:if>
	
	
</c:forEach>
 --%>
<c:if test="${(!empty cardInfo.merchantViewSavedCardsList || !empty cardInfo.addAndPayViewCardsList) && (ccEnabled || dcEnabled)}">
	<c:set var="saveCardEnabled" value="true"/>
</c:if>

<c:if test="${'AMEX' eq sessionScope.amexCard.bankName}">
		<c:set var="amexEnabled" value="true" />
	</c:if>
<c:if test="${'true' eq ccEnabled}">
	<c:set var="amexEnabled" value="true" />
</c:if>
 <%--
<c:if test="${empty sessionScope.MerchBankList}">
	<c:set var="netBankingEnabled" value="true" />
</c:if>
--%>
<c:set var="paymentFailure" value="false" />
<c:if test="${!empty requestScope.status && 'TXN_FAILURE' eq requestScope.status}">
	<c:set var="paymentFailure" value="true" />
</c:if>
<c:set var="debug" value="true" />
<c:set var="mid" value="${param.MID}" />
<c:if test="${empty mid}">
	<c:set var="mid" value="default" />
</c:if>
<%-- open sc if available --%>
<c:if test="${empty  paymentType && (!empty cardInfo.merchantViewSavedCardsList || !empty cardInfo.addAndPayViewCardsList) && (ccEnabled  || dcEnabled)}">
	<c:set var="paymentType" value="5" />
</c:if>
<c:if test="${txnConfig.addMoneyFlag}">
 	<c:set var="dcEnabled" value="${entityInfo.addDcEnabled}"/>
    <c:set var="ccEnabled" value="${entityInfo.addCcEnabled}"/>
    <c:set var="saveCardEnabled" value="${cardInfo.addAndPayViewSaveCardEnabled}"/>
    <c:set var="netBankingEnabled" value="${entityInfo.addNetBankingEnabled}"/>
    <c:set var="atmEnabled" value="${entityInfo.addAtmEnabled}"/>
    <c:set var="impsEnabled" value="${entityInfo.addImpsEnabled}"/>
    <c:set var="codEnabled" value="${entityInfo.addCodEnabled}"/>
    <c:set var="emiEnabled" value="${entityInfo.addEmiEnabled}"/>
    <c:set var="chequeDDNeftEnabled" value="${txnConfig.chequeDDNeftEnabled}"/>
    <c:set var="upiEnabled" value="${entityInfo.addUpiEnabled}"></c:set>
<%--
	<c:set var="upiPushEnabled" value="${entityInfo.addUpiPushEnabled}" />
--%>
</c:if>

<%-- handle autofill (overrides sc) --%>
<c:if test="${empty  paymentType}">
	<c:choose>
		<c:when test="${(!empty cardInfo.merchantViewSavedCardsList || !empty cardInfo.addAndPayViewCardsList) && (ccEnabled  || dcEnabled) && saveCardEnabled=='true'}">
			<c:set var="paymentType" value="5" />
		</c:when>
		<c:when test="${dcEnabled}">
			<c:set var="paymentType" value="2" />
		</c:when>
		<c:when test="${ccEnabled}">
			<c:set var="paymentType" value="1" />
		</c:when>
		<c:when test="${netBankingEnabled}">
		<c:set var="paymentType" value="3" />
		</c:when>
		<c:when test="${atmEnabled}">
			<c:set var="paymentType" value="8" />
		</c:when>
		<c:when test="${impsEnabled}">
			<c:set var="paymentType" value="6" />
		</c:when>
		<c:when test="${emiEnabled}">
			<c:set var="paymentType" value="13" />
		</c:when>
		<c:when test="${codEnabled}">
			<c:set var="paymentType" value="12" />
		</c:when>
		<c:when test="${upiEnabled}">
			<c:set var="paymentType" value="15" />
		</c:when>
	</c:choose>
</c:if>
<%-- for cod, open sc is available (first time only) --%>
<c:if test="${paymentType eq 12 && empty param.txn_Mode}">
	
   	<c:if  test="${ saveCardEnabled eq true}">
   		<c:set var="paymentType" value="5"></c:set>
   	</c:if>
</c:if>
<c:if test="${(empty paymentType || (paymentType!=2 && paymentType !=1)) && themeInfo.subTheme eq 'ccdc'}">
	<c:set var="paymentType" value="2"></c:set>
</c:if>

<c:if test="${loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled && digitalCreditInfo.digitalCreditEnabled && (param.use_paytmcc eq 1 || (empty param.use_paytmcc && paymentType ne 17)) && empty param.txn_Mode}">
	<c:set var="ifPostpaid" value="true"></c:set>
</c:if>
<c:set var="isRetryAvaiable" value="false" ></c:set>
<!--changes for retry mode -->
<c:if test="${!empty retryPaymentInfo.paymentMode && empty param.txn_Mode && empty param.use_paytmcc && empty param.use_payment_bank && empty param.use_wallet}">
	<c:set var = "retryPaymentType" value = "${retryPaymentInfo.paymentMode}"></c:set>
	<c:set var="isRetryAvaiable" value="true" ></c:set>
	<c:choose>
		<c:when test="${'cc' == retryPaymentType}">
			<c:set var="paymentType" value="1" />
		</c:when>
		<c:when test="${'dc' == retryPaymentType}">
			<c:set var="paymentType" value="2" />
		</c:when>
		<c:when test="${'nb' == retryPaymentType}">
			<c:set var="paymentType" value="3" />
		</c:when>
		<c:when test="${'ot' == retryPaymentType}">
			<c:set var="paymentType" value="4" />
		</c:when>
		<c:when test="${'SC' == retryPaymentType}">
			<c:set var="paymentType" value="5" />
		</c:when>
		<c:when test="${'imps' == retryPaymentType}">
			<c:set var="paymentType" value="6" />
		</c:when>
		<c:when test="${'ppi' == retryPaymentType}">
			<c:set var="paymentType" value="7" />
		</c:when>
		<c:when test="${'atm' == retryPaymentType}">
			<c:set var="paymentType" value="8" />
		</c:when>
		<c:when test="${'telco' == retryPaymentType}">
			<c:set var="paymentType" value="9" />
		</c:when>
		<c:when test="${'cashcard' == retryPaymentType}">
			<c:set var="paymentType" value="10" />
		</c:when>
		<c:when test="${'cod' == retryPaymentType}">
			<c:set var="paymentType" value="12" />
		</c:when>
		<c:when test="${'rewards' == retryPaymentType}">
			<c:set var="paymentType" value="11" />
		</c:when>
		<c:when test="${'emi' == retryPaymentType}">
			<c:set var="paymentType" value="13" />
		</c:when>
		<c:when test="${'upi' == retryPaymentType}">
			<c:set var="paymentType" value="15" />
		</c:when>
		<c:when test="${'paytm_digital_credit' == retryPaymentType}">
			<c:set var="paymentType" value="16" />
		</c:when>
		<c:when test="${'paytm_payment_bank' == retryPaymentType}">
			<c:set var="paymentType" value="17" />
		</c:when>
		<c:when test="${'upi_push' == retryPaymentType}">
			<c:set var="paymentType" value="18" />
		</c:when>
	</c:choose>
</c:if>

<c:set var="isPgPaymodesAvaiable" value="true" />
<c:if test="${(!onlyWalletEnabled && !txnConfig.addAndPayAllowed) && (entityInfo.dcEnabled || entityInfo.ccEnabled || cardInfo.saveCardEnabled || entityInfo.netBankingEnabled ||
		      entityInfo.atmEnabled || entityInfo.impsEnabled || entityInfo.codEnabled || entityInfo.emiEnabled || txnConfig.chequeDDNeftEnabled || entityInfo.upiEnabled) eq false}">
	<c:set var="isPgPaymodesAvaiable" value="false" />
</c:if>

<!--end of changes-->