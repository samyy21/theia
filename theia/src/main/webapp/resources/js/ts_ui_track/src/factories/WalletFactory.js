"use strict";
const WalletEvent_1 = require('../events/WalletEvent');
const WalletMerchant4Event_1 = require('../events/WalletMerchant4Event');
class WalletFactory {
    static getInstance() {
        let event;
        switch (WalletFactory.theme) {
            case 'merchant4':
                event = new WalletMerchant4Event_1.default();
                break;
            default:
                event = new WalletEvent_1.default();
        }
        ;
        return event;
    }
}
WalletFactory.theme = window.PG_ANALYTICS.theme;
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = WalletFactory;
