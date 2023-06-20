<%@ page session="false"%>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Merchant Check Out Page</title>
    <meta name="GENERATOR" content="Evrsoft First Page">
    <script type="text/javascript" src="/theia/resources/js/jquery-1.10.1.min.js"></script>
    <script type="text/javascript">
        function calculateCheckSum() {
            document.forms[0].action = 'CheckSum';
            document.forms[0].submit();
        }

        function assignChecksum() {
            var order = document.getElementById("orderid");
            var orderId = order.value + parseInt(Math.random() * 1000000);
            order.value = orderId;
        }

        function payInIframe() {
            document.getElementsByTagName("form")[0].target = "myiframe";
            document.getElementsByName('myiframe')[0].style.display = "block";

            Paytm = {
                payment_response : null
            };

            checkForPaymentResponse(function(res) {
                document.getElementById('payment-response-msg').innerHTML = "Payment response : "
                    + res;
            });

            function checkForPaymentResponse(callback) {
                var cb = callback;
                var sid = setInterval(function() {
                    if (Paytm.payment_response) {
                        cb(Paytm.payment_response);
                        clearInterval(sid);
                    }
                }, 200)
            }
            ;

        }

        function validateForm(){
            /* var orderId = document.getElementById("orderid").value;

             if(orderId === undefined || orderId == null || orderId.length <= 0){
             alert("order id can not be blank")
             return;
             }

             var transAmount = document.getElementsByName("TXN_AMOUNT")[0].value;
             if(transAmount === undefined || transAmount == null || transAmount.length <= 0){
             alert("Amount can not be blank")
             return;
             }

             var requestType = document.getElementsByName("REQUEST_TYPE")[0].value; */

            /* if(requestType == "SUBSCRIBE"){
             var startDate = document.getElementsByName("SUBS_START_DATE")[0].value;

             if(startDate === undefined || startDate == null || startDate.length <= 0){
             alert("Start date can not be blank")
             return;
             }

             var today = new Date();
             startDate = Date.parse(startDate)

             if(startDate < today){
             alert("Subscription date should be greator or equal then current date")
             } else {
             document.getElementById("target").submit();
             }
             } else {
             document.getElementById("target").submit();
             } */
            document.getElementById("target").submit();
        }

        function validateNativeForm(){
            document.getElementById("nativeForm").submit();
        }

        /*
         * Test page modification script to enter payment flow
         *
         */
        function loadValuesInForm(encodedString){
            var splitArray=encodedString.split('&');
            try{
                splitArray.forEach(function(element){
                    var a=element.split('=');
                    if(element!='')
                        document.getElementsByName(a[0])[0].value=a[1];
                });
            }
            catch(ex){
                alert('payment flow string not proper. Reenter again');
            }
        }

        function extractValueInForm(){
            var string="";
            $('#target input').each(function(t){
                if($(this).attr('type')!='button'){
                    if($(this).attr('name')!='ORDER_ID') //dont copy for order id
                        string +=$(this).attr('name') + '=' + $(this).val() + '&';
                }
            });
            localStorage.setItem("paymentFlowString",string);
            return string;
        }

        function copyValueInForm(){
            var formString=extractValueInForm();
            $('#inputContainingTextToBeCopied').val(formString);
            var copyDiv = document.getElementById('inputContainingTextToBeCopied');
            copyDiv.style.display = 'block';
            copyDiv.focus();
            document.execCommand('SelectAll');
            var response=document.execCommand("Copy", false, null);
            // console.log('response',response);
            copyDiv.style.display = 'none';
            if(response){
                alert('Payment Flow has been copied to clipboard. Use ctrl(cmd)+v to paste');
            }
            else{
                alert('Some issue in copying. Report issue to manu.pandit@paytm.com with browser version you are using');
            }
        }

        function parsePaymentBeanToFetchParams(copyString){

            var paymentBeanText=$('#popUpBeanTextArea').val();
            var mapping = {};

            mapping["checksumhash"] = "CHECKSUMHASH";
            mapping["orderId"]="ORDER_ID";
            mapping["theme"]="THEME";
            mapping["loginTheme"]="LOGIN_THEME";
            mapping["custId"]="CUST_ID";
            mapping["mid"]="MID";
            mapping["industryTypeId"]="INDUSTRY_TYPE_ID";
            mapping["channelId"]="CHANNEL_ID";
            mapping["txnAmount"]="TXN_AMOUNT";
            mapping["authMode"]="AUTH_MODE";
            mapping["website"]="WEBSITE";
            mapping["ssoToken"]="SSO_TOKEN";
            mapping["paymentModeOnly"]="PAYMENT_MODE_ONLY";
            mapping["disabledPaymentMode"]="PAYMENT_MODE_DISABLE";
            mapping["mobileNo"]="MSISDN";
            mapping["email"]="EMAIL";
            mapping["merchantKey"]="merchantKey";
            mapping["promoCampId"]="PROMO_CAMP_ID";
            mapping["requestType"]="REQUEST_TYPE";
            mapping["subscriptionID"]="SUBSCRIPTION_ID";
            mapping["subscriptionServiceID"]="SUBS_SERVICE_ID";
            mapping["subscriptionAmountType"]="SUBS_AMOUNT_TYPE";
            mapping["subscriptionMaxAmount"]="SUBS_MAX_AMOUNT";
            mapping["subscriptionFrequency"]="SUBS_FREQUENCY";
            mapping["subscriptionFrequencyUnit"]="SUBS_FREQUENCY_UNIT";
            mapping["subscriptionStartDate"]="SUBS_START_DATE";
            mapping["subscriptionGraceDays"]="SUBS_GRACE_DAYS";
            mapping["subscriptionEnableRetry"]="SUBS_ENABLE_RETRY";
            mapping["subscriptionRetryCount"]="SUBS_RETRY_COUNT";
            mapping["subscriptionExpiryDate"]="SUBS_EXPIRY_DATE";
            mapping["storeCard"]="STORE_CARD";
            mapping["savedCardID"]="SAVED_CARD_ID";
            mapping["subsPaymentMode"]="SUBS_PAYMENT_MODE";
            mapping["connectiontype"]="CONNECTION_TYPE";
            mapping["subsPPIOnly"]="SUBS_PPI_ONLY";
            mapping["deviceId"]="DEVICE_ID";
            mapping["deviceSource"]="DEVICE_SOURCE";
            mapping["goodsInfo"]="GOODS_INFO";
            mapping["shippingInfo"]="SHIPPING_INFO";
            mapping["walletAmount"]="WALLET_AMOUNT";
            mapping["paytmToken"]="PAYTM_TOKEN";
            mapping["creditCardBillNo"]="CC_BILL_NO";
            mapping["emiOption"]="EMI_OPTIONS";
            mapping["address1"]="ADDRESS_1";
            mapping["pincode"]="PINCODE";
            mapping["isSavedCard"]="IS_SAVED_CARD";
            mapping["isAddMoney"]="addMoney";
            mapping["passCode"]="PASS_CODE";
            mapping["paymentTypeId"]="PAYMENT_TYPE_ID";

            if(paymentBeanText==''){
                alert('Payment String is empty. Re-enter');
            }
            else{
                var length=paymentBeanText.length;
                paymentBeanText=paymentBeanText.substr(1,length-2);
                //console.log(paymentBeanText);
                var splitArray=paymentBeanText.split(',');
                try{
                    splitArray.forEach(function(element){
                        var a=element.split('=');
                        if(mapping[a[0].trim()] && a[1]!=""){
                            //console.log(a[0],",",mapping[a[0].trim()]);
                            document.getElementsByName(mapping[a[0].trim()])[0].value=a[1];
                        }
                    });
                }
                catch(ex){
                    alert('Payment bean string not proper.Please re-enter.');
                }
                $('#popupPaymentBean').hide();
            }
        }

        function enterPaymentBean(){
            //popupPayment
            $('#popupPaymentBean').show();
            $('#popUpBeanTextArea').val('');
            $('#popUpBeanTextArea').focus();
        }

        function enterPaymentFlow(){
            //popupPaymentFlow
            $('#popupPaymentFlow').show();
            $('#popUpTextArea').val('');
            $('#popUpTextArea').focus();
        }


        function copyPaymentFlowInForm(copyString){

            var paymentText=$('#popUpTextArea').val();
            //console.log('paymentText',paymentText);
            if(paymentText==''){
                alert('Payment String is empty. Reneter');
            }
            else{
                loadValuesInForm(paymentText);
                //console.log('values copied in form');
                localStorage.setItem("paymentFlowString",paymentText);
                $('#popupPaymentFlow').hide();
            }

        }

        function copyLastFlow(){
            var string=localStorage.getItem("paymentFlowString");
            if(string!=null && string!=undefined && string!=''){
                var orderId=$('#orderid').val();
                loadValuesInForm(string);
                //handling back previous order id
                $('#orderid').val(orderId);
                //console.log('values copied in form');
            }
        }

        function copyNativeLastFlow(){
            var string=localStorage.getItem("nativeFormString");
            if(string!=null && string!=undefined && string!=''){
                var orderId=$('#orderid').val();
                loadValuesInForm(string);
                //handling back previous order id
                $('#orderid').val(orderId);
                //console.log('values copied in form');
            }
        }

        function copyNativeValueInForm(){
            var nativeFormStr = $( "#nativeForm" ).serialize();
            var string=localStorage.setItem("nativeFormString", nativeFormStr);
        }

    </script>
