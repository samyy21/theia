<%@page import="com.paytm.pgplus.theia.utils.ConfigurationUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="config.jsp"%>

<!DOCTYPE html>

<html>
<head>
<title>Payment Process</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link type="image/x-icon" rel="shortcut icon" href="https://staticgw1.paytm.in/1.4/images/web/Paytm.ico">
<style type="text/css">
html,body,div,span,applet,object,iframe,h1,h2,h3,h4,h5,h6,p,blockquote,pre,a,abbr,acronym,address,big,cite,code,del,dfn,em,font,img,ins,kbd,q,s,samp,small,strike,strong,sub,sup,tt,var,dl,dt,dd,ol,ul,li,fieldset,form,label,legend,table,caption,tbody,tfoot,thead,tr,th,td{margin:0;padding:0;border:0;outline:0;font-weight:inherit;font-style:inherit;font-size:100%;font-family:inherit;vertical-align:baseline}:focus{outline:0}body{line-height:1;color:black;background:white}ol,ul{list-style:none}table{border-collapse:separate;border-spacing:0}caption,th,td{text-align:left;font-weight:normal}blockquote:before,blockquote:after,q:before,q:after{content:""}blockquote,q{quotes:"" ""}.clear{clear:both;display:inline-block}.clear:after,.container:after{content:".";display:block;height:0;clear:both;visibility:hidden}* html .clear{height:1%}.clear{display:block}
input[type=button], input[type=submit], input[type=text] {
  -webkit-appearance: none;
  -webkit-border-radius: 0;
}
/*Webrupee*/
@font-face{font-family: 'WebRupee';src: url('/theia/resources/fonts/WebRupee.V2.1.eot');src: local('WebRupee'), url('/theia/resources/fonts/WebRupee.V2.1.ttf') format('truetype'),  url('/theia/resources/fonts/WebRupee.V2.1.woff') format('woff'), url('/theia/resources/fonts/WebRupee.V2.1.svg') format('svg');font-weight: normal;font-style: normal;}
.WebRupee{font-family: 'WebRupee';}

