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
<!-- 		<base href="/" />	 -->
		<%String useMinifiedAssets = ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow/mobile.css" />
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow/mobile.min.css" />
		<% } %>
		
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
					<% if(useMinifiedAssets.equals("N")){ %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/airtel.css" />
					<% } else { %>
						<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow2/airtel.min.css" />
					<% } %>
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp' || themeInfo.subTheme eq 'paytmLow'|| themeInfo.subTheme eq 'paytmAppSSL'|| themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme  eq 'Charges'}">
		      		<style>
		      			.deleteCard { display : block; }
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
		<c:if test="${themeInfo.subTheme  eq 'Charges'}">
			<style>
			#postConvCharge{
			    font-size: 12px;
	   			padding: 0 8px;
	   			
			}
			</style>


		</c:if>
		<style>
			#otherpaymentModeHeading,.smltxt, #otherText{padding-left:15px;}
			.pay-mode-tab{padding-left:10px;}

		</style>
		<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
		<style>
			.red-text {
				color : red;
			}
		</style>
		<script type = "text/javascript">



		 var paymentType = "${paymentType}";
		 var savedCardList = "${!empty savedCardList}";
		 var walletBalance="${walletInfo.walletBalance}";
		 var cancelUrl = "/theia/cancelTransaction";
		 var paymentFormUrl = "/theia/jsp/wap/merchantLow4/paymentForm.jsp";
		 var basePath="${ptm:stResPath()}";

		 function subWallets(){
			 var subwallets=$("#WalletsWABView")[0];
			 var arrowBtn=$("#showpaytmWallets")[0];
			 if(subwallets.style.display=="block"){
				 subwallets.style.display="none";
				 arrowBtn.style.background="url("+basePath+"/images/arrowUp.png) no-repeat";
			 }else{
				 arrowBtn.style.background="url("+basePath+"/images/arrowDown.png) no-repeat";


				 subwallets.style.display="block";
			 }
			 event.stopPropagation();
		 }


		
		 function processPaytmCC(checkbox){
			 //console.log('inside process paytmcc');
			 //change redirection

			 var isClicked = true;

			 if(!checkbox) {
				 checkbox = $("#paytmCC")[0];
				 isClicked = false;
			 }
			 if(checkbox == null) return;



			 if(checkbox.checked){
				 if(isClicked){
			 	location.href = paymentFormUrl + "?use_paytmcc=1" + "&${queryStringForSession}";
				 }
			 }
			 else{
				 location.href = paymentFormUrl + "?use_paytmcc=0" + "&${queryStringForSession}";
			 }
			 
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
						$('#post-conv-wallet-only').show();
						
					} else {
						//post conv related in case of hybrid payment
						$('#normal-post-con').hide();
						$('#hybrid-post-con').show();
						// changing pay now text in button
						if($('#hybrid-post-con')[0])
							$('#btnSubmit')[0].innerHTML = $('#hybrid-post-con')[0].getAttribute('data-post-bal');
						paidAmount = totalAmount - walletBalance;
                        paidAmount = (Math.round(paidAmount * 100) / 100).toFixed(2);
						walletAmountToUse = walletBalance; 
					}
					
						
					if(addMoney){
			
						$('.header').hide();
						$("#addMoneyText").show();
						$("#addMoneyAmt")[0].innerHTML = paidAmount;
						walletAmountToUse = walletBalance;
						//post conv changes
						$('#addnpay-post-con').show();
						$('#hybrid-post-con').hide();
						$('#normal-post-con').hide();
						if($('#addnpay-post-con')[0])
							$('#btnSubmit')[0].innerHTML = $('#addnpay-post-con')[0].getAttribute('data-post-bal');

					}
					
					
					$("#yourBal").show();
					// show remaining balance
					
					
					var amt = (walletBalance - remWalletBalance);
					amt = Math.round(amt * 100) / 100;
					/*if($("#walletBalanceAmt")[0]){
						$("#walletBalanceAmt")[0].innerHTML = amt;
					}*/

					if($("#walletBalance")[0]){
						$("#walletBalance")[0].style.display = "inline";
					}

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
			 if($("#balanceAmt").length > 0) {
				 $("#balanceAmt")[0].innerHTML = paidAmount;
			 }
				//$("#finalAmt")[0].innerHTML = paidAmount;

			}
		 
		 var formSubmitted = false;
		 
		 function submitForm(formEle) {

             if(!formEle){
                 var scForm = document.getElementById("SC-form");
                 if(scForm){
                     formEle = scForm;
                 }
             }

             if(formEle != undefined) {
                 var formValid = false;

                 if (formEle.id && formEle.id == 'CC-form')
                     formValid = validateCCForm();
                 else if (formEle.id && formEle.id == 'DC-form')
                     formValid = validateDCForm();
                 else if (formEle.id && formEle.id == 'emiForm')
                     formValid = validateEmiCCForm();
                 else if(formEle.id && formEle.id == 'SC-form')
                     formValid = validateSCForm(formEle);
				 /*else if(formEle.id && formEle.id == 'upiForm')
				  formValid = validateUPIForm();*/

                 if (!formValid)
                     return false;
             }


             var cardNumberEle = $("#card-number");
             if(cardNumberEle && cardNumberEle.length > 0){
                 $("#card-number")[0].value = removeSpaceFromSentence($("#card-number")[0].value);
             }

			 if(paymentType == 5) {
				if($("#cvvNum").length)
					setCVV();
			 }
			
			 if(paymentType == 3) {
				 var code =-1;
				 var nbSelect = $("#nbSelect")[0];
				 if(nbSelect && $("#bankCode").length){
				 	code = nbSelect.options[nbSelect.selectedIndex].value;
				 	$("#bankCode")[0].value = code;
				 }
				 if(code == -1){
				 	$("#bankCode")[0].value = document.getElementById('bankCode').value;
				 }
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
				 btns[i].innerHTML="Please wait...";
			 }

			 formSubmitted = true;
			 
			 setTimeout(function(){
				 formSubmitted = false;
				 for(var i=0; i< btns.length;i++){
					 btns[i].innerHTML="Proceed Securely";
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
			 /*try {
			 	trackEvent("onload");
			 } catch(e){};*/
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
                         ev.target.parentElement.style.display = "none";
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

			 // OPEN CC (OPEN PAYMENT MODE IF USER HAS PROMO CASHBACK)

			 var promoTypeval=$("#promoType")[0].getAttribute("data-value");
			 if($('.SC-tab').length==0 && $('#SC-link').length==0){

				 for(var i=0;i<promoPaymentModes.length;i++){
					 // show saved cards for cc dc
					 if(promoPaymentModes[i] == "CC"){
						 $("#" + promoPaymentModes[i] + "-link")[0].click();
						 break;
					 }
					 else if(promoPaymentModes[i] == "DC"){
						 $("#" + promoPaymentModes[i] + "-link")[0].click();
						 break;
					 }

				 }

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
			 location.href = "/theia/jsp/wap/merchantLow4/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
			 return;
		 };


         function removeSpaceFromSentence(sentence){
             return sentence.split('').filter(function(val){return val!=' '}).join('');
         }

         function checkIfFourDigitReached(word){

         }

         function pressCardSpace(ev){
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
                 } else { //remove space on backspace
                     if ((/\d\s$/).test(value)) {
                         ev.preventDefault();
                         ev.target.value = value.replace(/\d\s$/, '');
                     } else if((/\s\d?$/).test(value) == true) {
                         ev.target.value = value.replace(/\s\d?$/, '');
                         ev.preventDefault();
                     }
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


         function toggleLuhnErr(valid, form) {
             var luhnErr = form.querySelector("#luhnError");
             if (luhnErr && !valid) {
                 luhnErr.style.display = "inline-block";
                 luhnErr.classList.add("shake", "red-text");
                 setTimeout(function(){luhnErr.classList.remove("shake");}, 2000);
                 //	form.querySelectorAll(".error").addClass("inlineEle");
             } else if (luhnErr) {
                 luhnErr.style.display = "none";
                 luhnErr.classList.remove("shake", "red-text");
                 //	form.find(".error").removeClass("inlineEle");
             }
         }

         function checkLuhn(cardNo) {
             var nDigits = cardNo.length;
             var nSum = 0, isSecond = false;
             for (var i = nDigits - 1; i >= 0; i--) {
                 var d = parseInt(cardNo[i]);
                 if (isSecond == true)
                     d = d * 2;

                 nSum += parseInt(d / 10);
                 nSum += d % 10;

                 isSecond = !isSecond;
             }
             return (nSum % 10 == 0);
         }


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

         function showFieldError(form,Elem)
         {
             var name = Elem.name;
             if(!name)
                 return false;

             //	var label = form.querySelector("label[for=" + name + "]");
             var label = Elem.parentElement.querySelector("label[for=" + name + "]");
             label.classList.add("shake", "red-text");
             setTimeout(function(){label.classList.remove("shake");}, 2000);
         };

         function removeFieldError(form,Elem)
         {
             var name = Elem.name;
             if(!name)
                 return false;

             //	var label = form.querySelector("label[for=" + name + "]");
             var label = Elem.parentElement.querySelector("label[for=" + name + "]");
             label.classList.remove("shake", "red-text");
         }

		 //validate sc form
		 function validateSCForm(formEle)
		 {
			 var savedCards = document.getElementsByClassName("row-dot sc-row");
			 var curRadioEl;
			 var isCCCvvValid;
			 var radioflag = 0;
			 if(savedCards)
			 {
				 var curSavedCard;
				 for(var i = 0;i < savedCards.length ; i++)
				 {
					 curRadioEl = savedCards[i].querySelector("input[type='radio']");
					 if(curRadioEl && curRadioEl.checked)
					 {
						 radioflag = 1;
						 curSavedCard = savedCards[i];
						 break;
					 }
				 }
				 if(radioflag ==1)
				 {
					 var cvvElm = curSavedCard.querySelector("[id^=cvv_][id$=_input]") ;
					 //to remove red-text from label
					 if(cvvElm.name!=""){
						 var label = curSavedCard.querySelector("label[for=" + cvvElm.name + "]");

						 if(label && label.classList.contains("red-text"));
						 label.classList.remove("red-text");
					 }



					 if(cvvElm)
					 {
						 var cvv = cvvElm.value;
						 isCCCvvValid = ((cvv) && !isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) ? true : false;
					 }
					 else
						 isCCCvvValid = false;

					 var isScI_debit=curSavedCard.querySelector("#idebitSavedCard");
					 if(isScI_debit){
						 var securePinOtp=isScI_debit.querySelectorAll("[name=isIciciIDebit]")[1].checked

						 // For SC IDEBIT CARD
						 if(isScI_debit && !securePinOtp){
							 isCCCvvValid=true;
							 return true;
						 }

					 }

					 if(!isCCCvvValid)
					 {
						 showFieldError(curSavedCard,cvvElm);
						 return false;
					 }
					 else
						 removeFieldError(curSavedCard,cvvElm);

				 }

			 }

			 return true;
		 };

         //validations for credit cards
         function validateCCForm(){
             var form = document.getElementById('CC-form');

             //Card Number check
             var isCCNumberValid = false;
             var cardInput = form.elements['card-number'];
             if(cardInput)
             {
                 var cardNumber = cardInput.value;
                 if(cardNumber && !checkLuhn(cardNumber.replace(/\s/g, ''))) {
                     toggleLuhnErr(false, form);
                     return false;
                 }
                 if(!isNaN(removeSpaceFromSentence(cardNumber)) && ((!form.elements['card-number'].classList.contains("maestro") && cardInput.maxLength == cardNumber.length) ||
                         (form.elements['card-number'].classList.contains("maestro") && cardNumber.length >= 19)))
                     isCCNumberValid = true;
                 else
                     isCCNumberValid = false;
             }
             else
                 isCCNumberValid = false;

             if(!isCCNumberValid)
             {
                 showFieldError(form,cardInput);
                 return false;
             }
             else
                 removeFieldError(form,cardInput);

             if(form.elements['card-number'] && !form.elements['card-number'].classList.contains("maestro")) {
                 //Expiry Month
                 var monthElm = form.elements['ccExpMnth'];
                 var month = monthElm.value;
                 var isCCExpMonthValid = month == "0" ? false : true;

                 if (!isCCExpMonthValid) {
                     showFieldError(form, monthElm);
                     return false;
                 }
                 else
                     removeFieldError(form, monthElm);


                 //Expiry Year
                 var yearElm = form.elements['ccExpYr'];
                 var year = yearElm.value;
                 var isCCExpYearValid = year == "0" ? false : true;

                 if (!isCCExpYearValid) {
                     showFieldError(form, yearElm);
                     return false;
                 }
                 else
                     removeFieldError(form, yearElm);

                 if ((new Date()).getFullYear() == year && ((new Date()).getMonth() + 1) > parseInt(month)) {
                     showFieldError(form, monthElm);
                     showFieldError(form, yearElm);
                     return false;
                 }
             }
             //Cvv Check
             var cvvElm = form.elements['ccCvvBox'];
             var isCCCvvValid;
             if(cvvElm)
             {
                 var cvv = cvvElm.value;
                 isCCCvvValid = (((cvv) && !isNaN(cvv) && (cvv.length == cvvElm.maxLength)) || form.elements['card-number'].classList.contains("maestro")) ? true : false;
             }
             else
                 isCCCvvValid = false;
             if(!isCCCvvValid)
             {
                 showFieldError(form,cvvElm);
                 return false;
             }
             else
                 removeFieldError(form,cvvElm);

             return true;
         };

		 //validation for debit cards
		 function validateDCForm(){
			 var form = document.getElementById('DC-form');

			 //Card Number check
			 var isDCNumberValid = false;
			 var cardInput = form.elements['card-number'];
			 if(cardInput)
			 {
				 var cardNumber = cardInput.value;
                 if(cardNumber && !checkLuhn(cardNumber.replace(/\s/g, ''))) {
                     toggleLuhnErr(false, form);
                     return false;
                 }
                 if(!isNaN(removeSpaceFromSentence(cardNumber)) && ((!form.elements['card-number'].classList.contains("maestro") && cardInput.maxLength == cardNumber.length) ||
                         (form.elements['card-number'].classList.contains("maestro") && cardNumber.length >= 19)))
					 isDCNumberValid = true;
				 else
					 isDCNumberValid = false;
			 }
			 else
				 isDCNumberValid = false;
			 if(!isDCNumberValid)
			 {
				 showFieldError(form,cardInput);
				 return false;
			 }
			 else
				 removeFieldError(form,cardInput);

             if(form.elements['card-number'] && !form.elements['card-number'].classList.contains("maestro")) {
                 //Expiry Month
                 var monthElm = form.elements['dcMonth'];
                 var month = monthElm.value;
                 var isDCExpMonthValid = month == "0" ? false : true;
                 if (form.elements['card-number'].classList.contains("maestro")) {
                     isDCExpMonthValid = true;
                 }

                 if (!isDCExpMonthValid) {
                     showFieldError(form, monthElm);
                     return false;
                 }
                 else
                     removeFieldError(form, monthElm);


                 //Expiry Year
                 var yearElm = form.elements['dcYear'];
                 var year = yearElm.value;
                 var isDCExpYearValid = year == "0" ? false : true;

                 if (form.elements['card-number'].classList.contains("maestro")) {
                     isDCExpYearValid = true;
                 }


                 if (!isDCExpYearValid) {
                     showFieldError(form, yearElm);
                     return false;
                 }
                 else
                     removeFieldError(form, yearElm);

                 if ((new Date()).getFullYear() == year && ((new Date()).getMonth() + 1) > parseInt(month)) {
                     showFieldError(form, monthElm);
                     showFieldError(form, yearElm);
                     return false;
                 }
             }
             //Cvv Check
			 var cvvElm = form.elements['normalOptionCVVTxt'];
			 var isDCCvvValid=false;

             if(binObject.info.binDetail.iDebitEnabled && form.elements['isIciciIDebit'][1].checked) {
                 cvvElm = form.elements['idebitOptionCVVTxt'];
             } else if(binObject.info.binDetail.iDebitEnabled) {
                 cvvElm = null;
             }

             if(cvvElm)
             {
                 var cvv = cvvElm.value;

                 if((!isNaN(cvv) && cvv.length==cvvElm.maxLength) || (form.elements['card-number'].classList.contains("maestro")))
                     isDCCvvValid=true;
                 else
                     isDCCvvValid = false;
             }
             else
                 isDCCvvValid = true;

			 if(!isDCCvvValid)
			 {
				 showFieldError(form,cvvElm);
				 return false;
			 }
			 else
				 removeFieldError(form,cvvElm);

			 return true;
		 };

         //validation for EMI credit cards
         function validateEmiCCForm(){
             var form = document.getElementById('emiForm');

             //for card number
             var isEMINumberValid = false;
             var cardInput = form.elements['card-number'];
             if(cardInput)
             {
                 var cardNumber = cardInput.value;
                 if (!isNaN(removeSpaceFromSentence(cardNumber)) && removeSpaceFromSentence(cardNumber).length >= 14 && removeSpaceFromSentence(cardNumber).length <= 19)
                     isEMINumberValid = true;
                 else
                     isEMINumberValid = false;
             }
             else
                 isEMINumberValid = false;
             if(!isEMINumberValid)
             {
                 showFieldError(form,cardInput);
                 return false;
             }
             else
                 removeFieldError(form,cardInput);

             //for expiry month
             var monthElm = form.elements['emiExpMnth'];
             var month = monthElm.value;
             var isEMIExpMonthValid = month == "0" ? false : true;
             if(!isEMIExpMonthValid)
             {
                 showFieldError(form,monthElm);
                 return false;
             }
             else
                 removeFieldError(form,monthElm);

             //for expiry year
             var yearElm = form.elements['emiExpYear'];
             var year = yearElm.value;
             var isEMIExpYearValid = year == "0" ? false : true;
             if(!isEMIExpYearValid)
             {
                 showFieldError(form,yearElm);
                 return false;
             }
             else
                 removeFieldError(form,yearElm);

             //for cvv
             var isEMICvvValid;
             var cvvElm = form.elements['emiCVVBox'];
             if(cvvElm)
             {
                 var cvv = cvvElm.value;
                 isEMICvvValid = (!isNaN(cvv) && (cvv.length == 3)) ? true : false;
                 if(form.elements['card-number'].classList.contains("amex")){
                     var isEMICvvValid = (!isNaN(cvv) && (cvv.length == 4)) ? true : false;
                 }
             }
             else
                 isEMICvvValid = false;

             if(!isEMICvvValid)
             {
                 showFieldError(form,cvvElm);
                 return false;
             }
             else
                 removeFieldError(form,cvvElm);

             return true;

         };

         // UPI VALIDATION START

         //Regex test for UPI validation
         function validateStrVPA(vpa)
         {
             var re = /^[a-zA-Z0-9.-]*$/;
             return re.test(vpa);
         }
         // @ validation in VPA
         function validateAtVPA(vpa)
         {
             var re = /\S+@\S/;
             return re.test(vpa);
         }


         function validationUPI(vpa){
             var isValid=false;
             if(vpa.length > 0 && vpa.length <= 255 && validateAtVPA(vpa)){
                 vpaSplit = vpa.split("@");
                 if(vpaSplit.length == 2){
                     var handle = vpaSplit[0];
                     var psp = vpaSplit[1];
                     if(psp.length > 0 && handle.length){
                         isValid = validateStrVPA(psp) && validateStrVPA(handle);
                     }
                 }
             }
             return isValid;
         }

         //upi validation
         function validateUPIForm(){
             var form = document.getElementById('upiForm');
             var upiElm = form.elements['VIRTUAL_PAYMENT_ADDRESS'];
             var upi = upiElm.value;
             var isupiValid = validationUPI(upi);
             if(!isupiValid)
             {
                 showFieldError(form,upiElm);
                 return false;
             }
             else
                 removeFieldError(form,upiElm);
             return true;
         }


         var binObject ={
					firstSix: '',
					info: {}
				};


			function checkBinDetails(value,isCC, formNm){
				// value should be of string type
				if(!(typeof value === 'string' || value instanceof String))
					return ;

                var txnMode = "";

                if(formNm && formNm.children && formNm.children.txnMode) {
                    txnMode = formNm.children.txnMode.value;
                }
				// if value length is greater than 6 set value in bin level object
				if(value.length>=6){
					value = value.substring(0,6);
					if(binObject.firstSix !== value){
						ajax.post("/theia/bin/fetchBinDetails", {
					     	"bin": value.substring(0,6),
					     	"MID": document.getElementById("sucess-rate").getAttribute("data-mid").toString(),
					     	"ORDER_ID":document.getElementById("sucess-rate").getAttribute("data-oid").toString(),
							"txnMode":txnMode
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
                var disableMaestro = inputMaxLength();
			    showPromoError(value);
				var isIdebitEnabled = document.getElementById('payment-details').getAttribute('data-idebit-ic');
				if(!isCC){
					iciciIdebitBin(value);
				}
				cardLowSuccesRateBin(value);
				changeCardTypeIcon(value);
                disableMasetroCVV(disableMaestro);
			}


         function disableMasetroCVV(disableMaestro) {
             if(disableMaestro && document.getElementById("normalOptionCVVTxt")) {
                 document.getElementById("normalOptionCVVTxt").disabled = "disabled";
             }
         }

         function inputMaxLength() {
             var inputFld, ccCvvFld, dcCvv1, dcCvv2;
             inputFld = document.getElementById("card-number");
             cvvFld = document.getElementById("ccCvvBox");
             if(!cvvFld) {
                 dcCvv1 = document.getElementById("normalOptionCVVTxt");
                 dcCvv2 = document.getElementById("idebitOptionCVVTxt");
             }
             var cvvLength = 4;
             var disableCvv = false;

             if(inputFld && binObject.info.binDetail && binObject.info.binDetail.cardName) {
                 var cardName = binObject.info.binDetail.cardName;
                 switch(cardName) {
                     case "VISA" :
                         inputFld.maxLength = 19;
                         cvvLength = 3;
                         break;
                     case "MASTER" :
                         inputFld.maxLength = 19;
                         cvvLength = 3;
                         break;
                     case "DINERS" :
                         inputFld.maxLength = 17;
                         cvvLength = 3;
                         break;
                     case "MAESTRO" :
                         inputFld.maxLength = 23;
                         cvvLength = 3;
                         disableCvv = true;
                         break;
                     case "AMEX" :
                         inputFld.maxLength = 18;
                         cvvLength = 4;
                         break;
                     case "DISCOVER" :
                         inputFld.maxLength = 19;
                         cvvLength = 3;
                         break;
                     default:
                         inputFld.maxLength = 19;
                         cvvLength = 3;
                 }
                 if(cvvFld){
                     cvvFld.maxLength = cvvLength;
                 }
                 else {
                     dcCvv1.maxLength = cvvLength;
                     dcCvv2.maxLength = cvvLength;
                 }
             } else {
                 if(inputFld){
                     inputFld.maxLength = 19;
                     if(cvvFld) {
                         cvvFld.maxLength = 3;
                     }
                     else {
                         dcCvv1.maxLength = 3;
                         dcCvv2.maxLength = 3;
                     }
                 }
             }
             return disableCvv;
         }


         function showPromoError(value) {

             var $m = $("#wrong-promo-card-msg");
             var $cn = $("#card-number");

             if(value.length>=6 && binObject.info && binObject.info.promoResultMessage && binObject.info.promoResultMessage.length > 0) {
                 $m.show();
                 $m[0].innerHTML = binObject.info.promoResultMessage;
                 $cn.addClass("error");
             } else {
                 $m.hide();
                 $cn.removeClass("error");
             }
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
             event.stopPropagation();
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
             var cardNumberEle = document.getElementById('card-number');
             if(cardNumberEle){
                 cardNumberEle.value = cardNumberEle.value.replace(/[^0-9 ]/g,'');
                 var cardNo = removeSpaceFromSentence(cardNumberEle.value);
                 var formNm = $(event)[0].target.closest("form");
                 toggleLuhnErr(true, formNm);
                 checkBinDetails(cardNo,isCC, formNm);
             }
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
             var cardNumberEle = $("#card-number");
             if(cardNumberEle && cardNumberEle.length > 0){
                 var number = removeSpaceFromSentence($("#card-number")[0].value);
                 //Check if cardnumber is LUHN valid
                 toggleLuhnErr(checkLuhn(number), cardNumberEle[0].closest('form'));

                 if(!number || $("#promoType").length == 0)
                     return false;

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
			 location.href = "/theia/jsp/wap/merchantLow4/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value + "&${queryStringForSession}";
		 }

		 function onEMIPlanSelect(checkbox){
			 var emiValue = checkbox.value;
			 emiValue = emiValue.split('|')[1];
			 location.href = "/theia/jsp/wap/merchantLow4/paymentForm.jsp?txn_Mode=EMI&emi_bank=${emiSelectedBank}&emi_plan_id=" + emiValue + "&${queryStringForSession}";


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
             if(charCode == 229){
                 e.target.value = e.target.value.replace(/[^\d]/g,'');
                 return false;
             }

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
		    if(authConfig.autoLogin != "N") {
		        var open = authConfig.autoLogin == "M" ? true : false;
		        if(parseInt("${loginInfo.loginRetryCount }") <= 0) {
		            if (isOTPLoginAvailable) {
		                showAuthOTPView("otp", open)
		            }
		            else {
		                showAuthView("login", open);
		            }
		        }
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



		params.push('clientId=' + wap_client_id);

	    params.push("loginData=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + txnTransientId + ":" + jsessionid + ":" + loginType);




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
		

	 // on Saved Card Details Tab Click
	 function onSaveCardTabClick(event,tab){
            var constant = {
                    "SAVE_CARD_QRY":"row-dot sc-row",
                    "INP_RADIO_QRY":"input[type='radio']",
                    "TAB_DETAIL_QRY":"[id^=cvv_][id$=_pad]"
                },
                savedCards = document.getElementsByClassName(constant.SAVE_CARD_QRY),
                inputRadioEle = tab.querySelector(constant.INP_RADIO_QRY),
                tabDetail = tab.querySelector(constant.TAB_DETAIL_QRY);

         		$(tab)[0].closest("form").txnMode.value = $(tab)[0].getAttribute("data-txnMode");

         		if(inputRadioEle && inputRadioEle.checked && inputRadioEle !== event.target)
         		{
             		// user click on same tab
					return;
         		}


         		var innerRadioEl = tab.querySelectorAll(".paymentIdebit");
         		if(innerRadioEl && innerRadioEl.length > 1)
         		{
             		innerRadioEl[0].checked = true;
             		var innerCvvEl = tab.querySelector(".cvv-box");
             		if(innerCvvEl)
                 		innerCvvEl.classList.add('hide');
         		}
            // Hide all saved card details tab
            // Unchecked all saved card checkbox    
            if(savedCards){
                for(var i = 0;i < savedCards.length ; i++){
                    var eachRadioEle = savedCards[i].querySelector(constant.INP_RADIO_QRY);
                    var eachTabDetail = savedCards[i].querySelector(constant.TAB_DETAIL_QRY);

                    if(eachRadioEle){
                        eachRadioEle.checked = false;
                    }

                    if(eachTabDetail){
                        eachTabDetail.style.display = "none";
                    }
                }
            }

            // Checked current saved card radio button 
            if(inputRadioEle){
                inputRadioEle.checked = true;
            }

            // Show current saved card tab
            if(tabDetail){
                tabDetail.style.display = "block";
            }

			 if(tab.children.savedCardId){
				 showSavedCard(tab.children.savedCardId,tab.children.savedCardId.id);
			 }
        }

		function walletItemClickable(element){
            if(element && element.click){
                element.click();
            }
        }
         function checkRadioEl(){
             if($(".promo-valid-card") && $(".promo-valid-card").length > 0) {
                 $(".promo-valid-card")[0].closest("form").txnMode.value = $(".promo-valid-card")[0].getAttribute("data-txnMode");
             }
             var radioEl = document.getElementsByClassName('paymentIdebit');
             if(radioEl && radioEl.length > 0)
             	radioEl[0].checked = true;
         }

		</script>
	</head>
<body onload="checkRadioEl()">
	<%@ include file="../../common/common.jsp"%>
	<c:if test="${txnInfo == null}">
		<c:redirect url="/error"/>
	</c:if>
	<div id="txnTransientId" value="${txnInfo.txnId}"></div>
	<c:set var="formName" value="paymentForm"></c:set>

	<c:if test="${csrfToken.token != null}">
		<c:set var="varCSRF" value="${csrfToken.token}" scope="session"></c:set>

	</c:if>
	<div id="csrf-token" data-token = "${varCSRF}"></div>
	<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-wallet-only="${onlyWalletEnabled}"></div>
	<c:if test="${!loginInfo.loginWithOtp}">
		   <c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/authorize?response_type=code&scope=paytm&theme=pg&client_id=${loginInfo.oAuthInfoWAPClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if>
	<c:if test="${loginInfo.loginWithOtp}">	
		   <div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>
		   <c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/login/otp?response_type=code&scope=paytm&theme=pg-otp&clientId=${loginInfo.oAuthInfoWAPClientID}&redirectUri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	</c:if>
	
	<div id="config" data-addmoney="${txnConfig.addMoneyFlag}" data-walletonly="${onlyWalletEnabled}"></div>
	<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
	<div id="error-details" 	
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}">
	</div>
	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
	<div id="payment-details" 
		data-idebit-ic ="${entityInfo.iDebitEnabled}"
		data-postconv-enabled = "${txnConfig != null &&  txnConfig.paymentCharges != null}"
	></div>

	
	<!-- container -->
	<div>
	
		<!-- header -->

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
		
		
		<%-- for PCF:PGP-2506  --%>
		<c:if test = "${txnConfig != null &&  txnConfig.paymentCharges != null 
			&& walletInfo.walletBalance >= txnInfo.txnAmount && walletInfo.walletBalance < txnConfig.paymentCharges.PPI.totalTransactionAmount}">
			<c:set var="selectedModeType" value="BANK"/>
			<c:set var= "usePaytmCash" value="false"/>
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

		<%@ include file="login.jsp" %>
		
		<%-- headers --%>
		<div class="header row" id = "paytmCashText" style = "display:none">
			Uncheck Paytm to pay using other options.
		</div>

		
		<%--<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
		<c:if test="${empty hidePaytmCash}">	
			<div class="row pt20">
				<c:choose>
			<c:when test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges}">
			<div class="fl">Amount to be added</div>
			</c:when>
			<c:otherwise>
			<div class="fl">Total amount to be paid</div>
			</c:otherwise>
			</c:choose>
				<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt">${txnInfo.txnAmount}</span></div>
				<div class="clear"></div>
			</div>
		</c:if> --%>
		<%-- headers --%>

		<!-- changes for PGP-3228 by lehar bhandari -->
		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
		<c:if test="${empty hidePaytmCash}">
			<div class="row pt10 smltxt">
				<c:choose>
					<c:when test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges}">
						<div class="fl">Amount to be added</div>
					</c:when>
					<c:otherwise>
						<div class="fl">Total amount to be paid</div>
					</c:otherwise>
				</c:choose>
				<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span></div>
				<div class="clear"></div>
			</div>
		</c:if>
		<!-- end of changes -->
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
			
			
			<div id="showHideWallet" class="${!empty hidePaytmCash ? 'hide' : ''}">	
				<div class="row mb5 rel_pos">
					<div class="fl">
						<input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> /> 
						<span class="rel_pos" <c:if test="${disablePaytmCashCheckbox eq false}">onclick="walletItemClickable(this.previousElementSibling)"</c:if> <c:if test="${disablePaytmCashCheckbox}">style="color:#9a9999;"</c:if>>Paytm</span>
						<div class="clear"></div>
						<!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm)</div> -->
						<div class="bal" id = "yourBal" >(Available Balance  <span class="WebRupee">Rs</span> <span class = 'amt' id = "yourBalSpan"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /></span>)</div>
					    <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
					</div>
					<c:if test="${usePaytmCash && (!disablePaytmCashCheckbox && walletInfo.walletBalance < txnInfo.txnAmount)}">
						<div class="fr abs_pos" id = "walletBalance">
							  <span class="WebRupee">Rs</span>
							 <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /></span>
						</div>
					</c:if>

					<div class="clear"></div>
				</div>

				<c:if test="${disablePaytmCashCheckbox &&  walletInfo.walletBalance < txnInfo.txnAmount}">
					<div id="no-walletTextUpdate" class="failure no-walletTextUpdate" style="font-size:12px;color:#222;padding: 5px;margin-left:0;line-height:17px;width:78%;border:none;background:#f9ffcf;margin-left: 24px;">You do not have sufficient paytm balance for this transaction</div>
				</c:if>
	
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
				 	<c:set var="submitBtnText">Pay Now</c:set>
				 	<%-- for post conv of wallet only --%>
				 	  <div style="display:none;" id="post-conv-wallet-only">
					 	   <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
								<div class="post-conv-inclusion" style="margin-right: 17px; margin-left: 7px;">
									<%@ include file="../../common/post-con/wallet-postconv.jsp" %>
								</div>
							</c:if>
					  </div>

					 <c:if test="${disablePaytmCashCheckbox &&  walletInfo.walletBalance < txnInfo.txnAmount}">
						 <div id="no-walletTextUpdate" class="failure no-walletTextUpdate" style="font-size:12px;color:#222;padding: 5px;margin-left:0;line-height:17px;width:87%;border:none;background:#f9ffcf;margin-left: 24px;">You do not have sufficient paytm balance for this transaction</div>
					 </c:if>

				 	 <form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="walletForm" style = "margin:0;padding:0" onsubmit = "return submitForm()">
						 <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
				    	<input type="hidden" name="submit_count" id="submit_count" value="0" />
				    	<input type="hidden"  name="txnMode" value="PPI" />
						<input type="hidden"  name="channelId" value="WAP" />
						<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
						<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
					    <div style="display: none;" class="fullWalletDeduct" id = "onlyWalletAmt">
							<button name="" type="submit" class="blue-btn">${submitBtnText}</button>
					    </div>
					</form>
				 </c:if>
			 </div>
		</c:if>
		
	   <!-- paytm cash -->
	   
	   <!-- ICICI digital card -->
	   <c:if test="${loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled }">
	   		<div class="row mb5 rel_pos">
				<div class="fl">
					<input name="paytmCC" id="paytmCC" type="checkbox" value="" onchange="processPaytmCC(this)" <c:if test="${paytmCCchecked}">checked="checked"</c:if>  <c:if test="${digitalCreditInfo.digitalCreditInactive}">disabled="disabled"</c:if> />
					<span <c:if test="${digitalCreditInfo.digitalCreditInactive ne true}">onclick="walletItemClickable(this.previousElementSibling)"</c:if><c:if test="${digitalCreditInfo.digitalCreditInactive}">style="color:#9a9999;"</c:if>>Paytm Postpaid</span>
					<div class="clear"></div>
					<div class="bal" id = "paytmCCBal" >(Available Credit  <span class="WebRupee">Rs</span> <span class = 'amt' id = "paytmCCBalSpan"><fmt:formatNumber value="${digitalCreditInfo.accountBalance}" maxFractionDigits="2" minFractionDigits="2" /></span>)</div>
				</div>
				<div class="clear"></div>
				<c:if test="${paytmCCchecked &&  !digitalCreditInfo.digitalCreditInactive}">
					<form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
				    	<input type="hidden"  name="txnMode" value="PAYTM_DIGITAL_CREDIT" />
						<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
						<input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
						<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
						<input type="hidden" name="pccAccountBalance" id="pccAccountBalance" value="0" />
						<input type="hidden" name="storeCardFlag"  value="off" />
						<div class="fl mb5" style="width: 100%; margin-top: 15px;">
							Enter Paytm Passcode
						</div>
						<div class="fl mb5" style="width: 100%">
							<input type="tel" name="PASS_CODE" id="txtPassCode" class="digitalCreditPassCode mask" maxlength="6" style="width: 94%;"/>
						</div>
					<c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage}">
						<div class="error-txt mt10 mb5"> ${digitalCreditInfo.invalidPassCodeMessage}</div>
					</c:if>
					<c:if test="${not empty requestScope.validationErrors['INVALID_PASS_CODE_BLANK']}">
						<div class="error-txt mt10 mb5"> ${requestScope.validationErrors["INVALID_PASS_CODE_BLANK"]}</div>
					</c:if>
							
				    	<div class="btn-submit fl" style="width: 100%;">
							<input class="blue-btn" type="submit" value="Pay Now" name="">
						</div>
					</form>
				</c:if>
			</div>
	   
	   </c:if>
	   
	   <!-- End of ICICI digital card -->
	   
	   <!--  Login/ Sign up content -->
	   <%-- walletonly case : only login page  --%>

		<script>
		var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");
		var detectedChannel="WAP";
		var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));
		var stateVal="state="+authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + jsessionid;
		
		//window.location = "${loginUrl}&"+stateVal;
		
		</script>
		<!--<div>
			<div class="notification alert alert-info blue-text mb10 " id="login-stitch">
			<span class="b">
				<a id="login-btn" href="#" class="underline" onclick="showAuthView('login'); return false;">Login</a> / <a id="register-btn" href="#" class="underline" onclick="showAuthView('register'); return false;">Sign up</a> with Paytm
			</span>
			<span>to use your Patym Cash or Saved cards.</span>
		</div>
		</div> -->

	   <!--  End of login sign up content -->

	   <div class="header clear" id = "otherText">

			   <c:if test="${!disablePaytmCashCheckbox}">
				   <span id="balance">Select an option to pay <c:if test="${usePaytmCash}">balance</c:if></span>
			   </c:if>
			   <c:if test="${disablePaytmCashCheckbox &&  !empty walletInfo.walletBalance && walletInfo.walletBalance >= 0 && walletInfo.walletBalance < txnInfo.txnAmount}">
				   <span id="balance">Please select any other payment method </span>
			   </c:if>


		   <span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}">Rs <span id = "balanceAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></span>
		</div>
	   
	   <div class="header row clear" id = "addMoneyText" style = "display:none">
			<div class="msgTxtBox">To complete payment, add to Paytm </div><b class="fr">Rs <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b>
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
			<img src="${ptm:stResPath()}images/wap/merchantLow/partners-new.png" height="37px" alt="Norton Visa Mastercard PCI DSS" title="Norton Visa Mastercard PCI DSS" />
		</div>
	</div>
	<script type = "text/javascript">
		onPageLoad();
		processPaytmCash();

	</script>
	
	
</body>
</html>
