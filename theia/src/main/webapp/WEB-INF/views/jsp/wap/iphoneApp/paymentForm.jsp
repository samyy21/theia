<%@ include file="../../common/config.jsp"%>

<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Paytm Secure Online Payment Gateway</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<meta name="format-detection" content="telephone=no">
<link rel="shortcut icon" href="../images/Paytm.ico" type="image/x-icon" />
<c:set var="port" value="" />
<c:if test="${empty header['PRX']}">
	<c:set var="port" value=":${pageContext.request.serverPort}" />
</c:if>
<!-- <base href="/oltp-web/" /> -->
<link href="css/wap/iphoneApp/mobile.css" rel="stylesheet" type="text/css" />
<script src="js/jquery.js" type="text/javascript"></script>
<script src="js/radio.js" type="text/javascript"></script>
<script type="text/javascript" src="js/wap/iphoneApp/functions.js"></script>
<script type="text/javascript">	
	function cancelTxn() {
		window.location.href="cancelTransaction";
	}
 </script>
</head>
<%
	if(session.getAttribute("txnTransientId") == null){
		response.sendRedirect("https://secure.paytm.in/error");
	}
%>
<body>
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>

	<c:if test="${txnInfo.retry}">
		<div class="alert">
			<div class="fl">
				<img title="" alt="" src="images/wap/alert.png">
			</div>
			<div class="fl alert-txt" id="retryMsg">
			</div>
			<div class="clear"></div>
		</div>
	</c:if>
		
	<p class="pad">Order ready to be processed for
		Rs.${txnInfo.txnAmount}. Please complete your payment details.</p>
	
	<c:if test="${saveCardEnabled  && ('5' eq paymentType || '1' eq paymentType)}">
		<div class="margin center" style="width:265px;">
			<a href="jsp/wap/iphoneApp/paymentForm.jsp?txn_Mode=SC">
				<div class="tab <c:if test="${'5' ne paymentType}"> tab-inactive</c:if>">Saved card</div>
			</a>
			<a href="jsp/wap/iphoneApp/paymentForm.jsp?txn_Mode=CC">
				<div class="tab <c:if test="${'1' ne paymentType}"> tab-inactive</c:if>">Other card</div>
			</a>
			<div class="clear"></div>
		</div>
	</c:if>
	<c:if test="${saveCardEnabled}">
		<%@ include file="savedCard.jsp" %>
	</c:if>
	
	<c:if test="${ccEnabled}">
		<%@ include file="cc.jsp" %>
 	</c:if>
	<c:if test="${dcEnabled || atmEnabled}">
		<%@ include file="dc.jsp" %>
	</c:if>
	<c:if test="${netBankingEnabled}">
		<%@ include file="nb.jsp" %>
	</c:if>
	<c:if test="${impsEnabled}">
		<%@ include file="imps.jsp" %>
	</c:if>
	
	<c:if test="${txnInfo.retry}">
		<script>
			$(document).ready(function(){
				var msg = "<c:out value="${requestScope.displayMsg}" escapeXml="true" />";
				if(msg != null && msg.trim().length != 0) {
					msg = msg.replace(". ", ".</br>")
				}
				$('#retryMsg').get(0).innerHTML = msg;
			});
		</script>
	</c:if>
</body>
</html>
