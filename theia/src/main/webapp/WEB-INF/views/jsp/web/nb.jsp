<div id="netbanking-container" <c:if test="${'3' == paymentType}">style="display:block"</c:if>>
	<script type="text/javascript">
		maintainenceNBBank = new Object();
		lowPerfNBBank = new Object();
	</script>
	<%
   		String reqBankCode = (String) request.getAttribute("reqBankCode");
	%>
	<form autocomplete="off" name="netbanking-form" method="post" action="submitTransaction">
		<input type="hidden" name="txnMode" value="NB" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="NB" />
	 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	 	<input type="hidden" name="bankCode" id="bankCode">
		<div class="query">
			<c:if test="${!empty sessionScope.MerchBankList}">
				<div class=" margin-auto1">
					<div class="banks">
						<div class="label label1">Popular Banks</div>
						<ul class="netbanking-panel ul" style="margin-left: 250px">
							<c:forEach begin="0" end="2" step="1" varStatus="loopVar">
								<c:if test="${sessionScope.MerchBankList.size() > loopVar.index}">
									<c:set var="bank" value="${sessionScope.MerchBankList[loopVar.index]}" />
									
									<script type="text/javascript">
										<c:if test="${bank.maintainence}">
											maintainenceNBBank["${bank.bankName}"] = true;
										</c:if>
										<c:if test="${bank.lowPercentage}">
											lowPerfNBBank["${bank.bankName}"] = true;
										</c:if>
									</script>
									
									<li id="${bank.bankName}" title="${bank.displayName}" class="bankLi">
										<img src="images/web/bank/${bank.bankWebLogo}" class="cardPad" alt="${bank.displayName}"/>
									</li>
								</c:if>
							</c:forEach>
						</ul>
						<ul class="netbanking-panel ul" style="margin-left: 255px">
							<c:forEach begin="3" end="5" step="1" varStatus="loopVar">
								<c:if test="${sessionScope.MerchBankList.size() > loopVar.index}">
									<c:set var="bank" value="${sessionScope.MerchBankList[loopVar.index]}" />
									
									<script type="text/javascript">
										<c:if test="${bank.maintainence}">
											maintainenceNBBank["${bank.bankName}"] = true;
										</c:if>
										<c:if test="${bank.lowPercentage}">
											lowPerfNBBank["${bank.bankName}"] = true;
										</c:if>
									</script>
									
									<li id="${bank.bankName}" title="${bank.displayName}" class="bankLi">
										<img src="images/web/bank/${bank.bankWebLogo}" class="cardPad" alt="${bank.displayName}"/>
									</li>
								</c:if>
							</c:forEach>
						</ul>
						<div class="clear"></div>
					</div>
				</div>
				<c:if test="${sessionScope.MerchBankList.size() > 6}">
					<div class="padd-left gray">
						<div>
							<label>Others</label>
							<select id="bankCodeSelect" style="width:309px">
								<option value="-1">Select your bank</option>
								<c:forEach var="bank" items="${sessionScope.MerchBankList}" begin="6">
									<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${reqBankCode eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
									<script type="text/javascript">
										<c:if test="${bank.maintainence}">
											maintainenceNBBank["${bank.bankName}"] = true;
										</c:if>
										<c:if test="${bank.lowPercentage}">
											lowPerfNBBank["${bank.bankName}"] = true;
										</c:if>
									</script>
								</c:forEach>
							</select>
						</div>
					</div>
				</c:if>
				
				<div id="warningDiv" class="warning">
					<img class="fl" src="images/web/alert.png">
					<span id="errorMsg"></span>
				</div>
				<div class="padd-left">
					<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BANK']}">
						<span class="error" style="style="display: block;" id="nbcardId">${requestScope.validationErrors['INVALID_BANK']}</span>
					</c:if>
					<div class="ml137">
						<input type="submit" class="submit" title="Pay Now" value="Pay Now" id="nbSubmit"/>
						<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
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
			var obj = $('.netbanking-panel #' + bankName);
			if(obj.length > 0) {
				displayName = obj.attr('title');
			} else {
				displayName = $('#bankCodeSelect option:selected').text();
			}
			$('#bankCode').val(bankName);
			$('.netbanking-panel li#' + bankName).addClass('chek');
			
			if(maintainenceNBBank[bankName]) {
				$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#nbSubmit').attr("disabled", "disabled");
				$('#warningDiv').show();
				$('#nbSubmit').removeClass('submit');
				$('#nbSubmit').addClass('disableSubmit');
			} else if(lowPerfNBBank[bankName]) {
				$('#warningDiv').show();
				$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
		} else {
			$('#nbSubmit').removeClass('submit');
			$('#nbSubmit').addClass('disableSubmit');
			$('#nbSubmit').attr("disabled", "disabled");
		}
	});
</script>
