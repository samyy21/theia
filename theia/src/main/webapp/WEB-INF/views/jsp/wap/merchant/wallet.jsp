<c:if test="${'PPI' ne paymentTypeId}">
	<div class="border-bar">
		<div class="fl">
			<a href="jsp/wap/merchant/paymentForm.jsp?txn_Mode=PPI"> Wallet </a>
		</div>
		<div class="fr">
			<img src="images/wap/arrow.png" />
		</div>
		<div class="clear"></div>
	</div>
</c:if>
<c:if test="${'PPI' eq paymentTypeId}">
	<div class="blu-hd">
		<div class="fl">Wallet</div>
		<div class="fr">
			<img src="images/wap/dwn-arw.png" alt="" title="" />
		</div>
		<div class="clear"></div>
	</div>
	<div class="divider"></div>
	<div class="form-container">
		<form autocomplete="off" method="post" action="submitTransaction">
			<input type="hidden" name="txnMode" value="PPI" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="USRPWD" />
			
        	<p class="mt10">
            	User Id<br />
            	<input type="text" maxlength="45" autocomplete="off" name="walletUserId" value=""/>
            	<div class="error">${requestScope.validationErrors['INVALID_USER_ID']}</div>
        	</p>
        	
        	<p class="mt10">
            	Password<br />
            	<input autocomplete="off" type="password" name="password" maxlength="45" value=""/> <br /> 
          		<div class="error">${requestScope.validationErrors['INVALID_PASSOWRD']}</div>
        	</p>  
         
			<p class="mt10">
				<input name="Submit" type="submit" value="Pay Now" class="button" /> 
				<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
			</p>
		</form>
	</div>
</c:if>