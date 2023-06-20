<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:if test ="${ !loginInfo.loginFlag }">
<ul class="grid" style="float: left;margin-right: 30px;">
	<li class="lt-grey-text">
	<div>
	
	
		<c:if test="${!empty merchantImage}">
		<div id="merchant-logo" style="
		    display: inline-block;
		    border-radius: 50%;
		    width: 55px;
		    height: 45px;
		    overflow: hidden;
		    margin: 0;
		    border: 1px solid gray;">
		    
		    <!-- <img src="http://dtkmen.com/wp-content/uploads/2015/05/zara-logo1.jpg" alt=""/> -->
			<img src="${ptm:stResPath()}images/web/merchant/${merchantImage}" alt="" height="40" style="width: 100%;height: auto;padding: 12px 0;"/>
		</div>
		</c:if>
		
		<c:if test="${empty merchantImage}">
		<div id="merchant-logo" style="
		    display: inline-block;
		    border-radius: 50%;
		    width: 55px;
		    height: 45px;
		    overflow: hidden;
		    margin: 0;
		    background-color: rgba(128, 128, 128, 0.26);
		    border: 1px solid gray;">
		    <p style="
		    position: absolute;
		    top: 88px;
		    /* left: 10%; */
		    margin-left: 16px;
		    font-size: 35px;
		    text-transform: uppercase;
		">${fn:substring(merchantName , 0, 1)}</p>
		    
		</div>
		</c:if>
<!-- 		
		<ul>
			<li>Payment to be made to</li>
			<li>Rs 1</li>
		</ul> -->
		<%-- <span class="small">Payment to be made to</span>
			
				<span class="large">
					<span class="WebRupee">Rs</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
				</span> --%>
			
	</div>	
		
		<%-- <%@ include file="cartDetails.jsp" %> --%>
	</li>
	<%-- <li class="fr pt20 b">
		<span class="large">
			<span class="WebRupee">Rs</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
		</span>
    			<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
	</li> --%>
</ul>

<c:set var="merchantName" value="${merchInfo.merchantName}"></c:set>
<ul>
	<span style="display:block;font-size:12px;margin-bottom: 7px;color: rgb(0,0,0);">Total Payment to be made to ${merchantName }</span>
	<span style="font-size:32px;color: rgb(0,0,0);">&#8377 ${txnInfo.txnAmount}</span]>
</ul>
</c:if>
<c:if test ="${loginInfo.loginFlag}">
<ul style="margin-top: 7px;width: 58%;">
	<%-- <li><p style="font-size:12px;margin-bottom: 7px;color: #4a4a4a;font-size: 16px;width: 177px;">Payment to be made to ${merchantName }</p></li>
	<li><p style="font-size: 19px;color: #4a4a4a;position: absolute;top: 99px;right: 5%;">Rs ${txnInfo.txnAmount}</p></li> --%>
	
	<li style="font-size:12px;margin-bottom: 7px;color: #4a4a4a;font-size: 16px;display: inline;">Payment to be made to ${merchantName }</li>
	<li style="font-size: 19px;color: #4a4a4a;display: inline;position: absolute;right: 5%;">&#8377 ${txnInfo.txnAmount}</li>
</ul>
</c:if>