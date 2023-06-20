<%@ page import="java.util.List" %>
<%@ page import="com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO" %>
<script type="text/javascript">
	maintainenceATMBank = new Object();
	lowPerfATMBank = new Object();
</script>
<%String contextPathdc = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProjectProperty("context.path"); %>

<%
	boolean showOthers = false;
   	boolean selectOther = false;
   	String reqBankCode = (String) request.getAttribute("reqBankCode");
	String bankCode = "";
	String bankDisplayName = "Select your Bank";

	EntityPaymentOptionsTO entityInfo = ((EntityPaymentOptionsTO) session.getAttribute("entityInfo"));
	List dbList = null, atmList = null;
	if(entityInfo != null) {
		dbList = entityInfo.getCompleteDcList();
		atmList = entityInfo.getCompleteATMList();
	}


	if (reqBankCode != null
			&& reqBankCode.toString().trim().indexOf("other") > -1
			&& !"null".equalsIgnoreCase(reqBankCode)) {
		showOthers = true;
		bankCode = reqBankCode;
	}

	if ((dbList == null || dbList.size() == 0)
			&& (atmList == null || atmList.size() == 0)
			|| "other".equals(reqBankCode)) {
		showOthers = true;
		selectOther = true;
		bankCode = "other";
	}
%>

<div data-role="content" data-theme="i" id="debitcard-container" <c:if test="${'2' ne paymentType  && '8' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
    		<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
				<li>${requestScope.validationErrors['INVALID_CARD']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
				<li>${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
				<li>${requestScope.validationErrors['INVALID_CVV']}</li>
			</c:if>
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_DC_BANK']}">
				<div class="error">${requestScope.validationErrors['INVALID_DC_BANK']}</div>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for
				Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" method="post" action="<%=contextPathdc %>/payment/request/submit" data-ajax="false">
			<div data-role="fieldcontain" class="ui-field-contain ui-body ui-br">
       			<!-- <label for="selectBank" class="ui-input-text">Select your Bank</label>-->
<!--         		<a href="#" class="ui-link" id="atmBankSelect"> -->
<!--         			<div id="atmBankName" class="operator"> -->
<!--         				<span id="bankNameSpan">Select your Bank</span> -->
<!--          			 	<div class="arw fr"></div> -->
<!--           				<div class="clear"></div> -->
<!--         			</div> -->
<!--         		</a> -->
				<c:if test="${!empty entityInfo.completeDcList}">
					<script type="text/javascript">
						<c:forEach var="item1" items="${entityInfo.completeDcList}">
							<c:if test="${item1.maintainence}">
								maintainenceATMBank["${item1.bankName}"] = true;
							</c:if>
							<c:if test="${item1.lowPercentage}">
								lowPerfATMBank["${item1.bankName}"] = true;
							</c:if>
						</c:forEach>
					</script>
				</c:if>
					<select id="atmBankSelect">
						<option value="">Select</option>
						<c:if test="${!empty entityInfo.completeDcList}">
							<c:forEach var="item" items="${entityInfo.completeDcList}">
								<option id="${item.bankName}" value = "${item.bankName}">${item.displayName}</option>
							</c:forEach>
						</c:if>
				</select>
        	</div>
        	<div id="debitcard-form" style="display:none; margin-bottom: 50px">
        		<div data-role="fieldcontain">
        			<label for="debitcard" class="ui-input-text">Debit Card Number </label>
        			<input autocomplete="off" type="tel" name="cardNumber" maxlength="19">
      			</div>
      			
      			<div data-role="fieldcontain">
        			<label for="Expiry date" class="ui-input-text">Expiry date (MMYY) </label>
        			<input name="ccExpiryMonthYear" type="tel" maxlength="4">
        			<span class="sm-txt"> <em> (Optional for Maestro Cards) </em> </span>
        		</div>
      			
      			<div data-role="fieldcontain">
        			<label for="cvv" class="ui-input-text">CVV Number </label>
        			<input autocomplete="off" type="tel" name="cvvNumber" maxlength="4">
        			<span class="sm-txt"> <em> (Optional for Maestro Cards) </em> </span>
        		</div>
        		<c:if test="${saveCardOption}">
	        		<div class="pad mt5 mb10" id="card">
	        			<div  class="save-card-option">
	          				<input name="storeCardFlag" type="checkbox" class="styled" value="N">
	        			</div>
	        			<div class="label">
	          				<label for="card">Save this card for future transactions</label>
	        			</div>
	      			</div>
	      		</c:if>
        	</div>
        	
        	<div id="dcWarningDiv" class="alert">
        		<span id="dcErrorMsg"></span>
        	</div>
        	
			<input type="hidden" name="txnMode" value="DC"/>
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="bankCode" id="atmbankCode" value="<%=bankCode %>"/>
			
			<div data-role="footer" data-id="foo1" data-position="fixed" id="dcSubmit">
				<input	name="Submit" type="submit" value="Pay Now" class="button" />
			</div>
		</form>
	</section>
</div>

<!-- <div id="atmBankList" data-role="page" data-theme="i" style="display:none;">
  <div data-role="header" data-fullscreen="true" class="header">
  	<span class="ml5">Select Bank</span>
  	<a href="#" id="dcListBack" data-icon="custom-back-icon"></a>
  </div>
  <div data-role="content" data-theme="i" class="recharges">
    <section class="m-opt" id="dcListSection">
      <ul>
		<c:if test="${!empty entityInfo.completeDcList}">
			<c:forEach var="item" items="${entityInfo.completeDcList}">
				<li class="opr"> <a href="#" id="${item.bankName}">${item.displayName}</a></li>
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
      </ul>
    </section>
  </div>
</div> -->

<% if(showOthers){ %>
<script>

$(document).ready(function(){
	$('#debitcard-form').show();
	//$("#bankNameSpan").text($("#<%=bankCode%>").text());
});


</script>

<%} %>