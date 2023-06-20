
<c:if test="${!loginInfo.loginDisabled}">
<%@ include file="login.jsp" %>
</c:if>


<%-- headers --%>
<div class="header row" id = "paytmCashText" style = "display:none">
    Uncheck Paytm to pay using other options.
</div>


<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
<%-- <c:if test="${empty hidePaytmCash}"> --%>
<div class="row pt20">
    <c:choose>
        <c:when test="${themeInfo.subTheme eq 'Charges' && !empty txnConfig.paymentCharges}">
            <div class="fl">Amount to be added</div>
        </c:when>
        <c:otherwise>
            <div class="fl">Total amount to be paid</div>
        </c:otherwise>
    </c:choose>
    <div class="fr"><span class="WebRupee">Rs</span> <span id = "totalAmt"><fmt:formatNumber value="${txnInfo.txnAmount}" maxFractionDigits="2" minFractionDigits="2"></fmt:formatNumber></span></div>
    <div class="clear"></div>
</div>
<%-- </c:if> --%>
<%-- headers --%>




<!-- when Paytm User is Logged In -->


<!-- Payment Bank Conf -->

<c:set var="paymentBankEnabled" value="${savingsAccountInfo.savingsAccountEnabled}"></c:set>
<c:set var="paymentBankInactive" value="${savingsAccountInfo.savingsAccountInactive}"></c:set>
<c:set var="paymentBankAccountBalance" value="${savingsAccountInfo.effectiveBalance}"></c:set>
<c:set var="paymentBankInvalidPassCodeMessage" value="${savingsAccountInfo.invalidPassCodeMessage}"></c:set>
<c:set var="paymentBankRetryCount" value="${savingsAccountInfo.paymentRetryCount}"></c:set>
<c:set var="paymentBankCode" value="${savingsAccountInfo.paymentsBankCode}"></c:set>
<c:set var="isPPBL_addMoneyEnable" value="${entityInfo.addPaymentsBankEnabled}"></c:set>
<c:set var="isPPBL_merchantPayModeEnable" value="${entityInfo.paymentsBankEnabled}"></c:set>


<c:if test="${param.use_payment_bank eq 1}">
  <c:set var="paymentBankchecked" value="true"></c:set>
</c:if>

<c:if test="${loginInfo.loginFlag}">
    <c:if test="${empty usePaytmCash || param.use_wallet eq 0}">
      <c:set var="usePaytmCash" value="false" scope="session"></c:set>
    </c:if>
</c:if>

<c:if test="${param.use_wallet eq 1}">
  <c:set var="usePaytmCash" value="true" scope="session"></c:set>
</c:if>
<%-- payment mode logic --%>
<c:set var = "isInsufficientBalance" value = "${walletInfo.walletBalance < txnInfo.txnAmount}"/>
<c:set var="isHybridAllowed" value="${txnConfig.hybridAllowed }"></c:set>

<c:set var = "isHybrid" value="false" />
<c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}">
  <c:set var = "isHybrid" value = "${walletInfo.walletBalance < txnInfo.txnAmount}"/>
</c:if>

<c:set var="selectedModeType" value="BANK"/>

<c:if test="${usePaytmCash && !isHybrid}">
  <c:set var="selectedModeType" value="WALLET_ONLY"/>
</c:if>

<c:if test="${onlyWalletEnabled}">
  <c:set var="selectedModeType" value="WALLET_ONLY"/>
</c:if>

<c:if test="${usePaytmCash && txnConfig.addMoneyFlag}">
  <c:set var="selectedModeType" value="ADD_MONEY"/>
</c:if>

<!-- Payment Postpaid Logic-->

<c:set var="hybridPostpaid" value="false"/>
<c:set var="isInsufficientPaytmCCBalance" value="${digitalCreditInfo.digitalCreditInactive}"/>
<c:set var="paytmCCchecked" value="false" scope="session"></c:set>


<%-- paytm cash logic --%>


<%-- promocode logic --%>
<c:if test="${empty promoShowAllModes}">
  <c:set var="promoShowAllModes" value="0" scope="session"></c:set>
</c:if>
<c:if test="${param.showAll eq 1}">
  <c:set var="promoShowAllModes" value="1" scope="session"></c:set>
</c:if>
<%-- promocode logic --%>

