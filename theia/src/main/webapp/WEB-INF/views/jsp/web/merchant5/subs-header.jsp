<div id="header">
 		<div class="container">
    	<div class="fl">
    		<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
    	</div>
    	
    	<div class="user-view fr">
    		<c:if test ="${!loginInfo.loginFlag}">
				<div class="myaccount fl">
					<div class="login relative blue-text">
						<a id="login-btn" href="#">Login</a>
						<a id="register-btn" class="ml10" href="#">Sign up</a>

						<c:if test="${ empty showLoginNotification}">
							<c:set var="showLoginNotification" value="${walletInfo.walletBalance && empty loginInfo.user}" scope="session"></c:set>
						</c:if>
						<c:if test="${showLoginNotification eq true}">
							<c:set var="showLoginNotification" value="false" scope="session"></c:set>
							<div class="login-popover popover fade bottom in">
								<div class="arrow"></div>
								<div class="popover-content">Login with Paytm to use your Paytm and saved cards.</div>
								<div class="popover-close">
									<a href="#">x</a>
								</div>
							</div>
						</c:if>
					</div>
				</div>
			</c:if>
			
			<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user}">
				<div class="myaccount fl">
					<div class="logout">
						<div class="fl user-name">
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
						<c:out value="${displayName}" escapeXml="true" /></div>
						<div class="facebook-image fr">
							<div class="user-icon"></div>
						</div>
						<div class="clear"></div>
					</div>
				</div>
			</c:if>
			
		</div>
  	</div>
  	
  	<div class="clear"></div>
</div>