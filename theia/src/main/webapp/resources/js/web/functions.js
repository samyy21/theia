var contextRoot = '/oltp-web/';

// Disable context menu
$(document).ready(function() {
    $(document).bind("contextmenu",function(e){
        //e.preventDefault();
    });
});

// Disable copy paste
$(document).ready(function(){
    $('input').bind("cut copy paste",function(e) {
        e.preventDefault();
    });
  });

$(document).ready(function() {

	// tooltip click cancellation
	$('a.tooltip').live('click', function(){return false;});

	// Payment Cancellation
	$('form input[value="Cancel"]').live('click', function() {
		$(this).closest('form').attr('action', "cancelTransaction");
	});

	// Failure case
	$('.failure input[value="Yes"]').live('click', function() {
		$("#payment-container").slideToggle	();
	});

	// Tab selection
	$('#tab-wrapper ul.mytabs li').live('click', function() {
			$('#tab-wrapper ul.mytabs li span').removeClass();
			$("div[id$='-container']").hide();
			$(this).find('span').addClass('act');
			$('#'+this.id+'-container').show();
		}
	);

	// Toggle for billing details
	$('#displayText').live('click', 
		function() {
			var collapse_content_selector = $('#cart-details');

			var toggle_switch = $(this);
			$(collapse_content_selector).toggle(function() {
				if($(this).css('display')=='none'){
					toggle_switch.html('show details +');
				}
				else {
					toggle_switch.html('hide details -');
				}
			});

			return false;
		}
	);

	$('.saved-cards-list li').live('click',
		function() {
			$('.saved-cards-list li').removeClass('chek');
			$(this).addClass('chek');
			$(this).find('input[name="savedCardId"]').attr('checked', 'checked');
			
			$('#savedcard-container .cc-sec').hide();
			$('#savedcard-container .amex-sec').hide();

			var cvvPanel = '#savedcard-container .cc-sec';
			if ($(this).hasClass('amex')) {
				cvvPanel = '#savedcard-container .amex-sec';
			}
			
			$(cvvPanel).show();
		}
	);

	$('.save-card li a').live('click',
		function() {
			var savedCardId = $(this).attr('id');
			$.post(contextRoot + "DeleteCardDetails",
			    {
			     	"savedCardId": savedCardId
			    },
			    function(data,status) {
			      if ('success' == status) {
			    	  $('#' + savedCardId + '-item').hide('slow',
			    	    function() {
			    	      $('#' + savedCardId + '-item').remove();
						  if (0 == $('ul.save-card li').size()) {
							$('.save-card').hide('slow', function() {$('.save-card').remove();});
							$('ul.mytabs li#savedcard').hide('slow', function(){
									$('ul.mytabs li#savedcard').remove();
									$('ul.mytabs li:first-child').click();
									$('#savedcard-container').remove();
									$().delay(300);
									
								});
						  } else {
							  $('ul.save-card span')[0].click();
						  }
			    	  	}
			    	  );
			      }
			    }
			);

			return false;
		}
	);

	$('#cardNumber').live('keyup',
		function() {
			var cardNumber = $(this).val();
			var cardType = getCardType(cardNumber);
			var amexEnabled = false;
			if($("#amexImg").length > 0) {
				amexEnabled = true
			}
			if (cardType == "amex" && amexEnabled) {
				$(".cc-sec").hide();
				$(".amex-sec").show();
				$(".maestro").hide();
				$("#creditcard-container .save-card-option").hide();
				$("#cardType").val("AMEX");
			} 
			else {
				$(".amex-sec").hide();
				$(".cc-sec").show();
				$("#creditcard-container .save-card-option").show();
				$("#cardType")[0].value="";
			}
			
			if (cardType == "amex" && amexEnabled) {
				$("#masterImg").attr("src", "images/web/masterGrey.png");
				$("#visaImg").attr("src", "images/web/visaGrey.png");
				$("#amexImg").attr("src", "images/web/amex.png");
			} else if (cardType == "master") {
				$("#masterImg").attr("src", "images/web/master.png");
				$("#visaImg").attr("src", "images/web/visaGrey.png");
				$("#amexImg").attr("src", "images/web/amexGrey.png");
			} else if (cardType == "visa") {
				$("#masterImg").attr("src", "images/web/masterGrey.png");
				$("#visaImg").attr("src", "images/web/visa.png");
				$("#amexImg").attr("src", "images/web/amexGrey.png");
			} else {
				$("#masterImg").attr("src", "images/web/master.png");
				$("#visaImg").attr("src", "images/web/visa.png");
				$("#amexImg").attr("src", "images/web/amex.png");
			}
		}
	);

	$('#cardNumberDc').live('keyup',
		function() {
			var cardNumber = $(this).val();
			var cardType = getCardType(cardNumber);
			
			if (cardType == "maestro") {
				$("#dcImg #masterImg").attr("src", "images/web/masterGrey.png");
				$("#dcImg #maestroImg").attr("src", "images/web/maestro.png");
				$("#dcImg #visaImg").attr("src", "images/web/visaGrey.png");
			} else if (cardType == "master") {
				$("#dcImg #masterImg").attr("src", "images/web/master.png");
				$("#dcImg #maestroImg").attr("src", "images/web/maestroGrey.png");
				$("#dcImg #visaImg").attr("src", "images/web/visaGrey.png");
			} else if (cardType == "visa") {
				$("#dcImg #masterImg").attr("src", "images/web/masterGrey.png");
				$("#dcImg #maestroImg").attr("src", "images/web/maestroGrey.png");
				$("#dcImg #visaImg").attr("src", "images/web/visa.png");
			} else {
				$("#dcImg #masterImg").attr("src", "images/web/master.png");
				$("#dcImg #maestroImg").attr("src", "images/web/maestro.png");
				$("#dcImg #visaImg").attr("src", "images/web/visa.png");
			}
		}
	);
	
	// Save card
	$('.save-card span').live('click',
		function() {
			$('.save-card span').removeClass('chek');
			$(this).addClass('chek');
			var saveCard = $(this).attr('id');
			$("#savedCardId")[0].value=saveCard;
			$(".cc-sec").show();
			$(".cvv-cc").show();
		}
	);

	// Save card
	$('.save-card-option span').live('click', function() {
			var saveCard = $(this).parent().find('input');
			if ($(this).hasClass('chek')) {
				$(this).removeClass('chek');
				$(saveCard).val('N');
			}
			else {
				$(this).addClass('chek');
				$(saveCard).val('Y');
			}
		}
	);
	
	// Debit card - ATM WAP
	$('#debitcardwap-container select[name="bankCode"]').live('change', function() {
		if ($(this).val().indexOf('other')<0 || $(this).val()==-1) {
		    $("div[id$='debitcard-div']").hide();
		} else {
			 $("div[id$='debitcard-div']").show();
		}
	});
	// Net Banking panel
	$('.netbanking-panel li').live('click', 
		function() {
			var bankName = $(this).attr('id');
			var displayName = $(this).attr('title');
			$('.netbanking-panel li').removeClass('chek');
			$(this).addClass('chek');
			$("#bankCodeSelect option[value='-1']").attr('selected', 'selected');
			$('#bankCode').val(bankName);
			
			$('#warningDiv').hide();
			$('#errorMsg').text("");
			$('#nbcardId').hide();
			$('#nbSubmit').removeAttr("disabled");
			$('#nbSubmit').removeClass('disableSubmit');
			$('#nbSubmit').addClass('submit');
			if(maintainenceNBBank[bankName]) {
				$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#warningDiv').show();
				$('#nbSubmit').attr("disabled", "disabled");
				$('#nbSubmit').removeClass('submit');
				$('#nbSubmit').addClass('disableSubmit');
			} else if(lowPerfNBBank[bankName]) {
				$('#warningDiv').show();
				$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
		}
	);

	// Cash card select
	$('#other-container .unchk_btn span')
		.live('click', 
			function() {
				$('.unchk_btn span').removeClass('chek');
				$(this).addClass('chek');
	
				$('#bankCashId').value = this.id;
			}
		);
	
	$('#bankCodeSelect').live('change', 
		function() {
			
			var bankName = $(this).val();
			var displayName = $('#bankCodeSelect option:selected').text();
			$('.netbanking-panel li').removeClass('chek');
			$('#bankCode').val(bankName);
			
			$('#warningDiv').hide();
			$('#errorMsg').text("");
			$('#nbcardId').hide();
			$('#nbSubmit').removeAttr("disabled");
			$('#nbSubmit').removeClass('disableSubmit');
			$('#nbSubmit').addClass('submit');
			if(maintainenceNBBank[bankName]) {
				$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#nbSubmit').attr("disabled", "disabled");
				$('#warningDiv').show();
				$('#nbSubmit').removeClass('submit');
				$('#nbSubmit').addClass('disableSubmit');
			} else if(lowPerfNBBank[bankName]) {
				$('#warningDiv').show();
				$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
		}
	);
	
	$('.atm-panel li').live('click', 
		function() {
			var bankName = $(this).attr('id');
			var displayName = $(this).attr('title');
			$('.atm-panel li').removeClass('chek');
			$(this).addClass('chek');
			$("#ATMBankCodeSelect option[value='-1']").attr('selected', 'selected');
			$('#bankCodeAtm').val(bankName);
			
			$('#atmWarningDiv').hide();
			$('#atmErrorMsg').text("");
			$('#dccardId').hide();
			$('#dcSubmit').removeAttr("disabled");
			$('#dcSubmit').removeClass('disableSubmit');
			$('#dcSubmit').addClass('submit');
			if(maintainenceATMBank[bankName]) {
				$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#atmWarningDiv').show();
				$('#dcSubmit').attr("disabled", "disabled");
				$('#dcSubmit').removeClass('submit');
				$('#dcSubmit').addClass('disableSubmit');
			} else if(lowPerfATMBank[bankName]) {
				$('#atmWarningDiv').show();
				$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
			
			if (bankName.indexOf('other')>=0){
				$('#debitcard-form').show('slow');
				$('#dcCCnoId').hide();
				$('#dcexId').hide();
				$('#dcCvvId').hide();
			} else {
				$('#debitcard-form').hide('slow');
			}
		}
	);
	
	// Debit card - ATM
	$('#ATMBankCodeSelect').live('change', function() {
		var bankName = $(this).val();
		var displayName = $('#ATMBankCodeSelect option:selected').text();
		$('.atm-panel li').removeClass('chek');
		$('#bankCodeAtm').val(bankName);
		
		$('#atmWarningDiv').hide();
		$('#atmErrorMsg').text("");
		$('#dcSubmit').removeAttr("disabled")
		$('#dcSubmit').removeClass('disableSubmit');
		$('#dcSubmit').addClass('submit');
		if(maintainenceATMBank[bankName]) {
			$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#dcSubmit').attr("disabled", "disabled");
			$('#atmWarningDiv').show();
			$('#dcSubmit').removeClass('submit');
			$('#dcSubmit').addClass('disableSubmit');
		} else if(lowPerfATMBank[bankName]) {
			$('#atmWarningDiv').show();
			$('#atmErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
		$('#dccardId').hide();
		if (bankName.indexOf('other')>=0){
			$('#debitcard-form').show('slow');
			$('#dcCCnoId').hide();
			$('#dcexId').hide();
			$('#dcCvvId').hide();
		} else {
			$('#debitcard-form').hide('slow');
		}
	});
});

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