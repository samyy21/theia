<div class = 'card content ${8 eq paymentType ? "active" : ""}' id = "atm-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your payment details are secured via<br/>128 Bit encryption by Verisign</i></span>
	        </div>
		</div>
		<div class="clear"></div>
	</div>
	<%@ page import="java.util.List" %>
	<script type="text/javascript">
		maintainenceATMBank = new Object();
		lowPerfATMBank = new Object();
	</script>
    <%
        String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
    %>

	<form autocomplete="off" name="atm-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style="padding: 0px" class="validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="ATM" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
	 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	 	<input type="hidden" name="bankCode" id="atmBankCode">
	 	<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
	 	<c:if test="${txnConfig.addMoneyFlag && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		<c:set var="atmList" value="${entityInfo.completeATMList}" ></c:set>
		<c:if test="${existAddMoneyTab}">
			<c:set var="atmList" value="${entityInfo.addCompleteATMList}" ></c:set>
		</c:if>
		<c:if test="${!empty atmList}">
			<label class="" for="submit-btn">SELECT FROM POPULAR BANKS</label>
			<ul class="atm-panel pt20 pbanks grid banks-panel">
				<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
					 <c:if test="${atmList.size() > loopVar.index}"> 
						<c:set var="bank" value="${atmList[loopVar.index]}" />
						
						<script type="text/javascript">
							<c:if test="${bank.maintainence}">
								maintainenceATMBank["${bank.bankName}"] = true;
							</c:if>
							<c:if test="${bank.lowPercentage}">
								lowPerfATMBank["${bank.bankName}"] = true;
							</c:if>
						</script>

						<li>
							<div id="${bank.bankName}" title="${bank.bankName}" class="radio" style="background-position: center 0px;">
								<input type="radio" class="bankRadio pcb checkbox fl" value="${bank.bankName}" name="bank" ${bank.bankName eq txnInfo.selectedBank ? "checked='checked'" : ""} >
								<label class="fl">
									<span class="bank-logo" alt="${bank.bankName}" style="background : url(${ptm:stResPath()}images/web/bank/${bank.bankWebLogo}) no-repeat;"/>
								</label>
							</div>
						</li>
					</c:if>
				</c:forEach>
			</ul>
			<div class="clear"></div>
			
			
			<div id="atmWarningDiv" class="hide clear">
				<div id="atmErrorMsg" class="mt10"></div>
			</div>
			
			<div class="mt10">
				<div class="btn-submit ${submitBtnClass} fl">
		           	<input name="submit-btn" type="submit" class="blue-btn required btn-normal" value="Pay now" id="atmSubmit">
				</div>

				<div class="clear"></div>
	        </div>

		</c:if>
	</form>
</div>

<script>
	$(document).ready(function(){
		var bankName = "<%=reqBankCode%>";
		var displayName = "";
		preselectedATMBank = bankName || null;
	});
</script>
