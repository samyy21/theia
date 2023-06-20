<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<div class="logo">
	<a href="/theia/cancelTransaction" class="fl back-btn" onclick="return doCancel()">
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

		<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && walletInfo.walletEnabled}">
			
				<c:if test = "${!empty loginInfo.user.mobileNumber}">
					<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
				</c:if>
			
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
	
</div>