var MyTsLibrary=function(e){function n(r){if(t[r])return t[r].exports;var o=t[r]={exports:{},id:r,loaded:!1};return e[r].call(o.exports,o,o.exports,n),o.loaded=!0,o.exports}var t={};return n.m=e,n.c=t,n.p="",n(0)}([function(e,n,t){"use strict";function r(e,n){if(!(e instanceof n))throw new TypeError("Cannot call a class as a function")}var o=function(){function e(e,n){for(var t=0;t<n.length;t++){var r=n[t];r.enumerable=r.enumerable||!1,r.configurable=!0,"value"in r&&(r.writable=!0),Object.defineProperty(e,r.key,r)}}return function(n,t,r){return t&&e(n.prototype,t),r&&e(n,r),n}}(),u=t(1),i=function(){function e(){r(this,e),this.events=[]}return o(e,[{key:"pushToServer",value:function(e){}},{key:"pushEvent",value:function(e){this.events.push(e)}},{key:"executeAll",value:function(){for(var e=0;e<this.events.length;e++)console.log("i,this.events",e,this.events),this.events[e].execute(),console.log("event executed",this.events[e])}}]),e}();!function(){var e=new i;e.pushEvent(new u["default"]),e.executeAll()}()},function(e,n){"use strict";function t(e,n){if(!(e instanceof n))throw new TypeError("Cannot call a class as a function")}var r=function(){function e(e,n){for(var t=0;t<n.length;t++){var r=n[t];r.enumerable=r.enumerable||!1,r.configurable=!0,"value"in r&&(r.writable=!0),Object.defineProperty(e,r.key,r)}}return function(n,t,r){return t&&e(n.prototype,t),r&&e(n,r),n}}(),o=function(){function e(){t(this,e)}return r(e,[{key:"execute",value:function(){console.log("execute from test event")}}]),e}();n["default"]=o}]);
//# sourceMappingURL=index.js.map