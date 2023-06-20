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
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
		<base href="/" />	
		<%String useMinifiedAssets = ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="/theia/resources/css/wap/merchantLow3/mobile.css" />
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow3/mobile.min.css" />
		<% } %>
		
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
					<% if(useMinifiedAssets.equals("N")){ %>
						<link rel="stylesheet" type="text/css" href="/theia/resources/css/wap/merchantLow3/airtel.css" />
					<% } else { %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow3/airtel.min.css" />
					<% } %>
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp' || themeInfo.subTheme eq 'paytmLow'|| themeInfo.subTheme eq 'paytmAppSSL'|| themeInfo.subTheme eq 'paytmLowSSL'}">
		      		<style>
		      			.deleteCard { display : block;
   				margin-top: 10px;
				float: right; 		
				background: url('/theia/resources/images/wap/merchantLow3/ic-delete-card.png') no-repeat;	
				width: 12px;
				height: 13px;
				text-decoration: none;
				}
		      		</style>
		      </c:when>
		      <c:otherwise>
		      </c:otherwise>
		</c:choose>
	
		<c:if test="${themeInfo.subTheme eq 'videocon' }">
			<style>
				.blue-btn {
					background-color: #562D5E;
				}
			</style>
		</c:if>
		<c:if test="${themeInfo.subTheme  eq 'mts'}">
			<style>
				#dcCardSaved, #ccCardSaved
				{display:none;}
			</style>
		</c:if>
		<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
		<script type = "text/javascript">
		 var paymentType = "${paymentType}";
		 var savedCardList = "${!empty savedCardList}";
		 var walletBalance="${walletInfo.walletBalance}";
		 var cancelUrl = "/theia/cancelTransaction";
		 var paymentFormUrl = "/theia/jsp/wap/merchantLow3/paymentForm.jsp";


		 function processPaytmCC(checkbox){
			 //console.log('inside process paytmcc');
			 //change redirection
			 var paytmWallet=$("#paytmCashCB")[0];
			 if(checkbox.checked)
				 location.href = paymentFormUrl + "?use_paytmcc=1" + "&${queryStringForSession}";
			 else
				 location.href = paymentFormUrl + "?use_paytmcc=0" + "&${queryStringForSession}";

			 // make paytm cashbox checked to unchecked


			 // make payment mode such that no other payment modes are opened
		 }

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
						location.href = paymentFormUrl + "?use_wallet=1" + "&${queryStringForSession}";
						return;
					}
					
					
					if (fullWallet) {
						paidAmount = 0;
						remWalletBalance = walletBalance - totalAmount;
						remWalletBalance = Math.round(remWalletBalance * 100) / 100;
						
						walletAmountToUse = totalAmount;
						$("#onlyWalletAmt").show();
						
						$('.header').hide();
						if(!isWalletOnly){
							$("#paytmCashText").show();	
						}
					} else {
						paidAmount = totalAmount - walletBalance;
						paidAmount = Math.round(paidAmount * 100) / 100;
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
					
					
					var amt = (walletBalance - remWalletBalance);
					amt = Math.round(amt * 100) / 100;
					$("#walletBalanceAmt")[0].innerHTML = amt;
					$("#walletBalance")[0].style.display = "inline";
					

				} else {
					var input = document.getElementsByName("addMoney");
					if(input.length)
						input[0].value = 0;
					
					
					if(isClicked) {
						location.href = paymentFormUrl + "?use_wallet=0" + "&${queryStringForSession}";
						
					}
					
					
					walletAmountToUse = 0;
					// show user balance
					
					
				}
	
				// set hidden field
				var input = document.getElementsByName("walletAmount");
				if(input.length)
					input[0].value = walletAmountToUse;
				
				$("#balanceAmt")[0].innerHTML = paidAmount;
				//$("#finalAmt")[0].innerHTML = paidAmount;

			}
		 
		 var formSubmitted = false;
		 
		 function submitForm() {
			 
			 if(paymentType == 5) { 
				if($("#cvvNum").length)
					setCVV();
			 }
			
			 if(paymentType == 3) {
				 var nbSelect = $("#nbSelect")[0];
				 if(nbSelect && $("#bankCode").length)
				 	$("#bankCode")[0].value = nbSelect.options[nbSelect.selectedIndex].value;
				 
			 }
			 
			 if(paymentType == 8) {
				 var atmSelect = $("#atmSelect")[0];
				 if(atmSelect)
				 	$("#atmBankCode")[0].value = atmSelect.options[atmSelect.selectedIndex].value;
				 
			 }
			
			 if(paymentType == 1 || paymentType == 2) {
				 var saveCard = $("#saveCard")[0];
				 if(saveCard)
				 	saveCard.value = saveCard.checked ? "Y" : "N"; 
			 }
			 
			 if($("#card-number")){
				 $("#card-number")[0].value = removeSpaceFromSentence($("#card-number")[0].value);
			 }
			 
			 if(formSubmitted)
				 return false;
			 
			 var btns = $('.blue-btn');
			 for(var i=0; i< btns.length;i++){
				 btns[i].value="Please wait...";
			 }

			 formSubmitted = true;
			 
			 setTimeout(function(){
				 formSubmitted = false;
				 for(var i=0; i< btns.length;i++){
					 btns[i].value="Proceed Securely";
				 }
			 }, 60000);
			 
			 return true;
		 }
		 
		 function onPageLoad(){
			 
			//Visbility Other Payment Mode
			var modeLinksLen = $("#othermodesLinks");
			if(modeLinksLen[0])
				var modeLinksLen = modeLinksLen[0].getElementsByTagName("a");
			
			if(modeLinksLen.length<=1){
				$("#otherpaymentModeHeading").hide();
			}

			 // setup if promocode is applied
			 if($("#promoPaymentModes").length > 0 && $("#promoPaymentModes")[0].getAttribute('data-value'))
			 	setupPromocode();
			 
			 // track page load
			 try {
			 	trackEvent("onload");
			 } catch(e){};
			 
			 checkCookieSupport();
		 };
		 
		 
		 function checkCookieSupport(){
			 	document.cookie = "testcookie=true";
				
				if(!/testcookie/.test(document.cookie)){
					
					var JSESSIONID = "<%=request.getSession().getId() %>";
					var forms = document.getElementsByTagName('form');
					var action = forms[0].action; 
					action = action.split("?");
					var submitUrl = action[0] + ";jsessionid=" + JSESSIONID;
					if(action.length > 1) {
						submitUrl = submitUrl + "?" + action[1];
					}
					for(var i=0; i < forms.length; i++){
						forms[i].action = submitUrl;
					}
					
					var otherModeLinks = document.getElementsByClassName('pay-mode-link');
					for(var i=0; i < otherModeLinks.length; i++){
						var split = otherModeLinks[i].href.split("?");
						var newHref = split[0] + ";jsessionid=" + JSESSIONID;
						if(split.length > 1){
							newHref += "?" + split[1];
						}
						otherModeLinks[i].href = newHref;
					}
					paymentFormUrl = paymentFormUrl + ";jsessionid=" + JSESSIONID + "?${queryStringForSession}";
					cancelUrl = cancelUrl + ";jsessionid=" + JSESSIONID + "?${queryStringForSession}";
				}
		 }
		 
		 
		 function trackEvent(e){
// 			 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
			 
// 			 if(e == "onload"){
// 				 ajax.post("/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
// 					var status = res;
// 				 });
// 			 }
		 }
		 
		 function selectBoxColorChange(obj){
			 if(obj.selectedIndex!=0){
				 obj.style.color = "#222";
	    	} else {
			  obj.style.color = "#999";
			}
		 }

         function deleteCard(savedCardId) {
             var mid = document.getElementById("sucess-rate").getAttribute("data-mid").toString();
             var orderId = document.getElementById("sucess-rate").getAttribute("data-oid").toString();
             var ev = event;
             var csrf =$('#csrf-token')[0].getAttribute('data-token');
             // insert code for delete card
             ajax.sendcsrf("/theia/DeleteCardDetails", {
                     "savedCardId": savedCardId,
                     "MID": mid,
                     "ORDER_ID": orderId
                 },
                 csrf,
                 function(data, status) {
                     //perform with data and status
                     if ('success' == data) {
                         // Delete existing card
                         ev.target.parentElement.parentElement.style.display = "none";
                         ev.target.parentElement.parentElement.nextElementSibling.style.display = "none";
                         var savedCardRows = document.getElementsByClassName('row-dot sc-row');
                         var flagFound = false;
                         if(savedCardRows && savedCardRows.length>0){
                             for(var i = 0; i< savedCardRows.length; i++){
                                 if(savedCardRows[i].style.display != "none"){
                                     var queryRadioButton = savedCardRows[i].querySelector('input[type="radio"]');
                                     if(queryRadioButton)
                                         queryRadioButton.click();
                                     flagFound = true;
                                     break;
                                 }
                             }
                         }
                         if(!flagFound) {
                             document.getElementById('no-saved-card-left').style.display = "block";
                         }
                     }
                 });
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
			 location.href = "/theia/jsp/wap/merchantLow3/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
			 return;
		 };
		 
		 function removeSpaceFromSentence(sentence){
			 return sentence.split('').filter(function(val){return val!=' '}).join('');
		 }

		 function checkIfFourDigitReached(word){
			 
		 }
		 
		 function pressCardSpace(ev){
			 //console.log('pressed card',ev);
			 //console.log('key pressed & value',ev.which, ev.target.value);
			 var value = ev.target.value.toString();
			 var maxLen = parseInt(ev.target.maxLength);
			 
			 var digit = String.fromCharCode(ev.which);
			 if(ev.key !== undefined){
				 digit = ev.key;
			 }
				 
			 //any other character than digit
			 if (!/^\d+$/.test(digit)) {
			    	if(ev.which !== 8){
			    		ev.preventDefault();
			    	}
			      return;
			 }
			 
			 var re = /(?:^|\s)(\d{4})$/;
			 if (re.test(value) && value.length < maxLen -1) {
			      ev.preventDefault();
			      return ev.target.value=value+ ' ' + digit;
			    } else if (re.test(value + digit) && value.length < maxLen -1) {
			      ev.preventDefault();
			      return ev.target.value=value + digit + ' ';
			    }
		 }
		 
