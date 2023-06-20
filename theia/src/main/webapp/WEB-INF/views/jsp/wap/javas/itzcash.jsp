<c:if test="${10 ne paymentType}">
	<div class="border-bar">
		<div class="fl">
			<a href="jsp/wap/javas/paymentForm.jsp?txn_Mode=CASHCARD"> ITZ CASH </a>
		</div>
		<div class="fr">
			<img src="images/wap/arrow.png" />
		</div>
		<div class="clear"></div>
	</div>
</c:if>
<c:if test="${10 eq paymentType}">
	<div class="blu-hd">
		<div class="fl">ITZ CASH</div>
		<div class="fr">
			<img src="images/wap/dwn-arw.png" alt="" title="" />
		</div>
		<div class="clear"></div>
	</div>
	<div class="divider"></div>
	<div class="form-container">
		<form autocomplete="off" method="post" action="submitTransaction">
					
			<input type="hidden" name="txnMode" value="CASHCARD" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			
			<p class="mt10">
            	Itz account no.<br />
            	<input type="text" name="itzCashNumber" maxlength="12" id="itzCashNumber"/>
         		<div class="error">${requestScope.validationErrors['INVALID_ITZCARD']}</div>
        	</p>
        	<p class="mt10">
            	Password<br />
            	<input type="password" name="itzPwd" maxlength="20" id="itzPwd" />
            	<div class="error">${requestScope.validationErrors['INVALID_PWD']}</div>
        	</p>
        	
        
			<p class="mt10">
				<input name="Submit" type="submit" value="Pay Now" class="button" /> 
				<a href="cancelTransaction" class="cancel">Cancel</a>
			</p>
		</form>
	</div>
</c:if>