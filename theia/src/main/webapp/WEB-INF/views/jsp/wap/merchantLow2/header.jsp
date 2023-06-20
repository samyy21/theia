<%-- <div class="logo">
	<a href="cancelTransaction" class="fl back-btn" onclick="return doCancel()">
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
		      	<img src="${ptm:stResPath()}images/wap/merchantLow/back-white.gif" alt="Back" title="Back" />
		      </c:when>
		      <c:otherwise>
		      	<img src="${ptm:stResPath()}images/wap/merchantLow/back.gif" alt="Back" title="Back" />
		      </c:otherwise>
		</c:choose>
		
	</a> 
	
	<div class="userview fr">
		<c:if test ="${!loginInfo.loginFlag && walletInfo.walletEnabled}">
			<a class="login-btn" href='${loginUrl}'>Login</a>
			<a class="register-btn" href='${loginInfo.oAuthInfoHost}/register?response_type=code&scope=paytm&theme=WAP&client_id=${loginInfo.oAuthInfoClientID}&redirect_uri=${loginInfo.oAuthInfoReturnURL}'>Signup</a>
		</c:if>
		
		<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && walletInfo.walletEnabled}">
			<c:choose>
				<c:when test = "${!empty loginInfo.user.userName}">
					<c:set var = "displayName" value="${loginInfo.user.userName}"/>
				</c:when>
				<c:when test = "${!empty loginInfo.user.emailId}">
					<c:set var = "displayName" value="${loginInfo.user.emailId}"/>
				</c:when>
				<c:when test = "${!empty loginInfo.user.mobileNumber}">
					<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
				</c:when>
			</c:choose>
			<c:out value="${displayName}" escapeXml="true" />
		</c:if>
	</div>
	
	<div id="logo-img">
		<c:choose>
		      <c:when test="${themeInfo.subTheme eq 'airtel'}">
		      	<img src="${ptm:stResPath()}images/wap/merchantLow/airtel-logo.gif" alt="Paytm Payments" title="Paytm Payments" />
		      </c:when>
		      <c:otherwise>
		      	<img src="${ptm:stResPath()}images/wap/merchantLow/header-logo.gif" alt="Paytm Payments" title="Paytm Payments" />
		      </c:otherwise>
		</c:choose>
		
	</div>
	
</div> --%>


<div id="header">
	<a href="/theia/cancelTransaction" class="fl img-back-holder cancel" onclick="return doCancel()">
 		<div class="img img-back" alt="Cancel" title="Cancel"></div>
 	</a>
 	
 	<div class="user-view fr">
 		<c:if test ="${loginInfo.loginFlag eq 'Y' && !empty loginInfo.user}">
			<div class="myaccount">
				<div class="logout">
					<div class="user-name"><u class="userNm">
					
						<c:if test = "${!empty logingInfo.user.mobileNumber}">
							<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
						</c:if>
					
					<c:out value="${displayName}" escapeXml="true" />
					</u></div>
					<div class="facebook-image fr">
						<div class="user-icon"></div>
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</c:if>
	</div>
	
	<div class="container">
			<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
	</div>
  	
  	
	<div class="clear"></div>
</div>