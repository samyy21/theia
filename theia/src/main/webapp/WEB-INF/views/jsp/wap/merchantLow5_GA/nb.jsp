<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${3 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading">Net Banking</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
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
    	<label class="ml5">Select your Bank</label><br />

    	<select id="nbSelect" class="mt5 ml5 width97" onchange = "processNB(this); selectTab(); document.getElementById('btnSubmit').focus();">

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
    <div id="warningDiv" class = "clear failure ml5" style="display: none; margin-right: 10px !important; "><span id="errorMsg"></span></div>
    
     <%--post convenince related --%>
     <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null &&  txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
     	<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
		<div class="post-conv-inclusion" style="display:none;margin-right: 10px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
			<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
		</div>
	 </c:if>
	  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null &&  txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
	  		<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
			</div>
	 </c:if>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.NB != null &&  txnConfig.paymentCharges.NB.totalConvenienceCharges ne '0.00'}">
			<div class="post-conv-inclusion" id="normal-post-con" style="margin-right: 10px;">
				<%@ include file="../../common/post-con/nb-postconv.jsp" %>
			</div>
	</c:if>
    <p class="pt7">
    	<button type="submit" style="width:97%;margin-left:5px;border-radius: 5px;" class="clear gry-btn" data-txnmode="NB" onclick="pushGAData(this, 'pay_now_clicked')" disabled = "disabled" id="btnSubmit">Pay Now</button>
    </p>
 </c:if>
</div>
</form>

<script>
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		var lowSuccessRateMsg =  $("#error-details")[0].getAttribute("data-lowerrormsg");
		var maintenanceMsg = $("#error-details")[0].getAttribute("data-maintenancemsg");
		
		if (bankName && bankName.length > 0) {
			document.getElementById('bankCode').value = bankName;
			document.getElementById('nbSelect').value = bankName;
			checkBank(bankName, displayName);
		} 
		
		function checkBank(bankName, displayName){
			var btn = document.getElementById('btnSubmit');
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
				errorMsg.innerText = displayName + " "	+ maintenanceMsg;
				warningMsg.style.display = "block";
			} else if (lowPerfNBBank[bankName]) {
				var replacedTextLowSuccess = lowSuccessRateMsg.replace('@BANK @METHOD',bankName + " bank");
				errorMsg.innerText = replacedTextLowSuccess;
				warningMsg.style.display = "block";
			}
		}
		
		function processNB(obj) {
			var bankNameIn = obj.options[obj.selectedIndex].value;
			
			if(bankName === undefined || bankName == null || bankName.length <= 0){
				bankName = document.getElementById('bankCode').value = bankNameIn;
				document.getElementById('nbSelect').value = bankName;
			}

			var displayName =  obj.options[obj.selectedIndex].innerText;
			checkBank(bankNameIn, displayName);
		}
		
		function getBankName(obj){
			var bankName = obj.options[obj.selectedIndex].value;
			return bankName;
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
			document.getElementById('btnSubmit').focus();
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
</c:otherwise>
</c:choose>
</c:if>
