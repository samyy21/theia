<c:set var="addMoneySaveCardEnabled" value="false" />

<c:if test="${txnConfig.dcEnabled or txnConfig.ccEnabled}">
    <c:if test="${!empty cardInfo.savedCardsList}">
        <c:set var="addMoneySaveCardEnabled" value="true"></c:set>
    </c:if>
</c:if>

<c:set var="saveCardOption" value="false"/>
<c:if test="${cardInfo.cardStoreOption}">
    <c:set var="saveCardOption" value="true"/>
</c:if>


<c:set var="errorsPresent">0</c:set>
<c:if test="${!empty requestScope.validationErrors && requestScope.addMoneyFlag eq 1}">
    <c:set var="errorsPresent">1</c:set>
</c:if>
