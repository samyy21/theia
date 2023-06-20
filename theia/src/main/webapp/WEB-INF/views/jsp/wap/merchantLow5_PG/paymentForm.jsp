<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ include file="../../common/config.jsp" %>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<c:set var="isUPI_Push" value="${(entityInfo.upiPushEnabled || entityInfo.addUpiPushEnabled)}"></c:set>

<c:if test="${isUPI_Push}">

    <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && empty param.use_wallet && empty param.use_paytmcc && walletInfo.walletBalance < txnInfo.txnAmount}">
        <c:set var="paymentType" value="18"></c:set>
    </c:if>

    <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && param.use_wallet eq 0 && param.use_payment_bank eq 0 && paymentType eq 18}">
        <c:set var="paymentType" value="2"></c:set>
    </c:if>

</c:if>


<head>
	<title>Paytm Secure Online Payment Gateway</title>
	<meta name="robots" content="noindex,nofollow" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Expires" content="-1" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
	<!-- 		<base href="/" />	 -->
	<%String useMinifiedAssets = ConfigurationUtil.getProperty("context.useMinifiedAssets");%>

	<% if(useMinifiedAssets.equals("N")){ %>
	<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow5/mobile.css" />
	<% } else { %>
	<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/wap/merchantLow5/mobile.min.css" />
	<% } %>

	<c:choose>
		<c:when test="${themeInfo.subTheme eq 'airtel'}">
            <style>

                .blue-btn {
                    background-color: #ef1c23;
                }

                .logo {
                    background: #ef1c23;
                    border: none;
                }

            </style>
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
.pay-mode-tab .heading{text-align: center;}

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
        var paymentTypeId = "${paymentTypeId}";
        var curcard = '';
        var curcardschme = '';
        var curcardtype = '';
        var savedCardList = "${!empty savedCardList}";
        var walletBalance="${walletInfo.walletBalance}";
        var cancelUrl = "/theia/cancelTransaction";
        var paymentFormUrl = "/theia/jsp/wap/merchantLow5/paymentForm.jsp";
        var basePath="${ptm:stResPath()}";


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
			 location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
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
        function showFieldError(form,Elem)
        {
            if(!Elem) return;
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

                    var cvvL = 4;
                    if(curSavedCard.querySelector("div[id=CARDSCHEME]") && curSavedCard.querySelector("div[id=CARDSCHEME]").getAttribute("data-value") !== "AMEX")
                        cvvL = 3;

                    if(cvvElm)
                    {
                        var cvv = cvvElm.value;
                       // isCCCvvValid = (((cvv) && !isNaN(cvv) && (cvv.length == 3 || cvv.length == 4))  || (curSavedCard.querySelector("div[id=CARDSCHEME]").getAttribute("data-value")=="MAESTRO"));
                        isCCCvvValid = (((cvv) && !isNaN(cvv) && (cvv.length == cvvL))  || (curSavedCard.querySelector("div[id=CARDSCHEME]").getAttribute("data-value")=="MAESTRO"));

                    }
                    else
                        isCCCvvValid = true;

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
                if(cardNumber && cardNumber.replace(/\s/g, '').length >=12 && !(binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;
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
                if(cardNumber && cardNumber.replace(/\s/g, '').length >=12 && !(binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;
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

            if(binObject && binObject.info && binObject.info.binDetail.iDebitEnabled && form.elements['isIciciIDebit'][1].checked) {
                cvvElm = form.elements['idebitOptionCVVTxt'];
            } else if(binObject && binObject.info && binObject.info.binDetail.iDebitEnabled) {
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
        function validateEmiCCForm() {
            var form = document.getElementById('emiForm');
            var isBajajFin = form.querySelector("select[id=emiBankSelect]").value === "BAJAJFN" ? true : false;

            //for card number
            var isEMINumberValid = false;
            var cardInput = form.elements['card-number'];
            if (cardInput) {
                var cardNumber = cardInput.value;
                if (cardNumber && cardNumber.replace(/\s/g, '').length >= 12 && !(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;
                if (cardNumber && !checkLuhn(cardNumber.replace(/\s/g, ''))) {
                    toggleLuhnErr(false, form);
                    return false;
                }

                if (!isNaN(removeSpaceFromSentence(cardNumber)) && cardNumber && cardNumber.length)
                    isEMINumberValid = true;
                else
                    isEMINumberValid = false;
            }
            else
                isEMINumberValid = false;

            if (!isEMINumberValid) {
                showFieldError(form, cardInput);
                return false;
            }
            else
                removeFieldError(form, cardInput);

            if(form.elements['card-number'] && !form.elements['card-number'].classList.contains("maestro"))
            {
            	//for expiry month
            	var monthElm = form.elements['emiExpMnth'];
            	var month = monthElm.value;
            	var isEMIExpMonthValid = month == "0" ? false : true;

                if (form.elements['card-number'].classList.contains("maestro")) {
                    isEMIExpMonthValid = true;
                }

            	if (!isEMIExpMonthValid) {
                	showFieldError(form, monthElm);
                	return false;
            	}
            	else
                	removeFieldError(form, monthElm);


            //for expiry year
            var yearElm = form.elements['emiExpYear'];
            var year = yearElm.value;
            var isEMIExpYearValid = year == "0" ? false : true;

            if (form.elements['card-number'].classList.contains("maestro")) {
                isEMIExpYearValid = true;
			}

            if (!isEMIExpYearValid) {
                showFieldError(form, yearElm);
                return false;
            }
            else
                removeFieldError(form, yearElm);

            if((new Date()).getFullYear() == year && ((new Date()).getMonth() +1) > parseInt(month)) {
                    showFieldError(form, monthElm);
                    showFieldError(form, yearElm);
                    return false;
			}

        }

            //for cvv
            var isEMICvvValid;
            var cvvElm = form.elements['emiCVVBox'];
            if(cvvElm && !isBajajFin)
            {
                var cvv = cvvElm.value;
                isEMICvvValid = (((cvv) && !isNaN(cvv) && (cvv.length == cvvElm.maxLength)) || form.elements['card-number'].classList.contains("maestro")) ? true : false;
            }
            else if(cvvElm && isBajajFin)
                isEMICvvValid = true;
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

        //bin validation
        var binObject ={
					firstSix: '',
					info: {}
				};
			function checkBinDetails(value){
				//console.log('inside check bin details',value);
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
							binFunctionalities(value);
					    });
				    }
			    }
				// pass bin level object to respective functionalities
				setTimeout(binFunctionalities(value),500);
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

        //for postpaid pay now
        function initPostpaid(){

            var numberRegex = /^[0-9]*$/;

            // add event
            var inppost = document.getElementById("txtPassCode");
            if(!inppost) return;
            var postSubmit = document.getElementById("postSubmit");

            //disable postpaid pay button
            if(postSubmit){
                postSubmit.disabled = true;
            }


            if(inppost){
                inppost.addEventListener("keyup",function(e){
                    if(!numberRegex.test(e.target.value)){
                        e.target.value = e.target.value.replace(/[^\d]/g,'');
                        return;
                    }
                    if(postSubmit){
                        var value = Number(e.target.value);
                        if(e.target.value && e.target.value.length === 4 && (value === value)){
                            postSubmit.disabled = false;
                            postSubmit.className = "blue-btn";
                        }else{
                            postSubmit.disabled = true;
                            postSubmit.className = "gry-btn";
                        }
                    }
                });
            }
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
                queryStr += "&use_payment_bank=1&use_paytmcc=0";
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

        function processPaytmCC(checkbox){
            //console.log('inside process paytmcc');
            //change redirection
            var paytmWallet = document.getElementById("paytmCashCB");
            var form = document.getElementById("paytmCCFormId");
            var pCCBalanceFld = document.getElementById("paytmCCBalSpan");
            var pCCBalance = 0;

            if(pCCBalanceFld) pCCBalance = pCCBalanceFld.innerText;
            var transactionAmt = Number("${txnInfo.txnAmount}");
            var queryStringForSession = "${queryStringForSession}";
            var queryStr = "";
            var isClicked = true;

            if(!checkbox) {
                checkbox = $("#paytmCC")[0];
                isClicked = false;
            }
            if(checkbox == null) return;

            if(pCCBalance && pCCBalance.length > 0){
                pCCBalance = Number(pCCBalance.replace(",",""));
            }else{
                pCCBalance = 0;
            }
            if(paytmWallet && paytmWallet.checked){
                queryStr += "?use_wallet=1";
            }else{
                queryStr += "?use_wallet=0";
            }
            if(checkbox.checked){
                if(isClicked){
                    location.href = paymentFormUrl + "?use_paytmcc=1" + "&${queryStringForSession}";
                }
            }
            else{
                location.href = paymentFormUrl + "?use_paytmcc=0" + "&${queryStringForSession}";
            }
            if(checkbox.checked){
                if(isClicked){
                    queryStr += "&use_paytmcc=1&use_payment_bank=0";
                }
            }
            else{
                queryStr += "&use_paytmcc=0&use_payment_bank=0";
            }
            // display form when pb checkbox checked
            if(form){
                form.style.display = checkbox.checked ? "block" : "none";
            }
            // when paytm_wallet and payment bank have sufficient balance and user
            // click payment bank as options
            if(paytmWallet && paytmWallet.checked && checkbox.checked && parseInt(walletBalance) >= transactionAmt){
                queryStr = queryStr.replace("use_wallet=1","use_wallet=0");
            }
            queryStr += "&" + queryStringForSession;
            location.href = paymentFormUrl + queryStr;
            // make paytm cashbox checked to unchecked


            // make payment mode such that no other payment modes are opened
        }
        function processPaytmCash(checkbox) {
            var paytmBankInp = document.getElementById("paymentBankCheckbox");
            var paytmCCInp = document.getElementById("paytmCC");

            var isHybridAllowed = "${txnConfig.hybridAllowed}" === "true";
            //var paymentType = "${paymentType}";
            var insufficientBal = false;
            var bal = Number("${walletInfo.walletBalance}");
            var txtAmt = Number("${txnInfo.txnAmount}");
            var txn_Mode;
            insufficientBal = bal < txtAmt;

            switch(paymentType) {
                case "1": txn_Mode = "&txn_Mode=CC";
                    break;
                case "2": txn_Mode = "&txn_Mode=DC";
                    break;
                case "3": txn_Mode = "&txn_Mode=NB";
                    break;
                case "5": txn_Mode = "&txn_Mode=SC";
                    break;
                case "12": txn_Mode = "&txn_Mode=COD";
                    break;
                case "13": txn_Mode = "&txn_Mode=EMI";
                    break;
                case "15": txn_Mode = "&txn_Mode=UPI";
                    break;
                case "18": var upiChecked = document.querySelector("input[name='upiPush']:checked");
                    if(upiChecked) txn_Mode = "&txn_Mode=UPI_PUSH&UPI_KEY="+upiChecked.value;
                    break;
            }

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
            var qryString = "";
            if(txn_Mode && isHybridAllowed && insufficientBal) qryString += txn_Mode;

            if(paytmBankInp) {
                if(paytmBankInp.checked) qryString = "&use_payment_bank=1&use_paytmcc=0";
            }

            if(paytmCCInp) {
                if(paytmCCInp.checked) qryString = "&use_paytmcc=1&use_payment_bank=0";
            }

            if (checkbox.checked) {
                if(isClicked){
                    location.href = paymentFormUrl + "?use_wallet=1" + qryString + "&${queryStringForSession}";
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
                    if($("#addMoneyAmt").length > 0){
                        $("#addMoneyAmt")[0].innerHTML = paidAmount;
                    }
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
                    location.href = paymentFormUrl + "?use_wallet=0" + qryString + "&${queryStringForSession}";

                }

                walletAmountToUse = 0;
                // show user balance

            }

            // set hidden field
            var input = document.getElementsByName("walletAmount");
            if(input.length)
                input[0].value = walletAmountToUse;
            if($("#balanceAmt").length > 0){
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

        function onPageLoad(){

            //Visbility Other Payment Mode
            var modeLinksLen = $("#othermodesLinks");
            if(modeLinksLen[0])
                var modeLinksLen = modeLinksLen[0].getElementsByTagName("a");

            if(modeLinksLen.length === 0){
                $("#otherpaymentModeHeading").hide();
            }

            // setup if promocode is applied
            if($("#promoPaymentModes").length > 0 && $("#promoPaymentModes")[0].getAttribute('data-value'))
                setupPromocode();

            // track page load
			/*try {
			 trackEvent("onload");
			 } catch(e){};*/
            if("${loginInfo.loginFlag}" == "false" && "${loginInfo.autoLoginAttempt}" =="true") {
                // checkLogin();
            }

            if("${loginInfo.loginFlag}" == "false" && "${loginInfo.autoLoginAttempt}" =="false") {

                // $("#login-stitch")[0].style.display="block";
                // $("#login-wait")[0].style.display="none";
            }
            checkCookieSupport();
            // for bajaj finserv checks
            if("${emiEnabled}" === "true"){
                bajajFinservChanges();
            }

            initPostpaid();
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

        function setupPromocode() {

            var promoNbStr = $("#promoNbList")[0].getAttribute("data-value");
            var promoNbList = promoNbStr ? promoNbStr.split(',') : [];

            var promoModeStr = $("#promoPaymentModes")[0].getAttribute("data-value");
            var promoPaymentModes = promoModeStr ? promoModeStr.split(',') : [];

            // check to show all modes
            var promoShowAllModes = $("#promoShowAllModes")[0].getAttribute("data-value");
            if (promoShowAllModes == "1") {
                //showAllPaymentModes();
                $(".promocode-options-msg-2").removeClass('hide');
                $(".promocode-options-msg").addClass('hide');
                return false;
            }

            // hide all payment mode links and tabs

            var promoTypeval = $("#promoType")[0].getAttribute("data-value");

            if (promoTypeval != "CASHBACK") {
                $(".pay-mode-link").hide();
                $(".pay-mode-tab").addClass('hide');
                $("#showHideWallet").hide();
            }


            // show available modes
            var showSavedCards = false;
            for (var i = 0; i < promoPaymentModes.length; i++) {
                $("#" + promoPaymentModes[i] + "-link").show();
                $("." + promoPaymentModes[i] + "-tab").show();

                // show saved cards for cc dc
                if (promoPaymentModes[i] == "CC" || promoPaymentModes[i] == "DC")
                    showSavedCards = true;
            }

            // OPEN CC (OPEN PAYMENT MODE IF USER HAS PROMO CASHBACK)

            //var promoTypeval=$("#promoType")[0].getAttribute("data-value");
            if ($('.SC-tab').length == 0 && $('#SC-link').length == 0) {

                // for (var i = 0; i < promoPaymentModes.length; i++) {
                //     // show saved cards for cc dc
                //     if (promoPaymentModes[i] == "CC") {
                //         $("#" + promoPaymentModes[i] + "-link")[0].click();
                //         break;
                //     }
                //     else if (promoPaymentModes[i] == "DC") {
                //         $("#" + promoPaymentModes[i] + "-link")[0].click();
                //         break;
                //     }

                // }

            }


            // check to show saved cards mode
            // disable non promo save cards
            if (showSavedCards) {

                var show = true;
                var promoCardList = $("#promoCardList")[0].getAttribute("data-value");// BIN list

                if ($('.sc-row').length && $('.promo-valid-card').length == 0) {
                    show = false;
                }

                if (show) {
                    $('#SC-link').show();
                    $('.SC-tab').removeClass("hide");
                }
            }
        }

        function showAllPaymentModes(){
            location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?showAll=1" + "&${queryStringForSession}";
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
        var binObject ={
            firstSix: '',
            info: {}
        };
        var selectedFormEle = null;
        function checkBinDetails(value,isCC, formNm){
            // value should be of string type
            if(!(typeof value === 'string' || value instanceof String))
                return ;

            var txnMode = "";
            selectedFormEle = formNm;
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
                            if (typeof data === "string"  && data.indexOf('<html') !== -1)
                                return;
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


        //object to send data to app
        var resBankObj;
        //object to send bank code and payment type to app
        var bankObj = {
            bnkCode : '',
            payType : '',
            cardScheme : ''
        };

        //function to send bank code and payment type to app
		function getBankDetails(){
		   // console.log(binObject.info);
            if(binObject && binObject.info && binObject.info.binDetail !== {} && binObject.info.binDetail !== undefined)
			{
			    if(paymentTypeId && (paymentTypeId == 'CC' || paymentTypeId == 'DC' || paymentTypeId == 'EMI' ))
			    {
                    bankObj.bnkCode = binObject.info.binDetail.bankCode;
                    bankObj.cardScheme = binObject.info.binDetail.cardName;
                    bankObj.payType = binObject.info.binDetail.cardType;
                }
			}
			else if(binObject && binObject.info && binObject.info.binDetail !== {} && paymentTypeId && paymentTypeId == 'SC' )
			{
                bankObj.bnkCode = curcard;
                bankObj.cardScheme = curcardschme;
                bankObj.payType = curcardtype;
            }
			else if(binObject && binObject.info && binObject.info.binDetail !== {} && paymentTypeId && paymentTypeId == 'NB' )
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


        function binFunctionalities(value,isCC){
            var disableMaestro = inputMaxLength();
            showPromoError(value);
            var isIdebitEnabled = document.getElementById('payment-details').getAttribute('data-idebit-ic');
            //if(isIdebitEnabled == true || isIdebitEnabled == 'true'){
            if(!isCC)
                iciciIdebitBin(value);
            //}
            cardLowSuccesRateBin(value);
            changeCardTypeIcon(value);
            if(!(document.getElementById("DCtxnMode") && document.getElementById("DCtxnMode").value=='DC' && binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.iDebitEnabled)) {
                disableMasetroCVV(disableMaestro);
            }
           binCardErrorMsg(value,selectedFormEle);
        }

        function binCardErrorMsg(value,form){

            if(!form){
                // if form ele is not defined
                return;
            }

            if(binObject && binObject.info && binObject.info.binDetail){
                var errorMsg = binObject.info.cardStatusMessage;
                var errorMsgEle = form.querySelector(".card-bin-disable-error-msg");
                var ele = form.querySelector(".card-bin-disable");

                var isCardLength = false;
                if(value !== undefined && value !== null){
                    if(value.length < 6){
                        isCardLength = true;
                    }
                }
                if((errorMsg === null) || isCardLength){
                    // card enabled
                    if(ele){
                        ele.className += " hide";
                    }
                    if(errorMsgEle){
                        errorMsgEle.innerText = "";
                    }
                }else{
                    if(value.length === 0){
                        return;
                    }
                    // card disabled
                    if(ele){
                        ele.className = ele.className.replace(/hide/g,"").replace(/^\s+|\s+$/gm,'');
                    }
                    if(errorMsgEle){
                        errorMsgEle.innerText = errorMsg;
                    }
                }
            }
        }

        function disableMasetroCVV(disableMaestro) {
            var exp_month = document.getElementsByName("expiryMonth");
            var exp_year = document.getElementsByName("expiryYear");
            var cvv_Inp = document.getElementById("normalOptionCVVTxt");
            var ccCvvBox = document.getElementById("ccCvvBox");
            var emiCvvBox = document.getElementById("emiCVVBox");
            var inputFld = document.getElementById("card-number");
            var maestro_msg = $("#maestro_msg");

            if(disableMaestro && inputFld && inputFld.value && inputFld.value.replace(/\s/g, '').length >= 6) {
                if(cvv_Inp) cvv_Inp.disabled = "disabled";
                if(ccCvvBox) ccCvvBox.disabled = "disabled";
                if(emiCvvBox) emiCvvBox.disabled = "disabled";

                if(exp_month && exp_month.length) exp_month[0].disabled = "disabled";
                if(exp_year && exp_year.length) exp_year[0].disabled = "disabled";
                if(maestro_msg) maestro_msg.addClass("highlight");
            } else {
                if(cvv_Inp) cvv_Inp.disabled = "";
                if(ccCvvBox) ccCvvBox.disabled = "";
                if(emiCvvBox) emiCvvBox.disabled = "";

                if(exp_month && exp_month.length) exp_month[0].disabled = "";
                if(exp_year && exp_year.length) exp_year[0].disabled = "";
                if(maestro_msg) maestro_msg.removeClass("highlight");

            }
        }

        function inputMaxLength() {
            var inputFld, ccCvvFld, dcCvv1, dcCvv2,emiCvvFld;
            inputFld = document.getElementById("card-number");
            cvvFld = document.getElementById("ccCvvBox");

            if(!cvvFld) {
                dcCvv1 = document.getElementById("normalOptionCVVTxt");
                dcCvv2 = document.getElementById("idebitOptionCVVTxt");
                emiCvvFld = document.getElementById("emiCVVBox");
            }
            var cvvLength = 4;
            var disableCvv = false;

            if(inputFld && binObject && binObject.info && binObject.info.binDetail && binObject.info.binDetail.cardName) {
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
                    emiCvvFld && (emiCvvFld.maxLength = cvvLength);
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
                        emiCvvFld && (emiCvvFld.maxLength = 3);
                    }
                }
            }
            return disableCvv;
        }

        function showPromoError(value) {

            var $m = $("#wrong-promo-card-msg");
            var $cn = $("#card-number");

            if(value.length>=6 && binObject && binObject.info && binObject.info.promoResultMessage && binObject.info.promoResultMessage.length > 0) {
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
        function converterLowSuccessMsg(issuerLowSuccessRate,cardSchemeLowSuccessRate,issuerName, schemeName){
            lowSuccessRateMsg = document.getElementById("error-details").getAttribute("data-lowerrormsg");
            if(cardSchemeLowSuccessRate)
                lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD',schemeName+ " cards");
            else
                lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD',issuerName + " bank");
            return lowSuccessRateMsg;
        }

        function cardLowSuccesRateBin(value,formNm){
            if(value.length>=6 && binObject && binObject.info && (binObject.info.issuerLowSuccessRate || binObject.info.cardSchemeLowSuccessRate)){
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
            if(value.length>=6 && binObject && binObject.info && binObject.info.binDetail && (binObject.info.binDetail.iDebitEnabled
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

                // BIN list
                var promoBins = JSON.parse($("#promoBins")[0].getAttribute("data-value"));

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
                if(!isCardValid && promoBins.indexOf(parseInt(number.substring(0,6))) != -1) {
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
            var isHybridAllowedForEMI = "${txnConfig.hybridAllowed}" === "true",
                BAJAJ_FIN_SERV = "BAJAJFN",
                NONE = "none",
                url = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?txn_Mode=EMI&emi_bank=" + select.value,
                sessionQry = "&${queryStringForSession}",
                paytmWallet = document.getElementById("paytmCashCB");



            if(isHybridAllowedForEMI && paytmWallet){
                if(select.value === BAJAJ_FIN_SERV && paytmWallet.checked){
                    // when bajaj finserv is selected and wallet is checked
                    // uncheck wallet
                    url += "&use_wallet=0";
                }else if(select.value !== BAJAJ_FIN_SERV && !paytmWallet.checked){
                    // when other bank is selected and wallet is unchecked
                    // checked wallet
                    url += "&use_wallet=1";
                }
            }
            location.href = url + sessionQry;
        }


        function bajajFinservChanges(){
            var isEMIMode = location.search.search("txn_Mode=EMI") !== -1,
                cvvBlock,cvvInp,emiform,isBajajFinservBank,DEFAULT_CVV_VALUE,
                emiBankDropDownElm = document.getElementById("emiBankSelect");

            if(isEMIMode){
                emiform = $("#emiForm");
                cvvBlock = document.getElementsByClassName("cvv");
                cvvInp = undefined;
                isBajajFinservBank = location.search.search("emi_bank=BAJAJFN") !== -1;
                DEFAULT_CVV_VALUE = "111";
                bankSelectEle = $("#emiBankSelect");

              
                if(emiform && emiform[0]){
                    cvvInp = emiform[0].querySelector('#emiCVVBox');
                }

                if(cvvInp){
                    // set cvv input to default when bajaj finserv is selected
                    cvvInp.value = isBajajFinservBank ? DEFAULT_CVV_VALUE : "";

                    // set element to disabled
                    if(isBajajFinservBank){
                        cvvInp.disabled = true;
                    }
                }

                if(cvvBlock && cvvBlock.length > 0){
                    // hide cvv box when bajaj finserv is selected
                    cvvBlock[0].style.display = isBajajFinservBank ? "none":"block";
                }
            }
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

            var isWalletOnly = $("#other-details")[0].getAttribute("data-wallet-only");


            if(isWalletOnly && ("${loginInfo.loginFlag}" == "false")){
                setTimeout(function(){

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

            if(iframe){
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
 
        function walletItemClickable(element){
            if(element && element.click){
                element.click();
            }
        }

        function checkRadioEl(){
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


<script>

    var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");
    var detectedChannel="WAP";
    var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));
    var stateVal="state="+authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + jsessionid;

</script>
<%-- walletonly case : only login page  --%>
<%--<c:if test="${!loginInfo.loginFlag}">--%>
	<%--<script>--%>
        <%--var jsessionid = $("#other-details")[0].getAttribute("data-jsessionid");--%>
        <%--var detectedChannel="WAP";--%>
        <%--var authConfig = JSON.parse($("#auth-config")[0].getAttribute('data-value'));--%>
        <%--var stateVal="state="+authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + jsessionid;--%>


        <%--var isOTPLoginAvailable=$("#isOtpLoginAvailable").length ? true : false;--%>

        <%--if(isOTPLoginAvailable){--%>

            <%--var client_id = authConfig.client_id;--%>
            <%--var wap_client_id = authConfig.wap_client_id;--%>
            <%--var eid = authConfig.eid;--%>
            <%--var txnTransientId = $("#txnTransientId")[0].getAttribute("value");--%>
            <%--var loginType = "MANUAL";--%>

            <%--var detectedChannel = "WAP";--%>
            <%--var loginData="loginData=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + detectedChannel + ":" + txnTransientId + ":" + jsessionid + ":" + loginType;--%>
            <%--var loginURL="${loginUrl}&"+loginData;--%>
            <%--window.location =loginURL;--%>

        <%--}else{--%>
            <%--window.location = "${loginUrl}&"+stateVal;--%>
        <%--}--%>






	<%--</script>--%>
<%--</c:if>--%>

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
		<c:when test="${themeInfo.subTheme  eq 'Charges'}">
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

		<div id="tlsWarnMsgId" class="failure hide"></div>
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
			<%-- Don't set to previous payment type when wallet is selected and paytm cc and payment bank enabled --%>
			<c:choose>
				<c:when test="${sessionScope.previousPaymentType eq 16  && param.use_wallet eq 1 && saveCardEnabled}">
					<c:set var="paymentType" value="5"></c:set>
				</c:when>
				<c:when test="${sessionScope.previousPaymentType eq 16  && param.use_wallet eq 1 && dcEnabled}">
					<c:set var="paymentType" value="2"></c:set>
				</c:when>
				<c:when test="${sessionScope.previousPaymentType eq 16  && param.use_wallet eq 1 && ccEnabled}">
					<c:set var="paymentType" value="1"></c:set>
				</c:when>
			</c:choose>
		</c:if>
	</c:if>
	<c:if test="${param.use_wallet eq 0}">
		<c:set var="usePaytmCash" value="false" scope="session"></c:set>
		<%-- restore old paymentType --%>
		<c:if test="${empty param.txn_Mode && sessionScope.previousPaymentType ne 18}">
			<c:set var="paymentType" value="${sessionScope.previousPaymentType}"></c:set>
		</c:if>
	</c:if>



    <%@ include file="paytmWallets.jsp"%>



	<div class="header clear" id = "otherText" style="display:none;">
		<c:choose>
			<c:when test="${paymentType eq 17 || paymentType eq 16}">
				<!-- when payment is fulfilled by either paytm wallet or with payment bank -->
			</c:when>
			<c:when test="${!disablePaytmCashCheckbox || !(!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance < txnInfo.txnAmount)}">
					<span id="balance">Select an option to pay
						<c:if test="${usePaytmCash}">balance</c:if>
					</span>
				<span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}">
						<span class='WebRupee'>Rs</span>
						<span id = "balanceAmt">
							<fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" />
						</span>
					</span>
			</c:when>
			<c:when test="${disablePaytmCashCheckbox &&  !empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance < txnInfo.txnAmount}">
				<span id="balance">Please select any other payment method </span>
				<span class="fr ${!empty hidePaytmCash || usePaytmCash ? '' : 'hide'}">
						<span class='WebRupee'>Rs</span>
						<span id = "balanceAmt">
							<fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" />
						</span>
					</span>
			</c:when>
			<c:otherwise>
				<!-- -->
			</c:otherwise>
		</c:choose>
	</div>

	<c:if test="${paymentType ne 17}">
		<div class="header row clear" id = "addMoneyText" style = "display:none">
			<div class="msgTxtBox">Choose another option to complete the payment </div><b class="fr"><span class='WebRupee'>Rs</span> <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b>
			<%-- <div class="msgTxtBox">To complete payment, add to Paytm </div><b class="fr"><span class='WebRupee'>Rs</span> <span id = "addMoneyAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b> --%>
			<div class="clear"></div>
		</div>
	</c:if>

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
		<div id="promoBins" data-value="${txnInfo.promoBins}"></div>


		<div class="failure promocode-options-msg">
			<p>
					${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
			</p>
		</div>

		<div class="failure promocode-options-msg-2 hide">

			<p>
					${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
			</p>

		</div>

	</c:if>


    <c:if test="${isUPI_Push && isUpiPush_show}">

        <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && param.use_wallet eq 0 && param.use_payment_bank eq 0 && paymentType eq 18}">
            <c:set var="paymentType" value="2"></c:set>
        </c:if>

        <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && param.use_wallet eq 1 && param.use_payment_bank eq 0 && paymentType eq 18 && walletInfo.walletBalance  < txnInfo.txnAmount && saveCardEnabled ne true}">
            <c:set var="paymentType" value="2"></c:set>
        </c:if>

        <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && param.use_wallet eq 1 && param.use_payment_bank eq 0 && paymentType eq 18 && walletInfo.walletBalance  < txnInfo.txnAmount && saveCardEnabled eq true}">
            <c:set var="paymentType" value="5"></c:set>
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

<!-- <div class="footer">
	<div id="partner-logos" align="middle">

        <div style="    text-align: center;
    color: #666;
    vertical-align: middle;
    padding: 7px;">  <span style="    display: inline-block;
    top: -9px;
    position: relative;
    margin-right: 10px; font-size:12px;">Powered By </span><img src="${ptm:stResPath()}images/paytm_logo.png" alt="Paytm Payments" style="height: 30px;" title="Paytm Payments" /></div>


		<%--<img src="${ptm:stResPath()}images/wap/merchantLow5/partners-new.png" alt="Norton Visa Mastercard PCI DSS" title="Norton Visa Mastercard PCI DSS" />--%>
	</div>
</div> -->
<script type = "text/javascript">
    onPageLoad();
    processPaytmCash();
    var paymentBankEnabled = "${isPPBL_Enabled}";
    if(paymentBankEnabled === "true"){
        initPaymentBank();
    }
    attachEventonTooltip();
</script>

<script>
    // tls warning message show hide
    function tlsWarningMsg(){
        var ele = document.getElementById("tlsWarnMsgId");
        var isAddNPayAllow = "${txnConfig.addAndPayAllowed}" === "true";
        var tlsWarnMsg = "${messageInfo.merchantTLSWarnMsg}";
        var tlsWarnMsgAddNPay = "${messageInfo.addAndPayTLSWarnMsg}";
        var walletEle = document.getElementById("paytmCashCB");
        var isAddMoneyAllowed = false;
        if(walletEle && walletEle.checked && isAddNPayAllow){
            isAddMoneyAllowed = true;
        }
        if(ele){
            if(isAddMoneyAllowed || tlsWarnMsg.length !== 0){
                ele.className = ele.className.replace(/hide/g,"");
                if(tlsWarnMsg.length > 0 ){
                    ele.innerText = tlsWarnMsg;
                }else if(tlsWarnMsgAddNPay.length > 0){
                    ele.innerText = tlsWarnMsgAddNPay;
                }else {
                    ele.innerText = "";
                }
            }else{
                ele.className += " hide";
            }
        }
    }

    tlsWarningMsg();
</script>

</body>
</html>
