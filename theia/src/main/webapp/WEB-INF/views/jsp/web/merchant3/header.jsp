<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="header">
 		<div class="container">
    	<div class="fl">
	    	<c:if test="${themeInfo.subTheme ne 'torrent'}">
	    		<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div> 
	    	</c:if>
	   		<c:if test="${themeInfo.subTheme eq 'torrent'}">
	   			<div   title="Paytm Payments" style="margin-top: 13px;"><img src="${ptm:stResPath()}images/web/merchant3/torrentPower.png" alt="" height="50"/></div>
	   		</c:if>
    	</div>
	    <c:if test="${themeInfo.subTheme eq 'torrent'}">
	    	<div class="fr">
	    		<img src="${ptm:stResPath()}images/web/merchant3/torrentPowerHeader.png" style="width:680px" alt="" />
	    	</div>
	    </c:if>
    	
    		<div class="user-view fr">
    	
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
			
		</div>
		
		<c:if test="${!empty merchantImage && themeInfo.subTheme ne 'torrent' && !loginInfo.loginFlag}">
			<div id="merchant-logo" class="fr">
				<img src="${ptm:stResPath()}images/web/merchant3/${merchantImage}" alt="" height="40"/>
			</div>
		</c:if>
		
  	</div>
  	
  	<div class="clear"></div>
</div>