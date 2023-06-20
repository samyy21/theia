<c:if test ="${loginInfo.loginFlag}">
	<div id="footer" class="">
		<div class="container">
			<div class="img img-partner-logo"></div>
				<div class="btn-submit wap-submit-btn hide">
		       		<input name="" type="button" class="gry-btn required" value="Pay now" id="wapSubmit">
		        </div>
		</div>
	</div>
</c:if>

<c:if test ="${!loginInfo.loginFlag && merchInfo.mid ne 'scwpay09224240900570'}">
	<div id="footer" class="web-display">
		<div class="container">
			<div class="img img-partner-logo"></div>
		</div>
	</div>
</c:if>

<c:if test ="${loginInfo.loginFlag}">
	<div id="footer-placeholder"></div>
</c:if>