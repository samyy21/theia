<%@ taglib prefix="ptm" uri="PaytmCustomTags" %>

<html>
<head>
    <style>
        body{margin:0; background: #F7F9FA; font-family: arial; }
        .main{width: 1000px; margin: 0 auto;}
        header{margin: 15px 0;}

        .logo{
            background: url("${ptm:stResPath()}images/web/paytm_logo.png") no-repeat;
            float: left;
            padding: 10px;
            display: inline-block;
            width: 113px;
            height: 31px;
        }
        .clear{width: 100%; clear: both;}
        section{    border-radius: 4px;
            background: #fff;
            padding: 30px;
            width: 100%;
            min-height: 500px;
            margin-top: 20px;
            box-sizing: border-box;
        }
        .expiryNotes{font-size: 15px;}
        .bold{font-weight: bold; font-size: 17px;}
        .number{    padding: 7px;
            display: inline-block;
            border-radius: 20px;
            background: #ccc;
            margin-right: 10px;
            width: 20px;
            height: 18px;
            text-align: center;}
        footer{ height: 8px; background: #4CBAF6; border-bottom: 8px #073176 solid; margin-top: 50px; padding:0 10px; width: 100%; }


    </style>
</head>
<body>
<div class="main">
    <header>
        <span class="logo"></span>
        <div class="clear"></div>
    </header>
    <section>
        <img src="${ptm:stResPath()}images/web/expiryIcon.png"/>
        <div class="expiryNotes">
            <p class="bold">Your Session has Expired!</p>
            <p class="bold">This error has occurred due to one of the following reasons:</p>
            <p><span class="number">1</span>You have used Back/Forward/Refresh button of your browser</p>
            <p><span class="number">2</span>You have kept the browser window idle for a long time</p>
        </div>
        <div class="clear"></div>
    </section>
    <footer></footer>
</div>
</body>
</html>