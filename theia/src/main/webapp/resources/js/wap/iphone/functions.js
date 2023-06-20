var contextRoot = '/oltp-web/';

// Disable context menu
$(document).ready(function() {
    $(document).bind("contextmenu",function(e){
        //e.preventDefault();
    });
    
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

// Disable copy paste
$(document).ready(function(){
    $('input').bind("cut copy paste",function(e) {
        e.preventDefault();
    });
  });

$(document).ready(function(){
	var objList = $('#tab-wrapper div.cell');
	var size = objList.size();
	var startLeft = -5;
	var position = 0;
	for(var i=0; i < size; i++) {
		var obj  = objList.get(i);
		obj.setAttribute("position", i);
		
		var childrenId = obj.children[0].id;
		var containerObj = $("#" + childrenId + "-container").get(0);
		if(containerObj.style.display != "none") {
			position = i;
			obj.style.left = "-5px";
		} else {
			startLeft = parseInt(startLeft) + parseInt(125);
			obj.style.left = startLeft + "px";
		}
		
	}
  })
  
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
	$('#pmodeSelect').live('change', function() {
		$("div[id$='-container']").hide();
		$('#' + $(this).val() + '-container').show();
		$(".errors").hide();
	});
	
/*	$('#atmBankSelect').live('click', function() {
			$("div[id$='-container']").hide();
			$("#tab-wrapper").hide();
			$("#headerDiv").hide();
			$('#atmBankList').show();
		}
	);
*/	
/*	$('#dcListBack').live('click', function() {
			$('#atmBankList').hide();
			$("#headerDiv").show();
			$("#tab-wrapper").show();
			$("#debitcard-container").show();
		}
	);
*/	
/*	$('#dcListSection ul li a').live('click', function() {
			var bankName = $(this).attr('id');
			var displayName = $(this).text();
			
			$("#atmbankCode").val(bankName);
			$("#bankNameSpan").text(displayName);
			
			$('#dcWarningDiv').hide();
			$('#dcErrorMsg').text("");
			$('#atmBankList').hide();
			$("#headerDiv").show();
			$("#tab-wrapper").show();
			$("#debitcard-container").show();
			$(".errors").hide();
			if (bankName.indexOf('other')>=0){
				$('#debitcard-form').show();
			} else {
				$('#debitcard-form').hide();
			}
			
			$('#dcSubmit input').removeAttr("disabled");
			$('#dcSubmit div').removeClass('disableSubmit');
			$('#dcSubmit div').addClass('ui-btn');
			
			if(maintainenceATMBank[bankName]) {
				$('#dcErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
				$('#dcWarningDiv').show();
				
				$('#dcSubmit input').attr("disabled", "disabled");
				$('#dcSubmit div').removeClass('ui-btn');
				$('#dcSubmit div').addClass('disableSubmit');
				
			} else if(lowPerfATMBank[bankName]) {
				$('#dcWarningDiv').show();
				$('#dcErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
			}
		}
	);
*/	
	$('#atmBankSelect').live('change', function() {
		
		var bankName = $(this).val();
		var displayName = $(this).children("option:selected").text();
		$("#atmbankCode").val(bankName);
		$('#dcWarningDiv').hide();
		$('#dcErrorMsg').text("");
		$(".errors").hide();
		if (bankName.indexOf('other')>=0){
			$('#debitcard-form').show();
		} else {
			$('#debitcard-form').hide();
		}
		
		$('#dcSubmit input').removeAttr("disabled");
		$('#dcSubmit div').removeClass('disableSubmit');
		$('#dcSubmit div').addClass('ui-btn');
		
		if(maintainenceATMBank[bankName]) {
			$('#dcErrorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#dcWarningDiv').show();
			
			$('#dcSubmit input').attr("disabled", "disabled");
			$('#dcSubmit div').removeClass('ui-btn');
			$('#dcSubmit div').addClass('disableSubmit');
			
		} else if(lowPerfATMBank[bankName]) {
			$('#dcWarningDiv').show();
			$('#dcErrorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
		
	});
	/*$('#nbBankSelect').live('click', function() {
		$("div[id$='-container']").hide();
		$("#tab-wrapper").hide();
		$("#headerDiv").hide();
		$('#nbBankList').show();
	}
	);

	$('#nbListBack').live('click', function() {
			$('#nbBankList').hide();
			$("#headerDiv").show();
			$("#tab-wrapper").show();
			$("#netbanking-container").show();
		}
	);

	$('#nbListSection ul li a').live('click', function() {
		var bankName = $(this).attr('id');
		var displayName = $(this).text();
		
		$("#nbBankCode").val(bankName);
		$("#bankNameSpanNb").text(displayName);
		
		$('#warningDiv').hide();
		$('#errorMsg').text("");
		$('#nbBankList').hide();
		$("#headerDiv").show();
		$("#tab-wrapper").show();
		$("#netbanking-container").show();
		$(".errors").hide();
		
		$('#nbSubmit input').removeAttr("disabled");
		$('#nbSubmit div').removeClass('disableSubmit');
		$('#nbSubmit div').addClass('ui-btn');
		
		if(maintainenceNBBank[bankName]) {
			$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#warningDiv').show();
			
			$('#nbSubmit input').attr("disabled", "disabled");
			$('#nbSubmit div').removeClass('ui-btn');
			$('#nbSubmit div').addClass('disableSubmit');
			
		} else if(lowPerfNBBank[bankName]) {
			$('#warningDiv').show();
			$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});
	*/
	$('#nbBankSelect').live('change', function() {
		
		var bankName = $(this).val();
		var displayName = $(this).children("option:selected").text();
		$("#nbBankCode").val(bankName);
		$('#warningDiv').hide();
		$('#errorMsg').text("");
		$(".errors").hide();
		$('#nbSubmit input').removeAttr("disabled");
		$('#nbSubmit div').removeClass('disableSubmit');
		$('#nbSubmit div').addClass('ui-btn');
		
		if(maintainenceNBBank[bankName]) {
			$('#errorMsg').text(displayName + " is not available due to some maintainence activity. If available, pay using a different payment mode or try after sometime.");
			$('#warningDiv').show();
			
			$('#nbSubmit input').attr("disabled", "disabled");
			$('#nbSubmit div').removeClass('ui-btn');
			$('#nbSubmit div').addClass('disableSubmit');
			
		} else if(lowPerfNBBank[bankName]) {
			$('#warningDiv').show();
			$('#errorMsg').text("Experiencing high failures on "+ displayName + " in last few transactions. Recommend you pay using a different mode.");
		}
	});

	$('#deleteCardClose').live('click', function() {
		$("#overlay").popup("close");
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
	
	$('#deleteDiv a').live('click', function() {
			var cardId = $(this).attr("id");
			$("#deletCardId").val(cardId);
			$("#overlay").popup("open");
		}
	);
});
