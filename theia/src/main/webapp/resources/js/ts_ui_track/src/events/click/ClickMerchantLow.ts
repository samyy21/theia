import EventObject from '../../interface/EventObject';
import Logger from '../../utility/Logger';
import SendDataToServer from '../../ajax/SendDataToServer';
declare var window;
class ClickMerchant4 {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Click_handler_Merchant';
    }

    public processEvents(node) {
        // check for tab
        let eventResult: EventObject = new EventObject();
        Logger.log('inside process event', node, node.textContent);
        // check for if the clicked dom is tabs
        let tabs = ['Saved Details', 'Cash On Delivery (COD)', 'Debit Card',
            'Credit Card', 'Net Banking', 'ATM', 'IMPS', 'Cash Card', 'EMI'];
        eventResult.eventName = 'Click_Event';
        if (node && node.textContent != null && tabs.indexOf(node.textContent) !== -1) {
            eventResult.description = 'Tab Clicked on ' + node.textContent;
        } else {
            if (node.id !== undefined) {
                eventResult.description = 'Something else clicked' + node.id;
            } else if (node.class !== undefined) {
                eventResult.description = 'Something else clicked' + node.class;
            } else {
                eventResult.description = 'Something else clicked' + node;
            }


        }
        eventResult.time = new Date().toString();
        Logger.log('event result', eventResult);
        return eventResult;
    }
    public execute() {
        let that = this;
        document.body.addEventListener('click', function(event) {
            let eventResult: EventObject = new EventObject();
            if (event.isTrusted) {
                eventResult = that.processEvents(event.target);
            }
            if (event) {
                // TODO: push to server user trying to close
                eventResult.time = new Date().toString();
                let finalObject = {
                    'mid': window.PG_ANALYTICS.mId,
                    'orderId': window.PG_ANALYTICS.orderId,
                    'event': eventResult
                };
                Logger.log('final object in click handler',
                    finalObject, JSON.stringify(finalObject));
                SendDataToServer.send(finalObject);

            }

        });
    }

}
export default ClickMerchant4;
