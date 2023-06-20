<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${13 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading">EMI</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading">EMI</div>
<form id="emiForm" autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return submitForm(this)">
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
		<c:set var="emiSelectedPlanId" value="${emiSelectedBank}|${param.emi_plan_id}" scope="session"></c:set>
	</c:if>


	<c:set var="emiBankMap" value="${entityInfo.completeEMIInfoList}"></c:set>
	<c:if test="${usePaytmCash eq true && not empty entityInfo.hybridEMIInfoList}">
		<c:set var="emiBankMap" value="${entityInfo.hybridEMIInfoList}"></c:set>
	</c:if>


	<c:if test="${fn:length(emiBankMap) eq 1}">
		<c:set var="emiSelectedBank" value="${emiBankMap[0].bankName}" scope="session" />
		<c:set var="emiDisabledBank" value="true" scope="session" />
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
			<label>Select your Bank</label><br>
			<c:if test="${emiDisabledBank eq true}">
				<input type="hidden" name="emiBankName" value="${emiSelectedBank}">

			</c:if>
			<select id="emiBankSelect" name="emiBankName" class="mt5 mb5" onchange = "onEMIBankSelect(this)" <c:if test="${emiDisabledBank eq true}">disabled="disabled"</c:if>>
				<option id="select-option" value="none">Select</option>
				<c:forEach var="bank" items="${emiBankMap}">
			    	<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${emiSelectedBank eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
				</c:forEach>
			</select>

			<br>

			<c:if test="${(!empty param.emi_bank && param.emi_bank ne 'none') && ('AMEX' ne param.emi_bank || emiSelectedBank ne 'AMEX')}">
				<div id="emiAmexConveFeeMsg" class="failure" style="font-size:12px;color:#222;padding:10px;margin-left:0;line-height:17px;width:90%; border:none; background:#f9ffcf;">Taxes extra, as per bank terms and conditions.</div>
			</c:if>
			<br>

			
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
									<span class="emi-amt"><span class="WebRupee">Rs</span> ${emiPlan.emiAmount}</span>
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
			
		
		<div class='row-dot <c:if test="${empty emiSelectedPlanId}">hide</c:if>'>
			<p>
		    	<label for="cardNumber">Card No.</label><br />
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD"]}</div>' var="errorText"></c:set>
				</c:if>
		    	<input id="card-number" name="cardNumber" type="tel" maxlength="23" class="mt5 ${cssClass}" onblur="onCardNumberBlur()" onkeydown="pressCardSpace(event);" onkeyup="onCardDown(true,event);"/>
					${errorText}

		    </p>
		    
		    <p class="pt7">
		    	<div class="fl">
		            <label for="expiryMonth">Month</label><br />
		            <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		            <c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<c:set value = "error" var="cssClass"></c:set>
						<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARD_EXPIRY"]}</div>' var="errorText"></c:set>
					</c:if>
		            <select id="emiExpMnth" name="expiryMonth" class="expSelect mt5 ${cssClass}">
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
		        	<label for="expiryYear">Year</label><br />
		            <select id="emiExpYear" name="expiryYear" class="expSelect mt5 ${cssClass}">
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
		    	<label for="cvvNumber">CVV/Security Code</label><br />
		    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVV"]}</div>' var="errorText"></c:set>
				</c:if>
		        <input id="emiCVVBox" type="tel" maxlength="4" size="5" name="cvvNumber" class ="mask cvv-input ${cssClass}" onkeyup="onKeypressCvvNumber(arguments[0])"/> <img id="defaultCvvIcon" src="${ptm:stResPath()}images/wap/merchantLow5/cvv.png" alt="Security Code" title="Security Code" /><img id="amexCvvIcon" class="hide" src="${ptm:stResPath()}images/wap/merchantLow5/security-code.png" alt="Security Code" title="Amex Cvv" />
		        ${errorText}
		    </p>
		    
		   
		    <c:if test="${saveCardOption }">
			    <p class="pt7">
			    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/> Save this card for future checkout
			    </p>
		    </c:if>
		      <%--post convenince related --%>
		     <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null &&  txnConfig.paymentCharges.HYBRID.totalConvenienceCharges ne '0.00'}">
		     	<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
				<div class="post-conv-inclusion" style="display:none;margin-right: 10px;" id="hybrid-post-con" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
					<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
				</div>
	 		 </c:if>
	 		  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null &&  txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
					<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
					<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
						<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
					</div>
			 </c:if>
		    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.EMI != null &&  txnConfig.paymentCharges.EMI.totalConvenienceCharges ne '0.00'}">
					<div class="post-conv-inclusion" style="margin-right: 10px;" id="normal-post-con">
						<%@ include file="../../common/post-con/emi-postconv.jsp" %>
					</div>
			</c:if>
			<div class="card-bin-disable mt10 hide clear">
				<div class="card-bin-disable-error-msg mt5"></div>
			</div>
		    <p class="pt7 ">
		    	<button type="submit" id="btnSubmit" class="blue-btn" data-txnmode="EMI" onclick="pushGAData(this, 'pay_now_clicked')" >Pay Now</button>
		    </p>
		</div>
		
	</c:if>
	
</form>
<script>
	try {
		_paq.push(['trackEvent', 'Payment Mode', 'emi']);
	} catch(e){}


	function onEMIPlanSelect(checkbox){
		var emiValue = checkbox.value;
		emiValue = emiValue.split('|')[1];
		location.href = "/theia/jsp/wap/merchantLow5/paymentForm.jsp?txn_Mode=EMI&emi_bank=${emiSelectedBank}&emi_plan_id=" + emiValue + "&${queryStringForSession}";


	}

</script>
</c:otherwise>
</c:choose>
</c:if>