<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<script>function merchantLogoCb(e){document&&(e?document.getElementsByClassName("merchantLogoImage")[0].style.display="block":document.getElementsByClassName("merchangLogoName")[0].style.display="inline-block")}</script>
<div id="header">
 		<div class="container">
    	<div class="fl">
    	<c:choose>
			<c:when test = "${!txnInfo.onus}">
				<c:choose>
					<c:when test="${merchInfo.useNewImagePath eq true}">
						<img src="${merchantImage}" style="margin:16px 20px;display:none" alt="${merchInfo.merchantName}" height="50" class="merchantLogoImage" onerror="merchantLogoCb(false)" onload="merchantLogoCb(true)" />
						<h1 style="display:none;font-size: 20px;padding: 11px;padding-left: 21px;" class="merchangLogoName">${merchInfo.merchantName}</h1>
					</c:when>
					<c:otherwise>
						<img src="${ptm:stPath()}merchantLogo/${merchantImage}" style="margin:16px 20px;display:none" alt="${merchInfo.merchantName}" height="50" class="merchantLogoImage" onerror="merchantLogoCb(false)" onload="merchantLogoCb(true)" />
						<h1 style="display:none;font-size: 20px;padding: 11px;padding-left: 21px;" class="merchangLogoName">${merchInfo.merchantName}</h1>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
    			<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
    		</c:otherwise>
    	</c:choose>
    	</div>
		<c:if test="${txnInfo.onus}">
			<div class="txtHeading fl">
				<h3>Select Payment Method</h3>
			</div>
		</c:if>

    	<div class="user-view fr">

			<div  class="myaccount">
			<c:if test ="${loginInfo.loginFlag && !empty loginInfo.user && loginInfo.logoutAllowed}">
				<div class="">
					<div class="logout">
						<div class="fl user-name right-text">
							
								<c:if test = "${!empty loginInfo.user.mobileNumber}">
									<c:set var = "displayName" value="${loginInfo.user.mobileNumber}"/>
								</c:if>
							
							<span class="offus-mobile-bg"><c:out value="${displayName}" escapeXml="true" /></span>
							<div class="blue-text mt6" id="logout-btn-container">
								<a href="#" id="logout-btn" class="underline">Login</a> as a different user
							</div>
						</div>
						<iframe id="logout-iframe" class="hide" src=""></iframe>
						<div class="clear"></div>
					</div>
				</div>
			</c:if>

			<c:if test="${!txnInfo.onus && !empty txnConfig.paymentCharges}">
				<a href="javascript:void(0)" class="cancel-txn blue-text" style="
				margin-top: 10px;
				display: block;
				text-align:right;
				text-decoration: underline;
				">Cancel Transaction</a>
							</c:if>
				</div>
		</div>
		
		<%--<c:if test="${themeInfo.subTheme eq 'mapmystudy' || themeInfo.subTheme eq 'indiafin' || themeInfo.subTheme eq 'indiamart'}">--%>
			<%--<div id="merchant-logo" class="fr">--%>
				<%--<c:choose>--%>
					<%--<c:when test="${merchInfo.useNewImagePath eq true}">--%>
						<%--<img src="${merchantImage}" alt="" height="40" />--%>
					<%--</c:when>--%>
					<%--<c:otherwise>--%>
						<%--<img src="${ptm:stPath()}merchantLogo/${merchantImage}" alt="" height="40"/>--%>
					<%--</c:otherwise>--%>
				<%--</c:choose>--%>
			<%--</div>--%>
		<%--</c:if>--%>
  	</div>
  	
  	<div class="clear"></div>
</div>