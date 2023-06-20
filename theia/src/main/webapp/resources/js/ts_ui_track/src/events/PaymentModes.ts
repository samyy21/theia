import IAnalyticExecution from '../interface/IAnalyticExecution';
import EventObject from '../interface/EventObject';
import Logger from '../utility/Logger';
class PaymentModes implements IAnalyticExecution {
    private eventResult: EventObject;
    constructor() {
        this.eventResult = new EventObject();
        this.eventResult.eventName = 'Payment_Modes';
    }
    public execute() {
        if (document.getElementById('merchant-payment-modes')) {
            this.eventResult.description.push(
                { 'payment-main-div': 'merchant-payment-modes' });
        }
        if (document.getElementById('add-money-payment-modes')) {
            this.eventResult.description.push(
                { 'payment-main-div': 'add-money-payment-modes' });
        }
        let selector = document.getElementsByClassName('cards-control');
        if (selector && selector.length > 0) {
            // find the grid element first
            let gridElement = undefined;
            if (selector[0] && selector[0].childNodes && selector[0].childNodes.length > 0) {
                for (let i = 0; i < selector[0].childNodes.length; i++) {
                    let selectorInside: any = selector[0].childNodes[i];
                    if (selectorInside.className === 'grid') {
                        gridElement = selectorInside;
                        break;
                    }
                }
            }
            if (gridElement) {
                for (let i = 0; i < gridElement.childNodes.length; i++) {
                    if (gridElement.childNodes[i].className &&
                        gridElement.childNodes[i].className.indexOf('card') !== -1) {
                        if (gridElement.childNodes[i].className.indexOf('active') !== -1) {
                            this.eventResult.description.push(
                                { 'selected-payment-mode': gridElement.childNodes[i].textContent });
                        } else {
                            this.eventResult.description.push(
                                { 'payment-mode': gridElement.childNodes[i].textContent });
                        }
                    }
                }
            }

        }
        Logger.log('Capture event inside summary payment', this.eventResult);
        return this.eventResult;
    }
}

export default PaymentModes;
