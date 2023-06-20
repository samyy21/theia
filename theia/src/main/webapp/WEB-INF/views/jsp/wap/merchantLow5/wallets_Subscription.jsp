<!-- paytm cash -->
<%-- <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance > 0}"> --%>
<c:if test="${!empty walletInfo.walletBalance && !empty usePaytmCash}">

    <c:set var="checkPaytmCashCheckbox" value="false"/>
    <%-- <c:if test="${usePaytmCash && walletInfo.walletBalance > 0}"> --%>
    <c:if test="${usePaytmCash}">
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

    <c:if test="${isSubscriptonFlow}">
        <c:set var="disablePaytmCashCheckbox" value="true"></c:set>
    </c:if>

    <div id="showHideWallet" class="${!empty hidePaytmCash ? 'hide' : ''}">
        <div class="row mb5 rel_pos">
            <div class="fl">
                <input name="paytmCashCB" id="paytmCashCB" type="checkbox" value="" onclick="processPaytmCash(this)" <c:if test="${checkPaytmCashCheckbox}">checked="checked"</c:if>  <c:if test="${disablePaytmCashCheckbox}">disabled="disabled"</c:if> />
                <span>Paytm</span>
                <div class="clear"></div>
                <!-- <div class="bal" id = "remBal">(You have <span class="WebRupee">Rs</span> <span class = 'amt' id = "remBalSpan"></span> in your Paytm Wallet)</div> -->
                <div class="bal" id = "yourBal" >(Your current balance is  <span class="WebRupee">Rs</span> <span class = 'amt' id = "yourBalSpan"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2" minFractionDigits="2" /></span>)</div>
                <input type = "hidden" value = "${walletInfo.walletBalance}" id = "totalWalletVal"/>
            </div>
            <c:if test="${usePaytmCash}">
                <div class="fr abs_pos" id = "walletBalance">
                    - <span class="WebRupee">Rs</span>
                    <span id = "walletBalanceAmt"><fmt:formatNumber value="${walletInfo.walletBalance}" maxFractionDigits="2"  minFractionDigits="2" /></span>
                </div>
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
        <c:if test="${!empty walletInfo.walletBalance && walletInfo.walletBalance >= 0 && walletInfo.walletBalance >= txnInfo.txnAmount}">
            <form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" id="walletForm" style = "margin:0;padding:0" class="${txnInfo.subscriptionPaymentMode eq 'PPI' && txnInfo.subscriptionPPIOnly eq 'Y' ? '' : 'hide'}">
                <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
                <input type="hidden"  name="txnMode" value="PPI" />
                <input type="hidden"  name="txn_Mode" value="PPI" />
                <input type="hidden"  name="channelId" value="WAP" />
                <input type="hidden"  name="AUTH_MODE" value="USRPWD" />
                <input type="hidden" name="walletAmount" id="walletAmount" value="0" />
                <div style="display: none;" class="fullWalletDeduct" id = "onlyWalletAmt">
                    <input class="blue-btn" type="submit" value="Pay now" name="">
                </div>
            </form>
        </c:if>
    </div>
</c:if>

<!-- paytm cash -->

<!-- Payment Bank Conf -->

<c:set var="paymentBankEnabled" value="${savingsAccountInfo.savingsAccountEnabled}"></c:set>
<c:set var="paymentBankInactive" value="${savingsAccountInfo.savingsAccountInactive}"></c:set>
<c:set var="paymentBankAccountBalance" value="${savingsAccountInfo.effectiveBalance}"></c:set>
<c:set var="paymentBankInvalidPassCodeMessage" value="${savingsAccountInfo.invalidPassCodeMessage}"></c:set>
<c:set var="paymentBankRetryCount" value="${savingsAccountInfo.paymentRetryCount}"></c:set>
<c:set var="paymentBankCode" value="${savingsAccountInfo.paymentsBankCode}"></c:set>
<c:set var="isPPBL_addMoneyEnable" value="${entityInfo.addPaymentsBankEnabled}"></c:set>
<c:set var="isPPBL_merchantPayModeEnable" value="${entityInfo.paymentsBankEnabled}"></c:set>

<c:set var="isPPBL_Enabled" value="${(isPPBL_addMoneyEnable || isPPBL_merchantPayModeEnable)}"></c:set>

<c:if test="${param.use_payment_bank eq 1}">
  <c:set var="paymentBankchecked" value="true"></c:set>
</c:if>

<%--For subscrption, only "PPBL-only" mode is supported--%>
<c:set var="isPPBL_Enabled" value="${savingsAccountInfo.savingsAccountEnabled && txnInfo.subscriptionPaymentMode eq 'PPBL'}"></c:set>

<!-- Payment Bank Conf -->


<%-- promocode logic --%>
<c:if test="${empty promoShowAllModes}">
  <c:set var="promoShowAllModes" value="0" scope="session"></c:set>
</c:if>
<c:if test="${param.showAll eq 1}">
  <c:set var="promoShowAllModes" value="1" scope="session"></c:set>
</c:if>
<%-- promocode logic --%>


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


<%-- headers --%>
<%--<div class="header row" id = "paytmCashText" style = "display:none">
  Uncheck Paytm to pay using other options.
</div>


<input type = "hidden" value = "${txnInfo.txnAmount}" id = "totalAmtVal"/>
<c:if test="${empty hidePaytmCash}">
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
</c:if>--%>
<%-- headers --%>

<!-- Payment Bank -->
<c:if test="${isPPBL_Enabled && themeInfo.subTheme ne 'ccdc'}">
  <c:set var="isPaytmCCEnable" value="${not empty param.use_paytmcc && param.use_paytmcc eq 1}"></c:set>
  <c:set var="isPaymentBankCheckboxDisable" value="${!paymentBankInactive &&
			((usePaytmCash && ((walletInfo.walletBalance + paymentBankAccountBalance) < txnInfo.txnAmount)) ||
			(!usePaytmCash && (0 + paymentBankAccountBalance < txnInfo.txnAmount)))}">
  </c:set>

  <c:set var="isInsufficientPaymnetBankBalance" value="${(0 + paymentBankAccountBalance eq 0) || (usePaytmCash && walletInfo.walletBalance + paymentBankAccountBalance < txnInfo.txnAmount) ||
             (!usePaytmCash && (0 + paymentBankAccountBalance < txnInfo.txnAmount))}">
  </c:set>

   <%-- For Subscription case, As of now PPBL will always be checked if it is enabled--%>
  <c:set var="isPaymentBankCheckboxChecked" value="true"></c:set>


  <c:if test="${isPPBL_Enabled && !paymentBankInactive  &&
            isPaymentBankCheckboxChecked && (empty param.txn_Mode)&& (empty param.use_payment_bank || param.use_payment_bank eq 1)}">
    <!-- When payment bank balance is sufficient hide other modes payments -->
    <c:set var="paymentType" value="17"></c:set>
  </c:if>

  <c:if test="${isPPBL_Enabled && !paymentBankInactive  &&
           isPaymentBankCheckboxChecked && !empty errorMsg &&  (empty param.use_payment_bank || param.use_payment_bank eq 1)}">
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

