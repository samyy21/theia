import IAnalyticExecution from '../../interface/IAnalyticExecution';
import EventObject from '../../interface/EventObject';
import Logger from '../../utility/Logger';
import SendDataToServer from '../../ajax/SendDataToServer';
declare var window;
class ClosureEvent implements IAnalyticExecution {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Closure_check';
    }
    public execute() {
        window.onbeforeunload = function(event) {
            let eventResult: EventObject = new EventObject();
            eventResult.eventName = 'Closure check';
            let message = 'Are you sure you want to cancel the payment?';
            if (typeof event === 'undefined') {
                event = window.event;
            }
            if (event) {
                // TODO: push to server user trying to close
                eventResult.time = new Date().toString();
                let finalObject = {
                    'mid': window.PG_ANALYTICS.mId,
                    'orderId': window.PG_ANALYTICS.orderId,
                    'event': eventResult
                };
                //  Logger.log('final object in closure event', finalObject);
                SendDataToServer.send(finalObject);
                event.returnValue = message;

            }
            return message;
        };
    }
}

export default ClosureEvent;
