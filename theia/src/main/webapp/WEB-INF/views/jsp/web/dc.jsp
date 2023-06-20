<%@ page import="java.util.List" %>
<script type="text/javascript">
	maintainenceATMBank = new Object();
	lowPerfATMBank = new Object();
</script>
<%
   	String reqBankCode = (String) request.getAttribute("reqBankCode");
	List dbList = (List)session.getAttribute("DebitCardList") ;
	List atmList = (List)session.getAttribute("ATMCardList") ;
	if((dbList == null || dbList.size()==0) && (atmList == null || atmList.size()==0)
			|| "other".equals(reqBankCode)) {
		reqBankCode = "other";
	}
%>

<div id="debitcard-container" <c:if test="${'2' == paymentType || '8' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="debitcard-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="DC" />
		<input type="hidden" name="txn_Mode" value="DC" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="AUTH_MODE" value="3D" />
		<input type="hidden" name="bankCode" id="bankCodeAtm">
		<div class="query">
			<c:if test="${!empty sessionScope.completeDcList}">
				<div class=" margin-auto1">
					<div class="banks">
						<div class="label label1">Popular Banks</div>
						<ul class="atm-panel ul" style="margin-left: 250px">
							<c:forEach begin="0" end="2" step="1" varStatus="loopVar">
								<c:if test="${sessionScope.completeDcList.size() > loopVar.index}">
									<c:set var="bank" value="${sessionScope.completeDcList[loopVar.index]}" />
									
									<script type="text/javascript">
										<c:if test="${bank.maintainence}">
											maintainenceATMBank["${bank.bankName}"] = true;
										</c:if>
										<c:if test="${bank.lowPercentage}">
											lowPerfATMBank["${bank.bankName}"] = true;
										</c:if>
									</script>
									
									<li id="${bank.bankName}" title="${bank.displayName}" class="bankLi">
										<c:choose>
											 <c:when test="${!empty bank.bankWebLogo}">
												<img src="images/web/bank/${bank.bankWebLogo}" class="cardPad" alt="${bank.displayName}"/>
											 </c:when>
											 <c:otherwise>
											 	<span class="cardPad">${bank.displayName}</span>
											 </c:otherwise>
										</c:choose>
									</li>
								</c:if>
							</c:forEach>
						</ul>
						<ul class="atm-panel ul" style="margin-left: 255px">
							<c:forEach begin="3" end="5" step="1" varStatus="loopVar">
								<c:if test="${sessionScope.completeDcList.size() > loopVar.index}">
									<c:set var="bank" value="${sessionScope.completeDcList[loopVar.index]}" />
									
									<script type="text/javascript">
										<c:if test="${bank.maintainence}">
											maintainenceATMBank["${bank.bankName}"] = true;
										</c:if>
										<c:if test="${bank.lowPercentage}">
											lowPerfATMBank["${bank.bankName}"] = true;
										</c:if>
									</script>
									
									<li id="${bank.bankName}" title="${bank.displayName}" class="bankLi">
										<c:choose>
											 <c:when test="${!empty bank.bankWebLogo}">
												<img src="images/web/bank/${bank.bankWebLogo}" class="cardPad" alt="${bank.displayName}"/>
											 </c:when>
											 <c:otherwise>
											 	<span class="cardPad">${bank.displayName}</span>
											 </c:otherwise>
										</c:choose>
									</li>
								</c:if>
							</c:forEach>
						</ul>
						<div class="clear"></div>
					</div>
				</div>
				<c:if test="${sessionScope.completeDcList.size() > 6}">
					<div class="gray"">
						<label>Others</label>
						<select id="ATMBankCodeSelect" style="width: 309px">
							<option value="-1">Select your card</option>
							<c:forEach var="item" items="${sessionScope.completeDcList}" begin="6">
								<option value="${item.bankName}" <c:if test="${reqBankCode eq item.bankName}">selected=selected</c:if>>${item.displayName}</option>
								<script type="text/javascript">
									<c:if test="${item.maintainence}">
										maintainenceATMBank["${item.bankName}"] = true;
									</c:if>
									<c:if test="${item.lowPercentage}">
										lowPerfATMBank["${item.bankName}"] = true;
									</c:if>
								</script>
							</c:forEach>
						</select>
					</div>
				</c:if>
				<div class="gray" id="debitcard-form" style="padding-top:0px; display:none;">
					<div class="fl width615">
						<div class="mt6">
							<div class="fl">
								<label>Card Number</label>
								<input autocomplete="off" type="text" name="cardNumber" id="cardNumberDc" class="FI" maxlength="19" value="${param.cardNumber}" />
							</div>
							<div id="dcImg" class="fl mt6 ml6">
								<img id="maestroImg" title="Maestro card" src="images/web/maestro.png">
								<img id="masterImg" title="Master card" src="images/web/master.png">
								<img id="visaImg" title="Visa"  src="images/web/visa.png">
							</div>
							<div class="clear"></div>
						</div>
						<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
							<div id="dcCCnoId" style="display:block" class="error">${requestScope.validationErrors['INVALID_CARD']}</div>
						</c:if>
						<div class="mt6">
							<label>Expiry date</label>
							<select name="expiryMonth">
								<option value="0">month</option>
								<c:forEach var="loopVar" begin="1" end="12" step="1">
									<c:set var="selected" value="" />
									<fmt:formatNumber var="formattedLoopVar" value="${loopVar}" pattern="00" />
										<c:if test="${formattedLoopVar eq param.ccExpiryMonth}">
											<c:set var="selected">selected="selected"</c:set>
										</c:if>
										<option value="${formattedLoopVar}" ${selected}>${formattedLoopVar}</option>
								</c:forEach>
							</select>
							<select name="expiryYear">
								<option value="0">year</option>
								<c:forEach var="loopVar" begin="${year}" end="2049" step="1">
									<c:set var="selected" value="" />
									<c:if test="${loopVar eq param.ccExpiryYear}">
										<c:set var="selected">selected="selected"</c:set>
									</c:if>
									<option value="${loopVar}" ${selected}>${loopVar}</option>
								</c:forEach>
							</select>
							<div class="clear"></div>
							<label></label>
							<span class="italic maestro">(Optional for Maestro Cards)</span>
						</div>
						<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
							<div id="dcexId" style="display:block" class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
						</c:if>
						<div class="clear"></div>
						<div class="cc-sec">
							<label  class="fl">CVV</label>
							<input autocomplete="off" type="password" name="cvvNumber" class="fl padd3 width127" size="15" maxlength="3"/>
							
							<div class="clear"></div>
		                       <label></label>
		                       <span class="italic maestro">(Optional for Maestro Cards)</span>
						</div>
						<div class="clear"></div>
						<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
							<div id="dcCvvId" class="error">${requestScope.validationErrors['INVALID_CVV']}</div>
						</c:if>
					
						<div class="clear"></div>
							<c:if test="${1 eq saveCardOption}">
								 <div class="save-card-option ml137">
									<input value="N" name="storeCardFlag" type="hidden" />
									<span class="gry">Save this card for future transaction.</span>
								</div>
							</c:if>
	                	<div class="clear"></div>
					</div>
				
					<div class="fr width-180 cvv-cc" style="display: block;">
						<div class="fr lock left">
							<div class="fl">
								<img title="Secured" alt="Secured" src="images/web/lock.png">
							</div>
							<div class="fl widthh">
								<span class="gry">Your card details are secured via 128 Bit encryption by Verisign.</span>
							</div>
							<div class="clear"></div>
						</div>
						
						<div class="clear"></div>
						
						<div class="blue ml10" style="margin:0;">
							<img style="margin-bottom:5px;" title="CVV" alt="CVV" src="images/web/cvv-no.png">
							<br>
							What is CVV?
							<br>
							<span class="gry">CVV Number is the last 3 digit printed on the signature panel on the back of your credit card</span>
						</div>
						<div class="clear"></div>
					</div>
					
					<div class="clear"></div>
				</div>
				<div id="atmWarningDiv" class="warning">
					<img class="fl" src="images/web/alert.png">
					<span id="atmErrorMsg"></span>
				</div>
				<div class="padd-left">
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_DC_BANK']}">
						<span id="dccardId" style="display:block;" class="error">${requestScope.validationErrors['INVALID_DC_BANK']}</span>
					</c:if>
					<div class="ml137">
						<input type="submit" class="submit" title="Pay Now" value="Pay Now" id="dcSubmit"/>
						<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
						<div class="margin italics">(You will be directed to your bank's 3D-Verification page)</div>
					</div>
					<div class="clear"></div>
				</div>
			</c:if>
		</div>
	</form>
</div>
<script>
	$(document).ready(function(){
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		if(bankName.trim().length > 0) {
			var obj = $('.atm-panel #' + bankName);
			if(obj.length > 0) {
				displayName = obj.attr('title');
			} else {
				displayName = $('#ATMBankCodeSelect option:selected').text();
			}
			$('#bankCodeAtm').val(bankName);
			$('.atm-panel li#' + bankName).addClass('chek');
			if (bankName.indexOf('other')>=0){
				$('#debitcard-form').show('slow');
			}
			
			if(maintainenceATMBank[bankName]) {
				$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#dcSubmit').attr("disabled", "disabled");
				$('#atmWarningDiv').show();
				$('#dcSubmit').removeClass('submit');
				$('#dcSubmit').addClass('disableSubmit');
			} else if(lowPerfATMBank[bankName]) {
				$('#atmWarningDiv').show();
				$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
		} else {
			$('#dcSubmit').removeClass('submit');
			$('#dcSubmit').addClass('disableSubmit');
			$('#dcSubmit').attr("disabled", "disabled");
		}
	});
	
</script>
