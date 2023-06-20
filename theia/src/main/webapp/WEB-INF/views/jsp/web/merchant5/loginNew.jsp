
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="container">
	<c:set var="oauth_infoSplitArr" value="${fn:split(loginInfo.oauthInfo, ',')}" />
	
    <c:set var="phone1" value="" />
    <c:set var="comma" value="\"" />
    <c:forEach items="${oauth_infoSplitArr}" var="phoneSplit" >
       <c:set var="phoneSplit" value="${fn:replace(phoneSplit,comma,'')}" />
         <c:if test="${fn:split(phoneSplit,':')[0] eq 'MSISDN'}">
              <c:set var="phone1" value="${fn:split(phoneSplit,':')[1]}" />
         </c:if>
    </c:forEach>

    <c:set var="phone1" value="${fn:replace(phone1,'}', '')}" />
    
 	<%-- <c:set var="phoneSplit" value="${fn:split(fn:split(loginInfo.oauthInfo, ',')[6], ':')[1]}" />
	<c:set var="phone1" value="${fn:replace(phoneSplit,'\"', '')}" /> --%>
	
	<c:set var="phone2Split" value="${fn:split(sessionScope.TXN_INFO, ',')[4] }" />
	<c:set var="phone2" value="${fn:split(phone2Split,'=')[1]}" />
	
	
	<c:set var="returnUrl" value="" />

    <c:forEach items="${oauth_infoSplitArr}" var="returnUrlSplit1" >
       <c:set var="returnUrlSplit" value="${fn:replace(returnUrlSplit1, comma ,'')}" />
         <c:if test="${fn:split(returnUrlSplit,':')[0] eq 'return_url'}">
              <c:set var="returnUrl" value="${fn:split(returnUrlSplit1, comma)[2]}" />
         </c:if>
    </c:forEach>

    <c:set var="returnUrl" value="${fn:replace(returnUrl,'}', '')}" />
    
	<%-- <c:set var="returnUrlSplit" value="${fn:split(fn:split(loginInfo.oauthInfo, ',')[10], '\"')[2]}" />
	<c:set var="returnUrl" value="${returnUrlSplit}" /> --%>

	<c:choose>
	    <c:when test="${!empty phone1 && empty phone2}">
	        <c:choose>
			    <c:when test="${empty phone1 || empty phone2}">
			        <c:set var="phone" value=""/>
			    </c:when>    
			    <c:otherwise>
			        <c:set var="phone" value="${phone1 }"/>
			    </c:otherwise>
			</c:choose>
	    </c:when>
	    <c:when test="${empty phone1 && !empty phone2}">
	        <c:choose>
			    <c:when test="${empty phone1 || empty phone2}">
			        <c:set var="phone" value=""/>
			    </c:when>    
			    <c:otherwise>
			        <c:set var="phone" value="${phone2 }"/>
			    </c:otherwise>
			</c:choose>
	    </c:when> 
	    <c:when test="${!empty phone1 && !empty phone2}">
	    	<c:choose>
			    <c:when test="${empty phone1 || empty phone2}">
			        <c:set var="phone" value=""/>
			    </c:when>    
			    <c:otherwise>
			        <c:set var="phone" value="${phone1 }"/>
			    </c:otherwise>
			</c:choose>
	    </c:when> 
	    <c:when test="${empty phone1 && empty phone2}">
	    	<c:choose>
			    <c:when test="${empty phone1 || empty phone2}">
			        <c:set var="phone" value=""/>
			    </c:when>    
			    <c:otherwise>
			        <c:set var="phone" value=""/>
			    </c:otherwise>
			</c:choose>
	    </c:when>    
	</c:choose>
		
	<c:set var="oAuthInfo" value="${fn:split(loginInfo.oauthInfo,comma)}" />
	

    <c:forEach items="${oauth_infoSplitArr}" var="hostSplit1" >
       <c:set var="hostSplit" value="${fn:replace(hostSplit1,comma,'')}" />
         <c:if test="${fn:split(hostSplit,':')[0] eq 'oAuthBaseUrl'}">
              <c:set var="host" value="${fn:split(hostSplit1,comma)[2]}" />
         </c:if>
    </c:forEach>

    <c:set var="host" value="${fn:replace(host,'}', '')}" />
		
	<%-- <c:set var="hostSplit" value="${fn:split(fn:split(sessionScope.oauth_info, ',')[2], '\"')[2]}" />
	<c:set var="host" value="${fn:replace(hostSplit,'\"', '')}" />
	<br> --%>
	
	
    <c:set var="orderid" value="${txnInfo.orderId}" />
    
	<%-- <c:set var="orderidSplit" value="${fn:split(fn:split(loginInfo.oauthInfo, ',')[7], ':')[1]}" />
	<c:set var="orderid" value="${fn:replace(orderidSplit,'\"', '')}" /> --%>
		
		
	<c:set var="mid" value="${txnInfo.mid }" />

    
	<%-- <c:set var="midSplit" value="${fn:split(fn:split(loginInfo.oauthInfo, ',')[8], ':')[1]}" />
	<c:set var="mid" value="${fn:replace(midSplit,'\"', '')}" /> --%>
	
	<c:set var="txnTransientId" value="${txnInfo.txnId}" />

	<c:set var="detectedChannel" value="WEB" />
	
	<c:set var="jsessionid" value="<%=request.getSession().getId() %>" />
	
	<c:set var="clientId" value="${loginInfo.oAuthInfoClientID }" />
	
	<c:set var="loginType" value="MANUAL" />	

		<c:set var="eid" value="" />

		<c:forEach items="${oauth_infoSplitArr}" var="eidSplit">
			<c:set var="eidSplit" value="${fn:replace(eidSplit,'\"','')}" />
			<c:if test="${fn:split(eidSplit,':')[0] eq 'eid'}">
				<c:set var="eid" value="${fn:split(eidSplit,':')[1]}" />
			</c:if>
		</c:forEach>

		<c:set var="eid" value="${fn:replace(eid,'}', '')}" />

		<c:set var="jvm" value="${requestScope.jvmRoute}" />

		<c:set var="loginData"
			value="${orderid}:${mid}:${txnTransientId}:${detectedChannel}:${jsessionid}:${loginType}:${eid}" />

		<c:if test="${!empty phone}">
			<div class="message-area"
				style="display: inline-block; margin: 20px 15px">
				<%-- <p id="myContent" style="display:none;float: left;font-size: 14px;line-height: 1.4;color: rgb(102, 102, 102);">Enter Mobile Number to receive One Time Password</p>
		  	<p id="enter-otp" style="width: 65%; float: left; font-size: 14px; line-height: 1.4; color: rgb(74, 74, 74);">Enter One Time Password sent to ${phone }</p>
			<p id="open-change-mobile-frame" style="width: 24%;float: right;font-size: 12px;color: #00B9F5;cursor: pointer;" onclick="openFrame('loginData=${loginData }&phone=&redirectUri=${returnUrl }&clientId=${clientId}')">Change Mobile</p> --%>
				<p id="myContent"
					style="display: none; float: left; font-size: 17px; line-height: 1.4; color: rgb(102, 102, 102);">Enter
					Mobile Number to receive One Time Password</p>
				<p id="enter-otp"
					style="float: left; font-size: 17px; line-height: 1.4; color: rgb(74, 74, 74);">
					Enter One Time Password sent to <a id="open-change-mobile-frame"
						style="font-size: 17px; color: #00B9F5; cursor: pointer; text-decoration: underline;"
						onclick="openFrame('loginData=${loginData }&phone=&redirectUri=${returnUrl }&clientId=${clientId}')">${phone }</a>
				</p>

			</div>
			<div class="iframe-container" id="iframe-container-1"
				style="position: relative; border-radius: 10px; overflow: hidden;">
				<iframe id="frame-1"
					src="${host}/oauth2/login/otp?loginData=${loginData }&phone=${phone }&redirectUri=${returnUrl }&clientId=${clientId}"
					height="210px" width="100%"></iframe>
			</div>
		</c:if>

		<c:if test="${empty phone}">
			<div class="message-area"
				style="display: inline-block; margin: 20px 15px;">
				<%-- <p id="myContent" style="float: left;font-size: 14px;line-height: 1.4;color: rgb(102, 102, 102);">Enter Mobile Number to receive One Time Password</p>
			<p id="enter-otp" style="display:none; width: 65%; float: left; font-size: 14px; line-height: 1.4; color: rgb(74, 74, 74);"></p>
			<p id="open-change-mobile-frame" style="display:none;width: 24%;float: right;font-size: 12px;color: #00B9F5;cursor: pointer;" onclick="openFrame('loginData=${loginData }&phone=&redirectUri=${returnUrl }&clientId=${clientId}')">Change mobile</p> --%>

				<p id="myContent"
					style="float: left; font-size: 17px; line-height: 1.4; color: rgb(102, 102, 102);">Enter
					Mobile Number to receive One Time Password</p>
				<%-- <p id="enter-otp" style="display:none; width: 65%; float: left; font-size: 17px; line-height: 1.4; color: rgb(74, 74, 74);"></p>
			<p id="open-change-mobile-frame" style="display:none;width: 24%;float: right;font-size: 12px;color: #00B9F5;cursor: pointer;" onclick="openFrame('loginData=${loginData }&phone=&redirectUri=${returnUrl }&clientId=${clientId}')">Change mobile</p> --%>
				<p id="enter-otp"
					style="display: none; float: left; font-size: 17px; line-height: 1.4; color: rgb(74, 74, 74);">
					Enter One Time Password sent to <a id="open-change-mobile-frame"
						style="font-size: 17px; color: #00B9F5; cursor: pointer; text-decoration: underline;"
						onclick="openFrame('loginData=${loginData }&phone=&redirectUri=${returnUrl }&clientId=${clientId}')">abcs</a>
				</p>
			</div>

			<div class="iframe-container" style="position: relative;border-radius: 10px;overflow: hidden;">
				<iframe id="frame-1"
					src="${host}/oauth2/login/otp?phone=${phone }&loginData=${loginData }&redirectUri=${returnUrl }&clientId=${clientId}"
					height="210px" width="100%" name="add-mobile" id="add-mobile"
					onload="loaded()"></iframe>
			</div>
		</c:if>
		<p
			style="font-size: 10px; color: rgb(155, 155, 155); margin-left: 15px;">
			By proceeding you accept the terms and conditions mentioned at <a
				href="https://paytm.com/terms" target="_blank"
				style="color: #01bdf2; font-size: 10px;">www.paytm.com/terms</a>
		</p>
</div>



<div id="iframe-container-2" style="display: none; top: 50%; width: 90%; z-index: 2; margin-top: -200px; margin-left: 5%; position: relative; border-radius: 4px; overflow: hidden;">
		<span class="iframe-close" style="
	    position: absolute;
	    right: 5px;
	    top: 5px;
	    cursor: pointer;" onclick="closeFrame()">X</span>
</div>
	
<div id="overlay" style="display: none; background-color: #000;height: 100%;width: 110%;opacity: 0.4;position: fixed;top: 0;z-index: 1;margin-left: -10%;"></div>

