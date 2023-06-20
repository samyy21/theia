<%@ include file="../../common/config.jsp"%>

<%@ page import="java.util.List" %>
<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Paytm Secure Online Payment Gateway</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />

<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
 <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    
<meta name="format-detection" content="telephone=no">
<link rel="shortcut icon" href="../images/Paytm.ico" type="image/x-icon" />
<c:set var="port" value="" />
<c:if test="${empty header['PRX']}">
	<c:set var="port" value=":${pageContext.request.serverPort}" />
</c:if>
<!-- <base href="/oltp-web/" /> -->
	<link href="/resources/css/wap/iphone/mobile.css" rel="stylesheet" type="text/css" />
	<script src="/resources/js/radio.js" type="text/javascript"></script>
	<script src="/resources/js/jquery.js"></script>
	<script src="/resources/js/jquery.mobile-1.2.0.min.js"></script>
	<script src="/resources/js/wap/iphone/touchslider.js"></script>
	<script type="text/javascript" src="/resources/js/wap/iphone/functions.js"></script>
	
</head>
	
<body>
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>
	<div data-role="page" data-theme="i"> 
		<%@ include file="tab.jsp" %>
		
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
		
		<c:if test="${walletEnabled}">
			<%@ include file="ppi.jsp" %>
		</c:if>
		<%--
		<c:if test="${1 eq otherPaymentMethodEnabled}">
			<%@ include file="other.jsp" %>
		</c:if> --%>
		
	<c:if test="${txnInfo.retry}">
		<script>
			$(document).ready(function(){
				var msg = "<c:out value="${txnInfo.displayMsg}" escapeXml="true" />";
				if(msg != null && msg.trim().length != 0) {
					msg = msg.replace(". ", ".</br>")
				}
				$('#retryMsg').get(0).innerHTML = msg;
			});
		</script>
	</c:if>
	</div>
	
</body>

</html>
