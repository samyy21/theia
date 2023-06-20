<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="config.jsp"%>

<!DOCTYPE html>

<html>
<head>
<title>Payment Process</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link type="image/x-icon" rel="shortcut icon"
	href="https://staticgw1.paytm.in/1.4/images/web/Paytm.ico">
<style type="text/css">
html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p,
	blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn,
	em, font, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup,
	tt, var, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table,
	caption, tbody, tfoot, thead, tr, th, td {
	margin: 0;
	padding: 0;
	border: 0;
	outline: 0;
	font-weight: inherit;
	font-style: inherit;
	font-size: 100%;
	font-family: inherit;
	vertical-align: baseline
}

:focus {
	outline: 0
}

body {
	line-height: 1;
	color: black;
	background: white
}

ol, ul {
	list-style: none
}
/*Webrupee*/
@font-face{font-family: 'WebRupee';src: url('/theia/resources/fonts/WebRupee.V2.1.eot');src: local('WebRupee'), url('/theia/resources/fonts/WebRupee.V2.1.ttf') format('truetype'),  url('/theia/resources/fonts/WebRupee.V2.1.woff') format('woff'), url('/theia/resources/fonts/WebRupee.V2.1.svg') format('svg');font-weight: normal;font-style: normal;}
.WebRupee{font-family: 'WebRupee';}

table {
	border-collapse: separate;
	border-spacing: 0
}

caption, th, td {
	text-align: left;
	font-weight: normal
}

blockquote:before, blockquote:after, q:before, q:after {
	content: ""
}

blockquote, q {
	quotes: "" ""
}

.clear {
	clear: both;
	display: inline-block
}

.clear:after, .container:after {
	content: ".";
	display: block;
	height: 0;
	clear: both;
	visibility: hidden
}

* html .clear {
	height: 1%
}

.clear {
	display: block
}

input[type=button], input[type=submit], input[type=text] {
	-webkit-appearance: none;
	-webkit-border-radius: 0;
}

@font-face {
	font-family: 'OpenSans';
	font-style: normal;
	font-weight: 600;
	src: local('Open Sans Semibold'), local('OpenSans-Semibold'),
		url(https://themes.googleusercontent.com/static/fonts/opensans/v6/MTP_ySUJH_bn48VBG8sNSnhCUOGz7vYGh680lGh-uXM.woff)
		format('woff');
}

@font-face {
	font-family: 'OpenSans';
	font-style: normal;
	font-weight: 400;
	src: local('Open Sans'), local('OpenSans'),
		url(https://themes.googleusercontent.com/static/fonts/opensans/v6/cJZKeOuBrn4kERxqtaUH3T8E0i7KZn-EPnyo3HZu7kw.woff)
		format('woff');
}

body {
	font-family: 'OpenSans', arial, serif, sans-serif;
	margin: 0;
	padding: 0;
}

.main {
	width: 100%;
	box-sizing: border-box;
	-moz-box-sizing: border-box;
	-webkit-box-sizing: border-box;
	padding: 0 15px;
}

.header-text{
	text-align: center;
	font-size: 18px;
}
header {
	padding: 20px;
	border-bottom: 1px solid #ccc;
}

.paymentBox {
	display: block;
}

.paymentBox li {
	display: block;
	font-size: 15px;
	margin: 20px 0;
}

.borderBtm {
	border-bottom: 1px solid #ccc;
	width: 100%;
}

.grey {
	color: #ccc;
	font-size: 12px;
}

.fl {
	float: left;
}

.fr {
	float: right;
}

.clear {
	clear: both;
}

.txtBox {
	font-size: 14px;
	line-height: 22px;
}

.txtBox strong {
	font-weight: 600;
}

.b {
	font-weight: 600;
}

.formBox {
	width: 100%;
	margin-top: 30px;
	text-align: center;
}

.submitBtn {
	width: 100%;
	padding: 15px;
	color: #fff;
	border-radius: 2px;
	font-size: 18px;
	background-color: #00b9f5;
	border: solid 1px #00b9f5;
	cursor: pointer;
}

.backPaymentBtn {
	margin-top: 20px;
	font-size: 16px;
	font-weight: normal;
	font-style: normal;
	font-stretch: normal;
	line-height: normal;
	letter-spacing: normal;
	text-align: center;
	color: #00b9f5;
	border: none;
	outline: 0;
	background: none;
	cursor: pointer;
}
.pr2{
	padding-right: 2px;
}
.credit-card-fee{
    color: #666;
    font-size: 10px;
    font-family: OpenSans;
    line-height: 1.5;
    letter-spacing: 0.3px;
}
.credit-card-fee-sub{
	font-family: OpenSans;
	font-size: 10px;
	color: #666;
	line-height: 1.6
}
.trans-id{
	text-align: center;
    font-family: OpenSans;
    font-size: 12px;
    color: #ccc;
    margin-top: 10px;
    margin-left: 10%;
}
</style>

<script type="text/javascript">
function redirectPage() {
	var location = "${pageContext.request.contextPath}" + "/processTransaction"+"?MID=${txnInfo.mid}&ORDER_ID=${txnInfo.orderId}";
	window.location=location;
}
</script>
</head>
<body>
	<header>
		<div style="width:10%;float: left;">
			<img src="/theia/resources/images/wap/ic-back.png" alt="back"/>
		</div>
		<div style="width: 90%;">
			<div class="header-text">Confirm Payment</div>
			<div class="trans-id">Transaction ID: ${txnInfo.orderId }</div>
		</div>
	</header>
	<form autocomplete="off" name="postconv-form" method="post"
		action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}">
		<input type="hidden" name="txnID" value="${txnInfo.txnId}" />
		<div class="main">
			<ul class="paymentBox">
				<li><div class="fl">Amount</div>
					<div class="fr"><span class="WebRupee pr2">Rs</span>${txnInfo.txnAmount}</div>
					<div class="clear"></div></li>
				<li><div class="fl">
						Card Fee <span class="grey">(Incl. of service tax)</span>
					</div>
					<div class="fr">+<span class="WebRupee pr2" style="margin-left:10px;">Rs</span>${txnInfo.totalConvenienceCharges} </div>
					<div class="clear"></div></li>
				<li><div class="borderBtm"></div></li>
				<li class="b"><div class="fl">Net Amount to Pay</div>
					<div class="fr"><span class="WebRupee pr2">Rs</span>${txnInfo.totalTransactionAmount}</div>
					<div class="clear"></div></li>
				<li><div class="borderBtm"></div></li>
			</ul>
			<div class="clear"></div>
			<div class="txtBox">
				<p>
					<strong class="credit-card-fee">What is Card Fee?</strong>
				</p>
				<p class="credit-card-fee-sub"> Your card charges us higher fees for offering you benefits including credit period or reward points. </p>
			</div>
			<div class="clear"></div>

			<div class="formBox">
				<button type="submit" class="submitBtn">Pay <span class="WebRupee pr2">Rs</span> ${txnInfo.totalTransactionAmount}</button> 
				<input type=button class="backPaymentBtn" value="Choose Another Payment Option" onclick="redirectPage()">

				<div class="clear"></div>
			</div>
		</div>
		<input type="hidden" name="cacheCardToken" value="${cacheCardToken}">
	</form>
</body>
</html>
