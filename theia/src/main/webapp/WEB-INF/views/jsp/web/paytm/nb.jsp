<div data-netbanking="${sessionScope.promoNbList }" class = 'tab-pane fade <c:if test="${3 == paymentType}">in active</c:if>' id = "nbContent">
	<script type="text/javascript">
		maintainenceNBBank = new Object();
		lowPerfNBBank = new Object();
	</script>
	<%
   		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
	%>
	<h3 id="label-pop-banks">Select from popular banks</h3>
	<form autocomplete="off" name="netbanking-form" method="post" action="submitTransaction" style="padding: 0px">
		<input type="hidden" name="txnMode" value="NB" />
		<input type="hidden" name="channelId" value="WEB" />
		<input type="hidden" name="txn_Mode" value="NB" />
	 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	 	<input type="hidden" name="bankCode" id="bankCode">
	 	<input type="hidden" name="walletAmount" id="walletAmountNB" value="0" />
	 	
		<c:if test="${!empty sessionScope.MerchBankList}">
			<ul class="netbanking-panel pt20 pbanks">
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
						
						<li>
							<div id="${bank.bankName}" title="${bank.displayName}" class="radio" style="background-position: center 0px;">
								<input type="radio" class="bankRadio pcb" value="${bank.bankName}" name="bank" ${bank.bankName eq txnInfo.selectedBank ? "checked='checked'" : ""}>
								<label  class="fl ml5">
									<span class="bank-logo" alt="${bank.displayName}" style="background : url(images/web/bank/${bank.bankWebLogo}) no-repeat;"></span>
								</label>
							</div>
						</li>
					</c:if>
				</c:forEach>
			</ul>
			<div class="clear"></div>
			<ul class="netbanking-panel mt20 pbanks">
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
						
						<li>
							<div id="${bank.bankName}" title="${bank.displayName}" class="radio" style="background-position: center 0px;">
								<input type="radio" value="${bank.bankName}" class="bankRadio pcb" name="bank">
								<label class="fl ml5">
									<span class="bank-logo" alt="${bank.displayName}" style="background : url(images/web/bank/${bank.bankWebLogo}) no-repeat;"></span> 
								</label>
							</div>
						</li>
					</c:if>
				</c:forEach>
			</ul>
			<div class="clear"></div>
			
			<c:if test="${sessionScope.MerchBankList.size() > 0}">
				
				<div id = "nbWrapper">
					<h3 class="mt20" id="label-other-banks">Or select other bank</h3>
					<select class="selectpicker" id = "nbSelect" data-size="5">
						<option value = "-1">Select</option>
						<c:forEach var="bank" items="${sessionScope.MerchBankList}" begin="0">
							<option value="${bank.bankName}" <c:if test="${bank.bankName eq txnInfo.selectedBank}">selected='selected'</c:if>>${bank.displayName}</option>
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
			</c:if>
			
			<div id="warningDiv" class="gry-box clear">
				<span id="errorMsg"></span>
			</div>
			
			<p class="clear">
	           	<input name="" type="submit" class="gry-btn" value="Proceed Securely" id="nbSubmit" disabled="disabled">
	           	<a href="/oltp-web/cancelTransaction" class="cancel">Cancel</a>
	        </p>
	        <div class="secure">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
	        </div>
		</c:if>
	</form>
</div>

<script type = 'text/javascript'>
	$(document).ready(function(){
		var bankName = "<%=reqBankCode%>";
		preselectedNBBank = bankName || null;
	});
</script>
