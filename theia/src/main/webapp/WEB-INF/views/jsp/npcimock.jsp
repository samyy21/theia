<%@ page session="false" language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>NPCI MOCK TEST</title>

<style type="text/css">
  span.error{
    color: red;
    margin-left: 5px;
  }
</style>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
</head>
<body>
    <form name="npciMockForm"  id="npciMockForm" enctype="application/json" autocomplete="on">
        <table>
        <tr>
            <td>0</td>
            <td><label>ORDER_ID ::</label></td>
            <td><input type="text" id="ORDER_ID" tabindex="1"
                       maxlength="20000" size="100" value="nativesip12784ft1" name="orderId"
                       autocomplete="orderId"></td>
        </tr>
        <tr>
            <td>1</td>
            <td><label>TXN_AMOUNT ::</label></td>
            <td><input type="text" id="TXN_AMOUNT" tabindex="1"
                       maxlength="20000" size="100" value="0" name="trxnAmount"
                       autocomplete="trxnAmount"></td>
        </tr>
        <tr>
            <td>2</td>
            <td><label>MERCHANT_ID ::</label></td>
            <td><input type="text" id="MERCHANT_ID" tabindex="1"
                maxlength="20000" size="100" value="KKCcEX65760877396743" name="mid"
                autocomplete="mid"></td>
        </tr>
        <tr>
            <td>3</td>
            <td><label>SUBS_AMOUNT_TYPE ::</label></td>
            <td><input type="text" id="SUBS_AMOUNT_TYPE" tabindex="1"
                maxlength="20000" size="100" value="VARIABLE" name="subscriptionAmountType"
                autocomplete="subscriptionAmountType"></td>
        </tr>
        <tr>
            <td>4</td>
            <td><label>SUBS_MAX_AMOUNT ::</label></td>
            <td><input type="text" id="SUBS_MAX_AMOUNT" tabindex="1"
                maxlength="20000" size="100" value="123455" name="subscriptionMaxAmount"
                autocomplete="subscriptionMaxAmount"></td>
        </tr>
        <tr>
            <td>5</td>
            <td><label>SUBS_FREQUENCY ::</label></td>
            <td><input type="text" id="SUBS_FREQUENCY" tabindex="1"
                maxlength="20000" size="100" value="1" name="subscriptionFrequency"
                autocomplete="subscriptionFrequency"></td>
        </tr>
        <tr>
            <td>6</td>
            <td><label>SUBS_FREQUENCY_UNIT ::</label></td>
            <td><input type="text" id="SUBS_FREQUENCY_UNIT" tabindex="1"
                   maxlength="20000" size="100" value="MONTH" name="subscriptionFrequencyUnit"
                   autocomplete="subscriptionFrequencyUnit"></td>
        </tr>
        <tr>
            <td>7</td>
            <td><label>SUBS_START_DATE ::*</label></td>
            <td><input type="text" id="SUBS_START_DATE" tabindex="1"
                maxlength="20000" size="100" value="" name="subscriptionStartDate"
                autocomplete="subscriptionStartDate"></td>
        </tr>
        <tr>
            <td>8</td>
            <td><label>SUBS_EXPIRY_DATE ::*</label></td>
            <td><input type="text" id="SUBS_EXPIRY_DATE" tabindex="1"
                maxlength="20000" size="100" value="" name="subscriptionExpiryDate"
                autocomplete="subscriptionExpiryDate"></td>
        </tr>
        <tr>
            <td>9</td>
            <td><label>ACCOUNT_NUMBER ::</label></td>
            <td><input type="text" id="ACCOUNT_NUMBER" tabindex="1"
                maxlength="20000" size="100" value="123456789" name="ACCOUNT_NUMBER"
                autocomplete="ACCOUNT_NUMBER"></td>
        </tr>
        <tr>
            <td>10</td>
            <td><label>BANK_IFSC ::</label></td>
            <td><input type="text" id="BANK_IFSC" tabindex="1"
                maxlength="20000" size="100" value="HDFC0001" name="BANK_IFSC"
                autocomplete="BANK_IFSC"></td>
        </tr>
        <tr>
            <td>11</td>
            <td><label>ACCOUNT_TYPE ::</label></td>
            <td><input type="text" id="ACCOUNT_TYPE" tabindex="1"
                maxlength="20000" size="100" value="OTHERS" name="ACCOUNT_TYPE"
                autocomplete="ACCOUNT_TYPE"></td>
        </tr>
        <tr>
            <td>12</td>
            <td><label>USER_NAME ::</label></td>
            <td><input type="text" id="USER_NAME" tabindex="1"
                maxlength="20000" size="100" value="TESTUSER" name="USER_NAME"
                autocomplete="USER_NAME"></td>
        </tr>
        <tr>
            <td></td>
            <td><button type="submit">Submit</button></td>
        </tr>
        </table>
    </form>
