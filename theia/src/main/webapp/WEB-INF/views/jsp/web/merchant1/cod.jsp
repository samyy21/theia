<div class = 'card content ${12 eq paymentType ? "active" : ""}' id = "cod-card">
	<div class="fl">
		<form autocomplete="off" class="cod-form validated" name="cod-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="COD" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
			<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			
			<div id="cod-msg">
				<p class="grey-text mt20">
					<span>
					You'll receive an automated call from us to confirm this order. And at the time of delivery, Rs ${txnInfo.txnAmount} will have to be given to the courier boy
					</span>
				</p>
				
				<div class="mt20">
					<div class="btn-submit ${submitBtnClass} fl">
		           		<input name="" type="submit" class="gry-btn btn-normal" value="Complete Order" id="codSubmit">	          
		           	</div>
		           	<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
		           	<div class="clear"></div>
		        </div>
			</div>
			
			<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
				<div id="cod-hybrid-msg" class="hide">
					
					
					<c:if test="${txnConfig.codHybridAllowed}">
						<span >
						At the time of delivery, Rs <span id="cod-amount"></span> will have to be given to the courier boy
						</span>
						
						<div class="mt20">
							<div class="btn-submit ${submitBtnClass} fl">
				           		<input name="" type="submit" class="gry-btn btn-normal" value="Complete Order" id="codSubmit">	          
				           	</div>
				           	<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
				           	<div class="clear"></div>
				        </div>
			        </c:if>
			        
			        <c:if test="${!txnConfig.codHybridAllowed}">
			        	<div class="alert mt10">
							COD is not available for this transaction
						</div>
			        </c:if>
			        
					
				</div>
			</c:if>
			
	       
		</form>
	</div>
	
    <div class="clear"></div>
</div>