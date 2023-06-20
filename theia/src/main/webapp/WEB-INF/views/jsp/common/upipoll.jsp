<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
	<meta charset="utf-8">
	<meta name="robots" content="noindex,nofollow" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Expires" content="-1" />
	<meta http-equiv="x-ua-compatible" content="IE=9" />
	<meta name="HandheldFriendly" content="True">
	<meta name="MobileOptimized" content="320">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta http-equiv="cleartype" content="on">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
	<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700i,800" rel="stylesheet">
	<style>
			*{
				box-sizing: border-box;
				margin: 0;
				padding: 0;
				border:0;
			}
			body{
				color:#222;
				font-family: 'Open Sans', sans-serif;
			}
			.ib{
				display: inline-block
			}
			.fr{
				float:right
			}
			.fl{
				float:left
			}
			.vt{
				vertical-align: top
			}
			.ml-15{
				margin-left:15px
			}
			.pos-a{
				position:absolute
			}
			.pos-r{
				position:relative
			}
			.f10{
			  font-size:10px
			}
			.f12{
				font-size:12px
			}
			.fw600{
				font-weight: 600
			}
			.blur{
			   width:100%;
				height:100vh;
				background: rgba(0, 0, 0, 0.1607843137254902);
				opacity: 0.5;
				z-index: -1;
				position:fixed
			}
			.m-body{
				width:100%;
				max-width:800px;
				background:#fff;
				margin:auto;
				left: 0;
				right: 0;
				border-radius: 4px;
			}
			.m-header{
				padding:27px 32px;
			}
			.m-title{
				font-size: 20px
			}
			.subtitle{
				margin-top:7px;
				padding-bottom: 19px;
				border-bottom: 1px solid #f3f7f8;
			}
			.subtitle img{
				margin-left:4px;
			}
			.step{
				width:40%;
			}
			.step2{
				margin-left:50px
			}
			.main{
			  font-size: 18px
			}
			.badge{
				padding:2px 12px;
				border-radius:3px;
				background:#ebfaff;
				color:#666;
			}
			.text{
			  width:calc(100% - 62px);
			}
			.details{
				font-size: 14px;
                margin-top: 16px;
                letter-spacing: 0.15px;
                line-height: 1.5;
			}
			.m-footer{
			  background:#fff5e5;
			  padding:13px 32px;
			  /* margin: 0 -32px -13px -32px; */
			  padding:13px 32px;
			  border-bottom-left-radius: 4px;
			  border-bottom-right-radius: 4px;
			}
			.m-content{
			  padding: 33px 32px 45px 32px;
			}
			.time b{
				letter-spacing: 1px;
				text-align: right
			}
			.w-hide{
				display:none
			}
			.tr{
				text-align:right
			}
			.step-img{
			  position: absolute;
			  top: 0;
			  right: 0;
			  bottom: 0;
			  margin: auto;
			}
			.cp{
			  cursor: pointer;
			}
			.xs-text{
				font-size: 10px;
				letter-spacing: 0;
			}
			.info-box{
				line-height:16px;
				padding:17px 12px;
				box-shadow:0 2px 14px 2px rgba(0, 0, 0, 0.08);
				max-width:255px;
				border-radius:2px;
				top: -9px;
				left:15px;
				width:300px;
				margin-left: 20px;
				background:#fff;
				z-index:9;
			}
			.info-box:after {
				top:7px;
				right: 100%;
				border: solid transparent;
				content: " ";
				height: 0;
				width: 0;
				position: absolute;
				border-right-color: #fff;
				border-width: 10px;
			}
			.hide{
				display:none;
			}
			.informationWrap{display: inline-block;}
			@media (max-width:767px){
				.m-hide{
					display: none
				}
				.w-hide{
					display: block
				}
				.name-sec{
				  max-width: calc(100% - 48px);
				}
				.m-header{
					padding:25px 20px 0 20px
				}
				.amount-head{
					background:rgb(243,247,248);
					padding:20px;
				}
				.amount-head img{
				  max-width: 48px;
				}
				.m-content{
				  padding: 30px 20px 40px 20px;
				}
				.m-body{
				  max-width: 100%;
				}
				.mer{
				  max-width: calc(100% - 70px);
				  text-overflow: ellipsis;
				  white-space: nowrap;
				  overflow: hidden;
				}
				.upi{
				  color:rgb(68,68,68);
				}
				.ls-05{
					letter-spacing:0.5px;
				}
				.m-body{
					position:static;
					width:initial;
					margin-top:0;
				}
				.m-wrapper{
				  padding-bottom: 43px;
				}
				.subtitle{
				  border-bottom:0px;
				  padding-bottom:0px;
				  padding-right:20px;
				}
				.m-title{
					font-size: 21px;
					line-height: 27px;
				}
				.subtitle span, .unit{
					color:rgb(102,102,102);
				  }
				.m-f15{
					font-size:15px
				}
				.step{
					width: 100%;
					margin-bottom:26px;
				}
				.mm0{
					margin:0px
				}
				.text{
				  width: calc(100% - 120px);
				}
				.mvm{
					vertical-align: middle
				}
				.details{
				  line-height: 23px;
				  padding-left:23px;
				  margin-left: 5px;
				  min-height:70px;
				}
				.m-tc{
				  text-align:center
				  }
				  .time-left{
					  margin-top:85px;
				  }
				  .expire-text{
					  letter-spacing:1.6px;
				  }
				  .clock{
					  border-radius:4px;
					  margin:auto;
					  background:rgb(243,247,248);
					  padding:7px 0;
					  margin-top:8px;
				  }
	
				  .value{
					  letter-spacing: 2px;
					  width:55px;
				  }
	
				  .m-f12{
					  font-size:12px;
					  letter-spacing: 0px;
					  font-weight: normal
				  }
				  .subtitle img{
					  margin-left:0px;
					  margin-top: 3px;
				  }
				  .line:before{
					  position: absolute;
					  content: '';
					  height: 57px;
					  width: 2px;
					  background: rgb(184,194,203);
					  top: -45px;
					  left: -2px;
				  }
				  .line-1{
					  border-left: 2px solid rgb(184,194,203)
				  }
				  .line-2{
					  border-left: 2px solid transparent
				  }
				  .t-br{
					  border-color:transparent;
				  }
				  .bullet:after{
					  position: absolute;
					  content: '';
					  width: 10px;
					  height: 10px;
					  background: rgb(143,150,156);
					  top: 0px;
					  left: -12px;
					  border-radius: 50%;
					  border: 6px solid white;
				  }
				  .m-footer{
					  position: fixed;
					  bottom: 0;
					  width:100%;
					  padding: 13px 18px;
				  }
				  .s-cost{
					  right:22px
				  }
				  .entered-info{
					  width:22px;
					  height:auto;
				  }
				  .hide{
					  display:none;
				  }
				  .info-box{
					top: -92px;
					right:-2px;
					left:auto;
				  }
				  .info-box:after{
					  top:auto;
					  bottom:-18px;
					  right:3px;
					  border-top-color:#ffffff;
					  border-right-color: transparent;
				  }
				  .informationWrap{
					  right:-5px;
					  top:-2px;
				  }
	
			}
	
			@media (max-width:359px){
				.xs-f10{
				  font-size:10px
				}
				.upi{
					font-size:10px;
				}
				.entered-upi{
				  font-size: 12px;
				}
				/* .info-box{
					top:115px;
					left: 55px;
				} */
				/* .info-box:after{
					top: 82px;
					bottom: -18px;
					right: 15px;
					border-top-color: #fff;
					border-right-color: transparent;
				} */
            }
           
            @media all and (min-width: 768px){
                .merchant-section{
                max-width: 800px;
                margin: 0px auto;
                padding: 20px 0;
                }
                .ms-inner{
                    display: inline-block;
                    vertical-align: top;
                    width: 100%;
                }
                .ms-d{
                    float: left;
                }
                .ms-price{
                    float: right;
                    margin-top: 12px;
                    font-size: 20px;
                    color: #222;
                    font-weight: 600;
                }
                .ms-name{
                    font-size: 20px;
                    font-weight: 600;
                    margin-bottom: 5px;
                }
                .ms-upi{
                    font-size: 13px;
                    color: #444;
                }
                .m-body{
                    box-shadow: 0px 0px 9px 0px #e0e0e07a;
                    border: 1px solid #e0e0e0;
                    position: static;
                    margin-top: 0;
                }
                .blur {
                    width: 100%;
                    height: 100vh;
                    background: #fff;
                    opacity: 0.5;
                    z-index: -1;
                    position: fixed;
                }
                .f10 {
                    font-size: 13px;
                }
                .subtitle {
                    margin-top: 7px;
                    padding-bottom: 28px;
                }
                .time span:last-child{
                    color: #012b72;
                }
                .step2 {
                    margin-left: 14%;
                }
                .text {
                    width: calc(100% - 75px);
                }
                .text img{
                    height: 109px;
                }
                .step2 img{
                    height: 67px;
                }
                .ms-d img{
                    margin-right: 20px;
                }
                .ms-d >div{
                    overflow: hidden;
                }
            }
		</style>
		<script>
			function showInfo(val){
				var infoEl = document.getElementById('info');
				if(infoEl){
					if(infoEl.className.indexOf('hide') > -1)
					{
						infoEl.className = infoEl.className.replace(/hide/g,'');
						infoEl.className = infoEl.className.replace(/  +/g, ' ');
					}
					else if(infoEl.className.indexOf('hide') == -1)
						infoEl.className += ' hide';
				}
			}
		</script>

