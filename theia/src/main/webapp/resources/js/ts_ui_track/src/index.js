"use strict";
const LoadState_1 = require('./states/LoadState');
const EventObject_1 = require('./interface/EventObject');
const Logger_1 = require('./utility/Logger');
const SendDataToServer_1 = require('./ajax/SendDataToServer');
const ClosureEvent_1 = require('./events/closure/ClosureEvent');
const ClickMerchant4_1 = require('./events/click/ClickMerchant4');
class PAnalytics {
    constructor() {
        this.events = [];
        this.eventObject = [];
        if (window.PG_ANALYTICS && window.PG_ANALYTICS.logToConsole === 'true') {
            Logger_1.default.logToConsole = true;
        }
        else {
            Logger_1.default.logToConsole = false;
        }
        if (window.PG_ANALYTICS && window.PG_ANALYTICS.logToServer === 'true') {
            SendDataToServer_1.default.logToServer = true;
        }
        else {
            SendDataToServer_1.default.logToServer = false;
        }
    }
    pushEvent(event) {
        this.events.push(event);
    }
    executeAll() {
        Logger_1.default.log('window pg analytic:', window.PG_ANALYTICS);
        for (let i = 0; i < this.events.length; i++) {
            this.eventObject[i] = new EventObject_1.default();
            Logger_1.default.log('i,this.events', i, this.events);
            let result = this.events[i].execute();
            if (result && result.eventName) {
                this.eventObject[i].eventName = result.eventName;
                this.eventObject[i].description = result.description;
                this.eventObject[i].time = new Date().toString();
            }
        }
        let finalObject = {
            'mid': window.PG_ANALYTICS.mId,
            'orderId': window.PG_ANALYTICS.orderId,
            'events': this.eventObject
        };
        Logger_1.default.log('final object', finalObject, JSON.stringify(finalObject));
        SendDataToServer_1.default.send(finalObject);
    }
}
(function () {
    let pAnalytics = new PAnalytics();
    try {
        document.onreadystatechange = function () {
            if (document.readyState === 'complete') {
                pAnalytics.pushEvent(new LoadState_1.default());
                pAnalytics.pushEvent(new ClosureEvent_1.default());
                pAnalytics.pushEvent(new ClickMerchant4_1.default());
                pAnalytics.executeAll();
            }
        };
    }
    catch (Exception) {
        SendDataToServer_1.default.sendInPlainText(Exception);
    }
}());
