<%@ include file="../../common/config.jsp"%>
<%@ page import="java.util.List" %>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Paytm Secure Online Payment Gateway</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache" />
<meta http-equiv="Expires" content="-1" />
<link rel="shortcut icon" href="../images/Paytm.ico" type="image/x-icon" />
<c:set var="port" value="" />
<c:if test="${empty header['PRX']}">
	<c:set var="port" value=":${pageContext.request.serverPort}" />
</c:if>
<!-- <base -->
<!-- 	href="/oltp-web/" /> -->
<link href="css/wap/mobile.css" rel="stylesheet" type="text/css" />

</head>
<body>
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>
	<!--Header-->
	<div class="header">
		<div class="fr">
			<img src="images/wap/paytmLow/paytm-logo.png" alt="Paytm" title="Paytm" height="21" width="105" style="margin-top:3px"
			/>
		</div>
		<div class="clear"></div>
	</div>
	
	<c:if test="${txnInfo.retry}">
		<div class="alert">
			<div class="fl">
				<img title="" alt="" src="images/wap/alert.png">
			</div>
			<div class="fl alert-txt" id="retryMsg">
				<c:out value="${txnInfo.displayMsg}" escapeXml="true" />
			</div>
			<div class="clear"></div>
		</div>
	</c:if>
	
	<p class="pad">Order ready to be processed for
		Rs.${txnInfo.txnAmount}. Please complete your payment details.</p>
	<c:set var="customerDetails">
		<c:if test="${!empty txnInfo.custID}">${txnInfo.custID}<br />
		</c:if>
		<c:if test="${!empty txnInfo.emailId}">${txnInfo.emailId}<br />
		</c:if>
		<c:if test="${!empty txnInfo.address1}">${txnInfo.address1}<br />
		</c:if>
		<c:if test="${!empty txnInfo.address2}">${txnInfo.address2}<br />
		</c:if>
		<c:if test="${!empty txnInfo.city}">${txnInfo.city}<br />
		</c:if>
		<c:if test="${!empty txnInfo.pincode}">${txnInfo.pincode}</c:if>
	</c:set>
	<c:if test="${!empty customerDetails}">
		<h2>Billing details(js)</h2>
		<div class="outer">${customerDetails}</div>
	</c:if>
	<!--Payment mode-->
	<c:if test="${saveCardEnabled}">
	<c:if test="${ ('5' ne paymentType) }">
		<div class="border-bar">
			<div class="fl">
				<a href="jsp/wap/lowEnd/paymentForm.jsp?txn_Mode=SC"> Saved Cards </a>
			</div>
			<div class="fr">
				<img src="images/wap/arrow.png" />
			</div>
			<div class="clear"></div>
		</div>
		</c:if>
		<c:if test="${('5' eq paymentType) }">
		<div class="blu-hd">
			<div class="fl">Saved Cards</div>
			<div class="fr">
				<img src="images/wap/dwn-arw.png" alt="" title="" />
			</div>
			<div class="clear"></div>
		</div>
		<div class="divider"></div>
		<div class="form-container">
		<form autocomplete="off" name="creditcard-form" method="post" action="/payment/request/submit">
				<p class="mt10">
					Select your card<br /> 
					<c:set var="savedCardList"
							value="${cardInfo.merchantViewSavedCardsList}"></c:set>
						<c:if test="${txnConfig.addMoneyFlag}">
							<c:set var="savedCardList" value="${cardInfo.addAndPayViewCardsList}"></c:set>
						</c:if>
					<c:forEach var="card" varStatus="status" items="${savedCardList}">
						<c:choose>
							<c:when test="${'MASTER' eq card.cardType}">
								<c:set var="cardImagePrefix" value="master" />
							</c:when>
							<c:when test="${'VISA' eq card.cardType}">
								<c:set var="cardImagePrefix" value="visa" />
							</c:when>
							<c:when test="${'AMEX' eq card.cardType}">
								<c:set var="cardImagePrefix" value="amex" />
							</c:when>
						</c:choose>
						<li id="${card.cardId}-item" >
							<label><input class="hidden" type="radio" name="savedCardId" value="${card.cardId}" <c:if test="${status.index == 0}">checked</c:if> />Use ${card.cardNumber} </label><c:if test="${!empty cardImagePrefix}">&nbsp;<img src="images/wap/${cardImagePrefix}-card.jpg" alt="" title=""/></c:if>
							<a href="DeleteCardDetails?savedCardId=${card.cardId}&ajax=no" class="delete" >Delete</a>
						</li>
					</c:forEach>
				</p>
				<p class="mt10">
					CVV/Secure Code<br /> 
					<input type="text" style="width: 40px;" name="cvvNumber" maxlength="4" />
				</p>
				<c:if
						test="${'5' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
					</c:if>
				<p class="mt10">   
				<input type="hidden" name="txnMode" value="CC" />
				<input type="hidden" name="txn_Mode" value="SC" />
					<input type="hidden" name="channelId" value="WAP" />
					<input type="hidden" name="AUTH_MODE" value="3D" />
					<input name="Submit" type="submit" value="Pay Now" class="button" />
					<a href="cancelTransaction" class="cancel">Cancel</a>
				</p>
			</form>
		</div>
		</c:if>
	</c:if>
	<c:if test="${ccEnabled}">
		<c:if test="${'1' ne paymentType }">
			<div class="border-bar">
				<div class="fl">
					<a href="jsp/wap/lowEnd/paymentForm.jsp?txn_Mode=CC"> Credit/Debit Card </a>
				</div>
				<div class="fr">
					<img src="images/wap/arrow.png" />
				</div>
				<div class="clear"></div>
			</div>
		</c:if>
		<c:if test="${'1' eq paymentType }">
			<div class="blu-hd">
				<div class="fl">Credit/Debit Card</div>
				<div class="fr">
					<img src="images/wap/dwn-arw.png" alt="" title="" />
				</div>
				<div class="clear"></div>
			</div>
			<div class="divider"></div>
			<div class="form-container">
				<form autocomplete="off" method="post" action="/payment/request/submit">
					<p class="mt10">
						Credit/Debit card no. <br /> <input autocomplete="off" type="text"
							name="cardNumber" maxlength="19" /> <br />
							 <img src="images/wap/cards.png" alt="cards" title="cards" />
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD']}</p>
					</c:if>
					<p class="mt10">
						Expiry date (MMYY) <br /> 
						<input name="ccExpiryMonthYear"	type="text" maxlength="4"> <br /> 
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</p>
					</c:if>
					<p class="mt10">
						CVV no. <br /> <input autocomplete="off" type="password" name="cvvNumber" maxlength="4" /> <br /> 
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
					</c:if>
					
					<c:if test="${saveCardOption}">
					<p class="mt10">
						<label class="sm-txt">
						<input name="storeCardFlag"	type="checkbox" value="Y" />&nbsp;Save this card for future
							transactions</label>
					</p>
					</c:if>
					<p class="mt10">
						<input type="hidden" name="txnMode" value="CC" /> 
						<input type="hidden" name="txn_Mode" value="CC" />
						<input
							type="hidden" name="channelId" value="WAP" /> <input
							type="hidden" name="AUTH_MODE" value="3D" /> <input
							name="Submit" type="submit" value="Pay Now" class="button" /> 
					<a href="cancelTransaction" class="cancel">Cancel</a>
					</p>
				</form>
			</div>
		</c:if>
	</c:if>
	
	<c:if test="${impsEnabled}">
		<%@ include file="imps.jsp" %>
	</c:if>
	<c:if test="${walletEnabled}">
		<%@ include file="wallet.jsp" %>
	</c:if>
	
	<!--Footer-->
	<div class="footer">
		<img src="images/wap/footer_image.png" alt="" title="" />
	</div>
	<div class="footer-query ">
		<div class="fl">
			<script type="text/javascript" src="js/jquery.min.js"></script>
			<script type="text/javascript" src="js/wap/functions.js"></script>
			<script src="https://seal.verisign.com/getseal?host_name=secure.paytm.in&amp;size=S&amp;use_flash=NO&amp;use_transparent=NO&amp;lang=en"></script>
				
		</div>
		
	</div>
</body>
</html>
