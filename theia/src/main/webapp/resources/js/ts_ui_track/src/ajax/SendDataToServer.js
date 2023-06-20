"use strict";
class SendDataToServer {
    static send(data) {
        if (SendDataToServer.logToServer) {
            let xmlhttp = new XMLHttpRequest();
            xmlhttp.open('POST', '/theia/trackUIEvents?' + 'MID=' + window.PG_ANALYTICS.mId
                + '&ORDER_ID=' + window.PG_ANALYTICS.orderId);
            xmlhttp.setRequestHeader('Content-Type', 'application/JSON');
            xmlhttp.send(JSON.stringify(data));
        }
    }
    static sendInPlainText(data) {
        if (SendDataToServer.logToServer) {
            let xmlhttp = new XMLHttpRequest();
            xmlhttp.open('POST', '/theia/trackUIException?' + 'MID=' + window.PG_ANALYTICS.mId
                + '&ORDER_ID=' + window.PG_ANALYTICS.orderId);
            xmlhttp.setRequestHeader('Content-Type', 'text/plain');
            xmlhttp.send(data);
        }
    }
}
SendDataToServer.logToServer = true;
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = SendDataToServer;
