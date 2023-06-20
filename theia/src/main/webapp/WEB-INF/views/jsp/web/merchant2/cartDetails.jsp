<div id="cart-details" class="relative">

    <div class="show-btn">
        <a href="#" class="btn-show-payment-details small">Show More</a>
        <a href="#" class="btn-hide-payment-details hide small">Show Less</a>
    </div>

	<ul class="payment-details mb10 hide xsmall">
		<c:if test="${!empty txnInfo.orderDetails}">
		<li>
			Order Details: ${txnInfo.orderDetails }
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
                    , ${txnInfo.address2}<br/>
                </c:if>
                <c:if test="${!empty txnInfo.city}">
                    ${txnInfo.city}
                </c:if>
                <c:if test="${!empty txnInfo.pincode}">
                    , ${txnInfo.pincode}<br/>
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
        <c:if test="${isSubscription}">
            <li>
                <table class="grey-text mt10">
                    <tr>
                        <td class="show mr20">${txnInfo.orderDetails }</td>
                    </tr>
                    <tr>
                        <c:set var="frequencyLabel" value="Frequency"></c:set>
                        <c:if test="${themeInfo.subTheme eq 'airtel'}">
                            <c:set var="frequencyLabel" value="Duration"></c:set>
                        </c:if>
                        <td>${frequencyLabel}
                            : ${txnInfo.subscriptionFrequency}&nbsp;${txnInfo.subscriptionFrequencyUnit}</td>
                    </tr>
                    <tr>
                        <td class="show mr20">Start Date : ${txnInfo.subscriptionStartDate}</td>
                    </tr>
                    <tr>
                        <c:set var="endDateLabel" value="End Date"/>
                        <c:if test="${themeInfo.subTheme eq 'airtel'}">
                            <c:set var="endDateLabel" value="Renewal End Date" />
                        </c:if>
                        <td>${endDateLabel} : ${txnInfo.subscriptionExpiryDate}</td>
                    </tr>
                </table>
                <span class="show mt10">Maximum amount to be debited per subscription : Rs ${txnInfo.subscriptionMaxAmount}</span>
            </li>
        </c:if>
    </ul>

</div>
<div class="clear"></div>