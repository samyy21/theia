<div data-role="content" data-theme="i" id="itz-container" <c:if test="${'10' ne paymentType}">style="display:none"</c:if>>
	<div class="errors">
    	<ul id="errmsg">
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_ITZCARD']}">
				<li>${requestScope.validationErrors['INVALID_ITZCARD']}</li>
			</c:if>
			
			<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PWD']}">
				<li>${requestScope.validationErrors['INVALID_PWD']}</li>
			</c:if>
    	</ul>
	</div>
	
	<p class="pad">Order ready to be processed for Rs.${txnInfo.txnAmount}. Please complete the following details.</p>
				
	<section>
		<%String contextPathITZ = com.paytm.pgplus.theia.utils.ConfigurationUtil.getProjectProperty("context.path"); %>
		<form autocomplete="off" name="ProcessPaymentRequest" method="post" action="<%=contextPathITZ %>/payment/request/submit" data-ajax="false">
	      
	      <div data-role="fieldcontain">
	        <label for="itzCashNumber" class="ui-input-text">Itz account no.</label>
	        <input type="text" name="itzCashNumber" maxlength="12" id="itzCashNumber"/>
	      </div>
	      
	      <div data-role="fieldcontain"> 
	        <label for="itzPwd" class="ui-input-text">Password</label>
	        <input type="password" name="itzPwd" maxlength="20" id="itzPwd"/>
	      </div>
	      
	      <div class="clear"></div>
	      
			<input type="hidden" name="txnMode" value="CASHCARD" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
		  <div class="clear" style="margin-bottom: 50px"></div>
		  
		  <div data-role="footer" data-id="foo1" data-position="fixed">
		  	<input	name="Submit" type="submit" value="Pay Now" class="button" />
		  </div>
      </form>
    </section>
</div>

<div data-role="popup" id="positionWindow" class="txt14">
	<p><span class="bold">What is MMID?</span><br />
   	MMID (mobile money identifier) is 7-digit number issued by bank to customer for IMPS transaction.</p>
    <p><span class="bold"><a href="http://imps.npci.org.in/P2M_PB.asp" target="_blank">How to get MMID?</a></span></p>
</div>
 
<div data-role="popup" id="positionWindow1" class="txt14">
     <p><span class="bold">What is OTP?</span><br />
     OTP is one-time password issued by bank to customer for payment transaction through IMPS.</p>				
     <p><span class="bold"><a href="http://imps.npci.org.in/P2M_PB.asp" target="_blank">How to generate OTP?</a></span></p>
</div>