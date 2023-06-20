<c:if test="${15 eq paymentType}">
<c:choose>
 <c:when test="${themeInfo.subTheme eq 'paytmLowSSL' || themeInfo.subTheme eq 'paytmAppSSL'}">
        <div class="heading ml15 mt20" style="padding-top: 0">BHIM UPI</div>
                <p style="text-align:center;" class="smltxt">Kindly <a href="/theia/cancelTransaction?${queryStringForSession}" class="smltxt">upgrade</a>  the app to complete the transaction</p>
</c:when>
<c:otherwise>
<div class="heading ml15 mt20" >BHIM UPI</div>


<form autocomplete="off" method="post" action="${pageContext.request.contextPath}/payment/request/submit?${queryStringForSession}" onsubmit = "return upiFormSubmit(); ; submitForm()">
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
<div class="row-dot ml20 mr20" >
        <p class="error">
<!--         <label>Enter your Virtual Payment Address (<strong style="font-weight:bold;">VPA</strong>)</label><br /> -->


                <input type="text" name="VIRTUAL_PAYMENT_ADDRESS" placeholder="Virtual Payment Address (VPA)" class="upiPayMode text-input large-input all-input"   style="width: 98.2%; "  id="vpaID"  />
                <p style="margin-top:12px;margin-bottom: 15px;"><label style="font-size:10px; line-height:18px;opacity:0.5;">VPA is a unique payment address that can be linked to a person's bank accounts to make payments.</label></p>

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


       </p>

    <c:if test="${saveCardOption}">
        <c:choose>
            <c:when test="${themeInfo.subTheme eq 'ccdc' }">
                <p class="pt7">
                    <input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked" style="display:none"/>
                    <input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" checked="checked" disabled="disabled"/> Save this card for future checkout
                </p>
            </c:when>
            <c:when test="${themeInfo.subTheme ne 'ccdc' }">
                <p class="pt7" id="ccCardSaved">
                    <input type="checkbox" id="saveCard" name="storeCardFlag" value="Y" ${txnConfig.saveCardMandatory ? 'disabled="disabled"':''}
                           checked="checked"/> Save this VPA for faster checkout
                </p>
            </c:when>
        </c:choose>
    </c:if>

	<c:if test ="${txnConfig != null &&  txnConfig.paymentCharges != null}">
			<div class="post-conv-inclusion">
				<%@ include file="../../common/post-con-withr/upi-postconv.jsp" %>
			</div>
	</c:if>
    <p class="pt7" style="margin-top: 23px;"><button type="submit"  class="blue-btn" >${submitBtnText}</button></p>

    <!--Lock image-->
    <div class="pt7 mt15" style="margin-bottom: 23px;">
		<div class="fl image ml15"><img src="/theia/resources/images/wap/merchantLow3/lock.png" alt="" title="" /></div>
        <div class="fl lock-display"> Payment is secured via 128 Bit encryption by Verisign.</div>
        <div class="fl lock-display"> We do not share your card details with anyone</div>
		<div class="clear"></div>
	</div>

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
                     