// 		 var verifier={
//          	verified: false,
//          	firstSixDigit: ''
// 		  };
		 /**
		 * @param addOrRemove: true- add disabled attribute
		 	false - remove disabled attribute
		 */
		 function changeAttribute(addOrRemove){
			 $('.idebitDC').forEach(function(el){
				if(addOrRemove)
				 	el.setAttribute('disabled','disabled');
				else
					el.removeAttribute('disabled');
			 });
		 }
		 var binObject ={
					firstSix: '',
					info: {}
				};
			function checkBinDetails(value,isCC){
				console.log('inside check bin details',value);
				// value should be of string type
				if(!(typeof value === 'string' || value instanceof String))
					return ;
				// if value length is greater than 6 set value in bin level object
				if(value.length>=6){
					value = value.substring(0,6);
					if(binObject.firstSix !== value){
						ajax.post("/theia/bin/fetchBinDetails", {
					     	"bin": value.substring(0,6),
					     	"MID": document.getElementById("sucess-rate").getAttribute("data-mid").toString(),
					     	"ORDER_ID":document.getElementById("sucess-rate").getAttribute("data-oid").toString()
					    },
					    function(data, status) {
					    	binObject.firstSix = value;
							binObject.info = JSON.parse(data);
							binFunctionalities(value,isCC);
					    });
				    }
			    }
				// pass bin level object to respective functionalities
				setTimeout(binFunctionalities(value,isCC),500);
			}

			function binFunctionalities(value,isCC){
				var isIdebitEnabled = document.getElementById('payment-details').getAttribute('data-idebit-ic');
				if(!isCC){
					iciciIdebitBin(value);
				}
				cardLowSuccesRateBin(value);
				changeCardTypeIcon(value);
			}

			function changeCardTypeIcon(value, formNm) {
				// remove existing icon if any
				var allIcons = ['master','maestro','visa','discover','rupay','amex','diners','bajajfn','INVALID_CARD']
				// remove all icons from the list
				allIcons.forEach(function (cardType) {
					$('#card-number').removeClass(cardType);
				});
				var currentCardType = 'INVALID_CARD';
				if(value.length>=6 && binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.cardName) {
					currentCardType = binObject.info.binDetail.cardName.toLowerCase();
				}
				$('#card-number').addClass(currentCardType);
				return currentCardType;
			}
			function converterLowSuccessMsg(issuerLowSuccessRate,cardSchemeLowSuccessRate,issuerName, schemeName){
				lowSuccessRateMsg = document.getElementById("error-details").getAttribute("data-lowerrormsg");
				if(cardSchemeLowSuccessRate)
					lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD',schemeName+ " cards");
				else
					lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD',issuerName + " bank");
				return lowSuccessRateMsg;
			}

			function cardLowSuccesRateBin(value,formNm){
				if(value.length>=6 && (binObject.info.issuerLowSuccessRate || binObject.info.cardSchemeLowSuccessRate)){
					// use converterLowSuccessMsg() to display the message
					converterLowSuccessMsg(binObject.info.issuerLowSuccessRate, binObject.info.cardSchemeLowSuccessRate, binObject.info.binDetail.bankCode, binObject.info.binDetail.cardName);
					// display block for message element
					$('#warningDiv').show();
					if(document.getElementById('errorMsg')){
						document.getElementById('errorMsg').innerHTML = lowSuccessRateMsg;
					}
				} else {
					// display hidden for message element
					$('#warningDiv').hide();
					if(document.getElementById('errorMsg')){
						document.getElementById('errorMsg').innerHTML = '';
					}
				}
			}

			function iciciIdebitBin(value,formNm){
				if(value.length>=6 && binObject.info.binDetail && binObject.info.binDetail.bankCode === 'ICICI' && 
				  binObject.info.binDetail.cardType === 'DEBIT_CARD') {
					$('#idebitPayOption').show();
                	changeAttribute(false);
				} else {
					$('#idebitPayOption').hide();
                	changeAttribute(true);
				}
			}
		 function onCardDown(isCC){
			 var cardNo = removeSpaceFromSentence(document.getElementById('card-number').value);
			 checkBinDetails(cardNo,isCC);
		 }
				
		 
		 function onCardNumberBlur(mode){			 
			 var number = removeSpaceFromSentence($("#card-number")[0].value);
			 //$("#card-number")[0].value = number;
			 
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
				 $m[0].innerHTML = $("#promoErrorMsg")[0].getAttribute("data-value");
				 $cn.addClass("error");
			}
			
			// check card number via ajax
			 var checkValidity = $("#promoCheckValidity")[0].getAttribute("data-value") == "true";
			 if(isCardValid && checkValidity && number.length > 12) {
				var query = "?cardNumber=" + number;
				ajax.send("checkPromoValidity" + query, callback, "GET");
				function callback(data){
					data = JSON.parse(data);
					if(data.error){
						$m.show();
						$m[0].innerHTML = data.errorMsg;
						$cn.addClass("error");
					}
				}
			 }
				
		 };
		 
		 function showRewardsForm() {
				$(".rewards-otp-form").hide();
				$(".rewards-card-form").show();
				$("#rewardsAction").val("CARD_INPUT");
				return false;
	     }

		 function getCardType(e){
			 if(e.length==19||e.substring(0,4)=="6220"||e.substring(0,6)=="504834"||e.substring(0,6)=="508159"||e.substring(0,6)=="589458"){return"maestro"}
			 else if(e.substring(0,2)=="30"||e.substring(0,2)=="36"||e.substring(0,2)=="38"||e.substring(0,2)=="35"){return"diners"}
			 else if(e.substring(0,2)=="34"||e.substring(0,2)=="37"){return"amex"}else if(e.substring(0,1)=="4"){return"visa"}
			 else if(e.substring(0,2)=="51"||e.substring(0,2)=="52"||e.substring(0,2)=="53"||e.substring(0,2)=="54"||e.substring(0,2)=="55"){return"master"}
			 else if(e.substring(0,4)=="6011"){return"DISCOVER"}
			 else if(e.substring(0,6)=='608201' || checkRange(e.substring(0,6), 652850, 652865) || checkRange(e.substring(0,6), 508500, 508999) || checkRange(e.substring(0,6), 606985, 607984) || checkRange(e.substring(0,6), 608001, 608500) || checkRange(e.substring(0,6), 652150, 653149)){return"rupay"}
			 else{return"INVALID CARD"}
			 };
		 function checkRange(num, a, b){
				return (num >= a && num <= b) ? true : false;
			}
		 
		 function onEMIBankSelect(select){
			 location.href = "/theia/jsp/wap/merchantLow3/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value + "&${queryStringForSession}";
		 }
		 
		 function onEMIPlanSelect(checkbox){
			 location.href = "/theia/jsp/wap/merchantLow3/paymentForm.jsp?txn_Mode=EMI&emi_plan_id=" + checkbox.value + "&${queryStringForSession}";
		 }
		 
		 function doCancel(){
			ajax.send(cancelUrl + "?${queryStringForSession}", callback, "GET");
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
			 
			 var charCode = (e.which) ? e.which : e.keyCode;
			    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
			    	  e.preventDefault();
			        return false;
			    }
			    return true;
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
			ajax.get = function (url, data, callback, async) {
			    var query = [];
			    for (var key in data) {
			        query.push(encodeURIComponent(key) + '=' + encodeURIComponent(data[key]));
			    }
			    ajax.send(url + (query.length ? '?' + query.join('&') : ''), callback, 'GET', null, async)
			};

         ajax.sendcsrf = function(url, data, csrf, callback, sync) {
             var query = [],
                 method = 'POST';
             for (var key in data) {
                 query.push(encodeURIComponent(key) + '=' + encodeURIComponent(data[key]));
             }

             var x = ajax.x();
             x.open(method, url, sync);
             x.onreadystatechange = function() {
                 if (x.readyState == 4) {
                     callback(x.responseText)
                 }
             };

             if (method == 'POST') {
                 x.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                 x.setRequestHeader('X-CSRF-TOKEN', csrf);
             }

             x.send(query.join('&'));
         };

		// ajax plugin
		
		</script>
	</head>
