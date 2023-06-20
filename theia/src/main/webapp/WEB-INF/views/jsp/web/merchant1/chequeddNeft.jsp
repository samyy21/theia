<div class ='card content ${14 eq paymentType ? "active" : ""}' id ="cheque-dd-neft">

	<div id="innerTabs" class="relative">
		<ul class="innerTabs">
			<li class="active" style="margin-left:0;">
				<a href="#Pay-by-Cheque">Pay by Cheque</a>
			</li>
			<li>
				<a href="#Pay-by-DD">Pay by DD</a>
			</li>
			<li>
				<a href="#Pay-by-NEFT">Pay by NEFT</a>
			</li>
		</ul>
		<a href="https://paytm.com/faq?option=offline-payments" target="_blank" class="faqs">FAQ</a>
		<div class="clear"></div>
		
	</div>

	

	<div id="Pay-by-Cheque" class="neftddBox fl mt20">
	<!-- Pay Info -->
		
	<!-- Pay Info END -->
	
		<form autocomplete="off" class="Cheque-Pay validated" name="Cheque-Pay" method="post" action="submitTransaction?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="CHEQUE" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			<input type="hidden" name="bankCode" id="bankCode">
			

			<c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			
			
					
			<ul class="">
				<li>
					<label for="offlineTxnId" class="mb10 relative">CHEQUE NUMBER
						
					<div id="payInfo" class="relative payInfo">
						<div class="infoBox fr"></div>
						<div class="hide info_checkDDNeft">
							<p class="font600 b">Consider the following details, while depositing the cheque</p>
						<p>
						Bank Name: HDFC Bank Limited</p>
					<p>Account Number : 00030350012930</p>
			
					<p>	Account Name: ONE97 COMMUNICATIONS LIMITED</p>
						</div>
					</div>
						
					</label> 
					
					<c:set var="impsMobileErrorClass"></c:set>
					<%--<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<c:set var="itzCardNumberErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="text-input ChequeDD-Pay ${itzCardNumberErrorClass} required" name="offlineTxnId"  maxlength="6" size="25" id="chequeNo"/>
					</p>
					<c:if test="${itzCardNumberErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
					</c:if>--%>
				</li>
				
				<li class="mt20">
					
					<c:if test="${paymentConfig.offlineList.size() > 0}">
					<div id="other-banks-wrapper">
						<label class="mt20 mb10 chequeBank" for="chequeBank">SELECT YOUR BANK</label>
						<div id = "nbWrapper">
						
							<select class="offlineSelect chequeBank required" name="chequeBank" id="chequeBank" data-size="5">
								<option value = "-1">Select</option>
								<c:forEach var="bank" items="${paymentConfig.offlineList}" begin="0">
									<option value="${bank.bankName}" >${bank.displayName}</option>
								</c:forEach>
							</select>
							
						</div>	
					</div>
					</c:if>
					<%-- <c:set var="itzPasswordErrorClass"></c:set>
					<c:if test="${errorsPresent && !empty requestScope.validationErrors['INVALID_PWD']}">
						<c:set var="itzPasswordErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="password" class="text-input ${itzPasswordErrorClass} required" name="itzPwd" maxlength="20" id="itzPwd" size="12"/>
					</p>
					<c:if test="${itzPasswordErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_PWD']}</div>
					</c:if> --%>
				</li>
				
			</ul>
			
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="submit-btn" type="submit" class="gry-btn btn-normal " value="Pay now" id="itzCashSubmit">	          
	           	</div>
	           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
	           	<div class="clear"></div>
	        </div>
	       
		</form>
		
	</div>
	<div id="Pay-by-DD" class="neftddBox fl mt20 hide">
	<form autocomplete="off" class="dd-Pay validated" name="dd-Pay" method="post" action="submitTransaction?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="DD" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="txn_Mode" value="DD" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="bankCode" id="bankCode">
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />

			<c:if test="${txnConfig.addMoneyFlag}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			
			
					
			<ul class="">
				<li>
					<label for="offlineTxnId" class="mb10 relative">DD NUMBER
						<div id="payInfo" class="relative payInfo">
						<div class="infoBox fr"></div>
						<div class="hide info_checkDDNeft">
							<p class="font600 b">Consider the following details while depositing a DD.</p>
						<p>
						Bank Name: HDFC Bank Limited</p>
					<p>Account Number : 00030350012930</p>
			
					<p>	Account Name: ONE97 COMMUNICATIONS LIMITED</p>
						</div>
					</div>
					</label>
					<c:set var="impsMobileErrorClass"></c:set>
					${requestScope.validationErrors }
					<c:if test="${errorsPresent  && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<c:set var="itzCardNumberErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="text-input ChequeDD-Pay ${itzCardNumberErrorClass} required" name="offlineTxnId" size="25" maxlength="6" id="DDNo"/>
					</p>
									</li>
				
				<li class="mt20">
					<c:if test="${paymentConfig.offlineList.size() > 0}">
					<div id="other-banks-wrapper">
						<label class="mt20 mb10 ddBank" for="ddBank">SELECT YOUR BANK</label>
						<div id = "nbWrapper">
							<select class="offlineSelect ddBank required" name="ddBank" id="ddBank" data-size="5">
								<option value = "-1">Select</option>
								<c:forEach var="bank" items="${paymentConfig.offlineList}" begin="0">
									<option value="${bank.bankName}">${bank.displayName}</option>
								</c:forEach>
							</select>
						</div>	
					</div>
					</c:if>
				</li>
				
			</ul>
			
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="submit-btn" type="submit" class="gry-btn btn-normal" value="Pay now" id="itzCashSubmit">	          
	           	</div>
	           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
	           	<div class="clear"></div>
	        </div>
	       
		</form>
	
	</div>
	<div id="Pay-by-NEFT" class="neftddBox fl mt20 hide">
	<form autocomplete="off" class="NEFT-Pay validated" name="NEFT-Pay" method="post" action="submitTransaction?${queryStringForSession}" id="card">
			<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
			<input type="hidden" name="txnMode" value="NEFT" />
			<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="bankCode" id="bankCode">
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />

			<c:if test="${txnConfig.addMoneyFlag}">
				<input type="hidden" name="addMoney" value="1" />
			</c:if>
			
			
					
			<ul class="">
				<li>
					<label for="offlineTxnId" class="mb10 relative">NEFT NUMBER
					<div id="payInfo" class="relative payInfo">
						<div class="infoBox fr"></div>
						<div class="hide info_checkDDNeft" >
							<p class="font600 b">Consider the following details for NEFT</p>
						<p>
						Bank Name: HDFC Bank Limited</p>
					<p>Account Number : 00030350012930</p>
			
					<p>	Account Name: ONE97 COMMUNICATIONS LIMITED</p>
					<p>IFSC Code: HDFC0000003</p>
						</div>
					</div>
					</label>
					<c:set var="impsMobileErrorClass"></c:set>
					<c:if test="${errorsPresent  && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
						<c:set var="itzCardNumberErrorClass">error1</c:set>
					</c:if>
					<p>
						<input type="text" class="text-input ${itzCardNumberErrorClass} required" name="offlineTxnId" size="25" maxlength="22" id="neftNo"/>
					</p>
					<c:if test="${itzCardNumberErrorClass eq 'error1'}">
						<div class="error error2">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
					</c:if>
				</li>
				
				<li class="mt20">
					<c:if test="${paymentConfig.offlineList.size() > 0}">
					<div id="other-banks-wrapper">
						<label class="mt20 neftBank mb10" for="neftBank">SELECT YOUR BANK</label>
						<div id = "nbWrapper">
							<select class="offlineSelect neftBank required" name="neftBank" id="neftBank" data-size="5">
								<option value = "-1">Select</option>
								<c:forEach var="bank" items="${paymentConfig.offlineList}" begin="0">
									<option value="${bank.bankName}" >${bank.displayName}</option>
								</c:forEach>
							</select>
						</div>	
					</div>
					</c:if>
				</li>
				
			</ul>
			
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="submit-btn" type="submit" class="gry-btn btn-normal " value="Pay now" id="itzCashSubmit">	          
	           	</div>
	           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
	           	<div class="clear"></div>
	        </div>
	       
		</form>
	
	</div>
	
    <div class="clear"></div>
</div>