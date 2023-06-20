/**
*@author manu.pandit
*@description: testing for cookie with guid 
*/

  // https://tc39.github.io/ecma262/#sec-array.prototype.find
  (function() {
    if (!Array.prototype.find) {
      Object.defineProperty(Array.prototype, 'find', {
        value: function(predicate) {
        // 1. Let O be ? ToObject(this value).
          if (this == null) {
            throw new TypeError('"this" is null or not defined');
          }

          var o = Object(this);

          // 2. Let len be ? ToLength(? Get(O, "length")).
          var len = o.length >>> 0;

          // 3. If IsCallable(predicate) is false, throw a TypeError exception.
          if (typeof predicate !== 'function') {
            throw new TypeError('predicate must be a function');
          }

          // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
          var thisArg = arguments[1];

          // 5. Let k be 0.
          var k = 0;

          // 6. Repeat, while k < len
          while (k < len) {
            // a. Let Pk be ! ToString(k).
            // b. Let kValue be ? Get(O, Pk).
            // c. Let testResult be ToBoolean(? Call(predicate, T, « kValue, k, O »)).
            // d. If testResult is true, return kValue.
            var kValue = o[k];
            if (predicate.call(thisArg, kValue, k, o)) {
              return kValue;
            }
            // e. Increase k by 1.
            k++;
          }

          // 7. Return undefined.
          return undefined;
        },
        configurable: true,
        writable: true
      });
    }
  })();


  //TODO:Change the values in following lines for service hit
  var serviceParameters={
    url:'',
    params:{
      'sessionId':'',
      'txnId':''
    }
  };
  //Helper methods
  var getCookieForKey= function(cookieName) {
    //key all strings in an array paytmcookies
   var i, key, value, paytmCookies = document.cookie.split(";");
   for (i = 0; i < paytmCookies.length; i++) {
       //key
       key = paytmCookies[i].substr(0, paytmCookies[i].indexOf("="));
       //value
       value = paytmCookies[i].substr(paytmCookies[i].indexOf("=") + 1);
       key = key.replace(/^\s+|\s+$/g, "");
       if (key== cookieName) {
           return unescape(value);
       }
   }
   return false;
 }

 var setCookieForKey = function(cookieName,value){
   var cookieValue = escape(value);
   document.cookie = cookieName + "=" + cookieValue;
 }

 function createGuid() {
   function s4() {
      return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(0);
   }
   return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
 }

  /*
  * 1. Check if key already exists in Cookie
  *  1.a. Create cookie if doesn't
      1.a.i   Set value in the cookie
      1.a.ii  Set exiration of cookie
  *  1.b. if it does read the value from cookie
  * 2. Send the value to server
  */

  //Cookie related helper methods
   var COOKIE_KEY='PAYTM_PG';
   if(!getCookieForKey(COOKIE_KEY)){
     setCookieForKey(COOKIE_KEY,createGuid());
   }
   else{
    // console.log('Cookie value stored:',getCookieForKey(COOKIE_KEY));
   }
   
   
	 // If the user is logged in, set to user_id; else, set to '' 
	//var MERCHANT_USER_ID = '${selected_merchant_user_id}'; 
	var EBUCKLER_TOKEN = getCookieForKey(COOKIE_KEY); 
	// Set api key which is get from alipay 
	var EBUCKLER_API_KEY = 'paytm'; 
	// Set client's app name 
	var CLIENT_APP_NAME = 'paytm'; 
	
	(function(){ 
	     var f = document.createElement('script'); 
	     f.type = 'text/javascript'; 
	     f.async = true; 
	     var nameReplace='logger-cookie.js';
	     var srcEntry=Array.prototype.slice.call(document.getElementsByTagName('script')).find(function(element){
	       element=''+element.src;   
	       if(element.indexOf(nameReplace)!=-1){
	         return element;
	       }
	      }).src.replace(nameReplace,'entry.js');
	     f.src = srcEntry; 
	     var s = document.getElementsByTagName('script')[0]; 
	     s.parentNode.insertBefore(f,s); 
	})(); 