</head>
<body onload="pollCall();">
<form NAME='frm' id="txnForm" ACTION='${pageContext.request.contextPath}/transactionStatus?${queryStringForSession}' method='post' style="display:none;">
	<input type="hidden" name="txnTransientId" value="${txnInfo.txnId}" /> <!-- For Merchant Response (compositeTxnId Add Money) -->
	<input type="hidden" name="MID" value="${merchInfo.mid}"/> <!-- Parent MID For Merchant Response -->
	<input type="hidden" name="ORDER_ID" value="${txnInfo.orderId}">
	<input type="hidden" name="TXN_AMOUNT" value="${txnInfo.txnAmount}">
	<input type="hidden" name="STATUS_INTERVAL" id="timeInt" value="${upiTransactionInfo.statusInterval}">
	<input type="hidden" name="STATUS_TIMEOUT" id="timeOut" value="${upiTransactionInfo.statusTimeOut}">
	<input type="hidden" name="STATUS_API" value="${pageContext.request.contextPath}/upi/transactionStatus">
	<input type="hidden" name="vpaID" value="${upiTransactionInfo.vpaID}">

	<input type="hidden" name="paymentMode" value="UPI" />
	<input type="hidden" name="transId" value="${txnInfo.txnId}" /> <!-- For Merchant Response (compositeTxnId Add Money) -->
	<input type="hidden" name="merchantId" value="${merchInfo.mid}"/> <!-- Parent MID For Merchant Response -->
	<input type="hidden" name="cashierRequestId" value="${upiTransactionInfo.cashierRequestId}">

	<input type="submit" value="Proceed" id="proceedForm">
