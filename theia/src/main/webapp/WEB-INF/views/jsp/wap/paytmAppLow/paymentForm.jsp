<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ include file="../../common/config.jsp"%>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Paytm Secure Online Payment Gateway</title>
<meta name="robots" content="noindex,nofollow" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />
<link type="image/x-icon" rel="shortcut icon"
	href="images/web/Paytm.ico" />
<base href="/oltp-web/" />
<%String useMinifiedAssets = PropertiesUtil.getProjectProperty("context.useMinifiedAssets"); %>

<% if(useMinifiedAssets.equals("N")){ %>
<link rel="stylesheet" type="text/css"
	href="/css/wap/paytmAppLow/mobile.css" />
<% } else { %>
<link rel="stylesheet" type="text/css"
	href="/css/wap/paytmAppLow/mobile.min.css" />
<% } %>

<script type="text/javascript">
		 var paymentType = "${paymentType}";
		 var savedCardList = "${!empty cardInfo.savedCardsList}";
		var walletBalance="${walletInfo.walletBalance}";
		
		 
		 
		 function processPaytmCash(checkbox) {
			 
			 var isClicked = true;
			 if(!checkbox) {
				 checkbox = $("#paytmCashCB")[0];
				 isClicked = false;
			 }
			if(checkbox == null) return;
			
			var totalAmount = Number($("#totalAmtVal")[0].value);
			var walletBalance = Number($("#totalWalletVal")[0].value);
			var paidAmount = totalAmount;
			var remWalletBalance = 0;

			var fullWallet = (walletBalance >= totalAmount);
			var isAddMoneyAvailable = $('#config')[0].getAttribute('data-addmoney') == 'true' ? true : false;
			var isWalletOnly = $('#config')[0].getAttribute('data-walletonly') == 'true' ? true : false;
			var addMoney = !fullWallet && isAddMoneyAvailable;
			var walletAmountToUse = 0;
			
			if (checkbox.checked) {
					if(isClicked){
						
						location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?use_wallet=1";
						return;
					}
					
					if (fullWallet) {
						paidAmount = 0;
						remWalletBalance = walletBalance - totalAmount
						
						walletAmountToUse = totalAmount;
						$("#onlyWalletAmt").show();
						
						$('.header').hide();
						if(!isWalletOnly){
							$("#paytmCashText").show();	
						}
					} else {
						paidAmount = totalAmount - walletBalance;
						walletAmountToUse = walletBalance; 
					}
					
					if(addMoney){
						$('.header').hide();
						$("#addMoneyText").show();
						$("#addMoneyAmt")[0].innerHTML = paidAmount;
						walletAmountToUse = walletBalance; 
					}
					
					
					$("#yourBal").show();
					// show remaining balance
					
					
					
					$("#walletBalanceAmt")[0].innerHTML = (walletBalance - remWalletBalance);
					$("#walletBalance")[0].style.display = "inline";
					

				} else {					
					if(isClicked) {
						
						// for cod, full wallet case - show sc or dc when paytm cash unchecked
						if((paymentType=='12') && (walletBalance >= totalAmount)) {
							if(savedCardList) {
								location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=SC&use_wallet=0";
							} else {
								location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=DC";
							}
						} else { // normally uncheck the paytm cash
							location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?use_wallet=0";	
						}
						
					}
					
					
					walletAmountToUse = 0;
					// show user balance
					
					
				}
	
				// set hidden field
				var input = document.getElementsByName("walletAmount");
				if(input.length)
					input[0].value = walletAmountToUse;
				if($("#balanceAmt")[0]){
				$("#balanceAmt")[0].innerHTML = paidAmount;
				}
				if($("#finalAmt")[0]){
				$("#finalAmt")[0].innerHTML = paidAmount;
				}

			}
		 
		 function submitForm() {
			 if(paymentType == 5) { 
				setCVV();
			 }
			
			 if(paymentType == 3) {
				 var nbSelect = $("#nbSelect")[0];
				 $("#bankCode")[0].value = nbSelect.options[nbSelect.selectedIndex].value;
				 
			 }
			 
			 if(paymentType == 8) {
				 var atmSelect = $("#atmSelect")[0];
				 $("#atmBankCode")[0].value = atmSelect.options[atmSelect.selectedIndex].value;
				 
			 }
			
			 if(paymentType == 1 || paymentType == 2) {
				 var saveCard = $("#saveCard")[0];
				 saveCard.value = saveCard.checked ? "Y" : "N"; 
			 }
			 
		 }
		 
		 function onPageLoad(){

			 // setup if promocode is applied
			 if($("#promoPaymentModes").length > 0 && $("#promoPaymentModes")[0].getAttribute('data-value'))
			 	setupPromocode();
			 
			 // track page load
			 /*try {
			 	trackEvent("onload");
			 } catch(e){};*/
		 };
		 
		 
		 function trackEvent(e){
			 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
			 
			 if(e == "onload"){
				 ajax.post("/oltp/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
					var status = res;
				 });
			 }
		 }
		 
		 function setupPromocode(){
			 
			 var promoNbStr = $("#promoNbList")[0].getAttribute("data-value");
			 var promoNbList = promoNbStr ?  promoNbStr.split(',') : [];
			 
			 var promoModeStr = $("#promoPaymentModes")[0].getAttribute("data-value");
			 var promoPaymentModes = promoModeStr ?  promoModeStr.split(',') : [];
			 
			// check to show all modes
			var promoShowAllModes = $("#promoShowAllModes")[0].getAttribute("data-value");
			if(promoShowAllModes == "1"){
				//showAllPaymentModes();
				$(".promocode-options-msg-2").removeClass('hide');
			 	$(".promocode-options-msg").addClass('hide');
				return false;
			}
			 
			 // hide all payment mode links and tabs
			 $(".pay-mode-link").hide();
			 $(".pay-mode-tab").addClass('hide');
			 $("#showHideWallet").hide();
			 
			 // show available modes
			 var showSavedCards = false;
			 for(var i=0;i<promoPaymentModes.length;i++){
				 $("#" + promoPaymentModes[i] + "-link").show();
				 $("." + promoPaymentModes[i] + "-tab").show();
				 
				 // show saved cards for cc dc
				 if(promoPaymentModes[i] == "CC" || promoPaymentModes[i] == "DC")
					 showSavedCards = true;
			 }
			 
			 
			// check to show saved cards mode
			// disable non promo save cards
			if(showSavedCards){
				
				var show = true;
				var promoCardList = $("#promoCardList")[0].getAttribute("data-value");// BIN list
				
				if($('.sc-row').length && $('.promo-valid-card').length == 0){
					show = false;
				}
				
				/* if($('.sc-row').length && promoCardList){
						
					 var cards = $('.sc-row');
					 var cardAvailableCount = 0;
					 for(var i=0;i<cards.length;i++){
						var card = cards[i];
						var bin = card.getAttribute("data-firstsixdigits");
						
						if(promoCardList.indexOf(bin) == -1) {
							$(card).addClass("hide");
						} else {
							cardAvailableCount++;
						}
					 }
					 
					 // hide/show sc tab
					 if(cardAvailableCount == 0)
						 show = false; 
				 } */
				
				if(show){
					$('#SC-link').show();
					$('.SC-tab').removeClass("hide");
				}
			} 
			
			
			// disable non promo nb banks
			 if($('#nbSelect').length){
				 
				 var options = $('#nbSelect')[0].options;
				 for(var i=0;i<options.length;i++){
					var option = options[i];
					var bankId = option.value;
					
					if(promoNbList.indexOf(bankId) == -1){
						option.disabled = true;
					}
				 }
			 }
			 
		 };
		 
		 function showAllPaymentModes(){
			 location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?showAll=1";
			 return;
		 };
		 
		 
		 function onCardNumberBlur(mode){			 
			 var number = $("#card-number")[0].value;
			 
			 if(!number || $("#promoType").length == 0)
				 return false;
			 
			 // BIN list
			 var promoCardList = $("#promoCardList")[0].getAttribute("data-value");
			 
			// Category list
			 var promoCardTypeList = $("#promoCardTypeList")[0].getAttribute("data-value");

			 var isCardValid = false;
			
			var cardType = getCardType(number);
			var identifier = cardType + "-" + mode;
			 
			// check if card category-type maches cards in list
			 if(promoCardTypeList.indexOf(identifier) != -1) {
				 isCardValid = true;
			 }
			 
			 // check if card no maches cards in list
			 if(!isCardValid && promoCardList.indexOf(number.substring(0,6)) != -1) {
				 isCardValid = true;
			 } 
			 
			 var $m = $("#wrong-promo-card-msg");
			 var $cn = $("#card-number");

			if(isCardValid) {
				 $m.hide();
				 $cn.removeClass("error");
			} else {
				 $m.show();
				 $cn.addClass("error");
			}
				
		 };
		 
		 function showRewardsForm() {
				$(".rewards-otp-form").hide();
				$(".rewards-card-form").show();
				$("#rewardsAction").val("CARD_INPUT");
				return false;
	     }

		 function getCardType(e){if(e.length==19||e.substring(0,4)=="6220"||e.substring(0,6)=="504834"||e.substring(0,6)=="508159"||e.substring(0,6)=="589458"){return"maestro"}else if(e.substring(0,2)=="30"||e.substring(0,2)=="36"||e.substring(0,2)=="38"||e.substring(0,2)=="35"){return"diners"}else if(e.substring(0,2)=="34"||e.substring(0,2)=="37"){return"amex"}else if(e.substring(0,1)=="4"){return"visa"}else if(e.substring(0,2)=="51"||e.substring(0,2)=="52"||e.substring(0,2)=="53"||e.substring(0,2)=="54"||e.substring(0,2)=="55"){return"master"}else if(e.substring(0,4)=="6011"){return"DISCOVER"}else{return"INVALID CARD"}};
		 
		 function onEMIBankSelect(select){
			 location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value;
		 }
		 
		 function onEMIPlanSelect(checkbox){
			 location.href = "/jsp/wap/paytmAppLow/paymentForm.jsp?txn_Mode=EMI&emi_plan_id=" + checkbox.value;
		 }
		 
		 function doCancel(){
			ajax.send("/theia/cancelTransaction", callback, "GET");
			function callback(html){
				if(html){
					var regex = /<FORM[\s\S]*<\/FORM>/;
					var formStr = regex.exec(html);
					var elm = document.createElement("div");
					elm.innerHTML = formStr[0];
					document.body.appendChild(elm);
					var form = elm.children[0];
					if(window.top !== window){
						form.target="_parent";
					}
					form.submit();
				}
			}
			return false;
		 }
		 
		 function onKeypressCvvNumber(e){
			digit = String.fromCharCode(e.which);
		    if (!/^\d+$/.test(digit)) {
		    	e.preventDefault();
		      return;
		    }
		 }

		 
		 // jquery like custom plugin with few utility functions
		 // includes - id/class selection, hide, show, addClass, removeClass
		 function $(selector){
			 
			// for $(this) - to wrap elem
			 if(typeof(selector) != "string")
				 return extendArray([selector]);
			 
			 if(selector.indexOf("#") == 0){
				var elm = document.getElementById(selector.substr(1))
				var arr = [];
				if(elm)
					arr.push(elm);
			 	return extendArray(arr);
			 }
			 
			 if(selector.indexOf(".") != -1){
				var matchClass = selector.substr(1);
				var arr = [];
			    var elems = document.getElementsByTagName('*'), i;
			    for (var i in elems) {
			        if((' ' + elems[i].className + ' ').indexOf(' ' + matchClass + ' ') > -1) {
			            arr.push(elems[i]);
			        }
			    }
			    return extendArray(arr);
			 }
			 
		 }
		 
		 function extendArray(array){
			 for(var key in Utils){
				 array.constructor.prototype[key] = Utils[key];	 
			 }
			 
			 return array;
		 };
		 
		 var Utils = {
			show : function(){
				for(var i=0; i < this.length;i++){
					this[i].style.display = "block";
				}
				return this;
			},
			hide : function(){
				for(var i=0; i < this.length;i++){
					this[i].style.display = "none";
				}
				return this;
			},
			addClass : function(clas){
				for(var i=0; i < this.length;i++){
					var currClas = this[i].className;
					if(currClas.indexOf(clas) == -1)
						this[i].className = currClas + " " + clas;  
				}
				return this;
			},
			removeClass : function(clas){
				for(var i=0; i < this.length;i++){
					var currClas = this[i].className;
					this[i].className = currClas.replace(clas, "");  
				}
				return this;
			}
		 };
		// jquery like custom plugin
		
		// ajax plugin
			var ajax = {};
			ajax.x = function() {
			    if (typeof XMLHttpRequest !== 'undefined') {
			        return new XMLHttpRequest();  
			    }
			    var versions = [
			        "MSXML2.XmlHttp.5.0",   
			        "MSXML2.XmlHttp.4.0",  
			        "MSXML2.XmlHttp.3.0",   
			        "MSXML2.XmlHttp.2.0",  
			        "Microsoft.XmlHttp"
			    ];
			
			    var xhr;
			    for(var i = 0; i < versions.length; i++) {  
			        try {  
			            xhr = new ActiveXObject(versions[i]);  
			            break;  
			        } catch (e) {
			        }  
			    }
			    return xhr;
			};
			
			ajax.send = function(url, callback, method, data, sync) {
			    var x = ajax.x();
			    x.open(method, url, sync);
			    x.onreadystatechange = function() {
			        if (x.readyState == 4) {
			            callback(x.responseText)
			        }
			    };
			    if (method == 'POST') {
			        x.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
			    }
			    x.send(data)
			};
			
			ajax.post = function(url, data, callback, sync) {
			    var query = [];
			    for (var key in data) {
			        query.push(encodeURIComponent(key) + '=' + encodeURIComponent(data[key]));
			    }
			    ajax.send(url, callback, 'POST', query.join('&'), sync)
			};
		// ajax plugin
		
		</script>
