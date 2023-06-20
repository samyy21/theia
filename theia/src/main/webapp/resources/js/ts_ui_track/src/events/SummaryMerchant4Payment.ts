import IAnalyticExecution from '../interface/IAnalyticExecution';
import EventObject from '../interface/EventObject';
import Logger from '../utility/Logger';
class SummaryPayment implements IAnalyticExecution {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Summary_card';
    }
    public execute() {
        let selector = document.getElementsByClassName('clear rowBox');
        if (selector && selector.length > 0) {
            // check if paytm cash is active
            this.eventResult.description.push(
                { 'Amount to be paid': selector[0].textContent });

        }
        Logger.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}

export default SummaryPayment;
