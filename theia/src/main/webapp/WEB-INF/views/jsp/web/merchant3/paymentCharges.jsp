	<!-- New IRCTC PART -->

	<c:set var="loginFlag" value='${loginInfo.loginFlag}' />
			<div class="card  mb20" id="irctc-charges">
	    		<div>
	    			<div class="mb10">
	    			<span id="TransID">Transaction ID</span> <strong class="blue-text">${txnInfo.orderId}</strong>
					</div>
					<div  class="mb10 conv-charges">
	    			<span class="fl">Payment to be made to ${merchInfo.merchantName}</span> <span  class="fr"><span class="WebRupee">Rs.</span> 
	    			<c:choose>
							<c:when test="${! empty txnConfig.paymentCharges.PPI}">
								<c:if test="${loginFlag}">
								<span id="addMoney-baseAmt" class="ir-addMoney">${txnConfig.paymentCharges.PPI.baseTransactionAmount}</span>
								</c:if>
								<span id="baseAmt" class="ir-Money" <c:if test="${loginFlag}"> style="display:none;" </c:if> ></span>
							</c:when>
							<c:otherwise>
								<span id="baseAmt"></span>
							</c:otherwise>
						</c:choose> 
	    				
	    				
	    				
	    			</span>
	    			<div class="clear"></div>
					</div>
					<div  class="mb10 conv-charges">
		    			<span class="fl">Convenience Charges (
		    			<c:choose>
							<c:when test="${! empty txnConfig.paymentCharges.PPI}">
							<c:if test="${loginFlag}"><span id="addMoney-chargeTxt" class="ir-addMoney">${txnConfig.paymentCharges.PPI.text}</span></c:if>
							<span id="chargeTxt" <c:if test="${loginFlag}"> style="display:none;" </c:if> class="ir-Money"></span>
							</c:when>
							<c:otherwise>
								<span id="chargeTxt"></span>
							</c:otherwise>
						</c:choose>
		    			
		    			
		    			
		    			) 
		    			<!-- ToolTip  All Convenience Charges -->
		    			<strong class="img exclamation relative">
		    			<div id="convenienceChrg">
		    			<div class="mb5 b small">Convenience Charges Approximately</div>
		    			<ul class="xsmall">
		    			<c:forEach var="bankChargeInfo" items="${txnConfig.paymentChargesInfo}" begin="0">
		    				<li >${bankChargeInfo.value}</li>
		    				
		    			</c:forEach>
		    			</ul>
		    			</div>
		    			</strong>
		    			
		    			</span> <span  class="fr"> <span class="WebRupee">Rs.</span> 
		    			<c:choose>
							<c:when test="${! empty txnConfig.paymentCharges.PPI}">
							<c:if test="${loginFlag}"><span id="addMoney-chargeFeeAmt" class="ir-addMoney">${txnConfig.paymentCharges.PPI.totalConvenienceCharges}</span></c:if>
							<span id="chargeFeeAmt" <c:if test="${loginFlag}"> style="display:none;" </c:if> class="ir-Money"></span>
							</c:when>
							<c:otherwise>
								<span id="chargeFeeAmt"></span>
							</c:otherwise>
						</c:choose>
		    			
		    			
		    			</span>
		    			<div class="clear"></div>
		    			<div class="hr mt20"></div>
					</div>
					<div  class="mb10">
		    			<strong class="fl b">Total Payment to be made</strong> <strong  class="fr b"><span class="WebRupee">Rs.</span>
		    			<c:choose>
							<c:when test="${! empty txnConfig.paymentCharges.PPI}">
							<c:if test="${loginFlag}"><span id="addMoney-totaltxnAmt" class="ir-addMoney">${txnConfig.paymentCharges.PPI.totalTransactionAmount}</span></c:if>
							<span id="totaltxnAmt" <c:if test="${loginFlag}"> style="display:none;" </c:if> class="ir-Money"></span>
							</c:when>
							<c:otherwise>
								<span id="totaltxnAmt"></span>
							</c:otherwise>
						</c:choose>
		    			 
		    				
		    			
		    			</strong>
		    			<div class="clear"></div>
					</div>
	    		</div>
				<div class="clear"></div>
			</div>
			<!-- END New IRCTC PART -->