</head>
<body onload="javascript:assignChecksum(); javascript:generateDMRCPayload();">
<iframe name="myiframe"
        style="display: none; float: right; width: 640px; height: 600px; margin-right: 2%;"></iframe>
<h1>Merchant Check Out Page</h1>
<form id="target" method="post" action="agreement/initiate" name ="Agreement">
    <table border="1">
        <tbody>
        <tr>
            <td>
            </td>
            <td><input value="Pay" type="button" onclick="validateForm();"></td>
            <td>
                <input type="button" value="Copy Payment Flow" onclick="javascript:copyValueInForm()" />
            </td>
            <td>
                <input type="button" value="Enter Payment Bean" onclick="javascript:enterPaymentBean()"/>
                <input type="button" value="Enter Payment Flow" onclick="javascript:enterPaymentFlow()"/>
                <input type="button" value="Copy last Flow" onclick="javascript:copyLastFlow()"/>
            </td>
        </tr>

        <tr>
            <th>S.No</th>
            <th>Label</th>
            <th>Value</th>
        </tr>
        <tr>
            <td>0</td>
            <td><label>merchantMid ::*</label></td>
            <td><input type="text" id="merchantMid" tabindex="1"
                       maxlength="200" size="100" value="" name="merchantMid"
                       autocomplete="off"></td>
        </tr>
        <tr>
            <td>1</td>
            <td><label>merchantAgreementId ::*</label></td>
            <td><input type="text" id="merchantAgreementId" tabindex="1"
                       maxlength="200" size="100" value="" name="merchantAgreementId"
                       autocomplete="off"></td>
        </tr>
        <tr>
            <td>2</td>
            <td><label>description ::*</label></td>
            <td><input id="description" tabindex="1" maxlength="25" size="25"
                       name="description" autocomplete="off" value=""></td>
        </tr>

        <tr>
            <td>3</td>
            <td><label>callBackURL::*</label></td>
            <td><input id="callBackURL" tabindex="1" maxlength="20" size="20"
                       name="callBackURL" autocomplete="off" value=""></td>
        </tr>

        </tbody>
    </table>
    <div id="popupPaymentBean" style="z-index:10;position: absolute;left: 25%;top: 35%;width: 528px;height: 185px;background-color: beige;opacity: 0.98;display:none;">
        <div style="width: 100%;height: 70%;margin: 1%;">
            <textarea id="popUpBeanTextArea" rows="7" cols="60"></textarea>
        </div>
        <div style="margin-left: 1%;">
            <input type="button" id="beanBtnEnterSubmit" value="Enter Payment bean" onclick="javascript:parsePaymentBeanToFetchParams();">
            <input type="button" id="beanHideEnterSubmit" value="Cancel" onclick="document.getElementById('popupPaymentBean').style.display='none';"/>
        </div>
    </div>

    <div id="popupPaymentFlow" style="z-index:10;position: absolute;left: 25%;top: 35%;width: 528px;height: 185px;background-color: beige;opacity: 0.98;display:none;">
        <div style="width: 100%;height: 70%;margin: 1%;">
            <textarea id="popUpTextArea" rows="7" cols="60"></textarea>
        </div>
        <div style="margin-left: 1%;">
            <input type="button" id="btnEnterSubmit" value="Enter Payment flow" onclick="javascript:copyPaymentFlowInForm();">
            <input type="button" id="hideEnterSubmit" value="Cancel" onclick="document.getElementById('popupPaymentFlow').style.display='none';"/>
        </div>
    </div>
    <input type="text" name="Element To Be Copied" id="inputContainingTextToBeCopied" value="dummy input to copy" style="display:none; position: relative; left: -10000px;"/>

    <script>

    </script>
</body>
</html>
