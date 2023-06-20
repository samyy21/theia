$(function() {
	
	// CVV Focus on Tab change
	if($("#saveCard .ui-collapsible-content").css("display")=='block'){
		$("#saveCard .scCvvInput").focus();
	}
	
	var isCashBackPromocode = false;
	if($.trim($('#header').data('promocodetype')) == "CASHBACK") {
		isCashBackPromocode = true;
		var netBankingAvailable = $('#nbContent').data('netbanking').toString().split(',');
		$('#atmCard, #imps').hide();
		
		// uncheck paytmcash
		$('#pc input').removeAttr('checked');
		
		var promoCodes = $('#cn1').data('promocodes').toString().split(',');
		
		// hide unavailable tabs
		var showTabs = $('#header').data('showtabs').toString().split(',');
		//$('#cards .scContainer').hide();
		var savedCardsMatchedCount = $('#cards .promo-valid-card').length;
//		$('#cards .scContainer').each(function() {
//			if(promoCodes.indexOf($.trim($(this).find('.radio-label').data('firstsixdigits'))) > -1) {
//				$(this).show();
//				savedCardsMatchedCount++;
//			}	
//		});
		if(savedCardsMatchedCount == 0) {
			$('#saveCard').hide();
		}
		if(showTabs.indexOf('DC') === -1) {
			$('#debitCard').hide();
		}
		if(showTabs.indexOf('CC') === -1) {
			$('#creditCard').hide();
		}
		if(showTabs.indexOf('NB') === -1) {
			$('#netbanking').hide();
		}
		if(showTabs.indexOf('CASHCARD') === -1) {
			$('#itzcash').hide();
		}
		if(showTabs.indexOf('REWARDS') === -1) {
			$('#rewards').hide();
		}
		if(showTabs.indexOf('IMPS') === -1) {
			$('#imps').hide();
		}
		if(showTabs.indexOf('ATM') === -1) {
			$('#atmCard').hide();
		}
		// hide unavailable tabs
		
		// hide nb banks not availabe
		if(netBankingAvailable.length) {
			
			// hide popular nb banks if not available
			var popularBanksAvailableCount = 0;
			$('#pbanks li').each(function() {
				var bankId = $(this).children().attr('id');
				
				if(netBankingAvailable.indexOf(bankId) == -1){
					$(this).hide();
				} else {
					popularBanksAvailableCount++;
				}
			});
			
			// hide labels when no bank available
			if(popularBanksAvailableCount == 0){
				$("#label-pop-banks, #label-other-banks").hide();
			}
			
			// hide other nb banks in dropdown if not available
			var otherBanksAvailableCount = 0;
			$('#nbSelect option').each(function() {
				var bankId = $(this).attr('value');
				
				
				if(netBankingAvailable.indexOf(bankId) == -1){
					$(this).attr("disabled", "disabled");
				} else {
					otherBanksAvailableCount++;
				}
			});
			
			// show dropdown or hide dropdown
			if(otherBanksAvailableCount == 0){
				$("#label-other-banks").hide();
				$("#nbSelect").parent().hide();
			}
			
		} else {
			$('#netbanking').hide();
		}
	}
	
	$(document).on('click','.show-other-options', function() {
		$('#nbSelect option').removeAttr('disabled');
		$('#pbanks li').show();
		$("#label-pop-banks, #label-other-banks").show();
		$('#paymodes > div').show();
		$(this).parent().html($('.non-error-text').html());
		$('#summary-section').show();
		$('#cards .scContainer').removeClass("hide");
	});
	
	// function to show/hide error in case of cashback 
	$('#cn, #cn1').on('blur',function() {
		var card = $(this);
		var number = card.val();
		var mode = card.data("type");
		
		if(!number || $("#promoType").length == 0)
			 return false;
		
		// BIN list
		var promoCardList = card.data('promocodes');
		
		// Category list
		 var promoCardTypeList = $("#promoCardTypeList").data("value");
		 
		var cardType = getCardType(number);
		var identifier = cardType + "-" + mode;
		
		var isCardValid = false;
		
		// check if card category-type maches cards in list
		 if(promoCardTypeList.indexOf(identifier) != -1) {
			 isCardValid = true;
		 }
		 
		 // check if card no maches cards in list
		if(!isCardValid && promoCardList.indexOf(number.substring(0,6)) != -1) {
			isCardValid = true;
		}
		
		 
		 $('.promo-code-error').hide();
		 if(!isCardValid) {
			 $(this).parent().next().show();
		 }
	});
	
	// setup accordian
	$('.ui-collapsible-heading').click(function(){
		
		$('.ui-collapsible-heading .ui-icon').removeClass("ui-icon-arrow-d");
		$(this).find(".ui-icon").addClass("ui-icon-arrow-d");
		
		$(".ui-collapsible-content").hide();
		$(this).siblings(".ui-collapsible-content").show();
		
		// CVV Focus on Tab change
		if($("#saveCard .ui-collapsible-content").css("display")=='block'){
			$("#saveCard .scCvvInput").focus();
		}
		
	});
	// setup accordian
	
	// setup selectbox
	$("select").on("change", function(){
		var index = this.selectedIndex;
		var val = $(this).find('option:eq('+ index +')').text();
		$(this).parent().find(".ui-btn-text").text(val);
	});
	// setup selectbox
	
	
	// setup input focus
		$('.ui-input-text').on("focus", function(){
			$(this).parent().addClass("ui-focus");
		});
		$('.ui-input-text').on("blur", function(){
			$(this).parent().removeClass("ui-focus");
		});
	// setup input focus
	
	
	
	$('#paymodes').on('click', '.saveRadio', function(e) {
		$("#scCvvBox")[0].value = "";
		
		var saveCard = $(this).attr('id');
		//$(".saveRadio").removeClass('checked');
		//$("#" + saveCard).css("background-position", "center -50px")
		$("#savedCardId")[0].value=saveCard;
		$(".saveCardCvvDiv").hide();
		$("#cvvDiv-" + saveCard).show();
		
		$(".scCvvError").hide();
		$(".scCvvInput").parent().removeClass("cvvError");
		
		$("#scSubmit").attr("disabled", false);
	});
//	$('form').submit(function(){
//		$('.load-btn input[type="submit"]').attr("disabled", "disabled");
//		$('.load-btn').removeClass('load-btn').addClass('gry-btn');
//	});
	$(document).ready(function(){
		$(document).bind('click', function(e) {
			$clicked = $(e.target);
			
			if (!($clicked.attr("id") == "dcCvvBox")) {
				$('#showHideDiv').hide();
			}
			
			if (!($clicked.attr("id") == "ccCvvBox")) {
				$('#ccShowHideDiv').hide();
				$('#amexShowHideDiv').hide();
			}
			
			if (!($clicked.attr("id") == "mmid")) {
				$('#impsMmidShowHideDiv').hide();
			}
			
			if (!($clicked.attr("id") == "otp")) {
				$('#impsOtpshowHideDiv').hide();
			}
		});
		
	});

	$('#paymodes').on('click', function() {
		if($(this).data('disabled')) {
			$('#pc .ui-checkbox').dgUncheck("#pc");
			processWallet();
			$('#paymodes').data('disabled', false);
		}
		return true;
	});
	
	$("#login-btn").on("click", function(){
		showAuthView();
	});
	
	// show login/register page
	function showAuthView(type) {
		
		var showRegisterView = false;
		
		if(type == "register")
			showRegisterView = true;
		
		var authConfig = $("#auth-config").data("value");
		var hostUrl = authConfig.host;
		
		var params = [];

		params.push('response_type=code');
		params.push('scope=paytm')
		params.push('theme=HTML5');

		var client_id = authConfig.client_id;
		params.push('client_id=' + client_id);

		var returnUrl = authConfig.return_url;
		params.push('redirect_uri=' + returnUrl);

		var view = "/oauth2/authorize";
		if(showRegisterView == true)
			view = "/register";
		

		var src = hostUrl + view + "?" + params.join('&');
		
		window.location = src;
	}


	//$('.submitButton').live('click', function(e) {
//		$(this).siblings().children(".ui-icon").addClass("loader");
	//	
//		var id = $(this).attr("id");
//		if(id == "scSubmit") {
//			var saveCard = $("#savedCardId")[0].value;
//			var cvv = $("#cvv-" + saveCard)[0].value;
//			$("#scCvvBox")[0].value = cvv;
//			$('#scSubmit form').submit();
//		}
	//	
//		$(this).parents("form").submit();
	//});

	$('#paymodes').on('click','#scSubmit', function(e) {
		var saveCard = $("#savedCardId")[0].value;
		var cvv = $("#cvv-" + saveCard)[0].value;
		$("#scCvvBox")[0].value = cvv;
		return true;
	});

	$('#paymodes').on('click', '.deleteCard', function() {
			var savedCardId = $(this).attr('cardId');
			$.post("DeleteCardDetails", {
		     	"savedCardId": savedCardId
		    },
		    function(data,status) {
		      if ('success' == status) {
		    	  $("#scContainer-" + savedCardId).remove();
		    		if (0 == $(".scContainer").size()) {
		    			// open first tab
		    			var firstTab = $(".ui-collapsible")[1].id;
		    			$("#" + firstTab + " .ui-collapsible-heading").click();
		    			
						$("#saveCard").remove();
		    		} else {
		    			var saveCard = $(".saveRadio")[0].id;
		    			$("#" + saveCard).addClass("checked");
		    			$("#" + saveCard + " div").children().attr("checked", "checked");
		    			$("#" + saveCard + " div input").children().attr("checked", "checked");
		    			
		    			$("#savedCardId")[0].value = saveCard;
		    			$("#cvvDiv-" + saveCard).show();
		    			$("#scSubmit").attr("disabled", false);
		    		}
		      	}
		    }
		);
		return false;
		}
	);

	$('#summary-section').on('click','#pc', function(e){
		processWallet();
	});

	$('#paymodes').on('click', '#dcSaveCardLabel', function(e){
		if($(this).data("checked")) {
			$("#dcStoreCardWrapper input").val("Y");
		} else {
			$("#dcStoreCardWrapper input").val("N");
		}
	});

	$('#paymodes').on('click', '#ccSaveCardLabel', function(e){
		if($(this).data("checked")) {
			$("#ccStoreCardWrapper input").val("Y");
		} else {
			$("#ccStoreCardWrapper input").val("N");
		}
	});

	$('#paymodes').on('click', '#dcCvvBox', function(e){
		$('#showHideDiv').show();
	});

	$('#paymodes').on('click', '#ccCvvBox', function(e){
		var cardNumber = $('#cn').val();
		var cardType = getCardType(cardNumber);
		if (cardType == "amex") {
			$('#amexShowHideDiv').show();
		} else {
			$('#ccShowHideDiv').show();
		}
	});

	$('#paymodes').on('click','#mmid', function(e){
		$('#impsMmidShowHideDiv').show();
	});

	$('#paymodes').on('click', '#otp', function(e){
		$('#impsOtpshowHideDiv').show();
	});

	$('#paymodes').on('keyup', '#cn1', function() {
		var cardNumber = $(this).val();
		$('#maestroOpt').hide();
		$('#dcStoreCardWrapper').show();
		$.proxy(changeCardIconDC,this)(cardNumber);
	});
	
	$('#paymodes').on('keyup', '#rewardsCardNo', function() {
		var cardNumber = $(this).val();
		
		$.proxy(changeCardIconDC,this)(cardNumber);
	});
	
	$("#rewards .btn-change-rewards-details").click(function(){
		$(".rewards-otp-form").hide();
		$(".rewards-card-form").show();
		$("#rewardsAction").val("CARD_INPUT");
		return false;
	});
	
	function changeCardIconDC (cardNumber){
		var cardType = getCardType(cardNumber);
		if (cardType == "maestro") {
			$('#dcStoreCardWrapper').hide();
			$("#dcStoreCardWrapper input").val("Y");
			$("#dcSaveCardLabel .ui-checkbox").dgCheck("#dcSaveCardLabel");
			$('#maestroOpt').show();			
		} 
		
		if (cardType == "amex" || cardType == "diners")
			cardType = "INVALID CARD";
		
		var icon = cardType == "INVALID CARD" ? "d" : cardType;
		$(this).removeClass('d maestro master visa diners');
		$(this).addClass(icon);
			
	}


	$('#paymodes').on('keyup', '#cn', function() {
		var cardNumber = $(this).val();
		$('#ccStoreCardWrapper').show();
		var cardType = getCardType(cardNumber);
		if (cardType == "amex") {
			$("#ccStoreCardWrapper").hide();
			$("#ccStoreCardWrapper input").val("Y");
			$("#ccSaveCardLabel .ui-checkbox").dgCheck("#ccSaveCardLabel");
		} 
		
		var icon = cardType == "INVALID CARD" ? "c" : cardType;
		$(this).removeClass('c amex master visa diners');
		$(this).addClass(icon);
	});

	
	//Net Banking panel
	$('#paymodes').on('click', '.netbanking-panel li div', function() {	
		var bankName = $(this).attr('id');
		var displayName = $(this).attr('title');
		$("#nbSelect option[value='-1']").attr('selected', 'selected');
		//$('#nbSelect').selectmenu('refresh');
		$('#bankCode').val(bankName);
		
		$('.netbanking-panel li > div').addClass('bank');
		$('.netbanking-panel li > div').removeClass('bank-select');
		$(this).addClass('bank-select');
		$(this).removeClass('bank');
		
		$('#warningDiv').hide();
		$('#errorMsg').text("");
		$("#proceedButton").parent().parent().removeClass("disable");
		$("#proceedButton").attr("disabled", false);
		if(maintainenceNBBank[bankName]) {
			$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#warningDiv').show();
			$("#proceedButton").parent().parent().addClass("disable");
			$("#proceedButton").attr("disabled", true);
		} else if(lowPerfNBBank[bankName]) {
			$('#warningDiv').show();
			$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		} else {
			// auto proceed on selection
			var siteTheme = $("#siteTheme").data("value") || false; 
			if(siteTheme && siteTheme == "paytmApp"){
				$("form[name=netbanking-form]").submit()
			} else {
				scrollToBottom();
			}
		}
		
	});


	$('#paymodes').on('change','#nbSelect', function() {
		var bankName = $(this).val();
		var displayName = $('#nbSelect option:selected').text();
		$('#bankCode').val(bankName);
		
		$('.netbanking-panel li div').addClass('bank');
		$('.netbanking-panel li div').removeClass('bank-select');
		
		$('#warningDiv').hide();
		$('#errorMsg').text("");
		if(maintainenceNBBank[bankName] || bankName == "-1") {
			$("#proceedButton").parent().parent().addClass("disable");
			$("#proceedButton").attr("disabled", true);
		} else {
			$("#proceedButton").parent().parent().removeClass("disable");
			$("#proceedButton").attr("disabled", false);
		}
		if(maintainenceNBBank[bankName]) {
			$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#warningDiv').show();
		} else if(lowPerfNBBank[bankName]) {
			$('#warningDiv').show();
			$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		} else {
			// auto proceed on selection
			var siteTheme = $("#siteTheme").data("value") || false; 
			if(siteTheme && siteTheme == "paytmApp"){
				$("form[name=netbanking-form]").submit()
			}
		}
		
	});

	// ATM card tab
	$('#paymodes').on('click','.atm-panel li div', function() {
		var bankName = $(this).attr('id');
		var displayName = $(this).attr('title');
		$('#atmBankCode').val(bankName);
		
		$('.atm-panel li div').addClass('bank');
		$('.atm-panel li div').removeClass('bank-select');
		$(this).addClass('bank-select');
		$(this).removeClass('bank');
		
		$('#atmWarningDiv').hide();
		$('#atmErrorMsg').text("");
		$("#proceedButton").parent().parent().removeClass("disable");
		$("#proceedButton").attr("disabled", false);
		if(maintainenceATMBank[bankName]) {
			$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#atmWarningDiv').show();
			$("#proceedButton").parent().parent().addClass("disable");
			$("#proceedButton").attr("disabled", true);
		} else if(lowPerfATMBank[bankName]) {
			$('#atmWarningDiv').show();
			$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		} else {
			// auto submit on selection
			var siteTheme = $("#siteTheme").data("value") || false; 
			if(siteTheme && siteTheme == "paytmApp"){
				$("form[name=atm-form]").submit()
			} else {
				scrollToBottom();
			}
		}
		
		
	});
	
	
	$('#paymodes').data('disabled', false);
	
	openPreferedPaymentMode();
	
	$("#proceedButton").attr("disabled", false);
	$(".radio").dgStyle();
	$(".checkbox").dgStyle();
	if($('#walletBalance').length > 0) {
		if(!isCashBackPromocode && $("#pc :checkbox").is(":checked")) {
			$('#pc .ui-checkbox').dgCheck("#pc");
		}
		processWallet();
	}

	/*try {
		trackEvent("onload");
	} catch(e){}*/
});

