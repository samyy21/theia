<%--
  User: rajansingh
  Date: 23/02/18
  Time: 7:20 PM
  To change this template use File | Settings | File Templates.
--%>
<style>
    input[type=button] {
        -webkit-appearance: none;
        -moz-appearance:    none;
        appearance:         none;
    }
</style>

<div class="clear"></div>
<div id="UPI_PushSection" class="row mb5 rel_pos ${isUpiPush_show}" style="margin-top:-15px;">

    <c:choose>
        <c:when test="${txnInfo.txnAmount > walletInfo.walletBalance && (!empty param.UPI_KEY && !empty param.txn_Mode && param.txn_Mode eq 'UPI_PUSH') && checkPaytmCashCheckbox && (txnConfig.hybridAllowed || txnConfig.addMoneyFlag)}">
            <input type="hidden" value="${txnInfo.txnAmount - walletInfo.walletBalance}" id="upiPushAmount" />
        </c:when>
        <c:otherwise>
            <input type="hidden" value="${txnInfo.txnAmount}" id="upiPushAmount" />
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${txnConfig.addMoneyFlag  && checkPaytmCashCheckbox}">
            <input type="hidden" value="${sarvatraVpainfo.addMoneyMerchantDetails.merchantVpa}" id="upiPushMerchantUPI_ID" />
        </c:when>
        <c:otherwise>
            <input type="hidden" value="${sarvatraVpainfo.merchantDetails.merchantVpa}" id="upiPushMerchantUPI_ID" />
        </c:otherwise>
    </c:choose>

<c:forEach var="kycBankInfo" items="${sarvatraVpainfo.bankInfo}">

<c:forEach var="sarvatraVpainfo"  items="${sarvatraVpainfo.sarvatraVpaMapInfo}">
	
	<c:if test="${sarvatraVpainfo.key eq kycBankInfo.key }">

			<div class="mb10 clear">

                <c:if test="${!empty param.UPI_KEY && !empty param.txn_Mode && param.txn_Mode eq 'UPI_PUSH'}">

                    <c:set var="upiPushSelected" value="${param.UPI_KEY}"></c:set>

                    <c:set var="upiPushcheckBoxSelected" value="${sarvatraVpainfo.key eq upiPushSelected}"></c:set>

                </c:if>

  <div class="mb10 upiPushInput">

       <label style="display: block; float: left;width: 100%;margin-top: 10px;">
   
    <input type="checkbox" name="upiPush" value="${sarvatraVpainfo.key}" data-href="/theia/jsp/wap/merchantLow5/${formName}.jsp?txn_Mode=UPI_PUSH&UPI_KEY=${sarvatraVpainfo.key}${useWalletQuerystring}&${queryStringForSession}" data-value='${sarvatraVpainfo.value}' onchange="upiPushCardSelect(this)" <c:if test="${upiPushcheckBoxSelected}">checked</c:if>  />
    <span style="top:-3px;position: relative;display: inline-block;">${kycBankInfo.value.bank } AC/NO

        <span class="" style="float: right; position: relative; margin-left: 34px;"><span class="vpaAccountDot" style="position: absolute;top: -17px;right: 34px;">...</span>${kycBankInfo.value.account }</span>

        </span>


<c:if test="${txnInfo.txnAmount > walletInfo.walletBalance && sarvatraVpainfo.key eq upiPushSelected && checkPaytmCashCheckbox}">
       <div class="fr" style="margin-top: 2px;"><span class="WebRupee">Rs</span> <span id="totalAmt"><fmt:formatNumber value="${txnInfo.txnAmount-walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2"/></span></div>
</c:if>
      <div class="pushVPA" style="margin-left:27px;">${sarvatraVpainfo.key}</div></label>




     <div class="clear"></div>
     <div class="upiPushSubmit mb10 mt10">
      <input type="button" value="Pay Now" class="blue-btn cc-blue-btn" onclick="getUpiPushData(this)" />
     </div>
     <div class="clear"></div>
   </div>

  </div>
  <div class="clear"></div>
	
	
			
			</c:if>

</c:forEach>
</c:forEach>

<form id="upiPushForm" name="upiPushForm" autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}">
  <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
  <input type="hidden" name="txnMode" value="UPI" />
  <input type="hidden" name="txn_Mode" value="UPI" />
  <input type="hidden" name="AUTH_MODE" value="USRPWD" />
  <input type="hidden" name="channelId" value="WAP" />
  <input type="hidden" name="walletAmount" value="0" />
  
  <input type="hidden" name="mpin" id="mpin" value="" />
  <input type="hidden" name="deviceId" id="deviceId" value="" />
  <input type="hidden" name="sequenceNumber" id="sequenceNumber" value="" />
    <input type="hidden" name="VIRTUAL_PAYMENT_ADDRESS" id="vpa" value="" />

  <c:if test="${(txnConfig.addMoneyFlag  && checkPaytmCashCheckbox && !txnConfig.hybridAllowed)}">
  <input type="hidden" name="addMoney" value="1" />
  </c:if>
