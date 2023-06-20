"use strict";
const EventObject_1 = require('../../interface/EventObject');
const Logger_1 = require('../../utility/Logger');
const SendDataToServer_1 = require('../../ajax/SendDataToServer');
class ClickMerchant4 {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Click_handler_Merchant';
    }
    processEvents(node) {
        let eventResult = new EventObject_1.default();
        Logger_1.default.log('inside process event', node, node.textContent);
        let tabs = ['Saved Details', 'Cash On Delivery (COD)', 'Debit Card',
            'Credit Card', 'Net Banking', 'ATM', 'IMPS', 'Cash Card', 'EMI'];
        eventResult.eventName = 'Click_Event';
        if (node && node.textContent != null && tabs.indexOf(node.textContent) !== -1) {
            eventResult.description = 'Tab Clicked on ' + node.textContent;
        }
        else {
            if (node.id !== undefined) {
                eventResult.description = 'Something else clicked' + node.id;
            }
            else if (node.class !== undefined) {
                eventResult.description = 'Something else clicked' + node.class;
            }
            else {
                eventResult.description = 'Something else clicked' + node;
            }
        }
        eventResult.time = new Date().toString();
        Logger_1.default.log('event result', eventResult);
        return eventResult;
    }
    execute() {
        let that = this;
        document.body.addEventListener('click', function (event) {
            let eventResult = new EventObject_1.default();
            if (event.isTrusted) {
                eventResult = that.processEvents(event.target);
            }
            if (event) {
                eventResult.time = new Date().toString();
                let finalObject = {
                    'mid': window.PG_ANALYTICS.mId,
                    'orderId': window.PG_ANALYTICS.orderId,
                    'event': eventResult
                };
                Logger_1.default.log('final object in click handler', finalObject, JSON.stringify(finalObject));
                SendDataToServer_1.default.send(finalObject);
            }
        });
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = ClickMerchant4;
