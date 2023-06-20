// trim function
if (typeof String.prototype.trim !== 'function') {
    String.prototype.trim = function () {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

// underscore utils
_ = {};
var ArrayProto = Array.prototype;
var nativeIndexOf = ArrayProto.indexOf;
_.indexOf = function (array, item, isSorted) {
    if (array == null) return -1;
    var i = 0, length = array.length;
    if (nativeIndexOf && array.indexOf === nativeIndexOf) return array.indexOf(item, isSorted);
    for (; i < length; i++) if (array[i] === item) return i;
    return -1;
};

//jquery plugin to add/remove disabled class 
$.fn.disabled = function (state) {

    state = state || false;

    if (state)
        $(this).addClass("disabled");
    else
        $(this).removeClass("disabled");
};

//jquery plugin to disable button
$.fn.disableButton = function (state) {
    state = state || false;
    $(this).disabled(state);
    if (state)
        $(this).attr("disabled", "disabled");
    else
        $(this).removeAttr("disabled");

    return $(this);
};

function numberWithCommas(x) {

    if (typeof(x) != "string") {
        return x && x.toFixed(2).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
}
var mode = "", RESTART_CHECK_JS = true;
$(document).ready(function () {

    var walletDetails = $("#wallet-details");
    var walletAvailable = walletDetails.data("available");
    var walletBalance = walletDetails.data("balance");
    var isWalletOnly = walletDetails.data("wallet-only");
    var promocodeAvailable = $("#promocode-details").length ? true : false;
    var walletUsed = walletDetails.data("wallet-used");
    var authConfig = $("#auth-config").data("value");
    var authNewConfig = $("#auth-js-details");
    var saveCardMandatory = $("#other-details").data("save-card-mandatory");
    var isIrctcAvailable = $("#paymentCharges").data("value");
    var isOTPLoginAvailable = $("#isOtpLoginAvailable").length ? true : false;

    var iraddMoney = $("#ir-addMoneyCharge").data("iramount"); // total amount in case of add money
    //IRDEFAULT FEE
    var DEFAULTFEE = $("#irctcDefaultFee");
    var DEFAULTFEE_bm = DEFAULTFEE.data("bm"); // base money
    var DEFAULTFEE_ct = DEFAULTFEE.data("ct"); // convenience text
    var DEFAULTFEE_tm = DEFAULTFEE.data("tm"); // txn amount total
    var DEFAULTFEE_cf = DEFAULTFEE.data("cf"); // convenience fee
    var csrf = $("#csrf").data("value");
    var isSubscriptionFullWallet = false;

    if (walletBalance)
        walletBalance = Math.round(walletBalance * 100) / 100;

    var paymentDetails = $("#payment-details");
    var codHybridAllowed = paymentDetails.data("cod-hybrid-allowed");
    var offlinePaymode = paymentDetails.data("offline-paymode");
    var txnAmount = paymentDetails.data("amount");
    var limeroad = paymentDetails.data("limeroad");
    var addMoneyDetails = $('#addMoney-details');
    var addMoneyAvailable = addMoneyDetails.data("available");
    var addMoneySelected = addMoneyDetails.data("selected");
    var useWallet = false;
    var hybridPaymentAllowed = paymentDetails.data("hybrid-allowed") || false;
    var isIdebitEnabled = true;//$("#payment-details").data("idebit-ic") || false;

    var currentPaymentType = $("#current-payment-type").data("value");
    var errorsAvailable = $("#error-details").data("available");
    var isSubscription = $("#other-details").data("subscription");
    var isSubsPPIOnly = $("#other-details").data("subs-ppi-only");
    if (isSubsPPIOnly == "Y") {
        isSubsPPIOnly = true;
    } else {
        isSubsPPIOnly = false;
    }
    var subsMode = $("#other-details").data("subs-mode");
    var saveCardAmount = $("#payment-details").data("save-card-amount");
    var isLoggedIn = $("#login-details").data("value");
    var isAutoLogin=$("#login-details").data("autologin");
    var lowSuccessRateMsg = $("#error-details").data("lowerrormsg");
    var maintenanceMsg = $("#error-details").data("maintenancemsg");
    var csrf = $("#csrf").data("value");

    // setup tabs/cards
    $("body").on('click', '.cards-control .card', function (e) {
        e.stopPropagation();
        $(this).siblings().removeClass("active");
        $(this).addClass("active");

        var tabs = $(this).parents(".cards-tabs");
        tabs.find(".cards-content .content").removeClass("active");

        var contentId = $(this).find("a").attr("href");
        var contentTab = tabs.find(contentId).addClass("active");

        var lastState = tabs.data("last-paytmcash-state");
        var cardsControl = tabs.find(".cards-control");
        cardsControl.click();

        if ($(window).width() < 600) {

            window.setTimeout(function () {
                $(window).scrollTop(tabs.position().top + tabs.find(".cards-content").position().top - 20);

            }, 100);
        } else {
            // focus first input field
            var firstInput = contentTab.find("input[type=text], input[type=tel]").eq(0).focus();
            if (firstInput.hasClass("disabled")) // for saved cards
                contentTab.find("input[type=password]").eq(0).focus();
        }

        // IRCTC SAVE CARDS
        if (isIrctcAvailable && window.mode != 'hybrid') {

            var irData = $(this).find(".irctcData");
            setIrctcCharges(irData);

            // set selected saved card's amount
            if (contentId == "#sc-card") {
                var cardType = contentTab.find(".sc-checkbox:checked").data("ctype");
                if (cardType) {
                    if (cardType == 'CREDIT_CARD') {
                        cardType = 'cc'
                    }
                    if (cardType == 'DEBIT_CARD') {
                        cardType = 'dc'
                    }
                    if (cardType == 'UPI') {
                        cardType = 'UPI'
                    }
                    var href = "#" + cardType.toLowerCase() + "-card";
                    var div = tabs.find('a[href="' + href + '"]').siblings(".irctcData");
                    setIrctcCharges(div);
                }
            }

            // set selected nb's amount
            if (contentId == "#nb-card") {
                var select = $(contentId).find(".nbSelect");
                if (select[0].selectedIndex != 0) {
                    var irData = select.find('option').eq(select[0].selectedIndex);
                    setIrctcCharges(irData);
                }
            }
        }

        // uncheck any select saved card
//		var checkbox = tabs.find(".sc-checkbox:checked");
//		if(checkbox.length){
//			checkbox[0].checked = false;
//			checkbox.checkbox("checkChecked").trigger("change", true);
//		}

        if (e && e.keyCode) {
            $('.error1').removeClass('error1');
            $('.error2').addClass('hide');
        }

        // OFFLINE PAYMENT MODE
        if (offlinePaymode) {
            if ($("a[href='#cheque-dd-neft']").parent().hasClass("active")) {
                $("#paytm-cash-checkbox").checkbox("click");
                disablePaytmCash();

            }
            else {
                if (e && e.which) {
                    if ($("a[href='#cheque-dd-neft']").parent().hasClass("active") == false && $("#paytm-cash-checkbox:checked").length == 0 && $(".paytmcash-card .blur-overlay").hasClass("show") == false) {
                        return;
                    }
                    else if ($("a[href='#cheque-dd-neft']").parent().hasClass("active") == false && $("#paytm-cash-checkbox:checked").length == 0) {
                        $("#paytm-cash-checkbox").checkbox("click");
                    }
                }
                enablePaytmCash();
                $(".paytmcash-card .fullWalletDeduct").addClass("f-hide");
            }
        }


        return false;
    });

    // show more paytm wallets
    $("#showpaytmWallets").on("click", function () {
        if ($(this).hasClass("arrow-up")) {
            $(this).removeClass("arrow-up").addClass("arrow-down");
        }
        else {
            $(this).addClass("arrow-up").removeClass("arrow-down");
        }
        $(".paymentCards").toggleClass("hide");

    });


    $("body").on('touchstart click', '#paytm-wallet', function () {
        $("#login-btn").click();
    });
    // setup checkbox
    $('.checkbox').checkbox();


    // setup nb and atm checkbox as radio btns
    $(".banks-panel .checkbox").on("change", function () {
        setTimeout(function () {
            $(".banks-panel .checkbox").checkbox("checkChecked");
        }, 100);

    });

    $(".banks-panel .bank-logo").click(function () {
        $(this).parents("li").find(".checkbox").checkbox("click");
    });

    // card border coloring
    $('.paytmcash-card .checkbox').on("change", checkBorderStatus);

    function checkBorderStatus() {
        var checked = $(this)[0].checked;
        var card = $(this).parents(".card");
        if (checked) {
            card.addClass("active");
            card.find(".btn-submit").addClass("active");
        } else {
            card.removeClass("active");
            card.find(".btn-submit").removeClass("active");
        }
    };

//	$('input').bind("cut copy paste",function(e) {
//        e.preventDefault();
//    });


    // setup promocode
    function setupPromocode() {


        if (promocodeAvailable) {
            //console.log("promo code value : "+promocodeAvailable);
            var elm = $("#promocode-details");
            promocode = {
                type: elm.data("type"),
                paymentModes: elm.data("paymentmodes").toString().split(','),
                nbList: elm.data("nblist").toString().split(','),
                cardList: elm.data("cardlist").toString().split(','),
                cardTypeList: elm.data("cardtypelist").toString(),
                promoErrorMsg: elm.data("errormsg"),
                checkPromoValidity: elm.data("checkpromovalidity"),
                mid: elm.data("mid"),
                txnMode: elm.data("txn"),
                promocode: elm.data("promocode")
            };

            // hide paytm cash
            $(".paytmcash-card").addClass("hide");
        }

        //console.log("Promo code type : "+promocode.type );

        if (promocode.type == "DISCOUNT") {
            if (_.indexOf(promocode.paymentModes, 'PPI') != -1) {
                // show paytm cash
                $(".paytmcash-card").removeClass("hide");
            }
        }

        if (promocode.type == "CASHBACK") {

            // hide all mode links and content
            $('.cards-content .content').addClass("f-hide");
            $('.cards-control .card').addClass("hide");

            // check to show/hide SC tab
            var savedCardsMatchedCount = 0;
            if (promocode.cardList.length) { // check cards
                savedCardsElms = $('#sc-card .control-group.card');
                savedCardsMatchedCount = savedCardsElms.not(".hide").length; // valid cards

//				savedCardsElms.addClass("hide");
//				// match saved card bins
//				savedCardsElms.each(function() {
//					if(promocode.cardList.indexOf($.trim($(this).find('.sc-checkbox').data('firstsixdigits'))) > -1) {
//						$(this).removeClass("hide");
//						savedCardsMatchedCount++;
//					}	
//				});
            }


            // show tabs for available modes
            var showTabs = promocode.paymentModes;

            // check to show saved cards for not
            if (showTabs.indexOf('CC') != -1 || showTabs.indexOf('DC') != -1) {
                if (savedCardsMatchedCount > 0) {
                    showTabs.push("SC");
                } else { // activate first tab if no sc is applicable
                }
            }


            // hide nb banks that are not available
            if (promocode.nbList.length) {

                // hide popular nb banks if not available
                var popularBanksAvailableCount = 0;
                $('.netbanking-panel li').each(function () {
                    var bankId = $(this).find('input.checkbox').data('bankid');
                    if (bankId)
                        bankId = bankId.toString();
                    if (promocode.nbList.indexOf(bankId) == -1) {
                        $(this).addClass("hide");
                    } else {
                        popularBanksAvailableCount++;
                    }
                });

                // hide labels when no bank available
                if (popularBanksAvailableCount == 0) {
                    $("#popular-banks-wrapper").addClass("hide");
                }

                // hide other nb banks in dropdown if not available
                var otherBanksAvailableCount = 0;
                $('select#nbSelect option').each(function () {
                    var bankId = $(this).data('bankid');
                    if (bankId)
                        bankId = bankId.toString();
                    if (promocode.nbList.indexOf(bankId) == -1) {
                        $(this).attr("disabled", "disabled");
                    } else {
                        otherBanksAvailableCount++;
                    }
                });

                // hide dropdown
                if (otherBanksAvailableCount == 0) {
                    $("#other-banks-wrapper").addClass("hide");
                }

            }

            // show available mode tabs
            for (var i = 0; i < showTabs.length; i++) {
                var tab = showTabs[i];
                try {
                    var link = $('a[href="#' + tab.toLowerCase() + '-card"]').parent().removeClass("hide");
                    $("div#" + tab.toLowerCase() + "-card").removeClass("f-hide");
                } catch (e) {
                }
            }

            if (_.indexOf(showTabs, 'PPI') != -1) {
                // show paytm cash
                $(".paytmcash-card").removeClass("hide");
                showAllPaymentModes();
                $(".promocode-options-msg-2, .promocode-options-msg").toggleClass('hide');
            }


            // activate first tab if tab was open
            if ($('.cards-control .card.active:not(.hide)').length == 0) {
                openFirstPaymentMode();
            }
        }

    }

    // unhide all modes
    showAllPaymentModes = function () {
        $('.cards-content .content').removeClass("f-hide");
        $('.cards-control .card').removeClass("hide");
        $('#sc-card .control-group.card').removeClass("hide");
        $("#popular-banks-wrapper, #other-banks-wrapper").removeClass("hide");
        $('.netbanking-panel li').removeClass("hide");
        $('select#nbSelect option').removeAttr("disabled");
        $(".paytmcash-card").removeClass("hide");
        $(".promocode-options-msg-2, .promocode-options-msg").toggleClass('hide');
        $("a#show-other-options").hide();
        promocodeAvailable = false;
        processPayments();
    }
    // setup promocode //


    // setup saved cards //

    // border colors
    $('.sc-cards .sc-checkbox').on("change", function (e, isTriggered) {
        var scCards = $(this).parents(".sc-cards");
        var cardTabs = $(this).parents(".cards-tabs");

        scCards.find(".sc-checkbox").each(checkBorderStatus);

        // open first tab when sc is unchecked
        if (!isTriggered) {
            var checkbox = scCards.find(".sc-checkbox:checked");
            if (checkbox.length == 0) {
                cardTabs.find(".cards-control .card").eq(0).click();
            }
        }
    });

    // checkboxes
    $(".sc-cards .checkbox").on("change", function () {
        $(this).parents(".sc-cards").find(".checkbox").checkbox("checkChecked");
    });

    // delete card
    $("body").on('touchstart click', '.deleteCard', function () {
        var savedCardId = $(this).attr('cardId');
        var savedCardNum = $(this).closest(".card").find(".savedCardLabel").val();

        // show confirm modal
        $("#delete-confirm-modal").toggleClass("md-show");

        if (!$(this).hasClass('buttonCls')) {
            $("#delete-confirm-modal .deleteCard").data("confirmCardId", savedCardId);
            $("#delete-confirm-modal .del-card-num").text(savedCardNum);
        }

        var confirmCardId = $(this).data("confirmCardId");
        if (confirmCardId) {
            savedCardId = confirmCardId;
            $.ajax({
                url: "/theia/DeleteCardDetails",
                type: "POST",
                headers: {'X-CSRF-TOKEN': csrf},
                data: {
                    "savedCardId": savedCardId,
                    "MID": authNewConfig.data('mid'),
                    "ORDER_ID": authNewConfig.data('orderid')
                },
                success: function (data, status, jqXHR) {
                    if ('success' == data) {
                        $(".deleteCard[id=delete-" + savedCardId + "]").parents(".control-group").remove();
                        // TODO : auto select next saved card
                        checkSavedCardsCount();
                    }
                }
            });
        }
        return false;
    });


    // enable submit btn on cvv fill
    $(".scCvvInput").on("keyup", function () {
        var cvv = $(this).val();
        var card = $(this).parents(".card");
        var maxlength = parseInt($(this).attr("maxlength"));
        var minlength = parseInt($(this).attr("minlength"));

        var valid;
        if (card.find(".sc-icon").hasClass("MAESTRO-sc")) {
            if (cvv == '' || !isNaN(cvv)) {
                valid = true;
            } else {
                valid = false;
            }
        }
        else if (card.find(".sc-icon").hasClass("AMEX-sc")) {
            if (!isNaN(cvv) && (cvv.length == maxlength)) {
                valid = true;
            } else {
                valid = false;
            }
        } else {
            if (!isNaN(cvv) && (cvv.length == minlength)) {
                valid = true;
            } else {
                valid = false;
            }
        }
        var idebitOptionChecked = card.find("[name='isIciciIDebit']:checked");
        if (idebitOptionChecked && idebitOptionChecked.val() == 'Y') {
            valid = true;
        }
        card.find("#scSubmit").toggleClass("valid", valid).trigger("change-validity");
    });

    /*
     * idebit changes
     */
//	if(isIdebitEnabled){
    $("input.sc-checkbox").on("click change", function () {
        if( $(this).closest(".control-group") && $(this).closest(".control-group").hasClass("active")) {
            event  && event.preventDefault() && event.stopPropagation();
            return;
        }
        var bankName = $(this).parents("div.control-group").find(".issuerName").text();
        var cardId = $(this).parents("div.control-group").find(".sc-checkbox")[0].id;
        var issuerLowSuccessRate = $(this).parents("div.control-group").find(".issuerLowSuccessRate").text() == 'true';
        var cardSchemeLowSuccessRate = $(this).parents("div.control-group").find(".cardSchemeLowSuccessRate").text() == 'true';
        var cardSchemeName = $(this).parents("div.control-group").find(".cardSchemeName").text();
        var isIdebitEnabled = $(this).parents("div.control-group").find(".issuerIdebit").text();
        var formNm = $(this).parents("form");
        if ($(this)[0].checked) {

            if (isIdebitEnabled == "true" || isIdebitEnabled == true && $(this).parents("div.control-group").find(".idebitSavedCard")) {
                $(this).parents("div.control-group").find(".idebitSavedCard").show();
                if ($(this).parents("div.control-group").find("#idebitSavedCard").find('[name="isIciciIDebit"]') && $(this).parents("div.control-group").find("#idebitSavedCard").find('[name="isIciciIDebit"]')[0])
                    $(this).parents("div.control-group").find("#idebitSavedCard").find('[name="isIciciIDebit"]')[0].click()
            }

            if (issuerLowSuccessRate || cardSchemeLowSuccessRate) {
                var errorMsg = converterLowSuccessMsg(issuerLowSuccessRate, cardSchemeLowSuccessRate, bankName, cardSchemeName);
                formNm.find('#warningDiv-' + cardId).show();
                formNm.find('#errorMsg-' + cardId).text(errorMsg);
            } else {
                formNm.find('#warningDiv-' + cardId).hide();
                formNm.find('#errorMsg-' + cardId).text('');


            }
        } else {
            if ($(this).parents("div.control-group").find(".idebitSavedCard"))
                $(this).parents("div.control-group").find(".idebitSavedCard").hide();
            formNm.find('#warningDiv-' + cardId).hide();
            formNm.find('#errorMsg-' + cardId).text('');
        }
        var activeCardType = formNm.find("div.control-group.active").find(".cardType").val();
        //For low success rate

        // For Card Type disabled for UPI

        if (activeCardType == "UPI") {
            formNm.find("#scCvvBox").attr("disabled", "disabled");
        } else {
            formNm.find("#scCvvBox").removeAttr("disabled");

        }

        if ($(this)[0].checked) {
            if (isIdebitEnabled == "true" || isIdebitEnabled == true) {
                $("#idebitSavedCard").removeClass("hide");
                $(".paymentIdebit").removeAttr("disabled");
            } else {
                $("#idebitSavedCard").addClass("hide");
                $(".paymentIdebit").attr("disabled", "disabled");
            }
        }

    });

    $('#add-money-payment-modes #idebitSavedCard').on("click change", function (e) {
        e.stopPropagation();

        /*if(!$($(this).parents("div.control-group.card")[0]).hasClass("active"))
         {
         $($(this).parents("div.control-group.card")[0]).addClass("active");
         }*/
        console.log($(this).find("[name='isIciciIDebit']:checked").val());
        var elementSelected = $(this).find("[name='isIciciIDebit']:checked");
        if (elementSelected && elementSelected.val() == 'Y') {
            //remove CVV button from selected card element
            $(this).parents('.control-group').find('.saveCardCvvDiv .save-card-cvv').hide();
        } else {
            $(this).parents('.control-group').find('.saveCardCvvDiv .save-card-cvv').show();
        }
    });

    $('#merchant-payment-modes #idebitSavedCard').on("click change", function (e) {
        e.stopPropagation();

        /*if(!$($(this).parents("div.control-group.card")[0]).hasClass("active"))
         {
         $($(this).parents("div.control-group.card")[0]).addClass("active");
         }*/
        console.log($(this).find("[name='isIciciIDebit']:checked").val());
        var elementSelected = $(this).find("[name='isIciciIDebit']:checked");
        if (elementSelected && elementSelected.val() == 'Y') {
            //remove CVV button from selected card element
            $(this).parents('.control-group').find('.saveCardCvvDiv .save-card-cvv').hide();
        } else {
            $(this).parents('.control-group').find('.saveCardCvvDiv .save-card-cvv').show();
        }
    });

//	}
    $("input.idebitDC").on("click", function () {
//		 if(isIdebitEnabled){
        var idebitBox = $(this).parents("#idebitPayOption");
        idebitBox.find(".bootstrap-checkbox").find(".cb-icon-check").hide();
        idebitBox.find(".bootstrap-checkbox").find(".cb-icon-check-empty").show();
        if ($(this).is(':checked')) {
            $(this).siblings(".bootstrap-checkbox").find(".cb-icon-check").show();
            $(this).siblings(".bootstrap-checkbox").find(".cb-icon-check-empty").hide();
        }
        // if selected one is ATM pin hide CVV container
        if ($(this)[0].value == 'Y') {
            $(this).parents('.dc-form').children().find('#dcCvvWrapper').hide(); // to hide if ATM option is selected
        } else {
            $(this).parents('.dc-form').children().find('#dcCvvWrapper').show();
        }
//		 }
    });

    /*
     * End of idebit changes
     */

    // open / close card
    $("input.sc-checkbox").on("change", function (e) {

        var form = $(this).parents("form");
        var card = $(this).parents(".card");
        var tab = $(this).parents(".cards-tabs");
        var cards = $(this).parents('.sc-cards');


        // IRCTC SAVE CARDS
        if (isIrctcAvailable) {
            var cardType = cards.find(".sc-checkbox:checked").data("ctype");
            if (cardType) {
                if (cardType == 'CREDIT_CARD') {
                    cardType = 'cc'
                }
                if (cardType == 'DEBIT_CARD') {
                    cardType = 'dc'
                }
                if (cardType == 'UPI') {
                    cardType = 'upi'
                }
                var href = "#" + cardType.toLowerCase() + "-card";

                var div = tab.find('a[href="' + href + '"]').siblings(".irctcData");
                setIrctcCharges(div);
            }
        }

        form.find("#scCvvBox")[0].value = "";
        form.find(".saveCardCvvDiv").hide();

        // set selected card as required
        cards.find(".required").removeClass("required");

        // if card is selected
        var checkbox = this;
        setTimeout(function () {

            if (checkbox.checked) {

                // uncheck other save cards
                cards.find("input.sc-checkbox").not(checkbox).each(function () {
                    if (this.checked) {
                        this.checked = false;
                        $(this).trigger("change");
                        $(card).removeClass("active");
                    }
                });
                var sccardType = $(checkbox).parents(".control-group").find(".cardType").val();
                // set value hidden field
                var saveCard = $(checkbox).attr('id');
                form.find("#savedCardId")[0].value = saveCard;
                if(sccardType) {
                    if (sccardType == "UPI") {
                        form.find("#scCvvBox").attr("disabled", "disabled");
                    } else {
                        form.find("#scCvvBox").removeAttr("disabled");
                    }
                }

                // show cvv box
                form.find("#cvvDiv-" + saveCard).show();


                if (!card.find("#scSubmit").hasClass('no-check')) {
                    card.find("#scSubmit").addClass("required");
                }

				var sccardType = $(checkbox).parents(".control-group").find(".cardType").val();

				if (sccardType == "UPI") {
					form.find("#scCvvBox").attr("disabled", "disabled");
				} else {
					form.find("#scCvvBox").removeAttr("disabled");
				}

				$(card).addClass("active");


                // close other modes tabs
            }

        }, 100);


        form.find(".scCvvError").hide();
        form.find(".scCvvInput").removeClass("error1").focus();

        //card.find("#scSubmit").attr("disabled", true);
    });

    // Inner Tabs for CHEQUE/DD/NEFT
    $("body").on('click', '.innerTabs a', function (e) {

        var payID = $(this).attr("href");
        var parentDiv = $(this).parents("#merchant-payment-modes");

        // Active Inner Tab
        $(".innerTabs li").removeClass("active");
        $(this).parent("li").addClass("active");

        //Show Inner Tab Box
        $(".neftddBox").addClass("hide");
        parentDiv.find(payID).removeClass("hide");


        return false;
    });

    // Inner Tab Info CHEQUE/DD/NEFT
    $("body").on("mouseover", ".infoBox", function (e) {
        $(this).siblings("div.info_checkDDNeft").removeClass("hide");
    });
    $("body").on("mouseout", ".infoBox", function (e) {
        $(this).siblings("div.info_checkDDNeft").addClass("hide");
    });

    // NB/DD/CHEQUE BANK LIST to UPDATE VALUE
    $("body").on("change", ".offlineSelect", function (e) {
        var bankNM = $(this).val();
        var form = $(this).parents("form");
        if ($(this).val()) {
            form.find("#bankCode").val(bankNM);
        }
    })

    // submit
    $("body").on('touchstart click', '#scSubmit', function (e) {
        window.onbeforeunload = null;
        var form = $(this).closest("form");
        var card = $(this).parents(".card");

        // set cvv in hidden field
        var cvv = card.find(".scCvvInput").val();
        form.find("#scCvvBox")[0].value = cvv;
        if (card.find('.sc-icon').length > 0) {
            //card is a cc/dc card and not imps
            // replace hidden field with value of "cc dc"
            form.find('[name="txnMode"]')[0].value = "CC";
            form.find('[name="AUTH_MODE"]')[0].value = "3D";
            form.find('[name="txnMde"]')[0].value = "SC";
        } else {
            // case of imps
            form.find('[name="txnMode"]')[0].value = "IMPS";
            form.find('[name="AUTH_MODE"]')[0].value = "OTP";
            form.find('[name="txnMde"]')[0].value = "IMPS";
        }

        //form.submit();
    });

    // check first saved cards
    $('input[name=cno][checked]').trigger("change");

    // setup saved cards //


    // setup emi form //
//Check EMI CC Card valid/invalid
    $(".emi-form input.emiCCCardNumber").on("keyup change", function () {
        ccCard = $(this);
        var ccCardNoVal = $(this).val().replace(/\s/g, '');
        var form = $(this).parents("form");
        if (ccCardNoVal.length == 6) {
            checkboxEL = form.find('input[name=emiPlanId]:checked');
            var isAjaxRequired = checkboxEL.siblings("input.isAjaxRequired").val();


            if (isAjaxRequired == "true") {
                var emiPlanId = checkboxEL.val();
                var emiBankName = form.find("input[name=emiBankName]").val();
                var params = [];
                params.push('emiPlanId=' + emiPlanId);
                params.push('bin=' + ccCardNoVal);
                params.push('emiBankName=' + emiBankName);
                var url = "checkEmiCardValidity?" + params.join('&') + "&" + $("#queryStringForSession").data("value");

                //Ajax Call for CC Card check
                $.get(url, function (data, status, xhr) {
                    var dataVal = JSON.parse(data);
                    if (dataVal.isValid == 0) {
                        form.find("div#cardInvalidmsg").show();
                    }
                    else {
                        form.find("div#cardInvalidmsg").hide();
                    }
                });

            }

        } else {
            form.find("div#cardInvalidmsg").hide();
        }
    });


    $(".emi-form").on("change keyup", validateEmiCCForm);

    $(".emiBankSelect").on("change", function () {
        if ($(".emi-form").find("div#cardInvalidmsg")) {
            $(".emi-form").find("div#cardInvalidmsg").hide();
        }
        if ($(".emi-form input.emiCCCardNumber")) {
            // $(".emi-form input.emiCCCardNumber").val("");
        }
        var form = $(this).closest("form");
        var map = $(this).parent();
        var selectedBank = $(this).val();
        form.find(".emi-bank-plans").addClass("hide");
        var elm = form.find("." + selectedBank + "-bank").removeClass("hide");

        var isBajajFin = $(".emiBankSelect").val() === "BAJAJFN" ? true:false;
        var cvvElm = form.find("input.ccCvvBox");
        if(isBajajFin)
            cvvElm.addClass("valid");
        else
            cvvElm.removeClass("valid");

        //check for retry only
        var emiPlanId = $("#emiPlanId").data("value");
        if (emiPlanId != null && emiPlanId != '') {
            $(elm.find('input')).each(function () {
                var planId = $(this).attr("value");
                if (emiPlanId === planId) {
                    $(this).checkbox("click");
                    return false;
                }
            });
        } else {
            var firstPlan = elm.find('input').eq(0);
            firstPlan.checkbox("click");
        }

        form.find("input[name=emiBankName]").val(selectedBank);

        var state = selectedBank == "none" ? true : false;
        //form.find("#emiSubmit").disableButton(state);

        form.find(".card-details-wrapper").toggleClass("hide", state);

        // bajaj finserv
         bajajFinservChanges.call(this);
    });

    // setup emi checkbox as radio btns
    $("#emi-card .checkbox").on("change", function () {
        setTimeout(function () {
            $("#emi-card .checkbox").checkbox("checkChecked");
        }, 100);

    });


    // open already selected bank (in error case)
    $(".emi-bank-map:not(.hide)").find(".emiBankSelect.selected").change();

    // setup emi form //


    // setup rewards form validations //

    $(".btn-change-rewards-details").click(function () {
        $(".rewards-otp-form").hide();
        $(".rewards-card-form").show();
        $("#rewardsAction").val("CARD_INPUT");
        return false;
    });

    $(".rewards-form").on("change keyup", validateRewardsForm);

    function validateRewardsForm() {
        var form = $(this);

        var programElm = form.find("#rewardsProgram");
        var program = programElm.val();
        var isRewardsProgramValid = program == "-1" ? false : true;
        programElm.toggleClass("valid", isRewardsProgramValid);

        var otpElm = form.find("#rewardsOTP");
        var otp = otpElm.val();
        var isRewardsOTPValid = !isNaN(otp) && otp.length == 6 ? true : false;
        otpElm.toggleClass("valid", isRewardsOTPValid);

        var cardInput = form.find('#rewardsCardNo');
        var cardInputHidden = form.find("input[name=cardNumber]");
        var rewardsCardNo = cardInputHidden.val();
        var isRewardsCardValid = !isNaN(rewardsCardNo) && rewardsCardNo.length >= 12 ? true : false;
        cardInputHidden.toggleClass("valid", isRewardsCardValid);

        var mobileElm = form.find("#rewardsMobile");
        var rewardsMobile = mobileElm.val();
        var isRewardsMobileValid = !isNaN(rewardsMobile) && rewardsMobile.length == 10 ? true : false;
        mobileElm.toggleClass("valid", isRewardsMobileValid);


        if (form.find(".rewards-card-form").hasClass("hide")) {
            form.find(".rewards-card-form .required").addClass("valid");
        } else { // when otp form is shown
            form.find(".rewards-otp-form .required").addClass("valid");
        }
        if (rewardsCardNo.length > 6) {
            var formNm = $(this).parents("form");
            var cardType = changeCardTypeIcon(rewardsCardNo, formNm);

            if (cardType == "maestro") {
                //			form.find('#dcStoreCardWrapper').hide();
                form.find('#maestroOpt').show();
            }

            if (cardType == "amex" || cardType == "diners")
                cardType = "INVALID CARD";

            var icon = cardType == "INVALID CARD" ? "d" : cardType;
            cardInput.removeClass('d maestro master visa diners rupay');
            cardInput.addClass(icon);
        }
        form.trigger("change-validity");

    };

    // setup rewards form validations //

    // setup rewards form validations //
    $(".exclamation").on({
        mouseover: function () {
            $(this).find("div#convenienceChrg").show();
        },
        mouseout: function () {
            $(this).find("div#convenienceChrg").hide();
        }
    });


    // setup dc form validations //

    $(".dc-form").on("change keyup", validateDCForm);

	function validateDCForm(){
        //  if(!(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;

		var form = $(this);
        var monthElm = form.find("select.dcExpMonth");
        var month = monthElm.val();
        var isDCExpMonthValid = month == "0" ? false : true;

        var yearElm = form.find("select.dcExpYear");
        var year = yearElm.val();
        var isDCExpYearValid = year == "0" ? false : true;

        var binDetail = binObject.info.binDetail;
        if(!binDetail || (binDetail && binDetail.cardName && binDetail.cardName != "MAESTRO")) {
            monthElm.toggleClass("valid", isDCExpMonthValid);
            yearElm.toggleClass("valid", isDCExpYearValid);
        } else if(binDetail && binDetail.cardName && binDetail.cardName == "MAESTRO") {
            monthElm.addClass("valid");
            yearElm.addClass("valid");
        }
		var cvvElm = form.find("input.dcCvvBox");
		var cvv = cvvElm.val();

		/*
		 * @changes by Manu Pandit
		 * TODO change dcc valid value
		 * Rules as discussed with Vaishakh:
		 * In case of maestro keep cvv optional.. any length
		 * Other than mastero all cards to have 3 length
		 */
		var isDCCvvValid=false;

        isDCCvvValid = (!isNaN(cvv) && (cvv.length == 3)) ? true : false;
        if(form.find("input.dcCardNumber").hasClass("amex")){
            var isDCCvvValid = (!isNaN(cvv) && (cvv.length == 4)) ? true : false;
        }
		if (form.find("#idebitPayOption") && !form.find("#idebitPayOption").hasClass('hide') &&
			form.find("[name='isIciciIDebit']:checked") && form.find("[name='isIciciIDebit']:checked").val() == 'Y') {
			isDCCvvValid=true;
		}
//		var isDCCvvValid = (!isNaN(cvv) && (cvv.length == 3 || cvv.length == 4)) ? true  : false;
        cvvElm.toggleClass("valid", isDCCvvValid);

        var isDCNumberValid = false;
        var cardInput = form.find('input.dcCardNumber');
        var cardInputHidden = form.find("input[name=cardNumber]");
        var cardNumber = cardInputHidden.val();

        cardInputHidden.toggleClass("valid", isDCNumberValid);

        form.find('#dcStoreCardWrapper').show();
        form.find('#maestroOpt').hide();
        if (cardNumber.length >= 6) {
            var formNm = $(this).parents("form");
            var cardType = changeCardTypeIcon(cardNumber, formNm);
          //  var clueBox = form.find(".cvv-clue-box");

            //to show help icon for default and amex
            if (!isNaN(cardNumber) && cardNumber.length >= 12 && cardNumber.length <= 19) {
                /*form.find(".cvv-clue-box").removeClass("hide");

                var clueType = cardType == "amex" ? ".amex-clue" : ".default-clue";
                if(cardType == "amex")
                {
                    form.find(".default-clue").addClass("hide");
                    form.find(".amex-clue").removeClass("hide");
                }
                else{
                    form.find(".default-clue").removeClass("hide");
                    form.find(".amex-clue").addClass("hide");
                }
              //  form.find(clueType).removeClass("hide");
                clueBox.removeClass("hide"); */

                isDCNumberValid = true;
            }else {
                // form.find(".dc-cvv-clue").addClass("hide");
                // clueBox.addClass("hide");
                isDCNumberValid = false;
            }

            if (cardType == "maestro") {
                //			form.find('#dcStoreCardWrapper').hide();
                form.find(".cvv-clue-box").addClass("hide");
                form.find('#maestroOpt').show();
                monthElm.add(yearElm).add(cvvElm).addClass("valid");
            }


            var icon = cardType == "INVALID CARD" ? "d" : cardType;
            cardInput.removeClass('d maestro master visa diners rupay');
            cardInput.addClass(icon);
        }
        form.trigger("change-validity");

    };

    // setup dc form validations //

	var binObject ={
		firstSix: '',
		info: {},
        binData: {}
	};
	function checkBinDetails(value,formNm){
		// value should be of string type
		if(!(typeof value === 'string' || value instanceof String))
			return ;

        var txnMode = "";

        if(formNm && formNm.children("input[name='txnMode']")) {
            txnMode = formNm.children("input[name='txnMode']")[0].value;
        }

		// if value length is greater than 6 set value in bin level object
		if(value.length>=6){
			value = value.substring(0,6);
            if(!binObject.binData[value] && (binObject.firstSix.length == 0 || value != binObject.firstSix)){
                binObject.firstSix = "";
                $._post("/theia/bin/fetchBinDetails", {
						"bin": value.substring(0,6),
						"MID": $("#sucess-rate").data("mid").toString(),
						"ORDER_ID":$("#sucess-rate").data("oid").toString(),
                        "txnMode": txnMode
					},
					function(data, status) {
                        binObject.firstSix = "";
                        if (typeof data != "object") return;
                        binObject.info = data;
                        binObject.binData[value] = data;
                        binFunctionalities(value,formNm);
					},
                    function(data){
                        binObject.firstSix = "";
                    });
			} else if(binObject.firstSix.length == 0){
                binObject.info = binObject.binData[value];
                binFunctionalities(value,formNm);
            }
		}else {
            enableMaestroFields(true,formNm);
        }

		// pass bin level object to respective functionalities
		setTimeout(function() {
			binFunctionalities(value,formNm)
		},500);
	}

	function binFunctionalities(value,formNm){
        inputMaxLength(formNm);
        showPromoError(value, formNm);
		iciciIdebitBin(value,formNm);
		cardLowSuccesRateBin(value,formNm);
		changeCardTypeIcon(value,formNm);
	}


    function enableMaestroFields(enableMaestro,formNm) {

        var cvvFld = formNm.find("#ccCvvBox, #dcCvvBox, #scCvvBox, #emiCvvNumber");
        var yearFld = formNm.find('#dcExpYear, #ccExpYear');
        var monthFld = formNm.find('#dcExpMonth, #ccExpMonth, #emiExpMonth');

        if(enableMaestro)
        {
            cvvFld && (cvvFld.length > 0) && (cvvFld[0].disabled = "");
            yearFld && (yearFld.length > 0) && (yearFld[0].disabled = "");
            monthFld && (monthFld.length > 0) && (monthFld[0].disabled = "");
        }
    }


    function inputMaxLength(formNm) {
        var inputFld = [], cvvFld = [];
        inputFld = formNm.find("#cn1, #cn, #rewardsCardNo");
        cvvFld = formNm.find("#ccCvvBox, #dcCvvBox, #scCvvBox, #emiCvvNumber");
        var yearFld = formNm.find('#dcExpYear, #ccExpYear');
        var monthFld = formNm.find('#dcExpMonth, #ccExpMonth, #emiExpMonth');
        var disableCvv = false;

        if(inputFld && (inputFld.length > 0) && inputFld[0].value.replace(/\s/g, '').length < 6)
            return;

        if(inputFld && binObject.info.binDetail && binObject.info.binDetail.cardName) {
            var cardName = binObject.info.binDetail.cardName;
            switch(cardName) {
                case "VISA" :
                    inputFld[0].maxLength = 19;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
                    break;
                case "MASTER" :
                    inputFld[0].maxLength = 19;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
                    break;
                case "DINERS" :
                    inputFld[0].maxLength = 17;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
                    break;
                case "MAESTRO" :
                    inputFld[0].maxLength = 23;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
                    disableCvv = true;
                    break;
                case "AMEX" :
                    inputFld[0].maxLength = 18;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 4);
                    break;
                case "DISCOVER" :
                    inputFld[0].maxLength = 19;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
                    break;
                default:
                    inputFld[0].maxLength = 19;
                    cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
            }
        } else {
            if(inputFld){
                inputFld[0].maxLength = 19;
                cvvFld && (cvvFld.length > 0) && (cvvFld[0].maxLength = 3);
            }
        }
        if(cvvFld && (cvvFld.length > 0) && disableCvv) cvvFld[0].disabled = "disabled";
        else if(cvvFld && (cvvFld.length > 0)) cvvFld[0].disabled = "";

        if(disableCvv) {
            if(yearFld && yearFld.length > 0 && yearFld[0]) yearFld[0].disabled= "disabled";
            if(monthFld && monthFld.length > 0 && monthFld[0]) monthFld[0].disabled= "disabled";

        } else {
            if(yearFld && yearFld.length > 0 && yearFld[0]) yearFld[0].disabled= "";
            if(monthFld && monthFld.length > 0 && monthFld[0]) monthFld[0].disabled= "";
        }
    }

    function toggleLuhnErr(valid, form) {
        var luhnErr = form.find("#luhnError");
        if (luhnErr && !valid) {
            luhnErr.show();
            luhnErr.addClass("shake red-text");
            //  setTimeout(function(){luhnErr.addClass("shake red-text");}, 10);
            setTimeout(function(){luhnErr.removeClass("shake red-text");}, 2000);
            form.find(".error").addClass("inlineEle");
        } else if (luhnErr) {
            luhnErr.hide();
            luhnErr.removeClass("shake red-text");
            form.find(".error").removeClass("inlineEle");
        }
    }

    function checkLuhn(cardNo) {
        var nDigits = cardNo.length;
        var nSum = 0, isSecond = false;
        for (var i = nDigits - 1; i >= 0; i--) {
            var d = parseInt(cardNo[i]);
            if (isSecond == true)
                d = d * 2;

            nSum += parseInt(d / 10);
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }


    function validateCardOnSubmit(form) {
        if($(form).is("#card.cc-form") || $(form).is("#dc-form.dc-form") || $(form).is("#emi-form.emi-form")) {
            var inputFld = [], cvvFld = [];
            inputFld = form.find("#cn1, #cn, #rewardsCardNo");
            cvvFld = form.find("#ccCvvBox, #dcCvvBox, #scCvvBox, #emiCvvNumber");
            var yearFld = $(form).find('#dcExpYear, #ccExpYear');
            var monthFld = $(form).find('#dcExpMonth, #ccExpMonth, #emiExpMonth');
            var isBajajFin =  ((form).find(".emiBankSelect") && (form).find(".emiBankSelect").val() === "BAJAJFN") ? true:false;
            var cardInputHidden = form.find("input[name=cardNumber]");
            var cardNum = cardInputHidden.val();
            cardNum || (cardNum = inputFld.val().replace(/\s/g, ''));

            var CardName;

            if(binObject && binObject.binData && binObject.binData[cardNum.slice(0,6)] && typeof binObject.binData[cardNum.slice(0,6)] == "object") {
                CardName = binObject.binData[cardNum.slice(0,6)].binDetail.cardName;
            } else {
                CardName = getCardType(cardNum).toUpperCase();
            }
            var isDCCvvValid = false;
            if (form.find("#idebitPayOption") && !form.find("#idebitPayOption").hasClass('hide') &&
                form.find("[name='isIciciIDebit']:checked") && form.find("[name='isIciciIDebit']:checked").val() == 'Y') {
                isDCCvvValid=true;
            }


            if(!checkLuhn(cardNum)) {
                toggleLuhnErr(false, form);
                return false;
            }

            if(inputFld) {
                if(cardNum.length < 12 || cardNum.length > 19) {
                    cardInputHidden.removeClass('valid');
                    return false;
                } else {
                    cardInputHidden.addClass("valid");
                }


                var date = new Date();
                var year = (new Date()).getFullYear();
                var month = ((new Date()).getMonth()+1);
                (month < 10) && (month = "0"+month);
                var currDate = year.toString() + month.toString();


                if (CardName != "MAESTRO" && CardName != "NONE" && (yearFld.val() == "0" || monthFld.val() == "0" || ((yearFld.val() + monthFld.val()) < currDate))) {
                    monthFld.removeClass('valid');
                    return false;
                } else {
                    monthFld.addClass('valid');
                    yearFld.addClass('valid');
                }

                if(cvvFld && !isDCCvvValid && cvvFld[0] && CardName != "MAESTRO" && CardName != "NONE" && !isBajajFin) {
                    if((CardName == "AMEX" && cvvFld[0].value.length != 4) || (CardName != "AMEX" && cvvFld[0].value.length != 3)) {
                        cvvFld.removeClass('valid');
                        return false;
                    } else {
                        cvvFld.addClass('valid');
                    }

                } else {
                    cvvFld && (cvvFld.addClass('valid'));
                }

            }
            return true;
        }
        cardInputHidden && (cardInputHidden.addClass('valid'));
        cvvFld && (cvvFld.addClass('valid'));
        if($(form) && $(form).find("#dcExpMonth")) {
            $(form).find("#dcExpMonth").addClass('valid');
        }
        return true;
    }





    function showPromoError(value, formNm) {
        if(value.length>=6 && binObject && binObject.info && binObject.info.promoResultMessage && binObject.info.promoResultMessage.length > 0) {
            formNm.find(".promo-code-error").removeClass("hide").text(binObject.info.promoResultMessage);
        } else {
            formNm.find(".promo-code-error").addClass("hide");
        }
    }


    function changeCardTypeIcon(value, formNm) {

	    var currentCardType = 'INVALID CARD';
        var sixDigits="";
        (value.length>=6) && (sixDigits= value.substr(0, 6));

        if(value.length>=6 && binObject && binObject.binData && binObject.binData[sixDigits] && binObject.binData[sixDigits].binDetail && binObject.binData[sixDigits].binDetail.cardName) {
            currentCardType = binObject.binData[sixDigits].binDetail.cardName.toLowerCase();
        }

        if(formNm && formNm.length > 0) {

            // remove existing icon if any
            var allIcons = ['master', 'maestro', 'visa', 'discover', 'bajajfn', 'rupay', 'amex', 'diners', 'INVALID CARD']
            // remove all icons from the list
            allIcons.forEach(function (cardType) {
                formNm.find('.card-field-selector').removeClass(cardType);
            });

            formNm.find('.card-field-selector').addClass(currentCardType);
            if (formNm.hasClass('dc-form') || formNm.hasClass('cc-form') || formNm.hasClass('emi-form')) {

                if (currentCardType != "INVALID CARD") {
                    var clueBox = formNm.find(".cvv-clue-box, .clue-box");
                    if (clueBox) {
                        if (currentCardType == "amex") {
                            formNm.find(".default-clue").addClass("hide");
                            formNm.find(".amex-clue").removeClass("hide");
                        }
                        else if (currentCardType != "amex" && currentCardType != "maestro") {
                            formNm.find(".default-clue").removeClass("hide");
                            formNm.find(".amex-clue").addClass("hide");
                        }
                        clueBox.removeClass("hide");
                    }
                } else {
                    formNm.find(".default-clue").addClass("hide");
                    formNm.find(".amex-clue").addClass("hide");
                }

            }
        }
            return currentCardType;
	}

    function converterLowSuccessMsg(issuerLowSuccessRate, cardSchemeLowSuccessRate, issuerName, schemeName) {
        lowSuccessRateMsg = $("#error-details").data("lowerrormsg");
        if (cardSchemeLowSuccessRate)
            lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD', schemeName + " cards");
        else
            lowSuccessRateMsg = lowSuccessRateMsg.replace('@BANK @METHOD', issuerName + " bank");
        return lowSuccessRateMsg;
    }

    function cardLowSuccesRateBin(value, formNm) {
        if (value.length >= 6 && binObject && binObject.info && (binObject.info.issuerLowSuccessRate || binObject.info.cardSchemeLowSuccessRate)) {
            // use converterLowSuccessMsg() to display the message
            converterLowSuccessMsg(binObject.info.issuerLowSuccessRate, binObject.info.cardSchemeLowSuccessRate, binObject.info.binDetail.bankCode, binObject.info.binDetail.cardName);
            // display block for message element
            formNm.find('#warningDiv').show();
            formNm.find('#errorMsg').text(lowSuccessRateMsg);
        } else {
            // display hidden for message element
            formNm.find('#warningDiv').hide();
            formNm.find('#errorMsg').text('');
        }
    }

    function iciciIdebitBin(value, formNm) {
        if (value.length >= 6 && binObject && binObject.info && binObject.info.binDetail && (binObject.info.binDetail.iDebitEnabled
            || binObject.info.binDetail.iDebitEnabled == "true")) {
            formNm.find("div#idebitPayOption").removeClass("hide");
            formNm.find(".idebitDC").removeAttr("disabled");
            // check if option selected is ATM then hide CVV box
            if (formNm.find("[name='isIciciIDebit']:checked") && formNm.find("[name='isIciciIDebit']:checked").val() == 'Y') {
                formNm.children().find('#dcCvvWrapper').hide();
                $(formNm.find(".idebitDC")[0]).click();
            }
        } else {
            formNm.find("div#idebitPayOption").addClass("hide");
            formNm.find(".idebitDC").attr("disabled", "disabled");
            formNm.children().find('#dcCvvWrapper').show();
        }
    }

    //DC IDEBIT CARD CHECK
//	 var verifier={
//         	verified: false,
//         	firstSixDigit: ''
//         };
//	 if(isIdebitEnabled){
//	    $(".dcCardNumber").on("keydown",function(){
//            var formNm= $(this).parents("form");
//            var cardNo=$(this).parent().find("input[name='cardNumber']").val();
//            if(isIdebitEnabled){
//	            if(cardNo && cardNo.length>5){
//		            var url="/theia/bin/checkIciciDebitBin?bin="+cardNo.substring(0,6);
//		            if(!verifier.verified || (verifier.firstSixDigit!=cardNo.substring(0,6))) {
//		            	$.get(url, function(res){
//		                       //response data are now in the result variable
//		            		//console.log('result:',res);
//		            		//the response is returned as string
//		                    if(res=="true"){ 
//		                    	verifier.verified=true;
//		                    	formNm.find("div#idebitPayOption").removeClass("hide");
//	                           	formNm.find(".idebitDC").removeAttr("disabled");
//		                    } else {
//		                    	verifier.verified=false;
//		                    	formNm.find("div#idebitPayOption").addClass("hide");
//	                            formNm.find(".idebitDC").attr("disabled","disabled");
//		                    }
//		                    verifier.firstSixDigit = cardNo.substring(0,6);
//	                    });
//		            } else if(verifier.verified && verifier.firstSixDigit==cardNo.substring(0,6)){
//		            	//if it is verified and card digits are also same, it should always be displayed
//		            	formNm.find("div#idebitPayOption").removeClass("hide");
//	                   	formNm.find(".idebitDC").removeAttr("disabled");
//		            }
//	            } else {
//	                    formNm.find("div#idebitPayOption").addClass("hide");
//	            }
//            }
//	    });
//
//	 }

    // setup cc form validations //

    // only numeric cvv
    $("input[name=cvvNumber], .scCvvInput").on("keypress", isNumber);

    function isNumber(e) {
        var charCode = (e.which) ? e.which : evt.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
            e.preventDefault();
            return false;
        }
        return true;
    };
/*
    $(".sc-Form").on("change keyup", validateSCForm);

    function validateSCForm(){

        var form = $(this);

        var cvvElm = form.find("input.scCvvInput");
        var cvv = (cvvElm) ? cvvElm.val() : '';
        var isCCCvvValid = (!isNaN(cvv) && (cvv.length == 3)) ? true : false;
        if(form.find("input.ccCardNumber").hasClass("amex")){
            var isCCCvvValid = (!isNaN(cvv) && (cvv.length == 4)) ? true : false;
        }
        cvvElm.toggleClass("valid", isCCCvvValid);


    }*/

    $(".cc-form").on("change keyup", validateCCForm);

	// NOTE : works for both cc and emi and rewards
	function validateCCForm(){
        //  if(!(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;

		var form = $(this);

        var monthElm = form.find('select.ccExpMonth');
        var month = monthElm.val();
        var isCCExpMonthValid = month == "0" ? false : true;

        var yearElm = form.find('select.ccExpYear');
        var year = yearElm.val();
        var isCCExpYearValid = year == "0" ? false : true;

        var binDetail = binObject.info.binDetail;
        if(!binDetail || (binDetail && binDetail.cardName && binDetail.cardName != "MAESTRO")) {
            monthElm.toggleClass("valid", isCCExpMonthValid);
            yearElm.toggleClass("valid", isCCExpYearValid);
        } else if(binDetail && binDetail.cardName && binDetail.cardName == "MAESTRO") {
            monthElm.addClass("valid");
            yearElm.addClass("valid");
        }
		var cvvElm = form.find("input.ccCvvBox");
		var cvv = cvvElm.val();
		var isCCCvvValid = (!isNaN(cvv) && (cvv.length == 3)) ? true : false;
		if(form.find("input.ccCardNumber").hasClass("amex")){
			var isCCCvvValid = (!isNaN(cvv) && (cvv.length == 4)) ? true : false;
		}
		cvvElm.toggleClass("valid", isCCCvvValid);

        var isCCNumberValid = false;
        var cardInput = form.find("input.ccCardNumber");
        var cardInputHidden = form.find("input[name=cardNumber]");
        var cardNumber = cardInputHidden.val();
        if (cardNumber.length >= 6) {
            var formNm = $(this).parents("form");
            var cardType = changeCardTypeIcon(cardNumber, formNm);
          //  var clueBox = form.find(".clue-box");
            if (!isNaN(cardNumber) && cardNumber.length >= 12 && cardNumber.length <= 19) {
                /* show cvv image
                var clueType = cardType == "amex" ? ".amex-clue" : ".default-clue";
                if(cardType == "amex")
                {
                    form.find(".default-clue").addClass("hide");
                    form.find(".amex-clue").removeClass("hide");
                }
                else{
                    form.find(".default-clue").removeClass("hide");
                    form.find(".amex-clue").addClass("hide");
                }
               // form.find(clueType).removeClass("hide");
                clueBox.removeClass("hide"); */

				isCCNumberValid = true;
			} else {
				// form.find(".cc-cvv-clue").addClass("hide");
				// clueBox.addClass("hide");
				isCCNumberValid = false;
			}
            if (cardType == "maestro") {
                //			form.find('#dcStoreCardWrapper').hide();
                form.find(".cvv-clue-box").addClass("hide");
                form.find('#maestroOpt').show();
                monthElm.add(yearElm).add(cvvElm).addClass("valid");
            }
		}
		cardInputHidden.toggleClass("valid", isCCNumberValid)

//		form.find('#ccStoreCardWrapper').show();

        /*if (cardType == "amex") {
         form.find("#ccStoreCardWrapper").hide();
         }*/

        var icon = cardType == "INVALID CARD" ? "c" : cardType;
        cardInput.removeClass('c amex master visa diners rupay');
        cardInput.addClass(icon);

        form.trigger("change-validity");
   //     document.getElementById("cardType").value = "CREDIT_CARD";
    };

	function validateEmiCCForm(){
        //  if(!(binObject.info.binDetail && binObject.info.binDetail.cardName)) return true;

		var form = $(this);

        var monthElm = form.find('select.ccExpMonth');
        var month = monthElm.val();
        var isCCExpMonthValid = month == "0" ? false : true;
        monthElm.toggleClass("valid", isCCExpMonthValid);

        var yearElm = form.find('select.ccExpYear');
        var year = yearElm.val();
        var isCCExpYearValid = year == "0" ? false : true;
        yearElm.toggleClass("valid", isCCExpYearValid);

        var cvvElm = form.find("input.ccCvvBox");
        var cvv = cvvElm.val();
        var isBajajFin = $(".emiBankSelect").val() === "BAJAJFN" ? true:false;
        var isCCCvvValid = ((!isNaN(cvv) && (cvv.length == 3)) || (isBajajFin)) ? true : false;
        if (form.find("input.emiCCCardNumber").hasClass("amex")) {
            var isCCCvvValid = ((!isNaN(cvv) && (cvv.length == 4)) || (isBajajFin)) ? true : false;
        }
        cvvElm.toggleClass("valid", isCCCvvValid);

        var isCCNumberValid = false;
        var cardInput = form.find("input.emiCCCardNumber");
        var cardInputHidden = form.find("input[name=cardNumber]");
        var cardNumber = cardInputHidden.val();

        if (cardNumber.length >= 6) {
            var formNm = $(this).parents("form");
            var cardType = changeCardTypeIcon(cardNumber, formNm);
          //  var clueBox = form.find(".clue-box");
            if (!isNaN(cardNumber) && cardNumber.length >= 12 && cardNumber.length <= 19) {
                /* show cvv image
                var clueType = cardType == "amex" ? ".amex-clue" : ".default-clue";
                form.find(clueType).removeClass("hide");
                clueBox.removeClass("hide"); */

                isCCNumberValid = true;
            } else {
                // form.find(".cc-cvv-clue").addClass("hide");
                // clueBox.addClass("hide");
                isCCNumberValid = false;
            }
        }
        cardInputHidden.toggleClass("valid", isCCNumberValid)

//		form.find('#ccStoreCardWrapper').show();

        /*if (cardType == "amex") {
         form.find("#ccStoreCardWrapper").hide();
         }*/

        var icon = cardType == "INVALID CARD" ? "c" : cardType;
        cardInput.removeClass('c amex master visa diners rupay');
        cardInput.addClass(icon);

        form.trigger("change-validity");
    };


    // for cc dc emi rewards form - refresh card no. on submit
    $("form.validated").submit(function (e) {

        $(this).find(".ccCardNumber,.emiCCCardNumber, .dcCardNumber, .rewardsCardNumber").each(function () {
            $(this).trigger("keyup");
        });

    });


// UPI VALIDATION START

    //Regex test for UPI validation
    function validateStrVPA(vpa) {
        var re = /^[a-zA-Z0-9.-]*$/;
        return re.test(vpa);
    }

    // @ validation in VPA
    function validateAtVPA(vpa) {
        var re = /\S+@\S/;
        return re.test(vpa);
    }


    function validationUPI(vpa) {
        var isValid = false;
        if (vpa.length > 0 && vpa.length <= 255 && validateAtVPA(vpa)) {
            vpaSplit = vpa.split("@");
            if (vpaSplit.length == 2) {
                var handle = vpaSplit[0];
                var psp = vpaSplit[1];
                if (psp.length > 0 && handle.length) {
                    isValid = validateStrVPA(psp) && validateStrVPA(handle);
                }
            }
        }
        return isValid;
    }


    //upi validation scenario
    function validateUPIForm() {
        var form = $(this);
        var upiElm = form.find('input.upiPayMode');
        var upi = upiElm.val();
        var isupiValid = validationUPI(upi);
        upiElm.toggleClass("valid", isupiValid);
    }

    $(".upi-form").on("change keyup", validateUPIForm);

// UPI VALIDATION END


    var formSubmitted = false;
    // false for mobile, true for web (resubmit only for mobile)
    //var formReSubmitted = $(window).width() < 600 ? false : true;

	// show errors
	$(".btn-submit").on("touchstart click", function(e){
		e.stopPropagation();
		window.onbeforeunload = null;
		var form = $(this).closest("form");
		var errorFound = false;
		var controlGroup= $(this).closest(".control-group");
        var cardInputHidden = form.find("input[name=cardNumber]");
        var cardNum = cardInputHidden.val();

        var card = $(this).parents(".card");

        // set cvv in hidden field
        if(card) {
            var cvvInp = card.find(".scCvvInput");
            var cvv;
            if(cvvInp && cvvInp.length) {
                cvv = cvvInp.val();
                form.find("#scCvvBox")[0].value = cvv;
            }
        }


        if(!validateCardOnSubmit(form)) errorFound = true;
/*        if(cardNum && cardNum.length >= 6 && !(binObject.info.binDetail && binObject.info.binDetail.cardName)) {
            errorFound = false;
            form.find(".required").addClass("valid")
        }*/
		if(!(form.attr('name')=='savecard-form' && (controlGroup.find('.MAESTRO-sc').length!=0 || controlGroup.find('.UPI-sc').length!=0)) &&
			!(form.attr('name')=='savecard-form' && form.find("[name='isIciciIDebit']:checked") && form.find("[name='isIciciIDebit']:checked").val() == 'Y')){
			try {
				form.find(".required").not(".valid").each(function(){
					showError(form, $(this));
					errorFound = true;
				});
			} catch(e){}

		}


        if (form.attr('name') == 'savecard-form') {
            var activeCard = form.find(".control-group.active");
            var cvv = activeCard.find(".scCvvInput");
            var cvvVal = cvv.val();

            if (activeCard.length == 0) {
                return false;
            }

            form.find("input#scCvvBox").val(cvvVal);

            //  skip cvv validations when subscription full wallet
            if ($(cvv).parents(".saveCardCvvDiv").css("display") === "block" && (isSubscriptionFullWallet === false)) {

				//for maestro cvv is not required
				if(activeCard.find(".sc-icon").hasClass("MAESTRO-sc")){
					//do nothing cvv is not required
				}
				else if(activeCard.find(".sc-icon").hasClass("AMEX-sc")){
					if(cvvVal.length!=4){
						//cvv.focus();
						$(cvv).parent().find("label").addClass("shake red-text");

						return false;
					}
				}
				else if(form.hasClass("impsForm")){
					if(cvvVal.length<6){
						//cvv.focus();
						$(cvv).parent().find("label").addClass("shake red-text");
						//cvv.addClass("error1");
						return false;
					}
				}
				else{

					if(!$(activeCard[0]).find("[name='isIciciIDebit']").length >0 && cvvVal.length !=3 && !activeCard.find(".sc-icon").hasClass("SBIME-sc")){
						//cvv.focus();

						$(cvv).parent().find("label").addClass("shake red-text");
						//cvv.addClass("error1");

						return false;


                    }
                }


            }


        }


        if (errorFound || formSubmitted)
            return false;

        formSubmitted = true;

        var spinner = $("<div class='spinner-blue'></div>");
        var disable_overlay = $("<div class='disable_overlay'></div>");
        var btn = form.find('.btn-submit.active').add(form.find('.btn-submit')); // active btn in sc or normal btn
        btn.eq(0).append(spinner);
        btn.eq(0).append(disable_overlay);

        var input = btn.find('input');
        var btnText = input.val();
        input.data("value", btnText);
        input.val("Processing...");

        // fix for browser save password
        form.find("#ccCvvBox, #dcCvvBox").attr("type", "text").hide();

        setTimeout(function () {
            form.submit();
        }, 500);

        e.preventDefault();

        return true;
    });

    $("form.validated").on("change-validity", function (e) {
        var form = $(this);
        form.find(".required.valid").each(function () {
            showError(form, $(this), true);
        });
    });

    function showError(form, elm, hide) {
        var name = elm.attr("name");
        if (!name)
            return false;

        var label = form.find("label[for=" + name + "]");

        if (elm.prop("tagName") == "SELECT") {
            label = form.find("label." + elm.attr("id"));
        }

        label.removeClass("shake red-text");

        // dont show
        if (hide)
            return false;

		setTimeout(function(){
			label.addClass("shake red-text");
		}, 10);
		setTimeout(function(){
			label.removeClass("shake");
		}, 2000);
	}
    function removeFieldError(label) {
        if(label) {
            label.removeClass("red-text");
        }
    }
	// update card num in hidden field
	$("#cn, #cn1, #rewardsCardNo").on("keyup", function(e){
		var $target = $(e.target);
		var value = $target.val();
		$target.siblings('input[type=hidden]').val(value.replace(/ /g, ""));
		var formNm= $(this).parents("form");
		checkBinDetails($target.siblings('input[type=hidden]').val(),formNm);
	});

    // number spacing (doesnt work on ie, no keypress event)
    $("#cn, #cn1, #rewardsCardNo").on("keypress", function (e) {
        var $target = $(e.target);
        digit = String.fromCharCode(e.which);
        if (!/^\d+$/.test(digit)) {
            if (e.which !== 8) {
                e.preventDefault();
            }
            return;
        }
        var value = $target.val();
        var maxLen = parseInt($target.attr("maxlength"));
        var re = /(?:^|\s)(\d{4})$/;

        if (re.test(value) && value.length < maxLen - 1) {
            e.preventDefault();
            return $target.val(value + ' ' + digit);
        } else if (re.test(value + digit) && value.length < maxLen - 1) {
            e.preventDefault();
            return $target.val(value + digit + ' ');
        }

    });

    $("#cn, #cn1, #rewardsCardNo, #ccCvvBox, #dcCvvBox, #scCvvBox").on("keyup", function(e){
        var $target = $(e.target);
        if($target && $target[0].className.split(' ').indexOf("cardInput") !== -1)
            $target = $target.siblings();

        if($target && $target[0].name) {
            var label = $target.closest("form").find("label[for=" + $target[0].name + "]")
            label && removeFieldError(label);

        }
    });
    $("#dcExpYear, #ccExpYear, #dcExpMonth, #ccExpMonth").on("change", function(e){
       // e.stopPropagation();
        var $target = $(e.target);
        if($target) {
            var label = $("label."+$target[0].id);
            label && removeFieldError(label);

        }
    });

    $('#cn, #cn1').on('blur',function() {
        var card = $(this);
        var form = card.closest('form');

        var number = form.find("input[name=cardNumber]").val();

        //	 var number = card.val();
        if (!number)
            return false;

        //Check if cardnumber is LUHN valid
        toggleLuhnErr(checkLuhn(number), card.closest("form"));
    });




    // numbers only
    $("#cn, #cn1, #rewardsCardNo").on("keydown", function (e) {
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
        } else if ((/\s\d?$/).test(value) == true) {
            $target.val(value.replace(/\s\d?$/, ''));
            e.preventDefault();
        }
    });


    // check promocode bin
    if (promocodeAvailable) {

        $('#cn, #cn1').on('blur', function () {
            var card = $(this);
            var number = card.val();
            var mode = card.data("type");

            if (!number)
                return false;

            // BIN list
            var promoCardList = promocode.cardList;

            // Category list
            var promoCardTypeList = promocode.cardTypeList;
            number = number.replace(/ /g, "");
            if (number.length > 6) {
                var formNm = $(this).parents("form");
                var cardType = changeCardTypeIcon(number, formNm);
                var identifier = cardType + "-" + mode;

                var isCardValid = false;

                // check if card category-type maches cards in list
                if (promoCardTypeList.indexOf(identifier.toUpperCase()) != -1) {
                    isCardValid = true;
                }

                // check if card no maches cards in list
                if (!isCardValid && promoCardList.indexOf(number.substring(0, 6)) != -1) {
                    isCardValid = true;
                }

                if (_.indexOf(promocode.paymentModes, 'PPI') != -1) {
                    isCardValid = true;
                }


                $('.promo-code-error').addClass("hide");
                if (!isCardValid) {
                    $(this).closest('form').find(".promo-code-error").removeClass("hide").text(promocode.promoErrorMsg);
                }

                var that = this;

                // check card number via ajax
                if (isCardValid && promocode.checkPromoValidity && number.length > 12) {
                    var query = "?CARD_NO=" + number;
                    query += "&PROMO_CAMP_ID=" + promocode.promocode;
                    query += "&MID=" + promocode.mid;
                    query += "&txnMode=" + promocode.txnMode;
                    $.getJSON("checkPromoValidity" + query, function (data) {
                        if (data.error) {
                            $(that).closest('form').find(".promo-code-error").removeClass("hide").text(data.errorMsg);
                        }
                    });
                }
            }
        });
    }

    // setup cc form validations //
    // CHEQUE FORM VALIDATIONS
    $(".ChequeDD-Pay").on("keyup", validateChequeForm);

    function validateChequeForm() {
        var form = $(this).closest("form");

        var chequeNoElm = $(this);
        var chequeNo = chequeNoElm.val();
        var ischequeNoValid = (!isNaN(chequeNo) && chequeNo.length == 6) ? true : false;
        chequeNoElm.toggleClass("valid", ischequeNoValid);
        form.trigger("change-validity");
    }

    $("#neftNo").on("keyup", validateNEFTForm);

    function validateNEFTForm() {
        var form = $(this).closest("form");

        var chequeNoElm = $(this);
        var chequeNo = chequeNoElm.val();
        var ischequeNoValid = (!isNaN(chequeNo) && chequeNo.length > 3) ? true : false;
        chequeNoElm.toggleClass("valid", ischequeNoValid);
        form.trigger("change-validity");
    }


    $(".offlineSelect").on("change", validatebankNoForm);


    function validatebankNoForm() {
        var form = $(this).closest("form");

        var BNKElm = $(this);
        var BNKNo = BNKElm.val();
        var isBANK_NmValid = (BNKNo != -1) ? true : false;
        if (isBANK_NmValid) {
            $(this).addClass("valid");
        }
        else {
            $(this).removeClass("valid");
            $(this).addClass("required");
        }
        form.trigger("change-validity");
    };
    // Prefill value validation
    $(window).on("load", prefillBankname);
    function prefillBankname() {
        $(".offlineSelect").each(function () {
            var form = $(this).closest("form");

            var BNKElm = $(this);
            var BNKNo = BNKElm.val();
            var isBANK_NmValid = (BNKNo != -1) ? true : false;
            if (isBANK_NmValid) {
                $(this).addClass("valid");
                $(this).removeClass("required");
            }
            else {

                $(this).removeClass("valid");
            }
            form.trigger("change-validity");
        });
    };


    // setup imps form validations //

    $(".imps-form").on("keyup", validateIMPSForm);

    function validateIMPSForm() {
        var form = $(this);

        var mobileElm = form.find("#mobileNo");
        var mobileNo = mobileElm.val();
        var isMobileNoValid = (!isNaN(mobileNo) && mobileNo.length == 10) ? true : false;
        mobileElm.toggleClass("valid", isMobileNoValid);

        var mmidElm = form.find("#mmid");
        var mmid = mmidElm.val();
        var isMMIDValid = (!isNaN(mmid) && mmid.length == 7) ? true : false;
        mmidElm.toggleClass("valid", isMMIDValid);

        var otpElm = form.find("#otp");
        var otp = otpElm.val();
        var isOTPValid = (!isNaN(otp) && otp.length > 5) ? true : false;
        otpElm.toggleClass("valid", isOTPValid);

        form.trigger("change-validity");
    };

    // setup imps form validations //


    // setup itz form validations //

    $(".itz-form").on("keyup", validateITZForm);

    function validateITZForm() {
        var form = $(this);

        var numberElm = form.find("#itzCashNumber");
        var cardNo = numberElm.val();
        var isCardNoValid = (!isNaN(cardNo) && cardNo.length == 12) ? true : false;
        numberElm.toggleClass("valid", isCardNoValid);

        var passElm = form.find("#itzPwd");
        var password = passElm.val();
        var isPasswordValid = (!isNaN(password) && password.length > 3) ? true : false;
        passElm.toggleClass("valid", isPasswordValid);

        form.trigger("change-validity");
    };

    // setup itz form validations //


    // setup nb form validations //

    // when netbanking popular bank is selected
    $('.netbanking-panel input.bankRadio').on("change", function (e) {
        var form = $(this).parents("form");

        var selectElm = form.find(".nbSelect");
        if (selectElm.length)
            selectElm[0].selectedIndex = 0;
        var bankName = $(this).parent().attr('id');
        var displayName = $(this).parent().attr('title');
        form.find('#bankCode').val(bankName);
        form.find('#warningDiv').hide();
        form.find('#errorMsg').text("");
        form.find('#nbcardId').hide();
        //form.find("#nbSubmit").removeAttr("disabled").disabled(false);
        form.find("#nbSubmit").addClass("valid");

        // Checked to Selected Select Box
        form.find('#other-banks-wrapper .nbSelect option').each(function (index) {
            if ($(this).val() == bankName) {
                $(this).prop('selected', true);


                //IRCTC CHARGES TEXT
                if (isIrctcAvailable) {
                    var irData = $(this);
                    setIrctcCharges(irData);
                }
            }
        });


        if (maintainenceNBBank[bankName]) {
            form.find('#errorMsg').text(displayName + " " + maintenanceMsg);
            form.find('#warningDiv').show();
            //form.find('#nbSubmit').disableButton(true);
            form.find("#nbSubmit").removeClass("valid");
            //$('#nbSubmit').removeClass('blue-btn').addClass('gry-btn');
        } else if (lowPerfNBBank[bankName]) {
            form.find('#warningDiv').show();
            lowSuccessRateMsg = converterLowSuccessMsg(true, false, bankName, undefined);
            form.find('#errorMsg').text(lowSuccessRateMsg);
        }

        form.trigger("change-validity");
    });
    //$('.netbanking-panel input.bankRadio[checked]').trigger("change");

    // when netbanking other bank is selected
    $(".nbSelect").on("change", function (e) {
        var form = $(this).parents("form");


        //IRCTC CHARGES TEXT
        if (isIrctcAvailable) {
            var irData = $(e.target).find('option').eq(e.target.selectedIndex)
            setIrctcCharges(irData);
        }


        // uncheck any bank in popular list
        var elm = form.find('input.bankRadio:checked');
        if (elm && elm[0])
            elm[0].checked = false;
        // refresh
        $(".banks-panel .checkbox").checkbox("checkChecked");

        //Change val to Checked Upperbank/Select Bank
        var selectedBank;
        selectedBank = $(this).val();
        if ($(this).val() != -1) {
            $('.netbanking-panel li div').each(function () {

                checkedbank = $(this);
                banksVal = $(this).attr('id');
                if (selectedBank == banksVal) {
                    checkedbank.find("input.bankRadio").checkbox('click');

                }


            });
        }

        var bankName = $(this).val();
        var displayName = $(this).find('option:checked').text();
        //$(".netbanking-panel li div a.checked").removeClass("checked");
        form.find('#bankCode').val(bankName);
        form.find('#warningDiv').hide();
        form.find('#errorMsg').text("");
        form.find('#nbcardId').hide();
        var state = (bankName == -1) ? true : false;

        form.find("#nbSubmit").toggleClass("valid", !state);

        //form.find('#nbSubmit').disableButton(state);

        if (maintainenceNBBank[bankName]) {
            form.find('#errorMsg').text(displayName + " " + maintenanceMsg);
            //panel.find('#nbSubmit').disableButton(true);
            form.find("#nbSubmit").removeClass("valid");
            form.find('#warningDiv').show();
        } else if (lowPerfNBBank[bankName]) {
            form.find('#warningDiv').show();
            lowSuccessRateMsg = converterLowSuccessMsg(true, false, bankName, undefined);
            form.find('#errorMsg').text(lowSuccessRateMsg);
        }

        form.trigger("change-validity");
    });
    $('.nbSelect option[selected]').trigger("change");

    // setup nb form validations //

    // setup atm form validations //

    $("body").on('click', '.atm-panel li div', function () {
        var form = $(this).parents("form");
        form.find('.atm-panel li div a.checked').removeClass('checked');
        $(this).find('a').addClass("checked");
        var bankName = $(this).attr('id');
        var displayName = $(this).attr('title');
        form.find('#atmBankCode').val(bankName);
        form.find('#atmWarningDiv').hide();
        form.find('#atmErrorMsg').text("");
        form.find('#atmcardId').hide();
        //form.find('#atmSubmit').removeAttr("disabled").disabled(false);
        form.find('#atmSubmit').addClass("valid");

        if (maintainenceATMBank[bankName]) {
            form.find('#atmErrorMsg').text(displayName + " " + maintenanceMsg);
            form.find('#atmWarningDiv').show();
            //form.find('#atmSubmit').disableButton(true);
            form.find('#atmSubmit').removeClass("valid");
        } else if (lowPerfATMBank[bankName]) {
            form.find('#atmWarningDiv').show();
            form.find('#atmErrorMsg').text(lowSuccessRateMsg);
        }

        form.trigger("change-validity");
    });
    $('.atm-panel input.bankRadio[checked]').trigger("click");

    // setup atm form validations //


    // setup cod //

    // hide paytm cash for COD

//	if(currentPaymentType == 12) {
//		var paytmCashCheckbox = $("#paytm-cash-checkbox");
//		if(paytmCashCheckbox.length && paytmCashCheckbox[0].checked == true){
//			$("#paytm-cash-checkbox").checkbox("click");
//		}
//		$(".paytmcash-card").addClass("hide");
//	}

    // setup cod //


    // Setup login //

    $("body").on('touchstart click', '#login-btn', function () {
        showAuthView("login");
        return false;
    });

    $("body").on('touchstart click', '#register-btn', function () {
        showAuthView("register");
        return false;
    });

    $("body").on('touchstart click', '#otp-btn', function () {
        showAuthOTPView("otp");
        return false;
    });

    $("body").on('touchstart click', '#otp-signUp-btn', function () {
        showAuthOTPView("otp");
        return false;
    });

	function getAuthUrl(type,open){
        var authNewConfig= authNewConfig || $("#auth-js-details");
        if(open == undefined)
            open = true;

        if($('#login-modal').hasClass('md-show'))
            return false;

        var showRegisterView = false;

        if(type == "register")
            showRegisterView = true;
        //TODO: remove hardcode in future
        var hostUrl = authConfig.host || authConfig.oAuthBaseUrl;

        var params = [];

        params.push('response_type=code');
        params.push('scope=paytm');

        if(isQRCodeEnabled){
            params.push('theme=scanandpay');
            params.push('isSignup=false');
        }else{
            params.push('theme=' + (authConfig.login_theme || 'pg'));
        }

        if(isSubscription){
            params.push('subscription=true');
        }

        var client_id = authConfig.client_id;
        var wap_client_id = authConfig.wap_client_id;


        var returnUrl = authConfig.return_url;
        params.push('redirect_uri=' + returnUrl);
        if(authConfig.EMAIL)
            params.push('email-prefill=' + authConfig.EMAIL);

        params.push('ADDRESS1=' + authConfig.ADDRESS1);
        params.push('ADDRESS2=' + authConfig.ADDRESS2);

        if(authConfig.MSISDN)
            params.push('mobile-prefill=' + authConfig.MSISDN);

        var txnTransientId = $("#txnTransientId").data("value");

        var isMobile = $(window).width() < 600;

        var detectedChannel = isMobile ? "WAP" : "WEB";

        var jsessionid = $("#other-details").data("jsessionid");
        var loginType = open ? "MANUAL" : "AUTO";
        var eid = authConfig.eid;
        var jvmRoute = $("#jvmRoute").data("value");
        //TODO comment this line in future when testing done
//		params.push("state=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + txnTransientId + ":" + detectedChannel + ":" + jsessionid + ":" + loginType + ":" + eid + ":" + jvmRoute);
        params.push("state=" + authConfig.ORDERID + ":" + authNewConfig.data('mid') + ":" + detectedChannel + ":" + jsessionid);

        var view = "/oauth2/authorize";
        if(showRegisterView == true)
            view = "/register";


        // check for wap version
        if(isMobile){
            params.push('client_id=' + wap_client_id);
            params.push('device=mobile');
        } else {
            params.push('client_id=' + client_id);
        }
//		params.push('client_id=' + 'paytm-pg-client-staging');
        // check for auto register
        if(open && authConfig.auto_signup){
            params.push('auto-signup=true');
            if(authConfig.titletext)
                params.push('titletext=' + authConfig.titletext);
        }
        /*
         * check if any of params is undefined
         * if it's undefined change it to ''
         */
        for(var i=0;i<params.length;i++){
            if(params[i]==undefined)
                params[i]='';
        }

        var src = hostUrl + view + "?" + params.join('&');
        return encodeURI(src);
	}

	// show login/register popup
	// type = "login"/"register", default - "login"
	function showAuthView(type, open) {
		window.onbeforeunload = null;
		var src = getAuthUrl(type,open);
        var isMobile = $(window).width() < 600;
        if(open == undefined){
            open = true;
        }

		// redirect
		if(open){
			if(isMobile){
				setTimeout(function(){
					window.location.href = src;
				}, 100);
				return true;
			} else {
				$('#login-modal').addClass('md-show');
			}
		}


        $('#login-iframe').attr('src', src);
        $('#login-iframe').one("load", function (e) {
            $(this).addClass("loaded");
            $("#login-spinner").hide();
        });

        // fix For IE
        if ($.support) {
            if (!$.support.opacity) {
                $('.md-overlay').css({"filter": "alpha(opacity=60)"});
            }
        }

        // setCentered
        // set position to center the popup
//		var topMargin = $('#login-modal').height() / 2;
//		var leftMargin = $('#login-modal').width() / 2;
//
//		$('#login-modal').css({ "margin-top" : -topMargin, "margin-left" : -leftMargin});

        // Tracking manual/auto login
        try {
            var url = $(open ? '#pgTrackUrlManual' : '#pgTrackUrlAuto').val();

            if (url != null && url != '' && url != 'null') {
                var ele = document.createElement("img");
                ele.src = url;
            }
        } catch (e) {
        }

    }

    //LOGIN VIA OTP
    function showAuthOTPView(type, open) {
        window.onbeforeunload = null;
        var authNewConfig = authNewConfig || $("#auth-js-details");
        if (open == undefined)
            open = true;

        if ($('#login-modal').hasClass('md-show'))
            return false;


        //TODO: remove hardcode in future
        var hostUrl = authConfig.host || authConfig.oAuthBaseUrl;

        var params = [];

        params.push('response_type=code');
        params.push('scope=paytm');
        params.push('theme=pg-otp');

        if (isSubscription) {
            params.push('subscription=true');
        }

        var client_id = authConfig.client_id;
        var wap_client_id = authConfig.wap_client_id;


        var returnUrl = authConfig.return_url;
        params.push('redirectUri=' + returnUrl);


        if (authConfig.MSISDN)
            params.push('mobile-prefill=' + authConfig.MSISDN);

        var txnTransientId = $("#txnTransientId").data("value");

        var isMobile = $(window).width() < 600;

        var detectedChannel = isMobile ? "WAP" : "WEB";

        var jsessionid = $("#other-details").data("jsessionid");
        var loginType = open ? "MANUAL" : "AUTO";
        var eid = authConfig.eid;
        var jvmRoute = $("#jvmRoute").data("value");
        //TODO comment this line in future when testing done
//		params.push("state=" + authConfig.ORDERID + ":" + authConfig.MID + ":" + txnTransientId + ":" + detectedChannel + ":" + jsessionid + ":" + loginType + ":" + eid + ":" + jvmRoute);
        params.push("loginData=" + authConfig.ORDERID + ":" + authNewConfig.data('mid') + ":" + detectedChannel + ":" + txnTransientId + ":" + loginType + ":" + jsessionid);

        var view = "/oauth2/login/otp";


        // check for wap version
        if (isMobile) {
            params.push('clientId=' + wap_client_id);
            params.push('device=mobile');
        } else {
            params.push('clientId=' + client_id);
        }
//		params.push('client_id=' + 'paytm-pg-client-staging');
        // check for auto register
        if (open && authConfig.auto_signup) {
            params.push('auto-signup=true');
            if (authConfig.titletext)
                params.push('titletext=' + authConfig.titletext);
        }
        /*
         * check if any of params is undefined
         * if it's undefined change it to ''
         */
        for (var i = 0; i < params.length; i++) {
            if (params[i] == undefined)
                params[i] = '';
        }

        var src = hostUrl + view + "?" + params.join('&');
        src = encodeURI(src);

        // redirect
        if (open) {
            if (isMobile) {
                setTimeout(function () {
                    window.location.href = src;
                }, 100);
                return true;
            } else {
                $('#login-modal').addClass('md-show');
            }
        }


        $('#login-iframe').attr('src', src);
        $('#login-iframe').one("load", function (e) {
            $(this).addClass("loaded");
            $("#login-spinner").hide();
        });

        // fix For IE
        if ($.support) {
            if (!$.support.opacity) {
                $('.md-overlay').css({"filter": "alpha(opacity=60)"});
            }
        }

        // setCentered
        // set position to center the popup
//		var topMargin = $('#login-modal').height() / 2;
//		var leftMargin = $('#login-modal').width() / 2;
//
//		$('#login-modal').css({ "margin-top" : -topMargin, "margin-left" : -leftMargin});

        // Tracking manual/auto login
        try {
            var url = $(open ? '#pgTrackUrlManual' : '#pgTrackUrlAuto').val();

            if (url != null && url != '' && url != 'null') {
                var ele = document.createElement("img");
                ele.src = url;
            }
        } catch (e) {
        }

    }


    $("body").on('touchstart click', '#logout-btn', function () {
        doLogout();
        return false;
    });

    function doLogout() {

//		console.log($("#auth-config"));
        var authConfig = $("#auth-config").data("value");
        var authNewConfig = $("#auth-js-details");
        //TODO: change auto url with new url once confirmed
        var logoutUrl = authConfig.AUTH_LOGOUT_URL + "/" + authConfig.PAYTM_TOKEN;

        var params = [];
        var returnUrl = authConfig.AUTH_LOGOUT_RETURN_URL + "?" + $("#queryStringForSession").data("value");
        params.push('redirect_uri=' + returnUrl);


        var src = logoutUrl + "?" + params.join('&');
        src = encodeURI(src);

//		// redirect
//		if(open){
//			if(isMobile){
//				setTimeout(function(){
//					window.location.href = src;
//				}, 100);
//				return true;
//			} else {
//				$('#login-modal').addClass('md-show');
//			}
//		}

        $('#logout-iframe').attr('src', src);
        $('#logout-iframe').one("load", function (e) {
            // logout req done
            window.location = returnUrl;
        });

        // Tracking
//	     try{
//			var url =  $(open ? '#pgTrackUrlManual' : '#pgTrackUrlAuto').val();
//		
//			if(url!=null && url!='' && url!='null'){
//				var ele = document.createElement("img");
//				ele.src= url;		  
//			}
//		}catch(e){}

    }


    $("body").on('touchstart click', '.close-modal', function () {
        $(this).closest(".md-modal").removeClass('md-show');
        $('.md-overlay').removeClass('md-overlay-show');
        $('#login-iframe').attr('src', '');
        return false;
    });

    // Setup login //

    function getCardType(cardNumber) {
        var first = cardNumber.substring(0, 1);
        var firstTwo = cardNumber.substring(0, 2);
        var firstFour = cardNumber.substring(0, 4);
        var firstSix = cardNumber.substring(0, 6);
        var res;

        if (cardNumber.length == 19) {
            res = "maestro";
        } else if (firstTwo == "30" || firstTwo == "36" || firstTwo == "38" || firstTwo == "35") {
            res = "diners";
        } else if (firstTwo == "34" || firstTwo == "37") {
            res = "amex";
        } else if (first == "4") {
            res = "visa";
        } else if (checkRange(firstTwo, 51, 55)) {
            res = "master";
        } else if (firstFour == "6011") {
            res = "DISCOVER";
        } else if (rupayCard(firstSix)) {
            res = "rupay";
        } else if (firstTwo == "50" || checkRange(firstTwo, 58, 62)) {
            res = "maestro";
        } else if (cardNumber.length == 0) {
            res = "INVALID CARD";
        } else {
            res = "none";
        }

		return res;
	}

    function rupayCard(cardNo) {

        if (checkRange(cardNo, 508500, 508999) || checkRange(cardNo, 606985, 607984) || checkRange(cardNo, 608001, 608500) || checkRange(cardNo, 652150, 607984) || checkRange(cardNo, 608001, 608100) || checkRange(cardNo, 652150, 653149)) {
            return true;
        }
        else {
            return false;
        }
    }

    function checkRange(num, a, b) {
        return (num >= a && num <= b) ? true : false;
    }

    function bindOnceBlur() {
        $("#merchant-payment-modes .card").one("click", clickOnBlured);
        $("#paytm-cash-checkbox").one("change", clickOnBlured);
        $(".merchantOverlay").one("click", clickOnBlured);
    }

    function clickOnBlured(e) {
        e.preventDefault();
        if ($(e.target).hasClass('sc-checkbox'))
            return false;
        $("#merchant-payment-modes .card").off("click", clickOnBlured);
        $("#paytm-cash-checkbox").off("change", clickOnBlured);
        // only for click on blured area
        if (!$(e.target).hasClass('checkbox')) {
            $("#paytm-cash-checkbox").checkbox("click");
        }
    }


    function initPayments() {

        // on paytmcash checkbox change
        $("body").on('change', '#paytm-cash-checkbox', function (e) {
            useWallet = $("#paytm-cash-checkbox")[0].checked;
            processPayments();
        });


        // use wallet by default (if available)
        if (walletAvailable) {
            useWallet = true;
        } else {
            // disable paytmcash
            $("#paytm-cash-checkbox").checkbox("toggleEnabled");
        }

        // set class on disabled elems
        $('*[disabled=disabled]').disabled(true);

        processPayments();

        //checkSavedCardsCount();

        if (promocodeAvailable) {
            try {
                setupPromocode();
            } catch (e) {
                var er = e;
            }
        }

    };

    function checkSavedCardsCount() {
        // hide/show msg for other paymnt methods
        try {
            var num = $('.sc-cards .control-group.card').length;
            if (num == 0) {
                $('a[href="#sc-card"]').parent().remove();
                $('.cards-control').each(function () {
                    $(this).find(".card").eq(0).click();
                });
            }
        } catch (e) {
        }

    };


    // payments starts here...
    initPayments();


    // rebuilds ui
    function processPayments() {


        var totalAmount = txnAmount;

        var paidAmount = totalAmount;
        var remWalletBalance = 0;

        $("#totalAmountSpan, .totalAmountSpan").text(numberWithCommas(totalAmount));

        if (walletAvailable) {
            $('#yourBal').show();
            $('#yourBal .amt').text(numberWithCommas(walletBalance));

            // calculate balance used

            var comparisionAmount = totalAmount;

            if (isIrctcAvailable) {
                comparisionAmount = iraddMoney;
            }
            var balanceUsed = walletBalance >= comparisionAmount ? comparisionAmount : walletBalance;
            $("#balance-used").text(numberWithCommas(balanceUsed));
        }

        // identify mode
        mode = "";
        if (useWallet) {
            var comparisionAmount = totalAmount;

            if (isIrctcAvailable) {
                comparisionAmount = iraddMoney;
            }
            if (comparisionAmount != "" && walletBalance >= comparisionAmount) { // sufficient balance
                mode = "full-wallet"; // full wallet payment
                if (isSubscription) {
                    mode = "subs-full-wallet";
                }

            } else { // insufficient balance

                if (isSubscription) {
                    mode = "subs-default";
                } else if (addMoneyAvailable) {
                    mode = "add-money"; // wallet with add money
                } else if (hybridPaymentAllowed && walletBalance > 0) {
                    mode = "hybrid"; // hybrid payment
                } else {
                    mode = "no-wallet"; // only bank payment
                }

            }

        } else {
            mode = "default"; // default
        }

        //For retry purpose only
        var retryPaymentMode = $("#retryPaymentMode").data("value");
        if (retryPaymentMode != null && retryPaymentMode != '') {
            var cardInputField = $(".cardInput")[0];
            var retryInputFld = $("."+retryPaymentMode+"CardNumber")[0];
            if(retryInputFld && retryInputFld.value && retryInputFld.value.length >= 6) {
                checkBinDetails(retryInputFld.value.replace(/\s/g,''), $(retryInputFld).parents("form"));
            }
            $(".cards-control .card").each(function () {
                var index = $(this).find("a").attr("href").toLowerCase().indexOf(retryPaymentMode);
                if (index >= 0) {
                    $(this).addClass("active");
                    if (mode == "add-money")
                        $('#add-money-payment-modes a[href="#' + retryPaymentMode + '-card"]').click();
                    else
                        $('#merchant-payment-modes a[href="#' + retryPaymentMode + '-card"]').click();
                    return false;
                }
            });
        }
        //End

		// default mode in case of promocode
		var promoPaymentModes = promocodeAvailable && $("#promocode-details").data("paymentmodes").toString().split(',');
		if(promocodeAvailable && _.indexOf(promoPaymentModes, 'PPI') == -1){
			mode = "default";
		}


        // set mode based on current payment type (used in case of error showing)
        if (errorsAvailable) {
            errorsAvailable = false;

            // case - errors + no-wallet
            if (mode == "no-wallet") {
                disablePaytmCash();
            }

            //if(addMoneySelected){
            if (walletUsed || addMoneySelected || isSubscription) {
                // dont change mode
                openPreferedModeTab(mode);
            }
            else {
                mode = "default";
            }
        }

        // check paytmcash checkbox for valid modes
        if (_.indexOf(["full-wallet", "hybrid", "add-money", "subs-full-wallet", "subs-default"], mode) != -1) {
            var paytmCashCheckbox = $("#paytm-cash-checkbox");
            // click checkbox if its not clicked
            if (paytmCashCheckbox.length && paytmCashCheckbox[0].checked == false) {
                $("#paytm-cash-checkbox").checkbox("click");
                return false;
            }
        }

        // show emi normal map
        $(".emi-bank-map-1").removeClass("hide");
        $(".emi-bank-map-2").addClass("hide");

        // hide add money modes by default
        $("#merchant-payment-modes").removeClass("hide");
        $("#add-money-payment-modes").addClass("hide");
        // hide all msgs
        $("#payment-user-msg .msg").addClass("hide");
        // hide full wallet submit btn
        $('.fullWalletDeduct').addClass("f-hide");

        $('.fullWalletDeduct').addClass("f-hide");

        $("#cod-msg").show();
        $('#cod-hybrid-msg').hide();
        $("a[href='#cod-card']").parent().removeClass("hide");


        switch (mode) {
            case "default" :
                unblurAllModes();
                paidAmount = totalAmount;
                $('#yourBal .amt').text(numberWithCommas(walletBalance));
                $('input[name=walletAmount]').val('0');
                $('#walletBalance, #remBal').hide();
                $('#yourBal').show();
                $('#remBal').hide();
                $(".hybridMoneyAmount").text(numberWithCommas(paidAmount));
                // show heading text
                $(".bankPaymentText").removeClass("hide");
                $('.balance-used-box').addClass("hide");


                if (isIrctcAvailable) {
                    $(".ir-addMoney").hide();
                    $(".ir-Money").show();

                    if (!useWallet && ($("#merchant-payment-modes").length == 0 || $("#add-money-payment-modes").length == 0)) {
                        var irctcDataDiv = $("#irctcOnlyWallet");
                        setIrctcCharges(irctcDataDiv);
                    }
                }

                if (isSubscription) {

                    if (subsMode == "CC" || subsMode == "DC") {
                        $(".bankPaymentText").addClass("hide");
                        $(".subs-default-msg").removeClass("hide");
                    }

                    if (paidAmount == 0) {
                        if (subsMode == "CC")
                            $("#cc-card").find(".storeCardWrapper").append($(".subs-min-amt-msg").eq(0).clone().removeClass("hide"));
                        else
                            $("#dc-card").find(".storeCardWrapper").append($(".subs-min-amt-msg").eq(0).clone().removeClass("hide"));
                        // hide cvv box & make submit btn valid
                        $('.scCvvInput').parent('div').hide();
                        $('input[id=scSubmit]').addClass("valid");
                    }
                }

                if ($("#paytm-cash-checkbox").attr("disabled") == "disabled") {
                    $(".no-walletTextUpdate").removeClass("hide");
                }
                else {
                    $(".no-walletTextUpdate").addClass("hide");
                }

                if(!$("#merchant-payment-modes").hasClass("hide")){
                    $("#merchant-payment-modes .cards-control .active").click();
                }

                if(!$("#add-money-payment-modes").hasClass("hide")){
                    $("#add-money-payment-modes .cards-control .active").click();
                }


                break;

            case "no-wallet" :
                // show heading text
                $(".bankPaymentText").removeClass("hide");
                disablePaytmCash();
                if (isIrctcAvailable) {
                    $(".ir-addMoney").hide();
                    $(".ir-Money").show();
                    $(".cards-control .active").click();
                }


                break;

            case "full-wallet" :
                blurAllModes();
                bindOnceBlur();

                paidAmount = totalAmount;
                remWalletBalance = walletBalance - totalAmount;
                $('input[name=walletAmount]').val(totalAmount);
                $('.fullWalletDeduct').removeClass("f-hide");
                $('.balance-used-box, .fullWalletText').removeClass("hide");

                if (isIrctcAvailable) {
                    $('input[name=walletAmount]').val(iraddMoney);
                    $("#balance-used").text(iraddMoney);

                    $(".ir-addMoney").show();
                    $(".ir-Money").hide();

                }

                break;

            case "hybrid" :
                paidAmount = totalAmount - walletBalance;
                paidAmount = Math.round(paidAmount * 100) / 100;
                $('input[name=walletAmount]').val(walletBalance);

                // show heading text
                $('.hybridPaymentText').removeClass("hide");
                $('.balance-used-box').removeClass("hide");
                $(".hybridMoneyAmount").text(numberWithCommas(paidAmount));

                // show emi hybrid map
                $(".emi-bank-map-1, .emi-bank-map-2").toggleClass("hide");
                if ($(".emi-bank-map:not(.hide)").find(".emiBankSelect").length == 0) {
                    $('.card-details-wrapper').addClass('hide');
                }

                $("#cod-msg, #cod-hybrid-msg").toggle();
                $("#cod-amount").text(paidAmount);

                // open already selected bank (in error case)

                if($(".emi-bank-map:not(.hide)").find(".emiBankSelect.selected").val()!="BAJAJFN" && $(".emi-bank-map:not(.hide)").find(".emiBankSelect.selected option").length < 3){
                      $(".emi-bank-map:not(.hide)").find(".emiBankSelect.selected").change();
                }
                if (codHybridAllowed != true ) {
                    $("a[href='#cod-card']").parent().addClass("hide");
                    $("#cod-card").removeClass("active");
                    if ($("a[href='#cod-card']").parent().hasClass("active")) {
                        $(".cards-control .card:not(.hide) a").eq(0).click();
                    }

                }

                if (offlinePaymode) {
                    if ($("a[href='#cheque-dd-neft']").parent().hasClass("active")) {
                        $("#paytm-cash-checkbox").checkbox("click");
                        disablePaytmCash();
                    }

                }

                if (isIrctcAvailable) {
                    $(".ir-addMoney").hide();
                    $(".ir-Money").show();

                    //if(!useWallet && ($("#merchant-payment-modes").length==0 || $("#add-money-payment-modes").length==0)){
                    var irctcDataDiv = $("#irctcHybridWallet");
                    setIrctcCharges(irctcDataDiv);
                    //}
                }

                break;

            case "add-money" :
                paidAmount = totalAmount - walletBalance;
                paidAmount = Math.round(paidAmount * 100) / 100;
                $("#merchant-payment-modes").addClass("hide");
                $("#add-money-payment-modes").removeClass("hide");

                $('input[name=walletAmount]').val(walletBalance);

                $(".addMoneyAmount").text(numberWithCommas(paidAmount));
                if (iraddMoney) {
                    paidAmount = iraddMoney - walletBalance;
                    $(".addMoneyAmount").text(paidAmount.toFixed(2));

                    $(".ir-addMoney").show();
                    $(".ir-Money").hide();
                }


                $('.balance-used-box').removeClass("hide");

                // show heading text
                $(".addMoneyText").removeClass("hide");
                openPreferedModeTab(mode);
                break;

            case "subs-default" :
                //unblurAllModes();
                paidAmount = totalAmount - walletBalance;
                paidAmount = Math.round(paidAmount * 100) / 100;
                $("#merchant-payment-modes").addClass("hide");
                $("#add-money-payment-modes").removeClass("hide");

                $('input[name=walletAmount]').val(walletBalance);

                $('#yourBal .amt').text(numberWithCommas(walletBalance));

                if (isSubsPPIOnly) {
                    $(".subs-ppi-only-complete-payment").removeClass("hide");
                } else {
                    $(".subs-complete-payment").removeClass("hide");
                }

                $(".addMoneyAmount").text(numberWithCommas(paidAmount));

                $('.balance-used-box').removeClass("hide");
                $('#walletBalance, #remBal').hide();
                $('.bankPaymentText, #yourBal').show();
                $(".fullWalletDeduct").addClass("f-hide");

                $("#payment-user-msg .msg").addClass("hide");

                // enable save card checkbox
                if (isSubsPPIOnly) {
                    $('input[name=storeCardFlag][type=checkbox]').checkbox("toggleEnabled");
                }

                openPreferedModeTab("subs-default");
                break;

            case "subs-full-wallet" :
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

                // dont show wallet in case of zero balance
                if (paidAmount == 0)
                    $(".wallet-pay-text, .paytmcash-card").addClass("hide");

                // if saved cards are available
                if ($("input.sc-checkbox").length) {
                    $(".subs-select-card").removeClass("hide");
                    $(".storeCardWrapper").append($(".subs-min-amt-msg").eq(0).clone().removeClass("hide"));
                } else {
                    $(".subs-save-card").removeClass("hide");
                }

                // hide cvv box and enable save card submit
                $('.scCvvInput').parent('div').hide();

                // Set if subscription full wallet
                isSubscriptionFullWallet = true;

                // enable sc submit btn
                $('input[id=scSubmit]').addClass("valid");

                if (isSubsPPIOnly) {
                    $('.fullWalletDeduct').removeClass("f-hide");
                    $(".paytmcash-card").removeClass("hide");
                    $('.subs-text, .wallet-pay-text, .subs-msgs .hr').addClass('hide');
                    $('.subs-only-full-wallet').removeClass('hide');
                }
                break;
        }

        $('.finalAmountSpan').html(numberWithCommas(paidAmount));
        // limeroad Subtheme Amount Button
        if (limeroad) {
            $("form .btn-submit input").val("Pay Rs. " + numberWithCommas(paidAmount) + "/-");
        }


    }


    function openPreferedModeTab(mode) {
        var showTab = null;

        switch (currentPaymentType) {
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
            case 11 : // rewards
                showTab = "rewards";
                break;
            case 13 : // emi
                showTab = "emi";
                break;
            default:
                openFirstPaymentMode(mode);
                break;
        }

        if (showTab) {

            if (mode == "add-money" || mode == "subs-default" || mode == "subs-full-wallet")
                $('#add-money-payment-modes a[href="#' + showTab + '-card"]').click();
            else
                $('#merchant-payment-modes a[href="#' + showTab + '-card"]').click();
        }

    };

    // open first tab
    function openFirstPaymentMode(mode) {
        if (mode == "add-money")
            $('#add-money-payment-modes .cards-control .card:not(.hide) a').eq(0).click();
        else
            $('#merchant-payment-modes .cards-control .card:not(.hide) a').eq(0).click();
    }

    function checkPaytmCashCheckbox(state) {
        state = state || false;

        var paytmCashCheckbox = $("#paytm-cash-checkbox");
        if (paytmCashCheckbox.length && paytmCashCheckbox[0].checked != state) {
            $("#paytm-cash-checkbox").checkbox("click");
        }
    }

    function disablePaytmCash() {
        $('.fullWalletDeduct').addClass("f-hide");
        $('#remBal').hide();
        $(".paytmcash-card .blur-overlay").addClass("show");
        $('.balance-used-box').addClass("hide");
        $(".no-walletTextUpdate").removeClass("hide");
        $(".bankPaymentText").hide();
    }

    function enablePaytmCash() {
        $('.fullWalletDeduct').removeClass("f-hide");
        $('.balance-used-box').removeClass("hide");
        $(".paytmcash-card .blur-overlay").removeClass("show");
        $(".no-walletTextUpdate").addClass("hide");
        $(".bankPaymentText").show();
    }


    function blurAllModes() {

        toggleMerchantModes(false);
    }

    function unblurAllModes() {

        toggleMerchantModes(true);

    }

    function toggleMerchantModes(show) {
        var merchant = $("#merchant-payment-modes");
        var control = merchant.find(".cards-control");
        var overlay = $("#merchant-payment-modes .card");


        if (show == false) {
            overlay.addClass("blured");
            merchant.find(".merchantOverlay").show();
        } else {

            $(".scCvvInput").focus();
            overlay.removeClass("blured");
            merchant.find(".merchantOverlay").hide();
            // activate first mode
            if (merchant.find(".cards-control .card.active").length == 0)
                merchant.find(".cards-control .card").first().click();
        }

    }

    // setup payment details btn //
    $(".btn-show-payment-details").click(function () {
        $('.payment-details').toggle();
        $(this).hide();
        $(".btn-hide-payment-details").show();
        return false;
    });

    $(".btn-hide-payment-details").click(function () {
        $('.payment-details').toggle();
        $(this).hide();
        $(".btn-show-payment-details").show();
        return false;
    });
    // setup payment details btn //


    // setup notification popover
    $("#login-btn, #register-btn, .popover-close a").click(function () {
        $(".login-popover").hide();
    });


    // setup login check //
    if ($("#login-btn").length || $("#otp-btn").length) {
        //TODO: change after taken
        if (authConfig.autoLogin != "N") {
            var open = authConfig.autoLogin == "M" ? true : false;
            //check for retry count -- if retry count>=1 don't submit login request
            if (parseInt(authNewConfig.data('retrycount'))<=0 && isAutoLogin) {

                if (isOTPLoginAvailable) {
                    showAuthOTPView("otp", open);

                } else {
                    showAuthView("login", open);
                }
            }
            else {
                $("#login-stitch, #login-wait").toggle();
            }
        }
    }

    $("#login-iframe").one("load", function () {
        setTimeout(function () {
            // fix for mozilla for load event on empty iframe
            if (!isOTPLoginAvailable && $("#login-btn").length == 0)
                return false;
            if (isOTPLoginAvailable && $("#otp-btn").length == 0)
                return false;

            $("#login-stitch, #login-wait").toggle();
            if (!isLoggedIn && isWalletOnly && isAutoLogin)
                if (isOTPLoginAvailable) {
                    showAuthOTPView();

                } else {
                    showAuthView();
                }
        }, 1000);
    });

    // hide login notification if preset
    setTimeout(function () {
        var elm = $("#login-success-alert").addClass("closed");
        setTimeout(function () {
            elm.hide();
            $('.summary-card').addClass("mb20");
        }, 500);
    }, 3000);


    // setup login check //


    // setup wallet and wallet only //


    if (isWalletOnly) {
        // auto open login popup
        //$("#login-btn").click();

        // cancel txn on close popup
        $("body").one('touchstart click', '.closePop', function (e) {
            var target = $("#login-iframe").hasClass("loaded") ? "login" : "pre-login";
            cancelTxn(e, {target: target});
            return false;
        });


        // adjust footer
//		if($(window).width() > 600 && $(document).height() < $(window).height()){
//			$("#footer").addClass("fixed-bottom");
//			$("#footer-placeholder").removeClass("hide");
//		}

        // disable paytmcash checkbox
        var elm = $("#paytm-cash-checkbox");
        if (!elm.hasClass("disabled"))
            elm.checkbox("toggleEnabled");
    }

    // setup wallet and wallet only //

    // setup wap theme //

    if ($(window).width() < 600) {
        $('#login-modal').hide();

        // change type of cvv field
        $("input.type-tel").attr('type', 'tel');
    }

    // setup wap theme //

    // setup iframe flow
    document.cookie = "testcookie=true";

    if (!/testcookie/.test(document.cookie)) {

        var JSESSIONID = $("#other-details").data("jsessionid");
        var action = $("form").attr("action");
        action = action.split("?");
        var submitUrl = action[0] + ";jsessionid=" + JSESSIONID;
        if (action.length > 1) {
            submitUrl = submitUrl + "?" + action[1];
        }
        $("form").attr("action", submitUrl);

        var href = $("a.cancel").attr("href");
        href = href.split("?");
        var cancelUrl = href[0] + ";jsessionid=" + JSESSIONID;
        if (href.length > 1) {
            cancelUrl = cancelUrl + "?" + href[1];
        }
        $("a.cancel").attr("href", cancelUrl);
    }

    // cancel button
    $("a.cancel").click(cancelTxn);

    function cancelTxn(e, data) {
        var that = this;
        if (data && data.fake)
            return;

        var url = "/theia/cancelTransaction?" + $("#queryStringForSession").data("value");
        if (data && data.target)
            url += "&target=" + data.target;

        $.get(url, function (html, status) {
            if (status == "success") {
                var regex = /<FORM[\s\S]*<\/FORM>/;
                var formStr = regex.exec(html);
                if (!formStr)
                    return $(that).trigger("click", {fake: true}); // no fallback when func directly called
                $("body").append(formStr[0]);
                var form = $("form[name=TESTFORM]")[0];
                if (window.top !== window) {
                    form.target = "_parent";
                }
                form.submit();
            }
        });

        e.preventDefault();
    }


    // track page load
    /*try {
     trackEvent("onload");
     } catch(e){};*/

    function trackEvent(e) {
        var txnTransientId = $("#txnTransientId").data("value");

        if (e == "onload") {
            /*$.post("/oltp/HANDLER_INTERNAL/UPDATE_OPEN?JsonData={TXNID:" + txnTransientId+ "}", null, function(res){
             var status = res;
             });*/
        }
    }

    // disable save card checkbox (used for some promocodes)
    if (saveCardMandatory) {
        $('input[name=storeCardFlag][type=checkbox]').not('[disabled]').checkbox("toggleEnabled");
    }

    // setup subscription //
    if (isSubscription) {

        // disable save card checkbox
        $('input[name=storeCardFlag][type=checkbox]').checkbox("toggleEnabled");

        $(".paytmcash-card").removeClass("active");

        // Disabled and show wallet
        $(".paytmcash-card .green-tick").addClass("disabled");
        $(".paytmcash-card .green-tick").removeClass("hide");
        var walletEle = document.getElementById("paytm-cash-checkbox");
        if(walletEle){
            walletEle.disabled = true;
        }

    }
    // setup subscription //
//Fix Safari Bug (Login View Popup)
    var N = navigator.appName;
    var UA = navigator.userAgent;
    var temp;
    var browserVersion = UA.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
    if (browserVersion && (temp = UA.match(/version\/([\.\d]+)/i)) != null)
        browserVersion[2] = temp[1];
    browserVersion = [browserVersion[1], browserVersion[2].substring(0, 2)];
    if (browserVersion[0] == 'Safari') {
        if (browserVersion[1] < 6) {
            $("#login-modal,#delete-confirm-modal").removeClass("md-effect-10");
            $(".md-overlay").css("-webkit-transition", "none");
        }
    }


    // IF ALL TAB IS NOT ACTIVE
    if (!$(".grid .card:not(.hide)").hasClass("active")) {
        $(".cards-tabs:not(.hide) .grid .card:not(.hide)").first().addClass("active");
        var form_Id = $(".cards-tabs:not(.hide) .grid .card:not(.hide)").first().find("a").attr("href");
        $(".cards-tabs:not(.hide) .cards-content").find(form_Id).addClass("active");

    }

    //irctc Set Payment Amounts
    function setIrctcCharges(irData) {

        var irctcSection = $("#irctc-charges");

        var bm = irData.data("bm");
        var ct = irData.data("ct");
        var tm = irData.data("tm");
        var cf = irData.data("cf");

        if (!((bm && tm && ct)) && DEFAULTFEE_bm && DEFAULTFEE_cf && DEFAULTFEE_tm) {
            bm = DEFAULTFEE_bm;
            cf = DEFAULTFEE_cf;
            tm = DEFAULTFEE_tm;
            ct = DEFAULTFEE_ct;
        }



        irctcSection.find("#baseAmt").text(numberWithCommas(bm));
        irctcSection.find("#chargeFeeAmt").text(cf);
        irctcSection.find("#totaltxnAmt").text(numberWithCommas(tm));
        irctcSection.find("#chargeTxt").text(ct);

        // if Add Money Wallet Amount


        var addMoneyDetails = $('#addMoney-details');
        if(addMoneyDetails && $("#paytm-cash-checkbox").length >0){
            var PPIFEE=$("#irctcOnlyWallet");
             var addMoneyAvailable = addMoneyDetails.data("available");

            if(addMoneyAvailable && $("#paytm-cash-checkbox")[0].checked && PPIFEE.length > 0){

                var bm = PPIFEE.data("bm");
                var ct = PPIFEE.data("ct");
                var tm = PPIFEE.data("tm");
                var cf = PPIFEE.data("cf");
            }
        }


        if (irctcSection.find('.mb10.conv-charges')) {
            if (cf == 0 || cf == "0") {
                irctcSection.find('.mb10.conv-charges').hide();
            } else {
                irctcSection.find('.mb10.conv-charges').show();
            }
        }






    }

    //for contents cards active more than 1
    (function DisabledMoreThanOneActive() {
        if ($('.card.content.active').length > 1) {
            $('.card.content.active').removeClass('active');
            openFirstPaymentMode(mode);
        }
    })();

    // Scan pay flow - start

    function setScanPayIframeUrl(){
        var iframe = $("#scanPayAuthLogin");
        if(iframe && iframe.length > 0){
            var url = getAuthUrl();
            url = url.replace("isSignup=true","isSignup=false");
            iframe[0].src = url;
            iframe[0].onload = function () {
                var iFrameLoadingImg = document.getElementById("scanPayAuthIframeLoadingId");
                if(iFrameLoadingImg){
                    //iFrameLoadingImg.classList.add("hide");
                    iFrameLoadingImg.className += " hide";
                }
            }
        }
    }

    setScanPayIframeUrl();

    function webSocketInit(){
        var orderDetails = $("#auth-js-details"),
            room,
            retryCount = 15,
            createOrderTimeout,createOrderTimeoutValue=360000,userInActiveTimeout,sessionTimeout;

        if(orderDetails){
            room = orderDetails.data("mid") + "_" + orderDetails.data("orderid");
        }

        var socket = io.connect(scanNpayURL,{path:"/scanpay/socket.io/"});

        // check for NAN value
        if(scanPayTimeout === scanPayTimeout){
            // call when session timeout occur
            userInActiveTimeout = setTimeout(function(){
                if(socket){
                    socket.disconnect();
                    window.location = "/theia/session-timeout";
                }
            },scanPayTimeout);
        }

        socket.on('ConnectionEstablished', function(data) {
            retryCount = 15;
            socket.on("STATUS_RECEIVED",function(response){
                var form = getTransactionStatusForm(response.data);
                document.body.appendChild(form);
                form.submit();
            });
            socket.emit("JOIN_ROOM",[room]);
        });

        socket.on("SESSION_TIMEOUT_EVENT",function (response) {
            sessionTimeout = setTimeout(function(){
                var form = getTransactionStatusForm(response.data);
                document.body.appendChild(form);
                form.submit();
            },response.data.qrTimeout);

            if(createOrderTimeout){
                clearTimeout(createOrderTimeout);
            }
        });

        socket.on("CREATE_ORDER",function(response){
            createOrderTimeout = setTimeout(function () {
                var url = "/theia/abandonTransaction?",
                    authDetail = $("#auth-js-details");

                if(authDetail){
                    url +=  "MID=" + authDetail.data("mid") + "&ORDER_ID=" + authDetail.data("orderid");
                }
                // redirect to abandon transaction
                window.location = url;
            },createOrderTimeoutValue);
        });

        socket.on('connect_error',function(){
            console.log("connection failed");
            if(retryCount === 0){
                socket.disconnect();

                // Clear timeout when socket connection failed
                if(userInActiveTimeout){
                    clearTimeout(userInActiveTimeout);
                }

                if(createOrderTimeout){
                    clearTimeout(createOrderTimeout);
                }

                if(sessionTimeout){
                    clearTimeout(sessionTimeout);
                }
                return;
            }
            --retryCount
        });
    }

    function attachScanPayEvents(){
        var learnMoreTxt = $(".pl5");
        if(learnMoreTxt && learnMoreTxt[0]){
            learnMoreTxt[0].addEventListener("mouseover",function(){
                // mouse hover in
                $(".learnmore-image").removeClass("hide");
            });
            learnMoreTxt[0].addEventListener("mouseout",function(){
                // mouse hover out
                $(".learnmore-image").addClass("hide");
            });
        }
    }

    function getTransactionStatusForm(output){
        // var output = response.data;
        var form = document.createElement("form");
        var merchantId = document.createElement("input");
        var acquirementId = document.createElement("input");
        var cashierRequestId = document.createElement("input");
        var paymentMode = document.createElement("input");
        var destination = document.createElement("input");
        form.method = "POST";
        form.action = transactionStatusEndpoint;
        merchantId.name="merchantId";
        merchantId.value=$("#auth-js-details").data("mid");
        form.appendChild(merchantId);
        acquirementId.name="transId";
        acquirementId.value=output.acquirementId;
        form.appendChild(acquirementId);
        cashierRequestId.name="cashierRequestId";
        cashierRequestId.value=output.cashierRequestId;
        form.appendChild(cashierRequestId);
        paymentMode.name="paymentMode";
        paymentMode.value=output.paymentMode;
        form.appendChild(paymentMode);
        destination.name = "responseDestination";
        destination.value = "WEB";
        form.appendChild(destination);
        return form;
    }

    if(!isLoggedIn && isQRCodeEnabled){
        // connect websocket if user is not login
        webSocketInit();
        attachScanPayEvents();
    }

    // Scan pay flow - end


}); // $ ready ends


// handle optional data/success arguments
function parseArguments(url, data, success, error, dataType) {
    if ($.isFunction(data)) dataType = success, success = data, data = undefined
    if (!$.isFunction(success)) dataType = success, success = undefined
    if (!$.isFunction(error)) dataType = error, error = undefined
    return {
        url: url,
        data: data,
        success: success,
        error:error,
        dataType: dataType
    }
}

// Check if $ loaded and add _post method
if($){

    $._post = function(/* url, data, success, dataType */){
        var options = parseArguments.apply(null, arguments)
        options.type = 'POST'
        return $.ajax(options)
    }
}


function showMessage(selector) {
    $("#" + selector).toggle();
    $(".webDisplayName").toggleClass('blue-text');
    //.userImgNewBlue
    $(".userImgNew").toggleClass('userImgNewBlue');
}


// Bajaj Finserv related changes
    function bajajFinservChanges(){
        var options = this.selectedOptions,
            selectedValue = "",
            isHybridAllowed = false,
            paymentDetails = document.getElementById("payment-details"),
            paytmWallet = document.getElementById("paytm-cash-checkbox"),
            emiForm = $(".emi-form"),
            cvvInp = undefined,
            cvvBlock = document.querySelector(".card-details-wrapper .cvv-block"),
            cvvWrapper = document.querySelector(".card-details-wrapper .clue-box"),
            BAJAJ_FIN_SERV = "BAJAJFN",
            NONE = "none",
            DEFAULT_CVV_VALUE = "1111";

        if(emiForm && emiForm[0]){
            cvvInp = emiForm[0].querySelector('#ccCvvBox');
        }

        if(paymentDetails){
            isHybridAllowed = paymentDetails.getAttribute("data-hybrid-allowed") === "true";
        }

        if(options && options.length > 0){
            selectedValue = options[0]["value"] || "";

            if(cvvInp){
                // set default value for cvv in bajaj finserv
                if(selectedValue === BAJAJ_FIN_SERV){
                    cvvInp.value = DEFAULT_CVV_VALUE;
                    cvvInp.disabled = true;
                }else{
                    cvvInp.value = "";
                    cvvInp.disabled = false;
                }
            }

            if(cvvBlock){
                // hide cvv box for bajaj finserv
                cvvBlock.style.display = selectedValue === BAJAJ_FIN_SERV ? "none" : "block";
            }

            if(cvvWrapper){
                // hide cvv clue box for bajaj finserv
                cvvWrapper.style.display = selectedValue === BAJAJ_FIN_SERV ? "none" : "block";
            }
            if(isHybridAllowed && paytmWallet && paytmWallet.click){
                if((selectedValue === BAJAJ_FIN_SERV && paytmWallet.checked) ||
                    (selectedValue !== BAJAJ_FIN_SERV && selectedValue !== NONE && !paytmWallet.checked)){
                    // Click only on two conditions
                    // 1. When user select bajaj finserv and wallet is checked then uncheck wallet
                    // 2. When user select any other bank and wallet is not checked then checked wallet
                    paytmWallet.click();
                }
            }
        }
	}


