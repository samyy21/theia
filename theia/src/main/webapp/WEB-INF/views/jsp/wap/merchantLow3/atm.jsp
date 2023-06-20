<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${8 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading ml15 mt20">ATM</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
	<script type="text/javascript">
		maintainenceATMBank = new Object();
		lowPerfATMBank = new Object();
	</script>
	<%
	String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
<div class="heading" >ATM</div>
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
<div class="row-dot ml25 mr20">

	<c:set var="atmList" value="${entityInfo.completeATMList}" ></c:set>
	<c:if test="${existAddMoneyTab}">
		<c:set var="atmList" value="${entityInfo.addCompleteATMList}" ></c:set>
	</c:if>
	<c:if test="${!empty atmList}">
		<p>
<!-- 			<label>Select your Bank</label><br /> -->
			<select id="atmSelect" class="mt5 all-input special-input-select" onchange = "processATM(this);selectBoxColorChange(this);" style="width:100%; margin-bottom: 15px;">
				<option value="-1" selected = "selected">Select your bank</option>
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
		<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
			<div class="post-conv-inclusion">
				<%@ include file="../../common/post-con-withr/atm-postconv.jsp" %>
			</div>
		</c:if>
    
		<p class="pt7"><button type="submit"  class="gry-btn" disabled = "disabled" style="width: 100%;padding-top: 10px; padding-bottom: 15px;" id = "submitButton"/>${submitBtnText}</button></p> 
		<!--Lock image-->
		<div class="pt7 mt15">
			<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
        <div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        <div class="fl lock-display"> We do not share your card details with anyone</div>
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
				var replacedTextLowSuccess = lowSuccessRateMsg.replace('@BANK @METHOD',bankName + " bank");
				errorMsg.innerText = replacedTextLowSuccess;
				warningMsg.style.display = "block";
			}
			
		}
		
		function processATM(obj) {
			var bankName = obj.options[obj.selectedIndex].value;
			var displayName =  obj.options[obj.selectedIndex].innerText;
			if(obj.selectedIndex == 0){
				var btn = document.getElementById('submitButton');
				btn.disabled = true;
				btn.className = "gry-btn";
			} else {
				checkATMBank(bankName, displayName);
			}
		}
</script>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'atm']);
	} catch(e){}
</script>
</c:otherwise>
</c:choose>
<div class="mt20"></div>
</c:if>
