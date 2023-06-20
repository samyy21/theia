"use strict";
const EventObject_1 = require('../interface/EventObject');
const Logger_1 = require('../utility/Logger');
class PaymentModes {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Payment_Modes';
    }
    execute() {
        if (document.getElementById('merchant-payment-modes')) {
            this.eventResult.description.push({ 'payment-main-div': 'merchant-payment-modes' });
        }
        if (document.getElementById('add-money-payment-modes')) {
            this.eventResult.description.push({ 'payment-main-div': 'add-money-payment-modes' });
        }
        let selector = document.getElementsByClassName('cards-control');
        if (selector && selector.length > 0) {
            let gridElement = undefined;
            if (selector[0] && selector[0].childNodes && selector[0].childNodes.length > 0) {
                for (let i = 0; i < selector[0].childNodes.length; i++) {
                    let selectorInside = selector[0].childNodes[i];
                    if (selectorInside.className === 'grid') {
                        gridElement = selectorInside;
                        break;
                    }
                }
            }
            if (gridElement) {
                for (let i = 0; i < gridElement.childNodes.length; i++) {
                    if (gridElement.childNodes[i].className &&
                        gridElement.childNodes[i].className.indexOf('card') !== -1) {
                        if (gridElement.childNodes[i].className.indexOf('active') !== -1) {
                            this.eventResult.description.push({ 'selected-payment-mode': gridElement.childNodes[i].textContent });
                        }
                        else {
                            this.eventResult.description.push({ 'payment-mode': gridElement.childNodes[i].textContent });
                        }
                    }
                }
            }
        }
        Logger_1.default.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = PaymentModes;
