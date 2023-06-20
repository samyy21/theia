<%@ include file="../common/config.jsp"%>

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
<base
		href="/oltp-web/" />
<link href="css/wap/mobile.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">
	maintainenceNBBank = new Object();
	lowPerfNBBank = new Object();
	maintainenceATMBank = new Object();
	lowPerfATMBank = new Object();
	
	function atmSelectChange(param) {
		var bankName = param.value;
		var selectedIndex = param.options.selectedIndex;
		var displayName = param.options[selectedIndex].text;
		
		document.getElementById("atmErrorMsg").innerHTML = "";
		document.getElementById("dcSubmit").disabled = "";
		if(maintainenceATMBank[bankName]) {
			document.getElementById("atmErrorMsg").innerHTML = displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
				document.getElementById("dcSubmit").disabled = "disabled";
		} else if(lowPerfATMBank[bankName]) {
			document.getElementById("atmErrorMsg").innerHTML =  "Experiencing high failures on "+ displayName +" in last few transactions. Recommend you pay using a different mode.";
		}
	}
	
	function nbSelectChange(param) {
		var bankName = param.value;
		var selectedIndex = param.options.selectedIndex;
		var displayName = param.options[selectedIndex].text;
		
		document.getElementById("nbErrorMsg").innerHTML = "";
		document.getElementById("nbSubmit").disabled = "";
		if(maintainenceNBBank[bankName]) {
			document.getElementById("nbErrorMsg").innerHTML = displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.";
			document.getElementById("nbSubmit").disabled = "disabled";
		} else if(lowPerfNBBank[bankName]) {
			document.getElementById("nbErrorMsg").innerHTML =  "Experiencing high failures on "+ displayName +" in last few transactions. Recommend you pay using a different mode.";
		}
	}
</script>

</head>
<%
	boolean showOthers = false;
    boolean selectOther = false;
	if ((request.getParameter("bankCode") != null && (request.getParameter("bankCode").trim().indexOf("other")>-1 ))
			|| (request.getAttribute("reqBankCode") != null && (request.getAttribute("reqBankCode").toString().trim().indexOf("other")>-1 ))) {
		showOthers = true;
	}
	
	List dbList = (List)session.getAttribute("DebitCardList") ;
	List atmList = (List)session.getAttribute("ATMCardList") ;
	if((dbList == null || dbList.size()==0) && (atmList == null || atmList.size()==0))
	{
		showOthers = true;
		selectOther = true;
		request.setAttribute("reqBankCode", "other");
	}

%>
<body>
<c:if test="${txnInfo == null}">
	<c:redirect url="/error"/>
