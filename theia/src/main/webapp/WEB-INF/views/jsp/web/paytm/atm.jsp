<div class = 'tab-pane fade <c:if test="${8 == paymentType}">in active</c:if>' id = "atmContent">
	<%@ page import="java.util.List" %>
	<script type="text/javascript">
		maintainenceATMBank = new Object();
		lowPerfATMBank = new Object();
	</script>
	<%
   		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
	<h3>Select from popular banks</h3>
	<form autocomplete="off" name="atm-form" method="post" action="submitTransaction" style="padding: 0px">
		<input type="hidden" name="txnMode" value="ATM" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="ATM" />
	 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	 	<input type="hidden" name="bankCode" id="atmBankCode">
	 	<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
		<c:if test="${!empty sessionScope.ATMCardList}">
			<ul class="atm-panel pt20 pbanks">
				<c:forEach begin="0" end="2" step="1" varStatus="loopVar">
					<c:if test="${sessionScope.ATMCardList.size() > loopVar.index}">
						<c:set var="bank" value="${sessionScope.ATMCardList[loopVar.index]}" />
						
						<script type="text/javascript">
							<c:if test="${bank.maintainence}">
								maintainenceATMBank["${bank.bankName}"] = true;
							</c:if>
							<c:if test="${bank.lowPercentage}">
								lowPerfATMBank["${bank.bankName}"] = true;
							</c:if>
						</script>
						
						<li>
							<div id="${bank.bankName}" title="${bank.displayName}" class="radio" style="background-position: center 0px;">
								<input type="radio" class="bankRadio pcb" value="${bank.bankName}" name="bank" ${bank.bankName eq txnInfo.selectedBank ? "checked='checked'" : ""}>
								<label class="fl ml5">
									<span class="bank-logo" alt="${bank.displayName}" style="background : url(images/web/bank/${bank.bankWebLogo}) no-repeat;"></span>
								</label>
							</div>
						</li>
					</c:if>
				</c:forEach>
			</ul>
			<div class="clear"></div>
			<ul  class="atm-panel mt20 pbanks">
				<c:forEach begin="3" end="5" step="1" varStatus="loopVar">
					<c:if test="${sessionScope.ATMCardList.size() > loopVar.index}">
						<c:set var="bank" value="${sessionScope.ATMCardList[loopVar.index]}" />
						
						<script type="text/javascript">
							<c:if test="${bank.maintainence}">
								maintainenceATMBank["${bank.bankName}"] = true;
							</c:if>
							<c:if test="${bank.lowPercentage}">
								lowPerfATMBank["${bank.bankName}"] = true;
							</c:if>
						</script>
						
						<li>
							<div id="${bank.bankName}" title="${bank.displayName}" class="radio" style="background-position: center 0px;">
								<input type="radio" value="${bank.bankName}" class="bankRadio pcb" name="bank" <c:if test="${bank.bankName eq txnInfo.selectedBank}">selected='selected'</c:if>>
								<label class="fl ml5">
									<span class="bank-logo" alt="${bank.displayName}" style="background : url(images/web/bank/${bank.bankWebLogo}) no-repeat;"></span>
								</label>
							</div>
						</li>
					</c:if>
				</c:forEach>
			</ul>
			<div class="clear"></div>
			
			
			<div id="atmWarningDiv" class="gry-box clear">
				<span id="atmErrorMsg"></span>
			</div>
			
			<p class="clear">
	           	<input name="" type="submit" class="gry-btn" value="Proceed Securely" id="atmSubmit" disabled="disabled">
	           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
	        </p>
	        <div class="secure">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
	        </div>
		</c:if>
	</form>
</div>

<script>
	$(document).ready(function(){
		var bankName = "<%=reqBankCode%>";
		preselectedATMBank = bankName || null;
	});
</script>
