"use strict";
class Logger {
    static log(...argument) {
        if (Logger.logToConsole) {
            console.log(argument);
        }
    }
}
Logger.logToConsole = true;
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = Logger;