<%-- for PCF:PGP-2506  --%>
<c:if test = "${txnConfig != null &&  txnConfig.paymentCharges != null
			&& walletInfo.walletBalance >= txnInfo.txnAmount && walletInfo.walletBalance < txnConfig.paymentCharges.PPI.totalTransactionAmount}">
  <c:set var="selectedModeType" value="BANK"/>
  <c:set var= "usePaytmCash" value="false"/>
</c:if>


<c:if test="${!empty ppi}">
  <c:set var="paymentType" value="0"/>
</c:if>


<%-- payment mode logic --%>

<%-- ppi : ${ppi} |
isHybrid : ${isHybrid} |
selectedModeType : ${selectedModeType} |
paymentType : ${paymentType} |
bal : ${walletInfo.walletBalance} |
usePaytmCash : ${usePaytmCash} |
onlyWalletEnabled : ${onlyWalletEnabled} |
isInsufficientBalance : ${isInsufficientBalance}
isHybridAllowed : ${isHybridAllowed} --%>

<%-- no show wallet if bal is zero and add money is not available --%>
<c:if test="${(walletInfo.walletBalance eq 0 && !txnConfig.addMoneyFlag)  || (!empty txnInfo.promoCodeResponse && txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName eq 'DISCOUNT')}">
  <c:set var="hidePaytmCash" value="true"/>
</c:if>




<!-- paytm cash -->
<%-- <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}"> --%>
<c:if test="${!empty walletInfo.walletBalance && !empty usePaytmCash}">

  <c:set var="checkPaytmCashCheckbox" value="false"/>
  <%-- <c:if test="${usePaytmCash && walletInfo.walletBalance > 0}"> --%>
  <c:if test="${usePaytmCash && !isRetryAvaiable}">
    <c:set var="checkPaytmCashCheckbox" value="true"/>
  </c:if>

  <c:set var="disablePaytmCashCheckbox" value="false"/>
  <%-- when hybrid not allowed --%>
  <c:if test="${isInsufficientBalance && !txnConfig.addMoneyFlag && (!isHybridAllowed or walletInfo.walletBalance <= 0)}">
    <c:set var="disablePaytmCashCheckbox" value="true"/>
    <c:set var="checkPaytmCashCheckbox" value="false"/>
  </c:if>

  <%-- when wallet only --%>
  <c:if test="${onlyWalletEnabled}">
    <c:set var="disablePaytmCashCheckbox" value="true"/>
  </c:if>


  <!--   If UPI Push Enalbled  -->
  <c:if test="${usePaytmCash && param.txn_Mode eq 'COD'}">
      <c:set var="disablePaytmCashCheckbox" value="true"/>
      <c:set var="checkPaytmCashCheckbox" value="false"/>
  </c:if>

  <c:if test="${isUPI_Push}">

    <c:if test="${param.txn_Mode eq 'UPI_PUSH' && empty param.use_wallet && walletInfo.walletBalance >= txnInfo.txnAmount}">
      <c:set var="checkPaytmCashCheckbox" value="false"/>

    </c:if>

  </c:if>

  <!--  -->


  <div id="showHideWallet" class="${!empty hidePaytmCash ? 'hide' : ''}">
    <div class="row mb5 rel_pos" <c:if test="${disablePaytmCashCheckbox}">style="color:#9a9999;"</c:if>>
      <div class="" style="margin-left:6px; width: 100%; margin-bottom:11px; clear:both;">
        <input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> />
          <span class="rel_pos span_inline" <c:if test="${disablePaytmCashCheckbox eq false}">onclick="walletItemClickable(this.previousElementSibling)"</c:if>>Paytm Balance
              <c:if test="${disablePaytmCashCheckbox &&  walletInfo.walletBalance < txnInfo.txnAmount}">

                <span class="arrowIcon"></span>

                  <span class="insufficientIcon" onclick="insufficientBlance(this)">
                    <span class="tooltiptext">This payment method is disabled because your balance is insufficient for this transaction.</span>
                  </span>
              </c:if>


						</span>
        <div class="clear"></div>
        <!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm)</div> -->
        <div class="bal" id = "yourBal" >
          
            <c:if test="${!empty walletInfo.paytmWalletAmount && !empty walletInfo.giftVoucherAmount}">
                <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
               <div>(Wallet Balance <span class="WebRupee">Rs</span>
                         <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.paytmWalletAmount}" maxFractionDigits="2"  minFractionDigits="2" /></span>)
               </div></c:if>
               
               <!-- gift wallet-->
                     <c:if test="${!empty walletInfo.giftVoucherAmount}">
               <div style="margin-top:4px;">
               (Gift Voucher Balance <span class="WebRupee">Rs</span>
                         <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.giftVoucherAmount}" maxFractionDigits="2"  minFractionDigits="2" /></span>)</div>
               </c:if>
               
               <c:if test="${empty walletInfo.giftVoucherAmount}">
               (Available Balance  <span class="WebRupee">Rs</span> <span class = 'amt' id = "yourBalSpan"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2"  /></span>)</div>
                       <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
                     </div>
                     <c:if test="${usePaytmCash && (!disablePaytmCashCheckbox && walletInfo.walletBalance < txnInfo.txnAmount)}">
                       <div class="fr abs_pos" id = "walletBalance">
                          <span class="WebRupee">Rs</span>
                         <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2"  minFractionDigits="2" /></span>
                       </div>
                     </c:if>
               </c:if>

      

      <div class="clear"></div>
    </div>

      <%-- <div class="row mt5" style="display:none">
         <div class="fl">
             <b>Balance amount to be paid</b>
         </div>
         <div class="fr" >
              <b class="WebRupee">Rs</b>
             <b id = "balanceAmt">${txnInfo.txnAmount}</b>
         </div>
         <div class="clear"></div>
     </div>  --%>
    <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0 && walletInfo.walletBalance >= txnInfo.txnAmount}">
      <c:set var="submitBtnText">Pay Now</c:set>
      <%-- for post conv of wallet only --%>
      <div style="display:none;" id="post-conv-wallet-only">
        <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null &&  txnConfig.paymentCharges.PPI.totalConvenienceCharges ne '0.00'}">
          <div class="post-conv-inclusion" style="margin-right: 17px; margin-left: 7px;">
            <%@ include file="../../common/post-con/wallet-postconv.jsp" %>
          </div>
        </c:if>
      </div>


      <form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="walletForm" style = "margin:0;padding:0" onsubmit = "return submitForm()">
        <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
        <input type="hidden" name="submit_count" id="submit_count" value="0" />
        <input type="hidden"  name="txnMode" value="PPI" />
        <input type="hidden"  name="channelId" value="WAP" />
        <input type="hidden"  name="AUTH_MODE" value="USRPWD" />
        <input type="hidden" name="walletAmount" id="walletAmount" value="0" />
        <div style="display: none;" class="fullWalletDeduct" id = "onlyWalletAmt">
          <button name="" type="submit" class="blue-btn"  data-txnmode="PAYTMWALLET" onclick="pushGAData(this, 'pay_now_clicked')">${submitBtnText}</button>
        </div>
      </form>
    </c:if>
    <div class="clear"></div>
  </div>
