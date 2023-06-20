import SummaryPayment from '../events/SummaryPayment';
import SummaryMerchant4Payment from '../events/SummaryMerchant4Payment';
declare var window;

class SummaryFactory {
    private static theme: string = window.PG_ANALYTICS.theme;
    public static getInstance() {
        let event: any;
        switch (SummaryFactory.theme) {
            case 'merchant4':
                event = new SummaryMerchant4Payment();
                break;
            default:
                event = new SummaryPayment();
                break;
        };
        return event;
    }
}

export default SummaryFactory;