<body>
	<%@ include file="../../common/common.jsp"%>
	<div id="txnTransientId" value="${txnInfo.txnId}"></div>
	<c:set var="formName" value="paymentForm"></c:set>
	<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-wallet-only="${onlyWalletEnabled}"></div>
	<c:if test="${!loginInfo.loginWithOtp}">
		<c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/authorize?response_type=code&scope=paytm&theme=pg&client_id=${loginInfo.oAuthInfoWAPClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if>
	<c:if test="${loginInfo.loginWithOtp}">l

		<div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>

		<c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/login/otp?response_type=code&scope=paytm&theme=pg-otp&clientId=${loginInfo.oAuthInfoWAPClientID}&redirectUri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if><div id="config" data-addmoney="${txnConfig.addMoneyFlag}" data-walletonly="${onlyWalletEnabled}"></div>
	<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
	<div id="error-details" 	
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}">
	</div>
	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
	<div id="payment-details" 
		data-idebit-ic ="${entityInfo.iDebitEnabled}"
	></div>
	<c:if test="${csrfToken.token != null}">
		<c:set var="varCSRF" value="${csrfToken.token}" scope="session"></c:set>
	</c:if>
	<div id="csrf-token" data-token = "${varCSRF}"></div>
	<%-- walletonly case : only login page  --%>
	<c:if test="${!loginInfo.loginFlag}">
		<script>
			var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");
			var detectedChannel="WAP";
			var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));
			var stateVal="state="+authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + jsessionid;


			var isOTPLoginAvailable=$("#isOtpLoginAvailable").length ? true : false;

			if(isOTPLoginAvailable){

				var client_id = authConfig.client_id;
				var wap_client_id = authConfig.wap_client_id;
				var eid = authConfig.eid;
				var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
				var loginType = "MANUAL";

				var detectedChannel = "WAP";
				var loginData="loginData=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + txnTransientId + ":" + jsessionid + ":" + loginType;
				var loginURL="${loginUrl}&"+loginData;
				window.location =loginURL;

			}else{
				window.location = "${loginUrl}&"+stateVal;
			}


		</script>
	</c:if>
	
	<!-- container -->
	<div>
	
		<!-- header -->
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmAppSSL'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'videocon'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'walApp'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'ccdc'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'mts'}">
		      </c:when>
		      <c:otherwise>
		      	<%@ include file="header.jsp" %>
		      </c:otherwise>
		</c:choose>
		<!-- header -->
		
		<!-- notifications -->
		<div>
		
			<c:if test="${!empty errorMsg}">
				<div class="failure danger">${errorMsg}</div>
			</c:if>
			
			<c:if test="${ empty showLoginNotification}">
				<c:set var="showLoginNotification" value="${walletInfo.walletEnabled && empty loginInfo.user}" scope="session"></c:set>
			</c:if>
			<c:if test="${showLoginNotification eq true}">
				<c:set var="showLoginNotification" value="false" scope="session"></c:set>
				<div class="failure">Login with Paytm to use your Paytm and saved cards.</div>
			</c:if>
			
			<c:if test="${txnInfo.retry}">
				<div class="failure" id="retryMsg">${retryPaymentInfo.errorMessage}</div>
			</c:if>
			
			<c:if test="${!empty txnInfo.offerMessage}">
				<div class="failure" id="offerMsg">${txnInfo.offerMessage}</div>
			</c:if>
			
			<!-- Wallet Failed Notifications -->
			<c:choose>
				<c:when test="${walletInfo.walletFailed && !empty walletInfo.walletFailedMsg}">
					<div class="failure">
						<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
						<c:if test="${onlyWalletEnabled}">
							<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel" onclick="return doCancel()">Cancel</a>
						</c:if>
					</div>
				</c:when>
				<c:when test="${!walletInfo.walletEnabled && !empty walletInfo.walletFailedMsg}">
					<div class="failure">
						<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					</div>
				</c:when>
				<c:when test="${onlyWalletEnabled && !txnConfig.addMoneyFlag && walletInfo.walletBalance < txnInfo.txnAmount}">
					<div class="failure">
						You have insufficient balance for this transaction. 
						<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel" onclick="return doCancel()">Cancel</a>
					</div>
				</c:when>
			</c:choose>
			
			
		</div>
		<!-- notifications -->
		
		
		
		<%-- paytm cash mode logic --%>
		
		<%-- first time setting --%>
		<c:if test="${ loginInfo.loginFlag && empty usePaytmCash}">
			<c:set var="usePaytmCash" value="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}" scope="session"></c:set>


			<%-- for zero bal add money case : usePaytmCash is true --%>
			<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance eq 0 && txnConfig.addMoneyFlag}">
				<c:set var="usePaytmCash" value="true" scope="session"></c:set>
			</c:if>
			
			<%-- for promocode case : usePaytmCash is false initially --%>
			<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
				<c:set var="usePaytmCash" value="false" scope="session"></c:set>
			</c:if>
			
			<c:if test="${walletInfo.walletFailed}">
				<c:set var="usePaytmCash" value="false"></c:set>
			</c:if>
		</c:if>

		<%-- remember old paymentType when paytmcash is checked or unchecked --%>
		<c:choose>
			<c:when test="${digitalCreditInfo.digitalCreditEnabled eq true}">
				<c:if test="${empty param.use_wallet && empty param.use_paytmcc}">
					<c:set var="previousPaymentType" value="${paymentType}" scope="session"></c:set>
				</c:if>
			</c:when>
			<c:otherwise>
				<c:if test="${empty param.use_wallet}">
					<c:set var="previousPaymentType" value="${paymentType}" scope="session"></c:set>
				</c:if>
			</c:otherwise>
		</c:choose>



		
		
		
		<c:if test="${param.use_wallet eq 1}">
			<c:set var="usePaytmCash" value="true" scope="session"></c:set>
			<%-- restore old paymentType --%>
			
			<c:if test="${empty param.txn_Mode}">
				<c:set var="curr" value="${paymentType}"></c:set>
				<c:set var="paymentType" value="${sessionScope.previousPaymentType}"></c:set>
				
				
				<%-- Don't set to previous payment type when cod is selected & codhybrid is false --%>
				<c:if test="${sessionScope.previousPaymentType eq 12 && !txnConfig.codHybridAllowed}">
					<c:set var="paymentType" value="${curr}"></c:set>
				</c:if>
			</c:if>
		</c:if>
		<c:if test="${param.use_wallet eq 0}">
			<c:set var="usePaytmCash" value="false" scope="session"></c:set>
			<%-- restore old paymentType --%>
			<c:if test="${empty param.txn_Mode}">
				<c:set var="paymentType" value="${sessionScope.previousPaymentType}"></c:set>
			</c:if>
		</c:if>
		
		<%-- paytm cash logic --%>

