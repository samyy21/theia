<%@ taglib prefix="ptm" uri="PaytmCustomTags"%>
<div class="card content ${3 eq paymentType ? 'active' : ''}" id="nb-card">
	<div class="card-header xsmall grey-text mb10 relative">
		<div class="fr">
			<div class="secure lt-grey-text">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span>
					<i>Your payment details are secured via
					<br/>
					128 Bit encryption by Verisign
					</i>
				</span>
	        </div>
		</div>
		<div class="clear"></div>
	</div>
	<script type="text/javascript">
		maintainenceNBBank = new Object();
		lowPerfNBBank = new Object();
	</script>
	<%
		String reqBankCode = (request.getAttribute("reqBankCode") == null)
				? ""
				: (String) request.getAttribute("reqBankCode");
	%>
	<form autocomplete="off" name="netbanking-form" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style="padding: 0px" class="validated">
		<input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
		<input type="hidden" name="txnMode" value="NB" />
		<input type="hidden" name="channelId" value="${channelInfo.channelID}" />
	 	<input type="hidden" name="AUTH_MODE" value="USRPWD" />
	 	<input type="hidden" name="bankCode" id="bankCode">
	 	<input type="hidden" name="walletAmount" id="walletAmountNB" value="0" />
	 	<c:if test="${txnConfig.addAndPayAllowed  && isAddMoneyAvailable}">
			<input type="hidden" name="addMoney" value="1" />
		</c:if>
		<c:set var="nbList" value="${entityInfo.completeNbList}" ></c:set>
		<c:if test="${existAddMoneyTab}">
			<c:set var="nbList" value="${entityInfo.addCompleteNbList}" ></c:set>
		</c:if>
		<c:if test="${!empty nbList}">
			<div id="popular-banks-wrapper">
				<label class="mb10" for="submit-btn">SELECT FROM POPULAR BANKS</label>
				<ul class="netbanking-panel pt20 pbanks grid banks-panel">
					<c:forEach begin="0" end="5" step="1" varStatus="loopVar" items="${nbList}">
						<c:if test="${nbList.size() > loopVar.index}">
							<c:set var="bank" value="${nbList[loopVar.index]}" />
							
							<script type="text/javascript">
								<c:if test="${bank.maintainence}">
									maintainenceNBBank["${bank.bankName}"] = true;
								</c:if>
								<c:if test="${bank.lowPercentage}">
									lowPerfNBBank["${bank.bankName}"] = true;
								</c:if>
							</script>
							
							<li>
								<div id="${bank.bankName}" title="${bank.bankName}" class="radio" style="background-position: center 0px;">
									<input type="radio" class="bankRadio pcb checkbox fl" value="${bank.bankName}"  data-bankid="${bank.bankId}" name="bank" >
									<label class="fl">
										<span class="bank-logo" alt="${bank.bankName}" style="background : url(${ptm:stResPath()}images/web/bank/${bank.bankWebLogo}) no-repeat;"></span>
									</label>
								</div>
							</li>
						</c:if>
					</c:forEach>
				</ul>
				<div class="clear"></div>
			</div>
		
			<c:if test="${nbList.size() > 0}">
			<div id="other-banks-wrapper">
				<label class="mt20 mb10" for="submit-btn">
					OR SELECT OTHER BANK
				</label>
				<div id = "nbWrapper">
					<select class="nbSelect" id = "nbSelect" data-size="5">
						<option value = "-1">Select</option>
						<c:forEach var="bank" items="${nbList}" begin="0">
							<option value="${bank.bankName}" ${bank.bankName eq txnInfo.selectedBank ? "selected='selected'" : ""} data-bankid="${bank.bankId }">${bank.displayName}</option>
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
			
			<div id="warningDiv" class="hide clear">
				<div id="errorMsg" class="mt10"></div>
			</div>
			
			<div class="mt20">
				<div class="btn-submit ${submitBtnClass} fl">
	           		<input name="submit-btn" type="submit" class="blue-btn required btn-normal" value="Pay now" id="nbSubmit">
	           	</div>
	           	<a href="/theia/cancelTransaction?${queryStringForSession}" class="cancel">Cancel</a>
	           	<div class="clear"></div>
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
