<%--
  Created by IntelliJ IDEA.
  User: rajansingh
  Date: 21/09/17
  Time: 2:05 PM
  To change this template use File | Settings | File Templates.
--%>


<div class="mt5">



<!-- Payment Bank Sufficent Sections -->
  <div id="sufficientBank">
  <div class="card paytmcash-card paybox fl">
    <div class="blur-overlay"></div>
    <div >
      <ul class="grid">
        <li>
          <div class="fl text-box">
            <div class="bal  small mt10"  >
              <span class="">Money in Your Bank Account</span>
              <div class=" mt6 lightTxt">
                <span class="titletext b WebRupee">Rs</span> <span class = 'amt titletext b'><fmt:formatNumber value="${paymentBankBalance}" maxFractionDigits="2" minFractionDigits="2" /></span>
              </div>

            </div>
            <div class="clear"></div>

          </div>
          <input type = "hidden" value = "${paymentBankBalance}" id = "totalPaytmtCardVal"/>
        </li>
        <%--  <li class="fr balance-used-box hide">
             <div class="large b mt6">
                 - <span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span>
                 <span id="balance-used"></span>
             </div>
         </li> --%>
      </ul>
    </div>

    <div class="clear"></div>

    <div class="bal  small mt6 remPaymentBankBal remTxtPos lightTxt" id="remPaymentBankBal" >Remaining balance <span class=" "><span class="WebRupee">Rs</span> <span class = 'amt'><fmt:formatNumber value="${paymentBankBalance}" maxFractionDigits="2" minFractionDigits="2" /></span></span></div>

  </div>
  <div id="sign-hybrid" class="sign"><span class="minus">-</span></div>
  <div id="paybox-hybrid" class="card paybox fl">
    <div class="small mt10">Payment to be made</div>
    <div class=" titletext mt6 b"><span class="titletext b WebRupee">Rs</span>
      <span class="totalPcfAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" />
    </span>
    </div>
    <!-- <div class="bal  small mt6" id="remWalletBal">Remaining balance <span class=" "><span class="">Rs</span> <span class = 'amt'></span></span></div> -->
  </div>

  </div>

  <div id="hybridPaymentBankTxn">

    <div id="wallet-hybrid-box" class="card paybox fl">
      <div class="small mt10">Money From Paytm</div>
      <div class=" titletext mt6 b"><span class="titletext b WebRupee">Rs</span> <fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /></div>
      <div class="bal  small remTxtPos mt6" id="remWalletBal">Remaining balance <span class=" "><span class="titletext  WebRupee">Rs</span><span class = 'amt'></span></span></div>
    </div>

    <div class="sign"><span class="minus">+</span></div>

    <div id="paytmBank-hybrid-box" class="card paybox fl">
      <div class="small mt10">Money From Saving Account</div>
      <div class=" titletext mt6 b" id="paymentBankBal"><span class="titletext b WebRupee">Rs</span> <fmt:formatNumber value="${paymentBankBalance}" maxFractionDigits="2" minFractionDigits="2" /></div>
      <div class="bal remTxtPos small mt6" id="remPaytmBankBal">Remaining balance <span class=" "><span class="titletext  WebRupee">Rs</span><span class = 'amt'></span></span></div>
    </div>



    <div id='payment-user-msg' class="exactPayment hide">
      <div class="sign"><span class="equal">=</span></div>
      <div class="paybox card fl">
        <h2 class="small mt10">
          <span class="addMoneyText hide msg">
          <div>Additional money needed in paytm</div>
          <div class="mt6 b"><span class="titletext WebRupee">Rs</span> <span class="addMoneyAmount titletext"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></div> <%-- <b><span class="${CURRENCY_CLASS}">${CURRENCY_TXT}</span> <span class="totalAmountSpan b">${txnInfo.txnAmount}</span></b> --%></span>

          <span class="hybridPaymentText hide msg">
          <div class="small">Payment to be made</div>
          <b class="fl mt6 mr20 titletext b"><span class="titletext WebRupee">Rs</span> <span class="hybridMoneyAmount  titletext">><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></span></b></span>

          <span class="fullWalletText hide msg">Uncheck Paytm to pay using other options</span>
        </h2>
        <div class="mt20"></div>
      </div>
    </div>


  </div>

  <!-- Payment Bank Sufficent Sections -->



  <div id="hybrid-mode-sign" class="sign hide"><span class="minus">-</span></div>

  <div id="hybrid-mode-paybox" class="card paybox fl hide">
    <div class="small mt10">Payment to be made</div>
    <div class=" titletext mt6 lightTxt"><span class="titletext">Rs</span> <fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2" /></div>
    <!-- <div class="bal  small mt6" id="remWalletBal">Remaining balance <span class=" "><span class="">Rs</span> <span class = 'amt'></span></span></div> -->
  </div>



  <!--  Total payment -->


  <div class="clear"></div>
  <c:if test="${!paymentBankInactive}">
    <div class=" row mt20">
      <form autocomplete="off" name="creditcard-form" method="post"  action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0">
        <input type="hidden"  name="txnMode" value= "${isSubscriptionFlow eq true ? 'PPBL' : 'NB'}" />
        <input type="hidden" name="bankCode" value="${paytmBankCode}">
        <input type="hidden" name="CSRF_PARAM" value="${csrfToken.token}" />
        <input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
        <input type="hidden"  name="AUTH_MODE" value="USRPWD" />
        <input type="hidden" name="storeCardFlag"  value="off" />


        <c:if test="${txnConfig.addMoneyFlag  && txnConfig.addAndPayAllowed}">
          <input type="hidden" name="addMoney" id="addNpay-PaymentBank" value="1" />
          <input type="hidden" name="walletAmount" id="walletAmountPPBL" value="0" />
        </c:if>

        <c:if test="${txnConfig.hybridAllowed}">
          <input type="hidden" name="walletAmount" id="hybrid-PaymentBank-walletBal" value="${walletInfo.walletBalance}" />
        </c:if>

        <div class="b mb10">To complete the payment enter your Paytm Passcode</div>
        <div class="mt10  fl">
          <input type="password" name="PASS_CODE" id="banktxtPassCode" class="digitalCreditPassCode text-input medium-input passcode <c:if test="${not empty paymentBankInvalidPassCodeMessage}"> error1</c:if>"  maxlength="4" placeholder="Enter Paytm Passcode" />

          <c:if test="${not empty blankPassCodeMsg}">
            <div class="notification alert-danger mt10" style="width: 45%;padding: 10px 24px;"> ${blankPassCodeMsg}</div>
            <script>pushGAData(false, 'exception_visible', "${blankPassCodeMsg}");</script>
          </c:if>
        </div>
        <div class="mt10 ml20 fl paymentBank" style="margin-top:-2px;">
          <input class="btn-normal blur-btn-new" type="submit" value="Proceed to Pay" data-txnmode="PPBL" onclick="pushGAData(this, 'pay_now_clicked')" style="width:280px; height: 41px;"name="">


        </div>
      </form>

      <div class="clear"></div>
    </div>
  </c:if>
  <div class="clear"></div>


</div>

<c:if test="${not empty paymentBankInvalidPassCodeMessage}">
  <div class="notification alert-danger mt10" style="width: 45%;padding: 10px 24px;"> ${paymentBankInvalidPassCodeMessage}</div>
  <script>pushGAData(false, 'exception_visible', " ${paymentBankInvalidPassCodeMessage}")</script>
</c:if>