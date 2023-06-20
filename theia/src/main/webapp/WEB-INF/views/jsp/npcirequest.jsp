<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <style>
            .loader-wrapper {
                height: 100%;
                width: 100%;
                padding-bottom: 20px;
                position: absolute;
            }
            .loader-wrapper:before {
                content: "";
                position: absolute;
                left: 0;
                right: 0;
                bottom: 10px;
                height: 10px;
                width: 100%;
                background: #00b9f5;
            }
            .loader-wrapper:after {
                content: "";
                position: absolute;
                left: 0;
                right: 0;
                bottom: 0;
                height: 10px;
                width: 100%;
                background: #002e6e;
            }
            .loader-box {
                position: absolute;
                text-align: center;
                background: #ffffff;
                min-width: 200px;
                left: 0;
                right: 0;
                top: 0;
                bottom: 0;
                margin: auto;
                height: 150px;
            }
            .loader-box h3 {
                font-size: 24px;
                color: #000;
                margin: 15px 0 12px 0;
            }
            .loader-box img {
                max-width: 80px;
            }
            .loader-box p {
                font-size: 17px;
                color: #666666;
                /* max-width:232px; */
                line-height: 24px;
                margin: 0 auto;
                padding: 0 15%;
            }
            #ptm-spinner {
                width: 70px;
                height: 25px;
                text-align: center;
                margin: 0 auto;
            }
            #ptm-spinner > div {
                width: 10px;
                height: 10px;
                background: #012b71;
                border-radius: 100%;
                display: inline-block;
                animation: bounce 1s infinite ease-in-out both;
            }
            #ptm-spinner .bounce4,
            #ptm-spinner .bounce5 {
                background: #48baf5;
            }
            #ptm-spinner .bounce1 {
                animation-delay: -0.64s;
            }
            #ptm-spinner .bounce2 {
                animation-delay: -0.48s;
            }
            #ptm-spinner .bounce3 {
                animation-delay: -0.32s;
            }
            #ptm-spinner .bounce4 {
                animation-delay: -0.16s;
            }
            @keyframes bounce {
                0%,
                100%,
                80% {
                    transform: scale(0.2);
                }
                40% {
                    transform: scale(1);
                }
            }
        </style>

        <script language='javascript'>
            function onLoadSubmit(){
                document.npciForm.submit();
            }
        </script>
    </head>
    <body onload="onLoadSubmit()">

        <form name="npciForm"  id="npciForm" enctype="application/json" action="${npciUrl}" method="post">
            <input name="MerchantID" id="merchantId" type="hidden" value='${npci.merchantId}' />
            <input name="MandateReqDoc" id="mandateReqDoc" type="hidden" value='${npci.mandateReqDoc}' />
            <input name="CheckSumVal" id="checkSumVal" type="hidden" value='${npci.checkSumVal}' />
            <input name="BankID" id="bankId" type="hidden" value='${npci.bankId}' />
            <input name="AuthMode" id="authMode" type="hidden" value='${npci.authMode}' />
            <input name="SPID" id="spId" type="hidden" value='${npci.spId}' />
<!--            <input type="submit" id="npciRedirection" value="Click here to continue" />-->
        </form>

        <div class="loader-wrapper" id="ptm_wait_screen">
            <div class="loader-box">
                <div id="ptm-spinner">
                    <div class="bounce1"></div>
                    <div class="bounce2"></div>
                    <div class="bounce3"></div>
                    <div class="bounce4"></div>
                    <div class="bounce5"></div>
                </div>
                <h3>Please wait</h3>
                <p>Redirecting you to the payment page</p>
            </div>
        </div>
    </body>
</html>