</form>
<script>

   // When UPI PUSH IS Selected
    if(paymentType== 18 && !getParameter('txn_Mode') && getParameter('use_paytmcc') !== "1"){

        var upiPushCheckboxes=document.querySelector('input[name=upiPush]');
        if(upiPushCheckboxes){
            upiPushCheckboxes.click();
        }
    }

   var upiPushChecked="${isUpiPushChecked}";

   if(upiPushChecked == 'true'){
       var upiPushCheckboxes=document.querySelector('input[name=upiPush]');
       upiPushCheckboxes.click();
   }

   var upiPushCheckedBox = document.querySelector('input[name=upiPush]:checked');

   var UPIPush_VPA=document.getElementById("vpa");
if(upiPushCheckedBox && UPIPush_VPA){
   UPIPush_VPA.value=upiPushCheckedBox.value;
}

   if(upiPushCheckedBox){

   //upiPushCheckedBox.parentElement.parentElement;
       var checkboxParent=upiPushCheckedBox.parentElement.parentElement.parentElement;
        if(checkboxParent){
                var currentSubmitBtn=checkboxParent.querySelector("div.upiPushSubmit");

                if(currentSubmitBtn){
                    currentSubmitBtn.style.display='block';
                }
        }

   }

   function getParameter(theParameter) { var params = window.location.search.substr(1).split('&');   for (var i = 0; i < params.length; i++) { var p=params[i].split('='); if (p[0] == theParameter) { return decodeURIComponent(p[1]); } } return false; }

   function upiPushCardSelect(upiPushRadioBtn){

   var  selectedBtnParentBox=upiPushRadioBtn.parentElement.parentElement;

    var pushBtns = $('.upiPushSubmit');
    for(var i=0; i< pushBtns.length;i++){
      pushBtns[i].style.display="none";
    }

     var upiLocation= upiPushRadioBtn.getAttribute("data-href");
      if(upiPushRadioBtn.checked){
          window.location = upiLocation;
      }

      if(!upiPushRadioBtn.checked){
          if("${paymentBankInactive}" === "false" && "${isPPBL_Enabled && (paymentBankInactive eq false)  && ((usePaytmCash && walletInfo.walletBalance < txnInfo.txnAmount && walletInfo.walletBalance + paymentBankAccountBalance >= txnInfo.txnAmount) || (!usePaytmCash && (0 + paymentBankAccountBalance >= txnInfo.txnAmount)))}" === "true"){
              // ppbl enabled
              var ppbl = document.getElementById("paymentBankCheckbox");
              if(ppbl && ppbl.click){
                  ppbl.click();
              }
          }else if("${digitalCreditInfo.digitalCreditInactive}" === "false"){
              // postpaid enabled
              var iciciPostpaid = document.getElementById("paytmCC");
              if(iciciPostpaid && iciciPostpaid.click){
                  iciciPostpaid.click();
              }
          }else{
              // other pay modes
              var otherPayModes = document.getElementById("othermodesLinks");
              if(otherPayModes && otherPayModes.children && otherPayModes.children.length > 0){
                  otherPayModes.children[0].click();
              }
          }
      }
      /* var upiSubmitBtn=selectedBtnParentBox.querySelector("div.upiPushSubmit");
       upiSubmitBtn.style.display='block';
       var vpa=document.getElementById("vpa");
         vpa.value=upiPushRadioBtn.value;
         */

  }

  var upiPushFormSubmitted = false;

  function getUpiPushData(inputBtn){
    // get UPI Push payer Data

   var upiPushInput =inputBtn.parentElement.parentElement.querySelector("input[name=upiPush]");

    var payeeData=upiPushInput.getAttribute("data-value");
    // txn Amount
    var upiPushTxnInput=document.getElementById("upiPushAmount");
    var upiPushTxnAmt=upiPushTxnInput.value;

    var upiPushMIDInput=document.getElementById("upiPushMerchantUPI_ID");
    var upiPushMIDInputVal=upiPushMIDInput.value;

    // For Android Methods
    if(window['HtmlOut']){
          if(upiPushFormSubmitted){
            return false;
          }
          HtmlOut.onPayNowClick(upiPushTxnAmt, payeeData, upiPushMIDInputVal);

          var btns = $('.blue-btn');
          for(var i=0; i< btns.length;i++){
            btns[i].innerHTML="Please wait...";
          }
          formSubmitted = true;

          return false;
    }
      if(!window['HtmlOut']){
          var mpinTxt=document.getElementById("mpin");
          var device=document.getElementById("deviceId");
          var sequenceNumber=document.getElementById("sequenceNumber");
          mpinTxt.disabled=true;
          device.disabled=true;
          sequenceNumber.disabled=true;

          document.forms["upiPushForm"].submit();


      }

  }

  function setMpinAndSubmit(mpin,deviceId,sequenceNo){
    if((mpin && mpin!= undefined) && (deviceId && deviceId!= undefined) ){
    var mpinTxt=document.getElementById("mpin");
    var device=document.getElementById("deviceId");
    var sequenceNumber=document.getElementById("sequenceNumber");
	   
    	  if(mpin){
	     mpinTxt.value=mpin;
	   }
    	  
      if(deviceId){
          device.value=deviceId;
      }
      
      if(sequenceNumber){
    	  sequenceNumber.value=sequenceNo;
      }

        if(mpin && deviceId && sequenceNumber){


            var btns = $('.blue-btn');
            for(var i=0; i< btns.length;i++){
                btns[i].value="Please wait...";
                btns[i].disabled=true
            }
        }

      document.forms["upiPushForm"].submit();

    }
  }

</script>
  <div class="clear"></div>
</div>