</form>

<input type="hidden" id="isSelfPush" value="${upiTransactionInfo.paytmVpa}" />

<div class="main-body">
		<div class="amount-head w-hide ib">
		<c:choose>
			<c:when test="${merchInfo.useNewImagePath eq true}">
				<img src="${merchInfo.merchantImage}" height="48" class="fl"  alt="${merchInfo.merchantName}" onerror="onErrorImage(this)"/>
			</c:when>
			<c:otherwise>
				<img src="${ptm:stPath()}merchantLogo/${merchInfo.merchantImage}" height="48" class="fl"  alt="${merchInfo.merchantName}" onerror="onErrorImage(this)"/>
			</c:otherwise>
		</c:choose>
	
		 <div class="data">
			 <div class="name-sec ib">
				 <div class="main f18">
						 <div class="mer ib ml-15 fw600">${merchInfo.merchantName}</div>
						 <div class="s-cost ib fw600 pos-a">
								 <svg height="13" enable-background="new 0 0 401.998 401.998" version="1.1" viewBox="0 0 402 402" xml:space="preserve" xmlns="http://www.w3.org/2000/svg">
									 <path d="m326.62 91.076c-1.711-1.713-3.901-2.568-6.563-2.568h-48.82c-3.238-15.793-9.329-29.502-18.274-41.112h66.52c2.669 0 4.853-0.856 6.57-2.565 1.704-1.712 2.56-3.903 2.56-6.567v-29.128c0-2.666-0.855-4.853-2.56-6.567-1.719-1.71-3.903-2.569-6.572-2.569h-237.54c-2.666 0-4.853 0.859-6.567 2.568-1.709 1.714-2.568 3.901-2.568 6.567v37.972c0 2.474 0.904 4.615 2.712 6.423s3.949 2.712 6.423 2.712h41.399c40.159 0 65.665 10.751 76.513 32.261h-117.91c-2.666 0-4.856 0.855-6.567 2.568-1.709 1.715-2.568 3.901-2.568 6.567v29.124c0 2.664 0.855 4.854 2.568 6.563 1.714 1.715 3.905 2.568 6.567 2.568h121.92c-4.188 15.612-13.944 27.506-29.268 35.691-15.325 8.186-35.544 12.279-60.67 12.279h-31.977c-2.474 0-4.615 0.905-6.423 2.712-1.809 1.809-2.712 3.951-2.712 6.423v36.263c0 2.478 0.855 4.571 2.568 6.282 36.543 38.828 83.939 93.165 142.18 163.02 1.715 2.286 4.093 3.426 7.139 3.426h55.672c4.001 0 6.763-1.708 8.281-5.141 1.903-3.426 1.53-6.662-1.143-9.708-55.572-68.143-99.258-119.15-131.04-153.03 32.358-3.806 58.625-14.277 78.802-31.404 20.174-17.129 32.449-39.403 36.83-66.811h47.965c2.662 0 4.853-0.854 6.563-2.568 1.715-1.709 2.573-3.899 2.573-6.563v-29.121c0-2.669-0.858-4.856-2.573-6.57z"/>
								 </svg><span class="txn-amount"></span>
						 </div>
				 </div>
				 <div class="upi f12 ml-15 ls-05">UPI Address: ${upiTransactionInfo.merchantVpaTxnInfo.maskedMerchantVpa}</div>
			 </div>
		 </div>
		</div>
	 <div class="m-wrapper">
         <div class="blur m-hide"></div>
         <section class="m-hide merchant-section">
                <div class="ms-inner">
                    <div class="ms-d">
                        <c:choose>
                        <c:when test="${merchInfo.useNewImagePath eq true}">
                            <img src="${merchInfo.merchantImage}" height="48" class="fl"  alt="${merchInfo.merchantName}" onerror="onErrorImage(this)"/>
                        </c:when>
                        <c:otherwise>
                            <img src="${ptm:stPath()}merchantLogo/${merchInfo.merchantImage}" height="48" class="fl"  alt="${merchInfo.merchantName}" onerror="onErrorImage(this)"/>
                        </c:otherwise>
                    </c:choose>
                    <div>
                        <div class="ms-name">${merchInfo.merchantName}</div>
                        <span class="ms-upi">UPI address: ${upiTransactionInfo.merchantVpaTxnInfo.maskedMerchantVpa}</span>
                    </div>
                    </div>
                    <div class="ms-price">
						<svg height="15" enable-background="new 0 0 401.998 401.998" version="1.1" viewBox="0 0 402 402" xml:space="preserve" xmlns="http://www.w3.org/2000/svg">
									 <path d="m326.62 91.076c-1.711-1.713-3.901-2.568-6.563-2.568h-48.82c-3.238-15.793-9.329-29.502-18.274-41.112h66.52c2.669 0 4.853-0.856 6.57-2.565 1.704-1.712 2.56-3.903 2.56-6.567v-29.128c0-2.666-0.855-4.853-2.56-6.567-1.719-1.71-3.903-2.569-6.572-2.569h-237.54c-2.666 0-4.853 0.859-6.567 2.568-1.709 1.714-2.568 3.901-2.568 6.567v37.972c0 2.474 0.904 4.615 2.712 6.423s3.949 2.712 6.423 2.712h41.399c40.159 0 65.665 10.751 76.513 32.261h-117.91c-2.666 0-4.856 0.855-6.567 2.568-1.709 1.715-2.568 3.901-2.568 6.567v29.124c0 2.664 0.855 4.854 2.568 6.563 1.714 1.715 3.905 2.568 6.567 2.568h121.92c-4.188 15.612-13.944 27.506-29.268 35.691-15.325 8.186-35.544 12.279-60.67 12.279h-31.977c-2.474 0-4.615 0.905-6.423 2.712-1.809 1.809-2.712 3.951-2.712 6.423v36.263c0 2.478 0.855 4.571 2.568 6.282 36.543 38.828 83.939 93.165 142.18 163.02 1.715 2.286 4.093 3.426 7.139 3.426h55.672c4.001 0 6.763-1.708 8.281-5.141 1.903-3.426 1.53-6.662-1.143-9.708-55.572-68.143-99.258-119.15-131.04-153.03 32.358-3.806 58.625-14.277 78.802-31.404 20.174-17.129 32.449-39.403 36.83-66.811h47.965c2.662 0 4.853-0.854 6.563-2.568 1.715-1.709 2.573-3.899 2.573-6.563v-29.121c0-2.669-0.858-4.856-2.573-6.57z"/>
						</svg><span class="txn-amount" id="txnAmount"></span>
					</div>
                </div>
         </section>
		 <div class="m-body pos-a">
			 <div class="m-header">
                 
				 <div class="m-title fw600 ls-05">
						 Complete Your Payment
				 </div>
				 <div class="subtitle f10 pos-r">
						 <span class="vt m-f15 entered-upi mvm">Entered UPI Address: ${upiTransactionInfo.vpaID} </span>
						 <div class="pos-a informationWrap">
							 <img src="${ptm:stResPath()}images/info.png" class="entered-info m-ml0 cp mvm" height="16" onclick="showInfo()"/>
							 <div id="info" class="info-box xs-text ib pos-a hide">If the UPI address belongs to your friend/ family, they will need to approve the payment request on their UPI linked bank application</div>
						</div>
						 
				 </div>
			 </div>
			 <div class="m-content">
				 <div class="steps">
					 <div class="step ib pos-r vt">
						 <div class="text fl ib">
							 <div class="badge ib f10 m-hide">
								 Step 1
							 </div>
								<c:set var="upi_appName" value="UPI linked Bank/ UPI mobile application"> </c:set>
								<c:if test="${!empty upiTransactionInfo && !empty upiTransactionInfo.upiHandleInfo && !empty upiTransactionInfo.upiHandleInfo.upiAppName}">
									<c:set var="upi_appName" value="${upiTransactionInfo.upiHandleInfo.upiAppName} mobile app"> </c:set>
								</c:if>
								 <div class="details fw600 m-f15 bullet pos-r line-1" style="padding-right: 10px;">
									<span> Go to the ${upi_appName} </span>
							 </div>
						 </div>

						<c:set var="svgOrPng" value="svg"> </c:set>
						<c:set var="upi_imageName" value="upi_default"> </c:set>
						<c:if test="${!empty upiTransactionInfo && !empty upiTransactionInfo.upiHandleInfo && upiTransactionInfo.upiHandleInfo.upiImageName eq 'paytm'}">
							<c:set var="svgOrPng" value="png"> </c:set>
						</c:if>
						<c:if test="${!empty upiTransactionInfo && !empty upiTransactionInfo.upiHandleInfo && !empty upiTransactionInfo.upiHandleInfo.upiImageName}">
							<c:set var="upi_imageName" value="${upiTransactionInfo.upiHandleInfo.upiImageName}"> </c:set>
						</c:if>

						 <c:if test="${upi_imageName eq 'upi_default'}">
							 <c:set var="svgOrPng" value="png"> </c:set>
						 </c:if>

						 <img src="${ptm:stResPath()}images/${upi_imageName}.${svgOrPng}" class="fr step-img"  height="99" />
					 </div>
					 <div class="step step2 ib pos-r vt mm0">
							 <div class="text fl ib">
								 <div class="badge ib f10 m-hide">
									 Step 2
								 </div>
								 <div class="details fw600 m-f15 line bullet pos-r line-2">
										<span> Check pending requests and approve payment by entering UPI PIN</span>
								 </div>
							 </div>
							 <img src="${ptm:stResPath()}images/upipin.png" class="fr  step-img" height="57" />
						 </div>
				 </div>
				 <div class="time-left w-hide m-tc">
					 <div class="f10 expire-text">PAGE EXPIRES IN</div>
					 <div class="clock ib">
						 <div class="ib">
							 <div class="minLeft value fw600"></div>
							 <div class="unit f10">MIN</div>
						 </div>
						 <div class="vt ib">:</div>
						 <div class="ib">
							 <div class="secLeft value fw600"></div>
							 <div class="unit f10">SEC</div>
						 </div>
					 </div>
				 </div>
			 </div>
			 <div class="m-footer f10 fw600 m-tc">
				 <div class="ib m-f12 xs-f10">
						 Please do not press back button until payment is completed
				 </div>
				 <div class="time fr ib m-hide">
					 <span>Page Expires in </span>
					 <span><b><span class="minLeft"></span> Mins : <span class="secLeft"></span> Sec</b></span>
				 </div>
			 </div>
		 </div>
		</div>
		</div>
	<script>
		var isSelfPush;
		var timeOut=parseInt(document.getElementById("timeOut").value);
		var timeInterval=parseInt(document.getElementById("timeInt").value);
		var minElems = document.getElementsByClassName('minLeft');
		var secElems = document.getElementsByClassName('secLeft');

		(function initTimer(){

				var timeInMin = (timeOut/60000);
				var minLeft = timeInMin < 10 ? "0" + timeInMin : timeInMin;
				var secLeft = '00';

				if(minElems && minElems.length > 0 && secElems && secElems.length > 0){
					for(var i=0;i<minElems.length;i++)
					{
						minElems[i].innerHTML = minLeft;
					}
					for(var i=0;i<secElems.length;i++)
					{
						secElems[i].innerHTML = secLeft;
					}
				}
		})();

			function updateTimeCount(){
				var totalTime = (timeOut / 1000);          //convert ms to sec
        		const time = setInterval(()=>{

				const calcMins = parseInt(totalTime / 60, 10)
				const calcSecs = parseInt(totalTime % 60, 10);

				var minLeft = (calcMins < 10) ? "0" + calcMins : calcMins;
				var secLeft = (calcSecs < 10) ? "0" + calcSecs : calcSecs;

				if(minElems && minElems.length > 0 && secElems && secElems.length > 0){
						for(var i=0;i<minElems.length;i++)
						{
							minElems[i].innerHTML = minLeft;
						}
						for(var i=0;i<secElems.length;i++)
						{
							secElems[i].innerHTML = secLeft;
						}
				}
				if (--totalTime < 0) {
						clearInterval(time);
					}
				}, 1000);
			}

        	function getEncodedQs(obj){
            	var arr = [];
				if(!obj){
					return '';
				}
				for(var key in obj) {
					if(obj.hasOwnProperty(key)){
						arr.push(key + "=" + window.encodeURIComponent(obj[key]));
					}
				}
				return arr.join('&');
        	}


        function ajaxCall(){

				var mid = document.getElementsByName('MID')[0].value;
				var orderID=document.getElementsByName('ORDER_ID')[0].value;
				var apiURL=document.getElementsByName('STATUS_API')[0].value;
				var transID = document.getElementsByName('transId')[0].value;
				var cashierRequestID = document.getElementsByName('cashierRequestId')[0].value;
				var paymentMode = "UPI";

				var JSONData = {
					'merchantId': mid,
					'orderId': orderID,
					'transId': transID,
					'cashierRequestId' : cashierRequestID,
					'paymentMode' : paymentMode
				};

				var xhttp = new XMLHttpRequest();
				xhttp.open('POST', apiURL);
				xhttp.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
				xhttp.send(getEncodedQs(JSONData));

				xhttp.onreadystatechange = function() {
                  if(this.readyState == 4 && this.status == 200) {
					  var response = JSON.parse(this.responseText);
					  if(response.POLL_STATUS && response.POLL_STATUS === "STOP_POLLING") {
						  	document.getElementById("txnForm").submit();
					  }
				  }
				}
			}

			function checkSelfPushFlag(){
				return isSelfPush;
			}

			function pollCall(){
				var isSelfPushElem = document.getElementById('isSelfPush');
				if(isSelfPushElem){
					isSelfPush = (isSelfPushElem.value) ? true : false;
				}

				//start timer for polling
				updateTimeCount();

				//call polling
				var startTimeout=setInterval(ajaxCall, timeInterval);

				setTimeout(function(){
					clearInterval(startTimeout);
					document.getElementById('txnForm').submit();
				}, timeOut);
			}

			function onErrorImage(context) {
			    if(context){
                    context.style.display = "none";
                }
            }

            function numberFormat(amount) {
				var eles = document.getElementsByClassName("txn-amount"),
					numbers,output;

				try{
                    numbers = window.parseFloat(amount).toFixed(2).split('.');
                    output = window.parseInt(numbers[0]).toLocaleString('en-in');

                    if(numbers.length > 1 && window.parseInt(numbers[1])){
                        output += "." + numbers[1];
                    }
                }catch (e){
				    output = amount;
                }finally {
				    if(eles) {
				        for(var i = 0 ; i < eles.length ; i++){
                            eles[i].innerHTML = output;
						}
                    }
                }
            }

        	numberFormat("${upiTransactionInfo.transactionAmount}");
		</script>
</body>
</html>