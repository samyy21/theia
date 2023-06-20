<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="cardType">1</c:set>
<c:set var="txnId">${sessionScope.txnTransientId}</c:set>
<!DOCTYPE html>
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

  <link rel="shortcut icon" href="https://secure.paytm.in/app/images/Paytm.ico" type="image/x-icon">

  <base href="${pageContext.request.contextPath}/" />

  <link href="css/ccpayment.css" rel="stylesheet" type="text/css">
  <link href="css/rs.css" rel="stylesheet" type="text/css">

  <style type="text/css">
    input.styled {
      display:  none;
    }
    select.styled {
      position:  relative;
      width:  116px;
      opacity:  0;
      filter:  alpha(opacity=0);
      z-index:  5;
    }
    .disabled {
      opacity:  0.5;
      filter:  alpha(opacity=50);
    }
  </style>
<c:choose>
  <c:when test="${1 == cardType}">
  <script src="js/radio.js" type="text/javascript"></script>
  <script src="js/jquery.js" type="text/javascript"></script>
  <script src="js/data.js" type="text/javascript"></script>
  <script type="text/javascript">
    var txnId = "${txtId}";
    var pageType = "newcc";
    function cancelTxn()
    {
      document.forms.namedItem("3DSecureProcess").action="PaymentCancellation.action";
      document.forms.namedItem("3DSecureProcess").submit();
    }
    function disableCtrlKeyCombination(e)
    {
      var forbiddenKeys = new Array("a", "n", "c", "x", "v", "j");
      var key;
      var isCtrl;
      if(window.event)
      {
        key = window.event.keyCode;
        //IE
        if(window.event.ctrlKey)
        isCtrl = true;
        else
        isCtrl = false;
      }
      else
      {
        key = e.which;
        //firefox
        if(e.ctrlKey)
        isCtrl = true;
        else
        isCtrl = false;
      }
      if(isCtrl)
      {
        for(i=0;
        i<forbiddenKeys.length;
        i++)
        {
          if(forbiddenKeys[i].toLowerCase() == String.fromCharCode(key).toLowerCase())
          {
            alert('Key combination CTRL + '+String.fromCharCode(key)+ 'has been disabled.');
            return false;
          }
        }
      }
      return true;
    }
    function backButtonOverride()
    {
      setTimeout("backButtonOverrideBody()", 1);
    }
    function backButtonOverrideBody()
    {
      try
      {
        history.forward();
      }
      catch (e)
      {
      }
      setTimeout("backButtonOverrideBody()", 500);
    }
    history.forward(0);
    function right(e)
    {
      if (navigator.appName == 'Netscape' &&(e.which == 3 || e.which == 2))
      return false;
      else if (navigator.appName == 'Microsoft Internet Explorer' &&(event.button == 2 || event.button == 3))
      {
        alert("Mouse Right Click Disabled.");
        return false;
      }
      return true;
    }
    document.onmousedown=right;
    document.onmouseup=right;
    if (document.layers) window.captureEvents(Event.MOUSEDOWN);
    if (document.layers) window.captureEvents(Event.MOUSEUP);
    window.onmousedown=right;
    window.onmouseup=right;
    var showRow = (navigator.appName.indexOf("Internet Explorer") != -1) ? "block" : "table-row";
    function showCvv()
    {
      if(document.getElementById("ccNumber").value.length <18)
      {
        document.getElementById("hideMonth").style.display=showRow;
        if(document.getElementById("idcvv")!=null)
        document.getElementById("idcvv").style.display=showRow;
        if(document.getElementById("idcvvImg")!=null)
        document.getElementById("idcvvImg").style.display=showRow;
        if(document.getElementById("idstoreCC")!=null)
        document.getElementById("idstoreCC").style.display=showRow;
      }
      else
      {
        if(document.getElementById("idstoreCC")!=null)
        document.getElementById("idstoreCC").style.display="none";
      }
    }
    function isNumberKey(evt)
    {
      var charCode = (evt.which) ? evt.which : event.keyCode;
      if (charCode > 31 && (charCode < 48 || charCode > 57))
      return false;
      return true;
    }
  </script>
  </c:when>
  <c:when test="${2 == cardType}">
  <script src="js/radio.js" type="text/javascript"></script>
  <script type="text/javascript">
    function cancelTxn()
    {
      document.forms.namedItem("3DSecureProcess").action="PaymentCancellation.action";
      document.forms.namedItem("3DSecureProcess").submit();
    }
    function disableCtrlKeyCombination(e)
    {
      //list all CTRL + key combinations you want to disable
      var forbiddenKeys = new Array("a", "n", "c", "x", "v", "j");
      var key;
      var isCtrl;
      if(window.event)
      {
        key = window.event.keyCode;
        //IE
        if(window.event.ctrlKey)
        isCtrl = true;
        else
        isCtrl = false;
      }
      else
      {
        key = e.which;
        //firefox
        if(e.ctrlKey)
        isCtrl = true;
        else
        isCtrl = false;
      }
      //if ctrl is pressed check if other key is in forbidenKeys array
      if(isCtrl)
      {
        for(i=0;
        i<forbiddenKeys.length;
        i++)
        {
          //case-insensitive comparation
          if(forbiddenKeys[i].toLowerCase() == String.fromCharCode(key).toLowerCase())
          {
            alert('Key combination CTRL + '+String.fromCharCode(key)+ 'has been disabled.');
            return false;
          }
        }
      }
      return true;
    }
    function backButtonOverride()
    {
      setTimeout("backButtonOverrideBody()", 1);
    }
    function backButtonOverrideBody()
    {
      try
      {
        history.forward();
      }
      catch (e)
      {
        // OK to ignore 
      }
      setTimeout("backButtonOverrideBody()", 500);
    }
    history.forward(0);
    /* To prevent right click of mouse */
  </script>
  </c:when>
