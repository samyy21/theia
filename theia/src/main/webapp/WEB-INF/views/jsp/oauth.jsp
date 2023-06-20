<%@ include file="common/config.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta name="robots" content="noindex,nofollow" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		<meta http-equiv="x-ua-compatible" content="IE=9" />
	</head>
	<body>
		<script type="text/javascript">
			var  parent = window.parent || window.top;
			parent.location= "processTransaction?${queryStringForSession}&oauth=true";	
			
			setInterval(function(){
				parent.location = "processTransaction?${queryStringForSession}&oauth=true";	
			}, 4000);
		</script>
	</body>
</html>