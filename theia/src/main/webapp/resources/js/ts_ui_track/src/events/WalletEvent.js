"use strict";
const EventObject_1 = require('../interface/EventObject');
const Logger_1 = require('../utility/Logger');
class WalletEvent {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Wallet_check';
    }
    execute() {
        let selector = document.getElementsByClassName('card paytmcash-card');
        if (selector && selector.length > 0) {
            if (selector[0] && selector[0].className &&
                selector[0].className.indexOf('active') !== -1) {
                this.eventResult.description.push({ 'wallet active': 'true' });
            }
            else {
                this.eventResult.description.push({ 'wallet active': 'false' }, { 'info': 'wallet active but not visible' });
            }
        }
        Logger_1.default.log('Capture event inside Wallet Event', this.eventResult);
        return this.eventResult;
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = WalletEvent;