</c:if>

<!-- paytm cash -->

<!-- Payment Bank -->
<c:set var="isPPBL_show" value="hide"></c:set>

<c:if test="${selectedModeType eq 'ADD_MONEY' && isPPBL_addMoneyEnable}">
    <c:set var="isPPBL_show" value=""></c:set>
</c:if>
<c:if test="${selectedModeType eq 'BANK' && isPPBL_merchantPayModeEnable}">
    <c:set var="isPPBL_show" value=""></c:set>
</c:if>

<c:if test="${selectedModeType eq 'WALLET_ONLY' && isPPBL_merchantPayModeEnable}">
    <c:set var="isPPBL_show" value=""></c:set>
</c:if>

<c:set var="isPPBL_Enabled" value="${(isPPBL_addMoneyEnable || isPPBL_merchantPayModeEnable) && (empty isPPBL_show)}"></c:set>

<c:if test="${isPPBL_Enabled && themeInfo.subTheme ne 'ccdc'}">
  <c:set var="isPaytmCCEnable" value="${not empty param.use_paytmcc && param.use_paytmcc eq 1}"></c:set>
  <c:set var="isPaymentBankCheckboxDisable" value="${!paymentBankInactive &&
			((usePaytmCash && ((walletInfo.walletBalance + paymentBankAccountBalance) < txnInfo.txnAmount)) ||
			(!usePaytmCash && (0 + paymentBankAccountBalance < txnInfo.txnAmount)))}">
  </c:set>

  <c:set var="isInsufficientPaymnetBankBalance" value="${(0 + paymentBankAccountBalance eq 0) || (usePaytmCash && walletInfo.walletBalance + paymentBankAccountBalance < txnInfo.txnAmount) ||
             (!usePaytmCash && (0 + paymentBankAccountBalance < txnInfo.txnAmount))}">
  </c:set>
  <c:set var="isPaymentBankCheckboxChecked" value="${param.use_payment_bank ne 0 && !paymentBankInactive  &&
			((usePaytmCash && walletInfo.walletBalance < txnInfo.txnAmount &&
				walletInfo.walletBalance + paymentBankAccountBalance >= txnInfo.txnAmount) ||
				(!usePaytmCash && (0 + paymentBankAccountBalance >= txnInfo.txnAmount))) && !isRetryAvaiable}">
  </c:set>


  <c:if test="${isPPBL_Enabled && !paymentBankInactive  &&
            isPaymentBankCheckboxChecked && (empty param.txn_Mode)&& (empty param.use_payment_bank || param.use_payment_bank eq 1) && !isRetryAvaiable}">
    <!-- When payment bank balance is sufficient hide other modes payments -->
    <c:set var="paymentType" value="17"></c:set>
  </c:if>

  <c:if test="${isPPBL_Enabled && !paymentBankInactive  &&
           isPaymentBankCheckboxChecked && !empty errorMsg &&  (empty param.use_payment_bank || param.use_payment_bank eq 1) && !isRetryAvaiable}">
    <!-- When Retry || Error MSG not Empty modes payments -->
    <c:set var="paymentType" value="17"></c:set>
  </c:if>


  <!-- If wallet has balance and used -->
  <c:if test="${usePaytmCash && isPPBL_Enabled && isPaymentBankCheckboxChecked}">
    <c:set var="amountDeductFromPaymentBank" value="${txnInfo.txnAmount - walletInfo.walletBalance}"></c:set>
  </c:if>

  <!-- If wallet has no balance -->
  <c:if test="${!usePaytmCash && isPPBL_Enabled && isPaymentBankCheckboxChecked}">
    <c:set var="amountDeductFromPaymentBank" value="${txnInfo.txnAmount}"></c:set>
  </c:if>


  <c:if test="${paytmCCchecked eq true}">
    <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
  </c:if>


  <c:if test="${(usePaytmCash && (walletInfo.walletBalance >= txnInfo.txnAmount)) &&
			(isPPBL_Enabled && !paymentBankInactive && (0 + paymentBankAccountBalance >= txnInfo.txnAmount))}">
    <c:set var="amountDeductFromPaymentBank" value="0"></c:set>
  </c:if>

    <c:if test="${(not empty param.use_wallet) && (param.use_wallet eq 0) && (not empty param.use_paytmcc && param.use_paytmcc ne 0) && (not empty param.use_payment_bank) && (param.use_payment_bank eq 0) && (digitalCreditInfo.digitalCreditInactive eq false && digitalCreditInfo.digitalCreditEnabled eq true)}">
        <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
        <c:set var="paymentType" value="16"></c:set>
    </c:if>




  </c:if>


