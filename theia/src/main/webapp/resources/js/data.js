function createImage(txnId, fieldName, pageType) {
  var now = (new Date()).getTime();
  var imagePath = "images/white.gif?txnId=" + txnId + "&page=" + pageType + "&field=" + fieldName + "&cb=" + now;
  $("<img>", {src : imagePath, style : "display:none !important; width:0px; height:0px;"}).appendTo($("body"));
}

// DOM Load
$(function() {
  // Page onDomReady
  window.createImage(window.txnId, "START", window.pageType);

  // Track events
  $("#ccNumber, #ccExpiryMonth, #ccExpiryYear, [type='password'], #checkli, .data-test").one("mousedown", function(event) {
    try {
      var fieldName = $(this).attr("id") || "CC_" + ($(this).find("input").attr("value") || "cvv") || "cvv";  // "value" is used for saved CC, cvv field doesn't has an ID
      window.createImage(window.txnId, fieldName, window.pageType);
    } catch(e) {}
  });

  // Track how many digits were entered in CC number field
  $("#ccNumber").focusout(function() {
    var cc = $("#ccNumber").val();
    var fieldName = "ccNumberLength_" + cc.length || 0;
    window.createImage(window.txnId, fieldName, window.pageType);
  });

});

// Page Load
$(window).load(function() {
  window.createImage(window.txnId, "END", window.pageType);
  try {
	  var now = new Date().getTime();
	  var timeTaken = eval(performance.timing.responseEnd -performance.timing.requestStart);
	  window.callAjax(timeTaken);
  } catch (e) {
  }
});

function callAjax(time) {
	try {
		$.ajax({
		  type: 'POST',
		  url: 'RecordTxnTime.action',
		  data: {totalTime: time},
		  success:function(data){
		  },
		  error:function(){
		  }
		});
	} catch (e) {
	}
}