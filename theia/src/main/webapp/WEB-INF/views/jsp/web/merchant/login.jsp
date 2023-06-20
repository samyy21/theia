<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570' && !(themeInfo.subTheme eq 'bigBasket')}">
	
	<c:set var="show_login_success" value="1" scope="session"></c:set>
	<div id="login-box" >
	<div class="notification alert alert-info blue-text-3 mb20 hide" id="login-stitch">
			<div class="img img-walletlogo"></div>
			<span class="b">
				<c:if test="${loginInfo.loginWithOtp eq true }">
					<a id="otp-btn" href="#" class="underline">Login</a>
				</c:if>
				<c:if test="${loginInfo.loginWithOtp ne true }">
					<a id="login-btn" href="#" class="underline">Login</a>
				</c:if>

			</span>
			<span> ${loginInfo.loginStripText}</span>
	</div>
	
	<div class="notification alert loading blue-text-3 mb20 " id="login-wait">
		<span class="b">
			Checking for login. Please Wait...
		</span>
		<img src="${ptm:stResPath()}images/loading_dots.gif" style="height:22px;margin-bottom: -6px;" class="mr10">
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