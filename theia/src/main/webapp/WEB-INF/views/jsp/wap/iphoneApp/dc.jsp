<%@ page import="java.util.List" %>
<%@ page import="com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO" %>
<script type="text/javascript">
	maintainenceATMBank = new Object();
	lowPerfATMBank = new Object();
</script>
<%
	boolean showOthers = false;
	boolean selectOther = false;
	if ((request.getParameter("bankCode") != null && (request.getParameter("bankCode").trim().indexOf("other")>-1 ))
				|| (request.getAttribute("reqBankCode") != null && (request.getAttribute("reqBankCode").toString().trim().indexOf("other")>-1 ))) {
		showOthers = true;
	}
	EntityPaymentOptionsTO entityInfo = ((EntityPaymentOptionsTO) session.getAttribute("entityInfo"));
	List dbList = null, atmList = null;
	if(entityInfo != null) {
		dbList = entityInfo.getCompleteDcList();
		atmList = entityInfo.getCompleteATMList();
	}
	if((dbList == null || dbList.size()==0) && (atmList == null || atmList.size()==0)) {
		showOthers = true;
		selectOther = true;
		request.setAttribute("reqBankCode", "other");
	}
%>

<%-- <c:if test="${'2' ne paymentType && '8' ne paymentType}">
	<section class="nav-bar">
		<a href="/jsp/wap/iphoneApp/paymentForm.jsp?txn_Mode=DC"> Debit Card </a>
	</section>
</c:if> --%>
<c:if test="${'2' eq paymentType || '8' eq paymentType}">
	<div class="divider"></div>
	<form autocomplete="off" method="post" action="/payment/request/submit">
		<div id="debitcardwap-container">
			<p class="mt15">
				<label>Select your Bank</label><br />
				
				<c:set var="atmCardOptions" value=""/>
				<c:if test="${!empty entityInfo.completeDcList}">
					<c:forEach var="item" items="${entityInfo.completeDcList}">
						<c:set var="atmCardOptions">${atmCardOptions}
						<option value="${item.bankName}" <c:if test="${reqBankCode eq item.bankName}">selected=selected</c:if>>${item.displayName}</option>
						</c:set>
						<script type="text/javascript">
							<c:if test="${item.maintainence}">
								maintainenceATMBank["${item.bankName}"] = true;
							</c:if>
							<c:if test="${item.lowPercentage}">
								lowPerfATMBank["${item.bankName}"] = true;
							</c:if>
						</script>
					</c:forEach>
				</c:if>
				
				<select name="bankCode" id="ATMBankCodeSelect" style="width: 100%">
					<option value="-1">Select your Bank</option>
					${atmCardOptions}
				</select>
			</p>
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_DC_BANK']}">
				<p class="error">${requestScope.validationErrors['INVALID_DC_BANK']}</p>
			</c:if>
			<span class="errorMsg" id="atmErrorMsg" style="display: block;"></span>
			<div id="debitcard-div" style="display:none">
				<p class="mt15">
					<label>Debit Card Number</label> <br /> 
					<input autocomplete="off"  type="tel" name="cardNumber" maxlength="19" /> <br /> 
				</p>
				<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<p class="error">${requestScope.validationErrors['INVALID_CARD']}</p>
				</c:if>
				
				<p class="mt15">
					<label>Expiry date (MMYY)</label> <br />
					<input name="ccExpiryMonthYear" type="tel" maxlength="4" /> <br /> <span class="sm-txt">
						<em> (Optional for Maestro Cards) </em> </span>
				</p>
				<c:if
					test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
					<p class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</p>
				</c:if>
				
				<p class="mt15">
					<label>CVV Number</label> <br />
					<input autocomplete="off"  type="tel" name="cvvNumber" maxlength="4" /> <br /> <span class="sm-txt">
						<em> (Optional for Maestro Cards) </em> </span>
				</p>
				<c:if
					test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
					<p class="error" id="dcCvvId">${requestScope.validationErrors['INVALID_CVV']}</p>
				</c:if>
				<c:if test="${saveCardOption}">
		        	<div class="pad mt5" id="card">        
			        	<div ><input  name="storeCardFlag"	type="checkbox" class="styled" value="Y"/></div>
			        	<div class="label"><label for="card" style="font-size: 15px">Save this card for future transactions</label></div>
			  		</div>
				</c:if>
			</div>			
			<input type="hidden" name="txnMode" value="DC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<div class="margin">
				 <input	name="Submit" type="submit" value="Pay Now" class="button" id="dcSubmit"/>
			</div> 
			<div class="margin mb15">
				<input id="cancelButton" name="cancleBtn" onClick="cancelTxn();" type="button" class="cancel" value="Cancel" />
			</div> 
		</div>
	</form>
</c:if>
<script type="text/javascript">

<% if(showOthers){%>
	$(document).ready(function(){
		 $("div[id$='debitcard-div']").show();
	});
<%} %>
$(document).ready(function(){
	var bankName = $('#ATMBankCodeSelect').val();
	var displayName = $('#ATMBankCodeSelect option:selected').text();
	if(maintainenceATMBank[bankName]) {
		$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
		$('#dcSubmit').attr("disabled", "disabled");
		$('#dcSubmit').removeClass('button');
		$('#dcSubmit').addClass('disableSubmit');
	} else if(lowPerfATMBank[bankName]) {
		$('#atmErrorMsg').text("Experiencing high failures on " + displayName + " in last few transactions. Recommend you pay using a different mode.");
	}
});
</script>