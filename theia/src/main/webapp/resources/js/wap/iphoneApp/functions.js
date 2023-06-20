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
	
	$('.saved-wapcards-list li a').live('click',
			function() {
				

				return false;
			}
		);

	// CC card type select
	$('.unchk_btn span').live('click',
		function() {
			$('.unchk_btn span').removeClass('chek');
			$(this).addClass('chek');
			
			if ($(this).hasClass('cc-amex')) {
				$(".cc-sec").hide();
				$(".cvv-cc").hide();
				$(".amex-sec").show();
				$(".maestro").hide();
				$("#creditcard-container input[name='cardNumber']").attr('maxlength', 19);
				$("#creditcard-container .save-card-option").hide();
				$("#cardType")[0].value="AMEX";
			} 
			else {
				$(".amex-sec").hide();
				$(".cc-sec").show();
				$(".cvv-cc").show();
				$(".maestro").show();
				$("#creditcard-container input[name='cardNumber']").attr('maxlength', 16);
				$("#creditcard-container .save-card-option").show();
				$("#cardType")[0].value="";
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
		
		var bankName = $(this).val();
		var displayName = $('#ATMBankCodeSelect option:selected').text();
		
		$('#atmErrorMsg').text("");
		$('#dcSubmit').get(0).disabled = "";
		$('#dcSubmit').removeClass('disableSubmit');
		$('#dcSubmit').addClass('button');
		if(maintainenceATMBank[bankName]) {
			$('#atmErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#dcSubmit').attr("disabled", "disabled");
			$('#dcSubmit').removeClass('button');
			$('#dcSubmit').addClass('disableSubmit');
		} else if(lowPerfATMBank[bankName]) {
			$('#atmErrorMsg').text("Last few transactions on " + displayName + ", we have seen over 30% failures. We recommend you pay using a different mode.");
		}
		 $(".error").hide();
		if ($(this).val().indexOf('other')<0 || $(this).val()==-1) {
		    $("div[id$='debitcard-div']").hide();
		} else {
			 $("div[id$='debitcard-div']").show();
		}
	});
	
	$('#nbBankCodeId').live('change', function() {
		var bankName = $(this).val();
		var displayName = $('#nbBankCodeId option:selected').text();
		
		$('#nbErrorMsg').text("");
		$('#nbSubmit').get(0).disabled = "";
		$('#nbSubmit').removeClass('disableSubmit');
		$('#nbSubmit').addClass('button');
		if(maintainenceNBBank[bankName]) {
			$('#nbErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#nbSubmit').attr("disabled", "disabled");
			$('#nbSubmit').removeClass('button');
			$('#nbSubmit').addClass('disableSubmit');
		} else if(lowPerfNBBank[bankName]) {
			$('#nbErrorMsg').text("Last few transactions on " + displayName + ", we have seen over 30% failures. We recommend you pay using a different mode.");
		}
	});

	// Net Banking panel
	$('.netbanking-panel li').live('click', 
		function() {
			$('.netbanking-panel li').removeClass('chek');
			$(this).addClass('chek');

			$('#' + this.id + '-option').attr('selected', 'selected');
		}
	);

	$('#netbanking-container select[name="bankCode"]').live('change', 
		function(){
			$('.netbanking-panel li').removeClass('chek');
			$('#' + $(this).val()).addClass('chek');
		}
	);

	$('#other-container .unchk_btn span')
		.live('click', 
			function() {
				$('.unchk_btn span').removeClass('chek');
				$(this).addClass('chek');
	
				$('#bankCashId').value = this.id;
			}
		);

});
