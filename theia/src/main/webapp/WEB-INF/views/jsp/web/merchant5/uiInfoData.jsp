<%--
  Created by IntelliJ IDEA.
  User: rajansingh
  Date: 20/12/17
  Time: 9:54 AM
  To change this template use File | Settings | File Templates.
--%>
<c:set var="wltAmnt" value="${walletInfo.walletBalance}" />
<c:set var="walletUsed" value="${txnInfo.walletAmount > 0 ? true  : false}" />
<div id="auth-config" data-value='${loginInfo.oauthInfo}'></div>

<!-- For payment retry purpose -->
<div id="retryPaymentMode"
     data-value='${retryPaymentInfo.paymentMode}'>
</div>
<div id="wallet-details" data-available="${walletInfo.walletFailed ? false : true}" data-balance="${walletInfo.walletBalance}" data-wallet-only="${onlyWalletEnabled}" data-wallet-used="${walletUsed}"></div>
<div id="payment-details" data-amount="${txnInfo.txnAmount}"
     data-limeroad="${themeInfo.subTheme  eq 'limeroad' ? true : false}" data-hybrid-allowed="${txnConfig.hybridAllowed}"
     data-cod-hybrid-allowed="${txnConfig.codHybridAllowed}"
     data-offline-paymode="${txnConfig.chequeDDNeftEnabled}"
     data-idebit-ic ="${entityInfo.iDebitEnabled}"
        ></div>
<div id="addMoney-details" data-available="${txnConfig.addAndPayAllowed}"
     data-selected="${txnConfig.addMoneyFlag}"></div>
<div id="login-details"
     data-value='${loginInfo.loginFlag}' data-autoLogin="${loginInfo.autoLoginAttempt}">
</div>
<div id="current-payment-type" data-value='${paymentType}'></div>
<div id="error-details"
     data-available="${ !empty requestScope.validationErrors ? true : false }"
     data-maintenancemsg="${messageInfo.maintenanceMessage}"
     data-lowerrormsg="${messageInfo.lowPercentageMessage}"
        ></div>
<div id="sucess-rate" data-mid="${txnInfo.mid}" data-oid="${txnInfo.orderId}"></div>
<c:if test="${!empty txnInfo.promoCodeResponse && !empty txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
  <div id="promocode-details"
       data-type="${txnInfo.promoCodeResponse.promoCodeDetail.promocodeTypeName}"
       data-paymentmodes="${txnInfo.promoCodeResponse.paymentModes}"
       data-showallmodes="${promoShowAllModes}"
       data-nblist="${txnInfo.promoCodeResponse.nbBanks}"
       data-cardlist="${txnInfo.promoCodeResponse.promoCodeDetail.cardBins}"
       data-cardtypelist="${txnInfo.promoCodeResponse.promoCodeDetail.promoCardType}"
       data-errormsg="${txnInfo.promoCodeResponse.promoCodeDetail.promoErrorMsg}"
       data-checkPromoValidity="${txnInfo.promoCodeResponse.checkPromoValidityURL}"
       data-mid="${merchInfo.mid}"
       data-txn="${txnInfo.paymentTypeId}"
       data-promocode="${txnInfo.promoCodeResponse.promoCodeDetail.promoCode}">
  </div>
</c:if>
<div id="auth-js-details"
     data-mid="${txnInfo.mid}"
     data-orderid="${txnInfo.orderId}"
<%--data-txnInfo="${txnInfo}"--%>
<%--data-loginInfo="${loginInfo}"--%>
     data-theme="${themeInfo.loginTheme}"
     data-retrycount="${loginInfo.loginRetryCount}"></div>



<div id="other-details" data-jsessionid="<%=request.getSession().getId() %>" data-save-card-mandatory="${cardInfo.cardStoreMandatory}"></div>
<div id="txnTransientId" data-value="${txnInfo.txnId}"></div>
<div id="paymentCharges" data-value="${!empty txnConfig.paymentCharges ? true : false}"></div>
<div id="ir-addMoneyCharge" data-iramount="${txnConfig.paymentCharges.PPI.totalTransactionAmount}"></div>

<div id="irctcDefaultFee" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
<div id="irctcOnlyWallet" data-bm="${txnConfig.paymentCharges.PPI.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.PPI.text}" data-tm="${txnConfig.paymentCharges.PPI.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.PPI.totalConvenienceCharges}">	</div>
<div id="irctcHybridWallet" data-bm="${txnConfig.paymentCharges.HYBRID.baseTransactionAmount}" data-ct="${txnConfig.paymentCharges.HYBRID.text}" data-tm="${txnConfig.paymentCharges.HYBRID.totalTransactionAmount}" data-cf="${txnConfig.paymentCharges.HYBRID.totalConvenienceCharges}">	</div>
<!-- Configurations -->