<c:if test="${isUPI_Push}">

    <c:set var="isUpiPush_show" value="false"></c:set>

    <c:if test="${selectedModeType eq 'ADD_MONEY' && entityInfo.addUpiPushEnabled}">
        <c:set var="isUpiPush_show" value="true"></c:set>
    </c:if>

    <c:if test="${selectedModeType eq 'BANK' && entityInfo.upiPushEnabled}">
        <c:set var="isUpiPush_show" value="true"></c:set>
    </c:if>

    <c:if test="${selectedModeType eq 'WALLET_ONLY' && entityInfo.upiPushEnabled}">
        <c:set var="isUpiPush_show" value="true"></c:set>
    </c:if>

    <c:if test="${isPPBL_Enabled}">

        <c:if test="${isPaymentBankCheckboxChecked && param.use_wallet eq 0 && empty param.use_payment_bank && empty param.txn_Mode && isUpiPush_show}">
            <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
            <c:set var="isUpiPushChecked" value="true" />
        </c:if>

        <c:if test="${isPaymentBankCheckboxChecked && param.use_wallet eq 1 && empty param.use_payment_bank && empty param.txn_Mode && isUpiPush_show}">
            <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
            <c:set var="isUpiPushChecked" value="true" />
        </c:if>

        <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode && empty param.use_wallet && empty param.use_payment_bank  && !walletInfo.walletEnabled && isUpiPush_show}">
            <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
            <c:set var="isUpiPushChecked" value="true" />
        </c:if>

        <c:if test="${loginInfo.loginFlag  && empty param.txn_Mode &&  ! empty param.use_wallet && (param.use_wallet eq 1 || param.use_wallet eq 0) && empty param.use_payment_bank  && isUpiPush_show && isPPBL_Enabled && paymentBankInactive && walletInfo.walletBalance < txnInfo.txnAmount}">
            <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
            <c:set var="isUpiPushChecked" value="true" />
        </c:if>

        <c:if test="${param.use_paytmcc eq 0 && param.use_payment_bank ne 1}">
            <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
            <%-- When set perference to sc/dc/cc/upi after uncheck postpaid --%>
            <c:if test="${param.use_paytmcc eq 0 && param.use_payment_bank ne 1}">
                <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
                <c:choose>
                    <c:when test="${saveCardEnabled eq true}">
                        <c:set var="paymentType" value="5"></c:set>
                    </c:when>
                    <c:when test="${upiEnabled eq true}">
                        <c:set var="paymentType" value="15"></c:set>
                    </c:when>
                    <c:when test="${dcEnabled eq true}">
                        <c:set var="paymentType" value="2"></c:set>
                    </c:when>
                    <c:when test="${ccEnabled eq true}">
                        <c:set var="paymentType" value="1"></c:set>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </c:if>
    </c:if>

    <c:if test="${(not empty param.use_wallet) && (param.use_wallet eq 0) && (not empty param.use_payment_bank) && (param.use_payment_bank eq 0) && (not empty param.use_paytmcc && param.use_paytmcc ne 0) && (digitalCreditInfo.digitalCreditInactive eq false && digitalCreditInfo.digitalCreditEnabled eq true)}">
        <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
        <c:set var="paymentType" value="16"></c:set>
    </c:if>


    </c:if>
    <c:if test="${isUpiPush_show && themeInfo.subTheme ne 'ccdc'}">
        <%@ include file="upiPush.jsp"%>
    </c:if>