<c:if test="${digitalCreditInfo.digitalCreditEnabled eq true}">
		<%--for paytm cc code --%>
		<c:set var="paytmCCchecked" value="false" scope="session"></c:set>

		<c:if test ="${param.use_paytmcc eq 1 }">
			<c:set var="paytmCCchecked" value="true" scope="session"></c:set>
			<c:set var="usePaytmCash" value="false" scope="session"></c:set>
		</c:if>

		<c:if test ="${param.use_paytmcc eq 0 && param.usePaytmCash eq true}">
			<c:set var="paytmCCchecked" value="false" scope="session"></c:set>
			<c:set var="usePaytmCash" value="true" scope="session"></c:set>
		</c:if>

		<c:choose>
			<c:when test ="${paymentType eq 16 && param.use_wallet eq 1}">
				<c:set var="paytmCCchecked" value="false" scope="session"></c:set>
				<c:set var="usePaytmCash" value="true" scope="session"></c:set>
			</c:when>

			<c:when test ="${paymentType eq 16 && param.use_wallet ne 1}">
				<c:set var="paytmCCchecked" value="true" scope="session"></c:set>
				<c:set var="usePaytmCash" value="false" scope="session"></c:set>
				<c:set var="use_paytmcc" value="1" scope="session"></c:set>
			</c:when>
		</c:choose>
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
		<c:set var = "isInsufficientBalance" value = "${walletInfo.walletBalance < txnInfo.txnAmount}"/>
		<c:set var="isHybridAllowed" value="${txnConfig.hybridAllowed }"></c:set>
		
		<c:set var = "isHybrid" value="false" />
		<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
			<c:set var = "isHybrid" value = "${walletInfo.walletBalance < txnInfo.txnAmount}"/>
		</c:if>
		
		<c:set var="selectedModeType" value="BANK"/>
		
		<c:if test="${usePaytmCash && !isHybrid}">
			<c:set var="selectedModeType" value="WALLET_ONLY"/>
		</c:if>
		
		<c:if test="${onlyWalletEnabled}">
			<c:set var="selectedModeType" value="WALLET_ONLY"/>
		</c:if>
		
		<c:if test="${usePaytmCash && txnConfig.addMoneyFlag}">
			<c:set var="selectedModeType" value="ADD_MONEY"/>
		</c:if>
		
		<c:if test="${!empty ppi}">
			<c:set var="paymentType" value="0"/>
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
		
		<%-- no show wallet if bal is zero and add money is not available --%>
		<c:if test="${(walletInfo.walletBalance eq 0 && !txnConfig.addMoneyFlag)  || (!empty txnInfo.promoCodeResponse && txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName eq 'DISCOUNT')}">
			<c:set var="hidePaytmCash" value="true"/>
		</c:if>
		
		
		
		
		
		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
		<c:if test="${empty hidePaytmCash}">	
