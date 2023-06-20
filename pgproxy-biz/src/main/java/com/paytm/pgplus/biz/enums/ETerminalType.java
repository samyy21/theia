package com.paytm.pgplus.biz.enums;

import com.paytm.pgplus.facade.enums.TerminalType;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.validators.ParameterValidator;

public enum ETerminalType {

    APP("APP"), WEB("WEB"), WAP("WAP"), SYSTEM("SYSTEM"), ;

    String terminal;

    ETerminalType(String terminalType) {
        this.terminal = terminalType;
    }

    /**
     * @return the terminal
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * This method is used to fetch the TerminalType given the terminal.
     * 
     * @param terminal
     * @return
     * @throws InvalidParameterException
     *             if the input parameter is blank, or a value that is not
     *             supported by the system
     */
    public static TerminalType getTerminalTypeByTerminal(String terminal) throws FacadeInvalidParameterException {
        ParameterValidator.validateInputStringParam(terminal);
        for (TerminalType terminalType : TerminalType.values()) {
            if (terminal.equals(terminalType.getTerminal())) {
                return terminalType;
            }
        }
        throw new FacadeInvalidParameterException(
                "Given value of terminal is a value that is not supported by the system");
    }

}
