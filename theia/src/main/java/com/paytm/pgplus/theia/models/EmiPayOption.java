package com.paytm.pgplus.theia.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by charuaggarwal on 12/9/17.
 */
public class EmiPayOption {
    private Set<String> allowedPayOption = new HashSet<String>();
    private Set<String> disallowedPayOption = new HashSet<String>();
    private Set<String> banks = new HashSet<String>();
    private boolean dropAll;
    private boolean showAll;

    public boolean isDropAll() {
        return dropAll;
    }

    public void setDropAll(boolean dropAll) {
        this.dropAll = dropAll;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    public Set<String> getAllowedPayOption() {
        return allowedPayOption;
    }

    public void setAllowedPayOption(Set<String> allowedPayOption) {
        this.allowedPayOption = allowedPayOption;
    }

    public Set<String> getDisallowedPayOption() {
        return disallowedPayOption;
    }

    public void setDisallowedPayOption(Set<String> disallowedPayOption) {
        this.disallowedPayOption = disallowedPayOption;
    }

    public Set<String> getBanks() {
        return banks;
    }

    public void setBanks(Set<String> banks) {
        this.banks = banks;
    }
}