function trackEvent(e){
	 var txnTransientId = $("#txnTransientId")[0].getAttribute("value");
	 
	 if(e == "onload"){
		 $.post("/oltp/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
			var status = res;
		 });
	 }
}

function openPreferedPaymentMode(mode){
	var paymentMode = mode || $('#prefered-payment-mode').data("value");
	
	var modeElm = null;
	switch(paymentMode) {
		case 5:
			modeElm = $("#saveCard");
		break;
		case 2:
			modeElm = $("#debitCard");
		break;
		case 1:
			modeElm = $("#creditCard");
		break;
		case 8:
			modeElm = $("#atmCard");
		break;
		case 3:
			modeElm = $("#netbanking");
		break;
		case 6:
			modeElm = $("#imps");
		break;
		case 10:
			modeElm = $("#itzcash");
		break;
		case 11:
			modeElm = $("#rewards");
		break;
	} 
	
	if(modeElm)
		modeElm.find(".ui-collapsible-heading").click();
}

function getCardType(cardNumber) {
	if (cardNumber.length == 19 || cardNumber.substring(0, 4) == "6220" || cardNumber.substring(0, 6) == "504834"
		|| cardNumber.substring(0, 6) == "508159" || cardNumber.substring(0, 6) == "589458" ) {
		return "maestro";
	}else if (cardNumber.substring(0, 2) == "30" || cardNumber.substring(0, 2) == "36" || cardNumber.substring(0, 2) == "38" || cardNumber.substring(0, 2) == "35") {
		return  "diners";
	} else if (cardNumber.substring(0, 2) == "34" || cardNumber.substring(0, 2) == "37") {
		return  "amex";
	} else if (cardNumber.substring(0, 1) == "4") {
		return  "visa";
	} else if (cardNumber.substring(0, 2) == "51" || cardNumber.substring(0, 2) == "52" || cardNumber.substring(0, 2) == "53"
		|| cardNumber.substring(0, 2) == "54" || cardNumber.substring(0, 2) == "55") {
		return  "master";
	} else if (cardNumber.substring(0, 4) == "6011") {
		return  "DISCOVER";
	} else {
		return  "INVALID CARD";
	}
}

