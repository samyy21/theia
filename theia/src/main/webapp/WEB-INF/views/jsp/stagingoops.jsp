<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ page import="com.paytm.pgplus.theia.utils.StagingParamValidatorResponse" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page session="false" %>
<!DOCTYPE html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link href="https://fonts.googleapis.com/css?family=Roboto:400,700" rel="stylesheet">
    <link rel="shortcut icon" href="https://staticpg.paytm.in/pgp/365/mobile/favicon.ico">
    <style>
        *{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        html,body{
            margin:0;
            padding: 0;
            height:100%;
            font-family: 'Roboto', sans-serif;
            color:#182223;
        }
        .header{
            text-align:center;
            border-bottom:1px solid #e6ebf3;
        }
        .logo{
            margin:0 auto;
            padding:16px 0;
            display:block;
        }
        .error-wrapper{
            width:100%;
            max-width:640px;
            margin: 0 auto;
        }
        .error-txt-box{
            text-align: center;
        }
        .error-img{
            margin-top:16px;
        }
        .error-heading{
            font-size:20px;
            line-height: 24px;
            color: #182233;
            margin-top:24px;
            margin-bottom:8px;
            font-weight:800;
        }
        .error-para{
            font-size:16px;
            line-height: 20px;
            color: #86a0c2;
            margin-bottom:32px;
            padding:0 16px;
        }
        .bg-heading{
            padding:10px 16px;
            background:#f6f8fc;
            font-size:12px;
            line-height: 16px;
            font-weight: 800;
            color:#536e92;
            letter-spacing: 0.8px;
        }
        .param-list li{
            list-style:none;
        }
        .param-list li:last-child .param-wrap{
            border-bottom:none;
        }
        .param-wrap{
            border-bottom:1px solid #e6ebf3;
            padding:20px 16px;
            position:relative;
            padding-left:48px;
        }
        .param-wrap::after{
            content: "";
            width:49px;
            height:2px;
            background:#ffffff;
            position: absolute;
            left:0;
            bottom:-1px;

        }
        .param-heading{
            font-size:14px;
            line-height: 20px;
            font-weight: bold;
            color:#182223;
        }
        .param-dscrpt{
            font-size:12px;
            line-height: 16px;
            color:#536e92;
            margin-top:4px;
            margin-bottom:0;
        }
        .cross{
            background: url('${ptm:stResPath()}images/web/paytm/ic-cancel.svg') no-repeat left 16px top 18px;
        }
        .warning{
            background: url('${ptm:stResPath()}images/web/paytm/ic-warning.svg') no-repeat left 16px top 18px;
        }
        .check{
            background: url('${ptm:stResPath()}images/web/paytm/ic-check-circle.svg') no-repeat left 16px top 18px;
        }
        .order-support-box{
            background:#f6f8fc;
            padding:16px;
            font-size:12px;
            line-height: 16px;
        }
        .order-time{
            color:#182233;
        }
        .instruction-para{
            color:#536e92;
            margin-top:4px;
        }
        .mailing-txt{
            color:#00b9f5;
            text-decoration:none;
            cursor:pointer;
        }
        .footer{
            width:100%;
            padding:30px 16px 10px 16px;
            position:relative;
        }
        .footer::before{
            content: "";
            width:100%;
            height:10px;
            background:#00b9f5;
            position:absolute;
            top:0px;
            left:0;
        }
        .footer::after{
            content: "";
            width:100%;
            height:10px;
            background:#012b72;
            position:absolute;
            top:10px;
            left:0;
        }
        .footer-list{
            display:inline;
        }
        .footer-list li{
            display:inline-block;
            list-style: none;
        }

        .footer-para{
            font-size:12px;
            line-height:16px;
            color:#86a0c2;
            display:block;
            padding-top:4px;

        }
        .footer-link{
            color:#182233;
            font-size:12px;
            line-height: 16px;
            cursor:pointer;
            text-decoration: none;
            margin-right:35px;
            display:inline-block;
        }

        @media(min-width:768px){
            .logo{
                margin:0 auto;
                padding:20px 0 20px 0;
            }
            .error-img{
                margin-top: 42px;
            }
            .error-heading{
                margin-top:40px;
            }
            .error-img{
                width:302px;
            }
            .param-wrap{
                padding-top:22px;
                padding-left:40px;
            }
            .order-support-box{
                margin-top:20px;
                margin-bottom:40px;
            }
            .cross,.warning,.check{
                background-position-x: 0px;
            }
            .footer{
                padding:40px 40px 20px 40px;
            }
            .footer-para{
                float:right;

            }

        }
    </style>
</head>
<body>
<div class="header">
    <img class="logo" src="${ptm:stResPath()}images/web/paytm/logo.svg" alt="paytm-logo"/>
</div>
<div class="error-wrapper">
    <div class="error-txt-box">
        <img src="${ptm:stResPath()}images/web/paytm/img-error-404.svg" alt="error 404" class="error-img"/>
        <h2 class="error-heading">Something went wrong</h2>
        <%
            ArrayList<StagingParamValidatorResponse> list = (ArrayList<StagingParamValidatorResponse>) request.getAttribute("paramValid");
            if(list==null){
                String mid = getMid(request);
                String orderId = getOrderId(request);%>
                <p class="error-para">The transaction request sent by you is incorrect. Please raise the issue with all details like MID <%if(!StringUtils.EMPTY.equals(mid)){%>- <%=mid%><%}%>, Order ID <%if(!StringUtils.EMPTY.equals(orderId)){%>- <%=orderId%><%}%> and order time - <%= (new Date()).toLocaleString()%> at <a class="mailing-txt" href="https://business.paytm.com/contact-us">https://business.paytm.com/contact-us</a> </p>
            <%} else { %>
                <p class="error-para">The transaction request sent by you is incorrect. Listed below are mandatory attributes required in transaction request along with debugging details. Kindly check </p>
    </div>
    <h3 class="bg-heading">DEBUG DETAILS</h3>
    <ul class="param-list">
<%
        for(StagingParamValidatorResponse resp : list) {%>

        <li>
            <div class="param-wrap
                <%if (resp.getRespStatus().equals("missing")) {%> cross <% }
                else if(resp.getRespStatus().equals("invalid")) {%> warning <% }
                else if(resp.getRespStatus().equals("correct")) {%> check <% }
                %>  ">
                <p class="param-heading"> <%=resp.getRespAttribute()%></p>
                <p class="param-dscrpt">
                    <%if(!resp.getRespStatus().equals("correct")){ %>
                    <%=resp.getRespStatus()%>: <%=resp.getRespMsg()%> <%}%></p>
            </div>
        </li>
<%      }%>
    </ul>
    <div class="order-support-box">
        <p class="order-time">Order Time: <%= (new Date()).toLocaleString()%></p>
        <p class="instruction-para">Click <a href="https://developer.paytm.com/docs" class="mailing-txt" target="_blank">here</a> for a complete list of required parameters to fix this error. If you still have trouble integrating Checkout, email us a screenshot of this page on <a class="mailing-txt" href="https://business.paytm.com/contact-us">https://business.paytm.com/contact-us</a></p>
    </div>
            <%}%>

    <%!
        public String getMid(HttpServletRequest request) {
            if(request.getParameter("MID")!=null){
                return request.getParameter("MID");
            }else if(request.getParameter("mid")!=null){
                return request.getParameter("mid");
            }
            return StringUtils.EMPTY;
        }
        public String getOrderId(HttpServletRequest request) {
            if(request.getParameter("ORDER_ID")!=null){
                return request.getParameter("ORDER_ID");
            }else if(request.getParameter("orderId")!=null){
                return request.getParameter("orderId");
            }
            return StringUtils.EMPTY;
        }
    %>
</div>
<div class="footer">
    <ul class="footer-list">
        <li><a href="#" class="footer-link">Terms of Service</a></li>
        <li><a href="#" class="footer-link">Privacy Policy</a></li>
    </ul>
    <span class="footer-para">&copy; 2018, One97 Communications Pvt. Ltd</span>
</div>

</body>