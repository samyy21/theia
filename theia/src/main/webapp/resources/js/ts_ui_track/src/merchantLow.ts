import IAnalyticExecution from './interface/IAnalyticExecution';
import LoadEvent from './events/LoadEvent';
import LoadState from './states/LoadState';
import EventObject from './interface/EventObject';
import Logger from './utility/Logger';
import SendDataToServer from './ajax/SendDataToServer';
import ClosureEvent from './events/closure/ClosureEvent';
import ClickMerchant4 from './events/click/ClickMerchant4';
declare var window;
class PAnalytics {
    // events array to keep track of event
    private events: IAnalyticExecution[];
    // key as combination of mid:orderid
    private key: string;
    private eventObject: EventObject[];

    constructor() {
        this.events = [];
        this.eventObject = [];
        if (window.PG_ANALYTICS && window.PG_ANALYTICS.logToConsole === 'true') {
            Logger.logToConsole = true;
        } else {
            Logger.logToConsole = false;
        }
        if (window.PG_ANALYTICS && window.PG_ANALYTICS.logToServer === 'true') {
            SendDataToServer.logToServer = true;
        } else {
            SendDataToServer.logToServer = false;
        }

    }

    public pushEvent(event: IAnalyticExecution) {
        this.events.push(event);
    }

    public executeAll() {

        Logger.log('window pg analytic:', window.PG_ANALYTICS);
        for (let i = 0; i < this.events.length; i++) {
            this.eventObject[i] = new EventObject();
            Logger.log('i,this.events', i, this.events);
            let result = this.events[i].execute();
            if (result && result.eventName) {
                this.eventObject[i].eventName = result.eventName;
                this.eventObject[i].description = result.description;
                this.eventObject[i].time = new Date().toString();
            }
        }

        let finalObject = {
            'mid': window.PG_ANALYTICS.mId,
            'orderId': window.PG_ANALYTICS.orderId,
            'events': this.eventObject
        };
        Logger.log('final object', finalObject, JSON.stringify(finalObject));
        SendDataToServer.send(finalObject);
        // let xmlhttp = new XMLHttpRequest();   // new HttpRequest instance
        // xmlhttp.open('POST', '/theia/trackUIEvents?' + 'MID=' + window.PG_ANALYTICS.mId
        //     + '&ORDER_ID=' + window.PG_ANALYTICS.orderId);
        // xmlhttp.setRequestHeader('Content-Type', 'application/JSON');
        // xmlhttp.send(JSON.stringify(finalObject));
    }
}

(function() {
    // TODO: put this js in setTimeout(0)
    let pAnalytics = new PAnalytics();
    try {
        document.onreadystatechange = function() {
            if (document.readyState === 'complete') {
                pAnalytics.pushEvent(new LoadState());
                pAnalytics.pushEvent(new ClosureEvent());
                pAnalytics.pushEvent(new ClickMerchant4());
                pAnalytics.executeAll();
            }
        };
    } catch (Exception) {
        SendDataToServer.sendInPlainText(Exception);
    }




} ());