</head>
<%
	if(session.getAttribute("txnTransientId") == null){
		response.sendRedirect("https://secure.paytm.in/oltp-web/oltp-web/error");
	}
%>
<body>
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>
	<div id="txnTransientId" value="${txnInfo.txnID}"></div>

	<c:set var="loginUrl"
		value="${loginInfo.oAuthInfoHost}/oauth2/authorize?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}"></c:set>
	<div id="config"
		data-addmoney="${!empty sessionScope.addMoneyPayModes ? true : false}"
		data-walletonly="${onlyWalletEnabled}"></div>

	<%-- walletonly case : only login page  --%>
	<c:if
		test="${onlyWalletEnabled && !loginInfo.loginFlag}">
		<script>
			window.location = "${loginUrl}";
		</script>
	</c:if>

	<!-- container -->
	<div>

		<!-- header -->
		<%-- <div class="logo">
			<a href="cancelTransaction" class="fl back-btn"><img src="/images/wap/paytmAppLow/back.gif" alt="Back" title="Back" /></a> 
			
			<div class="userview fr">
				<c:if test ="${loginInfo.loginFlag eq 'N' && walletInfo.walletEnabled}">
					<a class="login-btn" href='${loginUrl}'>Login</a>
					<a class="register-btn" href='${loginInfo.oAuthInfoHost}/register?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}'>Signup</a>
				</c:if>
				
				<c:if test ="${loginInfo.loginFlag eq 'Y' && !empty loginInfo.user && walletInfo.walletEnabled}">
					<c:choose>
						<c:when test = "${!empty loginInfo.user.userName}">
							<c:set var = "displayName" value="${loginInfo.user.userName}"/>
						</c:when>
						<c:when test = "${!empty loginInfo.user.emailId}">
							<c:set var = "displayName" value="${loginInfo.user.emailId}"/>
						</c:when>
						<c:when test = "${!empty loginInfo.user.mobileNumber}">
							<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
						</c:when>
					</c:choose>
					<c:out value="${displayName}" escapeXml="true" />
				</c:if>
			</div>
			
			<div id="logo-img">
				<img src="/images/wap/paytmAppLow/header-logo.gif" alt="Paytm Payments" title="Paytm Payments" />
			</div>
			
		</div> --%>
		<!-- header -->

		<!-- notifications -->
		<div>
			<c:if test="${ empty showLoginNotification}">
				<c:set var="showLoginNotification"
					value="${walletInfo.walletBalance_INFO eq 'Y' && empty loginInfo.user}"
					scope="session"></c:set>
			</c:if>
			<c:if test="${showLoginNotification eq true}">
				<c:set var="showLoginNotification" value="false" scope="session"></c:set>
				<div class="failure">Login with Paytm to use your Paytm Wallet and saved cards.</div>
			</c:if>

			<c:if test="${txnInfo.retry}">
				<div class="failure" id="retryMsg">${txnInfo.displayMsg}</div>
			</c:if>
			<c:if test="${walletInfo.walletFailed}">
				<div class="failure">
					<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					<c:if test="${onlyWalletEnabled}">
						<a href="cancelTransaction" class="cancel"
							onclick="return doCancel()">Cancel</a>
					</c:if>
				</div>
			</c:if>
			<c:if test="${walletInfo.walletFailed}">
				<div class="failure">
					<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
				</div>
			</c:if>
			<c:if
				test="${onlyWalletEnabled && empty sessionScope.addMoneyPayModes && walletInfo.walletBalance < txnInfo.txnAmount}">
				<div class="failure">
					You have insufficient balance for this transaction. <a
						href="cancelTransaction" class="cancel"
						onclick="return doCancel()">Cancel</a>
				</div>
			</c:if>
		</div>
		<!-- notifications -->


		<%-- <div class="header" id = "otherText">
			Select an option to pay <b>Rs <span id = "finalAmt">${txnInfo.txnAmount}</span></b>
		</div> --%>

		<div class="header row" id = "paytmCashText" style = "display:none">
			Uncheck Paytm Wallet to pay using other options.
		</div>
		<div class="header row" id = "addMoneyText" style = "display:none">
			To complete payment, add to Paytm Wallet <b>Rs <span id = "addMoneyAmt">${txnInfo.txnAmount}</span></b>
		</div>
		
		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>	
		<div class="row pt20">
			<div class="fl">Total amount to be paid</div>
			<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></div>
			<div class="clear"></div>
		</div>

		<%-- paytm cash mode logic --%>

		<%-- first time setting --%>
		<c:if test="${ loginInfo.loginFlag && empty usePaytmCash}">
			<c:set var="usePaytmCash"
				value="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}"
				scope="session"></c:set>


			<%-- for zero bal add money case : usePaytmCash is true --%>
			<c:if
				test="${!empty walletInfo.walletBalance && walletInfo.walletBalance eq 0 && !empty sessionScope.addMoneyPayModes}">
				<c:set var="usePaytmCash" value="true" scope="session"></c:set>
			</c:if>

			<%-- for promocode case : usePaytmCash is false initially --%>
			<c:if test="${!empty sessionScope.promoCode}">
				<c:set var="usePaytmCash" value="false" scope="session"></c:set>
			</c:if>

		</c:if>

		<c:if test="${param.use_wallet eq 1}">
			<c:set var="usePaytmCash" value="true" scope="session"></c:set>
		</c:if>
		<c:if test="${param.use_wallet eq 0}">
			<c:set var="usePaytmCash" value="false" scope="session"></c:set>
		</c:if>

		<%-- paytm cash logic --%>

		<%-- promocode logic --%>
		<c:if test="${empty promoShowAllModes}">
			<c:set var="promoShowAllModes" value="0" scope="session"></c:set>
		</c:if>
		<c:if test="${param.showAll eq 1}">
			<c:set var="promoShowAllModes" value="1" scope="session"></c:set>
		</c:if>
		<%-- promocode logic --%>


		<%-- payment mode logic --%>
		<c:set var="isInsufficientBalance"
			value="${walletInfo.walletBalance < txnInfo.txnAmount}" />
		<c:set var="isHybridAllowed"
			value="${txnConfig.hybridAllowed}"></c:set>

		<c:set var="isHybrid" value="false" />
		<c:if
			test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
			<c:set var="isHybrid"
				value="${walletInfo.walletBalance < txnInfo.txnAmount}" />
		</c:if>

		<c:set var="selectedModeType" value="BANK" />

		<c:if test="${usePaytmCash && !isHybrid}">
			<c:set var="selectedModeType" value="WALLET_ONLY" />
		</c:if>

		<c:if test="${onlyWalletEnabled}">
			<c:set var="selectedModeType" value="WALLET_ONLY" />
		</c:if>

		<c:if test="${usePaytmCash && !empty sessionScope.addMoneyPayModes}">
			<c:set var="selectedModeType" value="ADD_MONEY" />
		</c:if>

		<c:if test="${!empty ppi}">
			<c:set var="paymentType" value="0" />
		</c:if>

		<%-- payment mode logic --%>

		<%-- ppi : ${ppi} |
		isHybrid : ${isHybrid} | 
		selectedModeType : ${selectedModeType} | 
		paymentType : ${paymentType} | 
		bal : ${walletInfo.walletBalance} | 
		usePaytmCash : ${usePaytmCash} | 
		onlyWalletEnabled : ${onlyWalletEnabled} |
		isInsufficientBalance : ${isInsufficientBalance}
		isHybridAllowed : ${isHybridAllowed} --%>

		<!-- paytm cash -->
		<%-- <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}"> --%>
		<c:if
			test="${!empty walletInfo.walletBalance && !empty usePaytmCash}">

			<c:set var="checkPaytmCashCheckbox" value="false" />
			<%-- <c:if test="${usePaytmCash && walletInfo.walletBalance > 0}"> --%>
			<c:if test="${usePaytmCash}">
				<c:set var="checkPaytmCashCheckbox" value="true" />
			</c:if>

			<c:set var="disablePaytmCashCheckbox" value="false" />

			<%-- when hybrid not allowed --%>
			<c:if
				test="${isInsufficientBalance && empty sessionScope.addMoneyPayModes && (!isHybridAllowed or walletInfo.walletBalance <= 0)}">
				<c:set var="disablePaytmCashCheckbox" value="true" />
				<c:set var="checkPaytmCashCheckbox" value="false" />
			</c:if>

			<%-- when wallet only --%>
			<c:if test="${onlyWalletEnabled}">
				<c:set var="disablePaytmCashCheckbox" value="true" />
			</c:if>
			<c:if test="${walletInfo.walletBalance > 0}">

			<div id="showHideWallet">	
				<div class="row mb5 rel_pos">
					<div class="fl">
						<input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> />
						<span>Use Paytm Wallet</span>
						<div class="clear"></div>
						<!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm Wallet)</div> -->
						<div class="bal" id = "yourBal" >(Your current balance is  <span class="WebRupee">Rs</span> <span class = 'amt' id = "yourBalSpan">${walletInfo.walletBalance}</span>)</div>
					    <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
					</div>
					<div class="fr abs_pos" id = "walletBalance">
						 <span class="WebRupee">Rs</span>
						 <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="0" /></span>
					</div>

					<%-- <div class="row mt5" style="display:none">
					<div class="fl">
						<b>Balance amount to be paid</b>
					</div>
					<div class="fr" >
						 <b class="WebRupee">Rs</b>
						<b id = "balanceAmt">${txnInfo.txnAmount}</b>
					</div>
					<div class="clear"></div>
				</div>  --%>
					<c:if
						test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance >= txnInfo.txnAmount}">
						<form autocomplete="off" method="post"
							action="/payment/request/submit" id="walletForm"
							style="margin: 0; padding: 0">
							<input type="hidden" name="txnMode" value="PPI" />
							<input type="hidden" name="channelId" value="WAP" />
							<input type="hidden" name="AUTH_MODE" value="USRPWD" />
							<input type="hidden" name="walletAmount" id="walletAmount"
								value="0" />
							<div style="display: none;" class="fullWalletDeduct"
								id="onlyWalletAmt">
								<input class="blue-btn" type="submit" value="Confirm" name="">
							</div>
						</form>
					</c:if>
				</div>
			</c:if>
		</c:if>
		<div class="header" id="otherText">
			<c:if test="${usePaytmCash}">
				<c:if test="${txnInfo.txnAmount > walletInfo.walletBalance}">
					<span id="balance">Select an option to pay balance</span>
				</c:if>
			</c:if>
			<c:if test="${!usePaytmCash}">

				<span id="balance">Select an option to pay</span>

			</c:if>
			<b class="fr">Rs <span id="balanceAmt">${txnInfo.txnAmount}</span></b>
		</div>
		<!-- paytm cash -->


		<%-- promocode logic --%>
		<c:if test="${!empty sessionScope.promoCode}">

			<div id="promoType" data-value="${sessionScope.promocodeType}"></div>
			<div id="promoNbList" data-value="${sessionScope.promoNbList }"></div>
			<div id="promoCardList" data-value="${sessionScope.promoBinList }"></div>
			<div id="promoPaymentModes"
				data-value="${sessionScope.promoPaymentModes }"></div>
			<div id="promoCardTypeList"
				data-value="${sessionScope.promoCardType}"></div>
			<div id="promoShowAllModes" data-value="${promoShowAllModes}"></div>

			<div class="failure promocode-options-msg">
				<p>${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}</p>
			</div>

			<div class="failure promocode-options-msg-2 hide">
				<p>If you pay using other option you will not get benefits of
					your promocode.</p>
				<br>
					<p>${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}</p>
			</div>
			<c:if test="${sessionScope.promocodeType eq 'CASHBACK'}">
				<c:if test="${promoShowAllModes eq 0}">
					<a href="javascript:void()" id="show-other-options"
						onclick="showAllPaymentModes();">Show other options to pay</a>
				</c:if>
			</c:if>
		</c:if>
		<%-- promocode logic --%>


		<%-- show other modes link for wallet only mode --%>
		<c:if test="${selectedModeType eq 'WALLET_ONLY'}">
			<c:set var='paymentType' value="7"></c:set>
			<c:set var='selectedModeType' value="BANK"></c:set>
		</c:if>

		<!-- Merchant Payment Modes -->
		<c:set var="paymentConfig" value="${sessionScope}" />
		<c:if test="${selectedModeType eq 'BANK'}">
			<div id="merchant-payment-modes" class="cards-tabs relative">
				<%@ include file="merchantPaymentModes.jsp"%>
			</div>
		</c:if>
		<!-- Merchant Payment Modes -->

		<!-- Add Money Payment Modes -->
		<c:if test="${selectedModeType eq 'ADD_MONEY'}">
			<c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
			<c:set var="isAddMoneyAvailable" value="" />
			<div id="add-money-payment-modes" class="cards-tabs">
				<%@ include file="addMoneyPaymentModes.jsp"%>
			</div>
		</c:if>
		<!-- Add Money Payment Modes -->

	</div>
	<!-- container -->

	<div class="footer">
		<div id="partner-logos">
			<img src="/images/wap/paytmAppLow/partners.gif"
				alt="Norton Visa Mastercard PCI DSS"
				title="Norton Visa Mastercard PCI DSS" />
		</div>
	</div>
	<script type="text/javascript">
		onPageLoad();
		processPaytmCash();
	</script>
</body>
</html>