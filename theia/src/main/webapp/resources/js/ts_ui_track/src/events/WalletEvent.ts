import IAnalyticExecution from '../interface/IAnalyticExecution';
import EventObject from '../interface/EventObject';
import Logger from '../utility/Logger';
class WalletEvent implements IAnalyticExecution {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Wallet_check';
    }
    public execute() {
        let selector = document.getElementsByClassName('card paytmcash-card');
        if (selector && selector.length > 0) {
            // check if paytm cash is active
            if (selector[0] && selector[0].className &&
                selector[0].className.indexOf('active') !== -1) {
                this.eventResult.description.push({ 'wallet active': 'true' });
            } else {
                this.eventResult.description.push({ 'wallet active': 'false' },
                    { 'info': 'wallet active but not visible' });
            }

        }
        Logger.log('Capture event inside Wallet Event', this.eventResult);
        return this.eventResult;
    }
}

export default WalletEvent;
