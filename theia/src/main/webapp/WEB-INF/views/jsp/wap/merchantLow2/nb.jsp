<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${3 eq paymentType}">
	<script type="text/javascript">
		maintainenceNBBank = new Object();
		lowPerfNBBank = new Object();
	</script>
	<%
   		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
<div class="heading">Net Banking</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="NB" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	<input type="hidden" name="bankCode" id="bankCode">
	<input type="hidden" name="walletAmount" value="0" />
	<c:if test="${txnConfig.addMoneyFlag}">
		<input type="hidden" name="addMoney" value="1" />
	</c:if>
<div class="row-dot">
<c:set var="nbList" value="${entityInfo.completeNbList}" ></c:set>
<c:if test="${existAddMoneyTab}">
	<c:set var="nbList" value="${entityInfo.addCompleteNbList}" ></c:set>
</c:if>
<c:if test="${!empty nbList}">
<!-- BANKS Tabs  -->
<p class="row-dot">
	<div class="fields">
			<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
				<c:if test="${nbList.size() > loopVar.index}">
					<c:set var="bank" value="${nbList[loopVar.index]}" />
						<script type="text/javascript">
							<c:if test="${bank.maintainence}">
								maintainenceNBBank["${bank.bankName}"] = true;
							</c:if>
							<c:if test="${bank.lowPercentage}">
								lowPerfNBBank["${bank.bankName}"] = true;
							</c:if>
						</script>
						<label class="nbTab" onclick="nbBankSelect()"><input type="radio" class="bankNms" name="bankNm" id="${bank.bankName}" value="${bank.bankName}" <c:if test="${bank.bankName eq txnInfo.selectedBank}">checked</c:if> />${bank.displayName}</label>
					</c:if>
			</c:forEach>
</p>
<div class="clear"></div>


	<p>
    	<label>Select your Bank</label><br />

    	<select id="nbSelect" class="mt5" onchange = "processNB(this); selectTab(); document.getElementById('submitButton').focus();">

    		<option value="-1" selected = "selected">Select</option>
        	<optgroup label="Popular Banks"></optgroup>
        		<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
					<c:if test="${nbList.size() > loopVar.index}">
						<c:set var="bank" value="${nbList[loopVar.index]}" />
							<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
							</script>
							<option value="${bank.bankName}" <c:if test="${bank.bankName eq txnInfo.selectedBank}">selected=selected</c:if> >${bank.displayName}</option>
						</c:if>
					</c:forEach>	
        	
        	
        	<c:if test="${nbList.size() > 6}">
        		<optgroup label="Other Banks"></optgroup>
        			<c:forEach var="bank" items="${nbList}" begin="6">
              			<option id="${bank.bankName}-option" value="${bank.bankName}"
							<c:if test="${reqBankCode eq bank.bankName}">selected=selected</c:if>
							<c:if test="${bank.bankName eq txnInfo.selectedBank}">selected=selected</c:if> >${bank.displayName}
						</option>
              				<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
						</script>
					</c:forEach>
            	
            </c:if>
        </select>
        
    </p>
    </div>
    <p class="pt7"><input type="submit" value="${submitBtnText}" class="gry-btn" disabled = "disabled" id = "submitButton"/></p> 
    
    <!--Lock image-->
    <div class="pt7">
    	<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
        <div class="fl small secure-text"> Your payment details are secured via 128 Bit encryption by Verisign</div>
        <div class="clear"></div>
    </div>
    <div id="warningDiv" class = "failure" style="display: none"><span id="errorMsg"></span></div>
 </c:if>
</div>
</form>

<script>
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		var lowSuccessRateMsg =  $("#error-details")[0].getAttribute("data-lowerrormsg");
		var maintenanceMsg = $("#error-details")[0].getAttribute("data-maintenancemsg");
		if (bankName.length > 0) {
			document.getElementById('bankCode').value = bankName;
			document.getElementById('nbSelect').value = bankName;
			checkBank(bankName, displayName);
		} 
		
		function checkBank(bankName, displayName){
			var btn = document.getElementById('submitButton');
			var errorMsg = document.getElementById('errorMsg');
			var warningMsg = document.getElementById('warningDiv');
			warningMsg.style.display = "none";
			errorMsg.innerText = "";
			
			if(maintainenceNBBank[bankName] || bankName == "-1") {
				btn.disabled = true;
				btn.className = "gry-btn";
			} else {
				btn.disabled = false;
				btn.className = "blue-btn";
			}
			
			if (maintainenceNBBank[bankName]) {
				errorMsg.innerText = displayName + " " + maintenanceMsg;
				warningMsg.style.display = "block";
			} else if (lowPerfNBBank[bankName]) {
				var replacedTextLowSuccess = lowSuccessRateMsg.replace('@BANK @METHOD',bankName + " bank");
				errorMsg.innerText = replacedTextLowSuccess;
				warningMsg.style.display = "block";
			}
		}
		
		function processNB(obj) {
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			checkBank(bankName, displayName);
		}
		
		processNB($('#nbSelect')[0]);
		
		// NB TABS
		function nbBankSelect(){
			if(document.querySelector('input[name="bankNm"]:checked')){
			var tabSelectVal=document.querySelector('input[name="bankNm"]:checked').value;
			if(tabSelectVal){
				var obj=document.getElementById("nbSelect");
				obj.value = tabSelectVal;
			
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			checkBank(bankName, displayName);
			document.getElementById('submitButton').focus();
			}
			}
		}

		//NB CHANGE DROP BOX
		function selectTab(){
			var e = document.getElementById("nbSelect");
			var nbselectVal = e.options[e.selectedIndex].value;
			if(document.getElementById(nbselectVal)){
			document.getElementById(nbselectVal).checked = true;
			}
			else{
				clearRB($(".bankNms"));
			}
		}
		
		function clearRB(buttonGroup)
		{
		  for (var i=0; i < buttonGroup.length; i++) {
		 
		    if (buttonGroup[i].checked == true) { // if a button in group is checked,
		          buttonGroup[i].checked = false;  // uncheck it
		      }
		     
		  }  
		}
		

		
</script>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'nb']);
	} catch(e){}
</script>
</c:if>
