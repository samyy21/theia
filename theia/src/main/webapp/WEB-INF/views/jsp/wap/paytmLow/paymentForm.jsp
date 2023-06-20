<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ include file="../../common/config.jsp" %>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta name="robots" content="noindex,nofollow" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		<link type="image/x-icon" rel="shortcut icon" href="images/web/Paytm.ico" />
		<base href="/oltp-web/" />	
		<%-- <c:set var="theme" value="paytmLow" scope="session"></c:set> --%>
		<link rel="stylesheet" type="text/css" href="/css/wap/${sessionScope.theme}/mobile.css" />
		<script type = "text/javascript">
		 var paymentType = "${paymentType}";
		 function processPaytmCash(obj) {
			 var isClicked = true;
			 if(!obj) {
				 obj = document.getElementById("paytmCashCB");
				 isClicked = false;
			 }
			if(obj == null) return;
			var totalAmount = Number(document.getElementById("totalAmtVal").value);
			var walletBalance = Number(document.getElementById("totalWalletVal").value);
			var paidAmount = totalAmount;
			var remWalletBalance = 0;
			var onlyWallet = (walletBalance >= totalAmount);

			if (obj.checked) {
					if (onlyWallet) {
						if(isClicked) {
						location.href = "/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=PPI&showAll=1&use_wallet=1";
							return;
						}
						paidAmount = 0;
						remWalletBalance = walletBalance - totalAmount
						document.getElementsByName("walletAmount")[0].value = totalAmount;
						document.getElementById("paytmCashText").style.display = "block";
						if(document.getElementById("otherText")){
						document.getElementById("otherText").style.display = "none";
						}
						document.getElementById("onlyWalletAmt").style.display = "block";
					} else {
						paidAmount = totalAmount - walletBalance;
						document.getElementsByName("walletAmount")[0].value = walletBalance;
					}
					//document.getElementById("yourBal").style.display = "none";
					//document.getElementById("remBalSpan").innerHTML = walletBalance;
					//document.getElementById("remBal").style.display = "block";
					document.getElementById("yourBalSpan").innerHTML = walletBalance;
					document.getElementById("walletBalanceAmt").innerHTML = (walletBalance - remWalletBalance);
					document.getElementById("walletBalance").style.display = "block";
					document.getElementById("balance").style.display="block";
					document.getElementById("notBalance").style.display="none";
					
					 // On Check CVV Focus
					 if(document.getElementById("cvv_2_input")){
						 document.getElementById("cvv_2_input").focus();
					 }
					

				} else {
					if(onlyWallet && isClicked) {
						location.href = "/jsp/wap/paytmLow/paymentForm.jsp?showAll=1&use_wallet=0";
					}
					paidAmount = totalAmount;
					if(document.getElementById("otherText")){
					document.getElementById("otherText").style.display = "block";
					}
					document.getElementById("paytmCashText").style.display = "none";
					//document.getElementsByName("walletAmount")[0].value = '0';
					//document.getElementById("remBal").style.display = "none";
					document.getElementById("yourBalSpan").innerHTML = walletBalance;
					//document.getElementById("yourBal").style.display = "block";
					document.getElementById("walletBalance").style.display = "none";
					document.getElementById("balance").style.display="none";
					document.getElementById("notBalance").style.display="block";
					
					// On Check CVV Focus
					 if(document.getElementById("cvv_2_input")){
						 document.getElementById("cvv_2_input").focus();
					 }
				}
				if(document.getElementById("balanceAmt")){
				document.getElementById("balanceAmt").innerHTML = paidAmount;
				}
				if(document.getElementById("finalAmt")){
				document.getElementById("finalAmt").innerHTML = paidAmount;
				}

			}
		 
		 function submitForm() {
			 if(paymentType == 5) { 
				setCVV();
			 }
			
			 if(paymentType == 3) {
				 var nbSelect = document.getElementById("nbSelect");
				 document.getElementById("bankCode").value = nbSelect.options[nbSelect.selectedIndex].value;
				 
			 }
			 
			 if(paymentType == 8) {
				 var atmSelect = document.getElementById("atmSelect");
				 document.getElementById("atmBankCode").value = atmSelect.options[atmSelect.selectedIndex].value;
				 
			 }
			
			 if(paymentType == 1 || paymentType == 2) {
				 var saveCard = document.getElementById("saveCard");
				 if(saveCard.checked) {
					 saveCard.value = "Y";
				 } else {
					 saveCard.value = "N";
				 }
				 
			 }
			 
		 }
		 
		 function onPageLoad(){
			 // On Load CVV Focus
			 if(document.getElementById("cvv_2_input")){
				 document.getElementById("cvv_2_input").focus();
			 }
			 // setup if promocode is applied
			 if($("#promoPaymentModes").length > 0 && $("#promoPaymentModes")[0].getAttribute('data-value'))
			 	setupPromocode();
			 
			 // track page load
			 /*try {
			 	trackEvent("onload");
			 } catch(e){}*/
		 };
		 
		 
		 function trackEvent(e){
			 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
			 
			 if(e == "onload"){
				 ajax.post("/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
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
				showAllPaymentModes();
				return false;
			}
			 
			 // hide all payment mode links and tabs
			 $(".pay-mode-link").hide();
			 $(".pay-mode-tab").addClass('hide');
			 
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
			 $(".pay-mode-link").show();
			 $(".pay-mode-tab").show();
			 $("#show-other-options").hide();
			 $(".promocode-options-msg-2").removeClass('hide');
			 $(".promocode-options-msg").addClass('hide');
			 $('.sc-tab').removeClass("hide");
			 $('.sc-row').removeClass("hide");
			 $("#showHideWallet").show();
			 
			 // enable all nb banks
			 if($('#nbSelect').length){
				 
				 var options = $('#nbSelect')[0].options;
				 for(var i=0;i<options.length;i++){
					var option = options[i];
					option.disabled = false;
				 }
			 }
			 for(var i=0; i<$(".pay-mode-link").length;i++) {
				 $(".pay-mode-link")[i].href +="&showAll=1"
			 }
			 return false;
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
		 
		 
		 function getCardType(e){if(e.length==19||e.substring(0,4)=="6220"||e.substring(0,6)=="504834"||e.substring(0,6)=="508159"||e.substring(0,6)=="589458"){return"maestro"}else if(e.substring(0,2)=="30"||e.substring(0,2)=="36"||e.substring(0,2)=="38"||e.substring(0,2)=="35"){return"diners"}else if(e.substring(0,2)=="34"||e.substring(0,2)=="37"){return"amex"}else if(e.substring(0,1)=="4"){return"visa"}else if(e.substring(0,2)=="51"||e.substring(0,2)=="52"||e.substring(0,2)=="53"||e.substring(0,2)=="54"||e.substring(0,2)=="55"){return"master"}else if(e.substring(0,4)=="6011"){return"DISCOVER"}else{return"INVALID CARD"}};
		 
		 function showRewardsForm() {
				$(".rewards-otp-form").hide();
				$(".rewards-card-form").show();
				$("#rewardsAction").val("CARD_INPUT");
				return false;
		}
		 
		 // jquery like custom plugin with few utility functions
		 // includes - id/class selection, hide, show
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
<body onload="onPageLoad()">

<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>
	<div id="txnTransientId" value="${txnInfo.txnID}"></div>
	
	<div>

		<div class="logo">
			<a href="cancelTransaction" class="fl back-btn"><img src="/images/wap/${sessionScope.theme}/back.gif" alt="Back" title="Back" /></a> 
			<div id="logo-img">
				<img src="/images/wap/${sessionScope.theme}/header-logo.gif" alt="Paytm Payments" title="Paytm Payments" />
			</div>
		</div>

		
		<div class="header" id = "paytmCashText" style = "display:none">
			Uncheck Paytm Wallet to pay using other options.
		</div>
		
		<c:if test="${!empty param.showAll}">
			<c:set var="promoShowAllModes" value = "1"/>
		</c:if>

		<c:if test="${txnInfo.retry}">
			<div class="failure" id="retryMsg">${txnInfo.displayMsg}</div>
		</c:if>
		<c:if test="${sessionScope.walletFailed}">
			<div class="failure">
				<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
			</div>
		</c:if>
		<c:if test="${walletInfo.walletInactive}">
			<div class="wht-box1 mt10">
				<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
			</div>
		</c:if>
		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>	
		<div class="row pt20">
			<div class="fl">Total amount to be paid</div>
			<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></div>
			<div class="clear"></div>
		</div>
		
		<%-- first time setting --%>
		<c:if test="${ empty usePaytmCash}">
			<c:set var="usePaytmCash" value="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}" scope="session"></c:set>
		</c:if>
		<c:if test="${param.use_wallet eq 1}">
			<c:set var="usePaytmCash" value="true" scope="session"></c:set>
		</c:if>
		<c:if test="${param.use_wallet eq 0}">
			<c:set var="usePaytmCash" value="false" scope="session"></c:set>
		</c:if>
		<c:if test="${requestScope.applyWallet eq false}">
			<c:set var="usePaytmCash" value="false" scope="session"></c:set>
		</c:if>
		
		<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
		<c:set var = "isHybrid" value = "${walletInfo.walletBalance < txnInfo.txnAmount}"/>
		<c:if test="${empty sessionScope.promoCode || !empty promoShowAllModes}">
		<c:if test="${param.txn_Mode eq 'PPI' || (!isHybrid and empty param.showAll and empty param.txn_Mode)}">
			<c:set var = "ppi" value = "1"/>
		</c:if>
		</c:if>
		<div id="showHideWallet" <c:if test="${!empty sessionScope.promoCode and empty promoShowAllModes}">style='display:none'</c:if>>	
		<div class="row mb5">
			<div class="fl">
				<input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${(!empty ppi or isHybrid) and usePaytmCash}">checked = "checked"</c:if> /> 
				<span>Use Paytm Wallet</span>
				<div class="clear"></div>
				<!-- <div class="bal" id = "remBal">(Remaining balance <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span>)</div> -->
				<div class="bal" id = "yourBal">(Your current balance is <span class="WebRupee">Rs</span> <span class = 'amt' id = "yourBalSpan"></span>)</div>
			    <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
			</div>
			<div class="fr" id = "walletBalance">
				 - <span class="WebRupee">Rs</span>
				 <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="0" /></span>
			</div>
			<div class="clear"></div>
		</div>

		<div class="row mt5 header">
			<div class="fl" id="notBalance">
				Select an option to pay 
			</div>
			<div class="fl" id="balance">
				Select an option to pay balance
			</div>
			<div class="fr" >
				 <span class="WebRupee">Rs</span>
				<span id = "balanceAmt">${txnInfo.txnAmount}</span>
			</div>
			<div class="clear"></div>
		</div>
		 <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance >= txnInfo.txnAmount}">
		 	 <form autocomplete="off" method="post" action="/submitTransaction" id="walletForm" style = "margin:0;padding:0">
		    	<input type="hidden"  name="txnMode" value="PPI" />
				<input type="hidden"  name="channelId" value="WAP" />
				<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
				<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
			    <div style="display: none;" class="fullWalletDeduct" id = "onlyWalletAmt">
					<input class="blue-btn" type="submit" value="Confirm" name="">
			    </div>
			</form>
		 </c:if>
		 </div>
		</c:if>
	   
		 <c:if test="${!empty sessionScope.promoCode}">
		 
		 		<div id="promoType" data-value="${sessionScope.promocodeType}"></div>
		 		<div id="promoNbList" data-value="${sessionScope.promoNbList }"></div>
		 		<div id="promoCardList" data-value="${sessionScope.promoBinList }"></div>
		 		<div id="promoPaymentModes" data-value="${sessionScope.promoPaymentModes }"></div>
		 		<div id="promoCardTypeList" data-value="${sessionScope.promoCardType}"></div>
		 		<div id="promoShowAllModes" data-value="${promoShowAllModes}"></div>
		 		
				<div class="failure promocode-options-msg">
					<p>
						${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
					</p>
				</div>
				
				<div class="failure promocode-options-msg-2 hide">
					<p>
						If you pay using other option you will not get benefits of your promocode.
					</p>
					<br>
					<p>
						${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
					</p>
													
				</div>
				<c:if test="${sessionScope.promocodeType eq 'CASHBACK'}">
					<c:if test="${empty promoShowAllModes}">
						<a href = "javascript:void()" id="show-other-options" onclick="showAllPaymentModes();">Show other options to pay</a> 
					</c:if>	
				</c:if>
					
				
		</c:if>
		<c:if test="${empty ppi || isHybrid || (!empty sessionScope.promoCode and empty promoShowAllModes)}">
			<c:if test="${saveCardEnabled}">
				<div class="pay-mode-tab SC-tab">
					<%@ include file="sc.jsp"%>
				</div>
			</c:if>
			<c:if test="${dcEnabled}">
				<div class="pay-mode-tab DC-tab">
					<%@ include file="dc.jsp"%>
				</div>
			</c:if>
			<c:if test="${ccEnabled}">
				<div class="pay-mode-tab CC-tab">
					<%@ include file="cc.jsp"%>
				</div>
			</c:if>
			<c:if test="${netBankingEnabled}">
				<div class="pay-mode-tab NB-tab">
					<%@ include file="nb.jsp"%>
				</div>
			</c:if>
			<c:if test="${atmEnabled}">
				<div class="pay-mode-tab ATM-tab">
					<%@ include file="atm.jsp"%>
				</div>
			</c:if>
			<c:if test="${impsEnabled}">
				<div class="pay-mode-tab IMPS-tab">
					<%@ include file="imps.jsp"%>
				</div>
			</c:if>
			<c:if test="${cashcardEnabled}">
				<div class="pay-mode-tab CASHCARD-tab">
					<%@ include file="itzcash.jsp"%>
				</div>
			</c:if>
			<c:if test="${rewardsEnabled}">
				<div class="pay-mode-tab REWARDS-tab">
					<%@ include file="rewards.jsp"%>
				</div>
			</c:if>
		</c:if>
	</div>
	<div class="heading non-promocode-options">Other payment modes</div>
		<c:if test="${(5 ne paymentType || !empty ppi) && saveCardEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=SC" id="SC-link" class="pay-mode-link">
		<span class="bottom-link">
			Saved Card
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a> 
		</c:if>
		
		<c:if test="${(2 ne paymentType || !empty ppi) && dcEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=DC" id="DC-link" class="pay-mode-link">
		<span class="bottom-link">
			Debit Card
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a> 
		</c:if>
		
		<c:if test="${(1 ne paymentType || !empty ppi) && ccEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=CC" id="CC-link" class="pay-mode-link">
		<span class="bottom-link">
			Credit Card
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a> 
		</c:if>
		
		<c:if test="${(3 ne paymentType || !empty ppi) && netBankingEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=NB" id="NB-link" class="pay-mode-link">
		<span class="bottom-link">
			Net Banking 
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a>
		</c:if>
		
		<c:if test="${(8 ne paymentType || !empty ppi) &&  atmEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=ATM" id="ATM-link" class="pay-mode-link">
		<span class="bottom-link">
			ATM Card
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a>
		</c:if>

		<c:if test="${(6 ne paymentType ||  !empty ppi) &&  impsEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=IMPS" id="IMPS-link" class="pay-mode-link">
		<span class="bottom-link">
			IMPS
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a>
		</c:if>


		<c:if test="${(10 ne paymentType || !empty ppi) &&  cashcardEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=CASHCARD" id="CASHCARD-link" class="pay-mode-link">
		<span class="bottom-link-last">
			Cash Card
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a>
		</c:if>
		
		<c:if test="${(11 ne paymentType || !empty wallet) && rewardsEnabled}">
		<a href="/jsp/wap/paytmLow/paymentForm.jsp?txn_Mode=REWARDS">
		<span class="bottom-link-last">
			Rewards
			<img src="/images/wap/paytmLow/arrow.gif" align="right" alt="" title="" />
		</span>
		</a>
		</c:if>

		<div class="footer">
			<div id="partner-logos">
				<img src="/images/wap/paytmLow/norton.png" alt="Norton" title="Norton" /> 
				<img src="/images/wap/paytmLow/verified.png" alt="" title="" />
				<img src="/images/wap/paytmLow/mastercard.png" alt="" title="" />
				<img src="/images/wap/paytmLow/PCI.png" alt="PCI DSS" title="PCI DSS" />
			</div>
		</div>
		<script type = "text/javascript">
			processPaytmCash();
		</script>
</body>
</html>