<!-- 			<div class="row pt20"> -->
<!-- 				<div class="fl">Total amount to be paid</div> -->
<%-- 				<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></div> --%>
<!-- 				<div class="clear"></div> -->
<!-- 			</div> -->
			<div class = "payment-balance-msg">
				<div class="payment-balance-pay-class">
					<span class="mr5">Pay </span> <b><span class="WebRupee" style="font-size: 18.5px;">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></b><span> 
<%-- 					to ${merchInfo.merchantName} --%>
					</span>
				</div>
			</div>

		</c:if>
		<%-- headers --%>
		
		
		<!-- paytm cash -->
		<%-- <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}"> --%>
		<c:if test="${!empty walletInfo.walletBalance && !empty usePaytmCash}">		
			
			<c:set var="checkPaytmCashCheckbox" value="false"/>
			<%-- <c:if test="${usePaytmCash && walletInfo.walletBalance > 0}"> --%>
			<c:if test="${usePaytmCash}">
				<c:set var="checkPaytmCashCheckbox" value="true"/>
			</c:if>
			
			<c:set var="disablePaytmCashCheckbox" value="false"/>
			
			<%-- when hybrid not allowed --%>
			<c:if test="${isInsufficientBalance && !txnConfig.addMoneyFlag && (!isHybridAllowed or walletInfo.walletBalance <= 0)}">
				<c:set var="disablePaytmCashCheckbox" value="true"/>
				<c:set var="checkPaytmCashCheckbox" value="false"/>
			</c:if>
			
			<%-- when wallet only --%>
			<c:if test="${onlyWalletEnabled}">
				<c:set var="disablePaytmCashCheckbox" value="true"/>
			</c:if>
			
			
			
			<div id="showHideWallet" style="margin-top: 25px; margin-right: 20px;" class="${!empty hidePaytmCash ? 'hide' : ''}" >	
				<div class="row" style= "padding-right: 0px; padding-top: 0px;">
					<div class="fl ml15">
						<input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> /> 
						<label for="paytmCashCB" class="paytmCash-balance mt5">Use Paytm balance</label>
						<div class="clear"></div>
						<!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm)</div> -->
						<div class="bal" id = "yourBal" style="margin-top: -2px;" >(Available Balance <span class="WebRupee ml5">Rs</span> <span class = 'amt' id = "yourBalSpan"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" /></span>)</div>
					    <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
					</div>
					<c:if test="${usePaytmCash}">
						<div class="fr  mt5" id = "walletBalance">
							  <span class="WebRupee">Rs</span>
							 <span id = "walletBalanceAmt" class="amount-bal-ptm"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" /></span>
						</div>
					</c:if>

					<div class="clear"></div>
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
				 <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance >= txnInfo.txnAmount}">
				 	 <form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="walletForm" style = "margin:0;padding:0" onsubmit = "return submitForm()">
						 <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
				    	<input type="hidden" name="submit_count" id="submit_count" value="0" />
				    	<input type="hidden"  name="txnMode" value="PPI" />
						<input type="hidden"  name="channelId" value="WAP" />
						<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
						<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
					    <div style="display: none;" class="fullWalletDeduct mr20" id = "onlyWalletAmt">
							<button class="pay-now-btn ml15 mr20" type="submit" name="" style="width: 100%; font-weight: 600;">Pay now</button>
					    </div>
					</form>
				 </c:if>
			 </div>
		</c:if>



		<!-- ICICI digital card -->
		<c:if test="${loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled }">
			<div class="row mb5 rel_pos" id="paytmIciciDigital">
				<div class="fl">
					<input name="paytmCC" id="paytmCC" type="checkbox" value="" onchange="processPaytmCC(this)" <c:if test="${paytmCCchecked}">checked="checked"</c:if>  <c:if test="${digitalCreditInfo.digitalCreditInactive}">disabled="disabled"</c:if> />
					<label for="paytmCC" class="bold-font-600">Use ICICI Bank Digital Credit</label>
					<div class="clear"></div>
					<div class="bal" id = "paytmCCBal" >(Available Digital Credit  <span class="WebRupee">Rs</span> <span class = 'amt' id = "paytmCCBalSpan"><fmt:formatNumber value="${digitalCreditInfo.accountBalance}" maxFractionDigits="2" /></span>)</div>
				</div>
			<c:if test="${paytmCCchecked}">
				<div class="fr abs_pos bold-font-600" id = "paytmCCBalance" style="top: 27px;">
					<span class="WebRupee">Rs</span>
					<span id = "paytmCCAmt">${txnInfo.txnAmount}</span>
				</div>
				</c:if>
				<div class="clear"></div>
				<c:if test="${paytmCCchecked &&  !digitalCreditInfo.digitalCreditInactive}">
					<form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
						<input type="hidden"  name="txnMode" value="PAYTM_DIGITAL_CREDIT" />
						<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
						<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
						<input type="hidden" name="pccAccountBalance" id="pccAccountBalance" value="0" />
						<input type="hidden" name="storeCardFlag"  value="off" />

						<div class="fl mb10 mt50" style="width: 100%; margin-top: 40px;">
							<input type="password" name="PASS_CODE" id="txtPassCode" placeholder="Enter Paytm Passcode" class="digitalCreditPassCode removeBoxSizing bottomBorder" maxlength="6" />
							<div class="clear"></div>
							<c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage}">
								<div class="failure low-success-rate danger" style="margin: 10px 0px 10px 0;">${digitalCreditInfo.invalidPassCodeMessage}</div>
							</c:if>
							<c:if test="${not empty requestScope.validationErrors['INVALID_PASS_CODE_BLANK']}">
								<div class="failure low-success-rate danger" style="margin: 10px 0px 10px 0;">${requestScope.validationErrors["INVALID_PASS_CODE_BLANK"]}</div>
							</c:if>

							<div class="clear"></div>
						</div>

						<div class="btn-submit fl" style="width: 100%;">
							<input class="blue-btn" type="submit" value="Pay now" name="">
						</div>
					</form>
				</c:if>
			</div>

		</c:if>

		<!-- End of ICICI digital card -->
		
	   <%-- headers --%>
		<div class="header row mt10  ml15 none-border" id = "paytmCashText" style = "display:none; margin-top: 20px;">
			OR choose any other option to pay
		</div>
	   
		
	   <!-- paytm cash -->
	   
	   
	   <div class="header mt10" id = "otherText" style="border-bottom:0.5px solid #efefef">
	   	 <c:if test="${not empty hidePaytmCash}">
	   		<div class = "payment-balance-msg">
				<div class="payment-balance-pay-class">
					<span class="pr5">Pay</span>
					<span class="WebRupee">Rs</span>
					 <b> <span id = "totalAmt" >${txnInfo.txnAmount}</span></b> 