function processWallet() {
	var totalAmount = $('#totalAmtVal').val() * 1 ;
	var walletBalance = $('#totalWalletVal').val() * 1;
	var paidAmount = totalAmount;
	var remWalletBalance = 0;
	if($("#pc").data("checked")) {
		if(walletBalance >= totalAmount) {
			$('#paymodes').data('disabled', true);
			$('.fullWalletDeductDiv').show();
			
			// close all accordion tabs
			$('.ui-collapsible-heading .ui-icon').removeClass("ui-icon-arrow-d");
			$(".ui-collapsible-content").hide();
			
			paidAmount = 0;
			remWalletBalance = walletBalance - totalAmount;
			$('input[name=walletAmount]').val(totalAmount);
			$('.fullWalletDeduct').show();
			$('.otherText').hide();
		} else {
			paidAmount = totalAmount - walletBalance;
			$('input[name=walletAmount]').val(walletBalance);
			$('.fullWalletDeductDiv').hide();
			$('.fullWalletDeduct').hide();
			$('.otherText').show();
		}
		//$('#yourBal').hide();
		$('#yourBal .amt').text(walletBalance);
		$('#walletBalanceSpan').text(walletBalance - remWalletBalance);
		$('#walletBalance').show();
		$("#balance").show();
		$("#Notbalance").hide();
		// CVV Focus on Tab change
		if($("#saveCard .ui-collapsible-content").css("display")=='block'){
			$("#saveCard .scCvvInput").focus();
		}
	} else {
		$('#paymodes').data('disabled', false);
		paidAmount = totalAmount;
		$('#yourBal .amt').text(walletBalance);
		$('input[name=walletAmount]').val('0');
		$('.fullWalletDeductDiv').hide();
		$('.fullWalletDeduct, #walletBalance, #remBal').hide();
		$('.otherText, #yourBal').show();
		$("#balance").hide();
		$("#Notbalance").show();
		// open first mode
		// $('.ui-collapsible-heading a').first().click();
		openPreferedPaymentMode();
	}
	$('.finalAmountSpan').html(paidAmount);
}

function scrollToBottom() {
	$(document).scrollTop($(document).height());
}