</c:if>



  <c:if test="${loginInfo.loginFlag && isPPBL_Enabled && themeInfo.subTheme ne 'ccdc'}">
    <c:if test="${empty param.use_paytmcc && !empty param.use_wallet && (!empty param.use_payment_bank &&  param.use_payment_bank eq 0) && paymentType eq 18}">

               <c:if test="${param.use_wallet eq 1 && param.use_payment_bank ne 1}">
                   <c:choose>
                       <c:when test="${param.use_wallet eq 1 && saveCardEnabled eq true}">
                           <c:set var="paymentType" value="5"></c:set>
                       </c:when>
                       <c:when test="${param.use_wallet eq 1 && upiEnabled eq true}">
                           <c:set var="paymentType" value="15"></c:set>
                       </c:when>
                       <c:when test="${param.use_wallet eq 1 && dcEnabled eq true}">
                           <c:set var="paymentType" value="2"></c:set>
                       </c:when>
                       <c:when test="${param.use_wallet eq 1 && ccEnabled eq true}">
                           <c:set var="paymentType" value="1"></c:set>
                       </c:when>
                       <c:otherwise>
                       </c:otherwise>
                   </c:choose>
               </c:if>
       </c:if>
    <div class="row mb5 rel_pos" <c:if test="${paymentBankInactive eq true|| paymentBankRetryCount eq 0 || isInsufficientPaymnetBankBalance eq true}">style="color:#9a9999;"</c:if>>
      <div class="fl">
        <input name="paymentBankCheckbox" id="paymentBankCheckbox" type="checkbox" value=""
               onchange="processPaymentBank(this)"
               <c:if test="${paymentBankInactive || entityInfo.reseller eq true}">disabled="disabled"</c:if>
               <c:if test="${isPaymentBankCheckboxChecked}">checked="true"</c:if> />
        <span class="rel_pos span_inline" <c:if test="${paymentBankInactive ne true}">onclick="walletItemClickable(this.previousElementSibling)"</c:if> >Paytm Payments Bank Account


          <c:if test="${isInsufficientPaymnetBankBalance}">
            <span class="arrowIcon"></span>

                  <span class="insufficientIcon" onclick="insufficientBlance(this)">
                    <span class="tooltiptext">This payment method is disabled because your balance is insufficient for this transaction.</span>
                  </span>
          </c:if>
              </span>


        <div class="bal" id = "paymentBankBal" >(Available Balance
          <span class="WebRupee">Rs</span>
						<span class = 'amt' id = "paymentBankBalSpan">
							<fmt:formatNumber value="${paymentBankAccountBalance}" maxFractionDigits="2" minFractionDigits="2"  />
						</span>)
        </div>
      </div>
      <c:if test="${!paymentBankInactive}">
        <div class="fr abs_pos" id = "paytmCCBalance">
          <c:if test="${isPaymentBankCheckboxChecked && paymentType eq 17 && (amountDeductFromPaymentBank + 0) != 0 && (amountDeductFromPaymentBank + 0) != txnInfo.txnAmount}">
             <span class="WebRupee">Rs</span>
							<span id = "paymentBankAmt">
							<fmt:formatNumber value="${amountDeductFromPaymentBank}" maxFractionDigits="2" minFractionDigits="2" />
						</span>
          </c:if>
        </div>
      </c:if>
      <div class="clear"></div>

      <c:if test="${isPaymentBankCheckboxChecked && !paymentBankInactive}">
        <form autocomplete="off"
              id="paymentBankFormId"
              name="paymentbank-form"
              method="post" class="validated"
              action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}"
              style = "margin:0;padding:0" onsubmit="return ppblNpp_submit(this)">
          <input type="hidden"  name="txnMode" value="NB" />
          <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
          <input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
          <input type="hidden"  name="AUTH_MODE" value="USRPWD" />
          <input type="hidden" name="bankCode"  value="${paymentBankCode}">
          <input type="hidden" name="storeCardFlag"  value="off" />

          <c:if test="${txnConfig.addMoneyFlag}">
            <input type="hidden" name="addMoney" value="1" />
            <input type="hidden" name="walletAmount" value="0" />
          </c:if>

          <c:if test="${txnConfig.hybridAllowed}">
            <input type="hidden" name="walletAmount"  value="${walletInfo.walletBalance}" />
          </c:if>

          <!-- <div class="fl mb5" style="width: 100%; margin-top: 15px;">
              Enter Payment Bank Passcode
          </div> -->
          <div class="fl mb5" style="width: 100%;margin-top:15px;">
            <input type="tel" name="PASS_CODE" id="paymentBankTxtPassCode"
                   class="paymentBankPassCode mask"
                   maxlength="4" style="width: 94%;padding:5px;"
                   placeholder="Enter Paytm Passcode" />
          </div>
          <c:if test="${not empty paymentBankInvalidPassCodeMessage}">
            <div class="error-txt mt10 mb5">
                ${paymentBankInvalidPassCodeMessage}
            </div>
          </c:if>
          <div class="btn-submit fl" style="width: 100%;">
            <input class="gry-btn" type="submit"
                   value="Pay Now" name=""
                   style="width: 98%;border-radius: 5px;"
                   id="pbSubmit" />
          </div>
        </form>
        <div class="clear"></div>
      </c:if>
    </div>
  </c:if>

  <!-- Payment Bank -->

