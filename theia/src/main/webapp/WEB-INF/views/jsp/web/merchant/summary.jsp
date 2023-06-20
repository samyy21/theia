<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<div class="card summary-card">
	<c:if test="${!empty merchInfo.merchantImage && themeInfo.subTheme ne 'ndtv'}">
	<div id="merchant-logo" class="fl mr20">
		<c:choose>
			<c:when test="${merchInfo.useNewImagePath eq true}">
				<img src="${merchantImage}" alt=""/>
			</c:when>
			<c:otherwise>
				<img src="${ptm:stPath()}merchantLogo/${merchantImage}" alt="" />
			</c:otherwise>
		</c:choose>
	</div>
	</c:if>
	
	<ul class="grid mt10 mb5">
	  <%-- <c:set var="merchantName" value="${fn:toUpperCase(merchInfo.merchantName)}"></c:set> --%>
	  <c:set var="merchantName" value="${merchInfo.merchantName}"></c:set>
	  
		<c:set var="paymentLabel" value="Total payment to be made to ${merchInfo.merchantName}"></c:set>
		<c:if test="${themeInfo.subTheme eq 'airtel'}">
			<c:set var="paymentLabel" value="Total Payment"></c:set>
		</c:if>
		
		<li class="merchant-name medium b">${paymentLabel}</li>
      <li class="fr large b">
      	<span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
      	<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
      </li>
      <li class="clear"></li>
    </ul>
    <%@ include file="cartDetails.jsp" %>    
</div>