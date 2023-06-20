import IAnalyticExecution from '../interface/IAnalyticExecution';
import EventObject from '../interface/EventObject';
import Logger from '../utility/Logger';
class NotificationEvent implements IAnalyticExecution {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Notification';
    }
    public execute() {

        Logger.log('Inside notification event');

        // notifications
        if (document.getElementsByClassName('notification') &&
            document.getElementsByClassName('notification').length > 0) {
            for (let i = 0; i < document.getElementsByClassName('notification').length; i++) {
                let aText = document.getElementsByClassName('notification')[i].textContent ||
                    document.getElementsByClassName('notification')[i].innerHTML;
                this.eventResult.description.push({ 'notification': aText });
            }
            Logger.log('array of notification', this.eventResult);

        } else {
            // no notification found
            Logger.log('no notification found');
        }
        return this.eventResult;
    }
}

export default NotificationEvent;
