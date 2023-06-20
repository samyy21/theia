"use strict";
const notification_1 = require('../events/notification');
class NotificationFactory {
    constructor() {
    }
    static getInstance() {
        switch (NotificationFactory.theme) {
            default:
                return new notification_1.default();
        }
        ;
    }
}
NotificationFactory.theme = window.PG_ANALYTICS.theme;
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = NotificationFactory;