<%-- 					to ${merchInfo.merchantName} --%>
				</div>
			</div>
			</c:if>
			<div style="margin-bottom: 5px; margin-left: 7px;">
		   		<span id="balance" class="ml15 mb15">Choose an option to pay 
		   			<c:if test="${usePaytmCash}">balance</c:if>
		   		</span>
		   		<span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}" style="margin-right: 20px;">
		   			<span class="WebRupee">Rs</span> <span id = "balanceAmt" style="font-weight: 600;">
		   					<fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" />
		   				</span>
		   		</span>
	   		</div>
		</div>
	   
	   <div class="header row mt15" id = "addMoneyText" style = "display:none">
			<div class="msgTxtBox" style="margin-left: 15px;">To complete payment, add to Paytm </div><b class="fr" style="margin-right: 10px;"><span class="WebRupee">Rs</span> <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" /></span></b>
			<div class="clear"></div>
		</div>
	   
	   
	   <%-- promocode logic --%>
		<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
		 
		 		<div id="promoType" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}"></div>
		 		<div id="promoNbList" data-value="${txnInfo.promoCodeResponse.nbBanks}"></div>
		 		<div id="promoCardList" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.cardBins}"></div>
		 		<div id="promoPaymentModes" data-value="${txnInfo.promoCodeResponse.paymentModes}"></div>
		 		<div id="promoCardTypeList" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.promoCardType}"></div>
		 		<div id="promoShowAllModes" data-value="${promoShowAllModes}"></div>
		 		<div id="promoErrorMsg" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}"></div>
		 		<div id="promoCheckValidity" data-value="${txnInfo.promoCodeResponse.checkPromoValidityURL}"></div>
		 		
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
				<c:if test="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName eq 'CASHBACK'}">
					<c:if test="${promoShowAllModes eq 0}">
						<a href = "javascript:void()" id="show-other-options" onclick="showAllPaymentModes();">Show other options to pay</a> 
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
			<div id="merchant-payment-modes" class="cards-tabs relative ">
				<%@ include file="merchantPaymentModes.jsp" %>
			</div>
		</c:if>
		<!-- Merchant Payment Modes -->
		
		<!-- Add Money Payment Modes -->
		<c:if test="${selectedModeType eq 'ADD_MONEY'}">
			<c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
			<c:set var="isAddMoneyAvailable" value="true"/>
			<div id="add-money-payment-modes" class="cards-tabs ">
					<%@ include file="addMoneyPaymentModes.jsp" %>
			</div>
		</c:if>
		<!-- Add Money Payment Modes -->
		
	</div>
	<!-- container -->

	<div class="footer">
		<div id="partner-logos">
			<img src="/theia/resources/images/wap/merchantLow3/partners-new.png" height="37px"
			 srcset="/theia/resources/images/wap/merchantLow3/img-pg-footer-logos@2x.png 2x, 
             /theia/resources/images/wap/merchantLow3/img-pg-footer-logos@3x.png 3x"
			 alt="Norton Visa Mastercard PCI DSS" title="Norton Visa Mastercard PCI DSS" style="float: left;width: 100%;" /> 
		</div>
	</div>
	<div class="light-blue-strip"></div>
	<div class="dark-blue-strip"></div>
	<script type = "text/javascript">
		onPageLoad();
		processPaytmCash();
		
	</script>
</body>
</html>
