<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="header">
 		<div class="container">
    	<div class="fl">
    	<c:choose>
			<c:when test = "${themeInfo.subTheme eq 'DU_Univ'}">
				<img src="${ptm:stResPath()}images/web/merchant5/${merchInfo.merchantImage}" style="margin:7px 0px;" class="fl" alt="" height="70" />
				<span class="fl merchantTitleHeading">UNIVERSITY OF DELHI</span>
			</c:when>

			<c:when test = "${themeInfo.subTheme eq 'appsperts'}">
				<img src="${ptm:stResPath()}images/web/merchant5/${merchInfo.merchantImage}" style="margin:16px 0px;" alt="" height="40" />
			</c:when>

			<c:when test="${themeInfo.subTheme eq 'torrent'}">
				<div   title="Paytm Payments" style="margin-top: 13px;"><img src="${ptm:stResPath()}images/web/merchant5/torrentPower.png" alt="" height="50"/></div>
			</c:when>

			<c:otherwise>
    			<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
    		</c:otherwise>

    	</c:choose>
    	</div>

			<c:if test="${themeInfo.subTheme eq 'torrent'}">
				<div class="fr">
					<img src="${ptm:stResPath()}images/web/merchant5/torrentPowerHeader.png" style="width:680px" alt="" />
				</div>
			</c:if>


    	<div class="user-view fr">

			<c:if test="${themeInfo.subTheme == 'goair' }">
				<div class="fl">
					<img src="${ptm:stResPath()}images/web/merchant5/${merchInfo.merchantImage}" style="margin:16px 0px;" alt="" height="40" />
				</div>
			</c:if>

			<c:if test="${themeInfo.subTheme != 'goair'  && themeInfo.subTheme ne 'DU_Univ'}">
				<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && loginInfo.logoutAllowed}">
					<div class="myaccount fl relative">
						<div class="logout">
							<div class="fl user-name right-text">

								<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>

								<c:if test="${fn:substring(displayName, 0, 2) == '91'}">
									<c:set var="displayName" value="${fn:substring(displayName, 2, 12)}"></c:set>
								</c:if>
								<!--
								<div class="userDetailContainer">
									<ul class="userIconNew">
										<div class="userImgNew" alt="user" title="user"></div>
									</ul>
									<ul class="userIconNewMobile">
										<div class="userImgNew" alt="user" title="user" onclick="showMessage('logoutMessage')"></div>
									</ul>
									<ul>
										<span class="webDisplayName" onclick="showMessage('logoutMessage')"><%-- <c:out value="${displayName}" escapeXml="true" /> --%> <span style="font-size: 24px;margin-top: 4px;margin-left: 4px;position: absolute;">&#x002C7;</span> </span>

									</ul>
								</div> -->
								<!-- to display logout option -->
								<span><c:out value="${displayName}" escapeXml="true" /></span>
								<div class="blue-text mt6">
									<a href="#" id="logout-btn" class="underline">Login</a> as a different user
								</div>

							</div>
							<iframe id="logout-iframe" class="hide" src=""></iframe>
							<div class="clear"></div>
						<!--
							<a href="#" id="logout-btn">
								<div id="logoutMessage" style="position: absolute; right: 10px;color:#4a4a4a;">
									Sign In with a different account
								</div>
							</a> -->
						</div>


					</div>
				</c:if>
			</c:if>
			
		</div>

		<c:if test="${themeInfo.subTheme eq 'mapmystudy' || themeInfo.subTheme eq 'indiafin' || themeInfo.subTheme eq 'indiamart' || themeInfo.subTheme eq 'ndtv'}">
			<div id="merchant-logo" class="fr">
				<img src="${ptm:stResPath()}images/web/merchant5/${merchInfo.merchantImage}" alt="" height="40"/>
			</div>
		</c:if>

		<c:if test = "${themeInfo.subTheme eq 'DU_Univ'}">
			<div id="merchant-logo" class="fr" style="margin-top: 14px;">
				<img src="${ptm:stResPath()}images/web/merchant5/IDBI_Logo_1_.png" alt="" height="40"/>
			</div>
		</c:if>
		
  	</div>
  	
  	<div class="clear"></div>
</div>