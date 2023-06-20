
<div id="footer" class="mt50">
	<div class="container">
		<c:if test="${themeInfo.subTheme eq 'torrent'}">
			<div class="fl small"><span class="fl">Powered By </span> <img src="${ptm:stResPath()}images/web/merchant5/paytm-50x17.png" style="margin-left:3px; margin-top: -4px;" class="fl"/> </div>
			<div class="img img-partner-logo fr"></div>
		</c:if>
		<c:if test="${themeInfo.subTheme ne 'torrent'}">
			<div class="img img-partner-logo"></div>
		</c:if>

	</div>
</div>