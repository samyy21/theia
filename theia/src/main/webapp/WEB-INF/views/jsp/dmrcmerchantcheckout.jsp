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
		document.getElementById("target").submit();
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
	
	
</script>
</head>
<body onload="javascript:assignChecksum(); javascript:generateDMRCPayload();">
	<iframe name="myiframe"
		style="display: none; float: right; width: 640px; height: 600px; margin-right: 2%;"></iframe>
	<h1>DMRC Check Out Page</h1>
	<form id="target" method="post" action="customProcessTransaction?MID=">
		<table border="1">
			<tbody>
				<tr>
					<td>
					</td>
					<td><input value="Pay" type="button" onclick="validateForm(); appendMidInFormAction();"></td>
					<td>
						<input type="button" value="Copy Payment Flow" onclick="javascript:copyValueInForm()" />
					</td>
					<td>
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
					<td>-2</td>
					<td><label>client_id::*</label></td>
					<td><input id="client_id" tabindex="1" maxlength="20" size="20"
							   name="client_id" autocomplete="off" value="FBClient1"></td>
				</tr>

				<tr>
					<td>-1</td>
					<td><label>request_id::*</label></td>
					<td><input id="request_id" tabindex="1" maxlength="20" size="20"
							   name="request_id" autocomplete="off" type="number"></td>
				</tr>

				<tr>
					<td>1</td>
					<td><label>CheckSum ::*</label></td>
					<td><input type="text" id="CHECKSUMHASH" tabindex="1"
						maxlength="12" size="10" value="" name="CHECKSUMHASH"
						autocomplete="off" readonly="readonly"></td>
				</tr>
				<tr>
					<td>2</td>
					<td><label>ORDER_ID::*</label></td>
					<td><input id="orderid" tabindex="1" maxlength="20" size="20"
						name="ORDER_ID" autocomplete="off" value="PARCEL"></td>
				</tr>
				<tr>
					<td>4</td>
					<td><label>MID ::*</label></td>
					<td><input id="MID" tabindex="1" maxlength="20" size="20" name="MID" autocomplete="off" value="HYBADD50520222544592">
					</td>
				</tr>
				<tr>
					<td>0</td>
					<td><label>Data for DMRC</label></td>

					<script>
                        function generateDMRCPayload(){
                            var orderId = document.getElementById("orderid").value;
                            var data = '{"merchantTxnId":"'+ orderId + '","paymentMode":"CREDIT_CARD","returnUrl":"http://localhost:3741/Gateway_payment/A_returnurl_Thales.aspx","notifyUrl":"http://localhost:3741/Gate way_Payment/notifyurl_Thales.aspx","secSignature":"01234567890123456789","email":"jean.leonetti@free.fr","reqtime":"63582602222894","currency":"INR","orderAmount":"1","firstNa me":"Leonetti","lastName":"Jean","addressStreet1":"6 Rue des Lilas","addressCity":"Janakpuri","addressState":"Delhi","addressZip":"45678","phoneNumber" :"0670556677","customParameters":{"EngravedID":"11000000"}}';
                            document.getElementById('data').value = data;
						};
						function appendMidInFormAction() {
							var mid = document.getElementById("MID").value;
							var dom = document.getElementById("target");
							dom.setAttribute('action', "customProcessTransaction?MID=" + mid);
							document.getElementById("MID").remove();
                            document.getElementById("target").submit();
                        };
					</script>


					<td>
						<input type='text' id='data' name='data'  value=''/>
					</td>
				</tr>



				<tr>
					<td>12</td>
					<td><label>EMAIL ::*</label></td>
					<td><input id="EMAIL" tabindex="9" maxlength="50" size="50"
						name="EMAIL" autocomplete="off" value="">
					</td>
				</tr>
			</tbody>
		</table>
		* - Mandatory Fields
	</form>




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
