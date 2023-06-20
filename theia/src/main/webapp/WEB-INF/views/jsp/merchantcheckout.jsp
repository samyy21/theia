<%@ page session="false"%>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<!DOCTYPE html>
<html>
<head>
	<title>My Merchant Check Out Page</title>
	<meta name="GENERATOR" content="Evrsoft First Page">
	<!--<script type="text/javascript" src="/theia/resources/js/jquery-1.10.1.min.js"></script>-->
	<script src="https://code.jquery.com/jquery-1.10.1.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.serializeJSON/2.9.0/jquery.serializejson.js"></script>
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
			document.getElementById("target").action = "processTransaction";
            document.getElementById("target").submit();
        }
        function validateSbiForm() {
            document.getElementById("SbiForm").submit();
        }

		function validateSdkForm() {
			document.getElementById("target").action = "sdk/processTransaction";
			document.getElementById("target").submit();
		}

        function validateNativeForm(event,type){
            if(type === "native"){
                var data = $("#nativeForm").serializeArray();
                var payload = {},obj = {};
                if(data && data.length > 0){
                    data.forEach(function (item) {
                        obj[item.name] = item.value;
                    });
                }

                var payload = {
                    "head":{
                        "version":"v1",
                        "requestTimestamp":Date.now(),
                        "channelId":"WEB",
                        "txnToken":obj['txnToken'],
                        "workFlow":obj['workFlow']
                    },
                    "body":obj
                };

                $.ajax({
                    type:"POST",
                    url:"api/v1/processTransaction?mid=" + obj['mid'] + "&orderId=" + obj['orderId'],
                    data:JSON.stringify(payload),
                    contentType:"application/json",
                    dataType: "json",
                    success:function (data) {
                        var form = document.createElement("form");
                        if(!data.body.bankForm){
                            document.getElementById('nativeJSONResponse').innerText = JSON.stringify(data.body);
                            return;
                        }
                        var bankForm = data.body.bankForm;
                        if(bankForm.pageType=='redirect' || bankForm.pageType=='direct') {

                            form.action = bankForm.redirectForm.actionUrl;
                            var content = bankForm.redirectForm.content;

                            var method = bankForm.redirectForm.method;
							form.method = method;

							var headers = bankForm.redirectForm.headers;
                            var keys = Object.keys(content);
                            keys.forEach(function (key) {
                                var input = document.createElement('input');
                                input.type = 'text';
                                input.value = content[key];
                                input.name = key;
                                form.append(input);
                            });
                            document.body.append(form);
                            /*$.ajax({
                                method: "POST",
                                url: form.action,
                                data:JSON.stringify(content),
                                dataType : "json",
                                contentType: "application/json"
                            });*/
                            form.submit();
						}
                    },
                    error:function (error) {
                        document.getElementById('nativeJSONResponse').innerText = JSON.stringify(error);
                    }
                });
            }

			else if(type === "scanPay"){
				var jsonData = $("#scanPayForm").serializeJSON();
				var payload = {
					"head":{
						"version":"v1",
						"requestTimestamp":Date.now(),
						"channelId":jsonData.channelId,
						"tokenType":jsonData.tokenType,
						"token":jsonData.token,
					},
					"body":jsonData
				};

				$.ajax({
					type:"POST",
					url:"api/v1/processTransaction?mid=" + jsonData.mid + "&orderId=" + jsonData.orderId,
					data:JSON.stringify(payload),
					contentType:"application/json",
					dataType: "json",
					success:function (data) {
						var form = document.createElement("form");
						if(!data.body.bankForm){
							document.getElementById('scanPaySpan').innerText = JSON.stringify(data.body);
							return;
						}
						var bankForm = data.body.bankForm;
						if(bankForm.pageType=='redirect' || bankForm.pageType=='direct') {

							form.action = bankForm.redirectForm.actionUrl;
							var content = bankForm.redirectForm.content;

							var method = bankForm.redirectForm.method;
							form.method = method;

							var headers = bankForm.redirectForm.headers;
							var keys = Object.keys(content);
							keys.forEach(function (key) {
								var input = document.createElement('input');
								input.type = 'text';
								input.value = content[key];
								input.name = key;
								form.append(input);
							});
							document.body.append(form);
							/*$.ajax({
                                method: "POST",
                                url: form.action,
                                data:JSON.stringify(content),
                                dataType : "json",
                                contentType: "application/json"
                            });*/
							form.submit();
						}
					},
					error:function (error) {
						document.getElementById('scanPaySpan').innerText = JSON.stringify(error);
					}
				});
			}
			else{
                document.getElementById("nativeForm").submit();
            }
        }

        function validateAppInvokeForm() {
            document.getElementById("appInvokeForm").submit();
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
                        document.getElementsByName(a[0])[0].value= window.decodeURIComponent(a[1]);
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
            mapping["validationRequired"]="IS_CART_VALIDATION_REQUIRED";
            mapping["txnToken"]="txnToken";
			mapping["bankName"]="bankName";
			mapping["seqNumber"]="seqNumber"


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

		function copyScanPayValueInForm(){
			var scanPayStr = $( "#scanPayForm" ).serializeArray();
			var string=localStorage.setItem("scanPayFormString", JSON.stringify(scanPayStr));
			alert("Done! Click Paste button to restore data");
		}

		function copyScanPayLastFlow(){
			var string=JSON.parse(localStorage.getItem("scanPayFormString"));
			if(string!=null && string!=undefined && string!=''){
				var orderId=$('#orderid').val();
				loadValuesInFormScanPay(string);
				//handling back previous order id
				$('#orderid').val(orderId);
				//console.log('values copied in form');
				$("scanPayForm").each(function(){
					debugger;
					console.log("wqeq");
					console.log($(this).find('input[name=orderId]').val()+" 1");
				});
			}
		}

		function loadValuesInFormScanPay(encodedString){

			try {
				$.each(encodedString, function (index, data) {
					debugger;
					document.getElementById(data.name+"scp").value = data.value;
				});
			}
			catch(ex){
				alert('payment flow string not proper. Reenter again');
			}
/*
			var splitArray=encodedString.split('&');
			try{
				splitArray.forEach(function(element){
					var a=element.split('=');
					if(element!='')
						document.getElementsByName(a[0])[0].value= window.decodeURIComponent(a[1]);
				});
			}*/

		}

	</script>
</head>
<body onload="javascript:assignChecksum(); javascript:generateDMRCPayload();">
<iframe name="myiframe"
		style="display: none; float: right; width: 640px; height: 600px; margin-right: 2%;"></iframe>
<h1>Merchant Check Out Page</h1>

<h3>Wrapper</h3>
<form id="WrapperForm" method="post" action="customProcessTransaction" name ="WrapperForm">
	<table border="1">
		<tbody>
		<tr>
			<th>S.No</th>
			<th>Label</th>
			<th>Value</th>
		</tr>
		<tr>
			<td>1</td>
			<td><label>msg ::*</label></td>
			<td><input id="msg" tabindex="1" maxlength="1000" size="30"
					   name="msg" autocomplete="off" value="">
			</td>
		</tr>
		<tr>
			<td>1</td>
			<td><label>encdata ::*</label></td>
			<td><input id="encdata" tabindex="1" maxlength="1000" size="30"
					   name="encdata" autocomplete="off" value="">
			</td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>Merchant_code::*</label></td>
			<td><input Merchant_code="Auto" type="text" name="Merchant_code"></td>
		</tr>
		<tr>
			<td>3</td>
			<td><label>MID ::*</label></td>
			<td><input id="MID" tabindex="1" maxlength="30" size="30"
					   name="MID" autocomplete="off" value="rupayc27783269792805">
			</td>
		</tr>
		<tr>
			<td>4</td>
			<td><label>ORDER_ID::*</label></td>
			<td><input id="ORDER_ID" tabindex="1" maxlength="20" size="20"
					   name="ORDER_ID" autocomplete="off" value=""></td>
		</tr>

		<tr>
			<td>5</td>
			<td><label>ENCRYPTED KEY::</label></td>
			<td><input id="encrypted_key" tabindex="1" maxlength="500" size="20"
					   name="encrypted_key" autocomplete="off" value=""></td>
		</tr>

		<tr>
			<td></td>
			<!--  <td><input value="Pay in Iframe" type="submit" onclick="javascript:payInIframe();"></td>-->
			<!--<td><input value="Pay" type="submit" ></td>-->
			<td><input value="Pay" type="button" onclick="validateWrapperForm();"></td>
			<td>
				<!--<input value="CalculateChecksum" type="button"  onclick="javascript:calculateCheckSum();"> -->
			</td>
		</tr>
		</tbody>
	</table>
</form>

<tr>

</tr>

<form id="target" method="post" action="processTransaction" name ="ProcessTransactionForm">
	<table border="1">
		<tbody>
		<tr>
			<td>
			</td>
			<td><input value="Pay" type="button" onclick="validateForm();">
			<input value="Sdk Pay" type="button" onclick="validateSdkForm();"></td>
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
			<td><label>MERC_UNQ_REF ::*</label></td>
			<td><input type="text" id="MERC_UNQ_REF" tabindex="1"
					   maxlength="20000" size="100" value="" name="MERC_UNQ_REF"
					   autocomplete="off"></td>
		</tr>
		<tr>
			<td>1</td>
			<td><label>CheckSum ::*</label></td>
			<td><input type="text" id="CHECKSUMHASH" tabindex="1"
					   maxlength="200" size="100" value="" name="CHECKSUMHASH"
					   autocomplete="off"></td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>ORDER_ID::*</label></td>
			<td><input id="orderid" tabindex="1" maxlength="20" size="20"
					   name="ORDER_ID" autocomplete="off" value="PARCEL"></td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>THEME::*</label></td>
			<td><input id="THEME" tabindex="1" maxlength="25" size="25"
					   name="THEME" autocomplete="off" value="merchant4"></td>
		</tr>

		<tr>
			<td>2</td>
			<td><label>Login theme::*</label></td>
			<td><input id="THEME" tabindex="1" maxlength="20" size="20"
					   name="LOGIN_THEME" autocomplete="off" value=""></td>
		</tr>

		<tr>
			<td>3</td>
			<td><label>CUSTID ::*</label></td>
			<td><input id="CUST_ID" tabindex="2" maxlength="12" size="12"
					   name="CUST_ID" autocomplete="off" value='Test101'></td>
		</tr>
		<tr>
			<td>4</td>
			<td><label>MID ::*</label></td>
			<td><input id="MID" tabindex="1" maxlength="30" size="30"
					   name="MID" autocomplete="off" value="master86636472935906">
			</td>
		</tr>
		<tr>
			<td>5</td>
			<td><label>INDUSTRY_TYPE_ID ::</label></td>
			<td><input id="INDUSTRY_TYPE_ID" tabindex="4" maxlength="12"
					   size="12" name="INDUSTRY_TYPE_ID" autocomplete="off"
					   value="Retail"></td>
		</tr>
		<tr>
			<td>6</td>
			<td><label>Channel ::</label></td>
			<td><input id="CHANNEL_ID" tabindex="4" maxlength="12"
					   size="12" name="CHANNEL_ID" autocomplete="off" value="WEB">
			</td>
		</tr>
		<tr>
			<td>8</td>
			<td><label>txnAmount</label></td>
			<td><input title="TXN_AMOUNT" tabindex="10" type="text"
					   name="TXN_AMOUNT" value="2"></td>
		</tr>
		<tr>
			<td>9</td>

			<td><label>AUTH_MODE</label></td>

			<td><input title="Is Email Id Verified" tabindex="10"
					   type="text" name="AUTH_MODE" value="3D"></td>
		</tr>
		<tr>
			<td>10</td>
			<td><label>Web site ::</label></td>
			<td><input id="WEBSITE" title="Web Site Name" tabindex="10"
					   type="text" name="WEBSITE" value="retail"></td>
		</tr>
		<tr>
			<td>9</td>
			<td><label>SSO TOKEN</label></td>

			<td><input title="SSO_TOKEN" tabindex="100" type="text"
					   name="SSO_TOKEN" value=""></td>
		</tr>

		<tr>
			<td>9.2</td>
			<td><label>IDebit Option</label></td>
			<td><input title="IDEBIT_OPTION" tabindex="100" type="text"
					   name="IDEBIT_OPTION" value="otp"></td>
		</tr>

		<tr>
			<td>10</td>
			<td><label>Token Type:</label></td>
			<td><select name="TOKEN_TYPE">
				<option value="OAUTH">OAUTH</option>
				<option value="SSO">SSO</option>
			</select></td>
		</tr>

		<tr>
			<td>11</td>
			<td><label>Locale</label></td>
			<td>
				<input title="locale" tabindex="100" type="text" name="locale">
			</td>
		</tr>

		<tr>
			<td>27</td>
			<td><label>PAYMENT_MODE_ONLY ::*</label></td>
			<td><input id="selbank" title="PAYMENT_MODE_ONLY"
					   tabindex="10" size="35" maxlength="50" name="PAYMENT_MODE_ONLY"
					   value="" type="text"></td>
		</tr>
		<tr>
			<td>27</td>
			<td><label>PAYMENT_TYPE_ID ::*</label></td>
			<td><input id="selbank" title="PAYMENT_TYPE_ID" tabindex="10"
					   size="35" maxlength="50" name="PAYMENT_TYPE_ID" value=""
					   type="text"></td>
		</tr>
		<tr>
			<td>27</td>
			<td><label>EMI_PLAN_ID ::*</label></td>
			<td><input id="selbank" title="EMI_PLAN_ID" tabindex="10"
					   size="35" maxlength="50" name="planId" value=""
					   type="text"></td>
		</tr>
		<tr>
			<td>27</td>
			<td><label>PAYMENT_MODE_DISABLE ::*</label></td>
			<td><input id="selbank" title="PAYMENT_MODE_DISABLE"
					   tabindex="10" size="35" maxlength="50" name="PAYMENT_MODE_DISABLE"
					   value="" type="text"></td>
		</tr>
		<tr>
			<td>27</td>
			<td><label>BANK NAME ::*</label></td>
			<td><input id="selbank" title="BANKCODE" tabindex="10"
					   size="35" maxlength="50" type="text" name="BANK_CODE"
					   value="ICICI"></td>
		</tr>

		<!--
<tr>
            <td>12</td>
            <td><label>EMAIL ::*</label></td>
            <td><input id="EMAIL" tabindex="9" maxlength="50" size="50"	name="EMAIL" autocomplete="off" value="testone@tarangtech.com">
            </td>
        </tr>

-->
		<!--   <tr>
                                <td>9</td>

                                <td><label>MOBILE_NO ::</label></td>

                                <td><input id="MOBILE_NO" tabindex="6" maxlength="14" size="14"  name="MOBILE_NO" value=""></td>
                        </tr>
                     -->



		<!-- <tr>
            <td>9</td>

            <td><label>PAN_CARD ::</label></td>

            <td><input id="PAN_CARD" tabindex="6" maxlength="12" size="12"	name="PAN_CARD" autocomplete="off" value="PAN12334"></td>
        </tr>

        <tr>
            <td>10</td>

            <td><label>Driving License ::</label></td>

            <td><input id="DL_NUMBER" tabindex="7" maxlength="12" size="12" name="DL_NUMBER" autocomplete="off" value="DL12334">
            </td>
        </tr>
                       -->

		<tr>
			<td>11</td>
			<td><label>MOBILE_NO::*</label></td>

			<td><input id="MSISDN" tabindex="8" maxlength="12" size="12"
					   name="MSISDN" autocomplete="off" value=""></td>
		</tr>

		<tr>
			<td>12</td>
			<td><label>EMAIL ::*</label></td>
			<td><input id="EMAIL" tabindex="9" maxlength="50" size="50"
					   name="EMAIL" autocomplete="off" value="">
			</td>
		</tr>
		<!--	<tr>
            <td>13</td>
            <td><label>Verified By</label></td>
            <td><input id="VERIFIED_BY" tabindex="9" maxlength="50"	size="50" name="VERIFIED_BY" value="Bond"></td>
        </tr>
        <tr>
            <td>14</td>
            <td><label>Is Email Id Verified? ::*</label></td>
            <td><input id="ISEMAILVERIFIED" title="Is Email Id Verified" type="checkbox" name="ISEMAILVERIFIED" checked></td>
        </tr>
        <tr>
            <td>15</td>
            <td><label>Address One</label></td>
            <td><input id="ADDRESS_1" title="Is Email Id Verified"	tabindex="10" type="text" name="ADDRESS_1" value="56"></td>
        </tr>
        <tr>
            <td>16</td>

            <td><label>Address Two</label></td>

            <td><input id="ADDRESS_2" title="Is Email Id Verified"	tabindex="10" type="text" name="ADDRESS_2" value="1st A cross">
            </td>
        </tr>
        <tr>
            <td>17</td>
            <td><label>City</label></td>
            <td><input id="CITY" title="Is Email Id Verified" tabindex="10" type="text" name="CITY" value="BANGALORE"></td>
        </tr>
        <tr>
            <td>18</td>
            <td><label>STATE</label></td>
            <td><input id="STATE" title="Is Email Id Verified"
                tabindex="10" type="text" name="STATE" value="KARNATAKA">
            </td>
        </tr>
        <tr>
            <td>19</td>

            <td><label>pincode</label></td>

            <td><input id="PINCODE" title="Is Email Id Verified"	tabindex="10" type="text" name="PINCODE" value="560048"></td>


        </tr>
         <tr>
            <td>26</td>
            <td><label>CheckSumOrder</label>
            </td>
            <td><textarea rows="6" cols="50" name="checkSumOrder"	id="checkSumOrder">Order_Id</textarea>
            </td>
        </tr> -->
		<tr>
			<td>26</td>
			<td><label>Merchant Key</label></td>
			<td><input title="merchantKey" tabindex="10" type="text"
					   name="merchantKey" value=""></td>
		</tr>
		<!--<tr>
            <td>28</td>
            <td><label>Merchant Callback</label>
            </td>
            <td><input  tabindex="10"
                type="text" name="CALLBACK_URL"
                                        value="http://www.paytm.com" />
            </td>
        </tr>-->
		<tr>
			<td>29</td>
			<td><label>Promocode</label></td>
			<td><input tabindex="10" type="text" name="PROMO_CAMP_ID" />
			</td>
		</tr>
		<tr>
			<td>30</td>
			<td><label>Request Type</label></td>
			<td><input tabindex="10" type="text" name="REQUEST_TYPE"
					   value="DEFAULT" /></td>
		</tr>
		<tr>
			<td>31</td>
			<td><label>Subscription ID</label></td>
			<td><input tabindex="10" type="text" name="SUBSCRIPTION_ID"
					   value="" /></td>
		</tr>
		<tr>
			<td>32</td>
			<td><label>Subscription Service ID</label></td>
			<td><input tabindex="10" type="text" name="SUBS_SERVICE_ID"
					   value="" /></td>
		</tr>
		<tr>
			<td>33</td>
			<td><label>Subscription Amount Type</label></td>
			<td><input tabindex="10" type="text" name="SUBS_AMOUNT_TYPE"
					   value="VARIABLE" /></td>
		</tr>
		<tr>
			<td>34</td>
			<td><label>Subscription Max Amount </label></td>
			<td><input tabindex="10" type="text" name="SUBS_MAX_AMOUNT"
					   value="" /></td>
		</tr>
		<tr>
			<td>35</td>
			<td><label>Subscription Frequency </label></td>
			<td><input tabindex="10" type="text" name="SUBS_FREQUENCY"
					   value="" /></td>
		</tr>
		<tr>
			<td>36</td>
			<td><label>Subscription Frequency Unit </label></td>
			<td><input tabindex="10" type="text"
					   name="SUBS_FREQUENCY_UNIT" value="MONTH" /></td>
		</tr>
		<tr>
			<td>37</td>
			<td><label>Subscription Start Date </label></td>
			<td><input tabindex="10" type="text" name="SUBS_START_DATE"
					   value="" /></td>
		</tr>
		<tr>
			<td>38</td>
			<td><label>Subscription Grace Days </label></td>
			<td><input tabindex="10" type="text" name="SUBS_GRACE_DAYS"
					   value="" /></td>
		</tr>
		<tr>
			<td>39</td>
			<td><label>Subscription Retry Enabled </label></td>
			<td><input tabindex="10" type="text" name="SUBS_ENABLE_RETRY"
					   value="" /></td>
		</tr>
		<tr>
			<td>40</td>
			<td><label>Subscription Retry Count </label></td>
			<td><input tabindex="10" type="text" name="SUBS_RETRY_COUNT"
					   value="" /></td>
		</tr>
		<tr>
			<td>41</td>
			<td><label>Subscription Expiry Date </label></td>
			<td><input tabindex="10" type="text" name="SUBS_EXPIRY_DATE"
					   value="" /></td>
		</tr>
		<tr>
			<td>42</td>
			<td><label>PAYMENT DETAILS</label></td>
			<td><input tabindex="10" type="text" name="PAYMENT_DETAILS"
					   value="" /></td>
		</tr>
		<tr>
			<td>43</td>
			<td><label>Store Card</label></td>
			<td><input tabindex="10" type="text" name="STORE_CARD"
					   value="" /></td>
		</tr>

		<tr>
			<td>44</td>
			<td><label>CUST_NAME</label></td>
			<td><input tabindex="10" type="text" name="CUST_NAME"
					   value="" /></td>
		</tr>

		<tr>
			<td>45</td>
			<td><label>SAVED_CARD_ID</label></td>
			<td><input tabindex="10" type="text" name="SAVED_CARD_ID"
					   value="" /></td>
		</tr>
		<tr>
			<td>46</td>
			<td><label>SUBS_PAYMENT_MODE</label></td>
			<td><input tabindex="10" type="text" name="SUBS_PAYMENT_MODE"
					   value="" /></td>
		</tr>
		<tr>
			<td>42</td>
			<td><label>CONNECTION_TYPE</label></td>
			<td><input tabindex="10" type="text" name="CONNECTION_TYPE"
					   value="" /></td>
		</tr>
		<tr>
			<td>47</td>
			<td><label>SUBS_PPI_ONLY</label></td>
			<td><input tabindex="10" type="text" name="SUBS_PPI_ONLY"
					   value="" /></td>
		</tr>
		<tr>
			<td>48</td>
			<td><label>DEVICE_ID</label></td>
			<td><input tabindex="10" type="text" name="DEVICE_ID"
					   value="" /></td>
		</tr>
		<tr>
			<td>49</td>
			<td><label>DEVICE_SOURCE</label></td>
			<td><input tabindex="10" type="text" name="DEVICE_SOURCE"
					   value="" /></td>
		</tr>
		<tr>
			<td>50</td>
			<td><label>GOODS_INFO</label></td>
			<td><input tabindex="10" type="text" name="GOODS_INFO"
					   value="" /></td>
		</tr>
		<tr>
			<td>51</td>
			<td><label>SHIPPING_INFO</label></td>
			<td><input tabindex="10" type="text" name="SHIPPING_INFO"
					   value="" /></td>
		</tr>

		<tr>
			<td>52</td>
			<td><label>AddMoneyFlag</label></td>
			<td><input tabindex="10" type="text" name="addMoney"
					   value="" /></td>
		</tr>

		<tr>
			<td>52</td>
			<td><label>ACCOUNT_TYPE</label></td>
			<td><input tabindex="10" type="text" name="ACCOUNT_TYPE"
					   value="" /></td>
		</tr>

		<tr>
			<td>52</td>
			<td><label>WalletAmount</label></td>
			<td><input tabindex="10" type="text" name="WALLET_AMOUNT"
					   value="" /></td>
		</tr>

		<tr>
			<td>52</td>
			<td><label>WalletAmount</label></td>
			<td><input tabindex="10" type="text" name="WALLET_AMOUNT"
					   value="" /></td>
		</tr>

		<tr>
			<td>53</td>
			<td><label>PAYTM TOKEN</label></td>

			<td><input title="PAYTM_TOKEN" tabindex="100" type="text"
					   name="PAYTM_TOKEN" value=""></td>
		</tr>

		<tr>
			<td>54</td>
			<td><label>CC_BILL_NO</label></td>
			<td><input title="CC_BILL_NO" type="text" name="CC_BILL_NO"></td>
		</tr>

		<tr>
			<td>54</td>
			<td><label>CC_BILL_NO</label></td>
			<td><input title="CC_BILL_NO" type="text" name="CC_BILL_NO"></td>
		</tr>
		<tr>
			<td>55</td>
			<td><label>EMI_OPTIONs</label></td>
			<td><input title="EMI_OPTIONS" type="text" name="EMI_OPTIONS"></td>
		</tr>

		<tr>
			<td>56</td>
			<td><label>ADDRESS_1</label></td>
			<td><input title="ADDRESS_1" type="text" name="ADDRESS_1"></td>
		</tr>
		<tr>
			<td>57</td>
			<td><label>PINCODE</label></td>
			<td><input title="PINCODE" type="text" name="PINCODE"></td>
		</tr>
		<tr>
			<td>58</td>
			<td><label>IS_SAVED_CARD</label></td>
			<td><input title="IS_SAVED_CARD" type="text" name="IS_SAVED_CARD"></td>
		</tr>
		<tr>
			<td>59</td>
			<td><label>PASS_CODE</label></td>
			<td><input title="PASS_CODE" type="text" name="PASS_CODE"></td>
		</tr>
		<tr>
			<td>60</td>
			<td><label>CRED_BLOCK</label></td>
			<td><input title="credsAllowed" type="text" name="credsAllowed"></td>
		</tr>

		<tr>
			<td>61</td>
			<td><label>accountNumber</label></td>
			<td><input title="accountNumber" type="text" name="accountNumber"></td>
		</tr>
		<tr>
			<td>62</td>
			<td><label>bank</label></td>
			<td><input title="bank" type="text" name="bank"></td>
		</tr>
		<tr>
			<td>63</td>
			<td><label>MPIN</label></td>
			<td><input title="MPIN" type="text" name="MPIN"></td>
		</tr>
		<tr>
			<td>64</td>
			<td><label>ACCOUNT_REF_ID</label></td>
			<td><input title="ACCOUNT_REF_ID" type="text" name="ACCOUNT_REF_ID"></td>
		</tr>
		<tr>
			<td>65</td>
			<td><label>BUSINESS_ID</label></td>
			<td><input title="BUSINESS_ID" type="text" name="BUSINESS_ID"></td>
		</tr>
		<tr>
			<td>66</td>
			<td><label>IS_MOCK_REQUEST</label></td>
			<td><input title="IS_MOCK_REQUEST" type="text" name="IS_MOCK_REQUEST"></td>
		</tr>
		<tr>
			<td>67</td>
			<td><label>Subwallet Details</label></td>
			<td><input title="Subwallet Details" type="text" name="subwalletAmount"></td>
		</tr>
		<tr>
			<td>68</td>
			<td><label>cardTokenRequired</label></td>
			<td><input title="cardTokenRequired" type="text" name="cardTokenRequired"></td>
		</tr>
		<tr>
			<td>69</td>
			<td><label>merchantClientId</label></td>
			<td><input title="merchantClientId" type="text" name="CLIENTID"></td>
		</tr>

		<tr>
			<td>70</td>
			<td><label>cartValidationRequired</label></td>
			<td><input title="validationRequired" type="text" name="validationRequired"></td>
		</tr>

		<tr>
			<td>71</td>
			<td><label>MOBILE_NO</label></td>
			<td><input MOBILE_NO="Auto" type="text" name="MOBILE_NO"></td>
		</tr>
		<tr>
			<td>72</td>
			<td><label>targetPhoneNo</label></td>
			<td><input title="targetPhoneNo" type="text" name="targetPhoneNo"></td>
		</tr>
		<tr>
			<td>73</td>
			<td><label>templateId</label></td>
			<td><input title="templateId" type="text" name="templateId"></td>
		</tr>
		<tr>
			<td>74</td>
			<td><label>bId</label></td>
			<td><input title="bId" type="text" name="bId"></td>
		</tr>
		<tr>
			<td>75</td>
			<td><label>corporateCustId</label></td>
			<td><input title="corporateCustId" type="text" name="corporateCustId"></td>
		</tr>
		<tr>
			<td>76</td>
			<td><label>IS_AUTO_RETRY</label></td>
			<td><input title="isAutoRetry" type="text" name="isAutoRetry"></td>
		</tr>
		<tr>
			<td>77</td>
			<td><label>IS_AUTO_RENEW</label></td>
			<td><input title="isAutoRenewal" type="text" name="isAutoRenewal"></td>
		</tr>
		<tr>
			<td>78</td>
			<td><label>IS_COMMUNICATION_MANAGER</label></td>
			<td><input title="isCommunicationManager" type="text" name="isCommunicationManager"></td>
		</tr>
		<tr>
			<td>79</td>
			<td><label>RENEWAL_AMOUNT</label></td>
			<td><input title="renewalAmount" type="text" name="renewalAmount"></td>
		</tr>

		<tr>
			<td>80</td>
			<td><label>splitSettlementInfo</label></td>
			<td><input title="splitSettlementInfo" type="text" name="splitSettlementInfo"></td>
		</tr>

		<tr>
			<td>81</td>
			<td><label>appVersion</label></td>
			<td><input title="appVersion" type="text" name="appVersion"></td>
		</tr>

		<tr>
			<td>82</td>
			<td><label>origin-channel</label></td>
			<td><input title="origin-channel" type="text" name="origin-channel"></td>
		</tr>

		<tr>
			<td>83</td>
			<td><label>CALLBACK_URL</label></td>
			<td><input title="CALLBACK_URL" type="text" name="CALLBACK_URL"></td>
		</tr>

		<tr>
			<td>84</td>
			<td><label>PEON_URL</label></td>
			<td><input title="PEON_URL" type="text" name="PEON_URL"></td>
		</tr>

		<tr>
			<td>83</td>
			<td><label>USER_NAME</label></td>
			<td><input title="USER_NAME" type="text" name="USER_NAME"></td>
		</tr>

		<tr>
			<td>84</td>
			<td><label>bankIfsc</label></td>
			<td><input title="bankIfsc" type="text" name="bankIfsc"></td>
		</tr>

		<tr>
			<td>85</td>
			<td><label>Subscription Purpose</label></td>
			<td><input title="subscriptionPurpose" type="text" name="subscriptionPurpose"></td>
		</tr>

		<tr>
			<td>86</td>
			<td><label>UDF_2</label></td>
			<td><input title="UDF_2" type="text" name="UDF_2"></td>
		</tr>

		<tr>
			<td>87</td>
			<td><label>Ultimate Beneficiary Name</label></td>
			<td><input title="ultimateBeneficiaryName" type="text" name="ultimateBeneficiaryName"></td>
		</tr>

		<tr>
			<td></td>
			<!--	<td><input value="Pay in Iframe" type="submit" onclick="javascript:payInIframe();"></td>-->
			<!--<td><input value="Pay" type="submit" ></td>-->
			<td><input value="Pay" type="button" onclick="validateForm();"></td>
			<td>
				<!--<input value="CalculateChecksum" type="button"	onclick="javascript:calculateCheckSum();"> -->
			</td>
		</tr>
		</tbody>
	</table>
	* - Mandatory Fields
</form>
<br/><br/>
<span id="nativeJSONResponse"></span>
<br/><br/>
<form id="nativeForm" method="post" action="api/v1/processTransaction" onsubmit="return false;">
	<table border="1">
		<tbody>
		<tr>
			<td>
			</td>
			<td>
				<button value="Pay" type="button" onclick="validateNativeForm(event,'formPost')">Pay</button>
				<button value="Native+ Pay" type="button" onclick="validateNativeForm(event,'native')">Native+ Pay</button>
			</td>
			<td>
				<input type="button" value="Copy" onclick="copyNativeValueInForm()" />
			</td>
			<td>
				<input type="button" value="Paste" onclick="copyNativeLastFlow()"/>
			</td>
		</tr>

		<tr>
			<th>S.No</th>
			<th>Label</th>
			<th>Value</th>
		</tr>

		<tr>
			<td>1</td>
			<td><label>MID</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="mid"></td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>OrderId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="orderId"></td>
		</tr>
		<tr>
			<td>3</td>
			<td><label>ChannelId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="channelId"></td>
		</tr>
		<tr>
			<td>4</td>
			<td><label>TxnToken</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="txnToken"></td>
		</tr>
		<tr>
			<td>5</td>
			<td><label>PaymentMode</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="paymentMode"></td>
		</tr>
		<tr>
			<td>6</td>
			<td><label>CardInfo</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="cardInfo"></td>
		</tr>
		<tr>
			<td>7</td>
			<td><label>AuthMode</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="authMode"></td>
		</tr>
		<tr>
			<td>8</td>
			<td><label>ChannelCode</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="channelCode"></td>
		</tr>
		<tr>
			<td>9</td>
			<td><label>saveForFuture</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="saveForFuture"></td>
		</tr>
		<tr>
			<td>10</td>
			<td><label>PaymentFlow</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="paymentFlow"></td>
		</tr>
		<tr>
			<td>11</td>
			<td><label>PayerAccount</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="payerAccount"></td>
		</tr>
		<tr>
			<td>12</td>
			<td><label>MPin</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="mpin"></td>
		</tr>
		<tr>
			<td>13</td>
			<td><label>EMI planId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="planId"></td>
		</tr>
		<tr>
			<td>14</td>
			<td><label>Encrypted CardInfo</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="encCardInfo"></td>
		</tr>
		<tr>
			<td>15</td>
			<td><label>Promo Details</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="PROMO_CAMP_ID"></td>
		</tr>
		<tr>
			<td>16</td>
			<td><label>cardTokenRequired</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="cardTokenRequired"></td>
		</tr>
		<tr>
			<td>17</td>
			<td><label>aggMID</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="aggMid"></td>
		</tr>
		<tr>
			<td>18</td>
			<td><label>accountNumber</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="account_number"></td>
		</tr>
		<tr>
			<td>19</td>
			<td><label>storeInstrument</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="storeInstrument"></td>
		</tr>
		<tr>
			<td>20</td>
			<td><label>txnAmount</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="txnAmount"></td>
		</tr>

		<tr>
			<td>21</td>
			<td><label>workFlow</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="workFlow"></td>
		</tr>

		<tr>
			<td>22</td>
			<td><label>riskExtendInfo</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="riskExtendInfo"></td>
		</tr>
		<tr>
			<td>23</td>
			<td><label>tokenType</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="tokenType"></td>
		</tr>
		<tr>
			<td>24</td>
			<td><label>RISK_VERIFY_FLOW</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="RISK_VERIFY_FLOW"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>token</label></td>
			<td><input type="text" id="" tabindex="1"

					   maxlength="200" size="100" value="" name="token"></td>
		</tr>

		<tr>
			<td>25</td>
			<td><label></label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="token"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>GV_CONSENT_FLOW</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="GV_CONSENT_FLOW"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>seqNumber</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="seqNumber"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>bankName</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="bankName"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>upiAccRefId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="upiAccRefId"></td>
		</tr>
		<tr>
			<td>26</td>
			<td><label>checkoutJsConfig</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="checkoutJsConfig"></td>
		</tr>

		<tr>
			<td></td>
			<td>
				<button value="Pay" type="button" onclick="validateNativeForm(event,'formPost')">Pay</button>
				<button value="Native+ Pay" type="button" onclick="validateNativeForm(event,'native')">Native+ Pay</button>
			</td>
			<td></td>
		</tr>


		</tbody>
	</table>
</form>

<br/><br/>
<h3>Scan & Pay</h3>
<span id="scanPaySpan"></span>
<form id="scanPayForm" method="post" action="api/v1/processTransaction" onsubmit="return false;">
	<table border="1">
		<tbody>
		<tr>
			<td>
			</td>
			<td>
				<button value="Pay" type="button" onclick="validateNativeForm(event,'scanPay')">Pay</button>
			</td>
			<td>
				<input type="button" value="Copy" onclick="copyScanPayValueInForm()" />
			</td>
			<td>
				<input type="button" value="Paste" onclick="copyScanPayLastFlow()"/>
			</td>
		</tr>

		<tr>
			<th>S.No</th>
			<th>Label</th>
			<th>Value</th>
		</tr>

		<tr>
			<td>1</td>
			<td><label>ChannelId</label></td>
			<td><input type="text" id="channelIdscp" tabindex="1"
					   maxlength="200" size="100" value="WAP" name="channelId"></td>
		</tr>

		<tr>
			<td>2</td>
			<td><label>Token Type:</label></td>
			<td><select name="tokenType" id="tokenTypescp">
				<option value="SSO">SSO</option>
			</select></td>
		</tr>


		<tr>
			<td>3</td>
			<td><label>Token</label></td>
			<td><input type="text" id="tokenscp" tabindex="1"
					   maxlength="200" size="100" value="" name="token"></td>
		</tr>

		<tr>
			<td>4</td>
			<td><label>MID</label></td>
			<td><input type="text" id="midscp" tabindex="1"
					   maxlength="200" size="100" value="PGP31899144818088675" name="mid"></td>
		</tr>
		<tr>
			<td>5</td>
			<td><label>OrderId</label></td>
			<td><input type="text" id="orderIdscp" tabindex="1"
					   maxlength="200" size="100" value="" name="orderId"></td>
		</tr>

		<tr>
			<td>6</td>
			<td><label>RequestType</label></td>
			<td><input type="text" id="requestTypescp" tabindex="1"
					   maxlength="200" size="100" value="" name="requestType"></td>
		</tr>

		<tr>
			<td>7</td>
			<td><label>DeviceId</label></td>
			<td><input type="text" id="deviceIdscp" tabindex="1"
					   maxlength="200" size="100" value="" name="deviceId"></td>
		</tr>

		<tr>
			<td>8</td>
			<td><label>BankName</label></td>
			<td><input type="text" id="bankNamescp" tabindex="1"
					   maxlength="200" size="100" value="" name="bankName"></td>
		</tr>

		<tr>
			<td>9</td>
			<td><label>PaymentMode</label></td>
			<td><input type="text" id="paymentModescp" tabindex="1"
					   maxlength="200" size="100" value="" name="paymentMode"></td>
		</tr>

		<tr>
			<td>10</td>
			<td><label>CardInfo</label></td>
			<td><input type="text" id="cardInfoscp" tabindex="1"
					   maxlength="200" size="100" value="" name="cardInfo"></td>
		</tr>
		<tr>
			<td>11</td>
			<td><label>AuthMode</label></td>
			<td><input type="text" id="authModescp" tabindex="1"
					   maxlength="200" size="100" value="" name="authMode"></td>
		</tr>
		<tr>
			<td>12</td>
			<td><label>ChannelCode</label></td>
			<td><input type="text" id="channelCodescp" tabindex="1"
					   maxlength="200" size="100" value="" name="channelCode"></td>
		</tr>
		<tr>
			<td>13</td>
			<td><label>saveForFuture</label></td>
			<td><input type="text" id="saveForFuturescp" tabindex="1"
					   maxlength="200" size="100" value="" name="saveForFuture"></td>
		</tr>
		<tr>
			<td>14</td>
			<td><label>PaymentFlow</label></td>
			<td><input type="text" id="paymentFlowscp" tabindex="1"
					   maxlength="200" size="100" value="" name="paymentFlow"></td>
		</tr>
		<tr>
			<td>15</td>
			<td><label>PayerAccount</label></td>
			<td><input type="text" id="payerAccountscp" tabindex="1"
					   maxlength="200" size="100" value="" name="payerAccount"></td>
		</tr>
		<tr>
			<td>16</td>
			<td><label>MPin</label></td>
			<td><input type="text" id="mpinscp" tabindex="1"
					   maxlength="200" size="100" value="" name="mpin"></td>
		</tr>
		<tr>
			<td>17</td>
			<td><label>EMI Type</label></td>
			<td><input type="text" id="emiTypescp" tabindex="1"
					   maxlength="200" size="100" value="" name="emiType"></td>
		</tr>
		<tr>
			<td>18</td>
			<td><label>EMI planId</label></td>
			<td><input type="text" id="planIdscp" tabindex="1"
					   maxlength="200" size="100" value="" name="planId"></td>
		</tr>
		<tr>
			<td>19</td>
			<td><label>Encrypted CardInfo</label></td>
			<td><input type="text" id="encCardInfoscp" tabindex="1"
					   maxlength="200" size="100" value="" name="encCardInfo"></td>
		</tr>
		<tr>
			<td>20</td>
			<td><label>Promo Details</label></td>
			<td><input type="text" id="PROMO_CAMP_IDscp" tabindex="1"
					   maxlength="200" size="100" value="" name="PROMO_CAMP_ID"></td>
		</tr>
		<tr>
			<td>21</td>
			<td><label>cardTokenRequired</label></td>
			<td><input type="text" id="cardTokenRequiredscp" tabindex="1"
					   maxlength="200" size="100" value="" name="cardTokenRequired"></td>
		</tr>
		<tr>
			<td>22</td>
			<td><label>aggMID</label></td>
			<td><input type="text" id="aggMidscp" tabindex="1"
					   maxlength="200" size="100" value="" name="aggMid"></td>
		</tr>
		<tr>
			<td>23</td>
			<td><label>accountNumber</label></td>
			<td><input type="text" id="account_numberscp" tabindex="1"
					   maxlength="200" size="100" value="" name="account_number"></td>
		</tr>
		<tr>
			<td>24</td>
			<td><label>storeInstrument</label></td>
			<td><input type="text" id="storeInstrumentscp" tabindex="1"
					   maxlength="200" size="100" value="" name="storeInstrument"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>txnAmount</label></td>
			<td><input type="text" id="txnAmountscp" tabindex="1"
					   maxlength="200" size="100" value="" readonly name="txnAmount"></td>
		</tr>
		<tr>
			<td>26</td>
			<td><label>txnAmountValue</label></td>
			<td><input type="text" id="txnAmount[value]scp" tabindex="1"
					   maxlength="200" size="100" value="2" name="txnAmount[value]"></td>
		</tr>

		<tr>
			<td>27</td>
			<td><label>txnAmountCurrency</label></td>
			<td><input type="text" id="txnAmount[currency]scp" tabindex="1"
					   maxlength="200" size="100" value="INR" name="txnAmount[currency]"></td>
		</tr>

		<tr>
			<td>28</td>
			<td><label>seqNumber</label></td>
			<td><input type="text" id="seqNumberscp" tabindex="1"
					   maxlength="200" size="100" value="" name="seqNumber"></td>
		</tr>

		<tr>
			<td>29</td>
			<td><label>riskExtendInfo</label></td>
			<td><input type="text" id="riskExtendInfoscp" size="100" tabindex="1" value="" name="riskExtendInfo"></td>
		</tr>

		<tr>
			<td>30</td>
			<td><label>extendInfo</label></td>
			<td><input type="text" id="extendInfoscp" size="100" tabindex="1" value="" readonly name="extendInfo"></td>

		</tr>
		<tr>
		<td>31</td>
		<td><label>Additional Info</label></td>
		<td><input type="text" id="extendInfo[additionalInfo]scp" tabindex="1" size="100" value="" name="extendInfo[additionalInfo]"></td>
		</tr>

		<tr>
			<td>32</td>
			<td><label>website</label></td>
			<td><input type="text" id="websitescp" size="100" tabindex="1" value="retail" name="website"></td>

		</tr>
<%--		<tr>
			<td>24</td>
			<td><label>RISK_VERIFY_FLOW</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="RISK_VERIFY_FLOW"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>token</label></td>
			<td><input type="text" id="" tabindex="1"

					   maxlength="200" size="100" value="" name="token"></td>
		</tr>--%>

<%--		<tr>
			<td>25</td>
			<td><label>GV_CONSENT_FLOW</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="GV_CONSENT_FLOW"></td>
		</tr>--%>
<%--		<tr>
			<td>25</td>
			<td><label>bankName</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="bankName"></td>
		</tr>
		<tr>
			<td>25</td>
			<td><label>upiAccRefId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="upiAccRefId"></td>
		</tr>--%>
		<tr>
			<td></td>
			<td>
				<button value="Pay" type="button" onclick="validateNativeForm(event,'scanPay')">Pay</button>
			</td>
			<td></td>
		</tr>
		</tbody>
	</table>
</form>

<h3>SBI SANITY</h3>
<form id="SbiForm" method="post" action="processTransaction" name ="SBITRANSACTIONFORM">
	<table border="1">
		<tbody>
		<tr>
			<th>S.No</th>
			<th>Label</th>
			<th>Value</th>
		</tr>
		<tr>
			<td>1</td>
			<td><label>MID ::*</label></td>
			<td><input id="MID" tabindex="1" maxlength="30" size="30"
					   name="MID" autocomplete="off" value="">
			</td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>ENC_DATA</label></td>
			<td><input ENC_DATA="Auto" type="text" name="ENC_DATA"></td>
		</tr>
		<tr>
			<td></td>
			<!--	<td><input value="Pay in Iframe" type="submit" onclick="javascript:payInIframe();"></td>-->
			<!--<td><input value="Pay" type="submit" ></td>-->
			<td><input value="Pay" type="button" onclick="validateSbiForm();"></td>
			<td>
				<!--<input value="CalculateChecksum" type="button"	onclick="javascript:calculateCheckSum();"> -->
			</td>
		</tr>
		</tbody>
	</table>
</form>

<h4>APP INVOKE</h4>
<form id="appInvokeForm" method="post" action="api/v2/showPaymentPage" style="background-color:#abfa2d">
	<table border="1">
		<tbody>
		<tr>
			<td><input value="Pay" type="button" onclick="validateAppInvokeForm();"></td>
		</tr>

		<tr>
			<th>S.No</th>
			<th>Label</th>
			<th>Value</th>
		</tr>

		<tr>
			<td>1</td>
			<td><label>mid</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="mid"></td>
		</tr>
		<tr>
			<td>2</td>
			<td><label>orderId</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="orderId"></td>
		<tr>
			<td>3</td>
			<td><label>txnToken</label></td>
			<td><input type="text" id="" tabindex="1"
					   maxlength="200" size="100" value="" name="txnToken"></td>
		</tr>
		</tbody>
	</table>
</form>

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
	function validateWrapperForm() {
		document.getElementById("WrapperForm").submit();
	}
</script>
</body>
</html>
