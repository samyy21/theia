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
		
		<%String useMinifiedAssets = "N";/*ConfigurationUtil.getProperty("context.useMinifiedAssets");*/ %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow5/mobile.css" />
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow5/mobile.min.css" />
		<% } %>
		
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
		      		<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow5/airtel.css" />
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp' || themeInfo.subTheme eq 'paytmLow'}">
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
		<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
		<script type = "text/javascript">
		 var paymentType = "${paymentType}";
		 var savedCardList = "${!empty savedCardList}";
		 var walletBalance="${walletInfo.walletBalance}";
		 var isSubscription, isSubsPPIOnly;
         var paymentFormUrl = "/theia/jsp/wap/merchantLow5/subscriptionForm.jsp";


         function isNumber(e){
             var charCode = (e.which) ? e.which : e.keyCode;
             if (charCode > 31 && (charCode < 48 || charCode > 57)) {
                 e.preventDefault();
                 return false;
             }
             return true;
         };

         // When insufficient Balance (Paytm Wallet || Paytm PostPaid || Paytm Bank Account)
         function insufficientBlance(context){
             var elems = $(context.children[0]),
                 isActive = false;
             if(elems && elems.length > 0){
                 isActive = elems[0].style.display === "inline-block";
                 hideTooltip();
                 if(!isActive){
                     elems[0].style.display = "inline-block";
                     context.previousElementSibling.style.display="inline-block";
                 }else{
                     elems[0].style.display = "none"
                     context.previousElementSibling.style.display="none";
                 }
             }
         }

         function hideTooltip(){
             var tooltips = document.getElementsByClassName("tooltiptext"),
                 arrowIcons = document.getElementsByClassName("arrowIcon");

             if(tooltips){
                 for(var i =0 ;i < tooltips.length ; i++){
                     var tooltip = tooltips[i];
                     if(tooltip.style.display !== "none"){
                         tooltip.style.display = "none";
                     }
                 }
             }

             if(arrowIcons){
                 for(var i =0 ;i < arrowIcons.length ; i++){
                     var arrowIcon = arrowIcons[i];
                     if(arrowIcon.style.display !== "none"){
                         arrowIcon.style.display = "none";
                     }
                 }
             }
         }

         function attachEventonTooltip(){
             document.body.addEventListener("click",function(event){
                 var className = event.target.className;

                 if(className !== "insufficientIcon" && className !== "tooltiptext"){
                     hideTooltip();
                 }
             });



             document.body.addEventListener("touchstart",function(event){
                 var className = event.target.className;

                 if(className !== "insufficientIcon" && className !== "tooltiptext"){
                     hideTooltip();
                 }
             });
         }


         function processPaymentBank(chechbox){

             var paytmWallet = document.getElementById("paytmCashCB");
             var queryStr = "";
             var queryStringForSession = "${queryStringForSession}";
             var form = document.getElementById("paymentBankFormId");
             var transactionAmt = Number("${txnInfo.txnAmount}");
             var pbAccountBalance = document.getElementById("paymentBankBalSpan").innerText;

             if(pbAccountBalance && pbAccountBalance.length > 0){
                 pbAccountBalance = Number(pbAccountBalance.replace(",",""));
             }else{
                 pbAccountBalance = 0;
             }

             if(paytmWallet && paytmWallet.checked){
                 queryStr += "?use_wallet=1";
             }else{
                 queryStr += "?use_wallet=0";
             }

             if(chechbox.checked) {
                 queryStr += "&use_payment_bank=1";
             }else{
                 queryStr += "&use_payment_bank=0";
             }

             // display form when pb checkbox checked
             if(form){
                 form.style.display = chechbox.checked ? "block" : "none";
             }

             // when paytm_wallet and payment bank have sufficient balance and user
             // click payment bank as options
             if(paytmWallet && paytmWallet.checked && chechbox.checked && parseInt(walletBalance) >= transactionAmt){
                 queryStr = queryStr.replace("use_wallet=1","use_wallet=0");
             }

             queryStr += "&" + queryStringForSession;
             location.href = paymentFormUrl + queryStr;
         }
		
		 function processPaytmCash(checkbox) {
			 
			 var isClicked = true;
			 if(!checkbox) {
				 checkbox = $("#paytmCashCB")[0];
				 isClicked = false;
			 }
			 
			 var totalAmount = Number($("#totalAmtVal")[0].value);
			 isSubscription = $('#config')[0].getAttribute('data-subscription') == 'true' ? true : false;
			 isSubsPPIOnly = $('#config')[0].getAttribute('data-subs-ppi-only') == 'Y' ? true : false;
			
			 // no wallet
			if(checkbox == null){
				if(isSubscription && totalAmount == 0){
					showSubsMsg();
				}
				return;
			}
			
			
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
						location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?use_wallet=1" + "&${queryStringForSession}";
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
						
						if(isSubscription){
							
							if(totalAmount == 0 && !isSubsPPIOnly && (window.subscriptionPaymentMode !== "NORMAL"))
								$("#showHideWallet").hide();
							
							if(isSubsPPIOnly){
								$('#walletForm').show();
							}
							
							showSubsMsg();
						}
						
					} else {
						paidAmount = totalAmount - walletBalance;
						paidAmount = Math.round(paidAmount * 100) / 100;
						
						walletAmountToUse = walletBalance; 
					}
					
					if(addMoney){
						$('.header').hide();
						if(isSubsPPIOnly){
							$('#subs-ppi-only-complete-payment').show();
						} else {
							$("#addMoneyText").show();
						}
						$("#addMoneyAmt")[0].innerHTML = paidAmount;
						walletAmountToUse = walletBalance;
						
						if(($("#CC-form").length ||$("#DC-form").length )&& !isSubsPPIOnly){
							$("#saveCard")[0].setAttribute("disabled", "disabled")
						}
					}
					
					
					$("#yourBal").show();
					// show remaining balance
					
					
					var amt = (walletBalance - remWalletBalance);
					amt = Math.round(amt * 100) / 100;
					$("#walletBalanceAmt")[0].innerHTML = amt;
					$("#walletBalance")[0].style.display = "inline";
					

				} else {					
					if(isClicked) {
						location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?use_wallet=0" + "&${queryStringForSession}";
					}
					
					
					if(isSubscription){
						
						showSubsMsg();
					}
					
					
					walletAmountToUse = 0;
					// show user balance
					
					
				}
	
				// set hidden field
				var input = document.getElementsByName("walletAmount");
				for(var i=0; i < input.length; i++){
					input[i].value = walletAmountToUse;
				}
					
				if($("#balanceAmt") && $("#balanceAmt")[0]){
                    $("#balanceAmt")[0].innerHTML = paidAmount;
                }

				//$("#finalAmt")[0].innerHTML = paidAmount;

			}
		 
		 
		 function showSubsMsg(){
			 	$('.header').hide();
			 	
				// cc tab
				var btn = $('.cc-blue-btn')[0];
				if($("#CC-form").length && btn){
					btn.parentElement.appendChild($("#subs-min-amt-msg").show()[0]);
					$("#saveCard")[0].setAttribute("disabled", "disabled")
				}
				if($("#DC-form").length && $('.dc-blue-btn')[0]){
					btn=$('.dc-blue-btn')[0];
					btn.parentElement.appendChild($("#subs-min-amt-msg").show()[0]);
					$("#saveCard")[0].setAttribute("disabled", "disabled")
				}
				
				// if saved cards are available
				if($('.SC-tab').length){
					$("#subs-select-card").show();
					
					// sc-tab
					$(".cvv-box").hide();
					
				} else if(isSubsPPIOnly) {
					$("#subs-only-full-wallet").show();
				} else {
					$("#subs-save-card").show();
				} 
		 }

         //object to send data to app
         var resBankObj;
         //object to send bank code and payment type to app
         var bankObj = {
             bnkCode : '',
             payType : '',
             cardScheme : ''
         };
         var curcard = '';
         var curcardschme = '';
         var curcardtype = '';

		 //function to send bank code and payment type to app
		function getBankDetails(){
		   // console.log(binObject.info);
            if(binObject.info && binObject.info.binDetail !== {} && binObject.info.binDetail !== undefined)
			{
			    if(paymentTypeId && (paymentTypeId == 'CC' || paymentTypeId == 'DC' || paymentTypeId == 'EMI' ))
			    {
                    bankObj.bnkCode = binObject.info.binDetail.bankCode;
                    bankObj.cardScheme = binObject.info.binDetail.cardName;
                    bankObj.payType = binObject.info.binDetail.cardType;
                }
			}
			else if(binObject.info.binDetail !== {} && paymentTypeId && paymentTypeId == 'SC' )
			{
                bankObj.bnkCode = curcard;
                bankObj.cardScheme = curcardschme;
                bankObj.payType = curcardtype;
            }
			else if(binObject.info.binDetail !== {} && paymentTypeId && paymentTypeId == 'NB' )
			{
                bankObj.bnkCode = $("#bankCode")[0].value ? $("#bankCode")[0].value : '';
                bankObj.cardScheme = '';
                bankObj.payType = 'NB';
            }

            if(bankObj.payType === 'CREDIT_CARD' || bankObj.payType === 'credit_card')
                bankObj.payType = 'CC';
            else if(bankObj.payType === 'DEBIT_CARD' || bankObj.payType === 'debit_card')
                bankObj.payType = 'DC';

			resBankObj = JSON.stringify(bankObj);

            if(window['Android'])
                Android.sendBnkDtlToApp(resBankObj);

		};

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

            //to be called by APP to get bank code and paymode
            if(paymentTypeId && (paymentTypeId == 'CC' || paymentTypeId == 'DC' || paymentTypeId == 'NB' ||paymentTypeId == 'SC' ||paymentTypeId == 'EMI' ))
                getBankDetails();
            else if(paymentTypeId == "")
            {
                if(document.getElementsByName("txn_Mode").length != 0 && document.getElementsByName("txn_Mode") != [])
                    paymentTypeId = document.getElementsByName("txn_Mode")[0].value;
                else if(document.getElementsByName("txnMode").length != 0 && document.getElementsByName("txnMode").length != [])
                    paymentTypeId = document.getElementsByName("txnMode")[0].value;

                getBankDetails();
            }


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
                    if(cvvElm && cvvElm.name!=""){
                        var label = curSavedCard.querySelector("label[for=" + cvvElm.name + "]");

                        if(label && label.classList.contains("red-text"));
                        label.classList.remove("red-text");
                    }

                    if(cvvElm && cvvElm.disabled) {
                        isCCCvvValid=true;
                        return true;
                    }

                    if(cvvElm)
                    {
                        var cvv = cvvElm.value;
                        isCCCvvValid = (((cvv) && !isNaN(cvv) && (cvv.length == 3 || cvv.length == 4))  || (curSavedCard.querySelector("div[id=CARDSCHEME]").getAttribute("data-value")=="MAESTRO"));
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

                    // skip cvv validations if subscriptions full wallet
                    if(isSubscription && cvvElm && cvvElm.parentElement){
                        if(cvvElm.parentElement.style.display === "none"){
                            isCCCvvValid = true;
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
                if(cardNumber && cardNumber.replace(/\s/g, '').length >=12 && !(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;
                if(cardNumber && !checkLuhn(cardNumber.replace(/\s/g, ''))) {
                    toggleLuhnErr(false, form);
                    return false;
                }
                if(!isNaN(removeSpaceFromSentence(cardNumber)) && cardNumber && cardNumber.length)
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

                if (form.elements['card-number'].classList.contains("maestro")) {
                    isCCExpMonthValid = true;
                }

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

                if (form.elements['card-number'].classList.contains("maestro")) {
                    isCCExpYearValid = true;
                }

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
                if(cardNumber && cardNumber.replace(/\s/g, '').length >=12 && !(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;
                if(cardNumber && !checkLuhn(cardNumber.replace(/\s/g, ''))) {
                    toggleLuhnErr(false, form);
                    return false;
                }
                if(!isNaN(removeSpaceFromSentence(cardNumber)) && cardNumber && cardNumber.length)
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


            //Expiry Month
            if(form.elements['card-number'] && !form.elements['card-number'].classList.contains("maestro")) {
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
               // window.location = src;
            }

        }

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
                // $('#login-iframe')[0].setAttribute('src', src);
                window.location = src;
            }

        }

		 function checkLogin(){
            var iframe = $("#login-iframe")[0];
             var isWalletOnly = $("#other-details")[0].getAttribute("data-wallet-only");
             var isOTPLoginAvailable=$("#isOtpLoginAvailable").length ? true : false;
             if(isWalletOnly && ("${loginInfo.loginFlag}" == "false")){
                 setTimeout(function(){
                     $("#login-wait").hide();
                     $("#login-stitch").show();

                     if(isWalletOnly == "true" && parseInt("${loginInfo.loginRetryCount }")<1)

                         if(isOTPLoginAvailable){
                             showAuthOTPView()
                         }
                         else
                         {
                             showAuthView();
                         }
                 }, 1000);
             }
            if(!iframe) { // already logged in
                $("#login-wait").hide();
                $("#login-stitch").show();
                setTimeout(function(){
                    $('.alert-success').hide();
                }, 5000);
                return false;
            }

			var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));

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

		 function onPageLoad(){
			 
			//Visbility Other Payment Mode
			var modeLinks = $("#othermodesLinks");
			var modeLinksLen = modeLinks.length ? modeLinks[0].getElementsByTagName("a").length : 0;
			if(modeLinksLen < 1){
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
		 };
		 
		 
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
			 location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
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

		 function getCardType(e){if(e.length==19||e.substring(0,4)=="6220"||e.substring(0,6)=="504834"||e.substring(0,6)=="508159"||e.substring(0,6)=="589458"){return"maestro"}else if(e.substring(0,2)=="30"||e.substring(0,2)=="36"||e.substring(0,2)=="38"||e.substring(0,2)=="35"){return"diners"}else if(e.substring(0,2)=="34"||e.substring(0,2)=="37"){return"amex"}else if(e.substring(0,1)=="4"){return"visa"}else if(e.substring(0,2)=="51"||e.substring(0,2)=="52"||e.substring(0,2)=="53"||e.substring(0,2)=="54"||e.substring(0,2)=="55"){return"master"}else if(e.substring(0,4)=="6011"){return"DISCOVER"}else{return"INVALID CARD"}};
		 
		 function onEMIBankSelect(select){
			 location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value + "&${queryStringForSession}";
		 }
		 
		 function onEMIPlanSelect(checkbox){
			 location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?txn_Mode=EMI&emi_plan_id=" + checkbox.value + "&${queryStringForSession}";
		 }
		 
		 function doCancel(){
			ajax.send("/theia/cancelTransaction?${queryStringForSession}", callback, "GET");
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
		 
		 function showSubsDetails(btn){
			$(btn).hide();
			$("#subs-details").show(); 
			return false;
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
		

		function onCardDown(isCC,event){
            var cardNumberEle = document.getElementById('card-number');
            if(cardNumberEle){
                cardNumberEle.value = cardNumberEle.value.replace(/[^0-9 ]/g,'');
                var cardNo = removeSpaceFromSentence(cardNumberEle.value);
                if(event && event != undefined)
                {
                var formNm = $(event)[0].target.closest("form");
                toggleLuhnErr(true, formNm);
                }
                checkBinDetails(cardNo,isCC, formNm);
			}
		}

		function removeSpaceFromSentence(sentence){
            return sentence.split('').filter(function(val){return val!=' '}).join('');
		}

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
            } else {
                disableMasetroCVV(false);
			}
            // pass bin level object to respective functionalities
            setTimeout(binFunctionalities(value,isCC),500);
		}

		function disableMasetroCVV(disableMaestro) {
            var exp_month = document.getElementsByName("expiryMonth");
            var exp_year = document.getElementsByName("expiryYear");
            var cvv_Inp = document.getElementById("normalOptionCVVTxt");
            var ccCvvBox = document.getElementById("ccCvvBox");
            var inputFld = document.getElementById("card-number");
            var maestro_msg = $("#maestro_msg");

            if(disableMaestro && inputFld && inputFld.value && inputFld.value.replace(/\s/g, '').length >= 6) {
                if(cvv_Inp) cvv_Inp.disabled = "disabled";
                if(ccCvvBox) ccCvvBox.disabled = "disabled";

                if(exp_month && exp_month.length) exp_month[0].disabled = "disabled";
                if(exp_year && exp_year.length) exp_year[0].disabled = "disabled";
                if(maestro_msg) maestro_msg.addClass("highlight");
            } else {
                if(cvv_Inp) cvv_Inp.disabled = "";
                if(ccCvvBox) ccCvvBox.disabled = "";

                if(exp_month && exp_month.length) exp_month[0].disabled = "";
                if(exp_year && exp_year.length) exp_year[0].disabled = "";
                if(maestro_msg) maestro_msg.removeClass("highlight");

            }
		}

		function binFunctionalities(value,isCC){
            var disableMaestro = inputMaxLength();
            showPromoError(value);
            if(!isCC)
                iciciIdebitBin(value);

			cardLowSuccesRateBin(value);
            changeCardTypeIcon(value);
            if(!(document.getElementById("DCtxnMode") && document.getElementById("DCtxnMode").value=='DC' && binObject.info.binDetail && binObject.info.binDetail.iDebitEnabled)) {
                disableMasetroCVV(disableMaestro);
            }
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
            if(!Elem){
                // undefined check
                return;
            }

            var name = Elem.name;
            if(!name)
                return false;

            var label = Elem.parentElement.querySelector("label[for=" + name + "]");
            label.classList.remove("red-text");
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

		var paymentTypeId = "${paymentTypeId}";
		function changeCardTypeIcon(value, formNm) {
            // remove existing icon if any
            var allIcons = ['master','maestro','visa','discover','bajajfn','rupay','amex','diners','INVALID_CARD']
            // remove all icons from the list
            allIcons.forEach(function (cardType) {
                $('#card-number').removeClass(cardType);
            });
            var currentCardType = 'INVALID_CARD';
            var ifCC = paymentTypeId && paymentTypeId == 'CC' ? true : false;
            if(value.length>=6 && binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.cardName) {
                currentCardType = binObject.info.binDetail.cardName.toLowerCase();
            }
            $('#card-number').addClass(currentCardType);

            //change cvv icon for amex cards
            if(currentCardType !== 'INVALID_CARD' && currentCardType === 'amex'){
                $('#defaultCvvIcon').addClass('hide');
                $('#amexCvvIcon').removeClass('hide');
            }
            else if(currentCardType !== 'INVALID_CARD' && currentCardType !== 'amex' || (value.length<6 && !ifCC))
            {
                $('#defaultCvvIcon').removeClass('hide');
                $('#amexCvvIcon').addClass('hide');
            }

            if(value.length<6 && ifCC)
            {
                $('#amexCvvIcon').addClass('hide');
                $('#defaultCvvIcon').addClass('hide');
			}


            return currentCardType;
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
                    dcCvv1 && (dcCvv1.maxLength = cvvLength);
                    dcCvv2 && (dcCvv2.maxLength = cvvLength);
                }
            } else {
                if(inputFld){
                    inputFld.maxLength = 19;
                    if(cvvFld) {
                        cvvFld.maxLength = 3;
                    }
                    else {
                        dcCvv1 && (dcCvv1.maxLength = 3);
                        dcCvv2 && (dcCvv2.maxLength = 3);
                    }
                }
            }
            return disableCvv;
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

         function changeAttribute(addOrRemove){
             $('.idebitDC').forEach(function(el){
                 if(addOrRemove)
                     el.setAttribute('disabled','disabled');
                 else
                     el.removeAttribute('disabled');
             });
         }

         function cvvTopBottomToggle(isDisplayed) {
             // hide/show and disable/enable cvv box
             toggleCVVTopBottomIdebit(isDisplayed);
             // if this is displayed,
             optionBasedCVVCheck(isDisplayed);

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


         function onSaveCardTabClick(event,tab){
             var constant = {
                     "SAVE_CARD_QRY":"row-dot sc-row",
                     "INP_RADIO_QRY":"input[type='radio']",
                     "TAB_DETAIL_QRY":"[id^=cvv_][id$=_pad]"
                 },
                 savedCards = document.getElementsByClassName(constant.SAVE_CARD_QRY),
                 inputRadioEle = tab.querySelector(constant.INP_RADIO_QRY),
                 tabDetail = tab.querySelector(constant.TAB_DETAIL_QRY);

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

             if(tab.children.savedCardId) {
                 showSavedCard(tab.children.savedCardId, tab.children.savedCardId.id);
             }
             curcard = tab.children.BNKCODE ? tab.children.BNKCODE.getAttribute("data-value"):'';
             curcardschme = tab.children.CARDSCHEME ? tab.children.CARDSCHEME.getAttribute("data-value"):'';
             curcardtype = tab.children.CURCARDTYPE ? tab.children.CURCARDTYPE.getAttribute("data-value"):'';

         }

         function initPaymentBank(){
             var pbCheckbox = document.getElementById("paymentBankCheckbox"),
                 pbForm = document.getElementById("paymentBankFormId"),
                 pbSubmit = document.getElementById("pbSubmit"),
                 queryParams = location.search.replace("?","").split("&"),
                 isPaytmCCEnable = location.search.indexOf("use_paytmcc=1") !== -1,
                 numberRegex = /^[0-9]*$/,
                 invalidPassCodeMsg = '${requestScope.validationErrors["INVALID_PASS_CODE_BLANK"]}';

             // when icici debit card check
             if(isPaytmCCEnable){
                 if(pbCheckbox && pbForm){
                     pbCheckbox.checked = false;
                     pbForm.style.display = "none";
                 }
                 return;
             }


             // pb chechbox and pbForm exists
             if(pbCheckbox && pbForm){
                 if(queryParams.length > 0){
                     for(var index = 0; index < queryParams.length ; index++){
                         var pair = queryParams[index].split("=");
                         if(pair[0] === "use_payment_bank"){
                             pbCheckbox.checked = pair[1] === "1" ? true:false;
                         }
                     }
                 }

                 // when use click other payment options
                 if(location.search.search("use_payment_bank") === -1 && location.search.search("txn_Mode") !== -1){
                     pbCheckbox.checked = false;
                 }

                 if(pbCheckbox.checked){
                     // show form
                     pbForm.style.display = "block";
                 }else{
                     // hide form
                     pbForm.style.display = "none";
                 }
             }

             // disabled payment bank submit button
             if(pbSubmit){
                 pbSubmit.disabled = true;
             }

             // when invalid passcode msg present in other pay mode
             if(invalidPassCodeMsg.length){
                 if(pbCheckbox && pbForm){
                     pbCheckbox.checked = false;
                     pbForm.style.display = "none";
                 }
             }

             var walletAmount = document.getElementsByName("walletAmount");
             var walletEle = document.getElementById("paytmCashCB");
             var bal = Number("${walletInfo.walletBalance}");
             if(walletAmount.length && walletEle){
                 for(var index =0;index<walletAmount.length;index++){
                     walletAmount[index].value = walletEle.checked ? bal: 0;
                 }
             }

             if(walletEle && !walletEle.checked){
                 var pbankBalance = Number("${savingsAccountInfo.effectiveBalance}");
                 var txtAmt = Number("${txnInfo.txnAmount}");
                 if(pbankBalance < txtAmt){
                     if(pbCheckbox && pbForm){
                         pbCheckbox.disabled = true;
                         pbCheckbox.checked = false;
                         pbForm.style.display = "none";
                     }
                 }
             }



             // add event
             var inp = document.getElementById("paymentBankTxtPassCode");
             if(inp){
                 inp.addEventListener("keyup",function(e){
                     if(!numberRegex.test(e.target.value)){
                         e.target.value = e.target.value.replace(/[^\d]/g,'');
                         return;
                     }
                     if(pbSubmit){
                         var value = Number(e.target.value);
                         if(e.target.value && e.target.value.length === 4 && (value === value)){
                             pbSubmit.disabled = false;
                             pbSubmit.className = "blue-btn";
                         }else{
                             pbSubmit.disabled = true;
                             pbSubmit.className = "gry-btn";
                         }
                     }
                 });
             }



         }


		</script>
	</head>

<body>
	<%@ include file="../../common/common.jsp"%>
	<div id="txnTransientId" value="${txnInfo.txnId}"></div>
	<c:set var="formName" value="subscriptionForm"></c:set>
	<c:set var="isSubscriptonFlow" value="true"></c:set>
	<c:set  var="loginUrl" value="${loginInfo.oAuthInfoHost}/oauth2/authorize?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}" ></c:set>
	<div id="config" data-addmoney="${txnConfig.addMoneyFlag}" data-walletonly="${onlyWalletEnabled}" data-subscription="true" data-subs-ppi-only="${txnInfo.subscriptionPPIOnly}"></div>
	<div id="error-details" 	
			data-maintenancemsg="${messageInfo.maintenanceMessage}"
			data-lowerrormsg="${messageInfo.lowPercentageMessage}">
	</div>
	<c:if test="${csrfToken.token != null}">
		<c:set var="varCSRF" value="${csrfToken.token}" scope="session"></c:set>
	</c:if>
	<div id="csrf-token" data-token = "${varCSRF}"></div>
	<%-- walletonly case : only login page  --%>

	<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>
	<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-wallet-only="${onlyWalletEnabled}"></div>
	<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
	<div id="isOtpLoginAvailable" data-value="${loginInfo.loginWithOtp}"></div>
	<!-- container -->
	<div>
	
		<!-- header -->
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'paytmApp'}">
		      </c:when>
		      <c:when test="${themeInfo.subTheme eq 'videocon'}">
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
			<c:if test="${ empty showLoginNotification}">
				<c:set var="showLoginNotification" value="${walletInfo.walletEnabled && empty loginInfo.user}" scope="session"></c:set>
			</c:if>
			<c:if test="${showLoginNotification eq true}">
				<c:set var="showLoginNotification" value="false" scope="session"></c:set>
				<div class="failure">Login with Paytm to use your Paytm and saved cards.</div>
			</c:if>
			
			<c:if test="${txnInfo.retry}">
				<div class="failure" id="retryMsg">${txnInfo.displayMsg}</div>
			</c:if>
			
			<c:if test="${!empty txnInfo.offerMessage}">
				<div class="failure" id="offerMsg">${txnInfo.offerMessage}</div>
			</c:if>
			
			<c:if test="${walletInfo.walletFailed}">
				<div class="failure">
					<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					<c:if test="${onlyWalletEnabled}">
						<a href="cancelTransaction?${queryStringForSession}" class="cancel" onclick="return doCancel()">Cancel</a>
					</c:if>
				</div>
			</c:if>
			<c:if test="${walletInfo.walletInactive}">
				<div class="failure">
					<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
				</div>
			</c:if>
			<c:if test="${onlyWalletEnabled && !txnConfig.addMoneyFlag && walletInfo.walletBalance < txnInfo.txnAmount}">
				<div class="failure">
					You have insufficient balance for this transaction.
					<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel" onclick="return doCancel()">Cancel</a>
				</div>
            </c:if>
            
            <c:if test ="${!empty errorMsg && !loginInfo.loginFlag}">
               <div class="failure">
                   <c:out value="${errorMsg}" escapeXml="true" />
                 </div>
            </c:if>

		</div>
        <!-- notifications -->
        

		
		<%@ include file="login.jsp" %>
		
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
		
		
		<c:set var="isSubscription" value="true"/>
		<c:if test="${isSubscription}">
			
			
			<%-- mode subs-default  --%>
			<c:if test="${usePaytmCash && isInsufficientBalance}">
				<c:set var="selectedModeType" value="SUBS_DEFAULT"/>
			</c:if>
			
			<%-- mode subs-full-wallet  --%>
			<c:if test="${usePaytmCash && !isInsufficientBalance}">
				<c:set var="selectedModeType" value="SUBS_FULL_WALLET"/>
			</c:if>
			
		</c:if>

        <script>
            var isSubscriptionFullWallet = "${selectedModeType}" === "SUBS_FULL_WALLET";
        </script>

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
		<%-- <c:if test="${walletInfo.walletBalance eq 0 && selectedModeType ne 'ADD_MONEY'}">
			<c:set var="hidePaytmCash" value="true"/>
		</c:if> --%>
			
		
		<%-- headers --%>
		<div class="header row hide" id = "paytmCashText" >
			Uncheck Paytm to pay using other options.
		</div>

		
		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
		<%-- <c:if test="${empty hidePaytmCash}"> --%>	
			<div class="row pt20">
				<c:set var="merchantName" value="${fn:toUpperCase(merchantInfo.merchantName)}"></c:set>
				<c:set var="subsHeaderText" value="SUBSCRIPTION ON "></c:set>
				<c:if test="${themeInfo.subTheme eq 'airtel'}">
					<c:set var="subsHeaderText" value=""></c:set>
				</c:if>
				<div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span></div>
				<div class="clear"></div>

                    <div class="clear">
                        <div class="small mt10 mb5 fl" id="subs-details" style="width:40%; margin-top:13px; height:40px; position: relative;">

                        <div id="merchant-logo" class="mt10 mr20 mb20" style="position: absolute; top:-57px;">

                            <c:choose>
                                <c:when test="${merchInfo.useNewImagePath eq true}">
                                    <img src="${merchInfo.merchantImage}" alt="" height="40"/>
                                </c:when>
                                <c:otherwise>

                                    <img src="${ptm:stResPath()}images/web/merchant/${merchInfo.merchantImage}" alt="" height="40"/>
                                </c:otherwise>
                            </c:choose>
                        </div>


                        <div class="b" style="color:#000; ">Payment Schedule</div>
                        <span>Payment Interval : ${txnInfo.subscriptionFrequency}&nbsp;${txnInfo.subscriptionFrequencyUnit}</span><br>
                        </div>



                        <div class="fr mt5" style="font-size: 12px; text-align: right; color: #9a9999; margin-top:11px;">
                        <div style="color: #000; font-size: 13px;"> Recurring Amount</div>
                            <div>Amount Type: &nbsp;${txnInfo.subscriptionAmountType}</div>
                            <div>Maximum Amount: &nbsp;${txnInfo.subscriptionMaxAmount}</div>
                        </div>
                        <div class="clear"></div>
                    </div>
			</div>
		<%-- </c:if> --%>
		<%-- headers --%>

		<!-- paytm wallets included -->
		<%@ include file="wallets_Subscription.jsp" %>
	   <!-- <c:if test="${txnInfo.subscriptionPaymentMode eq 'CC' || txnInfo.subscriptionPaymentMode eq 'DC'}">
		<div class="header" id = "otherText">
			<span id="balance">Select an option to complete payment for subsequent subscription transactions.<c:if test="${usePaytmCash}">balance</c:if></span> <span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}"><span class="WebRupee">Rs</span> <span id = "balanceAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></span>
		 </div>
	   </c:if> -->

		
		<div class="header row hide" id="subs-select-card">
			<span>Select and save your 
				<c:if test="${txnInfo.subscriptionPaymentMode eq  'CC'}">
					<b class="b">Credit Card</b> 
				</c:if>
				<c:if test="${txnInfo.subscriptionPaymentMode eq  'DC'}">
					<b class="b">Debit Card</b> 
				</c:if>
				for subsequent transactions.
			</span>
		</div>
		
		<div class="header row hide" id="subs-save-card">
			<span>Save your 
			<c:if test="${txnInfo.subscriptionPaymentMode eq  'CC'}">
					<b class="b">Credit Card</b> 
				</c:if>
				<c:if test="${txnInfo.subscriptionPaymentMode eq  'DC'}">
					<b class="b">Debit Card</b> 
				</c:if>
			 for subsequent transactions.</span>
			<c:if test="${!empty txnInfo}">
				<span class="grey-text small show mt6">(<span class="WebRupee">Rs</span> <span class="saveCardAmount">${txnInfo.subscriptionMinAmount}</span> will be charged on your card to verify details)</span>
			</c:if>
		</div>
		
		<!-- <div class="header row hide" id="subs-only-full-wallet">
			For subsequent transaction, amount will be debited from your Paytm
		</div> -->
	   
	   <div class="header row hide" id="subs-ppi-only-complete-payment">
		   <b class="fr"><span class="WebRupee">Rs</span> <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b>
			Select an option to add money in <b class="b">Paytm Wallet</b> and complete payment<br>
			For subsequent transaction, amount will be debited from your Paytm Wallet
		</div>
		
		<div class="header row hide" id = "addMoneyText">
			<b class="fr"><span class="WebRupee">Rs</span> <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b>
			Select an option to add money in <b class="b">Paytm Wallet </b> and complete payment for subsequent subscription transactions
		</div>
		
		
		
		
		
		<c:if test="${!empty txnInfo}">
			<span id="subs-min-amt-msg" class="mt10 hide" >(<span class="WebRupee">Rs</span> <span class="saveCardAmount">${txnInfo.subscriptionMinAmount}</span> will be charged on your card to verify details)</span>
		</c:if>
	   
	   
	   <%-- promocode logic --%>
		<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
		 
		 		<div id="promoType" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}"></div>
		 		<div id="promoNbList" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.nbBanks}"></div>
		 		<div id="promoCardList" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.cardBins}"></div>
		 		<div id="promoPaymentModes" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.paymentModes}"></div>
		 		<div id="promoCardTypeList" data-value="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}"></div>
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
		<c:if test="${(selectedModeType eq 'ADD_MONEY' or selectedModeType eq 'SUBS_FULL_WALLET' or selectedModeType eq 'SUBS_DEFAULT') && ((txnInfo.subscriptionPaymentMode eq 'PPI' && walletInfo.walletBalance < txnInfo.txnAmount) || (txnInfo.subscriptionPaymentMode eq 'NORMAL'))}">
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
		<div id="partner-logos" style="text-align: center;">
			<img src="${ptm:stResPath()}images/wap/merchantLow5/partners.gif" alt="Norton Visa Mastercard PCI DSS" title="Norton Visa Mastercard PCI DSS" />
		</div>
    </div>
    <script>
        var subscriptionPaymentMode = "${txnInfo.subscriptionPaymentMode}";
        var subscriptionPPIOnly = "${txnInfo.subscriptionPPIOnly}";
    </script>
    <script type = "text/javascript">
		onPageLoad();
		processPaytmCash();
        var paymentBankEnabled = "${isPPBL_Enabled}";
        if(paymentBankEnabled === "true"){
            initPaymentBank();
        }
	</script>
	
	
</body>
</html>