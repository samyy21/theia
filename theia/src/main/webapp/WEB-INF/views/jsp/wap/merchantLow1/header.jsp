<div id="header">
	<a href="/theia/cancelTransaction" class="fl img-back-holder cancel" onclick="return doCancel()">
 		<div class="img img-back" alt="Cancel" title="Cancel"></div>
 	</a>
 	
 	<div class="user-view fr">
 		<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user}">
			<div class="myaccount">
				<div class="logout">
					<div class="user-name"><u class="userNm">
					
						<c:if test = "${!empty loginInfo.user.mobileNumber}">
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