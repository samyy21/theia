"use strict";
const Logger_1 = require('../utility/Logger');
class LoadEvent {
    execute() {
        document.onreadystatechange = function () {
            let a = [];
            if (document.readyState === 'complete') {
                Logger_1.default.log('load basic completed');
                if (document.getElementsByClassName('notification') &&
                    document.getElementsByClassName('notification').length > 0) {
                    let aText = document.getElementsByClassName('notification')[0].textContent ||
                        document.getElementsByClassName('notification')[0].innerHTML;
                    a.push({ 'notification': aText });
                }
                Logger_1.default.log('array of notification', a);
            }
        };
    }
}
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = LoadEvent;
