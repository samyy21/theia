<%@ include file="../common/config.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Paytm Secure Online Payment Gateway</title>
		<meta name="robots" content="noindex,nofollow" />
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		
		
	<c:set var="cssPathComponent" value="${merchInfo.mid}" />
	<c:if test="${empty header['PRX']}">
		<c:set var="cssPathComponent" value="default" />
	</c:if>
		<link rel="stylesheet" type="text/css" href="css/web/layout.css" />
		<link rel="stylesheet" type="text/css" href="css/web/${cssPathComponent}/theme.css" />
		<link type="image/x-icon" rel="shortcut icon" href="images/web/Paytm.ico" />
		<script type="text/javascript" src="js/jquery.js"></script>
		<script type="text/javascript" src="js/web/functions.js"></script>
		<script type="text/javascript" src="js/paytmLogin.js"></script>
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
	</head>
	<body onload="backButtonOverride();">

	<c:if test="${txnInfo == null}">
		<c:redirect url="/error"/>
	</c:if>
		<!--Header Start-->
		<div id="header-full">
			<div id="header">
				<c:if test="${!empty merchInfo.merchantImage}">
					<div id="merchand-logo" class="fl">
						<img src="images/web/merchant/${merchInfo.merchantImage}"/>
					</div>
				</c:if>
				<div id="logo" class="fr">
					<a href="http://www.paytmpayments.com" target="_blank">
						<img src="images/web/paytm-payment.png" alt="" />
					</a>
				</div>
			</div>
		</div>
		<!--Header End-->
		<!--Middle Container Start-->
		<div id="container">
			<div class="gray_container">
				<%--<c:if test="${txnInfo.retry}">--%>
					<c:if test="${txnInfo.orderId}">
						<c:out value="${txnInfo.orderId}"/>
					<div class="white_container width-pad pb6">
						<div class="pad15">
							<div>
								<h3>
									<div class="fl">
										<img title="" alt="" src="images/web/alert.png"/>
									</div>
									<div class="fl err">
										<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
									</div>
									<div class="clear"></div>
								</h3>
								<div class="clear"></div>
							</div>
							<div class="clear"></div>
						</div>
					</div>
					<div class="dshadow"></div>
				</c:if>
				
				<div class="white_container width-pad">
					<div class="pad15">
						<h3 class="fl">
							We are ready to process your order for <span class="blu">Rs. ${txnInfo.txnAmount}.</span> Please complete payment details below
						</h3>
						<a id="displayText" class="hide-up" href="#billing-details">
							show details +
						</a>
						<div class="clear"></div>
					</div>
					<div id="cart-details" >
						<dl class="margin" style="font:bold 13px 'Open Sans', Arial, sans-serif; padding:0; width:900px;background-color:#f9f9f9;">
							<div class="divider"></div>
							<dt class="width-480" style="padding:10px 0 10px 15px;">
								Transactions ID: 
								<span class="blu">
									<c:out value="${txnInfo.orderId}" escapeXml="true" />
								</span>
								<br>
								Amount: 
								<span class="blu">
									Rs.<b><c:out value="${txnInfo.txnAmount}" escapeXml="true" /></b>
								</span>
							</dt>
							<dd class="width-390">
								<c:set var="customerDetails">
									<c:if test="${!empty txnInfo.custID}">
										${txnInfo.custID}<br/>
									</c:if>
									
									<c:if test="${!empty txnInfo.address1}">
										${txnInfo.address1}
									</c:if>
									<c:if test="${!empty txnInfo.address2}">
										, ${txnInfo.address2}<br />
									</c:if>
									<c:if test="${!empty txnInfo.city}">
										${txnInfo.city}
									</c:if>
									<c:if test="${!empty txnInfo.pincode}">
										, ${txnInfo.pincode}<br />
									</c:if>
									<c:if test="${!empty txnInfo.mobileno}">
										${txnInfo.mobileno}
									</c:if>
									<c:if test="${!empty txnInfo.emailId}">
										, ${txnInfo.emailId}<br />
									</c:if>
								</c:set>
								<c:if test="${!empty customerDetails}">
									<span class="txt">Customer details:</span>
									${customerDetails}
								</c:if>
							</dd>
							<div class="clear"></div>
							<div class="divider"></div>
						</dl>
						
						<c:if test="${!empty sessionScope.shoppingCart}">
							<dl class="outer-tab" style="width:900px;">
								<table id="paytable">
									<tbody>
										<tr>
											<th width="20%"></th>
											<th class="odd">Product</th>
											<th class="odd">Amount (<span class="WebRupee">Rs</span>)</th>
										</tr>
											<c:forEach items="${sessionScope.shoppingCart}" var="item">
												<tr>
													<td width="20%"> </td>
													<td>
														<c:out value="${item.key}" escapeXml="true" />
													</td>
													<td>
														<c:out value="${item.value}" escapeXml="true" />
													</td>
												</tr>
										</c:forEach>
									</tbody>
								</table>
							</dl>
						</c:if>
					</div>
				</div>
				<div class="dshadow"></div>
				<%@ include file="tab.jsp" %>
				<c:if test="${true eq cardInfo.saveCardEnabled}">
					<%@ include file="savedCard.jsp" %>
				</c:if>
				<c:if test="${true eq txnConfig.ccEnabled}">
					<%@ include file="cc.jsp" %>
				</c:if>
				<c:if test="${true eq txnConfig.ccEnabled || true eq txnConfig.atmEnabled}">
					<%@ include file="dc.jsp" %>
				</c:if>
				<c:if test="${true eq txnConfig.netBankingEnabled}">
					<%@ include file="nb.jsp" %>
				</c:if>
				<c:if test="${true eq txnConfig.impsEnabled}">
					<%@ include file="imps.jsp" %>
				</c:if>
				<c:if test="${true eq walletInfo.walletEnabled}">
					<%@ include file="ppi.jsp" %>
				</c:if>
				<c:if test="${1 eq otherPaymentMethodEnabled}">
					<%@ include file="other.jsp" %>
				</c:if>
				<c:if test="${true eq txnConfig.cashcardEnabled}">
					<%@ include file="itzcash.jsp" %>
				</c:if>		
			</div>
		</div>
		<!--Middle Container End-->
		<div class="clear"></div>
		<!--Footer Container Start-->
		<div class="footer-query-top"></div>
		<div class="footer-query ">
			<div class="fl">
				<script src="https://seal.verisign.com/getseal?host_name=secure.paytm.in&amp;size=S&amp;use_flash=NO&amp;use_transparent=NO&amp;lang=en"></script>
				<img src="images/web/verified-visa.png" class="LR" title="Verified by Visa" alt="Verified by Visa" />
				<img src="images/web/master-card.png" class="LR" alt="Master Card" title="Master Card" />
				<img src="images/web/pci.png" alt="PCI" title="PCI" />			
			</div>
			<div class="fr footer-left">
				&copy; 2012-2013 Powered by Paytm payments
			</div>
		</div>
		<%@include file="../common/pgTrack.jsp" %>
		<!--Footer Container End-->
	</body>
</html>