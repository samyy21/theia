<c:if test="${saveCardEnabled}">
	<div class="pay-mode-tab SC-tab">
		<%@ include file="sc.jsp"%>
	</div>
</c:if>
<c:if test="${dcEnabled}">
	<div class="pay-mode-tab DC-tab">
		<%@ include file="dc.jsp"%>
	</div>
</c:if>
<c:if test="${ccEnabled}">
	<div class="pay-mode-tab CC-tab">
		<%@ include file="cc.jsp"%>
	</div>
</c:if>
<c:if test="${netBankingEnabled}">
	<div class="pay-mode-tab NB-tab">
		<%@ include file="nb.jsp"%>
	</div>
</c:if>
<c:if test="${atmEnabled}">
	<div class="pay-mode-tab ATM-tab">
		<%@ include file="atm.jsp"%>
	</div>
</c:if>
<c:if test="${impsEnabled}">
	<div class="pay-mode-tab IMPS-tab">
		<%@ include file="imps.jsp"%>
	</div>
</c:if>
<c:if test="${emiEnabled}">
	<div class="pay-mode-tab EMI-tab">
		<%@ include file="emi.jsp"%>
	</div>
</c:if>
<c:if test="${codEnabled}">
	<div class="pay-mode-tab COD-tab" <c:if test="${usePaytmCash && !txnConfig.codHybridAllowed}"> style="display:none;"</c:if>>
		<%@ include file="cod.jsp"%>
	</div>
</c:if>
<c:if test="${upiEnabled}">
        <div class="pay-mode-tab UPI-tab">
                <%@ include file="upi.jsp"%>
        </div>
</c:if>
