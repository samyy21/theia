<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${8 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading">ATM</div>
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
		 <%--post convenince related --%>
		  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null &&  txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
			<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" style="display:none; margin-right: 10px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
			</div>
		 </c:if>
		  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null &&  txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
		 	 <c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
			</div>
		 </c:if>
	    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.ATM != null &&  txnConfig.paymentCharges.ATM.totalConvenienceCharges ne '0.00'}">
				<div class="post-conv-inclusion" id="normal-post-con" style="margin-right: 10px;">
					<%@ include file="../../common/post-con/atm-postconv.jsp" %>
				</div>
		</c:if>
		<p class="pt7">
			<button type="submit" class="gry-btn" disabled = "disabled" id = "btnSubmit">${submitBtnText}</button>
		</p> 
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
		var lowSuccessRateMsg =  $("#error-details")[0].getAttribute("data-lowerrormsg");
		var maintenanceMsg = $("#error-details")[0].getAttribute("data-maintenancemsg");
		var displayName = "";
		if(bankName.length > 0) {
			document.getElementById('atmBankCode').value = bankName;
			document.getElementById('atmSelect').value = bankName;
			checkATMBank(bankName, displayName);
		}
		
		function checkATMBank(bankName, displayName){
			var btn = document.getElementById('btnSubmit');
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
			checkATMBank(bankName, displayName);
		}
</script>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'atm']);
	} catch(e){}
</script>
</c:otherwise>
</c:choose>
</c:if>
