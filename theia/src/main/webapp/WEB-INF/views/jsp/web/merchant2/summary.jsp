<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%-- <div class="card summary-card fl">
	<c:if test="${!empty merchInfo.merchantImage}">
	<div id="merchant-logo" class="fr">
		<img src="${ptm:stResPath()}images/web/merchant/${merchantImage}" alt="" height="20"/>
	</div>
	</c:if>
	
	<c:set var="paymentLabel" value="Payment to be made"></c:set>
	<c:if test="${isSubscription}">
		<c:set var="paymentLabel" value="Subscription on"></c:set>
	</c:if>
	
	<c:set var="merchantName" value="${merchInfo.merchantName}"/>	
    <span class="merchant-name medium">${paymentLabel}</span>${merchantName}
	<br>
	<br>
	<span class="large">
      	<span class="WebRupee">Rs</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
      	<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
    </span>
      
	
    <%@ include file="cartDetails.jsp" %>    
</div> --%>
<div class="card summary-card fl" style="padding: 6px;width: 30%;">
	<ul class="grid" style="float: left;margin-right: 6px;margin-bottom: 2px;">
	<li class="lt-grey-text">
	<div>
	
	 
		<c:if test="${!empty merchantImage}">
		<div id="merchant-logo">
		    
			<img src="${ptm:stResPath()}images/web/merchant/${merchantImage}" alt="" height="40" style="width: 100%;height: auto;padding: 16px 0;"/>
		</div>
		</c:if>
		
		<c:if test="${empty merchInfo.merchantImage}">
		
		<div id="merchant-logo" style="
		
		    background-color: rgba(128, 128, 128, 0.26); text-align: center;">
		    <p style="
		        margin: 4px;
			    font-size: 31px;
			    text-transform: uppercase;
			    margin-top:5px;
		">${fn:substring(merchInfo.merchantName , 0, 1)}</p>
		    
		</div>
		</c:if>
	</div>	
	</li>
</ul>
<c:set var="merchantName" value="${merchInfo.merchantName}"></c:set>
<ul style="margin-top: 6px;">
	<span style="display: inherit;font-size:12px;color: rgb(0,0,0);line-height: 1.2;"">Payment to be made to ${merchantName }</span>
	<br>
	
	<span class="newWebRupee">&#8377 ${txnInfo.txnAmount}</span]>
</ul>
</div>