</c:choose>
</head>
<body onload="backButtonOverride();">
  <div id="header-full">
    <div id="header">
      <div id="logo">
        <a href="#" onclick="cancelTxn();">
          <img src="images/paytm-logo.gif" alt="Paytm" title="Paytm">
        </a>
      </div>
    </div>
  </div>
  <div id="container">
    <h2 class="blu">
      Order ready to be processed for
      <span class="WebRupee">
        Rs.
      </span>
      1. Please complete your payment details:
    </h2>
    <div class="gray_container">
      <div class="tab_container">
        <div id="midd">
          <form id="3DSecureProcess" name="3DSecureProcess" action="submitTransaction" method="post">
            <div class="query">
<c:choose>
  <c:when test="${1 == cardType}">
              <div class="padd">
                <label>We accept</label>
                <img src="images/vcards.png">
              </div>
  </c:when>
  <c:when test="${2 == cardType}">
              <div style="height:30px;"></div>
  </c:when>
</c:choose>
              <div class="fl gray">
                <div style="width:650px;" class="fl">
                  <div style="padding:10px 0;">
                    <label>Card Number</label>
<c:set var="maxLength">16</c:set>
<c:set var="onBlur">onblur="showCvv();"</c:set>
<c:set var="cardImage"></c:set>
<c:choose>
  <c:when test="${2 == cardType}">
    <c:set var="maxLength">19</c:set>
    <c:set var="onBlur"></c:set>
    <c:set var="cardImage"><img src="images/amex.png" /></c:set>
  </c:when>
</c:choose>
					<!-- name="ccHistory.ccNumber" -->
                    <input
                      maxlength="${maxLength}" 
                      name="cardNumber"
                      onkeypress="return disableCtrlKeyCombination(event);" 
                      onkeydown="return disableCtrlKeyCombination(event);"
                      type="text"
                      ${onBlur}
                      />
                    <span class="overlay2">${cardImage}</span>
                    <div class="error"></div>
                  </div>
                  <div style="padding:10px 0;">
                    <label>Expiry date</label>
                    <!-- ccHistory.ccExpiryMonth -->
                    <select name="expiryMonth">
                      <option value="0" selected="selected">month</option>
                      <option value="01">01</option>
                      <option value="02">02</option>
                      <option value="03">03</option>
                      <option value="04">04</option>
                      <option value="05">05</option>
                      <option value="06">06</option>
                      <option value="07">07</option>
                      <option value="08">08</option>
                      <option value="09">09</option>
                      <option value="10">10</option>
                      <option value="11">11</option>
                      <option value="12">12</option>
                    </select>
                    <!-- ccHistory.ccExpiryYear -->
                    <select name="expiryYear">
                      <option value="0" selected="selected">year</option>
                      <option value="2012">2012</option>
                      <option value="2013">2013</option>
                      <option value="2014">2014</option>
                      <option value="2015">2015</option>
                      <option value="2016">2016</option>
                      <option value="2017">2017</option>
                      <option value="2018">2018</option>
                      <option value="2019">2019</option>
                      <option value="2020">2020</option>
                      <option value="2021">2021</option>
                      <option value="2022">2022</option>
                      <option value="2023">2023</option>
                      <option value="2024">2024</option>
                      <option value="2025">2025</option>
                      <option value="2026">2026</option>
                      <option value="2027">2027</option>
                      <option value="2028">2028</option>
                      <option value="2029">2029</option>
                      <option value="2030">2030</option>
                      <option value="2031">2031</option>
                      <option value="2032">2032</option>
                      <option value="2033">2033</option>
                      <option value="2034">2034</option>
                      <option value="2035">2035</option>
                      <option value="2036">2036</option>
                      <option value="2037">2037</option>
                      <option value="2038">2038</option>
                      <option value="2049">2049</option>
                    </select>
                    <div class="error"></div>
                  </div>
