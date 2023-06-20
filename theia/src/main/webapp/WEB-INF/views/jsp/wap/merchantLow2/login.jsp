<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	
	<c:set var="show_login_success" value="1" scope="session"></c:set>

	<div class="notification alert alert-info blue-text mb10 hide" id="login-stitch">
		<span class="b">
			<c:if test="${loginInfo.loginWithOtp}">

				<a id="otp-btn" href="#" class="underline" onclick="showAuthOTPView('otp'); return false;">Login</a> / <a id="otp-signUp-btn" href="#" class="underline" onclick="showAuthOTPView('otp'); return false;">Sign up</a> with Paytm
			</c:if>
			<c:if test="${!loginInfo.loginWithOtp}">
				<a id="login-btn" href="#" class="underline" onclick="showAuthView('login'); return false;">Login</a> / <a id="register-btn" href="#" class="underline" onclick="showAuthView('register'); return false;">Sign up</a> with Paytm
			</c:if>
		</span>
		<span>to use your Patym Cash or Saved cards.</span>
	</div>
	<div class="notification alert alert-info blue-text mb10 " id="login-wait">
		<img src="${ptm:stResPath()}images/wap/merchantLow2/spinner-blue.gif" style="height:22px;margin-right: 10px;" class="fl mr10">
		<span class="">
			Checking for login. Please Wait...
		</span>
	</div>
		
	<c:if test ="${!loginInfo.loginFlag}">
		<iframe id="login-iframe" src="" style="display:none;"></iframe>
		<c:set var="checked_login" value="1" scope="session"></c:set>
	</c:if>
	
	
</c:if>

<c:if test ="${loginInfo.loginFlag && loginInfo.showLoginSuccess}">
	<c:set var="show_login_success" value="0" scope="session"></c:set>
	<div class="slider" id="login-success-alert">
		<div class="notification alert alert-success mb10" >
			<span class="">
				You have successfully logged in!
			</span>
		</div>
	</div>
</c:if>