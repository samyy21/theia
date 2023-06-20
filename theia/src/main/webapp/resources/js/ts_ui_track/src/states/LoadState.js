"use strict";
const notification_1 = require('../events/notification');
const EventObject_1 = require('../interface/EventObject');
const WalletFactory_1 = require('../factories/WalletFactory');
const SummaryFactory_1 = require('../factories/SummaryFactory');
const PaymentModes_1 = require('../events/PaymentModes');
const Logger_1 = require('../utility/Logger');
class LoadState {
    constructor() {
        this.events = [];
        this.retObject = new EventObject_1.default();
        this.retObject.eventName = 'Load_Event';
    }
    execute() {
        this.registerAllEvents();
        this.events.forEach(event => {
            Logger_1.default.log('event recieved ', event);
            let result = event.execute();
            Logger_1.default.log('inside load state event', this.retObject, this.retObject.description);
            this.retObject.description.push(result);
        });
        Logger_1.default.log('Inside LoadState returning', this.retObject);
        return this.retObject;
    }
    registerEvent(event) {
        this.events.push(event);
    }
    registerAllEvents() {
        this.registerEvent(new notification_1.default());
        this.registerEvent(WalletFactory_1.default.getInstance());
        this.registerEvent(SummaryFactory_1.default.getInstance());
        this.registerEvent(new PaymentModes_1.default());
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = LoadState;
