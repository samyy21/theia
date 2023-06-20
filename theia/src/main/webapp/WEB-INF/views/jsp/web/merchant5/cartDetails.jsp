<div id="cart-details" class="fl">
	
	<ul class="payment-details mb10 hide">
		<c:if test="${!empty txnInfo.orderDetails}">
		<li>
			Order Details: ${txnInfo.orderDetails }
		</li>
		</c:if>
		<li>
			Transaction ID: 
			<span class="blue-text">
				<c:out value="${txnInfo.orderId}" escapeXml="true" />
			</span>
		</li>
		<li>
			<c:set var="customerDetails">
				<c:if test="${!empty txnInfo.mobileno}">
					${txnInfo.mobileno}
				</c:if>
				<c:if test="${!empty txnInfo.emailId}">
					 ${txnInfo.emailId}<br />
				</c:if>
			</c:set>
			<c:if test="${!empty customerDetails}">
				<span>Customer details:</span>
				<span class="blue-text">${customerDetails}</span>
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
	
	<div class="blue-text">
		<a href="#" class="btn-show-payment-details small b">Show More</a>
		<a href="#" class="btn-hide-payment-details hide small b">Show Less</a>
	</div>
</div>
<div class="clear"></div>