<!-- Result Container  -->
<div id="resultContainer" style="display: none;">
</div>
<div id="formContainer" style="display: none;">
    <form name="npciForm1"  id="npciForm1" enctype="application/json" action="https://103.14.161.144:8086/onmags/sendRequest" method="post">
        <table>
            <tr>
                <td>1</td>
                <td><label>MerchantID ::</label></td>
                <td><input type="text" id="merchantId1" tabindex="1"
                           maxlength="20000" size="100" value="" name="MerchantID"
                           readonly="readonly"></td>
            </tr>
            <tr>
                <td>2</td>
                <td><label>MandateReqDoc ::</label></td>
                <td><input type="text" id="mandateReqDoc1" tabindex="1"
                           maxlength="20000" size="100" value="" name="MandateReqDoc"
                           readonly="readonly"></td>
            </tr>
            <tr>
                <td>3</td>
                <td><label>CheckSumVal ::</label></td>
                <td><input type="text" id="checkSumVal1" tabindex="1"
                           maxlength="20000" size="100" value="" name="CheckSumVal" readonly="readonly"></td>
            </tr>
            <tr>
                <td>4</td>
                <td><label>BankID ::</label></td>
                <td><input type="text" id="bankId1" tabindex="1"
                           maxlength="20000" size="100" value="" name="BankID" readonly="readonly"></td>
            </tr>
            <tr>
                <td>5</td>
                <td><label>AuthMode ::</label></td>
                <td><input type="text" id="authMode1" tabindex="1"
                           maxlength="20000" size="100" value="" name="AuthMode" readonly="readonly"></td>
            </tr>
            <tr>
                <td></td>
                <td><input type="submit" id="npciRedirection1" value="Click here to continue"></td>
            </tr>
    </table>
    </form>
