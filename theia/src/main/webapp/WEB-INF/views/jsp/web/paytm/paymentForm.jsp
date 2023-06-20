<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ include file="../../common/config.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta name="robots" content="noindex,nofollow" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		<meta http-equiv="x-ua-compatible" content="IE=9" />
		
	<c:set var="cssPathComponent" value="${mid}" />
	<c:if test="${empty header['PRX']}">
		<c:set var="cssPathComponent" value="default" />
	</c:if>
		<link type="image/x-icon" rel="shortcut icon" href="images/web/Paytm.ico" />
		
		<script type="text/javascript">
			function backButtonOverride() {
				setTimeout("backButtonOverrideBody()", 1);
			}
			
			function backButtonOverrideBody() {
				try {
					history.forward();
				}
				catch (e) {}
				setTimeout("backButtonOverrideBody()", 500);
			}
			
			history.forward(0);
			
			
		</script>
		
		<%String useMinifiedAssets = PropertiesUtil.getProjectProperty("context.useMinifiedAssets"); %>
		
		<% if(useMinifiedAssets.equals("N")){ %>
			<link rel="stylesheet" type="text/css" href="css/bootstrap.css"/>
			<link rel="stylesheet" type="text/css" href="css/web/paytm/layout.css" />
			
			<script type="text/javascript" src="js/jquery-1.10.1.min.js"></script>
			<script type="text/javascript" src="js/bootstrap-tab.js"></script>
			<script type="text/javascript" src="js/bootstrap-dropdown.js"></script>
			<script type="text/javascript" src="js/bootstrap-select.js"></script>
			<script type="text/javascript" src="js/jquery.cluetip.js"></script>
			<script type="text/javascript" src="js/prettyCheckable.js"></script>
			<script type="text/javascript" src="js/web/paytm/functions.js"></script>
		<% } else { %>
			<link rel="stylesheet" type="text/css" href="css/web/paytm/layout.min.css" />
			<script type="text/javascript" src="js/web/paytm/functions.min.js"></script>
		<% } %>
		
		
	</head>
	<body onload="backButtonOverride();">
	<c:if test="${txnInfo == null}">
		<c:redirect url="/error"/>
	</c:if>
		<div id="txnTransientId" data-value="${sessionScope.txnTransientId}"></div>
		<!--Header Start-->
		<div id="header" data-promocodetype="${sessionScope.promocodeType}">
	  		<ul>
		    	<li>
		    		<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
		    	</li>
		    	<!-- 
		    	<div class="user-view pull-right">
		    		<c:if test ="${empty loginInfo.user}">
						<div class="myaccount pull-left">
							<div class="login">
								<a id="login-btn" href="#">Login with Paytm</a>
								<a id="register-btn" href="#">Signup</a>
							</div>
						</div>
					</c:if>
					
					<c:if test ="${!empty loginInfo.user}">
						<div class="myaccount pull-left">
							<div class="logout">
								<div class="pull-left user-name"><c:out value="${loginInfo.user.userName}" escapeXml="true" /></div>
								<div class="facebook-image pull-right">
									<div class="user-icon"></div>
								</div>
								<div class="clear"></div>
							</div>
						</div>
					</c:if>
				</div> -->
		  	</ul>
		  	
		  	<div class="clear"></div>
		</div>
		<!--Header End-->
		<!--Middle Container Start-->
		<div class="gry-cont">
			<div class="h1-border">
				<div class="outer">
					<c:choose>
					<c:when test="${txnInfo.retry}">
						<h1 style="color :red">
							<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
						</h1>
						</c:when>
						<c:otherwise>
							<h1 class="otherText">Great, let's get the payment done!</h1>
						</c:otherwise>
					</c:choose>
					<h1 class="fullWalletDeduct" style = "display:none">Just confirm to pay through Paytm Wallet</h1>
					<div class="clear"></div>
				</div>
				<div class="clear"></div>
			</div>
			
			<div class="outer">
				
				<div class="clear"></div>
				
				<c:if test="${sessionScope.walletFailed}">
					<div class="wht-box1 mt10">
						<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					</div>
				</c:if>
				
				<c:if test="${walletInfo.walletInactive}">
					<div class="wht-box1 mt10">
						<c:out value="${walletInfo.walletFailedMsg}" escapeXml="true" />
					</div>
				</c:if>
				
				<div class="wht-box" id="showHideWallet" <c:if test="${!empty promoCode and promocodeType  eq 'CASHBACK'}">style='display:none'</c:if>>
					<ul class="grid" style='<c:if test="${empty walletInfo.walletBalance || walletInfo.walletBalance == 0}">padding-bottom:35px;</c:if>'>
				      <li class="width600">Total payment to be made to Paytm.com</li>
				      <li class="width300 right" style='<c:if test="${empty walletInfo.walletBalance || walletInfo.walletBalance == 0}">font-weight:bold;</c:if>'>
				      	<span class="WebRupee">&#x20B9;</span> <span id="totalAmountSpan">${txnInfo.txnAmount}</span>
				      	<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
				      </li>
				      <li class="clear"></li>
				    </ul>
				    <div id="paytmcashbox">
				    <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
					    <div class="border width350 fr">
							<ul>
						        <li class="fl">
									<div id="pc" class="fl">
								  		<input type="checkbox" class = "pcb" value="pc" style = "display:none" <c:if test="${requestScope.applyWallet }">checked="checked"</c:if>/>
									</div>
						          	<label for="pc" class="txt13 fl mt6 ml10">Use paytm Wallet</label>
						          	<div class="clear"></div>
									<div class="txt12 bal" id = "remBal">(Your current balance is <span class="WebRupee">&#x20B9;</span> <span class = 'amt'>${walletInfo.walletBalance}</span>)</div>
									<!-- <div class="txt12 bal" id = "yourBal" style = "display:none">(Your current balance is <span class="WebRupee">&#x20B9;</span> <span class = 'amt'></span>)</div> -->
						        	<input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
						        </li>
						        <li class="fr pr30 txt14" id = "walletBalance">
						        	- <span class="WebRupee">&#x20B9;</span>
						        	<span id="walletBalanceSpan">
						        	</span>
						        </li>
					      	</ul>
						</div>
						<div class="clear"></div>
						<div class="width300 fr mt15">
				      		<ul>
					        	<li class="fl font400 font14"><b>Balance amount to be paid</b></li>
					        	<li class="fr pr30 font14"><b><span class="WebRupee">&#x20B9;</span> <span class="finalAmountSpan">${txnInfo.txnAmount}</span></b></li>
					      	</ul>
					    </div>
					    <div class="clear"></div>
					    <form autocomplete="off" name="creditcard-form" method="post" action="submitTransaction" id="walletForm" style = "margin:0;padding:0">
					    	<input type="hidden"  name="txnMode" value="PPI" />
							<input type="hidden"  name="txn_Mode" value="PPI" />
							<input type="hidden"  name="channelId" value="WEB" />
							<input type="hidden"  name="AUTH_MODE" value="USRPWD" />
							<input type="hidden" name="walletAmount" id="walletAmount" value="0" />
						    <div style="display: none;" class="fullWalletDeduct">
						    	<p class="clear fr mr30">
									<input class="blue-btn" type="submit" value="Confirm" name="">
								</p>
						    </div>
					    </form>
					    <div class="clear"></div>
				    </c:if>
				    </div>
				</div>
				<c:if test="${!empty sessionScope.promoCode}">
					<div id="promoType" data-value="${sessionScope.promocodeType}"></div>
					<div id="promoCardTypeList" data-value="${sessionScope.promoCardType}"></div>
					<div class="wht-box1 mt10 cashback-promo-code-error">
						<p style="width:70%;display:inline-block">
							${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
						</p>
						
											
					<c:if test="${sessionScope.promocodeType eq 'CASHBACK'}">
						<a class="show-other-options" style="float:right;display:inline-flex;text-decoration:underline;cursor:pointer">Show other options to pay</a> 
					</c:if>
					</div>
				</c:if>
				<div class="wht-box mt10 non-error-text" style="display:none;text-align:right;font-size:10px">
					If you pay using other option you will not get benefits of your promocode.
					<br>
					${txnInfo.promoCodeResponse.promoCodeDetail.promoMsg}
				</div>
				
				<c:if test="${0 eq onlyWalletEnabled}">	
				<div id='paymodes'>
				<div class="tabbable tabs-left mt15"> 	
				    <div class="wht-row">
			      		<h2 class="fl mt15">
			      			<span class="otherText" id="balanceTxt">
			      				<span id="Notbalance" >Select an option to pay</span>
			      				<span id="balanceAval" style="display: none;">Select an option to pay balance</span>
			      				<b>
			      					<span class="WebRupee">&#x20B9;</span>
			      					<span class="finalAmountSpan">${txnInfo.txnAmount}</span>
			      				</b>
			      			</span>
			      			<span class="fullWalletDeduct" style="display: none;">Uncheck Paytm Wallet to pay using other options</span>
			      		</h2>
				      	<div class="fr">
				      		<div class="img img-partner-logo"></div>
				      	</div>
				      	<div class="clear"></div>
   					</div>
   					
   						<c:if test="${dcEnabled ne 1 and ccEnabled ne 1}">
   							<c:set var="saveCardEnabled" value="0"></c:set>
   						</c:if>
   						
  						<%@ include file="tab.jsp" %>
  						
  						<div class="tab-content">
  							<c:if test="${1 eq saveCardEnabled}">
							<%@ include file="savedCard.jsp" %>
						</c:if>
  							<c:if test="${1 eq dcEnabled}">
							<%@ include file="dc.jsp" %>
						</c:if>
						<c:if test="${1 eq ccEnabled}">
							<%@ include file="cc.jsp" %>
						</c:if>
						<c:if test="${1 eq netBankingEnabled}">
							<%@ include file="nb.jsp" %>
						</c:if>
						<c:if test="${1 eq atmEnabled}">
							<%@ include file="atm.jsp" %>
						</c:if>
						<c:if test="${1 eq impsEnabled}">
							<%@ include file="imps.jsp" %>
						</c:if>

						<c:if test="${1 eq rewardsEnabled}">
							<%@ include file="rewards.jsp" %>
						</c:if>
						<c:if test="${1 eq cashcardEnabled}">
							<%@ include file="itzcash.jsp" %>
						</c:if>
						<c:if test="${1 eq codEnabled}">
							<%@ include file="cod.jsp" %>
						</c:if>
					</div>
					<div class="clear"></div>
				</div>
				</div>
				</c:if>
			</div>
				</div>
			
		<div id="footer">
			<div class="inner">
			<a href="http://www.paytm.com/about.html" target = "_blank">About us</a> | <a href="http://www.paytm.com/terms.html" target = "_blank">Terms &amp; Conditions</a> | <a href="http://www.paytm.com/faqs" target = "_blank">FAQs</a>
			</div>
		</div>
		
		<!-- <div id="login-modal" class="md-modal md-effect-10" style="margin-top: -225px; margin-left: -400px;">
			<div class="md-content">
				<a class="closePop" href="#"></a>
				<iframe id="login-iframe" src="">
				</iframe>
			</div>
		</div> -->
		
		<div class="md-overlay"></div>
	</body>
</html>