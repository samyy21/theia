<c:if test="${8 eq paymentType}">
	<script type="text/javascript">
		maintainenceATMBank = new Object();
		lowPerfATMBank = new Object();
	</script>
	<%
	String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
<div class="heading">ATM</div>
<form autocomplete="off" method="post" action="/payment/request/submit" onsubmit = "submitForm()">
	<input type="hidden" name="txnMode" value="ATM" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	<input type="hidden" name="bankCode" id="atmBankCode">
	<input type="hidden" name="walletAmount" value="0" />
<div class="row-dot">
<c:if test="${!empty entityInfo.completeATMList}">
	<p>
    	<label>Select your Bank</label><br />
    	<select id="atmSelect" class="mt5" onchange = "processATM(this)">
    		<option value="-1" selected = "selected">Select</option>
    		<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
					<c:if test="${entityInfo.completeATMList.size() > loopVar.index}">
						<c:set var="bank" value="${entityInfo.completeATMList[loopVar.index]}" />
						
						<script type="text/javascript">
							<c:if test="${bank.maintainence}">
								maintainenceATMBank["${bank.bankName}"] = true;
							</c:if>
							<c:if test="${bank.lowPercentage}">
								lowPerfATMBank["${bank.bankName}"] = true;
							</c:if>
						</script>
                <option id="${bank.bankName}" value="${bank.bankName}">${bank.displayName}</option>
                	</c:if>
                </c:forEach>
        </select>
    </p>
    
    <p class="pt7"><input type="submit"  value="Proceed securely" class="gry-btn" disabled = "disabled" id = "submitButton"/></p> 
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="/images/wap/paytmLow/lock.png" alt="" title="" /></div>
        <div class="fl small"> Your card details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    <div id="atmWarningDiv" class = "failure" style="display: none"><span id="atmErrorMsg"></span></div>
    </c:if>
</div>
</form>

<script>
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		if(bankName.length > 0) {
			document.getElementById('atmBankCode').value = bankName;
			document.getElementById('atmSelect').value = bankName;
			if(maintainenceATMBank[bankName]) {
				document.getElementById('submitButton').disabled = true;
				document.getElementById('submitButton').className = "gry-btn";
			} else {
				document.getElementById('submitButton').disabled = false;
				document.getElementById('submitButton').className = "blue-btn";
			}
			if(maintainenceATMBank[bankName]) {
				document.getElementById('atmErrorMsg').innerText = displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
				document.getElementById('atmWarningDiv').style.display = "block";
			} else if(lowPerfATMBank[bankName]) {
				document.getElementById('atmErrorMsg').innerText = "Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.";
				document.getElementById('atmWarningDiv').style.display = "block";
			}
		}
		
		function processATM(obj) {
			document.getElementById('atmWarningDiv').style.display = "none";
			document.getElementById('atmErrorMsg').innerText = "";
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			if(maintainenceATMBank[bankName] || bankName == "-1") {
				document.getElementById('submitButton').disabled = true;
				document.getElementById('submitButton').className = "gry-btn";
			} else {
				document.getElementById('submitButton').disabled = false;
				document.getElementById('submitButton').className = "blue-btn";
			}
			if (maintainenceATMBank[bankName]) {
				document.getElementById('atmErrorMsg').innerText = displayName	+ " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
				document.getElementById('atmWarningDiv').style.display = "block";
			} else if (lowPerfATMBank[bankName]) {
				document.getElementById('atmErrorMsg').innerText = "Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.";
				document.getElementById('atmWarningDiv').style.display = "block";
			}
			
		}
</script>

</c:if>
