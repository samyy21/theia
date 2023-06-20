<c:if test="${'IMPS' ne paymentTypeId}">
	<div class="border-bar">
		<div class="fl">
			<a href="jsp/wap/paymentForm.jsp?txn_Mode=IMPS"> IMPS </a>
		</div>
		<div class="fr">
			<img src="images/wap/arrow.png" />
		</div>
		<div class="clear"></div>
	</div>
</c:if>
<c:if test="${'IMPS' eq paymentTypeId}">
	<div class="blu-hd">
		<div class="fl">IMPS</div>
		<div class="fr">
			<img src="images/wap/dwn-arw.png" alt="" title="" />
		</div>
		<div class="clear"></div>
	</div>
	<div class="divider"></div>
	<div class="form-container">
		<form autocomplete="off" method="post" action="submitTransaction">
			<input type="hidden" name="txn_Mode" value="IMPS" />
			<input type="hidden" name="txnMode" value="IMPS" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="OTP" />
			
			<p class="mt10">
            	MMID<br />
           		<input autocomplete="off" type="text" maxlength="7" name="mmid"/><br /> 
         		<a href="http://imps.npci.org.in/P2M_PB.asp" class="blue" target="_blank">How to get MMID</a>
         		<div class="error">${requestScope.validationErrors['INVALID_MMID']}</div>
        	</p>
        	<p class="mt10">
            	Mobile<br />
            	<input type="text" maxlength="10" autocomplete="off" name="mobileNo"/>
            	<div class="error">${requestScope.validationErrors['INVALID_MOBILE']}</div>
        	</p>
        	
        	<p class="mt10">
            	OTP<br />
            	<input autocomplete="off" type="text" name="otp" maxlength="6"/> <br /> 
          		<a href="http://imps.npci.org.in/P2M_PB.asp" class="blue" target="_blank">How to generate OTP?</a>
          		<div class="error">${requestScope.validationErrors['INVALID_OTP']}</div>
        	</p>  
         
			<p class="mt10">
				<input name="Submit" type="submit" value="Pay Now" class="button" /> 
				<a href="cancelTransaction" class="cancel">Cancel</a>
			</p>
		</form>
		<p class="sm-txt mt10">
			<strong>IMPS</strong>
			is Immediate Payment Service that enables you to make payment through your bank account via mobile. For list of participating Banks,
			<a class="blu" target="_blank" href="http://www.npci.org.in/impsmerpay10.aspx">click here</a>
		</p>
	</div>
</c:if>