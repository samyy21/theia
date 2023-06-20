<div data-role="collapsible" id="debitCard" class="ui-collapsible ui-collapsible-inset ui-collapsible-collapsed">
	<h3 class="ui-collapsible-heading">
		<a href="#" class="ui-collapsible-heading-toggle ui-btn ui-btn-icon-right ui-btn-up-c">
			<span class="ui-btn-inner">
				<span class="ui-btn-text">
					<b>Debit Card</b>
				</span>
				<span class="ui-icon ui-icon-arrow-r ui-icon-shadow">&nbsp;</span>
			</span>
		</a>
	</h3>
	<div class="ui-collapsible-content ui-collapsible-content-collapsed">
		<form autocomplete="off" name="debitcard-form" method="post" action="payment/request/submit" id="card" data-ajax="false">
			<input type="hidden" name="txnMode" value="DC" />
			<input type="hidden" name="txn_Mode" value="DC" />
			<input type="hidden" name="channelId" value="WAP" />
			<input type="hidden" name="AUTH_MODE" value="3D" />
			<input type="hidden" name="CARD_TYPE" id="cardType" value="${txnInfo.cardType}" />
			<input type="hidden" name="walletAmount" id="walletAmountDC" value="0" />
			
			<p id="cd">
				<div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c">
	        		<input type="tel" id="cn1"  data-promocodes="${sessionScope.promoBinList}" maxlength="19" class="cardInput ui-input-text ui-body-c" name="cardNumber"/>
	        	</div>
	        	<c:if test="${promocodeType  eq 'DISCOUNT' && '2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_PROMO_DETAILS']}">
               		<c:set var="promoError" value="1"/> 
               </c:if>
	        	<div class="error clear promo-code-error" style="width:150px;<c:if test='${empty promoError}'>display:none</c:if>">
					Enter ${txnInfo.promoCodeResponse.promocodeData.cardBins } card to avail benefits 
				</div>
	            <c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
					<div id="dcCCnoId" class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CARD']}</div>
					<script type="text/javascript">
						$(document).ready(function(){
							$("#cn1").parent().css("border", "#b53b3b solid 1px");
						});
					</script>
				</c:if>
					
	        </p>
	        <label style="color: #666;">Expiry Date</label>        
			<ul class="ui-grid-b mt7 mb10">
	            <li class="ui-block-a mt7 expiry" id = "dcExpMonthWrapper">
	            	<div class="ui-select"><div data-corners="true" data-shadow="true" data-iconshadow="true" data-wrapperels="span" data-icon="dwn-arw" data-iconpos="right" data-theme="c" class="ui-btn ui-shadow ui-btn-corner-all ui-btn-icon-right ui-btn-up-c"><span class="ui-btn-inner"><span class="ui-btn-text"><span>MM</span></span><span class="ui-icon ui-icon-dwn-arw ui-icon-shadow">&nbsp;</span></span>
		                <select name="expiryMonth" id="dcMonth" data-icon="dwn-arw">
			                <option value="0">MM</option>
		                    <option value="01">01</option>
		                    <option value="02">02</option>
		                    <option value="03">03</option>
		                    <option value="04">04</option>
		                    <option value="05">05</option>
		                    <option value="06">06</option>
		                    <option value="07">07</option>
		                    <option value="08">08</option>
		                    <option value="09">09</option>
		                    <option value="10">10</option>
		                    <option value="11">11</option>
		                    <option value="12">12</option>
		              	</select>
		              </div>
		            <div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c ui-disabled">
	              		<input type = "text" disabled = "disabled" value = "xx"  class = "expText"/>
	              	</div>
	              	<c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<div id="dcexId" class="error clear" style="width: 150px;height: 20px;">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</div>
						<script type="text/javascript">
							$(document).ready(function(){
								$("#dcMonth").parent().css("border", "#b53b3b solid 1px");
								$("#dcYear").parent().css({"border":"#b53b3b solid 1px", "margin-bottom":"30px"});
							});
						</script>
					</c:if>
	          	</li>
		        
		        <li class="ui-block-b mt7 expiry" id = "dcExpYearWrapper">  
	            	<div class="ui-select"><div data-corners="true" data-shadow="true" data-iconshadow="true" data-wrapperels="span" data-icon="dwn-arw" data-iconpos="right" data-theme="c" class="ui-btn ui-shadow ui-btn-corner-all ui-btn-icon-right ui-btn-up-c"><span class="ui-btn-inner"><span class="ui-btn-text"><span>YY</span></span><span class="ui-icon ui-icon-dwn-arw ui-icon-shadow">&nbsp;</span></span>
		            	<select name="expiryYear" id="dcYear" data-icon="dwn-arw">
		            		<option value="0">YY</option>
		                    <option value="2014">2014</option>
		                    <option value="2015">2015</option>
		                    <option value="2016">2016</option>
		                   	<option value="2017">2017</option>
		                    <option value="2018">2018</option>
		                    <option value="2019">2019</option>
		                    <option value="2020">2020</option>
		                    <option value="2021">2021</option>
		                    <option value="2022">2022</option>
		                   	<option value="2023">2023</option>
		                   	<option value="2024">2024</option>
		                  	<option value="2025">2025</option>
		                    <option value="2026">2026</option>
		                    <option value="2027">2027</option>
		                    <option value="2028">2028</option>
		                    <option value="2029">2029</option>
		                    <option value="2030">2030</option>
		                    <option value="2031">2031</option>
		                    <option value="2032">2032</option>
		                    <option value="2033">2033</option>
		                    <option value="2034">2034</option>
		                    <option value="2035">2035</option>
		                    <option value="2036">2036</option>
		                    <option value="2037">2037</option>
		                    <option value="2038">2038</option>
		                    <option value="2039">2039</option>
		                    <option value="2040">2040</option>
		                    <option value="2041">2041</option>
		                    <option value="2042">2042</option>
		                    <option value="2043">2043</option>
		                    <option value="2044">2044</option>
		                    <option value="2045">2045</option>
		                    <option value="2046">2046</option>
		                    <option value="2047">2047</option>
		                    <option value="2048">2048</option>
		                    <option value="2049">2049</option>
		            	</select>
		            </div>
		            <div class="ui-input-text ui-shadow-inset ui-corner-all ui-btn-shadow ui-body-c ui-disabled">
	            		<input type = "text" disabled = "disabled" value = "xxxx" class = "expText"/>
	            	</div>
	            </li>

	            <li class="ui-block-c mt4">
	            	<div class="" id="dc-cvv-div">
	            		<input name="cvvNumber" type="password" id="dcCvvBox" placeholder="CVV" value="" maxlength="4"  autocomplete="off" class="dc-cvv-input">
	            	</div>
	                <c:if test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<div id="dcCvvId" class="error clear" style="width: 150px;">${requestScope.validationErrors['INVALID_CVV']}</div>
						<script type="text/javascript">
							$(document).ready(function(){
								$("#dcCvvBox").parent().css("border", "#b53b3b solid 1px");
							});
						</script>
					</c:if>
	                
	                <div id="showHideDiv" style="display:none;">
	                	<div class="cvv-icon"></div>
	                </div>
	            </li>
			</ul>
			 <div class="opt" id="maestroOpt" style = "display:none;">If your Maestro Card does not have Expiry Date/CVV, skip these fields</div>
			<c:if test="${saveCardOption}">
				 <div id = "dcStoreCardWrapper" style="margin-left: 10px; margin-bottom: 10px; height:25px;">
			        <div class="checkbox" id="dcSaveCardLabel">
			        	<div class="ui-checkbox">
			        		<input type="checkbox" value="Y" name = "storeCardFlag" checked="checked">
			        	</div>
			        	<span class="saveCardLabel">Save this card for faster checkout</span>
			        </div>
		        </div>
	        </c:if>
	        
	        <div class="load-btn">
  	<input type="submit" class="submitButton" value="Proceed Securely" data-icon="ldr" data-iconpos="right">
  </div>
  
		</form>
	</div>
</div>