/**
*  Used to send data to server
*/
declare var window;
import Logger from '../utility/Logger';
class SendDataToServer {
    public static logToServer: boolean = true; // set it to false in prod
    public static send(data) {
        if (SendDataToServer.logToServer) {
            let xmlhttp = new XMLHttpRequest();   // new HttpRequest instance
            xmlhttp.open('POST', '/theia/trackUIEvents?' + 'MID=' + window.PG_ANALYTICS.mId
                + '&ORDER_ID=' + window.PG_ANALYTICS.orderId);
            xmlhttp.setRequestHeader('Content-Type', 'application/JSON');
            xmlhttp.send(JSON.stringify(data));

        }
    }
    public static sendInPlainText(data) {
        if (SendDataToServer.logToServer) {
            let xmlhttp = new XMLHttpRequest();   // new HttpRequest instance
            xmlhttp.open('POST', '/theia/trackUIException?' + 'MID=' + window.PG_ANALYTICS.mId
                + '&ORDER_ID=' + window.PG_ANALYTICS.orderId);
            xmlhttp.setRequestHeader('Content-Type', 'text/plain');
            xmlhttp.send(data);
        }
    }
}

export default SendDataToServer;