<%--  <c:if test="${paytmCCchecked eq true}">
    <c:set var="isPaymentBankCheckboxChecked" value="false"></c:set>
  </c:if>--%>

  <c:if test="${(usePaytmCash && (walletInfo.walletBalance >= txnInfo.txnAmount)) &&
			(isPPBL_Enabled && !paymentBankInactive && (0 + paymentBankAccountBalance >= txnInfo.txnAmount))}">
    <c:set var="amountDeductFromPaymentBank" value="0"></c:set>
  </c:if>

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

  </c:if>


  <c:if test="${loginInfo.loginFlag && isPPBL_Enabled && themeInfo.subTheme ne 'ccdc'}">
    <div class="row mb5 rel_pos" <c:if test="${paymentBankInactive eq true|| paymentBankRetryCount eq 0 || isInsufficientPaymnetBankBalance eq true}">style="color:#9a9999;"</c:if>>
      <div class="fl">
        <input name="paymentBankCheckbox" id="paymentBankCheckbox" type="checkbox" value=""
               onchange="processPaymentBank(this)" disabled="disabled"
               <c:if test="${paymentBankInactive ne true}">checked="true"</c:if> />

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

      <c:if test="${!paymentBankInactive}">
        <form autocomplete="off"
              id="paymentBankFormId"
              name="paymentbank-form"
              method="post" class="validated"
              action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}"
              style = "margin:0;padding:0">
          <input type="hidden"  name="txnMode" value="PPBL" />
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
