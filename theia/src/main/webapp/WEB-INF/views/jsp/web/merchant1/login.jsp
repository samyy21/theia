<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	
	<c:set var="show_login_success" value="1" scope="session"></c:set>
	<div id="login-box" >
		<div class="notification alert alert-info blue-text-3 mb20 hide" id="login-stitch">

			<div class="img img-walletlogo"></div>
			<span class="b">
				<c:if test="${loginInfo.loginWithOtp}">
					<a id="otp-btn" href="#" class="underline">Login</a> / <a id="otp-signUp-btn" href="#" class="underline">Sign up</a> with Paytm
				</c:if>
				<c:if test="${!loginInfo.loginWithOtp}">
					<a id="login-btn" href="#" class="underline">Login</a> / <a id="register-btn" href="#" class="underline">Sign up</a> with Paytm
				</c:if>
			</span>
			<span>to use your Paytm or Saved Cards and to avail applicable offer.</span>
		</div>
	
		<div class="notification alert alert-info blue-text-3 mb20 " id="login-wait">
			<img src="${ptm:stResPath()}images/spinner-2.gif" style="height:22px" class="fl mr10">
			<span class="b">
				Checking for login. Please Wait...
			</span>
		</div>
	</div>

</c:if>

<c:if test ="${loginInfo.loginFlag && loginInfo.showLoginSuccess}">
	<c:set var="show_login_success" value="0" scope="session"></c:set>
	<div class="slider" id="login-success-alert">
		<div class="notification alert alert-success" >
			<span class="b">
				You have successfully logged in!
			</span>
		</div>
	</div>
</c:if>