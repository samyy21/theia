<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${8 eq paymentType}">
	<script type="text/javascript">
		maintainenceATMBank = new Object();
		lowPerfATMBank = new Object();
	</script>
	<%
	String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
<div class="heading">ATM</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="ATM" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	<input type="hidden" name="bankCode" id="atmBankCode">
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">

	<c:set var="atmList" value="${entityInfo.completeATMList}" ></c:set>
	<c:if test="${existAddMoneyTab}">
		<c:set var="atmList" value="${entityInfo.addCompleteATMList}" ></c:set>
	</c:if>
	<c:if test="${!empty atmList}">
		<p>
			<label>Select your Bank</label><br />
			<select id="atmSelect" class="mt5" onchange = "processATM(this)">
				<option value="-1" selected = "selected">Select</option>
				<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
						<c:if test="${atmList.size() > loopVar.index}">
							<c:set var="bank" value="${atmList[loopVar.index]}" />
						
							<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceATMBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfATMBank["${bank.bankName}"] = true;
								</c:if>
							</script>
		            <option id="${bank.bankName}" value="${bank.bankName}" <c:if test="${bank.bankName eq txnInfo.selectedBank}">selected=selected</c:if>>${bank.displayName}</option>
		            	</c:if>
		            </c:forEach>
		    </select>
		</p>
		
		<p class="pt7"><input type="submit"  value="${submitBtnText}" class="gry-btn" disabled = "disabled" id = "submitButton"/></p> 
		<!--Lock image-->
		<div class="pt7">
			<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
		    <div class="fl small"> Your payment details are secured via 128 Bit encryption by Verisign</div>
		    <div class="clear"></div>
		</div>
		<div id="atmWarningDiv" class = "failure" style="display: none"><span id="atmErrorMsg"></span></div>
    </c:if>
</div>
</form>

<script>
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		var lowSuccessRateMsg =  $("#error-details")[0].getAttribute("data-lowerrormsg");
		var maintenanceMsg = $("#error-details")[0].getAttribute("data-maintenancemsg");
		if(bankName.length > 0) {
			document.getElementById('atmBankCode').value = bankName;
			document.getElementById('atmSelect').value = bankName;
			checkATMBank(bankName, displayName);
		}
		
		function checkATMBank(bankName, displayName){
			var btn = document.getElementById('submitButton');
			var errorMsg = document.getElementById('atmErrorMsg');
			var warningMsg = document.getElementById('atmWarningDiv');
			warningMsg.style.display = "none";
			errorMsg.innerText = "";
			
			if(maintainenceATMBank[bankName]) {
				btn.disabled = true;
				btn.className = "gry-btn";
			} else {
				btn.disabled = false;
				btn.className = "blue-btn";
			}
			if (maintainenceATMBank[bankName]) {
				errorMsg.innerText = displayName + " " + maintenanceMsg;
				warningMsg.style.display = "block";
			} else if (lowPerfATMBank[bankName]) {
				errorMsg.innerText = lowSuccessRateMsg;
				warningMsg.style.display = "block";
			}
			
		}
		
		function processATM(obj) {
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			checkATMBank(bankName, displayName);
		}
</script>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'atm']);
	} catch(e){}
</script>
</c:if>