</c:if>
	<!--Header-->
	<div class="header">
		<c:if test="${!empty merchantImage}">
			<div class="fl">
				<img src="images/wap/merchant/${merchantImage}"/>
			</div>
		</c:if>
		<div class="fr">
			<img src="images/wap/logo.png" alt="Paytm" title="Paytm" />
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
		<h2>Billing details</h2>
		<div class="outer">${customerDetails}</div>
	</c:if>
	<!--Payment mode-->
	<c:if test="${saveCardEnabled}">
	<c:if test="${ ('5' ne paymentType) }">
		<div class="border-bar">
			<div class="fl">
				<a href="jsp/wap/paymentForm.jsp?txn_Mode=SC"> Saved Cards </a>
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
							<c:forEach var="card" varStatus="status" items="${cardInfo.savedCardsList}">
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
									<%-- <c:when test="${'maestro' eq card.cardType}">
										<c:set var="cardImagePrefix" value="maestro" />
									</c:when> --%>
								</c:choose>
								<li id="${card.cardIdentifier}-item" >
									<label><input class="hidden" type="radio" name="savedCardId" <c:if test="${status.index==0}">checked='checked' </c:if> value="${card.cardIdentifier}"/>Use ${card.cardNumber} </label><c:if test="${!empty cardImagePrefix}">&nbsp;<img src="images/wap/${cardImagePrefix}-card.jpg" alt="" title=""/></c:if>
									<a href="DeleteCardDetails?savedCardId=${card.cardIdentifier}&ajax=no" class="delete" >Delete</a>
									
								</li>
								
							</c:forEach>
							
				</p>
				<p class="mt10">
					CVV/Secure Code<br /> <input type="text" style="width: 40px;" name="cvvNumber"	maxlength="4" />
				</p>
					<c:if
						test="${'5' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
					</c:if>
					<input type="hidden" name="txnMde" value="SC" />
					<input type="hidden" name="channelId" value="WAP" />
					<input type="hidden" name="AUTH_MODE" value="3D" />
					<input type="hidden" name="txnMode" value="CC" />
				<p class="mt10">
				
					<input name="Submit" type="submit" value="Pay Now" class="button" />
					<!-- <input name="Submit" type="submit" value="Cancel" class="button" /> -->
					<a href="cancelTransaction" class="cancel">Cancel</a>
				</p>
			</form>
		</div>
		</c:if>
	</c:if>
	<c:if test="${'1' eq ccEnabled}">
		<c:if test="${'1' ne paymentType }">
			<div class="border-bar">
				<div class="fl">
					<a href="jsp/wap/paymentForm.jsp?txn_Mode=CC"> Credit Card </a>
				</div>
				<div class="fr">
					<img src="images/wap/arrow.png" />
				</div>
				<div class="clear"></div>
			</div>
		</c:if>
		<c:if test="${'1' eq paymentType }">
			<div class="blu-hd">
				<div class="fl">Credit Card</div>
				<div class="fr">
					<img src="images/wap/dwn-arw.png" alt="" title="" />
				</div>
				<div class="clear"></div>
			</div>
			<div class="divider"></div>
			<div class="form-container">
				<form autocomplete="off" method="post" action="/payment/request/submit">
					<p class="mt10">
						Credit card no. <br /> 
						<input autocomplete="off" type="text"	name="cardNumber" maxlength="19" /> <br /> <img
							src="images/wap/cards.png" alt="cards" title="cards" />
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD']}</p>
					</c:if>
					<p class="mt10">
						Expiry date (MMYY) <br /> <input name="ccExpiryMonthYear"
							type="text" maxlength="4"> <br /> <span class="sm-txt"></span>
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</p>
					</c:if>
					<p class="mt10">
						CVV no. <br /> <input autocomplete="off" type="password"
							name="cvvNumber" maxlength="4" /> <br /> <span class="sm-txt"></span>
					</p>
					<c:if
						test="${'1' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
					</c:if>
					<p class="mt10">
						<label class="sm-txt">
						<input name="storeCardFlag"	type="checkbox" value="Y" />&nbsp;Save this card for future
							transactions</label>
					</p>
					<p class="mt10">
					<input type="hidden" name="txn_Mode" value="CC" />
						<input type="hidden" name="txnMode" value="CC" /> <input
							type="hidden" name="channelId" value="WAP" /> <input
							type="hidden" name="AUTH_MODE" value="3D" /> <input
							name="Submit" type="submit" value="Pay Now" class="button" /> 
							<a href="cancelTransaction" class="cancel">Cancel</a>
					</p>
				</form>
			</div>
		</c:if>
	</c:if>
	<c:if test="${dcEnabled || atmEnabled}">
		<c:if test="${'2' ne paymentType && '8' ne paymentType}">
			<div class="border-bar">
				<div class="fl">
					<a href="jsp/wap/paymentForm.jsp?txn_Mode=DC"> Debit Card </a>
				</div>
				<div class="fr">
					<img src="images/wap/arrow.png" />
				</div>
				<div class="clear"></div>
			</div>
		</c:if>
		<c:if test="${'2' eq paymentType || '8' eq paymentType}">
			<div class="blu-hd">
				<div class="fl">Debit Card</div>
				<div class="fr">
					<img src="images/wap/dwn-arw.png" alt="" title="" />
				</div>
				<div class="clear"></div>
			</div>
			<div class="divider"></div>
			<div class="form-container">
				<form autocomplete="off" method="post" action="/payment/request/submit">
					<p class="mt10">
						Select your Bank <br />
						<c:set var="atmCardOptions" value="" />
						<c:if test="${!empty entityInfo.completeDcList}">
							<c:forEach var="item" items="${entityInfo.completeDcList}">
								<c:set var="atmCardOptions">${atmCardOptions}
									<option value="${item.bankName}" <c:if test="${reqBankCode eq item.bankName}">selected=selected</c:if>>${item.displayName}</option>
								</c:set>
								
								<script type="text/javascript">
									<c:if test="${item.maintainence}">
										maintainenceATMBank["${item.bankName}"] = true;
									</c:if>
									<c:if test="${item.lowPercentage}">
										lowPerfATMBank["${item.bankName}"] = true;
									</c:if>
								</script>
							</c:forEach>
						</c:if>
						
							<select name="bankCode" onChange="atmSelectChange(this)" id="atmBankCodeId">
								<option value="-1">Select your Bank</option>
								${atmCardOptions}
							</select>
							<span class="error" id="atmErrorMsg" style="display: block"></span>
					</p>
					<%
						if (showOthers) {
					%>

					<p class="mt10">
						Debit card no. <br /> <input autocomplete="off" type="text"
							name="cardNumber" maxlength="19" /> <br /> <img
							src="images/wap/cards.png" alt="cards" title="cards" />
					</p>
					<c:if
						test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD']}</p>
					</c:if>
					<p class="mt10">
						Expiry date (MMYY) <br /> <input name="ccExpiryMonthYear"
							type="text" maxlength="4" /> <br /> <span class="sm-txt">
							<em> (Optional for Maestro Cards) </em> </span>
					</p>
					<c:if
						test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CARD_EXPIRY']}">
						<p class="error">${requestScope.validationErrors['INVALID_CARD_EXPIRY']}</p>
					</c:if>
					<p class="mt10">
						CVV no. <br /> <input autocomplete="off" type="password"
							name="cvvNumber" maxlength="4" /> <br /> <span class="sm-txt">
							<em> (Optional for Maestro Cards) </em> </span>
					</p>
					<c:if
						test="${'2' eq paymentType && !empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_CVV']}">
						<p class="error">${requestScope.validationErrors['INVALID_CVV']}</p>
					</c:if>
					<p class="mt10">
						<label class="sm-txt"><input name="storeCardFlag"
							type="checkbox" value="true" />&nbsp;Save this card for future
							transactions</label>
					</p>
					<%
						}
					%>
					<p class="mt10">
					<input type="hidden" name="txn_Mode" value="DC" />
						<input type="hidden" name="txnMode" value="DC" /> <input
							type="hidden" name="channelId" value="WAP" /> <input
							type="hidden" name="AUTH_MODE" value="3D" /> <input
							name="Submit" type="submit" value="Pay Now" class="button" id="dcSubmit"/> 
							<a href="cancelTransaction" class="cancel">Cancel</a>
					</p>
				</form>
			</div>
		</c:if>
	</c:if>
	<c:if test="${netBankingEnabled}">
		<c:if test="${'3' ne paymentType}">
			<div class="border-bar">
				<div class="fl">
					<a href="jsp/wap/paymentForm.jsp?txn_Mode=NB"> Net Banking </a>
				</div>
				<div class="fr">
					<img src="images/wap/arrow.png" />
				</div>
				<div class="clear"></div>
			</div>
		</c:if>
		<c:if test="${'3' eq paymentType}">
			<div class="blu-hd">
				<div class="fl">Net Banking</div>
				<div class="fr">
					<img src="images/wap/dwn-arw.png" alt="" title="" />
				</div>
				<div class="clear"></div>
			</div>
			<div class="divider"></div>
			<div class="form-container">
				<form autocomplete="off" method="post" action="/payment/request/submit">
				<c:if test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_BANK']}">
								<div class="error">${requestScope.validationErrors['INVALID_BANK']}</div>
								</c:if>
					<p class="mt10">
						Select your Bank <br /> <select name="bankCode" onChange="nbSelectChange(this)" id="nbBankCodeId">
							<option value="-1">Select your Bank</option>
							<c:forEach var="bank" items="${entityInfo.completeNbList}">
								<option value="${bank.bankName}">${bank.displayName}</option>
								<script type="text/javascript">
									<c:if test="${bank.maintainence}">
										maintainenceNBBank["${bank.bankName}"] = true;
									</c:if>
									<c:if test="${bank.lowPercentage}">
										lowPerfNBBank["${bank.bankName}"] = true;
									</c:if>
								</script>
							</c:forEach>
						</select>
						<span class="error" id="nbErrorMsg" style="display: block"></span>
					</p>
					<p class="mt10">
					<input type="hidden" name="txn_Mode" value="NB" />
						<input type="hidden" name="txnMode" value="NB" /> <input
							type="hidden" name="channelId" value="WAP" /> <input
							type="hidden" name="AUTH_MODE" value="USRPWD" /> <input
							name="Submit" type="submit" value="Pay Now" class="button" id="nbSubmit"/> 
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

<% if(showOthers){ %>
<script>

//Debit card - ATM
function showAtm()
{
	$('#dccardId').hide();
	if ($(this).val().indexOf('other')>=0){
		$('#debitcard-form').show('slow');
		$('#dcCCnoId').hide();
		$('#dcexId').hide();
		$('#dcCvvId').hide();
	}
}
//showAtm();

</script>

<%} %>	
</body>
</html>
