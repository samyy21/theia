<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Paytm</title>
	</head>
	<body  leftmargin="0" topmargin="0">
		<form name="frm" method="POST" action="${responseURL}">
			<input type="hidden" name="status" value="${txnInfo.txnStatus}">
			<input type="hidden" name="message" value="${txnInfo.comments}">
			<input type="hidden" name="paytmTxnId" value="${txnInfo.txnId}">
			<input type="hidden" name="appTxnId" value="${txnInfo.orderId}">
			<input type="hidden" name="responseCode" value="${requestScope.infoTo.responseCode}">
			<input type="hidden" name="appUserId" value="${txnInfo.mid}">
			<input type="hidden" name="amount" value="${txnInfo.txnAmt}">
			<input type="hidden" name="paymentMode" value="${requestScope.infoTo.payTypeId}">
			<input type="hidden" name="binNumber" value="${requestScope.infoTo.binNumber}">
			<input type="hidden" name="bankTxnId" value="${requestScope.infoTo.processorIdentifier}">
			<input type="hidden" name="other" value="${requestScope.infoTo.other}">
			<input type="hidden" name="cardType" value="${requestScope.infoTo.cardType}">
			<input type="hidden" name="gatewayName" value="${requestScope.infoTo.gatewayName}">
		</form>
			
		<br><br><br><br><br><br>
			<table border="0" bgcolor="#FFFFFF" align="left"  style="width: 250;"><tr><td align="center">
				<FONT FACE="arial"  color="#6E6a6a" STYLE="font-size: 10pt"><b>Please wait</b></FONT>
				<br>
			<br><br>
			
			<img src="images/bar.gif" width="220" height="19" border="0">
				</td>
				</tr>
			</table>
		<script language="javascript">
			document.frm.submit();
		</script>
	</body>
</html>