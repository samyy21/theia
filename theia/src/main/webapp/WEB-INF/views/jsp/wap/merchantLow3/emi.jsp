<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<c:if test="${13 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
 <div class="heading ml15 mt20" >EMI</div>
	<p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading ml15 mt20" >EMI</div>
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
		<c:set var="emiSelectedPlanId" value="${emiSelectedBank}|${param.emi_plan_id}" scope="session"></c:set>
	</c:if>
s

	<c:set var="emiBankMap" value="${entityInfo.completeEMIInfoList}"></c:set>
	<c:if test="${usePaytmCash eq true && not empty entityInfo.hybridEMIInfoList}">
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
		<div class="row-dot ml20 mr20">
			<select id="emiBankSelect" name="emiBankName" class="mt5 mb5 all-input  color-fields special-input-select positionYExpYear" onchange = "onEMIBankSelect(this); selectBoxColorChange(this);" style="width: 100%;">
				<option id="select-option" value="none">Select Your Bank</option>
				<c:forEach var="bank" items="${emiBankMap}">
			    	<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${emiSelectedBank eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
				</c:forEach>
			</select>

			<br>
			<c:if test="${!empty param.emi_bank && 'AMEX' eq param.emi_bank || emiSelectedBank eq 'AMEX'}">
				<div id="emiAmexConveFeeMsg" class="failure" style="font-size:12px;color:#222;padding:10px;margin-left:0;line-height:17px;width:100%; box-sizing: border-box; border:none; background:#f9ffcf;">2% processing fee would be charged by your bank for the first EMI.</div>
			</c:if>
			<br>

			<c:forEach var="bank" items="${emiBankMap}">
				<c:if test="${emiSelectedBank eq bank.bankName}">
					<div class='mt10'>
						<label class="mt10" style="    margin-bottom: 15px;float: left; display: block;width: 100%;">Select EMI plan</label>
						<ul id="${bank.bankName}-emi-plans" class="emi-plans mt5" >
							<c:forEach var="emiPlan" items="${bank.emiInfo}" varStatus="loop">
								
								<%-- auto select first plan --%>
								<c:if test="${empty emiSelectedPlanId}">
									<c:set var="emiSelectedPlanId" value="${emiPlan.planId}"></c:set>
								</c:if>
								<script>
									console.log('index:','${loop.index}','${loop.last}');
								</script>
							    <li style="width: 100%; text-align: left;border: none;height: 55px;
							    		<c:if test="${loop.first }">margin-top: -10px;</c:if>
							    		<c:if test='${loop.index%2 == 1 }'>background: #f9f9f9;margin-left: -30px; padding-left: 35px; padding-right: 6%;<c:if test='${loop.last }'>margin-bottom: -15px;</c:if></c:if>">
							    	<div class="radio-item">
								    	<input type="radio" name="emiPlanId" id="emiPlanId-${emiPlan.planId}" value="${emiPlan.planId}" class="fl" <c:if test="${emiSelectedPlanId eq emiPlan.planId}">checked</c:if> onclick="onEMIPlanSelect(this)">
										<label class="emi-month" for="emiPlanId-${emiPlan.planId}">${emiPlan.ofMonths} Months</label>
										<span class="emi-interest ml20" for="emiPlanId-${emiPlan.planId}">${emiPlan.interestRate}% p.a</span>
										<span class="emi-amt fr" for="emiPlanId-${emiPlan.planId}" style="margin-right: 10px;">Rs ${emiPlan.emiAmount}</span>
										<br>
										<span class="fr" style="margin-top: -1%; font-size: 10px;color: #999; margin-right: 10px;">per month</span>
									</div>
								</li>    
							</c:forEach>
						</ul>
					</div>
				</c:if>
			</c:forEach>
		</div>
			
		
		<div class='row-dot ml20 mr20 <c:if test="${empty emiSelectedPlanId}">hide</c:if>'>
			<p>
<!-- 		    	<label>Card No.</label><br /> -->
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARDNMBR']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARDNMBR"]}</div>' var="errorText"></c:set>
				</c:if>
		    	<input id="card-number" name="cardNumber" type="tel" maxlength="19" class="mt5 ${cssClass} all-input" placeholder="Card Number" onblur="onCardNumberBlur()" onkeydown="pressCardSpace(event); onCardDown();" style="width: 98.4%;"/>
		    	${errorText}
		    	<c:if test="${!empty txnInfo.promoCodeResponse.promocodeData.cardBins}">
		    		<div id="wrong-promo-card-msg" class="error-txt hide">
						Enter ${txnInfo.promoCodeResponse.promocodeData.cardBins } to avail benefits
			    	</div>
		    	</c:if>
		    </p>
		    
		    <p class="pt7" style="padding-top: 20px;">
		    	<label class="color-label">Expiry / Validity Date</label><br />
		    	<div class="fl">
<!-- 		            <label>Month</label><br /> -->
		            <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		            <c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARDEXPIRY']}">
						<c:set value = "error" var="cssClass"></c:set>
						<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CARDEXPIRY"]}</div>' var="errorText"></c:set>
					</c:if>
		            <select name="expiryMonth" class="expSelect all-input  mt5 ${cssClass} color-fields special-input-select pr15 positionYExpYear" onchange="selectBoxColorChange(this);">
		                 <option value="0" class="color-fields">MM</option>
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
		            <br/>
            	${errorText}
		        </div>
		        <div class="fl ml20">
<!-- 		        	<label>Year</label><br /> -->
		            <select name="expiryYear" class="expSelect color-fields all-input mt5 ${cssClass} color-fields special-input-select pr15 positionYExpYear" onchange="selectBoxColorChange(this);">
		               <option value="0">YY</option>
		                    <c:forEach var="i" begin="${currentYear}" end="${currentYear + 35}">
							 <option value="${i}">${i}</option>
							  
							</c:forEach>
		            </select>
		        </div>
		       
<!-- 		    </p> -->
		    
<!-- 		    <p class="pt7 cvv"> -->
<!-- 		    	<label>CVV/Security Code</label><br /> -->
		    	 <c:remove var = "cssClass"/> <c:remove var = "errorText"/> 
		    	<c:if test="${'13' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVVNUMBER']}">
					<c:set value = "error" var="cssClass"></c:set>
					<c:set value = '<div class="error-txt">${requestScope.validationErrors["INVALID_CVVNUMBER"]}</div>' var="errorText"></c:set>
				</c:if>
		        <input type="password" placeholder="CVV" maxlength="4" size="5" name="cvvNumber" style="float: right;font-size: 14px !important;margin-right: 3px;" class ="ml20 cvv-input ${cssClass} mt7" onkeypress="onKeypressCvvNumber(arguments[0])"/> 
        		<img src="/theia/resources/images/wap/merchantLow3/c-v-v-example.png" alt="Security Code" title="Security Code" class="ml50 cvv-after-img" />
        		${errorText}
		    </p>
		    <div class="clear"></div>
		    
    
		    <c:if test="${saveCardOption}">
			    <p class="pt7 mt10" style="padding-top: 15px; padding-bottom: 10px;">
			    	<input  type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked"/>  <label for="saveCard" class="saved-card-label">Save this card for future payments</label>
			    </p>
		    </c:if>
		    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
				<div class="post-conv-inclusion">
					<%@ include file="../../common/post-con-withr/emi-postconv.jsp" %>
				</div>
			</c:if>
		    <p class="pt7 "><button type="submit"  class="blue-btn"  style="padding-bottom: 15px;" >${submitBtnText}</button></p> 
		    
		    <!--Lock image-->
		    <div class="pt7 mt15" style="margin-bottom: 15px;">
		    	<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
        		<div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
       			<div class="fl lock-display"> We do not share your card details with anyone</div>
		        <div class="clear"></div>
		    </div>
		    
		</div>
		
	</c:if>
	
</form>

</c:otherwise>
</c:choose>
<div class="mt20"></div>
</c:if>
