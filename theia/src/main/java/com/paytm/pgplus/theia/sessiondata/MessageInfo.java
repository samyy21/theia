package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author kartik
 * @date 30-03-2017
 */
public class MessageInfo implements Serializable {

    private static final long serialVersionUID = -9179770462665472443L;
    @Tag(value = 1)
    private String lowPercentageMessage;
    @Tag(value = 2)
    private String maintenanceMessage;
    @Tag(value = 3)
    private String inValidVPAMessage = "You have entered an invalid VPA. Please check and try again.";
    @Tag(value = 4)
    private String merchantTLSWarnMsg;
    @Tag(value = 5)
    private String addAndPayTLSWarnMsg;

    public MessageInfo() {
    }

    public String getLowPercentageMessage() {
        return lowPercentageMessage;
    }

    public void setLowPercentageMessage(String lowPercentageMessage) {
        this.lowPercentageMessage = lowPercentageMessage;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public String getInValidVPAMessage() {
        return inValidVPAMessage;
    }

    public void setInValidVPAMessage(String inValidVPAMessage) {
        this.inValidVPAMessage = inValidVPAMessage;
    }

    public String getMerchantTLSWarnMsg() {
        return merchantTLSWarnMsg;
    }

    public void setMerchantTLSWarnMsg(String merchantTLSWarnMsg) {
        this.merchantTLSWarnMsg = merchantTLSWarnMsg;
    }

    public String getAddAndPayTLSWarnMsg() {
        return addAndPayTLSWarnMsg;
    }

    public void setAddAndPayTLSWarnMsg(String addAndPayTLSWarnMsg) {
        this.addAndPayTLSWarnMsg = addAndPayTLSWarnMsg;
    }

    @Override
    public String toString() {
        return "MessageInfo{" + "lowPercentageMessage='" + lowPercentageMessage + '\'' + ", maintenanceMessage='"
                + maintenanceMessage + '\'' + ", inValidVPAMessage='" + inValidVPAMessage + '\''
                + ", merchantTLSWarnMsg='" + merchantTLSWarnMsg + '\'' + ", addAndPayTLSWarnMsg='"
                + addAndPayTLSWarnMsg + '\'' + '}';
    }
}
