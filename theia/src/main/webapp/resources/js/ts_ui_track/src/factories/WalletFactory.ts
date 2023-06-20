import WalletEvent from '../events/WalletEvent';
import WalletMerchant4 from '../events/WalletMerchant4Event';
declare var window;

class WalletFactory {
    private static theme: string = window.PG_ANALYTICS.theme;
    public static getInstance() {
        // TODO: as of now no notification changes are there in any theme
        let event: any;
        switch (WalletFactory.theme) {
            case 'merchant4':
                event = new WalletMerchant4();
                break;
            default:
                event = new WalletEvent();
        };
        return event;
    }


}

export default WalletFactory;
