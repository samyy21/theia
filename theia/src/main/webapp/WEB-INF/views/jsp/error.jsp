


<%@ page session="false" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Paytm Secure Online Payment Gateway</title>

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Cache-Control" content="no-cache" />
	<meta http-equiv="Expires" content="-1" />

	<meta name = "viewport" content = "width = device-width" />
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
	<link href="${ptm:stResPath()}css/web/merchant/style.css" rel="stylesheet" type="text/css" />
	<link type="image/x-icon" rel="shortcut icon" href="${ptm:stResPath()}images/web/Paytm.ico" />

	<script type="text/javascript">

	<%-- var url = '<%=request.getSession().getAttribute("errorPageUrl")%>';
	if(url!=null && url!=''){
		var ele = document.createElement("img");
		ele.src = url;
	} --%>

	function backButtonOverride(forward) {
		try {

			var ua = navigator.userAgent;
			var matches = ua.match(/^.*(iPhone|iPad).*(OS\s[0-9]).*(CriOS|Version)\/[.0-9]*\sMobile.*$/i);
			if (!(matches && matches[2] === 'OS 7' && matches[3] === 'CriOS')) {
				if(forward){
						history.forward();
				} else {
					setTimeout("backButtonOverrideBody()", 1);
				}
			}
		}
		catch (e) {}
	}

	function backButtonOverrideBody() {
		try {
			history.forward();
		}
		catch (e) {}
		setTimeout("backButtonOverrideBody()", 500);
	}

	backButtonOverride(true);

	</script>
	<style>

	@font-face {
    font-family: 'Open Sans';
    font-style: normal;
    font-weight: 700;
    src: local('Open Sans Bold'), local('OpenSans-Bold'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/k3k702ZOKiLJc3WVjuplzHhCUOGz7vYGh680lGh-uXM.woff) format('woff');
}
@font-face {
    font-family: 'Open Sans';
    font-style: normal;
    font-weight: 300;
    src: local('Open Sans Light'), local('OpenSans-Light'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/DXI1ORHCpsQm3Vp6mXoaTXhCUOGz7vYGh680lGh-uXM.woff) format('woff');
}
@font-face {
    font-family: 'Open Sans';
    font-style: normal;
    font-weight: 600;
    src: local('Open Sans Semibold'), local('OpenSans-Semibold'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/MTP_ySUJH_bn48VBG8sNSnhCUOGz7vYGh680lGh-uXM.woff) format('woff');
}
@font-face {
    font-family: 'Open Sans';
    font-style: normal;
    font-weight: 400;
    src: local('Open Sans'), local('OpenSans'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/cJZKeOuBrn4kERxqtaUH3T8E0i7KZn-EPnyo3HZu7kw.woff) format('woff');
}
body{ font-family:"Open Sans",Arial;}
	.f18, .f18 * {
		font-size: 16px;
		line-height: 22px;
	}

	.white_container {
		width : 506px;margin:auto;
	}
	@media (max-width: 767px) {
		.white_container {
			width : 100%;margin:auto;
		}
		.white_container .fr img{width:100%;}
	}
	</style>
</head>
<body onload="backButtonOverride();">
	<div id="header">
 		<ul class="container grid">
	    	<li>
	    		<div class="img img-logo" alt="Paytm Payments" title="Paytm Payments"></div>
	    	</li>
	  	</ul>
	  	<div class="clear"></div>
	</div>

	<div class="container">
		<div class="gray_container">
			<div class="white_container width-pad">
				<div style="text-align:center; margin-top:35px;"><img src="${ptm:stResPath()}images/errorPic.png" alt="" title="" /></div>
				<div style="text-align:center; margin-top:35px;">
					<div class="pad15">

						<%if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN){%>
							<div class="txt f18">
								<b class="b" style="color:#4A4A4A; margin-bottom:10px; display:block;">
									This is suspicious request, will not proceed further
								</b>
							</div>
						<%}else {%>
							<div class="txt f18">
								<b class="b" style="color:#4A4A4A; margin-bottom:10px; display:block;">Something went wrong. It may be due to any of these reasons:</b>
								<ul class="aln" style="color:#909090;">
									<li>Session expired due to inactivity</li>
									<li>Our system encountered an obstacle</li>
								</ul>
							</div>
                            <div class="txt f18"><br />
                                <b class="b" style="color:#4A4A4A; margin-bottom:10px; display:block;">You can fix it yourself! Here's how:</b>
                                <ul class="aln" style="color:#909090;">
                                    <li>Check payment status with your bank to avoid double payment</li>
                                    <li>Clear cookies &amp; temporary internet files of the browser & retry</li>
                                    <li>Launch a new browser &amp; start from the beginning</li>
                                    <li style="margin-top: 10px;">Still unable to transact? visit us at <a href="https://www.paytm.com/care" class="blue-text">paytm.com/care</a></li>
                                </ul>
                            </div>
						<%}%>
	                </div>
                </div>

           		<div class="clear"></div>
           		<br>
           		<br>
           		<br>

			</div>
		</div>
    </div>
	<!--Middle Container End-->
	<div class="clear"></div>
	<div id="footer" class="mt20">
		<div class="container">
			<div class="img img-partner-logo"></div>
		</div>
	</div>
</body>
</html>