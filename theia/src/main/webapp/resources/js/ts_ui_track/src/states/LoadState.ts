import IAnalyticExecution from '../interface/IAnalyticExecution';
import NotificationEvent from '../events/notification';
import EventObject from '../interface/EventObject';
import WalletFactory from '../factories/WalletFactory';
import SummaryFactory from '../factories/SummaryFactory';
import PaymentModes from '../events/PaymentModes';
import Logger from '../utility/Logger';
class LoadState implements IAnalyticExecution {
    private events: IAnalyticExecution[];
    private retObject: EventObject;

    public execute() {
        // register all events
        this.registerAllEvents();

        // execute all events and send an object array
        this.events.forEach(event => {
            Logger.log('event recieved ', event);
            let result = event.execute();
            Logger.log('inside load state event', this.retObject, this.retObject.description);
            this.retObject.description.push(result);

        });
        Logger.log('Inside LoadState returning', this.retObject);
        return this.retObject;
    }

    constructor() {
        this.events = [];
        this.retObject = new EventObject();
        this.retObject.eventName = 'Load_Event';
    }
    // allows to register
    private registerEvent(event: IAnalyticExecution) {
        this.events.push(event);
    }
    private registerAllEvents() {
        this.registerEvent(new NotificationEvent());
        this.registerEvent(WalletFactory.getInstance());
        this.registerEvent(SummaryFactory.getInstance());
        this.registerEvent(new PaymentModes());
    }


}

export default LoadState;
