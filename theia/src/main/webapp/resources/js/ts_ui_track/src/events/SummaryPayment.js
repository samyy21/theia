"use strict";
const EventObject_1 = require('../interface/EventObject');
const Logger_1 = require('../utility/Logger');
class SummaryPayment {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Summary_card';
    }
    execute() {
        let selector = document.getElementsByClassName('card summary-card');
        if (selector && selector.length > 0) {
            if (selector[0] && selector[0].childNodes && selector[0].childNodes.length > 0) {
                for (let i = 0; i < selector[0].childNodes.length; i++) {
                    let selectorInside = selector[0].childNodes[i];
                    if (selectorInside.className === 'grid mt10 mb5') {
                        this.eventResult.description.push({ 'Wallet Balance Text': selectorInside.textContent });
                    }
                }
            }
        }
        Logger_1.default.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = SummaryPayment;
