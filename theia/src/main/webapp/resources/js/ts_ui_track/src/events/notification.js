"use strict";
const EventObject_1 = require('../interface/EventObject');
const Logger_1 = require('../utility/Logger');
class NotificationEvent {
    constructor() {
        this.eventResult = new EventObject_1.default();
        this.eventResult.eventName = 'Notification';
    }
    execute() {
        Logger_1.default.log('Inside notification event');
        if (document.getElementsByClassName('notification') &&
            document.getElementsByClassName('notification').length > 0) {
            for (let i = 0; i < document.getElementsByClassName('notification').length; i++) {
                let aText = document.getElementsByClassName('notification')[i].textContent ||
                    document.getElementsByClassName('notification')[i].innerHTML;
                this.eventResult.description.push({ 'notification': aText });
            }
            Logger_1.default.log('array of notification', this.eventResult);
        }
        else {
            Logger_1.default.log('no notification found');
        }
        return this.eventResult;
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = NotificationEvent;
