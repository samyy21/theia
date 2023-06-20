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
		<link type="image/x-icon" rel="shortcut icon" href="${ptm:stResPath()}images/web/Paytm.ico" />
		<base href="/" />	
		<%String useMinifiedAssets = ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}/css/wap/merchantLow2/mobile.css" />
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/mobile.min.css" />
		<% } %>
		
		<c:choose>
			  <c:when test="${themeInfo.subTheme eq 'saavn'}">
					<% if(useMinifiedAssets.equals("N")){ %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/saavn.css" />
					<% } else { %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/saavn.min.css" />
					<% } %>
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
					<% if(useMinifiedAssets.equals("N")){ %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/airtel.css" />
					<% } else { %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/airtel.min.css" />
					<% } %>
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp' || themeInfo.subTheme eq 'paytmLow'}">
		      		<style>
		      			.deleteCard { display : block; }
		      		</style>
		      </c:when>
		      <c:otherwise>
		      </c:otherwise>
		</c:choose>
		<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
		<script type = "text/javascript">
		 var paymentType = "${paymentType}";
		 var savedCardList = "${!empty savedCardList}";
		 var walletBalance="${walletInfo.walletBalance}";
		 var walletFailed='true' == "${walletInfo.walletFailed}";
		 var cancelUrl = "/theia/cancelTransaction";
		 var paymentFormUrl = "/theia/jsp/wap/merchantLow/paymentForm.jsp";
		
		
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

					  if(!walletFailed) {
						
						paidAmount = 0;
						remWalletBalance = walletBalance - totalAmount;
						remWalletBalance = Math.round(remWalletBalance * 100) / 100;
						
						walletAmountToUse = totalAmount;
						$("#onlyWalletAmt").show();
						
						$('.header').hide();
						if(!isWalletOnly){
							$("#paytmCashText").show();	
						}
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
					
					if(!walletFailed) {
						var balAmount = (walletBalance - remWalletBalance);
						$("#walletBalanceAmt")[0].innerHTML = Math.round(balAmount * 100) / 100;
					}
					
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
			 }, 120000);
			 
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
			 /*try {
			 	trackEvent("onload");
			 } catch(e){};*/
			 
			 // check auto login
			 checkLogin();
			 
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
			 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
			 
			 if(e == "onload"){
				 ajax.post("/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
					var status = res;
				 });
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
                             document.querySelector('.pay-mode-tab form').style.display = "none";
                         }
                     }
                 });
         }

		 function setupPromocode(){
			 var promoType = $("#promoType")[0].getAttribute("data-value");
			 if(promoType == 'CASHBACK')
				 setupCashbackPromocode();
			 else if(promoType == 'DISCOUNT')
				 setupDiscountPromocode();
		 }
		 
		 function setupDiscountPromocode(){
			 var promoModeStr = $("#promoPaymentModes")[0].getAttribute("data-value");
			 var promoPaymentModes = promoModeStr ?  promoModeStr.split(',') : [];
		 }
		 
		 function setupCashbackPromocode(){
			 
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
			 	
			 	if(promoPaymentModes.indexOf('PPI') != -1){
					$(".promocode-options-msg-2").addClass('hide');
					$(".promocode-options-msg").removeClass('hide');
					return false;
				}
			 	
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
			 location.href = "/theia/jsp/wap/merchantLow2/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
			 return;
		 };
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
// 				}
				cardLowSuccesRateBin(value);
				changeCardTypeIcon(value);
			}
			
			function toggleCVVTopBottomIdebit(isDisplayed) {
				if (isDisplayed) {
					$('#idebitOptionCVV').show();
					$('#normalOptionCVV').hide();
					document.getElementById('idebitOptionCVVTxt').removeAttribute('disabled');
					document.getElementById('normalOptionCVVTxt').setAttribute('disabled', 'disabled');
				} else {
					$('#idebitOptionCVV').hide();
					$('#normalOptionCVV').show();
					document.getElementById('normalOptionCVVTxt').removeAttribute('disabled');
					document.getElementById('idebitOptionCVVTxt').setAttribute('disabled', 'disabled');
					
				}
			}
			
			function scOptionBasedCVVCheck(atmSelection) {
				console.log('atmSelection', atmSelection);
				if (atmSelection == 'Y') {
					event.target.parentElement.parentElement.parentElement.nextElementSibling.nextElementSibling.classList.add('hide');
				} else {
					event.target.parentElement.parentElement.parentElement.nextElementSibling.nextElementSibling.classList.remove('hide');
				}	
			}
			
			function optionBasedCVVCheck(isDisplayed) {
				if (isDisplayed) {
					// check which radio button is selected
					var allOptions = document.querySelectorAll('.idebitDC');
					if (allOptions != undefined && allOptions.length == 2) {
						var atmSelected = allOptions[0].value == 'Y' && allOptions[0].checked;
						var threeDSelected = allOptions[1].value == 'N' && allOptions[1].checked;
						if (atmSelected) {
							// lower cvv option to be hidden
							$('#idebitOptionCVV').hide();
						} else {
							// lower cvv option to be shown
							$('#idebitOptionCVV').show();
						}
					}
				}
			}
			
			/*
			* @param isDisplayed: true if idebit options are displayed
			* false otherwise
			* if idebit is displayed --> hide previous cvv box.. disable the param viceversa in opposite case
			* if selected radio is ATM pin.. hide current CVV box (below idebit as well)
			*/
			function cvvTopBottomToggle(isDisplayed) {
				// hide/show and disable/enable cvv box
				toggleCVVTopBottomIdebit(isDisplayed);
				// if this is displayed, 
				optionBasedCVVCheck(isDisplayed);
				
			}
			

			function changeCardTypeIcon(value, formNm) {
				// remove existing icon if any
				var allIcons = ['master','maestro','visa','discover','bajajfn','rupay','amex','diners','INVALID_CARD']
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
					
					document.getElementById('errorMsg').innerHTML = lowSuccessRateMsg;
				} else {
					// display hidden for message element
					$('#warningDiv').hide();
					document.getElementById('errorMsg').innerHTML = '';
				}
			}

			function iciciIdebitBin(value,formNm){
				if(value.length>=6 && binObject.info.binDetail && (binObject.info.binDetail.iDebitEnabled
					 || binObject.info.binDetail.iDebitEnabled == "true")) {
					$('#idebitPayOption').show();
                	changeAttribute(false);
                	cvvTopBottomToggle(true);
				} else {
					$('#idebitPayOption').hide();
                	changeAttribute(true);
                	cvvTopBottomToggle(false);
				}
			}
		 function onCardDown(isCC){
			 var cardNo = document.getElementById('card-number').value;
			 checkBinDetails(cardNo,isCC);
// 			 var isIdebitEnabled = document.getElementById('payment-details').getAttribute('data-idebit-ic');
// 			 if(isIdebitEnabled){
// 		            if(cardNo && cardNo.length>5){
// 			            var url="/theia/bin/checkIciciDebitBin?bin="+cardNo.substring(0,6);
// 			            if(!verifier.verified || (verifier.firstSixDigit!=cardNo.substring(0,6))) {
// 			            	ajax.get(url,undefined, function(res){
// 			                       //response data are now in the result variable
// 			            		console.log('result:',res);
// 			            		//the response is returned as string
// 			                    if(res=="true"){ 
// 			                    	verifier.verified=true;
// 			                    	$('#idebitPayOption').show();
// 			                    	changeAttribute(false);
// 			                    } else {
// 			                    	verifier.verified=false;
// 			                    	$('#idebitPayOption').hide();
// 			                    	changeAttribute(true);
// 			                    }
// 			                    verifier.firstSixDigit = cardNo.substring(0,6);
// 		                    });
// 			            } else if(verifier.verified && verifier.firstSixDigit==cardNo.substring(0,6)){
// 			            	//if it is verified and card digits are also same, it should always be displayed
// 			            	$('#idebitPayOption').show();
// 			            	changeAttribute(false);
// 			            }
// 		            } else {
// 		            	$('#idebitPayOption').hide();
// 		            	changeAttribute(true);
// 		            }
// 	           }
		 }
		 
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
			 
			 var promoModeStr = $("#promoPaymentModes")[0].getAttribute("data-value");
			 var promoPaymentModes = promoModeStr ?  promoModeStr.split(',') : [];
			 
			 if(promoPaymentModes.indexOf('PPI') != -1){
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
			 location.href = "/theia/jsp/wap/merchantLow2/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value + "&${queryStringForSession}";
		 }
		 
		 function onEMIPlanSelect(checkbox){
			 location.href = "/theia/jsp/wap/merchantLow2/paymentForm.jsp?txn_Mode=EMI&emi_plan_id=" + checkbox.value + "&${queryStringForSession}";
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

		 

		 function checkLogin(){
			 var iframe = $("#login-iframe")[0];
			 if(!iframe) { // already logged in
				 $("#login-wait").hide();
				 $("#login-stitch").show();
				 setTimeout(function(){
					 $('.alert-success').hide();
				 }, 5000);
				 return false;
			 }
			 var isOTPLoginAvailable=$("#isOtpLoginAvailable").length ? true : false;
			 if(isOTPLoginAvailable){
				 showAuthOTPView("otp", false)
			 }
			 else
			 {
				 showAuthView("login", false);
			 }

			 iframe.addEventListener("load", function(){
				 setTimeout(function(){
					 $("#login-wait").hide();
					 $("#login-stitch").show();
					 var isWalletOnly = $("#other-details")[0].getAttribute("data-wallet-only");
					 if(isWalletOnly == "true" && parseInt("${loginInfo.loginRetryCount }")<1)

						 if(isOTPLoginAvailable){
							 showAuthOTPView()
						 }
						 else
						 {
							 showAuthView();
						 }
				 }, 1000);
			 });
		 }
		 
		 
		// show login/register popup
		// type = "login"/"register", default - "login"
		function showAuthView(type, open) {
			
			if(open == undefined)
				open = true;
			
			var showRegisterView = false;
			
			if(type == "register")
				showRegisterView = true;
			
			var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));
			var hostUrl = authConfig.oAuthBaseUrl;
			
			var params = [];

			params.push('response_type=code');
			params.push('scope=paytm');
			params.push('theme=' + (authConfig.login_theme || 'pg'));

			var client_id = authConfig.client_id;
			var wap_client_id = authConfig.wap_client_id;

			var returnUrl = authConfig.return_url;
			params.push('redirect_uri=' + returnUrl);
			
			if(authConfig.EMAIL)
				params.push('email-prefill=' + authConfig.EMAIL);
			
			params.push('ADDRESS1=' + authConfig.ADDRESS1);
			params.push('ADDRESS2=' + authConfig.ADDRESS2);
			
			if(authConfig.MSISDN)
				params.push('mobile-prefill=' + authConfig.MSISDN);
			
			var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
			
			var detectedChannel = "WAP";
			
			var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");
			var loginType = open ? "MANUAL" : "AUTO";
		    var eid = authConfig.eid;
		    var jvmRoute = $("#jvmRoute")[0].getAttribute("data-value");
		    params.push("state=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + jsessionid);
			//params.push("state=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + txnTransientId + ":" + detectedChannel + ":" + jsessionid + ":" + loginType + ":" + eid + ":" + jvmRoute);

			var view = "/oauth2/authorize";
			if(showRegisterView == true)
				view = "/register";
			
			
			params.push('client_id=' + wap_client_id);
			params.push('device=mobile');
			
			// check for auto register
			if(open && authConfig.auto_signup){
				params.push('auto-signup=true');
				if(authConfig.titletext)
					params.push('titletext=' + authConfig.titletext);
			}
			
			var src = hostUrl + view + "?" + params.join('&');
			src = encodeURI(src);
			
			
			// Tracking manual/auto login
			try{
				var url =  (open ? "${sessionScope.pgTrackUrlManual}" : "${sessionScope.pgTrackUrlAuto}");
				
				if(url!=null && url!=''&& url!='null'){
					var ele = document.createElement("img");
					ele.src= url;		  
				}
			}catch(e){}
			
			// redirect
			if(open){
				setTimeout(function(){
					window.location.href = src;
				}, 100);
			} else {
				$('#login-iframe')[0].setAttribute('src', src);	
			}
			
		}


		 // FOR Login With OTP
		 function showAuthOTPView(type, open) {

			 if(open == undefined)
				 open = true;

			 var showOTPView = false;

			 if(type == "otp")
				 showOTPView = true;

			 var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));
			 var hostUrl = authConfig.oAuthBaseUrl;

			 var params = [];

			 params.push('response_type=code');
			 params.push('scope=paytm');
			 params.push('theme=pg-otp');

			 var client_id = authConfig.client_id;
			 var wap_client_id = authConfig.wap_client_id;

			 var returnUrl = authConfig.return_url;
			 params.push('redirectUri=' + returnUrl);

			 if(authConfig.EMAIL)
				 params.push('email-prefill=' + authConfig.EMAIL);

			 params.push('ADDRESS1=' + authConfig.ADDRESS1);
			 params.push('ADDRESS2=' + authConfig.ADDRESS2);

			 if(authConfig.MSISDN)
				 params.push('mobile-prefill=' + authConfig.MSISDN);

			 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");

			 var detectedChannel = "WAP";

			 var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");
			 var loginType = open ? "MANUAL" : "AUTO";
			 var eid = authConfig.eid;
			 var jvmRoute = $("#jvmRoute")[0].getAttribute("data-value");
			 //params.push("state=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + txnTransientId + ":" + detectedChannel + ":" + jsessionid + ":" + loginType + ":" + eid + ":" + jvmRoute);

			 var view = "/oauth2/login/otp";
			 params.push("loginData=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + txnTransientId + ":" + jsessionid + ":" + loginType);


			 params.push('clientId=' + wap_client_id);
			 params.push('device=mobile');

			 // check for auto register
			 if(open && authConfig.auto_signup){
				 params.push('auto-signup=true');
				 if(authConfig.titletext)
					 params.push('titletext=' + authConfig.titletext);
			 }

			 var src = hostUrl + view + "?" + params.join('&');
			 src = encodeURI(src);


			 // Tracking manual/auto login
			 try{
				 var url =  (open ? "${sessionScope.pgTrackUrlManual}" : "${sessionScope.pgTrackUrlAuto}");

				 if(url!=null && url!=''&& url!='null'){
					 var ele = document.createElement("img");
					 ele.src= url;
				 }
			 }catch(e){}

			 // redirect
			 if(open){
				 setTimeout(function(){
					 window.location.href = src;
				 }, 100);
			 } else {
				 $('#login-iframe')[0].setAttribute('src', src);
			 }

		 }

		 // END OF LOGIN WITH OTP
		 
		 
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
	<c:if test="${txnInfo == null}">
		<c:redirect url="/error"/>
	</c:if>
	<div id="txnTransientId" value="${txnInfo.txnId}"></div>
	<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
	<div id="config" data-addmoney="${txnConfig.addMoneyFlag}" data-walletonly="${onlyWalletEnabled}"></div>
	<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-wallet-only="${onlyWalletEnabled}"></div>
	<div id="error-details" 	
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}">
	</div>
	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
	<div id="payment-details" 
		data-idebit-ic ="${entityInfo.iDebitEnabled}"
	></div>

	<c:if test="${!loginInfo.loginWithOtp}">
		<c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/authorize?response_type=code&scope=paytm&theme=pg&client_id=${loginInfo.oAuthInfoWAPClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if>
	<c:if test="${loginInfo.loginWithOtp}">

		<div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>

		<c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/login/otp?response_type=code&scope=paytm&theme=pg-otp&clientId=${loginInfo.oAuthInfoWAPClientID}&redirectUri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if>

	<c:set var="formName" value="paymentForm"></c:set>
	<c:if test="${csrfToken.token != null}">
		<c:set var="varCSRF" value="${csrfToken.token}" scope="session"></c:set>
	</c:if>
	<div id="csrf-token" data-token = "${varCSRF}"></div>


	<%-- walletonly case : only login page  --%>
	<c:if test="${onlyWalletEnabled && !loginInfo.loginFlag}">
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
			  <c:when test="${themeInfo.subTheme eq 'saavn'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp'}">
		      </c:when>
		      <c:otherwise>
		      	<%@ include file="header.jsp" %>
		      </c:otherwise>
		</c:choose>
		<!-- header -->
		
		<c:if test="${!empty errorMsg}">
			<div class="mt10 mb10">
				<div class="failure danger">${errorMsg}</div>
			</div>
		</c:if>		
		
		<!-- notifications -->
		<div class="mt10 mb10">
		
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
			</c:choose>
			
			<c:if test="${onlyWalletEnabled && !txnConfig.addMoneyFlag && walletInfo.walletBalance < txnInfo.txnAmount}">
				<div class="failure">
					You have insufficient balance for this transaction. 
					<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel" onclick="return doCancel()">Cancel</a>
				</div>
			</c:if>
			
			<c:if test="${!loginInfo.loginFlag}">
				<%@ include file="login.jsp" %>
			</c:if>
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
			
			<%-- for PPI promocode case : use paytmCash --%>
			<c:if test="${fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'PPI') ne -1}">
				<c:set var="usePaytmCash" value="true" scope="session"></c:set>
			</c:if>
		</c:if>
		
		<%-- remember old paymentType when paytmcash is checked or unchecked --%>
		<c:if test="${empty param.use_wallet}">
		<c:set var="previousPaymentType" value="${paymentType}" scope="session"></c:set>
		</c:if>
		
		
		
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
		
		
		<%-- promocode logic --%>
		<c:if test="${empty promoShowAllModes}">
			<c:set var="promoShowAllModes" value="0" scope="session"></c:set>
		</c:if>
		<c:if test="${param.showAll eq 1}">
			<c:set var="promoShowAllModes" value="1" scope="session"></c:set>
		</c:if>
		<c:if test="${fn:indexOf(txnInfo.promoCodeResponse.promoCodeDetail.paymentModes, 'PPI') ne -1}">
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
		
		<%-- no show wallet if bal is zero --%>
		<c:if test="${walletInfo.walletBalance eq 0 && selectedModeType ne 'ADD_MONEY'}">
			<c:set var="hidePaytmCash" value="true"/>
		</c:if>
			
		
		<%-- Cart (amount, patym cash, add money) --%>
		<div class="cart bgwhite">
		
			<%-- headers --%>
			<div class="header row large center pt20" id = "paytmCashText" style = "display:none">
				Awesome! You have sufficient balance for this order.
			</div>
			
			
			<%@ include file="summary.jsp" %>
			
			<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
			
			<%-- <c:if test="${empty hidePaytmCash}">	
				<div class="row pt20 pb20">
					<div class="fl">Total amount to be paid</div>
					<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></div>
					<div class="clear"></div>
				</div>
			</c:if> --%>
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
				
				
				
				<div id="showHideWallet" class="${!empty hidePaytmCash ? 'hide' : ''}" style="${walletInfo.walletFailed ? 'opacity:.7;' : ''}">	
					<ul class="grid mb5 rel_pos">
						<li class="pad10">
							<input name="paytmCashCB" id="paytmCashCB" class="hide" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> /> 
							<span>Money in Paytm</span>
							<div class="clear"></div>
							<!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm)</div> -->
							<c:set var="remaining_balance" value="0"></c:set>
							
							<c:if test="${txnInfo.txnAmount < walletInfo.walletBalance}"> 
								<c:set var="remaining_balance" value="${walletInfo.walletBalance - txnInfo.txnAmount}"></c:set>
							</c:if>
							
							
							<c:if test="${!walletInfo.walletFailed}">
								<div class="bal x-small" id = "yourBal" >(Remaining money in paytm  <span class="WebRupee">Rs</span>
									<span class = 'amt' id = "yourBalSpan"><fmt:formatNumber value="${remaining_balance}" maxFractionDigits="2" /></span>)
								</div>
							</c:if>

						    <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
						</li>
						<%-- <c:if test="${usePaytmCash}"> --%>
							<li class="fr medium" id = "walletBalance">
								 <div class="pad10">
									 
									 <c:if test="${walletInfo.walletFailed eq false}">
									 (-) </c:if><span class="WebRupee">Rs</span>
									 
									 <span id = "walletBalanceAmt">
										 <fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" />
									 </span>
								</div>
							</li>
						<%-- </c:if> --%>
	
					</ul>
		
					
					<c:if test="${selectedModeType eq 'ADD_MONEY'}">
						<ul class="grid bt">
							<li class="pad10">
								<span>Additional money needed in Paytm</span>
							</li>
							<li class="fr b pad10 large">
								<c:set var="add_money_amount" value="${txnInfo.txnAmount - walletInfo.walletBalance}"></c:set>
								 <span class="WebRupee">Rs</span>
								 <span id = ""><fmt:formatNumber value="${add_money_amount}" maxFractionDigits="2" /></span>
							</li>
						</ul>
					</c:if>
					
					
					 <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance >= txnInfo.txnAmount && !walletInfo.walletFailed}">
					 	 <form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="walletForm" style = "margin:0;padding:0" onsubmit = "return submitForm()">
							 <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
					    	<input type="hidden"  name="txnMode" value="PPI" />
							<input type="hidden"  name="channelId" value="WAP" />
							<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
							<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
						    <div style="display: none;" class="fullWalletDeduct pb20" id = "onlyWalletAmt">
								<input class="blue-btn" type="submit" value="Pay now" name="">
						    </div>
						</form>
					 </c:if>
				 </div>
			</c:if>
			
		   <!-- paytm cash -->
		   
		   
		</div>
		<%-- Cart (amount, patym cash, add money) --%>
	   
	   <c:choose>
	   		<c:when test="${themeInfo.subTheme eq 'saavn'}">
	   			<c:set var="selectedMode" value=""/>
	   			<%@ include file="select-mode-link.jsp" %>
	   			<div class="header medium ${onlyWalletEnabled  ? 'hide' : ''}" id = "otherText">
	   				Payment Method
	   				<span class="fr selected-mode-name">${selectedMode}</span>
				</div>
	   		</c:when>
	   		<c:otherwise>
	   			<div class="header center medium ${onlyWalletEnabled ? 'hide' : ''}" id = "otherText"> 
			   		<span id="balance">Select an option to pay <c:if test="${usePaytmCash}">balance</c:if></span> <span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}">Rs <span id = "balanceAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" /></span></span>
				</div>
			   
			   <div class="header row center medium" id = "addMoneyText" style = "display:none">
					Select an option to <b class=""> add Rs <span id = "addMoneyAmt">${txnInfo.txnAmount}</span> to paytm </b> & complete this order
				</div>
		    </c:otherwise>
	   </c:choose>
	   
		
		
	   
	   
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
		<%-- <c:if test="${selectedModeType eq 'WALLET_ONLY'}">
			<c:set var='paymentType' value="7"></c:set>
			<c:set var='selectedModeType' value="BANK"></c:set>
		</c:if> --%>

		<!-- Merchant Payment Modes -->
		<c:set var="paymentConfig" value="${sessionScope}" />
		<c:if test="${selectedModeType eq 'BANK'}">
			<div id="merchant-payment-modes" class="cards-tabs relative">
				<%@ include file="merchantPaymentModes.jsp" %>
			</div>
		</c:if>
		<!-- Merchant Payment Modes -->
		
		<!-- Add Money Payment Modes -->
		<c:if test="${selectedModeType eq 'ADD_MONEY'}">
			<c:set var="paymentConfig" value="${sessionScope.addMoneyPayModes}" />
			<c:set var="isAddMoneyAvailable" value="true"/>
			<div id="add-money-payment-modes" class="cards-tabs">
					<%@ include file="addMoneyPaymentModes.jsp" %>
			</div>
		</c:if>
		<!-- Add Money Payment Modes -->
		
	</div>
	<!-- container -->

	<div class="footer">
		<div id="partner-logos" align="middle">
			<!-- <div class="img img-partner-logo"></div> -->
			<img src="${ptm:stResPath()}images/wap/merchantLow2/partners-new.png" height="37px" alt="Norton Visa Mastercard PCI DSS" title="Norton Visa Mastercard PCI DSS" />
		</div>
	</div>
	<script type = "text/javascript">
		onPageLoad();
		processPaytmCash();
	</script>
	
	
</body>
</html>
