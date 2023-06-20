<%--
  Created by IntelliJ IDEA.
  User: rajansingh
  Date: 21/09/17
  Time: 2:04 PM
  To change this template use File | Settings | File Templates.
--%>
<!-- Paytm and Cards-->



<div id="paytmWalletNDigitalCard" class="mb50">

  <div id="cardsTabs">

    <%--  Paytm Section--%>

    <c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
      <div class="walletSwitchBox fl relative " id="paytmWalletCheckButton" style="margin-right: 0;
    width: 311px;">
        <div class="blur-overlay cod-wallet-disable" style="border-radius: 4px;
    padding-bottom: 23px;"></div>
        <div id="pc" class="fl walletCheckbox">
          <input type="checkbox" class="pcb checkbox" id="paytm-cash-checkbox" value="pc" style = "display:none"/>
        </div>
        <span>Paytm Balance</span>

        <c:if test="${empty walletInfo.paytmWalletAmount && empty walletInfo.giftVoucherAmount} "> 
         <div class="bal  small mt6 totalWalletBal lightTxt" id="totalWalletBalance" >(Available Balance <span class=" "><span class="WebRupee">Rs</span> <span class = 'amt'><fmt:formatNumber value=" ${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /></span></span>)</div>
       </c:if>
        <!-- Remove Subwallet in UI-->
        

      <!-- Paytm Wallet Amount-->
      <c:if test="${!empty walletInfo.paytmWalletAmount}">
        <div class="fr abs_pos" id = "" style="left: 39px;
    position: absolute;
    top: 34px; color:#666;">
          (Wallet Balance <span class="WebRupee" style="color:#666;">>Rs</span>
          <span id = "walletBalanceAmt" style="color:#666;"><fmt:formatNumber value="${walletInfo.paytmWalletAmount}" maxFractionDigits="2"  minFractionDigits="2" /></span>)</div>
      </c:if>

   <!-- gift wallet-->
      <c:if test="${!empty walletInfo.giftVoucherAmount}">
        <div class="fr abs_pos" id = "" style="left: 39px;
    position: absolute;
    top: 55px; color:#666;">
     (Gift Voucher Balance <span class="WebRupee" style="color:#666;">>Rs</span>
          <span id = "walletBalanceAmt" style="color:#666;"><fmt:formatNumber value="${walletInfo.giftVoucherAmount}" maxFractionDigits="2"  minFractionDigits="2" /></span>)</div>
      </c:if>

        <!--END OF WALLET-->
        <div class="clear"></div>
        <div id="no-walletTextUpdate" class="notification no-walletTextUpdate alert hide fl" style="    padding: 4px;
    font-weight: 400;
    border: none;
    color: #222;
    font-size: 12px;
    margin-top: 10px;
    padding-left: 8px;
    padding-right: 10px;
    background: #f9ffcf;
    margin-left: -5px;
    width: 302px;">You do not have sufficient balance for this transaction</div>

      </div>


    </c:if>
    <%--  Paytm Section--%>

    <%--  Payment Bank CheckBox Section--%>
    <c:if test="${!empty paymentBankBalance && loginInfo.loginFlag && (isPPBL_addMoneyEnable || isPPBL_merchantPayModeEnable)}">
        <div class="walletSwitchBox fl paytm-Bank PPBL relative addMoneyPPBL_Bank merchantPayModePPBL_Bank <c:if test="${paymentBankInactive eq true}"> paytmCC-blur-overlay</c:if>" style="width: 323px;">

            <div id="paytmPaymentBank" class="fl paytmPaymentBank" style="padding-right:8px;">

              <input type="checkbox" class="pcb checkbox" id="paytm-paymentBank-checkbox" <c:if test="${paymentBankInactive eq true || entityInfo.reseller eq true || (isPgPaymodesAvaiable eq false)}"> disabled="disabled"</c:if> value="pc" style = "display:none"/>
            </div>
            <span>Paytm Payments Bank Account</span>
            <div class="bal  small mt6 totalPaymentBankBalance lightTxt" id="totalPaymentBankBalance" >(Available Balance <span class=" "><span class="WebRupee">Rs</span> <span class = 'amt'><fmt:formatNumber value="${paymentBankBalance}" maxFractionDigits="2" minFractionDigits="2" /></span></span>)</div>

          <c:if test="${paymentBankInactive eq true && (walletInfo.walletBalance + paymentBankBalance < txnInfo.txnAmount ||
			 paymentBankBalance < txnInfo.txnAmount)}">
            <div  class="notification  alert  fl" style="    padding: 4px;
    font-weight: 400;
    border: none;
    color: #222;
    font-size: 12px;
    margin-top: 2px;
    padding-left: 8px;
    padding-right: 0px;
    background: #f9ffcf;
    margin-left: 30px;
    width: 310px;">You do not have sufficient balance for this transaction</div>
          </c:if>
        </div>



    </c:if>
    <%--  Payment Bank Wallet Section--%>

    <%--  Payment Digital Card Section--%>
      <c:if test="${!empty digitalCreditInfo.accountBalance && loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled}">
        <div class="walletSwitchBox fl relative <c:if test="${digitalCreditInfo.digitalCreditInactive eq true}"> paytmCC-blur-overlay</c:if>">

          <div id="paytmDigitalCard" class="fl DigitalCardCheckbox">
            <input type="checkbox" class="pcb checkbox" id="paytm-digitalCard-checkbox" <c:if test="${digitalCreditInfo.digitalCreditInactive eq true}"> disabled="disabled"</c:if> value="pc" style = "display:none"/>
          </div>
          <span>Paytm Postpaid</span>
          <div class="bal  small mt6 totalPaytmCardBal lightTxt" id="totalPaytmCardBalance" >(Available Limit <span class=" "><span class="WebRupee">Rs</span> <span class = 'amt'><fmt:formatNumber value="${digitalCreditInfo.accountBalance}" maxFractionDigits="2" minFractionDigits="2" /></span></span>)</div>
        <c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage && digitalCreditInfo.digitalCreditInactive}">
          <div class="notification alert-danger mt10" style="width: 300px; padding: 16px 32px; background: none;"> ${digitalCreditInfo.invalidPassCodeMessage}</div>
        </c:if>

        </div>
      </c:if>
    <%--  Payment Digital Card Section--%>

    <div class="clear"></div>
  </div>
  <div class="clear"></div>

</div>

<!-- paytm cash card -->
<c:if test="${!empty walletInfo.walletBalance && loginInfo.loginFlag && walletInfo.walletEnabled}">
  <div id="paymentBox" class="payAmount">
    <%@ include file="paytmCash.jsp"%>
    <div class="clear"></div>
  </div>
</c:if>


<!-- paytm Credit card -->
<c:if test="${!empty digitalCreditInfo.accountBalance && loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled}">
  <div class="payCreditCard hide" id="paymentCreditCardBox">
    <%@ include file="paytmCreditCard.jsp"%>
    <div class="clear"></div>
  </div>
</c:if>

<%--  Payment Bank Amount Box Section--%>


<c:if test="${!empty paymentBankBalance && loginInfo.loginFlag && (isPPBL_addMoneyEnable || isPPBL_merchantPayModeEnable)}">
  <div class="paymentBank hide PPBL addMoneyPPBL_Bank merchantPayModePPBL_Bank" id="paymentBank">
    <%@ include file="paymentBank.jsp"%>
    <div class="clear"></div>
  </div>
</c:if>