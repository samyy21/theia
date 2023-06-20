<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<ul class="amount-grid grid bgwhite">
	<li class="lt-grey-text">
		<c:set var="paymentText" value="Payment to be made to ${merchantName}"/>
		<c:choose>
	   		<c:when test="${themeInfo.subTheme eq 'saavn'}">
	   			<c:set var="paymentText" value="Payment total"/>
	   		</c:when>
	   		<c:otherwise>
	   			
		    </c:otherwise>
	   	</c:choose>
	   	<c:if test="${isSubscription}">
			<c:set var="paymentText" value="Subscription on"/>
		</c:if>
		<span class="small">${paymentText}</span>
		<c:if test="${!empty merchantImage}">
		<div id="merchant-logo">
			<img src="${ptm:stResPath()}images/web/merchant/${merchantImage}" alt="" height="20"/>
		</div>
		</c:if>
		
		<div id="cart-details" class="relative">
			<c:if test="${isSubscription}">
			<a href="#" onclick="return showSubsDetails(this)" class="small blue">Show more</a></c:if>
			<div class="small mt10 mb5 hide" id="subs-details">
				<span>Frequency : ${txnInfo.subscriptionFrequency}&nbsp;${txnInfo.subscriptionFrequencyUnit}</span><br>
				<span>Start Date : ${txnInfo.subscriptionStartDate}</span><br>
				<span>End Date : ${txnInfo.subscriptionExpiryDate}</span><br>
				<span>Max. amount debited per subscription : Rs ${txnInfo.subscriptionMaxAmount}</span>
			</div>
			
			<!-- <div class="show-btn">
					<a href="#" class="btn-show-payment-details small">Show More</a>
					<a href="#" class="btn-hide-payment-details hide small">Show Less</a>
			</div> -->
			
			<ul class="payment-details mb10 hide xsmall">
				<c:if test="${!empty sessionScope.infoTo.orderDetails}">
				<li>
					Order Details: ${sessionScope.infoTo.orderDetails }
				</li>
				</c:if>
				<li>
					Transaction ID: 
					<span class="">
						<c:out value="${txnInfo.orderId}" escapeXml="true" />
					</span>
				</li>
				<li>
					<c:set var="customerDetails">
						<c:if test="${!empty txnInfo.custID}">
							${txnInfo.custID}<br/>
						</c:if>
						
						<c:if test="${!empty txnInfo.address1}">
							${txnInfo.address1}
						</c:if>
						<c:if test="${!empty txnInfo.address2}">
							, ${txnInfo.address2}<br />
						</c:if>
						<c:if test="${!empty txnInfo.city}">
							${txnInfo.city}
						</c:if>
						<c:if test="${!empty txnInfo.pincode}">
							, ${txnInfo.pincode}<br />
						</c:if>
						<c:if test="${!empty txnInfo.mobileno}">
							${txnInfo.mobileno}
						</c:if>
						<c:if test="${!empty txnInfo.emailId}">
							 ${txnInfo.emailId}<br />
						</c:if>
					</c:set>
					<c:if test="${!empty customerDetails}">
						<span>Customer details:</span>
						<span class="">${customerDetails}</span>
					</c:if>
				</li>
			</ul>
			
			<c:if test="${!empty sessionScope.shoppingCart}">
				<dl class="outer-tab">
					<table id="paytable">
						<tbody>
							<tr>
								<th></th>
								<th class="odd">Product</th>
								<th class="odd">Amount (<span class="WebRupee">Rs</span>)</th>
							</tr>
								<c:forEach items="${sessionScope.shoppingCart}" var="item">
									<tr>
										<td> </td>
										<td>
											<c:out value="${item.key}" escapeXml="true" />
										</td>
										<td>
											<c:out value="${item.value}" escapeXml="true" />
										</td>
									</tr>
							</c:forEach>
						</tbody>
					</table>
				</dl>
			</c:if>
			
		</div>
		<div class="clear"></div>
	</li>
	<li class="fr  b">
		<span class="medium">
			<span class="WebRupee">Rs</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
		</span>
    			<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
	</li>
</ul>