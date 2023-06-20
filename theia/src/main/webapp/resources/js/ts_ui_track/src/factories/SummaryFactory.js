"use strict";
const SummaryPayment_1 = require('../events/SummaryPayment');
const SummaryMerchant4Payment_1 = require('../events/SummaryMerchant4Payment');
class SummaryFactory {
    static getInstance() {
        let event;
        switch (SummaryFactory.theme) {
            case 'merchant4':
                event = new SummaryMerchant4Payment_1.default();
                break;
            default:
                event = new SummaryPayment_1.default();
                break;
        }
        ;
        return event;
    }
}
SummaryFactory.theme = window.PG_ANALYTICS.theme;
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = SummaryFactory;