<c:if test="${digitalCreditInfo.digitalCreditEnabled eq true && !digitalCreditInfo.digitalCreditInactive && empty param.txn_Mode}">

  <c:if test="${(empty param.use_wallet || param.use_wallet eq 1) && isHybridAllowed && isHybrid && (walletInfo.walletBalance + digitalCreditInfo.accountBalance > txnInfo.txnAmount)}">
    <c:set var="hybridPostpaid" value="true"/>
  </c:if>
  <c:set var="isInsufficientPaytmCCBalance" value="${(0 + digitalCreditInfo.accountBalance eq 0) || (usePaytmCash && hybridPostpaid && walletInfo.walletBalance + digitalCreditInfo.accountBalance < txnInfo.txnAmount) ||
             ((!usePaytmCash || !hybridPostpaid) && (0.0 + digitalCreditInfo.accountBalance < txnInfo.txnAmount))}">
  </c:set>

  <%--for paytm cc code --%>
  <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
  <c:if test="${!isInsufficientPaytmCCBalance}">
    <c:if test ="${(empty param.use_paytmcc || param.use_paytmcc eq 1) && (0 + digitalCreditInfo.accountBalance >= txnInfo.txnAmount) && !hybridPostpaid}">
      <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
    </c:if>

    <c:if test ="${param.use_paytmcc eq 0 && param.usePaytmCash eq true}">
      <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
      <c:set var="usePaytmCash" value="true" scope="session"></c:set>
    </c:if>
    <c:if test="${!empty ifPostpaid && (empty param.use_paytmcc || param.use_paytmcc eq 1)}">
      <c:choose>
        <c:when test ="${hybridPostpaid}">
          <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
          <%--<c:set var="usePaytmCash" value="true" scope="session"></c:set>--%>
        </c:when>
        <c:when test ="${param.use_wallet eq 0 && (0 + digitalCreditInfo.accountBalance >= txnInfo.txnAmount)}">
          <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
          <c:set var="usePaytmCash" value="false" scope="session"></c:set>
        </c:when>
        <c:when test ="${param.use_wallet eq 0 && (0 + digitalCreditInfo.accountBalance < txnInfo.txnAmount)}">
          <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
          <c:set var="usePaytmCash" value="false" scope="session"></c:set>
        </c:when>
      </c:choose>
    </c:if>
    <c:if test="${(param.use_paytmcc eq 1) && param.use_wallet eq 1 && hybridPostpaid}">
      <c:set var="paytmCCchecked" value="true" scope="session"></c:set>
      <%--<c:set var="usePaytmCash" value="true" scope="session"></c:set>--%>
    </c:if>
  </c:if>
  <c:if test ="${!hybridPostpaid && usePaytmCash}">
    <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
  </c:if>
  <c:if test="${param.use_payment_bank eq 1}">
    <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
  </c:if>
  <c:if test="${empty param.use_payment_bank && isPPBL_Enabled && ((isHybridAllowed && walletInfo.walletBalance + paymentBankAccountBalance >= txnInfo.txnAmount) ||
             (!isHybridAllowed && (0 + paymentBankAccountBalance > txnInfo.txnAmount)))}">
    <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
  </c:if>
  <c:if test="${upiPushcheckBoxSelected eq true}">
    <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
  </c:if>
    <c:if test="${isRetryAvaiable}">
        <c:set var="paytmCCchecked" value="false" scope="session"></c:set>
    </c:if>
