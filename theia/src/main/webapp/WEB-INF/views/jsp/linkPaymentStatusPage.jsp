<!DOCTYPE html>
<%@ page session="false" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="robots" content="noindex,nofollow">
	<meta http-equiv="Pragma" content="no-cache">
	<meta http-equiv="Cache-Control" content="no-cache">
	<meta http-equiv="Expires" content="-1">
	<meta http-equiv="x-ua-compatible" content="IE=9">
	<meta name="HandheldFriendly" content="True">
	<meta name="MobileOptimized" content="320">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta http-equiv="cleartype" content="on">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<meta name = "format-detection" content = "telephone=no">
	<script>
		console.log("LinkType ===> ${requestScope['LINK_TYPE']}");
		function merchantLogoCb(isLoaded) {
			if (document && document.getElementsByClassName) {
				var ele = document.getElementsByClassName(isLoaded ? "merchantLogo" :"defaultLogo");
				if(ele && ele.length > 0){
					ele[0].style.display = "block";
				}
			}
		}
	</script>
	<style>
		.pct-txt{position:relative;word-break:break-all}.pcf{background:#f3f7f8;height:4px;margin-bottom:24px}.pcf-total-box{border-top:1px solid #dde5ed}.txt-color{color:#1d252d}.pb-11{padding-bottom:11px}.pay-head{letter-spacing:1px;font-weight:600;color:#506d85}.sb{font-weight:600}.WebRupee{line-height:14px}*{padding:0;margin:0;box-sizing:border-box;outline:0;border:0}body{background:#f3f7f8;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol";color:#222}.container{height:100%}.c_primary,a{color:#00b9f5;text-decoration:none}.failed .s_color{color:#fd5c5c}.process .s_color{color:#ffa400}.process .border{border-color:#ffa400}.failed .border{border-color:#fd5c5c}.paid .border{border-color:#21c17a}.process .back{background-color:#fff5e5}.failed .back{background-color:#faefef}.vt{vertical-align:top}.vm{vertical-align:middle}.w100{width:100%}.content{padding:45px 30px;min-height:571px}.pt-10{padding-top:10px}.px-40{padding-left:40px;padding-right:40px}.mt-4{margin-top:4px}.bg_white{background:#fff}.mw-100{width:800px;max-width:100%}.center{margin-left:auto;margin-right:auto}.mb-34{margin-bottom:34px}.mt-26{margin-top:26px}.mt-36{margin-top:36px}.mb-40{margin-bottom:40px}.mt-34{margin-top:34px}.mt-14{margin-top:14px}.pt-22{padding-top:22px}.pl-22{padding-left:22px}.pb-22{padding-bottom:22px}.ml-20{margin-left:20px}h3{font-size:23px}.f-21{font-size:21px}.f-24{font-size:24px}.f-17{font-size:17px}.f-15{font-size:15px}.pt-2{padding-top:3px}.d-block{display:block}.d-i-b{display:inline-block}.alert+.s_seperator,.header,.hide,.paid .alert,.w-hide{display:none}.t-center{text-align:center}.fl{float:left}.fr{float:right}.f-12{font-size:12px}.f-36,.tAmount{font-size:36px}.pl-15{padding-left:15px}.footer{border-bottom-left-radius:8px;border-bottom-right-radius:8px;color:#666;background:#fafbfb}.lh-2{line-height:19.5px}.lh-20{line-height:20px}.alert{padding:14px;border:1px solid;max-width:360px;border-radius:3px;margin-bottom:56px}.tAmount{line-height:32px;letter-spacing:-.39px;color:#222;font-weight:700}.tAmount .WebRupee{font-weight:400;margin-right:4px}.merchent{position:relative;word-break:break-all}.paid .merchent:after{position:absolute;content:'';width:30px;background:#e2ebee;height:3px;bottom:-34px;left:0}.paid .o-id{margin-top:69px}.paid .g-color{color:#21c17a}.info-img{margin-top:110px}.default{margin:11px auto 0}.right img{margin-right:30px;max-width:211px}.left,.right{padding:22px 0;color:#666}@media only screen and (min-width:768px){.container{box-shadow:0 -4px 30px 0 rgba(0,0,0,.02)}.content{border-top-left-radius:8px;border-top-right-radius:8px;margin-top:91px}.default{display:inline-block}}@media only screen and (max-width:767px){.footer,.m-center,.m-tc{text-align:center}.m-d-i-b{display:inline-block}.m-center,.valid img:first-child{margin-left:auto;margin-right:auto}.f-14{font-size:14px}.valid img:first-child{margin-top:11px;max-height:69px;width:auto;display:block}.footer{height:45px;overflow:hidden}.footer .left{padding:12px 0}.f-none,.footer span img{float:initial}.plr20{padding-left:20px;padding-right:20px}.header,.m-db{display:block}.m-hide,.merchent:after{display:none}.w-hide{display:initial}.mobile{font-size:11px;color:#666}.m-mt-0{margin-top:0}.m-mb-0{margin-bottom:0}.m-mt-28{margin-top:28px}.m-mb-16{margin-bottom:16px}.m-mb-40{margin-bottom:40px}.m-mb-20{margin-bottom:20px}.m-mb-64{margin-bottom:64px}.m-p-0{padding:0}.m-br-0{border-radius:0}.m-p-8{padding:8px}.m-img{width:50px;height:50px}.m-px-0{padding-left:0;padding-right:0}.alert{max-width:335px}.failed .border,.paid .border,.process .border{border:none}.m-f-10{font-size:10px}.wrapper,body,html{height:100%}.content{min-height:calc(100% - 45px)}.m-mt-30{margin-top:30px}.m-mt-12{margin-top:12px}.paid .o-id{margin-top:32px}.tAmount{letter-spacing:-.4px;font-weight:700;display:inline-block;line-height:36px;vertical-align:middle;margin-right:14px;color:#222}.info-img{margin-top:11px}.ftr{padding:14px}.ftr span{margin-left:9px;padding-top:2px}.footer .ftr img{float:none}.ivw{display:flex;flex-direction:column;align-items:center;justify-content:center}.footer img{float:none;margin-left:0;margin-right:10px}.footer span{float:none;padding:0}}.web-icon{margin-left:6px;position:relative;top:3px}@media only screen and (max-width:359px){.sf-8{font-size:8px;padding-top:0}.xs-lh-18{line-height:18px}}@media only screen and (max-width:360px){.mobile{font-size:9px}}.defaultLogo,.merchantLogo{display:none}
	</style>

	<!-- Fonts -->
	<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700" rel="stylesheet">
	<title>Paytm Secure Online Payment Gateway</title>

</head>
<body>
<c:set var="currentView" scope="request" value="${requestScope['SHOW_VIEW_FLAG']}" />
<c:choose>
	<c:when test="${currentView == 'MERCHANT_LIMIT_ERROR_SCREEN'}">
		<script>document.body.id = "webView"</script>
	</c:when>
	<c:when test="${currentView != 'MERCHANT_LIMIT_ERROR_SCREEN'}">
		<script>document.body.id = "fullScreen"</script>
	</c:when>
</c:choose>

<c:choose>
	<c:when test="${currentView == 'MERCHANT_LIMIT_ERROR_SCREEN'}">
		<c:set var="imgSrc" scope="request" value="images/icFailed.png" />
		<c:set var="paymentStatus" value="Payment Declined" />
		<c:set var="statusClass" value="failed" />
	</c:when>
	<c:when test="${requestScope['PAYMENT_STATUS'] == 'SUCCESS'  or requestScope['PAYMENT_STATUS'] == 'TXN_SUCCESS'}">
		<c:set var="imgSrc" scope="request" value="images/success.png" />
		<c:set var="paymentStatus" value="Paid Successfully" />
		<c:set var="statusClass" value="paid" />
	</c:when>
	<c:when test="${requestScope['PAYMENT_STATUS'] == 'CANCEL'}">
		<c:set var="imgSrc" scope="request" value="images/icFailed.png" />
		<c:set var="paymentStatus" value="Payment Cancelled" />
		<c:set var="statusClass" value="failed" />
	</c:when>
	<c:when
			test="${requestScope['PAYMENT_STATUS'] == 'PROCESSING' or requestScope['PAYMENT_STATUS'] == 'REDIRECT'}">
		<c:set var="imgSrc" scope="request" value="images/process.png" />
		<c:set var="paymentStatus" value="Payment request under process" />
		<c:set var="statusClass" value="process" />
	</c:when>
	<c:otherwise>
		<c:set var="imgSrc" scope="request" value="images/icFailed.png" />
		<c:set var="paymentStatus" value="Payment failed" />
		<c:set var="statusClass" value="failed" />
	</c:otherwise>
</c:choose>



<c:choose>
	<c:when test="${requestScope['PAYMENT_STATUS'] == 'CANCEL' or requestScope['PAYMENT_STATUS'] == 'SUCCESS' or requestScope['PAYMENT_STATUS'] == 'FAIL' or requestScope['PAYMENT_STATUS'] == 'PROCESSING' or requestScope['PAYMENT_STATUS'] == 'REDIRECT' or requestScope['PAYMENT_STATUS'] == 'TXN_SUCCESS' or requestScope['PAYMENT_STATUS'] == 'TXN_FAILURE'}">
		<div class="container wrapper mw-100 center m-mt-0 m-d-i-b">
			<div class="content bg_white px-40 m-px-0">
				<!-- if Paid | Progress | Failed -->
				<div class="valid">

					<span class="merchantLogo"><img src="${requestScope['MERCHANT_IMAGE']}" class="m-db m-center" height="40px" onerror="merchantLogoCb(false)" onload="merchantLogoCb(true)" /> </span>
					<div class="m-db m-center defaultLogo">
						<svg class="d-block default" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="51" height="51" viewBox="0 0 51 51"> <defs> <path id="a" d="M0 25.5V51h51V0H0z"/> <path id="c" d="M0 51h51V0H0z"/> </defs> <g fill="none" fill-rule="evenodd"> <mask id="b" fill="#fff"> <use xlink:href="#a"/> </mask> <path fill="#FEF6E1" d="M51 25.5C51 39.583 39.583 51 25.5 51S0 39.583 0 25.5 11.417 0 25.5 0 51 11.417 51 25.5" mask="url(#b)"/> <path stroke="#012B72" stroke-linecap="round" stroke-linejoin="round" stroke-width=".8" d="M36.905 36.09h-22.81"/> <mask id="d" fill="#fff"> <use xlink:href="#c"/> </mask> <path fill="#FFF" d="M17.162 35.833h16.805v-4.018H17.162z" mask="url(#d)"/> <path fill="#FC9A7C" d="M17.375 20.99a2.021 2.021 0 0 1-2.021-2.02v-4.06h4.042v4.06a2.02 2.02 0 0 1-2.021 2.02M21.437 20.99a2.021 2.021 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.02 2.02M25.5 20.99a2.02 2.02 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.021 2.02M29.563 20.99a2.021 2.021 0 0 1-2.021-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.02 2.02M33.625 20.99a2.02 2.02 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.021 2.02" mask="url(#d)"/> <path stroke="#012B72" stroke-linecap="round" stroke-linejoin="round" stroke-width=".8" d="M17.217 31.716h16.336M17.375 20.99a2.021 2.021 0 0 1-2.021-2.02v-4.06h4.042v4.06a2.02 2.02 0 0 1-2.021 2.02zM21.437 20.99a2.021 2.021 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.02 2.02zM25.5 20.99a2.02 2.02 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.021 2.02zM29.563 20.99a2.021 2.021 0 0 1-2.021-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.02 2.02zM33.625 20.99a2.02 2.02 0 0 1-2.02-2.02v-4.06h4.041v4.06a2.021 2.021 0 0 1-2.021 2.02zM33.781 21.206V36.09H17.22V21.206" mask="url(#d)"/> </g> </svg>
					</div>

					<div class="status plr20 mt-36 m-mt-30 mb-34 m-mb-20 ${statusClass}">
							<div class="lh-2 s_color f-17 m-hide g-color">
									${paymentStatus} <img class="web-icon" src="${ptm:stResPath()}${imgSrc}" width="18px" />
							</div>
						<div class="d-i-b m-db m-tc">
								<span class="lh-2 s_color f-17 w-hide">
										${paymentStatus} <c:if test="${requestScope['PAYMENT_STATUS'] == 'SUCCESS' or requestScope['PAYMENT_STATUS'] == 'TXN_SUCCESS'}"><span> to</span></c:if>
								</span>
							<div class="f-24 c_dark merchent mt-4"><b>${requestScope['MERCHANT_NAME']}</b></div>
						</div>

						<div class="d-i-b fr m-db m-tc f-none m-mt-28 m-mb-16">
							<!-- <c:set var="txnAmount" value="${requestScope['TXN_AMOUNT']}" /> -->
							<div class="tAmount"><span class="WebRupee">&#8377</span><fmt:formatNumber value="${fn:trim(requestScope['TOTAL_AMOUNT'])}" maxFractionDigits="2"></fmt:formatNumber>
							</div>
							<img width="36px" height="36px" class=" w-hide vm" src="${ptm:stResPath()}${imgSrc}" />
						</div>

						<c:choose>
							<c:when test="${requestScope['PAYMENT_STATUS'] == 'CANCEL' or requestScope['PAYMENT_STATUS'] == 'FAIL'  or requestScope['PAYMENT_STATUS'] == 'TXN_FAILURE'}">
								<div class="alert mt-26 m-mt-0 m-mb-40 f-12 border back m-center lh-20">
									<c:choose>
										<c:when test="${!empty requestScope['ERROR_MESSAGE']}">
											${requestScope['ERROR_MESSAGE']}
										</c:when>
										<c:otherwise>
											<c:choose>
												<c:when test="${requestScope['PAYMENT_STATUS'] == 'FAIL' or requestScope['PAYMENT_STATUS'] == 'TXN_FAILURE'}">
													Your payment request could not be completed. Please use the link shared with you to make the payment again. Please note in case your money is deducted, it would be refunded to your bank/payment instrument within 7 days.
												</c:when>
												<c:otherwise>
													Your payment request could not be completed as you have cancelled the payment. Please use the link shared with you to make the payment again.
												</c:otherwise>
											</c:choose>
										</c:otherwise>
									</c:choose>
								</div>
							</c:when>
							<c:when
									test="${requestScope['PAYMENT_STATUS'] == 'PROCESSING' or requestScope['PAYMENT_STATUS'] == 'REDIRECT'}">
								<div class="alert mt-26 m-mt-0 m-mb-40 f-12 border back m-center lh-20">
									<c:choose>
										<c:when test="${!empty requestScope['ERROR_MESSAGE']}">
											${requestScope['ERROR_MESSAGE']}
										</c:when>
										<c:otherwise>
											Your payment request is under process as we could not get the final response from your bank/payment instrument. Please contact your merchant. In case the money is not credited to the merchant will be automatically refunded to your bank/payment instrument within 7 days.
										</c:otherwise>
									</c:choose>
								</div>
							</c:when>
						</c:choose>

						<div class="o-id c_dark mb-40 m-mb-0 mt-26 m-mb-64 m-tc  lh-2">
							<c:if test="${!empty requestScope['ORDER_ID']}">

								<c:set var="orderLength" value="${fn:length(requestScope['ORDER_ID'])}"></c:set>
								<fmt:parseNumber var="orderIntLength" integerOnly="true" type="number" value="${orderLength}" />

								<c:set var = "orderPart1" value = "${fn:substring(requestScope['ORDER_ID'], 0, 8)}" />
								<c:set var = "orderPart2" value = "${fn:substring(requestScope['ORDER_ID'], 8, orderIntLength)}" />

								<p class="f-17">Order ID: ${orderPart1} <b>${orderPart2}</b></p>
							</c:if>
							<p class="f-15 pt-10">${requestScope['TXN_DATE']}</p>
						</div>
						
						<!-- amount description detail start -->
						<c:if test="${!empty requestScope['CHARGEAMOUNT'] and !empty requestScope['TOTAL_AMOUNT'] and !empty requestScope['TXN_AMOUNT']}">
							<div>
								<div class="pcf"></div>
								<div class="w100 d-i-b">
									<p class="f-12 pb-11 pay-head">PAYMENT DETAILS</p>
									<div class="w100 fl pb-11">
										<div class="f-12 pct-txt mt-4 fl txt-color">Bill Amount</div>
										<div class="sb f-12 pct-txt mt-4 fr txt-color"><span class="WebRupee">&#8377</span>
											<fmt:formatNumber value="${fn:trim(requestScope['TXN_AMOUNT'])}" maxFractionDigits="2"></fmt:formatNumber>
										</div>
									</div>
									<div class="w100 fl pb-11">
										<div class="f-12 pct-txt mt-4 fl txt-color">Convenience Fee </div>
										<div class="sb f-12 pct-txt mt-4 fr txt-color"><span class="WebRupee">&#8377</span>
											<fmt:formatNumber value="${fn:trim(requestScope['CHARGEAMOUNT'])}" maxFractionDigits="2"></fmt:formatNumber>
										</div>
									</div>
									<!-- <div class="w100 fl pb-11">
										<div class="f-12 merchent mt-4 fl txt-color">GST on Convenience Fee</div>
										<div class="sb f-12 merchent mt-4 fr txt-color"><span class="WebRupee">&#8377</span>
											<fmt:formatNumber value="${fn:trim(txnAmount)}" maxFractionDigits="2"></fmt:formatNumber>
										</div>
									</div> -->
									<div class="w100 fl pcf-total-box txt-color ">
										<div class="f-12 pct-txt mt-4 pt-10 fl sb">Total Amount Paid</div>
										<div class="sb f-12 pct-txt mt-4 pt-10 fr txt-color"><span class="WebRupee">&#8377</span>
											<fmt:formatNumber value="${fn:trim(requestScope['TOTAL_AMOUNT'])}" maxFractionDigits="2"></fmt:formatNumber>
										</div>
									</div>
								</div>
							</div>
						</c:if>
						<!-- amount description detail end -->
                        
					</div>
				</div>
			</div>
			<div class="bg_dull footer m-br-0 m-p-0 m-tc">
				<div class="left pt-22 pb-22 d-i-b vt">
					<img class="fl ml-20 f-none d-i-b vm sfl" src="${ptm:stResPath()}images/logo-business.svg" width="40px" />	
					<c:choose>
						<c:when test="${requestScope['LINK_TYPE'] == 'INVOICE'}">
							<span class="fl f-12 pl-15 pt-2 mobile vt  d-i-b vm pl-15 sfl sp-4 sf-8 xs-lh-18">To create Payment Invoices, visit <a href="https://business.paytm.com"  target="_blank" rel="noopener noreferrer">paytm.business/invoices</a> </span>
						</c:when>
						<c:otherwise>
							<span class="fl f-12 pl-15 pt-2 mobile vt  d-i-b vm pl-15 sfl sp-4 sf-8 xs-lh-18">To create Payment Links, visit <a href="https://business.paytm.com/payment-link"  target="_blank" rel="noopener noreferrer">paytm.business/links</a> </span>
						</c:otherwise>
					</c:choose>
				</div>
				<div class="right ib fr m-hide">
						<img src="${ptm:stResPath()}images/pro.png"/>
				</div>
			</div>
		</div>
	</c:when>
	<c:otherwise>
		<div class="container mw-100 center m-mt-0">
			<div class="content m-br-0  bg_white center  t-center px-25 error ivw">
				<div class="invalid-link">
					<img class="m-img vm info-img" src="${ptm:stResPath()}${imgSrc}" />
					<h3 class="mt-34 m-mt-30 f-21"><b>${paymentStatus}</b></h3>
					<c:choose>
						<c:when test="${currentView == 'MERCHANT_LIMIT_ERROR_SCREEN'}">
							<p class="mt-14 f-14 m-mt-12">
								<c:choose>
									<c:when test="${!empty requestScope['ERROR_MESSAGE']}">
										${requestScope['ERROR_MESSAGE']}
									</c:when>
									<c:otherwise>
										Your payment request could not be completed as the merchant has crossed his payment acceptance limit. Please ask the merchant to reach out to Paytm helpdesk.
									</c:otherwise>
								</c:choose>
							</p>
						</c:when>
						<c:otherwise>
							<p class="mt-14 f-14 m-mt-12">
								<c:choose>
									<c:when test="${!empty requestScope['ERROR_MESSAGE']}">
										${requestScope['ERROR_MESSAGE']}
									</c:when>
									<c:otherwise>
										Your payment request could not be completed. Please use the link shared with you to make the payment again. Please note in case your money is deducted, it would be refunded to your bank/payment instrument within 7 days.
									</c:otherwise>
								</c:choose>
							</p>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="bg_dull footer pl-22 m-br-0 m-p-0 m-tc">

				<div class="left pt-22 pb-22 d-i-b ">
						<img class="fl ml-20 f-none d-i-b vm sfl" src="${ptm:stResPath()}images/logo-business.svg" width="40"  />
						<c:choose>
							<c:when test="${requestScope['LINK_TYPE'] == 'INVOICE'}">
								<span class="fl f-12 pl-15 pt-2 mobile vt  d-i-b vm pl-15 sfl sp-4 sf-8 xs-lh-18">To create Payment Invoices, visit <a href="https://business.paytm.com"  target="_blank" rel="noopener noreferrer">paytm.business/invoices</a> </span>
							</c:when>
							<c:otherwise>
								<span class="fl f-12 pl-15 pt-2 mobile vt  d-i-b vm pl-15 sfl sp-4 sf-8 xs-lh-18">To create Payment Links, visit <a href="https://business.paytm.com/payment-link"  target="_blank" rel="noopener noreferrer">paytm.business/links</a> </span>
							</c:otherwise>
						</c:choose>
					</div>
					<div class="right ib fr m-hide">
							<img src="${ptm:stResPath()}images/pro.png" />
					</div>
			</div>
		</div>
	</c:otherwise>
</c:choose>

<script async src="https://staticpg.paytm.in/pgp/lib/logger/logger.es.min.js" type="application/javascript"></script>
<script>
	var domLoadStart = Date.now();
	window.onload = function () {
		if (window && window.logger) {
			var isDebuggerOn = "${ConfigurationUtil.getProperty('environment')}" == "prod";
			if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
				window.mobileOrDesktop = "mobile";
			} else {
				window.mobileOrDesktop = "desktop";
			}
			logger.init({ 'debug': !isDebuggerOn });
			var domLoadEnd = Date.now(),
				typeOfLink = "${requestScope['LINK_TYPE']}" ? "${requestScope['LINK_TYPE']}" : "LINK";
			var domLoadedTime = (domLoadEnd - domLoadStart) + 'ms', isMerchantLogoAvailable = "${requestScope.MERCHANT_IMAGE}" ? true : false;
			logger.log({ "screen": "Payment Confirmation Screen", "eventType": "Payment_Confirmation_Screen_Load", "mid": "MID", "uid": "UID", "linkType": typeOfLink, "status": "${requestScope.PAYMENT_STATUS}", "pageLoadTime": domLoadedTime, "isLogoAvaiable": isMerchantLogoAvailable, "agent": window.mobileOrDesktop })
		}
	}
	if (history) {
		if (history.pushState) {
			history.pushState(null, null, location.href);
		}
		window.onpopstate = function () {
			if (history.go) {
				history.go(1);
			}
		};
	}
</script>
</body>
</html>
