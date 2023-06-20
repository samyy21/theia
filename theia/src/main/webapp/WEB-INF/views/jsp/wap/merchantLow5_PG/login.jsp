<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	
	<c:set var="show_login_success" value="1" scope="session"></c:set>

	<div class="hide" id="login-stitch">

	<c:if test="${!(themeInfo.subTheme eq 'idea' || themeInfo.subTheme eq 'bigBasket')}">
					<div class="login-strip">

						<div class="center inline-display left expSelect  ">
							<div class="fl inline-display wid16">
								<img src="${ptm:stResPath()}images/wap/merchantLow5/wallet.png" class="fl pt3 wid70" alt="" title="">
							</div>
							<div class="fl inline-display wid80 login-content" style="position:relative;"><span style="width:70%;display:inline-block;">Login to use Paytm balance or your saved cards</span>

									<%--<div class="mt5"> --%>

								<c:if test="${loginInfo.loginWithOtp}">
									<button class="login-button ml16 login-button-style" onclick="showAuthOTPView('otp'); return false;">Login</button>
									<%--	<button class="sign-up-button ml4" onclick="showAuthOTPView('otp'); return false;">Create Paytm Account</button> --%>
								</c:if>

								<c:if test="${!loginInfo.loginWithOtp}">
									<button class="login-button ml16 login-button-style" onclick="showAuthView('login'); return false;">Login</button>
									<%--	<button class="sign-up-button ml4" onclick="showAuthView('register'); return false;">Create Paytm Account</button> --%>
								</c:if>
							</div>
					</div>


	</div>
	</c:if>
	</div>


	<div class="notification alert loading blue-text mb10 " <c:if test="${themeInfo.subTheme eq 'idea' || themeInfo.subTheme eq 'bigBasket'}"> style="display: none"</c:if> id="login-wait">
		<span class="">
			Checking for login. Please Wait...
		</span>
		<img src="${ptm:stResPath()}images/wap/merchantLow/loading.gif" style="height:22px;margin-bottom: -6px;" class="mr10">
	</div>
		
	<c:if test ="${!loginInfo.loginFlag && loginInfo.autoLoginAttempt}">
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
