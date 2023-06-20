<div data-role="collapsible" id="atmCard" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>ATM Card</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<script type="text/javascript">
			maintainenceATMBank = new Object();
			lowPerfATMBank = new Object();
		</script>
		<%
		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
		%>
		<h2 class="mt15">Select from popular banks</h2>
		<form autocomplete="off" name="atm-form" method="post" action="/payment/request/submit" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="ATM" />
			<input type="hidden" name="channelId" value="WAP" />
		 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		 	<input type="hidden" name="bankCode" id="atmBankCode">
		 	<input type="hidden" name="walletAmount" id="walletAmountNB" value="0" />
		 	
			<c:if test="${!empty entityInfo.completeATMList}">
				<!-- <script type="text/javascript"> -->
					<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
						<c:if test="${entityInfo.completeATMList.size() > loopVar.index}">
						
								<c:if test="${bank.maintainence}">
									maintainenceATMBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfATMBank["${bank.bankName}"] = true;
								</c:if>
						</c:if>
					</c:forEach>
				<!-- </script> -->
				<ul id="pbanks" class="atm-panel ui-grid-c">
					<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
						<c:if test="${entityInfo.completeATMList.size() > loopVar.index}">
							<c:set var="bank" value="${entityInfo.completeATMList[loopVar.index]}" />
							<li class="ui-block-a">
								<div id="${bank.bankName}" title="${bank.displayName}" class="bank">
									<span class="bank-logo" alt="${bank.displayName}" style="background : url(images/wap/bank/${bank.bankWapLogo}) no-repeat;"></span>
								</div>
							</li>
							<c:if test= "${loopVar.count !=0 && (loopVar.count mod 2) == 0}">
							<div class = "clear"></div>
							</c:if>
						</c:if>
					</c:forEach>
				</ul>
				<div class="clear"></div>
				<div id="atmWarningDiv" class="failure1 mt21" style="display: none">
					<span id="atmErrorMsg"></span>
				</div>
			</c:if>
			<c:if test="${walletInfo.walletInactive}">
	       		<span>
        		By proceeding you agree to our <a href="#terms" data-rel="dialog">Terms &amp; Conditions</a> 
        		and that you have read our <a href="#privacy" data-rel="dialog"> Privacy Policy.</a>
        		</span>
        	</c:if>
			<div class="load-btn">
	  	<button type="submit" class="submitButton" value="Proceed Securely" data-icon="ldr" data-iconpos="right">Proceed Securely</button>
	  </div>
		</form>
		
		<script>
			$(document).ready(function(){
				var bankName = "<%=reqBankCode%>";
				var displayName = "";
				if(bankName.trim().length > 0) {
					$('#atmBankCode').val(bankName);
					if(!maintainenceATMBank[bankName]) {
						
						$("#proceedButton").parent().parent().removeClass("disable");
						$("#proceedButton").attr("disabled", false);
					}
					if(maintainenceATMBank[bankName]) {
						$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
						$('#atmWarningDiv').show();
					} else if(lowPerfATMBank[bankName]) {
						$('#atmWarningDiv').show();
						$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
					}
				} else {

				}
			});
		</script>
	</div>
</div>