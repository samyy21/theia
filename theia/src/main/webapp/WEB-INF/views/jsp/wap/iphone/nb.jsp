<%@ page import="java.util.List" %>
<script type="text/javascript">
	maintainenceNBBank = new Object();
	lowPerfNBBank = new Object();
</script>
<%String contextPathNb = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProjectProperty("context.path"); %>

<div data-role="content" data-theme="i" id="netbanking-container" <c:if test="${'3' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BANK']}">
				<div class="error">${requestScope.validationErrors['INVALID_BANK']}</div>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for
				Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<form autocomplete="off" method="post" action="<%=contextPathNb %>/payment/request/submit" data-ajax="false">
			<div data-role="fieldcontain" class="ui-field-contain ui-body ui-br">
       			<!-- <label for="selectBank" class="ui-input-text">Select your Bank</label>-->
<!--         		<a href="#" class="ui-link" id="nbBankSelect"> -->
<!--         			<div id="nbBankName" class="operator"> -->
<!--         				<span id="bankNameSpanNb">Select your Bank</span> -->
<!--          			 	<div class="arw fr"></div> -->
<!--           				<div class="clear"></div> -->
<!--         			</div> -->
<!--         		</a> -->
				<script type="text/javascript">
					<c:forEach var="bank" items="${entityInfo.completeNbList}">
						<c:if test="${bank.maintainence}">
							maintainenceNBBank["${bank.bankName}"] = true;
						</c:if>
						<c:if test="${bank.lowPercentage}">
							lowPerfNBBank["${bank.bankName}"] = true;
						</c:if>
					</c:forEach>
				</script>
				<select id="nbBankSelect">
						<option value="">Select</option>
						<c:forEach var="bank" items="${entityInfo.completeNbList}">
								<option id="${bank.bankName}" value = "${bank.bankName}">${bank.displayName}</option>
						</c:forEach>
				</select>
        	</div>
        	
        	<div id="warningDiv" class="alert">
        		<span id="errorMsg"></span>
        	</div>
        	
			<input type="hidden" name="txnMode" value="NB" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			<input type="hidden" name="bankCode" id="nbBankCode"/>
			
			<div data-role="footer" data-id="foo1" data-position="fixed" id="nbSubmit">
				<input	name="Submit" type="submit" value="Pay Now" class="button" />
			</div>
		</form>
	</section>
</div>

<!--  <div id="nbBankList" data-role="page" data-theme="i" style="display:none;">
  <div data-role="header" data-fullscreen="true" class="header">
  	<span class="ml5">Select Bank</span>
  	<a href="#" id="nbListBack" data-icon="custom-back-icon"></a>
  </div>
  <div data-role="content" data-theme="i" class="recharges">
    <section class="m-opt" id="nbListSection">
      <ul>
		<c:forEach var="bank" items="${entityInfo.completeNbList}">
			<li class="opr"> <a href="#" id="${bank.bankName}">${bank.displayName}</a></li>
			<script type="text/javascript">
				<c:if test="${bank.maintainence}">
					maintainenceNBBank["${bank.bankName}"] = true;
				</c:if>
				<c:if test="${bank.lowPercentage}">
					lowPerfNBBank["${bank.bankName}"] = true;
				</c:if>
			</script>
		</c:forEach>
      </ul>
    </section>
  </div>
</div> -->
