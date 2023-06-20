<%--
  Created by IntelliJ IDEA.
  User: rajansingh
  Date: 20/12/17
  Time: 9:52 AM
  To change this template use File | Settings | File Templates.
--%>
<link type="image/x-icon" rel="shortcut icon" href="${ptm:stResPath()}images/web/Paytm.ico" />

<script type="text/javascript">
  function backButtonOverride() {
    setTimeout("backButtonOverrideBody()", 1);
  }

  function backButtonOverrideBody() {
    try {
      history.forward();
    }
    catch (e) {
    }
    setTimeout("backButtonOverrideBody()", 500);
  }

  history.forward(0);

</script>
<script>
  var MERCHANT_USER_ID='';
</script>
<c:if test="${not empty loginInfo.user}">
  <script>
    var MERCHANT_USER_ID='${loginInfo.user.payerUserID}';
  </script>
</c:if>
<%String useMinifiedAssets=ConfigurationUtil.getProperty("context.useMinifiedAssets"); %>
<% if(useMinifiedAssets.equals("N")){ %>
<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant5/style.css" />

<!--[if lte IE 9 ]>
<script type="text/javascript" src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
<![endif]-->

<%-- NOTE : zepto will not run if jquery is already loaded (fix for IE) --%>
<script type="text/javascript" src="${ptm:stResPath()}js/zepto-1.1.3.js"></script>
<script type="text/javascript" src="${ptm:stResPath()}js/zepto-data-module.js"></script>
<script type="text/javascript" src="${ptm:stResPath()}js/bootstrap-checkbox.js"></script>

<c:if test="${txnInfo.qrDetails.isQREnabled}">
  <script type="text/javascript" src="${ptm:stResPath()}vendor/socket.io/socket.io.min.js"></script>
</c:if>

<script type="text/javascript" src="${ptm:stResPath()}js/web/merchant5/functions.js"></script>
<%--<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>--%>
<% } else { %>



<link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant5/style.min.css" />

<!--[if lte IE 9 ]>
<script type="text/javascript"  src="${ptm:stResPath()}js/jquery-1.10.1.min.js"></script>
<![endif]-->

<c:if test="${txnInfo.qrDetails.isQREnabled}">
  <script type="text/javascript" src="${ptm:stResPath()}vendor/socket.io/socket.io.min.js"></script>
</c:if>


<script type="text/javascript"  src="${ptm:stResPath()}js/web/merchant5/functions.min.js" ></script>
<%--
<script type="text/javascript" src="${ptm:stResPath()}js/logger-cookie.js"></script>
--%>
<% } %>

<c:set var="themeInfo.subTheme" value="${themeInfo.subTheme}"></c:set>
<%-- <c:set var="themeInfo.subTheme" value="airtel"></c:set> --%>



<div id="csrf" data-value="${csrfToken.token}"></div>
