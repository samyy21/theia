<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<div id="header">
 		<div class="container">
    	<div class="fl">
    	<c:choose>
			<c:when test = "${themeInfo.subTheme eq 'appsperts'}">
				<c:choose>
					<c:when test="${merchInfo.useNewImagePath eq true}">
						<img src="${merchantImage}" style="margin:16px 0px;" alt="" height="40" />
					</c:when>
					<c:otherwise>
						<img src="${ptm:stPath()}merchantLogo/${merchantImage}" style="margin:16px 0px;" alt="" height="40" />
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
    			<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
    		</c:otherwise>
    	</c:choose>
    	</div>
    	
    	<div class="user-view fr">
    	
    	<c:if test="${themeInfo.subTheme == 'goair' }">
    		<div class="fl">
				<c:choose>
					<c:when test="${merchInfo.useNewImagePath eq true}">
						<img src="${merchantImage}" style="margin:16px 0px;" alt="" height="40" />
					</c:when>
					<c:otherwise>
						<img src="${ptm:stResPath()}images/web/merchant/${merchantImage}" style="margin:16px 0px;" alt="" height="40" />
					</c:otherwise>
				</c:choose>
			</div>
    	</c:if>
    	
    	<c:if test="${themeInfo.subTheme != 'goair' }">
			<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && loginInfo.logoutAllowed}">
				<div class="myaccount fl">
					<div class="logout">
						<div class="fl user-name right-text">
							
								<c:if test = "${!empty loginInfo.user.mobileNumber}">
									<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
								</c:if>
							
							<span><c:out value="${displayName}" escapeXml="true" /></span>
							<div class="blue-text mt6" id="logout-btn-container">
								<a href="#" id="logout-btn" class="underline">Login</a> as a different user
							</div>
						</div>
						<iframe id="logout-iframe" class="hide" src=""></iframe>
						<div class="clear"></div>
					</div>
				</div>
			</c:if>
		</c:if>
			
		</div>
		
		<c:if test="${themeInfo.subTheme eq 'mapmystudy' || themeInfo.subTheme eq 'indiafin' || themeInfo.subTheme eq 'indiamart' || themeInfo.subTheme eq 'ndtv'}">
			<div id="merchant-logo" class="fr">
				<c:choose>
					<c:when test="${merchInfo.useNewImagePath eq true}">
						<img src="${merchantImage}" alt="" height="40" />
					</c:when>
					<c:otherwise>
						<img src="${ptm:stPath()}merchantLogo/${merchantImage}" alt="" height="40"/>
					</c:otherwise>
				</c:choose>
			</div>
		</c:if>
		
  	</div>
  	
  	<div class="clear"></div>
</div>