<%--
  Created by IntelliJ IDEA.
  User: rajansingh
  Date: 20/12/17
  Time: 9:50 AM
  To change this template use File | Settings | File Templates.
--%>
<%-- custom css for different merchants --%>
<c:if test="${themeInfo.subTheme eq 'airtel'}">
  <style>
    .btn-submit input {
      background-image: url("${ptm:stResPath()}images/web/merchant5/sprite_compressed_airtel.png") !important;
    }
  </style>
</c:if>


<c:if test="${themeInfo.subTheme eq 'paytmIframe'}">
  <link rel="stylesheet" type="text/css" href="${ptm:stResPath()}css/web/merchant5/style.paytmIframe.css" />
</c:if>

<c:if test="${themeInfo.subTheme eq 'recharge'}">
  <style>
    .deleteCard {
      display: block;
    }

    .c {
      background-image:url(${ptm:stResPath()}images/web/merchant5/cc-paytm.png);
      background-position:287px;
    }
  </style>
</c:if>

<c:if test="${themeInfo.subTheme eq 'ndtv'}">
  <style>
    .cards-control .card.active {
      border-color: #da0000;
      background: #da0000;
    }

    .card.active {
      border-color: #da0000;
    }

    .btn-submit input.btn-normal {
      background: #da0000 !Important;
    }

    .cancel, .blue-text, .blue-text * {
      color: #da0000;
    }

    a:hover, a.active {
      color: #da0000;
    }

    #header {
      border-bottom: 2px #da0000 solid;
    }

    .text-input:focus {
      border-color: #da0000;
      box-shadow: 0px 0px 2px #da0000;
    }

    #login-box .alert-info {
      background: #f3f3f3;
      border: 1px solid #D1D1D1;
    }

    #merchant-logo {
      margin-top: 15px;
    }

    .md-modal .md-content {
      border: 1px solid #da0000;
    }

    .md-modal .closePop {
      background-image: url(${ ptm : stResPath()}images/web/merchant5/sprite_ndtv.png);
    }
  </style>
</c:if>


<c:if test="${themeInfo.subTheme eq 'dishtv'}">
  <style>
    .tick {
      background:
      url("${ptm:stResPath()}images/web/merchant5/sprite_dishtv.png")
      no-repeat -209px -67px;
    }
    .cb-icon-check {
      background-position: -209px -40px;
    }
    .md-modal .closePop {
      background:
      url("${ptm:stResPath()}images/web/merchant5/sprite_dishtv.png")
      no-repeat;
      background-position: -168px -69px;
    }

    .blue-text, .blue-text * {
      color: #f15a22;
    }

    .blue-text-2, .blue-text-2 * {
      color: #f15a22;
    }

    .cancel {
      color: #f15a22;
    }

    #header, .card.active {
      border-color: #f15a22;
    }

    .cards-control .card.active {
      border-color: #f15a22;
      background: #f15a22;
    }

    a:hover, a.active {
      color: #f15a22;
    }

    .text-input:focus {
      border-color: #f15a22;
      box-shadow: 0px 0px 2px #f15a22;
    }
  </style>
</c:if>
<c:choose>
  <c:when
          test="${themeInfo.subTheme eq 'indiamart' || themeInfo.subTheme eq 'justeat'}">
    <style>
      #login-box {
        display: none;
      }

      #paytm-wallet_tab {
        display: block;
      }
    </style>
  </c:when>
  <c:otherwise>
    <style>
      #login-box {
        display: block;
      }

      #paytm-wallet_tab {
        display: none;
      }
    </style>
  </c:otherwise>
</c:choose>
<c:if test="${themeInfo.subTheme  eq 'goair'}">
  <style>
    .container-background {
      background:
      url("${ptm:stResPath()}images/web/merchant5/bg_without_plane_new.jpg")
      no-repeat;
    }

    .container-pad {
      padding: 1px 15px 15px 15px;
      background-color: rgba(255, 255, 255, 0.46);
    }

    .mb6 {
      margin-bottom: 6px;
    }

    .cards-control .card.active {
      border-color: #21307e;
      background: #21307e;
    }

    .paytmcash-card.active, .card {
      border-color: #fff;
      background-color: #fff;
    }

    .control-group.card.active {
      border-color: #00d7ff;
    }

    .control-group.card {
      border-color: #cccccc;
    }

    .btn-submit input.btn-normal {
      background: #21307e !important;
    }
  </style>
</c:if>
<c:if test="${themeInfo.subTheme  eq 'limeroad'}">

  <style>
    .cards-control .card.active, .btn-submit input.btn-normal {
      background: #c13c61 !Important;
      border: #c13c61 1px solid;
    }

    .card.active {
      border: #c13c61 1px solid;
    }

    .btn-show-payment-details, .btn-hide-payment-details {
      color: #c13c61;
    }

    .text-input:focus {
      border-color: #c13c61;
      box-shadow: 0px 0px 2px #c13c61;
    }
  </style>
</c:if>
<c:if test="${themeInfo.subTheme  eq 'mts'}">
  <style>
    #sc-card, #ccStoreCardWrapper, #dcStoreCardWrapper {
      display: none;
    }
  </style>
</c:if>


<!-- Merchant3 Theme Subthemes-->

<c:if test="${themeInfo.subTheme eq 'DU_Univ'}">
  <style>
    .cards-control .card.active {
      border-color: #300030;
      background: #300030;
    }
    .btn-submit input.btn-normal
    {
      background: #300030 !important;
    }
    #login-stitch{
      background-color: #330033;
    }
    #login-stitch span, #login-stitch a
    {
      color: #fff;
    }
    .merchantTitleHeading{
      display: inline-block;
      margin-top: 32px;
      font-size: 21px;
      margin-left: 10px;
      font-weight: bold;
    }
    a:hover, a.active{color: #300030;}
  </style>
</c:if>

<c:if test="${themeInfo.subTheme eq 'torrent'}">
  <style>
    #header{
      padding-bottom:0; border-bottom: 2px #FC9500 solid;
    }
    .blue-text{
      color:#FC9500;
    }
    .cards-control .card.active {
      border-color: #EA8B02;
      background: #FC9500;
    }
    .cancel, a:hover, a.active {
      color: #FC9500;
    }
    .btn-submit input.btn-normal{
      background: #FC9500 !important;
    }
    .card.active {
      border-color: #FC9500;
    }
    .alert-info{
      border-color: rgb(255, 239, 158);
      background-color: rgb(255, 239, 158);
      color: #FC9500;
    }
  </style>
</c:if>