<c:choose>
  <c:when test="${cardType < 2}">
                  <div id="idcvv">
                    <label class="fl" style="margin-top:10px;"> CVV</label>
                    <!-- name="ccHistory.cvv" -->
                    <input maxlength="4" name="cvvNumber" autocomplete="OFF" oncopy="return false" onpaste="return false" onkeypress="return disableCtrlKeyCombination(event);" onkeydown="return disableCtrlKeyCombination(event);" class="fl" style=" width:100px; margin:10px 10px 0 0;" type="password">
                    <div class="fl cvv">
                      <a href="#" class="tooltip">
                        What is CVV?
                        <span>
                          <img class="callout" src="WebPaymentRequest.action_files/callout_black.png">
                          <div class="fl" style="width:50px;">
                            <img src="WebPaymentRequest.action_files/cvv.png">
                          </div>
                          <div class="fr" style="width:180px;">
                            CVV Number is the last 3 digits printed at the signature panel on the back of your card.
                          </div>
                        </span>
                      </a>
                      <div style="color: #AAAAAA; font: 13px'Open Sans', Helvetica,Arial,Verdana,sans-serif; font-style: italic;" id="maestroIdcvv">
                        (Optional for Maestro Cards)
                      </div>
                    </div>
                    <div class="clear" style="padding:0;margin:0;"></div>
                    <div class="error"></div>
                  </div>
  </c:when>
  <c:when test="${cardType == 2}">
                  <div id="idcvv">
                    <label class="fl" style="margin-top:10px;">Security Code</label>
                    <input maxlength="4" name="ccHistory.cvv" autocomplete="OFF" oncopy="return false" onpaste="return false" onkeypress="return disableCtrlKeyCombination(event);" onkeydown="return disableCtrlKeyCombination(event);" class="fl" style=" width:100px; margin:0 10px 20px 0;" type="password">
                    <div class="fl cvv1">
                      <a href="#" class="tooltip">
                        What is Security Code?
                        <span>
                          <img class="callout" src="images/callout_black.png" />
                          <div class="fl" style="width:50px;">
                            <img src="images/security-code.jpg" />
                          </div>
                          <div class="fr" style="width:180px;">
                            Secure code is a four digits code printed at top of Amex logo on your card.
                          </div>
                        </span>
                      </a>
                    </div>
                    <div class="error" style="float: left;"></div>
                  </div>
  </c:when>
</c:choose>
                  <input name="ccHistory.id" value="" id="3DSecureProcess_ccHistory_id" type="hidden">
                  <div class="clear"></div>
                </div>
                <div class="fr lock">
                  <img title="Secured" alt="Secured" src="images/secured.png">
                  <br>
                  Your card details are secured via 128 Bit encryption by Verisign.
                </div>
                <div class="clear"></div>
                <div class="white">
                  <div id="idstoreCC">
                    <ul class="chk_btn">
                      <li>
                        <span style="background-position: 0px 0px;" class="checkbox"></span>
                        <input value="Y" name="ccHistory.storeCardFlag" id="checkli" class="styled" type="checkbox">
                        <span class="chek">
                          Save this card for future transaction.
                        </span>
                      </li>
                    </ul>
                  </div>
                  <div>
                    <input id="btnSubmit" class="submit" title="Pay Now" value="Pay Now" name="submit1" border="0" type="submit">
                    <input id="btnSubmit2" class="cancelButton" title="Cancel" value="Cancel" onclick="cancelTxn();" name="submit2" border="0" type="button">
                  </div>
                  <div class="clear"></div>
                </div>
              </div>
            </div>
          </form>
        </div>
        <!-- Rounded box end-->
      </div>
    </div>
  </div>
  <!--Footer-->
  <div class="clear"></div>
  <div class="footer-query-top"></div>
  <div id="footer" class="footer-query">
    <div class="fl">
      <table title="Click to Verify - This site chose VeriSign SSL for secure e-commerce and confidential communications." style="float: left" align="center" border="0" cellpadding="0" cellspacing="0">
        <tbody>
          <tr>
            <td align="left" valign="top">
              <script src="js/getseal"></script>
              <a href="javascript:vrsn_splash()" tabindex="-1">
                <img name="seal" src="images/getseal.gif" oncontextmenu="return false;" alt="Click to Verify - This site has chosen an SSL Certificate to improve Web site security" border="true">
              </a>
            </td>
          </tr>
        </tbody>
      </table>
      <img src="images/verified-visa.png" class="LR" title="Verified by Visa" alt="Verified by Visa" style="padding-top: 10px;">
      <img src="images/master-card.png" class="LR" alt="Master Card" title="Master Card">
      <img src="images/pci.png" alt="PCI" title="PCI">
    </div>
    <div class="fr footer-left">
      � 2012-2013 Paytm.com is powered by Paytm payments.
    </div>
  </div>
</body>
</html>
