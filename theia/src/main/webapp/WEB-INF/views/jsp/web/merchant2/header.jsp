<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="header">
	<a href="/theia/cancelTransaction" class="fl img-back-holder cancel">
 		<div class="img img-back" alt="Cancel" title="Cancel"></div>
 	</a>
 
	<div class="user-view fr">
 		<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && loginInfo.logoutAllowed}">
				<div class="myaccount fl">
					<div class="logout">
						<div class="fl user-name right-text">
							<%--removing conditions for user name and email id --%>
							<%-- <c:choose>
								<c:when test = "${!empty loginInfo.user.userName}">
									<c:set var = "displayName" value="${loginInfo.user.userName}"/>
								</c:when>
								<c:when test = "${!empty loginInfo.user.emailId}">
									<c:set var = "displayName" value="${loginInfo.user.emailId}"/>
								</c:when>
								<c:when test = "${!empty loginInfo.user.mobileNumber}">
									<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
								</c:when>
							</c:choose> --%>
						<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
						
						<c:if test="${fn:substring(displayName, 0, 2) == '91'}">
							<c:set var="displayName" value="${fn:substring(displayName, 2, 12)}"></c:set>
						</c:if>
						<div class="userDetailContainer">
							<ul class="userIconNew">
								<div class="userImgNew" alt="user" title="user"></div>
							</ul>
							<ul class="userIconNewMobile">
								<div class="userImgNew" alt="user" title="user" onclick="showMessage('logoutMessage')"></div>
							</ul>
							<ul>
								<span class="webDisplayName" onclick="showMessage('logout-btn-container')"><c:out value="${displayName}" escapeXml="true" /> <span style="font-size: 24px;margin-top: 4px;margin-left: 4px;position: absolute;">&#x002C7;</span> </span>
								<%-- <span class="wapDisplayName" onclick="showMessage('logoutMessage')"><c:out value="${displayName}" escapeXml="true" /></span> --%>
								
							</ul>
						</div>
						
					</div>
					<iframe id="logout-iframe" class="hide" src=""></iframe>
					<div class="clear"></div>
				</div>
				
					<a href="#" id="logout-btn">
						<div class="mt6" id="logout-btn-container">
							Sign In with a different account
						</div>
					</a>
					<a href="#" id="logout-btn">
						<div id="logoutMessage" style="position: absolute; right: 10px;color:#4a4a4a;">
							Sign In with a different account
						</div>
					</a>
				
			</div>
		</c:if>
	</div>
	
	<div class="container">
			<div class="imgNew" alt="Paytm Payments" title="Paytm Payments"></div>
	</div>
  	
  	<div class="clear"></div>
</div>