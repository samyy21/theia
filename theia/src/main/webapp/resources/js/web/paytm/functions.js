$(document).ready(function(){
	
	// promocode functionality
	try {
		
	var isCashBackPromocode = false;
	
	var promocodeType = $('#header').data('promocodetype');

	if($.trim(promocodeType) == "DISCOUNT"){
		// uncheck paytmcash
		$("#paytmcashbox").remove();
		$('#pc input.pcb').removeAttr("checked");
	}
	
	if($.trim(promocodeType) == "CASHBACK") {
		isCashBackPromocode = true;
		var netBankingAvailable = $('#nbContent').data('netbanking').toString().split(',');
		
		$('#verTabs li').hide();
		
		// uncheck paytmcash
		$('#pc input.pcb').removeAttr("checked");
		
		var promoCodes = [];
		
		if($('#cn1').data('promocodes'))
			promoCodes = $('#cn1').data('promocodes').toString().split(',');
		
		// hide tabs for unavailable modes
		var showTabs = $('#verTabs').data('showtabs').toString().split(',');
		
		var showSCTab = false;
		if(showTabs.indexOf('CC') != -1 || showTabs.indexOf('DC') != -1){
			showSCTab = true;
		}
		
		// check to show/hide SC tab	
		var savedCardsMatchedCount = 0;
		if(showSCTab == true){ // check cards
			savedCardsMatchedCount = $('#scContent .control-group:not(.hide)').length;
//			$('#scContent .control-group').addClass("hide");
//			$('#scContent .control-group').each(function() {
//				if(promoCodes.indexOf($.trim($(this).find('.savedCardLabel').data('firstsixdigits'))) > -1) {
//					$(this).removeClass("hide");
//					savedCardsMatchedCount++;
//				}	
//			});
		}
		
		if(savedCardsMatchedCount == 0) {
			showSCTab = false;
		} else {
			showTabs.push("SC");
		}
		
		for(var i=0; i< showTabs.length; i++){
			var tab = showTabs[i];
			var link = $("a[href=#" + tab.toLowerCase() + "Content]");
			link.parent().show().addClass("promo-mode");
		}
		
		// open first when now tab active
		if($("#verTabs .promo-mode.active").length == 0){
			$("#verTabs .promo-mode .a");
			var anchor = $("#verTabs .promo-mode a").eq(0);
			setTimeout(function(){
				anchor.click();
			}, 100);
		}
		
		// hide tabs for unavailable modes
		
		
		// hide nb banks that are not available
		if(netBankingAvailable.length) {

			// hide popular nb banks if not available
			var popularBanksAvailableCount = 0;
			$('#nbContent ul li').each(function() {
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
			$('#nbWrapper select option').each(function() {				
				var bankId = $(this).attr('value');
				
				if(netBankingAvailable.indexOf(bankId) == -1){
					$(this).attr("disabled", "disabled");
				} else {
					otherBanksAvailableCount++;
				}
			});
			
			// refresh dropdown or hide dropdown
			if(otherBanksAvailableCount > 0){
				$("#nbSelect").selectpicker({hideDisabled : true});
				$("#nbSelect").selectpicker("refresh");
			} else {
				$("#nbSelect").parent().hide();
			}
				
		} else {
			$('#netBanking').parent().hide();
		}
	}
	
	} catch (e){};
	// promocode functionality
	
	$(document).on('click','.show-other-options', function() {
		$('#nbWrapper select option').removeAttr('disabled');
		$('#nbContent ul li').show();
		$('.selectpicker').selectpicker('refresh');
		$("#label-pop-banks, #label-other-banks").show();
		$('#verTabs li').show();
		$('#scContent .control-group').removeClass("hide");
		$(this).parent().html($('.non-error-text').html());
		$('.non-error-text').hide();
		$('#showHideWallet').show();
	});
	
	$('input').bind("cut copy paste",function(e) {
        e.preventDefault();
    });

	$('input.pcb').prettyCheckable({customClass : 'fl1'});
	$('.selectpicker').selectpicker();
	$('#ccSubmit,#dcSubmit,#impsSubmit,#itzCashSubmit,#rewardsSubmit').attr('disabled', true);
	isMobileNoValid = isMMIDValid = isOTPValid = false;
	isItzCashValid = isItzPwdValid = false;
	isDCNumberValid = isDCCvvValid = isDCExpMonthValid = isDCExpYearValid = false;
	isCCNumberValid = isCCCvvValid = isCCNumberValid = isCCExpMonthValid = false;
	if($('#walletBalance').length > 0) {
		if(!isCashBackPromocode && $("#pc :checkbox").is(":checked")) {
			$('#pc a').addClass('checked');
		}
		processWallet();
	}
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
		number = number.replace(/ /g, "");
		
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
	
	// fix for browser save password
	$("form[name=creditcard-form]").submit(function(){
		$(this).find("#ccCvvBox, #dcCvvBox").attr("type", "text").hide();
	});
	
	// setup cluetip
	$('#clueTipBox').cluetip({splitTitle: '|', showTitle:true, cluetipClass: 'jtip', dropShadow:false});
	$('#clueTipBoxCC').cluetip({splitTitle: '|', showTitle:true, cluetipClass: 'jtip', dropShadow:false});
	$('#clueTipBoxAmex').cluetip({splitTitle: '|', showTitle:true, cluetipClass: 'jtip', dropShadow:false});
	

	// event bindings
	$('body').on('show', 'a[data-toggle="tab"]', function (e) {
		$('#clueTipBox, #clueTipBoxCC, #clueTipBoxAmex').mouseleave();
	});
	
	$("body").on('click', '.cvvRadio', function(e) {
		$("#scCvvBox")[0].value = "";
		
		var saveCard = $(this).attr('id');
		$("#savedCardId")[0].value=saveCard;
		$(".saveCardCvvDiv").hide();
		$("#cvvDiv-" + saveCard).show();
		$("#cvvDiv-" + saveCard).find('.scCvvInput').focus();
		
		$(".add-on").removeClass("savedCardSelect");
		$(this).parent().addClass("savedCardSelect");
		
		$(".savedCardLabel").removeClass("savedCardSelect1");
		$("#prependedradio-" + saveCard).addClass("savedCardSelect1").focus();
		$(".scCvvError").hide();
		$(".scCvvInput").removeClass("error1 savedCardSelect1");
		$("#prependedradio-" + saveCard).closest(".control-group").find(".scCvvInput").addClass("savedCardSelect1");
		
		//$("#scSubmit").attr("class","blue-btn");
		//$("#scSubmit").attr("disabled", false);
	});
	
	$("body").on('keyup', ".scCvvInput", function() {
		var cvv = $(this).val();
		if(!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) {
			$("#scSubmit").attr("class","blue-btn");
			$("#scSubmit").attr("disabled", false);
		} else {
			$("#scSubmit").attr("class","gry-btn");
			$("#scSubmit").attr("disabled", "disabled");
		}
		
	});
	
	$("body").on('click', '#scSubmit', function(e){
		var saveCard = $("#savedCardId")[0].value;
		var cvv = $("#cvv-" + saveCard)[0].value;
		$("#scCvvBox")[0].value = cvv;
		$('form[name=savecard-form]').submit();
	});
	
	$("body").on('click', '#cn', function(e){
		if($('#ulTest').children().length > 0) {
			$('#ulTest').slideDown('fast');
			$('.carddetails.savedCard').removeClass('savedCard');
		}
		
	});
	
	$("body").on('click', '#pc', function(e){
		processWallet();
	});
	
	
	
	$("#dcExpMonth").on('change', function() {
		var text = $('#dcExpMonth').val();
		if(text == "0") {
			isDCExpMonthValid = false;
		} else {
			isDCExpMonthValid = true;
		}
		checkDCValidity();
	});

	$("#dcExpYear").on('change', function() {
		var text = $('#dcExpYear').val();
		if(text == "0") {
			isDCExpYearValid = false;
		} else {
			isDCExpYearValid = true;
		}
		checkDCValidity();
	});
	
	$("#ccExpMonth").on('change', function() {
		var text = $('#ccExpMonth').val();
		if(text == "0") {
			isCCExpMonthValid = false;
		} else {
			isCCExpMonthValid = true;
		}
		checkCCValidity();
	});

	$("#ccExpYear").on('change', function() {
		var text = $('#ccExpYear').val();
		if(text == "0") {
			isCCExpYearValid = false;
		} else {
			isCCExpYearValid = true;
		}
		checkCCValidity();
	});
	
	$(document).on('click', function(e) {
		var $clicked = $(e.target);
		if (! $clicked.parents().hasClass("dropdown"))
			$(".dropdown dd ul").hide();
		
		if (!$clicked.hasClass("cardInput"))
			$('.cardList').slideUp('fast');
	});
	
	$(".dropdown img.flag").addClass("flagvisibility");
	
	$(".dropdown dt a").click(function() {
		$(".dropdown dd ul").hide();
		$(this).parents(".dropdown").find("dd ul").toggle();
	});
	
	$(".dropdown dd ul li a").click(function() {
		var text = $(this).html();
		$(this).parents(".dropdown").find("dt a span").html(text);
		$(this).parents(".dropdown").find("dd ul").hide();
	});
	
	$("#flagSwitcher").click(function() {
		$(".dropdown img.flag").toggleClass("flagvisibility");
	});
	
	
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
	
	// only numeric cvv
	// only numeric cvv
	$("input[name=cvvNumber], .scCvvInput").on("keypress", isNumber);
	
	function isNumber(e){
		var charCode = (e.which) ? e.which : e.keyCode;
	    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
	    	e.preventDefault();
	        return false;
	    }
	    return true;
	};
	/*$("input[name=cvvNumber], .scCvvInput").on("keypress", function(e){
		digit = String.fromCharCode(e.which);
	    if (!/^\d+$/.test(digit)) {
	    	e.preventDefault();
	      return;
	    }
	});*/

	// update card num in hidden field
	$("#cn, #cn1").on("keyup", function(e){
		var $target = $(e.target);
		var value = $target.val();
		$target.siblings('input[type=hidden]').val(value.replace(/ /g, ""));
	});

	
	$("body").on('keyup', '#cn1', function(e) {
		var cardNumber = $.trim($(this).val());
		cardNumber = cardNumber.replace(/ /g, "");
		if(!isNaN(cardNumber) && cardNumber.length >=12 && cardNumber.length <=19) {
			$('#clueTipBox').mouseenter();
			isDCNumberValid = true;
		} else {
			$('#clueTipBox').mouseleave();
			isDCNumberValid = false;
		}
		checkDCValidity();
		$('#dcStoreCardWrapper').show();
		$('#maestroOpt').hide();
		$.proxy(changeCardIconDC, this)(cardNumber);
	});
	
	
	function changeCardIconDC (cardNumber){
		var cardType = getCardType(cardNumber);
		
		if (cardType == "maestro") {
			$('#dcStoreCardWrapper').hide();
			$('#clueTipBox').mouseleave();
			$('#maestroOpt').show();
			
		}

		
		if (cardType == "amex" || cardType == "diners")
			cardType = "INVALID CARD";
		
		var icon = cardType == "INVALID CARD" ? "d" : cardType;
		$(this).removeClass('d maestro master visa diners rupay');
		$(this).addClass(icon);
	}

	$("body").on('keyup', "#dcCvvBox", function() {
		var cvv = $(this).val();
		if(!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) {
			isDCCvvValid = true;
		} else {
			isDCCvvValid = false;
		}
		
		// check other fields also - fixes auto fill
		$("#dcExpMonth, #dcExpYear").trigger("change");
		$("#cn1").trigger("keyup");
		
		checkDCValidity();
	});

	function checkDCValidity() {
		if(isDCNumberValid) {
			var num = $('#cn1').val().replace(/\s+/g,'');
			var cardType = getCardType(num);
			if(cardType == "maestro" && num.length >= 16) {
				$("#dcSubmit").attr("class","blue-btn");
				$("#dcSubmit").attr("disabled", false);
			} else {
				if(isDCExpMonthValid && isDCExpYearValid && isDCCvvValid) {
					$("#dcSubmit").attr("class","blue-btn");
					$("#dcSubmit").attr("disabled", false);
				} else {
					$("#dcSubmit").attr("class","gry-btn");
					$("#dcSubmit").attr("disabled", true);
				}
			}
		} else {
			$("#dcSubmit").attr("class","gry-btn");
			$("#dcSubmit").attr("disabled", true);
		}
	}

	$("body").on('keyup', '#rewardsCardNo', function(e) {
		var cardNumber = $.trim($(this).val());
		$.proxy(changeCardIconDC, this)(cardNumber);
	});
	
	$("body").on('keyup', '#cn', function(e) {
		var cardNumber = $.trim($(this).val());
		cardNumber = cardNumber.replace(/ /g, "");
		
		if(!isNaN(cardNumber) && cardNumber.length >=12 && cardNumber.length <=19) {
			showCCCvvImage();
			isCCNumberValid = true;
		} else {
			$('#clueTipBoxCC').mouseleave();
			$('#clueTipBoxAmex').mouseleave();
			isCCNumberValid = false;
		}
		checkCCValidity();
		$('#ccStoreCardWrapper').show();
		
		$.proxy(changeCardIconCC, this)(cardNumber);
	});
	
	function changeCardIconCC(cardNumber){
		var cardType = getCardType(cardNumber);
		if (cardType == "amex") {
			
			$("#ccStoreCardWrapper").hide();
		}
		
		var icon = cardType == "INVALID CARD" ? "c" : cardType;
		$(this).removeClass('c amex master visa diners rupay');
		$(this).addClass(icon);
	}
	
	$("body").on('keyup', "#ccCvvBox", function() {
		var cvv = $(this).val();
		if(!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) {
			isCCCvvValid = true;
		} else {
			isCCCvvValid = false;
		}
		
		// check other fields also - fixes auto fill
		$("#ccExpMonth, #ccExpYear").trigger("change");
		$('#cn').trigger("keyup");
		
		checkCCValidity();
	});

	function checkCCValidity() {
		if(isCCNumberValid) {
			var num = $('#cn').val().replace(/\s+/g,'');
			var cardType = getCardType(num);
			if(cardType == "maestro" && num.length >= 16) {
				$("#ccSubmit").attr("class","blue-btn");
				$("#ccSubmit").attr("disabled", false);
			} else {
				if(isCCExpMonthValid && isCCExpYearValid && isCCCvvValid) {
					$("#ccSubmit").attr("class","blue-btn");
					$("#ccSubmit").attr("disabled", false);
				} else {
					$("#ccSubmit").attr("class","gry-btn");
					$("#ccSubmit").attr("disabled", true);
				}
			}
		} else {
			$("#ccSubmit").attr("class","gry-btn");
			$("#ccSubmit").attr("disabled", true);
		}
	}

	$("#paymodes").on('click', "#ccSaveCardLabel", function() {
		if($("#ccSaveCardLabel a").hasClass("checked")) {
			$("#ccSaveCardLabel input").attr("checked", "checked");
		} else {
			$("#ccSaveCardLabel input").removeAttr("checked");
		}
		
	});
	$("#paymodes").on('click', "#dcSaveCardLabel", function() {
		if($("#dcSaveCardLabel a").hasClass("checked")) {
			$("#dcSaveCardLabel input").attr("checked", "checked");
		} else {
			$("#dcSaveCardLabel input").removeAttr("checked");
		}
		
	});

	$("body").on('keyup', "#mobileNo", function() {
		var mobileNo = $(this).val();
		if(!isNaN(mobileNo) && mobileNo.length == 10) {
			isMobileNoValid = true;
		} else {
			isMobileNoValid = false;
		}
		checkIMPSValidity();
		
	});
	
	$("body").on('keyup', "#mmid", function() {
		var mmid = $(this).val();
		if(!isNaN(mmid) && mmid.length == 7) {
			isMMIDValid = true;
		} else {
			isMMIDValid = false;
		}
		checkIMPSValidity();
		
	});
	
	$("body").on('keyup', "#otp", function() {
		var otp = $(this).val();
		if(!isNaN(otp) && otp.length == 6) {
			isOTPValid = true;
		} else {
			isOTPValid = false;
		}
		checkIMPSValidity();
		
	});

	
	$("body").on('keyup', "#itzCashNumber", function() {
		var mobileNo = $(this).val();
		
		if(!isNaN(mobileNo) && mobileNo.length == 12) {
			isItzCashValid = true;
		} else {
			isItzCashValid = false;
		}
		checkITzValidity();
		
	});
	
	$("body").on('keyup', "#itzPwd", function() {
		var mmid = $(this).val();
		
		if(!isNaN(mmid) && mmid.length > 3) {
			isItzPwdValid = true;
		} else {
			isItzPwdValid = false;
		}
		checkITzValidity();
		
	});
	
	function checkIMPSValidity() {
		if(isMobileNoValid && isMMIDValid && isOTPValid) {
			$("#impsSubmit").attr("class","blue-btn");
			$("#impsSubmit").attr("disabled", false);
		} else {
			$("#impsSubmit").attr("class","gry-btn");
			$("#impsSubmit").attr("disabled", true);
		}
	}
	
	function checkITzValidity() {
		if(isItzCashValid && isItzPwdValid) {
			$("#itzCashSubmit").attr("class","blue-btn");
			$("#itzCashSubmit").attr("disabled", false);
		} else {
			$("#itzCashSubmit").attr("class","gry-btn");
			$("#itzCashSubmit").attr("disabled", true);
		}
	}
	
	var isRewardsMobileValid, isRewardsCardValid, isRewardsOTPValid, isRewardsProgramValid;
	
	$("body").on('keyup', "#rewardsCardNo, #rewardsMobile", function() {
		var rewardsCardNo = $("#rewardsCardNo").val();
		var rewardsMobile = $("#rewardsMobile").val();
		
		isRewardsCardValid = !isNaN(rewardsCardNo) && rewardsCardNo.length >=12 ? true : false;
			
		isRewardsMobileValid = !isNaN(rewardsMobile) && rewardsMobile.length == 10 ? true : false;
		
		checkRewardsValidity();
	});
	
	$("body").on('keyup', "#rewardsOTP", function() {
		var otp = $(this).val();
		if(!isNaN(otp) && otp.length == 6) {
			isRewardsOTPValid = true;
		} else {
			isRewardsOTPValid = false;
		}
		checkRewardsValidity();
		
	});
	
	$("#rewardsContent .btn-change-rewards-details").click(function(){
		$(".rewards-otp-form").hide();
		$(".rewards-card-form").show();
		$("#rewardsAction").val("CARD_INPUT");
		return false;
	});
	
	
	$("body").on('change', "#rewardsProgram", function() {
		var val = $(this).val();
		isRewardsProgramValid = val == "-1" ? false : true;
		checkRewardsValidity();		
	});
	
	function checkRewardsValidity() {
		if((isRewardsCardValid && isRewardsMobileValid && isRewardsProgramValid)) {
			$("#rewardsSubmit").attr("class","blue-btn").attr("disabled", false);
		} else {
			$("#rewardsSubmit").attr("class","gry-btn").attr("disabled", true);
		}
		
		if(isRewardsOTPValid) {
			$("#rewardsOTPSubmit").attr("class","blue-btn").attr("disabled", false);
		} else {
			$("#rewardsOTPSubmit").attr("class","gry-btn").attr("disabled", true);
		}
	}

	$("body").on('click', '.deleteCard',
		function() {
			var savedCardId = $(this).attr('cardId');
			$.post("DeleteCardDetails", {
		     	"savedCardId": savedCardId
		    },
		    function(data,status) {
		    	if ('success' == status) {
		    		$("#delete-" + savedCardId).parents(".control-group").remove();
		    		if (0 == $(".control-group").size()) {
		    			$(".tab-pane")[1].className = "tab-pane fade in active";
		    			$("#verTabs").children()[1].className = "active";
		    			$("#scContent").remove();
		    			$("#savedcard").remove();
		    		} else {
		    			var saveCard = $(".cvvRadio")[0].id;
		    			$("#" + saveCard).children().attr("checked", "checked");
		    			$("#savedCardId")[0].value = saveCard;
		    			$("#cvvDiv-" + saveCard).show();
		    		
		    			$("#" + saveCard).parent().addClass("savedCardSelect");
		    			$("#prependedradio-" + saveCard).addClass("savedCardSelect1");
		    		
		    			$("#scSubmit").attr("class","blue-btn");
		    			$("#scSubmit").attr("disabled", false);
		    		}
		    	}
		    }
		);
			return false;
		}
	);

	$("body").on('click', '.netbanking-panel li div', function() {
			$('.netbanking-panel li div a.checked').removeClass('checked');
			$(this).find('a').addClass("checked");
			var bankName = $(this).attr('id');
			var displayName = $(this).attr('title');
			$('#nbWrapper .selectpicker').selectpicker('val', bankName);
			$('#bankCode').val(bankName);
			$('#warningDiv').hide();
			$('#errorMsg').text("");
			$('#nbcardId').hide();
			$("#nbSubmit").removeClass("gry-btn").addClass('blue-btn');
			$("#nbSubmit").removeAttr("disabled");
			
			if(maintainenceNBBank[bankName]) {
				$('#errorMsg').text(displayName + " is not available due to maintenance activity. If possible, pay using a different payment mode or try after sometime.");
				$('#warningDiv').show();
				$('#nbSubmit').attr("disabled", "disabled");
				$('#nbSubmit').removeClass('blue-btn').addClass('gry-btn');
			} else if(lowPerfNBBank[bankName]) {
				$('#warningDiv').show();
				$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. We recommend you to pay using a different mode.");
			}
		}
	);

	$("body").on('click', '#nbWrapper ul li', function() {
		var bankName = $("#nbWrapper .selectpicker").selectpicker('val');
		var displayName = $("#nbWrapper ul li.selected span").text();
		$(".netbanking-panel li div a.checked").removeClass("checked");
		$(".netbanking-panel li div").each(function(){
			if($(this).attr('id')==bankName){
				
				$(this).find('a').addClass('checked');
			}
		});
		
		$('#bankCode').val(bankName);
		$('#warningDiv').hide();
		$('#errorMsg').text("");
		$('#nbcardId').hide();
		if(bankName == -1) {
			$('#nbSubmit').attr("disabled", "disabled");
			$('#nbSubmit').removeClass('blue-btn').addClass('gry-btn');
			
		} else {
			$('#nbSubmit').removeClass('gry-btn').addClass('blue-btn');
			$("#nbSubmit").removeAttr("disabled");
		}
		
		if(maintainenceNBBank[bankName]) {
			$('#errorMsg').text(displayName + " is not available due to maintenance activity. If possible, pay using a different payment mode or try after sometime.");
			$('#nbSubmit').attr("disabled", "disabled");
			$('#warningDiv').show();
			$('#nbSubmit').removeClass('blue-btn').addClass('gry-btn');
			$("#nbSubmit").attr("disabled", "disabled");
		} else if(lowPerfNBBank[bankName]) {
			$('#warningDiv').show();
			$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. We recommend you to pay using a different mode.");
		}
	});
	$('#nbWrapper ul li').click();

	$("body").on('click', '.atm-panel li div', function() {
		$('.atm-panel li div a.checked').removeClass('checked');
		$(this).find('a').addClass("checked");
		var bankName = $(this).attr('id');
		var displayName = $(this).attr('title');
		$('#atmBankCode').val(bankName);
		$('#atmWarningDiv').hide();
		$('#atmErrorMsg').text("");
		$('#atmcardId').hide();
		$('#atmSubmit').removeAttr("disabled");
		$('#atmSubmit').removeClass('gry-btn').addClass('blue-btn');
		if(maintainenceATMBank[bankName]) {
			$('#atmErrorMsg').text(displayName + " is not available due to maintenance activity. If possible, pay using a different payment mode or try after sometime.");
			$('#atmWarningDiv').show();
			$('#atmSubmit').attr("disabled", "disabled");
			$('#atmSubmit').removeClass('blue-btn').addClass('gry-btn');
		} else if(lowPerfATMBank[bankName]) {
			$('#atmWarningDiv').show();
			$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. We recommend you to pay using a different mode.");
		}
	}
	);
	$('.atm-panel input.bankRadio[checked]').click();

	$("body").on('click', '#paymodes .nav, #paymodes .tab-content', function() {
		if($('#paymodes').data('disabled')) {
			$('#pc a').removeClass('checked');
			processWallet();
		}
	});

	$("body").on('click', '#login-btn', function() {
		$('#login-modal').addClass('md-show');
		$('.md-overlay').addClass('md-overlay-show');
		$('#login-iframe').attr('src', 'http://125.63.68.119/oauth2/authorize?response_type=code&client_id=testclient&scope=paytm&redirect_uri=http://localhost:8080/oltp-web/oauthResponse');
	});

	$("body").on('click', '#register-btn', function() {
		$('#login-modal').addClass('md-show');
		$('.md-overlay').addClass('md-overlay-show');
		$('#login-iframe').attr('src', 'http://125.63.68.119/register?response_type=code&client_id=testclient&scope=paytm&redirect_uri=http://localhost:8080/oltp-web/oauthResponse');
	});

	$("body").on('click', '.closePop', function() {
		$('#login-modal').removeClass('md-show');
		$('.md-overlay').removeClass('md-overlay-show');
		$('#login-iframe').attr('src', '');
	});

	function getCardType(cardNumber) {
		var first = cardNumber.substring(0, 1);
		var firstTwo = cardNumber.substring(0, 2);
		var firstFour = cardNumber.substring(0, 4);
		var firstSix = cardNumber.substring(0, 6);
		var res;
		
		if(cardNumber.length == 19) {
			res = "maestro";
		} else if (firstTwo == "30" || firstTwo == "36" || firstTwo == "38" || firstTwo == "35") {
			res =  "diners";
		} else if (firstTwo == "34" || firstTwo == "37") {
			res =  "amex";
		} else if (first == "4") {
			res =  "visa";
		} else if (checkRange(firstTwo, 51, 55)) {
			res =  "master";
		} else if ( firstFour == "6011") {
			res =  "DISCOVER";
		} else if (checkRange(firstSix, 508500, 508999) || checkRange(firstSix, 606985, 607984) || checkRange(firstSix, 608001, 608500) || checkRange(firstSix, 652150, 653149) ) {
			res =  "rupay";
		} else if (firstTwo == "50" || checkRange(firstTwo, 58, 62) ) {
			res = "maestro";
		} else if (cardNumber.length == 0){
			res = "INVALID CARD";
		} else {
			res = "none";
		}
		
		return res;
	}
	
	function checkRange(num, a, b){
		return (num >= a && num <= b) ? true : false;
	}
	
	function showCCCvvImage() {
		
		var cardNumber = $('#cn').val();
		cardNumber = cardNumber.replace(/ /g, "");
		var cardType = getCardType(cardNumber);
		if (cardType == "amex") {
			$('#clueTipBoxAmex').mouseenter();
			$('#cluetip').removeClass("ui-cluetip");
			$('#cluetip').addClass("ui-cluetip1");
		} else {
			$('#clueTipBoxCC').mouseenter();
			$('#cluetip').removeClass("ui-cluetip1");
			$('#cluetip').addClass("ui-cluetip");
		}
	}
	
	function processWallet() {
		
		var totalAmount = $('#totalAmtVal').val() * 1 ;
		var walletBalance = $('#totalWalletVal').val() * 1;
		var paidAmount = totalAmount;
		var remWalletBalance = 0;
		if($("#pc a").hasClass("checked")) {
			if(walletBalance >= totalAmount) {
				$('#paymodes').css('opacity', .4).data('disabled', true);
				paidAmount = 0;
				remWalletBalance = walletBalance - totalAmount;
				$('input[name=walletAmount]').val(totalAmount);
				$('.fullWalletDeduct, #walletForm').show();
				$('.otherText').hide();
			} else {
				paidAmount = totalAmount - walletBalance;
				$('input[name=walletAmount]').val(walletBalance);
				$('.fullWalletDeduct, #walletForm').hide();
				$('.otherText').show();
			}
			$('#walletBalance').show();
			$("#Notbalance").hide();
			$("#balanceAval").show();
			$('#walletBalanceSpan').text(walletBalance - remWalletBalance);
			
			/*$('#yourBal').hide();
			$('#remBal .amt').text(remWalletBalance);
			
			$('#remBal, #walletBalance').show();*/
			
		} else {
			$('.scCvvInput').focus();
			$('#paymodes').css('opacity', 1).data('disabled', false);
			paidAmount = totalAmount;
			$('#walletBalance').hide();
			$("#Notbalance").show();
			$("#balanceAval").hide();
			$('.fullWalletDeduct').hide();
			/*$('#yourBal .amt').text(walletBalance);
			
			$('.fullWalletDeduct, #walletBalance, #remBal').hide();
			$('.otherText, #yourBal').show();*/
			$('input[name=walletAmount]').val('0');
			$('.otherText').show();
		}
		$('.finalAmountSpan').html(paidAmount);
		
	}
	
	// cancel button
	$("a.cancel").click(function(e, data){
		var that = this;
		if(!data) {
			$.get("/oltp-web/cancelTransaction", function(html, status){
				if(status == "success"){
					var regex = /<FORM[\s\S]*<\/FORM>/;
					var formStr = regex.exec(html);
					if(!formStr)
						return $(that).trigger("click", {fake:true});
					$("body").append(formStr[0]);
					var form = $("form[name=TESTFORM]")[0];
					if(window.top !== window){
						form.target="_parent";
					}
					form.submit();
				}
			});
		
			e.preventDefault();
		}
	});
	// track page load
	 /*try {
	 	trackEvent("onload");
	 } catch(e){};*/
	 
	 function trackEvent(e){
		 var txnTransientId = $("#txnTransientId").data("value");
		 
		 if(e == "onload"){
			 $.post("/oltp/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
				var status = res;
			 });
		 }
	 }

	 
	 // dont show paytm cash for COD
	 var id = $("#verTabs .active a").attr("href");
	 if(id == "#codContent"){
		hidePaytmCash();
	 }
	 
	 
	 $('#verTabs').on("shown", function(e){
		var id = $(e.target).attr("href");
		// focus first input field of selected tab
		$(id).find("input[type=text]").eq(0).focus();
		
		// dont show paytm cash for COD 
		if(id == "#codContent"){
			hidePaytmCash();
			
		} else {
			$("#paytmcashbox").show();
		}
	 });
	 
	 function hidePaytmCash(){
		// uncheck paytm cash
		$("#paytmcashbox").hide();
		if($('#pc input.pcb')[0].checked){
			$('#pc .checked').click();
		}
	 }
	
	
	
}); // $ ready ends