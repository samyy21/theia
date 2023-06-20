import NotificationEvent from '../events/notification';
declare var window;

class NotificationFactory {
    private static theme: string = window.PG_ANALYTICS.theme;
    constructor() {
    }

    public static getInstance() {
        // TODO: as of now no notification changes are there in any theme so directly return notification event
        switch (NotificationFactory.theme) {
            default:
                return new NotificationEvent();
        };
    }
}

export default NotificationFactory;
