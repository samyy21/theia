<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>

<!DOCTYPE html>
<html>

<head>
    <title>Demo</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0">
    <link href="https://fonts.googleapis.com/css?family=Roboto:400,500,700,300" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            outline: none;
            box-sizing: border-box;
        }

        body {
            font-family: 'Roboto', sans-serif;
        }

        .account-wrap {
            width: 100%;

        }

        .account-header {
            text-align: center;
            position: relative;
            padding-top: 16px;
        }

        .back-link {
            position: absolute;
            left: 7px;
            top: 16px;
            cursor: pointer;
        }

        .account-main {
            text-align: center;
            padding: 0 42px;
        }

        .status-box {
            width: 83px;
            height: 83px;
            -webkit-border-radius: 50%;
            -moz-border-radius: 50%;
            border-radius: 50%;
            margin: 67px auto 32px auto;
            position: relative;
            text-align: center;
        }

        .status-img {
            position: absolute;
            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
            margin: auto;
            max-width: 80px;
        }

        .status-heading {
            font-size: 20px;
            font-weight: bold;
            color: #000000;
        }

        .status-para {
            margin-top: 4px;
            margin-bottom: 24px;
            font-size: 15px;
            color: #000000;
            line-height: 21px;
            padding: 0 20px;
        }

        .app-store-btn {
            display: inline-block;
            font-weight: bold;
            text-align: center;
            white-space: nowrap;
            vertical-align: middle;
            border: 1px solid transparent;
            padding: 16px 15px;
            max-height: 56px;
            background: #00b9f5;
            color: #fff;
            font-size: 17px;
            border-radius: 4px;
            cursor: pointer;
            -webkit-appearance: none;
            width: 100%;
            text-decoration:none;
        }

        .red-box {
            background: #ff0000;
        }

        .green-box {
            background: #09ac63;
        }

        @media all and (max-width: 767px) {
            .account-main {
                padding: 0 16px;
            }
        }
    </style>
</head>

<body>
<div class="account-wrap">
    <div class="account-header">
        <a class="back-link" href="">
            <img src="../../resources/images/back_arrow.svg" alt="previous-link">
        </a>
        <a href="#">
            <img src="../../resources/images/paytm_logo.svg" width="84">
        </a>
    </div>
    <div class="account-main">
        <div class="status-box green-box">
            <img class="status-img" src="../../resources/images/back_arrow.svg">
        </div>
        <h3 class="status-heading">Account Confirmed</h3>
        <p class="status-para">Continue to App Store and finish the linking process</p>
        <a href=${requestScope.callBackURL} class="app-store-btn">
            Proceed
        </a>
    </div>
</div>
</body>

</html>