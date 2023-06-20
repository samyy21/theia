<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${13 eq paymentType}">
<div class="heading">EMI</div>
<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm()">
	<input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
	<input type="hidden" name="txnMode" value="EMI" />
	<input type="hidden" name="channelId" value="WAP" />
	<input type="hidden" name="AUTH_MODE" value="3D" />
	<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
	<input type="hidden" name="walletAmount" value="0" />

	<c:if test="${!empty param.emi_bank}">
		<c:set var="emiSelectedBank" value="${param.emi_bank}" scope="session"></c:set>
		<c:if test="${param.emi_bank eq 'none'}">
			<c:remove var="emiSelectedPlanId" scope="session"></c:remove>
		</c:if>
	</c:if>
	<c:if test="${!empty param.emi_plan_id}">
		<c:set var="emiSelectedPlanId" value="${param.emi_plan_id}" scope="session"></c:set>
	</c:if>
	
	
	<c:set var="emiBankMap" value="${entityInfo.completeEMIInfoList}"></c:set>
	<c:if test="${usePaytmCash eq true}">
		<c:set var="emiBankMap" value="${entityInfo.hybridEMIInfoList}"></c:set>
	</c:if>
	
	
	<c:set var="emiAvailable" value="true"></c:set>
	<c:if test="${empty emiBankMap}">
		<div class="failure">
			EMI is not available for this transaction
		</div>
		<c:set var="emiAvailable" value="false"></c:set>
	</c:if>
	
	
	
	<c:if test="${emiAvailable}">
		<div class="row-dot">
			<div class="fields">
			<label>Select your Bank</label><br>
			<select id="emiBankSelect" name="emiBankName" class="mt5 mb5" onchange = "onEMIBankSelect(this)">
				<option id="select-option" value="none">Select</option>
				<c:forEach var="bank" items="${emiBankMap}">
			    	<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${emiSelectedBank eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
				</c:forEach>
			</select>
			
			<c:forEach var="bank" items="${emiBankMap}">
				<c:if test="${emiSelectedBank eq bank.bankName}">
					<div class='mt10'>
						<label class="mt10">Select EMI plan</label>
						<ul id="${bank.bankName}-emi-plans" class="emi-plans mt5">
							<c:forEach var="emiPlan" items="${bank.emiInfo}">
								
								<%-- auto select first plan --%>
								<c:if test="${empty emiSelectedPlanId}">
									<c:set var="emiSelectedPlanId" value="${emiPlan.planId}"></c:set>
								</c:if>
							
							    <li>
							    	<input type="radio" name="emiPlanId" value="${emiPlan.planId}" class="fl" <c:if test="${emiSelectedPlanId eq emiPlan.planId}">checked</c:if> onclick="onEMIPlanSelect(this)">
									<span class="emi-amt">Rs ${emiPlan.emiAmount}</span>
									<br>
									<span class="emi-month">${emiPlan.ofMonths} Months</span>
									<br>
									<span class="emi-interest">${emiPlan.interestRate}% p.a</span>
								</li>    
							</c:forEach>
						</ul>
					</div>
				</c:if>
			</c:forEach>
			</div>
		</div>
			
		
		<div class='row-dot <c:if test="${empty emiSelectedPlanId}">hide</c:if>'>
			<p>
		    	<label>Card No.</label><br />
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
				</c:if>
		    	<input id="card-number" name="cardNumber" type="tel" maxlength="16" class="mt5 ${cssClass}" onblur="onCardNumberBlur()"/>
		    	${errorText}
		    	<c:if test="${!empty txnInfo.promoCodeResponse.promocodeData.cardBins}">
		    		<div id="wrong-promo-card-msg" class="error-txt hide">
						Enter ${txnInfo.promoCodeResponse.promocodeData.cardBins } to avail benefits
			    	</div>
		    	</c:if>
		    </p>
		    
		    <p class="pt7">
		    	<div class="fl">
		            <label>Month</label><br />
		            <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		            <c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<c:set value = "error" var="cssClass"></c:set>
						<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
					</c:if>
		            <select name="expiryMonth" class="expSelect mt5 ${cssClass}">
		                 <option value="0">MM</option>
		                 <option value="01">01</option>
		                 <option value="02">02</option>
		                 <option value="03">03</option>
		                 <option value="04">04</option>
		                 <option value="05">05</option>
		                 <option value="06">06</option>
		                 <option value="07">07</option>
		                 <option value="08">08</option>
		                 <option value="09">09</option>
		                 <option value="10">10</option>
		                 <option value="11">11</option>
		                 <option value="12">12</option>
		            </select>
		        </div>
		        <div class="fl ml20">
		        	<label>Year</label><br />
		            <select name="expiryYear" class="expSelect mt5 ${cssClass}">
		               <option value="0">YY</option>
		                    <c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}">${i}</option>
							  
							</c:forEach>
		            </select>
		        </div>
		        <div class="clear"></div>
		        ${errorText}
		    </p>
		    
		    <p class="pt7 cvv">
		    	<label>CVV/Security Code</label><br />
		    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
				</c:if>
		        <input type="tel" maxlength="4" size="5" name="cvvNumber" class ="cvv-input ${cssClass}"/> <img src="${ptm:stResPath()}images/wap/merchantLow/security-code.png" alt="Security Code" title="Security Code" />
		        ${errorText}
		    </p>
		    <c:if test="${saveCardOption && !walletInfo.walletFailed}">
			    <p class="pt7">
			    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> Save this card for future checkout
			    </p>
		    </c:if>
		    
		    <p class="pt7 "><input type="submit" value="Pay now" class="blue-btn" /></p> 
		    
		    <!--Lock image-->
		    <div class="pt7">
		    	<div class="fl image"><img src="${ptm:stResPath()}images/wap/merchantLow/lock.png" alt="" title="" /></div>
		        <div class="fl small secure-text"> Your card details are secured via 128 Bit encryption by Verisign</div>
		        <div class="clear"></div>
		    </div>
		    
		</div>
		
	</c:if>
	
</form>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'emi']);
	} catch(e){}
</script>
</c:if>