<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${themeInfo.subTheme eq 'airtel' || themeInfo.subTheme eq 'idea' || themeInfo.subTheme eq 'bigBasket' || !txnInfo.onus}">
<div class="logo" style="background: #ffffff; padding-bottom: 6px;">

	<%--<c:if test="${themeInfo.subTheme ne 'idea'}">
		<a href="/theia/cancelTransaction" class="fl back-btn" onclick="return doCancel()">
			<c:choose>
				<c:when test="${themeInfo.subTheme eq 'airtel'}">
					<img src="${ptm:stResPath()}images/wap/merchantLow5/back-white.gif" alt="Back" title="Back" />
				</c:when>
				<c:otherwise>
					<img src="${ptm:stResPath()}images/wap/merchantLow5/back.gif" alt="Back" title="Back" />
				</c:otherwise>
			</c:choose>

		</a>
	</c:if>--%>

	<c:if test="${themeInfo.subTheme eq 'idea'}">

		<div id="headerIdea">
			<div class="container">
				<div class="fl">
					<img src="${ptm:stResPath()}images/web/merchant/img-idea-logo.png" style="margin:0px;" alt="" height="40" />
				</div>
				<%--<div class="fr">--%>
					<%--<img src="${ptm:stResPath()}images/web/merchant/img-adityabirla-logo.png" style="margin:0px;" alt="" height="40" />--%>
				<%--</div>--%>
				<div class="clear"></div>
			</div>
			<div class="clear"></div>

		</div>
	</c:if>

	<c:if test="${themeInfo.subTheme eq 'bigBasket'}">

		<div id="headerIdea">
			<div class="container">
				<div class="fl">
					<img src="${ptm:stResPath()}images/web/merchant/bigBasket.png" style="margin:16px 0px;" alt="" height="50" />
				</div>
				<div class="fr">
					<img src="${ptm:stResPath()}images/web/paytm_logo.png" style="margin:16px 0px;" alt="" height="40" />
				</div>
				<div class="clear"></div>
			</div>
			<div class="clear"></div>

		</div>
	</c:if>

	<div class="userview fr">
		<c:if test ="${!loginInfo.loginFlag && walletInfo.walletEnabled}">

			<c:if test="${loginInfo.loginWithOtp eq true }">
				<a class="login-btn" href='${loginUrl}'>Login</a>
				<a class="register-btn" href='${loginUrl}'>Signup</a>
			</c:if>
			<c:if test="${loginInfo.loginWithOtp ne true }">
				<a class="login-btn" href='${loginUrl}'>Login</a>
				<a class="register-btn" href='${loginInfo.oAuthInfoHost}/register?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}'>Signup</a>
			</c:if>



		</c:if>

		<%--<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && walletInfo.walletEnabled && themeInfo.subTheme ne 'idea'}">

			<c:if test = "${!empty loginInfo.user.mobileNumber}">
				<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
			</c:if>

			<c:out value="${displayName}" escapeXml="true" />
		</c:if>--%>
	</div>

	<c:if test="${!(themeInfo.subTheme eq 'idea' || themeInfo.subTheme eq 'bigBasket')}">
		<div id="logo-img">
			<c:choose>
				<c:when test="${themeInfo.subTheme eq 'airtel'}">
					<img src="${ptm:stResPath()}images/wap/merchantLow5/airtel-logo.gif" alt="Paytm Payments" title="Paytm Payments" />
				</c:when>
				<c:otherwise>
					<img src="${ptm:stResPath()}images/paytm_logo.png" alt="Paytm Payments" style="height: 30px;" title="Paytm Payments" />
				</c:otherwise>
			</c:choose>

		</div>
	</c:if>

</div>
</c:if>