</div>
</body>
<script type="text/javascript">
$(function() {
    /*  Submit form using Ajax */
    $("#npciMockForm").submit(function(e) {

        //Prevent default submission of form
        e.preventDefault();

        var data = $("#npciMockForm").serializeArray();
        //console.log(data);
        var payload = {},obj = {};
        if(data && data.length > 0){
            data.forEach(function (item) {
                obj[item.name] = item.value;
            });
        }

        obj["requestType"] = "NATIVE_MF_SIP";
        obj["websiteName"] = "TEST_SIP";

        var txnAmount = {
                            "value": obj["trxnAmount"],
                            "currency": "INR"
                        };
        //obj.remove("trxnAmount");

        obj["txnAmount"] = txnAmount;

        var userInfo = {
                        "custId": "1107196087",
                        "mobile": "7017658313",
                        "email": "apst41@gmail.com",
                        "firstName": "ajay",
                        "lastName": "tomar"
                        };

        obj["userInfo"] = userInfo;

        var paytmSsoToken =  "";
        var callbackUrl = "https://pg-staging.paytm.in/MerchantSite/bankResponse";

        obj["paytmSsoToken"] = paytmSsoToken;
        obj["callbackUrl"] = callbackUrl;

        var extendInfo = {
                        "udf1": "",
                        "udf2": "",
                        "udf3": "",
                        "mercUnqRef": "",
                        "comments": ""
                    };
        obj["extendInfo"] = extendInfo;

        obj["requestType"] = "NATIVE_MF_SIP";
        //obj["mid"] = "KKCcEX65760877396743";
        obj["subscriptionGraceDays"]= "5";
        obj["subscriptionEnableRetry"]= "1";
        obj["subscriptionRetryCount"]= "3";
        obj["subscriptionPaymentMode"]= "";

        //console.log(obj);

        var payload = {
            "head":{
                "version":"v1",
                "requestTimestamp":Date.now(),
                "channelId":"WEB",
                "signature": "CH"
            },
            "body":obj
        };

        //console.log(payload);

        $.post({
            url : "api/v1/subscription/create?mid=" + obj["mid"] +"&orderId=" + obj["orderId"],
            data : JSON.stringify(payload),
            contentType:"application/json",
            success : function(res) {
                //console.log(res);
                if(res.body){
                    //Set response
                    $('#resultContainer pre code').text(JSON.stringify(res.body));
                    //$('#resultContainer').show();

                    var ptcPayload = {};
                    ptcPayload["mid"] = obj["mid"];
                    ptcPayload["orderId"] = obj["orderId"];
                    ptcPayload["channelId"] = "WEB";
                    ptcPayload["authMode"] = "otp";
                    ptcPayload["txnToken"] = res.body.txnToken;
                    ptcPayload["SUBSCRIPTION_ID"] = res.body.subscriptionId;
                    ptcPayload["paymentMode"] = "BANK_MANDATE";
                    ptcPayload["cardInfo"] = "%7C4718650100010336%7C361%7C092021";
                    ptcPayload["addMoney"] = "0";
                    ptcPayload["WEBSITE"] = "Retail";
                    ptcPayload["REQUEST_TYPE"] = "NATIVE_MF_SIP";
                    ptcPayload["channelCode"] = "HDFC";
                    ptcPayload["account_number"] = obj["ACCOUNT_NUMBER"];
                    ptcPayload["bankIfsc"] = obj["BANK_IFSC"];
                    ptcPayload["ACCOUNT_TYPE"] = obj["ACCOUNT_TYPE"];
                    ptcPayload["USER_NAME"] = obj["USER_NAME"];

                    //console.log(ptcPayload);

                    $.post({
                        url : "api/v1/processTransaction?mid=" + obj["mid"] +"&orderId=" +obj["orderId"],
                        data : ptcPayload,
                        contentType : "application/x-www-form-urlencoded",
                        success : function(res) {
                            //console.log(res);
                            $('#resultContainer').text(res);
                            //$('#resultContainer').show();
                            var parser = new DOMParser();
                            var doc = parser.parseFromString($('#resultContainer')[0].innerHTML, "text/html");
                            var doc2 = parser.parseFromString(doc.body.innerText, "text/html");
                            //console.log(doc2);
                            document.getElementById("merchantId1").value = doc2.getElementById("merchantId").value;
                            document.getElementById("mandateReqDoc1").value = doc2.getElementById("mandateReqDoc").value;
                            document.getElementById("checkSumVal1").value = doc2.getElementById("checkSumVal").value;
                            document.getElementById("bankId1").value = doc2.getElementById("bankId").value;
                            document.getElementById("authMode1").value = doc2.getElementById("authMode").value;
                            $('#formContainer').show();
                        },
                        dataType: 'html'
                    });
                }else{
                    //Set error messages
                    $.each(res.errorMessages,function(key,value){
                        $('input[name='+key+']').after('<span class="error">'+value+'</span>');
                    });
                }
            }
        })
    });
});
</script>
</html>
