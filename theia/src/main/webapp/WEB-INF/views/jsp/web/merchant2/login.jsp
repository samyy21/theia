<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	
	<c:set var="show_login_success" value="1" scope="session"></c:set>

	<div class="notification alert alert-info blue-text-3 mb20 hide medium" id="login-stitch">
		<span class="relative">
			<img src="${ptm:stResPath()}images/web/merchant2/idea.png" class="mr10">
		</span>
		<span class="b">
			<a id="login-btn" href="#" class="underline">Login</a> / <a id="register-btn" href="#" class="underline">Sign up</a>
		</span>
		<span>to use your Paytm or Saved Cards and to avail applicable offer.</span>
	</div>
	<div class="notification alert alert-info blue-text-3 mb20 medium" id="login-wait">
		<span class="relative">
			<img src="${ptm:stResPath()}images/web/merchant2/spinner-blue.gif" class="mr10">
		</span>
		<span class="b">
			Checking for login. Please Wait...
		</span>
	</div>
</c:if>

<c:if test ="${loginInfo.loginFlag && loginInfo.showLoginSuccess}">
	<c:set var="show_login_success" value="0" scope="session"></c:set>
	<div class="slider" id="login-success-alert">
		<div class="notification alert alert-success medium" >
			<span class="b">
				You have successfully logged in!
			</span>
		</div>
	</div>
</c:if>