</c:if>

<!-- ICICI digital card -->
<c:if test="${loginInfo.loginFlag && digitalCreditInfo.digitalCreditEnabled  && themeInfo.subTheme ne 'ccdc'}">

  <!-- If wallet has balance and used -->
  <c:if test="${usePaytmCash && paytmCCchecked}">
    <c:set var="amountDeductFromPaytmCC" value="${txnInfo.txnAmount - walletInfo.walletBalance}"></c:set>
  </c:if>
  <!-- If wallet has no balance -->
  <c:if test="${!usePaytmCash && paytmCCchecked}">
    <c:set var="amountDeductFromPaytmCC" value="${txnInfo.txnAmount}"></c:set>
  </c:if>
  <%--  <div class="row mb5 rel_pos" <c:if test="${digitalCreditInfo.digitalCreditInactive}">style="color:#9a9999;"</c:if>>
    <div class="fl">--%>
  <div class="row mb5 rel_pos" <c:if test="${digitalCreditInfo.digitalCreditInactive || isInsufficientPaytmCCBalance eq true}">style="color:#9a9999;"</c:if>>
    <div class="fl">
      <input name="paytmCC" id="paytmCC" type="checkbox" value="" onchange="processPaytmCC(this)"
             <c:if test="${paytmCCchecked}">checked="checked"<c:set var="paymentType" value="16"></c:set></c:if>
             <c:if test="${digitalCreditInfo.digitalCreditInactive || isInsufficientPaytmCCBalance eq true}">disabled="disabled"</c:if> />
      <span class="rel_pos span_inline" <c:if test="${digitalCreditInfo.digitalCreditInactive ne true}">onclick="walletItemClickable(this.previousElementSibling)"</c:if>>Paytm Postpaid

      <c:if test="${digitalCreditInfo.digitalCreditInactive || isInsufficientPaytmCCBalance}">
        <span class="arrowIcon"></span>

                  <span class="insufficientIcon" onclick="insufficientBlance(this)">
                    <span class="tooltiptext">This payment method is disabled because your balance is insufficient for this transaction.</span>
                  </span>
      </c:if>
      </span>
      <div class="clear"></div>
      <div class="bal" id = "paytmCCBal" >(Available Limit  <span class="WebRupee">Rs</span> <span class = 'amt' id = "paytmCCBalSpan"><fmt:formatNumber value="${digitalCreditInfo.accountBalance}" maxFractionDigits="2" minFractionDigits="2"  /></span>)</div>
        <c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage && digitalCreditInfo.digitalCreditInactive}">
            <div class="error-txt mt10 mb5"> ${digitalCreditInfo.invalidPassCodeMessage}</div>
        </c:if>
    </div>
    <c:if test="${!digitalCreditInfo.digitalCreditInactive}">
      <div class="fr abs_pos" id = "paytmCCBalanceUsed">
        <c:if test="${(paytmCCchecked || param.use_paytmcc) && (amountDeductFromPaytmCC + 0) != 0 && (amountDeductFromPaytmCC + 0) != txnInfo.txnAmount}">
          <span class="WebRupee">Rs</span>
          <span id = "paytmCCAmt">
            <fmt:formatNumber value="${amountDeductFromPaytmCC}" maxFractionDigits="2" minFractionDigits="2" />
          </span>
        </c:if>
      </div>
    </c:if>
    <div class="clear"></div>
    <c:if test="${paytmCCchecked &&  !digitalCreditInfo.digitalCreditInactive}">
      <c:set var="paymentType" value="16" />
      <form autocomplete="off" name="creditcard-form" method="post" class="validated" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" style = "margin:0;padding:0" onsubmit="return ppblNpp_submit(this)">
        <input type="hidden"  name="txnMode" value="PAYTM_DIGITAL_CREDIT" />
        <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
        <input type="hidden"  name="channelId" value="${channelInfo.channelID}" />
        <input type="hidden"  name="AUTH_MODE" value="USRPWD" />
        <input type="hidden" name="pccAccountBalance" id="pccAccountBalance" value="${digitalCreditInfo.accountBalance}" />
        <input type="hidden" name="storeCardFlag"  value="off" />
        <c:if test="${hybridPostpaid}">
          <input type="hidden" name="walletAmount" id="hybrid-PaytmPostpaid-walletBal" value="${walletInfo.walletBalance}" />
        </c:if>
        <c:if test="${digitalCreditInfo.passcodeRequired}">
          <div class="fl mb5" style="width: 100%; margin-top: 15px;">
            Enter Paytm Passcode
          </div>
          <div class="fl mb5" style="width: 100%">
            <input type="tel" name="PASS_CODE" id="txtPassCode" class="digitalCreditPassCode mask" maxlength="6" style="width: 94%;padding:5px;"/>
          </div>
        </c:if>

        <c:if test="${not empty digitalCreditInfo.invalidPassCodeMessage}">
          <div class="error-txt mt10 mb5"> ${digitalCreditInfo.invalidPassCodeMessage}</div>
        </c:if>
        <c:if test="${not empty requestScope.validationErrors['INVALID_PASS_CODE_BLANK']}">
          <div class="error-txt mt10 mb5"> ${requestScope.validationErrors["INVALID_PASS_CODE_BLANK"]}</div>
        </c:if>

        <div class="btn-submit fl" style="width: 100%;">
          <c:choose>
            <c:when test ="${digitalCreditInfo.passcodeRequired}">
              <input class="gry-btn" id="postSubmit" type="submit" value="Pay Now" style="width: 98%;border-radius: 5px;" name="">
            </c:when>

            <c:when test ="${!digitalCreditInfo.passcodeRequired}">
              <input class="blue-btn" id="postSubmit" type="submit" value="Pay Now" style="width: 98%;border-radius: 5px;" name="">
            </c:when>
          </c:choose>
        </div>
      </form>
    </c:if>
    <div class="clear"></div>
  </div>

</c:if>

<!-- End of ICICI digital card -->

<!-- End of Login Flag check -->