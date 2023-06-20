<div class = 'tab-pane fade <c:if test="${12 == paymentType}">in active</c:if>' id = "codContent">
	<div class="fl">
		<form autocomplete="off" name="cod-form" method="post" action="submitTransaction" id="card">
			<input type="hidden" name="txnMode" value="COD" />
			<input type="hidden" name="channelId" value="WEB" />
			<input type="hidden" name="txn_Mode" value="COD" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="walletAmount" id="walletAmountATM" value="0" />
			
			<ul class="pbanks">
				<li>
					<div id="" title="COD" class="radio has-pretty-child" style="background-position: center 0px;">
						<div class="clearfix prettyradio labelright fl1 blue "><input type="radio" class="bankRadio pcb" value="ITZ" name="bank" style="display: none;" checked="checked">
							<label for="undefined"></label></div>
								<label class="fl ml5">
									<span class="bank-logo" alt="ITZ" style="">CASH ON DELIVERY</span>
						</label>
					</div>
				</li>
			</ul>
			<div class="clear"></div>
						
			<p class="clear">
	           	<input name="" type="submit" class="blue-btn" value="Proceed Securely" id="codSubmit">
	           	<a href="/theia/cancelTransaction" class="cancel">Cancel</a>
	        </p>
	        <div class="secure">
	        	<div class="img img-lock fl" alt="Secure" title="Secure"></div>
	        	<span><i>Your card details are secured via 128 Bit encryption<br/>by Verisign</i></span>
	        </div>
		</form>
	</div>
    <div class="clear"></div>
</div>