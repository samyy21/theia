/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @createdOn 02-Apr-2016
 * @author kesari
 */
public class CardInfo implements Serializable {

    private static final long serialVersionUID = 3145158677456167934L;

    @Tag(value = 1)
    private boolean saveCardEnabled = false;
    @Tag(value = 2)
    private boolean addAndPayViewSaveCardEnabled = false;
    @Tag(value = 3)
    private boolean cardStoreOption = true;
    @Tag(value = 4)
    private boolean cardStoreMandatory;
    @Tag(value = 5)
    private List<SavedCardInfo> merchantViewSavedCardsList = new ArrayList<>();
    @Tag(value = 6)
    private List<SavedCardInfo> addAndPayViewCardsList = new ArrayList<>();
    @Tag(value = 7)
    private Object configCardTypes;
    @Tag(value = 8)
    private Map<String, SavedCardInfo> savedCardMap;
    @Tag(value = 9)
    private Map<String, SavedCardInfo> addAnPaySavedCardMap;

    public Map<String, SavedCardInfo> getSavedCardMap() {
        return savedCardMap;
    }

    public void setSavedCardMap(Map<String, SavedCardInfo> savedCardMap) {
        this.savedCardMap = savedCardMap;
    }

    /**
     * @return the addAndPayViewSaveCardEnabled
     */
    public boolean isAddAndPayViewSaveCardEnabled() {
        return addAndPayViewSaveCardEnabled;
    }

    /**
     * @param addAndPayViewSaveCardEnabled
     *            the addAndPayViewSaveCardEnabled to set
     */
    public void setAddAndPayViewSaveCardEnabled(boolean addAndPayViewSaveCardEnabled) {
        this.addAndPayViewSaveCardEnabled = addAndPayViewSaveCardEnabled;
    }

    public Object getConfigCardTypes() {
        return configCardTypes;
    }

    public void setConfigCardTypes(Object configCardTypes) {
        this.configCardTypes = configCardTypes;
    }

    public boolean isSaveCardEnabled() {
        return saveCardEnabled;
    }

    public void setSaveCardEnabled(boolean saveCardEnabled) {
        this.saveCardEnabled = saveCardEnabled;
    }

    public boolean isCardStoreOption() {
        return cardStoreOption;
    }

    public void setCardStoreOption(boolean cardStoreOption) {
        this.cardStoreOption = cardStoreOption;
    }

    public List<SavedCardInfo> getMerchantViewSavedCardsList() {
        return merchantViewSavedCardsList;
    }

    public void setMerchantViewSavedCardsList(List<SavedCardInfo> merchantViewSavedCardsList) {
        this.merchantViewSavedCardsList = merchantViewSavedCardsList;
    }

    public List<SavedCardInfo> getAddAndPayViewCardsList() {
        return addAndPayViewCardsList;
    }

    public void setAddAndPayViewCardsList(List<SavedCardInfo> addAndPayViewCardsList) {
        this.addAndPayViewCardsList = addAndPayViewCardsList;
    }

    /**
     * @return the cardStoreMandatory
     */
    public boolean isCardStoreMandatory() {
        return cardStoreMandatory;
    }

    /**
     * @param cardStoreMandatory
     *            the cardStoreMandatory to set
     */
    public void setCardStoreMandatory(boolean cardStoreMandatory) {
        this.cardStoreMandatory = cardStoreMandatory;
    }

    /**
     * @return the addAnPaySavedCardMap
     */
    public Map<String, SavedCardInfo> getAddAnPaySavedCardMap() {
        return addAnPaySavedCardMap;
    }

    /**
     * @param addAnPaySavedCardMap
     *            the addAnPaySavedCardMap to set
     */
    public void setAddAnPaySavedCardMap(Map<String, SavedCardInfo> addAnPaySavedCardMap) {
        this.addAnPaySavedCardMap = addAnPaySavedCardMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CardInfo [saveCardEnabled=").append(saveCardEnabled).append(", addAndPayViewSaveCardEnabled=")
                .append(addAndPayViewSaveCardEnabled).append(", cardStoreOption=").append(cardStoreOption)
                .append(", cardStoreMandatory=").append(cardStoreMandatory).append(", merchantViewSavedCardsList=")
                .append(merchantViewSavedCardsList).append(", addAndPayViewCardsList=").append(addAndPayViewCardsList)
                .append(", configCardTypes=").append(configCardTypes).append(", savedCardMap=").append(savedCardMap)
                .append(", addAnPaySavedCardMap=").append(addAnPaySavedCardMap).append("]");
        return builder.toString();
    }
}