<%-- <c:if test="${'3' ne paymentType}">
	<section class="nav-bar">
		<a href="/jsp/wap/iphone/paymentForm.jsp?txn_Mode=NB"> Net Banking </a>
	</section>
</c:if> --%>
<script type="text/javascript">
	maintainenceNBBank = new Object();
	lowPerfNBBank = new Object();
</script>
<c:if test="${'3' eq paymentType}">
	<div class="divider"></div>
	<div class="form-container">
		<form autocomplete="off" method="post" action="/payment/request/submit">
		<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BANK']}">
						<div class="error">${requestScope.validationErrors['INVALID_BANK']}</div>
						</c:if>
			<p class="mt15"><label>
				Select your Bank</label> <br /> <select name="bankCode" id="nbBankCodeId" style="width: 100%">
					<option value="-1">Select your Bank</option>
					<c:forEach var="bank" items="${entityInfo.completeNbList}">
						<option value="${bank.bankName}"  <c:if test="${reqBankCode eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
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
			</p>
			<span class="errorMsg" id="nbErrorMsg" style="display: block;"></span>
			<input type="hidden" name="txnMode" value="NB" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<div class="margin">
				 <input	name="Submit" type="submit" value="Pay Now" class="button" id="nbSubmit" />
			</div> 
			<div class="margin mb15">
				<input id="cancelButton" name="cancleBtn" onClick="cancelTxn();" type="button" class="cancel" value="Cancel" />
			</div>
		</form>
	</div>
</c:if>
<script>
	$(document).ready(function(){
		var bankName = $('#nbBankCodeId').val();
		var displayName = $('#nbBankCodeId option:selected').text();
		if(maintainenceNBBank[bankName]) {
			$('#nbErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#nbSubmit').attr("disabled", "disabled");
			$('#nbSubmit').removeClass('button');
			$('#nbSubmit').addClass('disableSubmit');
		} else if(lowPerfNBBank[bankName]) {
			$('#nbErrorMsg').text("Experiencing high failures on " + displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});
</script>