<div id="cart-details" class="fr">
	
	<ul class="payment-details mb10">
		<c:if test="${!empty txnInfo.orderDetails}">
		<li>
			Order Details: ${txnInfo.orderDetails }
		</li>
		</c:if>
		<li>
			Transaction ID: 
			<span >
				<c:out value="${txnInfo.orderId}" escapeXml="true" />
			</span>
			<br>
			<span class="b">
				<c:out value="${txnInfo.promoCodeResponse.resultMsg}" escapeXml="true" />
			</span>
		</li>

		<%-- <li>
			<c:set var="customerDetails">
				<c:if test="${!empty txnInfo.mobileno}">
					${txnInfo.mobileno}
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
	</div> --%>
</div>
<div class="clear"></div>