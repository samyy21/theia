<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${3 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading" style="padding-bottom:0;">Net Banking</div>
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
<div class="heading ml15 mt20" style="padding-bottom:0;">Net Banking</div>
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
<div class="row-dot ml20 mr20 ">
	
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
				<div class="bank-block  <c:if test="${bank.bankName eq txnInfo.selectedBank}">bank-block-selected</c:if> " id="bank-block-${bank.bankName}">
					<div class="single-bank" onclick="selectBank('${bank.bankName}')">
						<script>
							console.log('wap logo:','${bank.bankWapLogo}','${bank.bankWebLogo}');
						</script>
						<span class="bank-logo-nb-new " alt="${bank.displayName}" style="background : url(/theia/resources/images/wap/bank/new-icon/${bank.bankWapLogo}) no-repeat;width: 40px; margin: auto; display: block;"></span>
						<span class="" style="text-align: center;margin: auto;display: block;" id ="bank-block-name-${bank.bankName}">${bank.bankName}</span>
					</div>
				</div>
			</c:if>
	</c:forEach>
</div>
<div class="clear"></div>
	<div class="row-dot ml20 mr20">

    	<select id="nbSelect" class="mt5 all-input special-input-select" style="width: 100%;margin-right: 23px;" onchange = "processNB(this); selectTab();selectBoxColorChange(this); document.getElementById('submitButton').focus();">

    		<option value="-1" selected = "selected">All Other Banks</option>
<!--         	<optgroup label="Popular Banks"></optgroup> -->
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
<!--         		<optgroup label="Other Banks"></optgroup> -->
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
        
    </div>
    <div id="warningDiv" class = "failure low-success-rate" style="display: none; margin: 10px 25px 0 25px "><span id="errorMsg"></span></div>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
			<div class="post-conv-inclusion" style="margin: 10px 20px;">
				<%@ include file="../../common/post-con-withr/nb-postconv.jsp" %>
			</div>
	</c:if>
    <p class="pt7 ml25 mt15" style="margin-right: 26px !important;">
    	<button type="submit" class="gry-btn" disabled = "disabled" id = "submitButton" style="width: 100%; padding-top: 11px; padding-bottom: 15px;">${submitBtnText}</button>
    </p> 
    
    <!--Lock image-->
    <div class="pt7 ml20 mt15" style="margin-bottom: 17px;">
    	<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
        <div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        <div class="fl lock-display"> We do not share your card details with anyone</div>
        <div class="clear"></div>
    </div>
    
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
		
		function selectBank(bankName){
			// clear selection of bank if any
			clearAllBankSelection();
			// highlight the current selected bank
			document.getElementById('bank-block-'+bankName).classList.add('bank-block-selected');
			document.getElementById('bank-block-name-'+bankName).classList.add('bold-font-600');
			// check for maintenance/ low performance msg
			checkBank(bankName, bankName);
			// put the same value in select checkbox
			document.getElementById('nbSelect').value = bankName;
			document.getElementById('nbSelect').style.color = "#222";
			document.getElementById('nbSelect').style.fontWeight = "600";
			
			// set value in hidden field
			document.getElementById('bankCode').value = bankName;
			document.getElementById('submitButton').focus();
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
			if(obj.selectedIndex != 0){
				obj.style.fontWeight = 600;
			} else {
				obj.style.fontWeight = 400;
			}
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
// 		function nbBankSelect(){
// 			if(document.querySelector('input[name="bankNm"]:checked')){
// 			var tabSelectVal=document.querySelector('input[name="bankNm"]:checked').value;
// 			if(tabSelectVal){
// 				var obj=document.getElementById("nbSelect");
// 				obj.value = tabSelectVal;
			
// 			var bankName = obj.options[obj.selectedIndex].value;
// 			var displayName =  obj.options[obj.selectedIndex].innerText;
// 			checkBank(bankName, displayName);
// 			document.getElementById('submitButton').focus();
// 			}
// 			}
// 		}

		//NB CHANGE DROP BOX
		function selectTab(){
			var e = document.getElementById("nbSelect");
			var nbselectVal = e.options[e.selectedIndex].value;
			//clear all bank selection till now.
			clearAllBankSelection();
			//select the bank in top box if it exists.
			if(document.getElementById("bank-block-" + nbselectVal)){
				document.getElementById('bank-block-'+nbselectVal).classList.add('bank-block-selected');
			}
		}
		function clearAllBankSelection(){
			var allBankBlocks = $('.bank-block');
			for (var i=0; i < allBankBlocks.length; i++) {
			    allBankBlocks[i].classList.remove('bank-block-selected');
			    allBankBlocks[i].childNodes[1].childNodes[5].classList.remove('bold-font-600');
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
<div class="mt20"></div>
</c:if>
