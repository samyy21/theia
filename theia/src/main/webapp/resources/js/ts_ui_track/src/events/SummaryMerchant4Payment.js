"use strict";
const EventObject_1 = require('../interface/EventObject');
const Logger_1 = require('../utility/Logger');
class SummaryPayment {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Summary_card';
    }
    execute() {
        let selector = document.getElementsByClassName('clear rowBox');
        if (selector && selector.length > 0) {
            this.eventResult.description.push({ 'Amount to be paid': selector[0].textContent });
        }
        Logger_1.default.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = SummaryPayment;
