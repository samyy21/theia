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
        let selector = document.getElementsByClassName('card summary-card');
        if (selector && selector.length > 0) {
            // check if paytm cash is active
            if (selector[0] && selector[0].childNodes && selector[0].childNodes.length > 0) {
                for (let i = 0; i < selector[0].childNodes.length; i++) {
                    let selectorInside: any = selector[0].childNodes[i];
                    if (selectorInside.className === 'grid mt10 mb5') {
                        this.eventResult.description.push(
                            { 'Wallet Balance Text': selectorInside.textContent });
                    }
                }
            }

        }
        Logger.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}

export default SummaryPayment;
