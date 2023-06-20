<c:if test="${15 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
        <div class="heading">BHIM UPI</div>
                <p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading">BHIM UPI</div>


<form id="upiForm" autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit="return upiFormSubmit(); submitForm(this);">
    <input type="hidden" name="CSRF_PARAM" value="${varCSRF}" />
        <input type="hidden" name="txnMode" value="UPI" />
        <input type="hidden" name="txn_Mode" value="UPI" />
        <input type="hidden" name="AUTH_MODE" value="USRPWD" />
        <input type="hidden" name="channelId" value="WAP" />
        <input type="hidden" name="walletAmount" value="0" />
        <c:if test="${cardInfo.cardStoreMandatory}">
                <input type="hidden" name="storeCardFlag"  value="on" />
        </c:if>
        <c:if test="${txnConfig.addMoneyFlag  && isAddMoneyAvailable}">
                <input type="hidden" name="addMoney" value="1" />
        </c:if>
<div class="row-dot">
        <p class="error">
        <label for="VIRTUAL_PAYMENT_ADDRESS">Enter your Virtual Payment Address (<strong style="font-weight:bold;">VPA</strong>)</label><br />


                <input type="text" name="VIRTUAL_PAYMENT_ADDRESS" class="upiPayMode text-input large-input"   style="width: 95%; margin-top:10px;" id="vpaID"  /><br>


                <p style="margin-top:10px;"><i style="font-size:12px; line-height:18px;">VPA is a unique payment address that can be linked to a person's bank accounts to make payments.</i></p>

<div class="clear"></div>

    <c:choose>
        <c:when test="${!empty requestScope.validationErrors && !empty requestScope.validationErrors['INVALID_VPA']}">
            <div class="error error2 error-txt mt10" id="invalidVpa">${requestScope.validationErrors['INVALID_VPA']}</div>
        </c:when>
        <c:otherwise>
            <c:if test="${!empty messageInfo.inValidVPAMessage}">
                <div class="error error2 error-txt mt10" style="display:none;" id="invalidVpa">${messageInfo.inValidVPAMessage}</div>
            </c:if>
        </c:otherwise>
    </c:choose>


 <%--post convenince related --%>
 	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.HYBRID != null}">
		<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.HYBRID.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
		<div class="post-conv-inclusion"  id="hybrid-post-con" style="display:none; margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
			<%@ include file="../../common/post-con/hybrid-postconv.jsp" %>
		</div>
	 </c:if>
	  <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.PPI != null}">
	  		<c:set var ="valueOfBalanceToBeDisplayed"><fmt:formatNumber  maxFractionDigits = "2" minFractionDigits = "2" value = "${txnConfig.paymentCharges.PPI.totalTransactionAmount - walletInfo.walletBalance}" /></c:set>
			<div class="post-conv-inclusion" id="addnpay-post-con" style="display:none;margin-right: 10px;" data-post-bal = "Pay <span class='WebRupee'>Rs</span> ${valueOfBalanceToBeDisplayed}">
				<%@ include file="../../common/post-con/add-money-postconv.jsp" %>
			</div>
	 </c:if>
    <c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null && txnConfig.paymentCharges.UPI != null}">
			<div class="post-conv-inclusion" id="normal-post-con" style="margin-right: 10px;">
				<%@ include file="../../common/post-con/upi-postconv.jsp" %>
			</div>
	</c:if>


    <!--Lock image-->
    <p class="pt7">
    	<button type="submit" class="blue-btn" id="btnSubmit" >Pay Now</button>
    </p>

</div>

</form>
<script>

    function validateStrVPA(vpa)
    {
        var re = /^[a-zA-Z0-9.-]*$/;
        return re.test(vpa);
    }
    // @ validation in VPA
    function validateAtVPA(vpa)
    {
        var re = /\S+@\S/;
        return re.test(vpa);
    }


    function validationUPI(vpa){
        var isValid=false;
        if(vpa.length > 0 && vpa.length <= 255 && validateAtVPA(vpa)){
            vpaSplit = vpa.split("@");
            if(vpaSplit.length == 2){
                var handle = vpaSplit[0];
                var psp = vpaSplit[1];
                if(psp.length > 0 && handle.length){
                    isValid = validateStrVPA(psp) && validateStrVPA(handle);
                }
            }
        }
        return isValid;
    }



    function upiFormSubmit(){
        var vpa=document.getElementById("vpaID");
        var invalidVpaMsg=document.getElementById("invalidVpa");
        var vpaVal=vpa.value;
        if(!validationUPI(vpaVal)){
            vpa.style.border="1px solid red";

            invalidVpaMsg.style.display="block";
            vpa.focus();
            return false;
        }
        vpa.style.border="none";
        invalidVpaMsg.style.display="none";
    }

</script>

</c:otherwise>
</c:choose>
</c:if>
                     
