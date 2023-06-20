<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ include file="../../common/config.jsp" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" class="ui-mobile">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta name="robots" content="noindex,nofollow" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-status-bar-style" content="black" />
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
		<meta name="format-detection" content="telephone=no">
		
	<c:set var="cssPathComponent" value="${mid}" />
	<c:if test="${empty header['PRX']}">
		<c:set var="cssPathComponent" value="default" />
	</c:if>
		
		<script type="text/javascript">
		
			function backButtonOverride() {
			
				var ua = navigator.userAgent;
				var matches = ua.match(/^.*(iPhone|iPad).*(OS\s[0-9]).*(CriOS|Version)\/[.0-9]*\sMobile.*$/i);
				if (!(matches && matches[2] === 'OS 7' && matches[3] === 'CriOS')) {
					setTimeout("backButtonOverrideBody()", 1);
				}
			}
			
			function backButtonOverrideBody() {
				try {
					history.forward();
				}
				catch (e) {}
				setTimeout("backButtonOverrideBody()", 500);
			}
		</script>
		<link type="image/x-icon" rel="shortcut icon" href="images/web/Paytm.ico" />
		
		<%-- <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script> --%>
		
		<%String useMinifiedAssets = PropertiesUtil.getProjectProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="css/wap/paytmApp/mobile.css" />
			<script type="text/javascript" src="js/zepto-1.1.3.js"></script>
			<script type="text/javascript" src="js/wap/paytmApp/jquery.custom_radio_checkbox.js"></script>
			<script type="text/javascript" src="js/wap/paytmApp/functions.js"></script>
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="css/wap/paytmApp/mobile.min.css" />
			<script type="text/javascript" src="js/wap/paytmApp/functions.min.js"></script>
		<% } %>
		
	</head>
	
	<body onload="backButtonOverride();" class="ui-mobile-viewport ui-overlay-c">
	<c:if test="${txnInfo == null}">
		<c:redirect url="/error"/>
	</c:if>
		<div id="txnTransientId" value="${txnInfo.txnID}"></div>
		<div id="siteTheme" data-value="paytmApp" style="display:none;"></div>
		
		<div data-role="page" class="ui-page ui-body-c ui-page-active">
		<div data-role="page" id="header" data-showtabs="${sessionScope.promoPaymentModes}" data-promocodetype="${sessionScope.promocodeType}" class="ui-page ui-body-c ui-page-active">
			<!--Middle Container Start-->
			<section data-role="content" class="ui-content">
				<c:if test="${txnInfo.retry}">
					<div class="failure">
						<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
					</div>
				</c:if>
				<c:if test="${walletInfo.walletFailed}">
					<div class="failure">
						<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					</div>
				</c:if>
				
				<c:if test="${!walletInfo.walletFailed}">
					<div class="wht-box1 mt10">
						Post this order, you will have all new Paytm Cash Wallet.
						<%-- <c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" /> --%>
					</div>
				</c:if>
				<article id="summary-section" <c:if test="${!empty promoCode and promocodeType  eq 'CASHBACK'}">style='display:none'</c:if>>
					<ul class="ui-grid-a">
			      		<li class='ui-block-a <c:if test="${empty walletInfo.walletBalance || walletInfo.walletBalance == 0}"> txt700' style='color: #666666;</c:if>'>Total amount to be paid</li>
				      	<li class='ui-block-b <c:if test="${empty walletInfo.walletBalance || walletInfo.walletBalance == 0}"> txt700' style='color: #666666;</c:if>'>
				      		<span class = "WebRupee">Rs.</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
				      		<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
				      	</li>
				    	<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
					        <li class="ui-block-a">
					        	<div class="checkbox" id="pc">
						  			<input type="checkbox" value="pc" <c:if test="${requestScope.applyWallet }">checked="checked"</c:if>>
					          		<span>Use paytm cash</span>
					          	</div>
					          	<div class="clear"></div>
								<div class="bal" id = "remBal">(Remaining balance <span class="WebRupee">Rs.</span> <span class = 'amt'></span>)</div>
								<div class="bal" id = "yourBal" style = "display:none">(Your balance <span class="WebRupee">Rs.</span> <span class = 'amt'></span>)</div>
						        <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
					        </li>
					        <li class="ui-block-b" id = "walletBalance">
					        	<span class = "WebRupee">Rs.</span>
					        	<span id="walletBalanceSpan">
					        		<fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="0"/>
					        	</span>
					        </li>
							<li class="clear divider"></li> 
				        	<li class="ui-block-a txt700 right width80 pr0"><span class="dark-txt">Balance amount to be paid</span></li>
				        	<li class="ui-block-b txt700 width20"><b><span class="dark-txt"><span class = "WebRupee">Rs.</span> <span class="finalAmountSpan">${txnInfo.txnAmount}</span></span></b></li>
				    	</c:if>
				    </ul>
				    
				    <form autocomplete="off" name="wallet-form" method="post" action="/payment/request/submit" id="card" data-ajax="false">
				    	<input type="hidden"  name="txnMode" value="PPI" />
						<input type="hidden"  name="channelId" value="WAP" />
						<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
						<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
					    <div style="display: none;" class="fullWalletDeductDiv">
					    	<div class="load-btn" style="width: 90%; margin: 10px auto">
							  	<button type="submit" class="submitButton" value="Proceed Securely" data-icon="ldr" data-iconpos="right">Proceed Securely</button>
							 </div>
					    </div>
					</form>
				</article>
				<h1 class="mt15">
					<span class="otherText">Select an option to pay <span class = "WebRupee">Rs.</span> <span class="finalAmountSpan">${txnInfo.txnAmount}</span></span>
		      		<span class="fullWalletDeduct" style="display: none;">Uncheck Paytm Cash to pay using other options</span>
				</h1>
				<c:if test="${!empty sessionScope.promoCode}">
				<div id="promoType" data-value="${sessionScope.promocodeType}"></div>
				<div id="promoCardTypeList" data-value="${sessionScope.promoCardType}"></div>
				<div class="failure" >
					
					<div class="mt10 cashback-promo-code-error" style="padding: 5px;">
						<span>
							${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
						</span>
											
					<c:if test="${sessionScope.promocodeType eq 'CASHBACK'}">
						<a class="show-other-options" style="float:right;display:inline-flex;text-decoration:underline;cursor:pointer">Show other options to pay</a> 
					</c:if>
					</div>
					
					<div class="wht-box mt10 non-error-text" style="display:none;text-align:right;font-size:10px">
						If you pay using other option you will not get benefits of your promocode.
						<br>
						${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
					</div>
				</div>
				</c:if>
				<article class="mt21">
 					<div id="paymodes" data-role="collapsible-set" data-theme="c" data-collapsed-icon="arrow-r" data-expanded-icon="arrow-d" data-iconpos="right" class="ui-icon-alt ui-collapsible-set ui-corner-all">
 						<c:if test="${saveCardEnabled}">
							<%@ include file="savedCard.jsp" %>
						</c:if>
						<c:if test="${dcEnabled}">
							<%@ include file="dc.jsp" %>
						</c:if>
						<c:if test="${ccEnabled}">
							<%@ include file="cc.jsp" %>
						</c:if>
						<c:if test="${netBankingEnabled}">
							<%@ include file="nb.jsp" %>
						</c:if>
						<c:if test="${atmEnabled}">
							<%@ include file="atm.jsp" %>
						</c:if>
						<c:if test="${impsEnabled}">
							<%@ include file="imps.jsp" %>
						</c:if>
						<c:if test="${cashcardEnabled}">
							<%@ include file="itzcash.jsp" %>
						</c:if>
						<c:if test="${rewardsEnabled}">
							<%@ include file="rewards.jsp" %>
						</c:if>
					</div>
				</article>
			</section>
			
			        
       		<ul class="lock">
      			<li class="image fl">
      				<div class="lock-icon"></div>
      			</li>
       			<li class="fl small">Your card details are secured via 128 Bit encryption by Verisign</li>
   			</ul>
    			
			<div class="footer">
				<div id="partners-img"></div>
 			</div>
 			
 			<div id="prefered-payment-mode" data-value="${paymentType}"></div>
 			 
		</div>
		
	</body>
</html>