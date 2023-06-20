<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Paytm Payment Gateway</title>
</head>
<body>
    <script>
        setInterval(function(){
            window.opener.postMessage("transaction complete","*");
        },1000);
    </script>
</body>
</html>
