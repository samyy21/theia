<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<div class="modals-container">
	<div id="login-modal" class="md-modal md-effect-10" style="margin-top: -280px; margin-left: -220px;">
		<div class="md-content">
			<a class="closePop close-modal" href="#"></a>
			<div id="login-spinner">
				<img src="${ptm:stResPath()}images/spinner.gif">
				<br><br>
				<span class="medium grey-text">Please wait...</span>
			</div>
			<iframe id="login-iframe" src="">
			</iframe>
		</div>
	</div>
	<div id="delete-confirm-modal" class="md-modal md-effect-10">
		<div class="md-content">
			<div class="text medium">
				Are you sure you want to remove this card?
				<br><br>
				<span class="del-card-num">5566XXXXXXXXXX848484</span>
			</div>
			<div>
				<div class="btn-submit fl mt20 ml20 deleteCard">
	           		<input name="" type="submit" class="gry-btn btn-normal" value="Yes" id="">	          
	           	</div>
	           	<div class="btn-submit fl mt20 ml20 close-modal">
	           		<input name="" type="submit" class="gry-btn btn-normal" value="No" id="">	          
	           	</div>
	           	<div class="clear"></div>
	         </div>
		</div>
	</div>
	<div class="md-overlay"></div>
</div>