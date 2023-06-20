<c:if test="${isAddMoneyAvailable}">
    <c:set var="dcEnabled" value="${entityInfo.dcEnabled}"/>
    <c:set var="ccEnabled" value="${entityInfo.ccEnabled}"/>
    <c:set var="saveCardEnabled" value="${cardInfo.saveCardEnabled}"/>
    <c:set var="netBankingEnabled" value="${entityInfo.netBankingEnabled}"/>
    <c:set var="atmEnabled" value="${entityInfo.atmEnabled}"/>
    <c:set var="impsEnabled" value="${entityInfo.impsEnabled}"/>
    <%--     <c:set var="cashcardEnabled" value="${txnConfig.cashcardEnabled}"/> --%>
    <c:set var="codEnabled" value="${entityInfo.codEnabled}"/>
    <c:set var="emiEnabled" value="${entityInfo.emiEnabled}"/>
    <c:set var="chequeDDNeftEnabled" value="${txnConfig.chequeDDNeftEnabled}"/>
    <c:set var="upiEnabled" value="${entityInfo.upiEnabled}"></c:set>
</c:if>

<c:if test="${existAddMoneyTab}">
    <c:set var="dcEnabled" value="${entityInfo.addDcEnabled}"/>
    <c:set var="ccEnabled" value="${entityInfo.addCcEnabled}"/>
    <c:set var="saveCardEnabled" value="${cardInfo.saveCardEnabled}"/>
    <c:set var="netBankingEnabled" value="${entityInfo.addNetBankingEnabled}"/>
    <c:set var="atmEnabled" value="${entityInfo.addAtmEnabled}"/>
    <c:set var="impsEnabled" value="${entityInfo.addImpsEnabled}"/>
    <c:set var="codEnabled" value="${entityInfo.addCodEnabled}"/>
    <c:set var="emiEnabled" value="${entityInfo.addEmiEnabled}"/>
    <c:set var="chequeDDNeftEnabled" value="${txnConfig.chequeDDNeftEnabled}"/>
    <c:set var="upiEnabled" value="${entityInfo.addUpiEnabled}"></c:set>
</c:if>
<c:if test="${cardInfo.saveCardEnabled}">
    <li class="card ${5 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#sc-card">Saved Details</a>
    </li>
</c:if>

<c:if test="${upiEnabled}">
    <li class="card ${15 eq paymentType ? 'active' : ''}">

        <a href="#upi-mode">BHIM UPI</a>
    </li>
</c:if>

<c:if test="${dcEnabled}">
    <li class="card ${2 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#dc-card">Debit Card</a>
    </li>
</c:if>
<c:if test="${ccEnabled}">
    <li class="card ${1 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#cc-card">Credit Card</a>
    </li>
</c:if>
<c:if test="${netBankingEnabled}">
    <li class="card ${3 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#nb-card">Net Banking</a>
    </li>
</c:if>

<c:if test="${emiEnabled}">
    <li class="card ${13 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#emi-card">EMI</a>
    </li>
</c:if>
<c:if test="${codEnabled}">
    <li class="card ${12 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#cod-card">Cash On Delivery (COD)</a>
    </li>
</c:if>
<c:if test="${atmEnabled}">
    <li class="card ${8 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#atm-card">ATM</a>
    </li>
</c:if>
<c:if test="${impsEnabled}">
    <li class="card ${6 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#imps-card">IMPS</a>
    </li>
</c:if>
<c:if test="${cashcardEnabled}">
    <li class="card ${10 eq paymentType ? 'active' : ''}">
        <span class="fr img img-arw-rt"></span>
        <a href="#itz-card">Cash Card</a>
    </li>
</c:if>