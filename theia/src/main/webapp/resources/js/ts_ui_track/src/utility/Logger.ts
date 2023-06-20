class Logger {
    public static logToConsole: boolean = true; // set it to false in prod
    public static log(...argument) {
        if (Logger.logToConsole) {
            console.log(argument);
        }
    }
}

export default Logger;
