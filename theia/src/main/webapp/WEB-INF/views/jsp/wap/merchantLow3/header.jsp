<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<div class="logo hide" >
	<a href="/theia/cancelTransaction" class="fl back-btn" onclick="return doCancel()">
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
<%-- 		      	<img src="${ptm:stResPath()}images/wap/merchantLow/back-white.gif" alt="Back" title="Back" /> --%>
		      </c:when>
		      <c:otherwise>
		      	<%--TODO: change to ptm tag lib --%>
<!-- 		      	<img src="/theia/resources/images/wap/merchantLow/ic-back.png" alt="Back" title="Back" /> -->
		      </c:otherwise>
		</c:choose>
		
	</a> 
	<%--TODO: ask product if they need user mobile number --%>
	<div class="userview fr">
		<c:if test ="${!loginInfo.loginFlag && walletInfo.walletEnabled}">



			<c:if test="${loginInfo.loginWithOtp}">
				<a class="login-btn" href='${loginUrl}'>Login</a>
				<a class="register-btn" href='${loginUrl}'>Signup</a>
			</c:if>
			<c:if test="${!loginInfo.loginWithOtp}">
				<a class="login-btn" href='${loginUrl}'>Login</a>
				<a class="register-btn" href='${loginInfo.oAuthInfoHost}/register?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}'>Signup</a>
			</c:if>

		</c:if>
		
		<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && walletInfo.walletEnabled}">
			
				<c:if test = "${!empty loginInfo.user.mobileNumber}">
					<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
				</c:if>
			
<%-- 			<c:out value="${displayName}" escapeXml="true" /> --%>
		</c:if>
	</div>
	
<!-- 	<div id="logo-img"> -->
<%--  		<c:choose>  --%>
<%-- 		      <c:when test="${themeInfo.subTheme eq 'airtel'}"> --%>
<!-- 		      	<img src="${ptm:stResPath()}images/wap/merchantLow/airtel-logo.gif" alt="Paytm Payments" title="Paytm Payments" /> -->
<%-- 		      </c:when>  --%>
<%-- 		      <c:otherwise> --%>
<%-- 		      	TODO: change the image path to ${ptm:stResPath()} --%>
<!-- 		      	<img src="/theia/resources/images/wap/merchantLow3/ic-paytm-log-0.png" alt="Paytm Payments" title="Paytm Payments" /> -->
<%--  		      </c:otherwise>  --%>
<%-- 		</c:choose>  --%>
		
<!-- 	</div> -->
<!-- 	<div class="Complete-Payment"> -->
<!-- 			Complete Payment -->
<!-- 	</div> -->
	
</div>