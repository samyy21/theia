<div data-role="collapsible" id="netbanking" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading" data-netbanking="${sessionScope.promoNbList }" id="nbContent">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>Net Banking</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<script type="text/javascript">
			maintainenceNBBank = new Object();
			lowPerfNBBank = new Object();
		</script>
		<%
		String reqBankCode = (request.getAttribute("reqBankCode") == null) ? "" : (String) request.getAttribute("reqBankCode");
		%>
		<h2 class="mt15" id="label-pop-banks">Select from popular banks</h2>
		<form autocomplete="off" name="netbanking-form" method="post" action="/payment/request/submit" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="NB" />
			<input type="hidden" name="channelId" value="WAP" />
		 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		 	<input type="hidden" name="bankCode" id="bankCode">
		 	<input type="hidden" name="walletAmount" id="walletAmountNB" value="0" />
		 	
			<c:if test="${!empty entityInfo.completeNbList}">
				<!-- <script type="text/javascript"> -->
					<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
						<c:if test="${entityInfo.completeNbList.size() > loopVar.index}">
								<c:set var="bank" value="${entityInfo.completeNbList[loopVar.index]}" />
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
						</c:if>
					</c:forEach>
				
				<ul id="pbanks" class="netbanking-panel ui-grid-c">
					<c:forEach begin="0" end="5" step="1" varStatus="loopVar">
						<c:if test="${entityInfo.completeNbList.size() > loopVar.index}">
							<c:set var="bank" value="${entityInfo.completeNbList[loopVar.index]}" />
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
				
				<c:if test="${entityInfo.completeNbList.size() > 6}">
					<script type="text/javascript">
						<c:forEach var="bank" items="${entityInfo.completeNbList}" begin="6">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
							
						</c:forEach>
					</script>
				</c:if>
				
				<div class="clear"></div>
				<c:if test="${entityInfo.completeNbList.size() > 6}">
					<h2 class="mt15" id="label-other-banks">Or select other bank</h3>
					<div class="ui-select"><div data-corners="true" data-shadow="true" data-iconshadow="true" data-wrapperels="span" data-icon="dwn-arw" data-iconpos="right" data-theme="c" class="ui-btn ui-shadow ui-btn-corner-all ui-btn-icon-right ui-btn-up-c"><span class="ui-btn-inner"><span class="ui-btn-text"><span>Select</span></span><span class="ui-icon ui-icon-dwn-arw ui-icon-shadow">&nbsp;</span></span>
						<select id="nbSelect" data-icon="dwn-arw">
							<option value="-1">Select</option>
							<c:forEach var="bank" items="${entityInfo.completeNbList}" begin="6">
								<option id="${bank.bankName}-option" value="${bank.bankName}" <c:if test="${reqBankCode eq bank.bankName}">selected=selected</c:if>>${bank.displayName}</option>
							</c:forEach>
						</select>
						</div>
					</div>
				</c:if>
				
				<div id="warningDiv" class="failure1 mt21" style="display: none">
					<span id="errorMsg"></span>
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
					$('#bankCode').val(bankName);
					if(!maintainenceNBBank[bankName]) {
						
						$("#proceedButton").parent().parent().removeClass("disable");
						$("#proceedButton").attr("disabled", false);
					}
					if(maintainenceNBBank[bankName]) {
						$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
						$('#warningDiv').show();
					} else if(lowPerfNBBank[bankName]) {
						$('#warningDiv').show();
						$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
					}
				} else {

				}
			});
		</script>
	</div>
</div>
