<c:if test="${3 eq paymentType}">
	<script type="text/javascript">
		maintainenceNBBank = new Object();
		lowPerfNBBank = new Object();
	</script>
	<%
   		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
<div class="heading">Net Banking</div>
<form autocomplete="off" method="post" action="/payment/request/submit" onsubmit = "submitForm()">
	<input type="hidden" name="txnMode" value="NB" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	<input type="hidden" name="bankCode" id="bankCode">
	<input type="hidden" name="walletAmount" value="0" />
<div class="row-dot">
<c:if test="${!empty entityInfo.completeNbList}">
	<p>
    	<label>Select your Bank</label><br />
    	<select id="nbSelect" class="mt5" onchange = "processNB(this)">
    		<option value="-1" selected = "selected">Select</option>
        	<optgroup label="Popular Banks">
        		<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
					<c:if test="${entityInfo.completeNbList.size() > loopVar.index}">
						<c:set var="bank" value="${entityInfo.completeNbList[loopVar.index]}" />
							<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
							</script>
							<option value="${bank.bankName}">${bank.displayName}</option>
						</c:if>
					</c:forEach>	
        	</optgroup>
        	<c:if test="${entityInfo.completeNbList.size() > 6}">
        		<optgroup label="Other Banks">
        			<c:forEach var="bank" items="${entityInfo.completeNbList}" begin="6">
              			<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${reqBankCode eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
              				<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
						</script>
					</c:forEach>
            	</optgroup>
            </c:if>
        </select>
        
    </p>
    
    <p class="pt7"><input type="submit" value="Proceed securely" class="gry-btn" disabled = "disabled" id = "submitButton"/></p> 
    
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="/images/wap/paytmLow/lock.png" alt="" title="" /></div>
        <div class="fl small"> Your card details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    <div id="warningDiv" class = "failure" style="display: none"><span id="errorMsg"></span></div>
 </c:if>
</div>
</form>

<script>
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		if (bankName.length > 0) {
			document.getElementById('bankCode').value = bankName;
			document.getElementById('nbSelect').value = bankName;
			if(maintainenceNBBank[bankName]) {
				document.getElementById('submitButton').disabled = true;
				document.getElementById('submitButton').className = "gry-btn";
			} else {
				document.getElementById('submitButton').disabled = false;
				document.getElementById('submitButton').className = "blue-btn";
			}
			if (maintainenceNBBank[bankName]) {
				document.getElementById('errorMsg').innerText = displayName	+ " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
				document.getElementById('warningDiv').style.display = "block";
			} else if (lowPerfNBBank[bankName]) {
				document.getElementById('errorMsg').innerText = "Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.";
				document.getElementById('warningDiv').style.display = "block";
			}
		} 
		function processNB(obj) {
			document.getElementById('warningDiv').style.display = "none";
			document.getElementById('errorMsg').innerText = "";
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			if(maintainenceNBBank[bankName] || bankName == "-1") {
				document.getElementById('submitButton').disabled = true;
				document.getElementById('submitButton').className = "gry-btn";
			} else {
				document.getElementById('submitButton').disabled = false;
				document.getElementById('submitButton').className = "blue-btn";
			}
			if (maintainenceNBBank[bankName]) {
				document.getElementById('errorMsg').innerText = displayName	+ " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
				document.getElementById('warningDiv').style.display = "block";
			} else if (lowPerfNBBank[bankName]) {
				document.getElementById('errorMsg').innerText = "Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.";
				document.getElementById('warningDiv').style.display = "block";
			}
			
		}
</script>
</c:if>