@font-face {
   font-family: 'OpenSans';
   font-style: normal;
   font-weight: 600;
   src: local('Open Sans Semibold'), local('OpenSans-Semibold'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/MTP_ySUJH_bn48VBG8sNSnhCUOGz7vYGh680lGh-uXM.woff) format('woff');
}
@font-face {
   font-family: 'OpenSans';
   font-style: normal;
   font-weight: 400;
   src: local('Open Sans'), local('OpenSans'), url(https://themes.googleusercontent.com/static/fonts/opensans/v6/cJZKeOuBrn4kERxqtaUH3T8E0i7KZn-EPnyo3HZu7kw.woff) format('woff');
}
body{font-family: 'OpenSans',arial,serif,sans-serif; margin: 0; padding:0;}


.fl{float: left;}
.fr{float: right;}
.clear{clear: both;}
.pdb10{
	padding-bottom: 10px;
}
 header{
  width: 1280px;
  height: 100px;
  background-color: #0f3569;
  color: #fff;
 }

 .container{
    width: 1200px;
 }
 .center-align{
    text-align: center;
 }
 .blue-bg{
  width: 1280px;
  height: 800px;
  background-color: #f6f9fa;
 }
 .confirm-payment{
  height: 22px;
  font-family: OpenSans;
  font-size: 16px;
  font-weight: bold;
  color: #ffffff;
  padding-top: 23px;
 }
 .main-content-div{
  width: 1200px;
  height: 673px;
  border-radius: 4px;
  background-color: #ffffff;
  border: solid 1px #e5eef1;
  position: absolute;
  top: 65px;
  left: 40px; 
 }

 .inner-content{
    padding-left: 30px;
    padding-top: 42px;
    float: left;
 }
 .left-content{
    float: left;
    padding-right: 30px;
    width: 600px;
    height: auto;
 }
.middle-line{
  float: left;
  height: 200px;
  border: solid 1px #ebebeb;
}
.right-content{
  float: left;
  padding-left: 30px;
  
}
.content{
  float: left;
  height: 22px;
  font-family: OpenSans;
  font-size: 16px;
  color: #222222;
  width: 100%;
  margin-bottom: 12px;
}
.service-tax-content{
  height: 17px;
  font-family: OpenSans;
  font-size: 12px;
  color: #999999;
  margin-left: 5px;
}
.horizontal-line{
  float: left;
  width: 100%;
  border: solid 1px #ebebeb;
}
.mt12{
  margin-top: 12px;
}
.b{font-weight: 600;}
.btn-contents{
  margin-top: 30px;
}
.submitBtn {
  width: 280px;
  height: 40px;
  border-radius: 2px;
  color:#fff;
  border-radius: 2px;
  font-size: 18px;
  background-color: #00b9f5;
  border: solid 1px #00b9f5;
  cursor: pointer;
  margin-left: 20px;
}
.paytm-logo{
  float: left;
  margin-left: 40px;
  padding-top: 21px;
}
.credit-text{
  height: 22px;
  font-family: OpenSans;
  font-size: 16px;
  color: #666666;
}
.credit-sub-text{
  height: 36px;
  font-family: OpenSans;
  font-size: 12px;
  line-height: 1.5;
  color: #666666;
  margin-top: 10px;
  width: 350px;
}
.another-option{
  height: 40px;
  border-radius: 2px;
  background-color: #ffffff;
  border: solid 0.5px #ebebeb;
}

.choose-another{
    text-decoration: none;
    border: 0;
    background: #fff;
    cursor: pointer;
    height: 19px;
    font-family: OpenSans;
    font-size: 14px;
    color: #00b9f5;
    padding: 9px 23px 12px 6px;
}
.img-shape{
  padding-left: 23px;
}
.trans-id{
    margin-top: 20px;
    margin-right: 50px;
    margin-bottom: 5px;
    color: #999;
    font-size: 14px;
    font-weight: 200;
    font-family: OpenSans;
}
.pr2{
	padding-right: 2px;
}
</style>
<script type="text/javascript">
function redirectPage() {
	var location = "${pageContext.request.contextPath}" + "/processTransaction"+"?MID=${txnInfo.mid}&ORDER_ID=${txnInfo.orderId}";
	window.location=location;
}
</script>
</head>
<body>
<form autocomplete="off" name="postconv-form" method="post"
		action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}">
  <input type="hidden" name="txnID" value="${txnInfo.txnId}" />
	
  <header>
    <div class="container">
      <div class="paytm-logo">
        <img src="${ptm:stResPath()}images/web/img-paytm-logo-white.png"  alt="paytm-logo"/>
      </div>
      <div class="center-align confirm-payment">Confirm Payment</div>
    </div>
    
  </header>
  <div class="blue-bg"></div>
  <div class="main-content-div"> 
  	
    <div class="inner-content">
      <div class="left-content">
      	<div class="content pdb10">
      		<div class="trans-id">
      			Transaction ID: ${txnInfo.orderId}     
    		</div>
      	</div>
      	<div class="horizontal-line"></div>
        <div class="content mt12"> 
          <div class="fl">Amount</div>
          <div class="fr"><span class="WebRupee pr2">Rs</span>${txnInfo.txnAmount}</div>
        </div>
        <div class="content"> 
          <div class="fl">Card Fee
            <span class="service-tax-content">(Incl. of service tax)</span>
          </div>
          <div class="fr"><span class="WebRupee pr2">Rs</span>${txnInfo.totalConvenienceCharges}</div>
        </div>
        <div class="horizontal-line"></div>
        <div class="content mt12"> 
          <div class="fl b">Net Amount to Pay</div>
          <div class="fr"><span class="WebRupee pr2">Rs</span>${txnInfo.totalTransactionAmount}</div>
        </div>
        <div class="horizontal-line"></div>
        <div class="btn-contents fl">
          <div class="another-option fl">
            <img src="${ptm:stResPath()}images/web/combined-shape.png" class="img-shape" alt="Back"/>
            <button type="button" onclick="redirectPage()" class="choose-another" >Choose Another Payment Option</button>
          </div>
          <div class="pay-btn fl">
            <button type="submit" class="submitBtn"> Pay <span class="WebRupee pr2">Rs</span>${txnInfo.totalTransactionAmount} </button>
          </div>

        </div>
      </div>
      <div class="middle-line">
      </div>
      <div class="right-content">
        <div class="credit-text">
          What is Card Fee?
        </div>
        <div class="credit-sub-text">
        Your card charges us higher fees for offering you benefits including credit period or reward points. 
        
        </div>
      </div>
    </div>
  </div>
  
  <input type="hidden" name="cacheCardToken" value="${cacheCardToken}">
  </form>
 
</body>
</html>
