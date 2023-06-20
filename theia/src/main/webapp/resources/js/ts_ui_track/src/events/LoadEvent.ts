import IAnalyticExecution from '../interface/IAnalyticExecution';
import Logger from '../utility/Logger';
class LoadEvent implements IAnalyticExecution {
    public execute() {

        document.onreadystatechange = function() {
            let a = [];
            if (document.readyState === 'complete') {
                Logger.log('load basic completed');

                // notifications
                if (document.getElementsByClassName('notification') &&
                    document.getElementsByClassName('notification').length > 0) {
                    let aText = document.getElementsByClassName('notification')[0].textContent ||
                                document.getElementsByClassName('notification')[0].innerHTML;
                    a.push({ 'notification': aText });
                }
                Logger.log('array of notification', a);
            }
        };
    }
}

export default LoadEvent;
