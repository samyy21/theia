$(document).ready(function(){
	// Merchant payment modes
	// Add money payment modes
	
	var walletAvailable = $("#wallet-details").data("available");
	var walletBalance = $("#wallet-details").data("balance");
	var isLoggedIn = $("#login-details").data("value");
	var isWalletOnly = $("#wallet-details").data("wallet-only");
	var saveCardAmount = $("#payment-details").data("save-card-amount");

	if(walletBalance)
		walletBalance = Math.round(walletBalance * 100) / 100;
	
	var txnAmount = $("#payment-details").data("amount");
	var addMoneyAvailable = $('#addMoney-details').data("available");
	var addMoneySelected = $('#addMoney-details').data("selected");
	var useWallet = false;
	var hybridPaymentAllowed = false;
	
	var currentPaymentType = $("#current-payment-type").data("value");
	var errorsAvailable = $("#error-details").data("available");
    var csrf = $("#csrf").data("value");
	
	// setup tabs/cards
	$(".cards-control .card").click(function(){
		$(this).siblings().removeClass("active");
		$(this).addClass("active");
		
		var tabs = $(this).parents(".cards-tabs");
		tabs.find(".cards-content .content").removeClass("active");
		
		var contentId = $(this).find("a").attr("href");
		tabs.find(contentId).addClass("active");
		
		
		var cardsControl = tabs.find(".cards-control");
		cardsControl.click();
		
		if($(window).width() < 600){
			cardsControl.find(".grid").hide();
			cardsControl.find(".selection .tab-name").html($(this).find("a").text());
			cardsControl.find(".selection").addClass("active");
			
			window.setTimeout(function(){
				$(window).scrollTop(tabs.position().top + cardsControl.position().top + 30);
			}, 50);
		}
		
		// uncheck any select saved card
		var checkbox = tabs.find(".sc-checkbox:checked");
		if(checkbox.length){
			checkbox[0].checked = false;
			checkbox.checkbox("checkChecked").trigger("change", true);
		}
			
		
		return false;
	});
	
	// setup checkbox
	$('.checkbox').checkbox();
	
	
	// setup nb and atm checkbox as radio btns
	$(".banks-panel .checkbox").on("change", function(){
		setTimeout(function(){
			$(".banks-panel .checkbox").checkbox("checkChecked");
		}, 100);
		
	});
	
	$(".banks-panel .bank-logo").click(function(){
		$(this).parents("li").find(".checkbox").checkbox("click");
	});
	
	// card border coloring
	$('.paytmcash-card .checkbox').on("change", checkBorderStatus);
	
	function checkBorderStatus(){
		var checked = $(this)[0].checked;
		var card = $(this).parents(".card");
		if(checked){
			card.addClass("active");
			card.find(".btn-submit").addClass("active");
		} else {
			card.removeClass("active");
			card.find(".btn-submit").removeClass("active");
		}
	};
	
	$('input').bind("cut copy paste",function(e) {
        e.preventDefault();
    });

	
	$('input[id=ccSubmit],input[id=dcSubmit], input[id=impsSubmit], input[id=nbSubmit], input[id=itzCashSubmit]').attr('disabled', true).disabled(true);

	
	
	// setup saved cards //
	
	// border colors
	$('.sc-cards .checkbox').on("change", function(e, isTriggered){
		var scCards = $(this).parents(".sc-cards");
		var cardTabs = $(this).parents(".cards-tabs");
		
		scCards.find(".checkbox").each(checkBorderStatus);
		
		// open first tab when sc is unchecked
		if(!isTriggered){
			var checkbox = scCards.find(".sc-checkbox:checked");
			if(checkbox.length == 0){
				cardTabs.find(".cards-control .card").eq(0).click();
			}
		}
	});
	
	// checkboxes
	$(".sc-cards .checkbox").on("change", function(){
		$(this).parents(".sc-cards").find(".checkbox").checkbox("checkChecked");
	});
	
	// delete card
	$("body").on('click', '.deleteCard', function() {
		var savedCardId = $(this).attr('cardId');
        $.ajax({
            url : "DeleteCardDetails",
            type: "POST",
            headers: { 'X-CSRF-TOKEN': csrf },
            data : {
                "savedCardId": savedCardId
            },
            success: function(data, status, jqXHR) {
                if ('success' == status) {
                    $(".deleteCard[id=delete-" + savedCardId + "]").parents(".control-group").remove();
                    // TODO : auto select next saved card
                    checkSavedCardsCount();
                }
            }
        });
		return false;
	});
	
	// enable submit btn on cvv fill
	$(".scCvvInput").on("keyup", function(){
		var cvv = $(this).val();
		var card = $(this).parents(".card");
		var state;
		if(!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)){
			state = false;
		} else {
			state = true;			
		}
		card.find("#scSubmit").disableButton(state);
	});
	
	
	// open / close card
	$("input.sc-checkbox").on("change", function(e) {

		var form = $(this).parents("form");
		var card = $(this).parents(".card");
		var tab  = $(this).parents(".cards-tabs");
		var cards = $(this).parents('.sc-cards');
		
		form.find("#scCvvBox")[0].value = "";
		form.find(".saveCardCvvDiv").hide();
		
		// if card is selected
		var checkbox = this;
		setTimeout(function(){
			
			if(checkbox.checked){
				
				// uncheck other save cards
				cards.find("input.sc-checkbox").not(checkbox).each(function(){
					if(this.checked){
						this.checked = false;
						$(this).trigger("change");
					}
				})
				
				// set value hidden field
				var saveCard = $(checkbox).attr('id');
				form.find("#savedCardId")[0].value=saveCard;
				
				// show cvv box
				form.find("#cvvDiv-" + saveCard).show();
				
				// close other modes tabs
//				tab.find(".cards-control .card").removeClass("active");
//				tab.find(".cards-content .content").removeClass("active");
//				if($(window).width() < 600){
//					tab.find(".cards-control .selection").removeClass("active");
//					tab.find(".cards-control .grid").hide();
//				}
			}
			
		}, 100);
		
		
		form.find(".scCvvError").hide();
		form.find(".scCvvInput").removeClass("error1").focus(); 
		
		//card.find("#scSubmit").attr("disabled", true);
	});
	
	// submit
	$("body").on('click', '#scSubmit', function(e){
		var form = $(this).parents("form");
		var card = $(this).parents(".card");
		
		// set cvv in hidden field 
		var cvv = card.find(".scCvvInput").val();
		form.find("#scCvvBox")[0].value = cvv;
		
		form.submit();
	});
	
	// check first saved cards
	setTimeout(function(){
		if(!errorsAvailable)
			$('.sc-checkbox').eq(0).checkbox("click");
	}, 100);
	
	
	// setup saved cards //
	

	
	// setup dc form validations //
	
	$(".dc-form").on("change keyup", validateDCForm);
	
	function validateDCForm(){
		var form = $(this);
		
		var month = form.find(".dcExpMonth").val();
		var isDCExpMonthValid = month == "0" ? false : true;
			
		var year = form.find(".dcExpYear").val();
		var isDCExpYearValid = year == "0" ? false : true;
			
		var cvv = form.find(".dcCvvBox").val();
		var isDCCvvValid = (!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) ? true  : false;
			
		var isDCNumberValid = false;
		var cardInput = form.find('.dcCardNumber');
		var cardNumber = $.trim(cardInput.val());
		cardNumber = cardNumber.replace(/ /g, "");
		if(!isNaN(cardNumber) && cardNumber.length >=12 && cardNumber.length <=19) {
			form.find(".dc-cvv-clue").removeClass("hide");
			isDCNumberValid = true;
		}

		form.find('#dcStoreCardWrapper').show();
		form.find('#maestroOpt').hide();
		
		var cardType = getCardType(cardNumber);

		if (cardType == "maestro") {
			form.find('#dcStoreCardWrapper').hide();
			form.find(".dc-cvv-clue").addClass("hide");
			form.find('#maestroOpt').show();
		}
		
		if (cardType == "amex" || cardType == "diners")
			cardType = "INVALID CARD";
				
		var icon = cardType == "INVALID CARD" ? "d" : cardType;
		cardInput.removeClass('d maestro master visa diners');
		cardInput.addClass(icon);
		
		if(isDCNumberValid) {
			if(form.find('.dcCardNumber').val().replace(/\s+/g,'').length == 19) {
				disableDCSubmit(form, false);
			} else {
				if(isDCExpMonthValid && isDCExpYearValid && isDCCvvValid) {
					disableDCSubmit(form, false);
				} else {
					disableDCSubmit(form, true);
				}
			}
		} else {
			disableDCSubmit(form, true);
		}
	};
	
	function disableDCSubmit(form, state){
		form.find("#dcSubmit").disableButton(state);
		return false;
	}
	
	// setup dc form validations //
	
	// number spacing
	$("#cn, #cn1").on("keypress", function(e){
		var $target = $(e.target);
		digit = String.fromCharCode(e.which);
	    if (!/^\d+$/.test(digit)) {
	    	if(e.which !== 8){
	    		e.preventDefault();
	    	}
	      return;
	    }
		var value = $target.val();
		var maxLen = parseInt($target.attr("maxlength"));
		var re = /(?:^|\s)(\d{4})$/;
		
//		if(value.length == maxLen)
//			return e.preventDefault();
		 
	    if (re.test(value) && value.length < maxLen -1) {
	      e.preventDefault();
	      return $target.val(value + ' ' + digit);
	    } else if (re.test(value + digit) && value.length < maxLen -1) {
	      e.preventDefault();
	      return $target.val(value + digit + ' ');
	    }
	    
	});
	
	$("#cn, #cn1").on("keydown", function(e) {
	    var $target, value;
	    $target = $(e.currentTarget);
	    value = $target.val();
	    if (e.meta) {
	      return;
	    }
	    if (e.which !== 8) {
	      return;
	    }

	    if ((/\d\s$/).test(value)) {
	      e.preventDefault();
	      $target.val(value.replace(/\d\s$/, ''));
	    } else if((/\s\d?$/).test(value) == true) {
	    	$target.val(value.replace(/\s\d?$/, ''));
	    	e.preventDefault();
	    }
	});


	// update card num in hidden field
	$("#cn, #cn1").on("keyup", function(e){
		var $target = $(e.target);
		var value = $target.val();
		$target.siblings('input[type=hidden]').val(value.replace(/ /g, ""));
	});
	
	// setup cc form validations //
	
	// only numeric cvv
	$("input[name=cvvNumber], .scCvvInput").on("keypress", function(e){
		digit = String.fromCharCode(e.which);
	    if (!/^\d+$/.test(digit)) {
	    	e.preventDefault();
	      return;
	    }
	});
	
	$(".cc-form").on("change keyup", validateCCForm);
	
	function validateCCForm(){
		var form = $(this);
		
		var month = form.find('.ccExpMonth').val();
		var isCCExpMonthValid = month == "0" ? false : true;
			
		var year = form.find('.ccExpYear').val();
		var isCCExpYearValid = year == "0" ? false : true;
		
		var cvv = form.find(".ccCvvBox").val();
		var isCCCvvValid = (!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) ? true : false;
		
		var isCCNumberValid = false;
		var cardInput = form.find(".ccCardNumber");
		var cardNumber = $.trim(cardInput.val());
		cardNumber = cardNumber.replace(/ /g, "");
		var cardType = getCardType(cardNumber);
		if(!isNaN(cardNumber) && cardNumber.length >=12 && cardNumber.length <=19) {
			// show cvv image
			if (cardType == "amex")
				form.find(".cc-cvv-clue.amex-clue").removeClass("hide");
			else
				form.find(".cc-cvv-clue.default-clue").removeClass("hide");
		
			isCCNumberValid = true;
		} else {
			form.find(".cc-cvv-clue").addClass("hide");
			isCCNumberValid = false;
		}
		
		form.find('#ccStoreCardWrapper').show();
		
		if (cardType == "amex") {
			form.find("#ccStoreCardWrapper").hide();
		}
		
		var icon = cardType == "INVALID CARD" ? "c" : cardType;
		cardInput.removeClass('c amex master visa diners');
		cardInput.addClass(icon);
		
		
		if(isCCNumberValid) {
			if(form.find(".ccCardNumber").val().replace(/\s+/g,'').length == 19) {
				disableCCSubmit(form, false);
			} else {
				if(isCCExpMonthValid && isCCExpYearValid && isCCCvvValid) {
					disableCCSubmit(form, false);
				} else {
					disableCCSubmit(form, true);
				}
			}
		} else {
			disableCCSubmit(form, true);
		}
		
	};
	
	function disableCCSubmit(form, state){
		form.find("#ccSubmit").disableButton(state);
		return false;
	}
	
	// setup cc form validations //

	
	
	// setup nb form validations //
	
	// when netbanking popular bank is selected 
	$('.netbanking-panel input.bankRadio').on("change", function(e){
		var form = $(this).parents("form");
		
		var selectElm = form.find(".nbSelect")
		if(selectElm.length)
			selectElm[0].selectedIndex = 0;
		var bankName = $(this).parent().attr('id');
		var displayName = $(this).parent().attr('title');
		form.find('#bankCode').val(bankName);
		form.find('#warningDiv').hide();
		form.find('#errorMsg').text("");
		form.find('#nbcardId').hide();
		form.find("#nbSubmit").removeAttr("disabled").disabled(false);
		
		if(maintainenceNBBank[bankName]) {
			form.find('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			form.find('#warningDiv').show();
			form.find('#nbSubmit').disableButton(true);
			//$('#nbSubmit').removeClass('blue-btn').addClass('gry-btn');
		} else if(lowPerfNBBank[bankName]) {
			form.find('#warningDiv').show();
			form.find('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});
	
	// when netbanking other bank is selected
	$(".nbSelect").on("change", function(e){
		var form = $(this).parents("form");
		
		// uncheck any bank in popular list
		var elm = form.find('input.bankRadio:checked');
		if(elm && elm[0])
			elm[0].checked = false;
		// refresh
		$(".banks-panel .checkbox").checkbox("checkChecked");
		
		var bankName = $(this).val();
		var displayName = $(this).find('option:checked').text();
		//$(".netbanking-panel li div a.checked").removeClass("checked");
		form.find('#bankCode').val(bankName);
		form.find('#warningDiv').hide();
		form.find('#errorMsg').text("");
		form.find('#nbcardId').hide();
		var state = (bankName == -1) ? true : false;
			
		form.find('#nbSubmit').disableButton(state);
		
		if(maintainenceNBBank[bankName]) {
			panel.find('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			panel.find('#nbSubmit').disableButton(true);
			panel.find('#warningDiv').show();
		} else if(lowPerfNBBank[bankName]) {
			panel.find('#warningDiv').show();
			panel.find('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});
	
	// setup nb form validations //

	// setup atm form validations //
	
	$("body").on('click', '.atm-panel li div', function() {
		var form = $(this).parents("form");
		form.find('.atm-panel li div a.checked').removeClass('checked');
		$(this).find('a').addClass("checked");
		var bankName = $(this).attr('id');
		var displayName = $(this).attr('title');
		form.find('#atmBankCode').val(bankName);
		form.find('#atmWarningDiv').hide();
		form.find('#atmErrorMsg').text("");
		form.find('#atmcardId').hide();
		form.find('#atmSubmit').removeAttr("disabled").disabled(false);
		
		if(maintainenceATMBank[bankName]) {
			form.find('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			form.find('#atmWarningDiv').show();
			form.find('#atmSubmit').disableButton(true);
		} else if(lowPerfATMBank[bankName]) {
			form.find('#atmWarningDiv').show();
			form.find('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});

	// setup atm form validations //
	
	
	// Setup login //
	

	$("body").on('click', '#login-btn', function(){
		showAuthView("login");
	});
	
	$("body").on('click', '#register-btn', function() {
		showAuthView("register");
	});
	
	// show login/register popup
	// type = "login"/"register", default - "login"
	function showAuthView(type) {
		
		var showRegisterView = false;
		
		if(type == "register")
			showRegisterView = true;
		
		var authConfig = $("#auth-config").data("value");
		var hostUrl = authConfig.host;
		
		var params = [];

		params.push('response_type=code');
		params.push('scope=paytm');
		params.push('theme=' + (authConfig.login_theme || 'pg'));

		var client_id = authConfig.client_id;
		params.push('client_id=' + client_id);

		var returnUrl = authConfig.return_url;
		params.push('redirect_uri=' + returnUrl);

		var view = "/oauth2/authorize";
		if(showRegisterView == true)
			view = "/register";
		

		var src = hostUrl + view + "?" + params.join('&');
		
		// check for wap version
		// redirect
		if($(window).width() < 600){
			window.location = src;
			return false;
		}
		
		// open popup
		$('#login-modal').addClass('md-show');
		$('#login-iframe').attr('src', src);
		
		// fix For IE
		if($.support){
			if(!$.support.opacity){
				$('.md-overlay').css({ "filter" : "alpha(opacity=60)"});
			}
		}
		
		// setCentered
		// set position to center the popup
		var topMargin = $('#login-modal').height() / 2;
		var leftMargin = $('#login-modal').width() / 2;

		$('#login-modal').css({ "margin-top" : -topMargin, "margin-left" : -leftMargin});
	}

	$("body").on('click', '.closePop', function() {
		$('#login-modal').removeClass('md-show');
		$('.md-overlay').removeClass('md-overlay-show');
		$('#login-iframe').attr('src', '');
	});
	
	// Setup login //

	function getCardType(cardNumber) {
		if (cardNumber.length == 19 || cardNumber.substring(0, 4) == "6220" || cardNumber.substring(0, 6) == "504834"
			|| cardNumber.substring(0, 6) == "508159" || cardNumber.substring(0, 6) == "589458" ) {
			return "maestro";
		}else if (cardNumber.substring(0, 2) == "30" || cardNumber.substring(0, 2) == "36" || cardNumber.substring(0, 2) == "38") {
			return  "DINERS CLUB/CARTE BLANCHE";
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
	
	function bindOnceBlur(){
		$("#merchant-payment-modes > .blur-overlay").one("click", clickOnBlured);
		$("#paytm-cash-checkbox").one("change", clickOnBlured);
	}
	
	function clickOnBlured(){
		$("#merchant-payment-modes > .blur-overlay").off("click", clickOnBlured);
		$("#paytm-cash-checkbox").off("change", clickOnBlured);
		$("#paytm-cash-checkbox").checkbox("click");
	}
	
	
	function initPayments(){
		// for subscription
		if(!isLoggedIn)
			return false;
		
		// on paytmcash checkbox change
		$("body").on('change', '#paytm-cash-checkbox', function(e){
			useWallet = $("#paytm-cash-checkbox")[0].checked;
			processPayments();
		});
		
		
		// use wallet by default (if available)
		if(walletAvailable){
			useWallet = true;
		}
	
		// set class on disabled elems
		$('*[disabled=disabled]').disabled(true);
		
		processPayments();
		
		checkSavedCardsCount();

	};
	
	function checkSavedCardsCount(){
		// hide/show msg for other paymnt methods
		try {
			var num = $('.sc-cards .control-group.card').length;
			if(num == 0){
				$('a[href="#sc-card"]').parent().remove();
				$('.cards-control').each(function(){
					$(this).find(".card").eq(0).click();
				});
			}
		}catch(e){}
			
	};
	
	
	// payments starts here...
	initPayments();
	
	// rebuilds ui
	function processPayments() {
		
		
		var totalAmount = txnAmount;
		
		var paidAmount = totalAmount;
		var remWalletBalance = 0;

		$("#totalAmountSpan, .totalAmountSpan").text(numberWithCommas(totalAmount));
		
		if(walletAvailable){
			$('#yourBal').show();
			$('#yourBal .amt').text(numberWithCommas(walletBalance));
			
			// calculate balance used
			var balanceUsed = walletBalance >= totalAmount ? totalAmount : walletBalance;
			$("#balance-used").text(numberWithCommas(balanceUsed));
		}
		
		// identify mode 
		var mode = "";
		
		if(walletBalance >= totalAmount) { // sufficient balance
			mode = "full-wallet"; // full wallet payment and select card
		} else { // insufficient balance
			mode = "default"; //partial wallet and save a card
			
		}
		
		
		
		// set mode based on current payment type (used in case of error showing)
		if(errorsAvailable){
			errorsAvailable = false;
			
			// case - errors + no-wallet
			if(mode == "no-wallet"){
				disablePaytmCash();
			}
			
			if(addMoneySelected){
				// dont change mode
				openPreferedModeTab(mode);
			} else {
				mode = "default";
			}
		}
		
		// check paytmcash checkbox for valid modes
		if(_.indexOf(["full-wallet", "default"], mode) != -1){
			var paytmCashCheckbox = $("#paytm-cash-checkbox");
			// click checkbox if its not clicked
			if(paytmCashCheckbox.length && paytmCashCheckbox[0].checked == false){
				$("#paytm-cash-checkbox").checkbox("click");
				return false;
			}
		}
		
		// hide add money modes by default
		$("#merchant-payment-modes").removeClass("hide");
		$("#add-money-payment-modes").addClass("hide");
		
		switch(mode){
			case "default" : 
				//unblurAllModes();
				paidAmount = totalAmount - walletBalance;
				$("#merchant-payment-modes").addClass("hide");
				$("#add-money-payment-modes").removeClass("hide");
				
				$('input[name=walletAmount]').val(walletBalance);
				
				$('#yourBal .amt').text(numberWithCommas(walletBalance));
				
				//$(".addMoneyText").removeClass("hide"); 
				$(".subs-complete-payment").removeClass("hide");
				$(".addMoneyAmount").text(numberWithCommas(paidAmount));
				
				$('.balance-used-box').removeClass("hide");
				$('#walletBalance, #remBal').hide();
				$('.bankPaymentText, #yourBal').show();
				$(".fullWalletDeduct").addClass("f-hide");
				
				$("#payment-user-msg .msg").addClass("hide");

				openPreferedModeTab();
			break;
				
			case "full-wallet" :
				paidAmount = totalAmount;
				$("#merchant-payment-modes").addClass("hide");
				$("#add-money-payment-modes").removeClass("hide");
				
				$('input[name=walletAmount]').val(totalAmount);
				$('.fullWalletDeduct').addClass("f-hide");
				$('.bankPaymentText').hide();
				
				//$(".addMoneyAmount").text(numberWithCommas(paidAmount));
				$(".saveCardAmount").text(numberWithCommas(saveCardAmount));
				$('.balance-used-box').removeClass("hide");
				// show heading text
				//$(".addMoneyText").removeClass("hide");
				
				// if saved cards are available
				if($("input.sc-checkbox").length){
					$(".subs-select-card").removeClass("hide");
					$(".storeCardWrapper").append($(".subs-min-amt-msg").eq(0).clone().removeClass("hide"));
				} else {
					$(".subs-save-card").removeClass("hide");
				}
				
				// hide cvv box and enable save card submit
				$('.scCvvInput').parents('li').hide();
				$('input[id=scSubmit]').attr("disabled", false).disabled(false); 
			break;
		}
		
		$('.finalAmountSpan').html(numberWithCommas(paidAmount));
		
	}
	
	
	function openPreferedModeTab(mode){
		var showTab = null;
		
		switch(currentPaymentType){
			case 5 : // sc
				/// check sc
				//hide other-methods-msg
				showTab = "sc";
			break;
			case 1 : // cc
				showTab = "cc";
			break;
			case 2 : // dc
				showTab = "dc";
			break;
			case 3 : // nb
				showTab = "nb";
			break;
			case 8 : // atm
				showTab = "atm";
			break;
			case 6 : // ims
				showTab = "imps";
			break;
		}
		
		if(showTab){
			$('#add-money-payment-modes a[href="#'+ showTab +'-card"]').click();
		}
		
	};
	
	
	function checkPaytmCashCheckbox(state){
		state = state || false;
		
		var paytmCashCheckbox = $("#paytm-cash-checkbox");
		if(paytmCashCheckbox.length && paytmCashCheckbox[0].checked != state){
			$("#paytm-cash-checkbox").checkbox("click");
		}
	}
	
	function disablePaytmCash(){
		//$(".paytmcash-card").addClass("blured");
		$('.fullWalletDeduct').addClass("f-hide");
		$('#remBal').hide();
		$(".paytmcash-card .blur-overlay").addClass("show");
		$('.balance-used-box').addClass("hide");
	}
	
	function enablePaytmCash(){
		//$(".paytmcash-card").removeClass("blured");
		$('.fullWalletDeduct').removeClass("f-hide");
		//$('#remBal').show();
		$('.balance-used-box').removeClass("hide");
		$(".paytmcash-card .blur-overlay").removeClass("show");
	}
	
	
	function blurAllModes(){
		
		//toggleSavedCards(false);
		toggleMerchantModes(false);
	}
	
	function unblurAllModes(){
		
		//toggleSavedCards(true);
		toggleMerchantModes(true);

	}
	
	function toggleSavedCards(show){
		var merchant = $("#merchant-payment-modes");
		if(show == false){
			merchant.find(".sc-cards .blur-overlay").addClass("show");
			//merchant.find(".sc-cards").removeClass("active").addClass("blured");
			merchant.find(".scCvvInput").blur();
		} else {
			//merchant.find(".sc-cards").removeClass("blured");
			merchant.find(".sc-cards .blur-overlay").removeClass("show");
			merchant.find(".scCvvInput").focus();
		}
	}
	
	function toggleMerchantModes(show){
		var merchant = $("#merchant-payment-modes");
		var control = merchant.find(".cards-control");
		var overlay = $("#merchant-payment-modes > .blur-overlay");
		
		if(show == false){
			//control.addClass("blured");
			overlay.addClass("show");
			merchant.find(".cards-control .card").removeClass("active");
			merchant.find(".cards-content .card").removeClass("active");
		} else {
			//control.removeClass("blured");
			overlay.removeClass("show");
			
			// activate first mode
			if(merchant.find(".cards-control .card.active").length == 0)
				merchant.find(".cards-control .card").first().click();
		}
		
	}
	
	// setup payment details btn //
	$(".btn-show-payment-details").click(function(){
		$('.payment-details').toggle();
		$(this).hide();
		$(".btn-hide-payment-details").show();
		return false;
	});
	
	$(".btn-hide-payment-details").click(function(){
		$('.payment-details').toggle();
		$(this).hide();
		$(".btn-show-payment-details").show();
		return false;
	});
	// setup payment details btn //
	
	
	// setup notification popover
	$("#login-btn, #register-btn, .popover-close a").click(function(){
		$(".login-popover").hide();
	});
	
	// setup wallet and wallet only //
	
	
	
	if(isWalletOnly){
		// auto open login popup
		$("#login-btn").click();
		
		// cancel txn on close popup
		$("body").one('click', '.closePop', function() {
			window.location.href = 'cancelTransaction';
		});
		
		// adjust footer
//		if($(window).width() > 600 && $(document).height() < $(window).height()){
//			$("#footer").addClass("fixed-bottom");
//			$("#footer-placeholder").removeClass("hide");
//		}
		
		$("#paytm-cash-checkbox").checkbox("toggleEnabled");
	}
	
	// setup wallet and wallet only //
	
	
	
	// setup subscription //
	if(!isLoggedIn){
		// open login popup
		$("#login-btn").click();
	}
	
	// disable save card checkbox
	$('input[name=storeCardFlag]').checkbox("toggleEnabled");
	
	// enable sc submit btn
	$('input[id=scSubmit]').disableButton(false);
	
	$(".paytmcash-card").removeClass("active");
	
	// hide checkbox
	$(".paytmcash-card .green-tick").hide();
	$(".paytmcash-card .text-box").removeClass("ml20");
	
	// setup subscription //
	
	
	
	
	// setup wap theme //
	
	var activeTab = $('.cards-control .grid .active');
	if(activeTab.length){
		$(".cards-control .selection .tab-name").html(activeTab.find("a").text());
		$(".cards-control .selection").addClass("active");
	}
	
	$(".cards-control .selection").click(function(){
		var cardsControl = $(this).parent();
		var cardsTab = $(this).parents(".cards-tabs");
		
		cardsControl.find(".grid").toggle();
		window.setTimeout(function(){
			$(window).scrollTop(cardsTab.position().top + cardsControl.position().top + 30);
		}, 50);
		return false;
	});
	
	// hide login modal
	if($(window).width() < 600){
		$("#login-modal").hide();
	}
	// setup wap theme //
	
}); // $ ready ends


//trim function
if(typeof String.prototype.trim !== 'function') {
  String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, ''); 
  }
}

// underscore utils
_ = {};
var ArrayProto = Array.prototype;
var nativeIndexOf = ArrayProto.indexOf;
_.indexOf = function(array, item, isSorted) {
    if (array == null) return -1;
    var i = 0, length = array.length;
    if (nativeIndexOf && array.indexOf === nativeIndexOf) return array.indexOf(item, isSorted);
    for (; i < length; i++) if (array[i] === item) return i;
    return -1;
  };
  
//jquery plugin to add/remove disabled class 
$.fn.disabled = function(state){
 
 state = state || false;
 
 if(state)
	 $(this).addClass("disabled");
 else
	 $(this).removeClass("disabled");
};

//jquery plugin to disable button
$.fn.disableButton = function(state){
	state = state || false;
	$(this).disabled(state);
	if(state)
		$(this).attr("disabled", "disabled");
	else
		$(this).removeAttr("disabled");
	
	return $(this);
};

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}