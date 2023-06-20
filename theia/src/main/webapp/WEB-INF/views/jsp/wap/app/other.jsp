<div id="other-container" <c:if test="${'4' == paymentType}">style="display:block"</c:if>>
	<form autocomplete="off" name="other-form" method="post" action="payment/request/submit">
		<input type="hidden" id="bankCashId" name="bankCashId" />
		<input type="hidden" name="channelId" value="WEB" />
		<div class="query">
			<div class="padd">
				<ul class="unchk_btn padd1">
					<li>
						<span id="55">
							<img src="images/web/airtel_money.jpg" class="cardPad" alt="Airtel Money" title="Airtel Money"/>
						</span>
					</li>
					<li>
						<span id="54">
							<img src="images/web/itz_cash.jpg" class="cardPad" alt="itz Cash card" title="itz Cash card"/>
						</span>
					</li>
				</ul>
				<div class="clear"></div>
			</div>
			<div class="white">					
				<div>
					<input type="submit" class="submit" title="Pay Now" value="Pay Now" />
					<input type="submit" class="cancelButton" title="Cancel" value="Cancel" />
				</div>
				<div class="clear"></div>
			</div>
		</div>
	</form>
</div>