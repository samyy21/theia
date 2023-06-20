"use strict";
const EventObject_1 = require('../../interface/EventObject');
const SendDataToServer_1 = require('../../ajax/SendDataToServer');
class ClosureEvent {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Closure_check';
    }
    execute() {
        window.onbeforeunload = function (event) {
            let eventResult = new EventObject_1.default();
            eventResult.eventName = 'Closure check';
            let message = 'Are you sure you want to cancel the payment?';
            if (typeof event === 'undefined') {
                event = window.event;
            }
            if (event) {
                eventResult.time = new Date().toString();
                let finalObject = {
                    'mid': window.PG_ANALYTICS.mId,
                    'orderId': window.PG_ANALYTICS.orderId,
                    'promoCodeResponseMsg' : window.PG_ANALYTICS.promoCodeResponseMsg,
                    'event': eventResult
                };
                SendDataToServer_1.default.send(finalObject);
                event.returnValue = message;
            }
            return message;
        };
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = ClosureEvent;
