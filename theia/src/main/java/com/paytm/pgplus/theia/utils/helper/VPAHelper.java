package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.PaytmVpaDetails;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.UNMASKED_VPA_MERCHANT;

@Component("vpaHelper")
public class VPAHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VPAHelper.class);

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    public void populateVPALinkedBankAccounts(UserProfileSarvatra userProfileSarvatra) {
        if (userProfileSarvatra == null || userProfileSarvatra.getResponse() == null) {
            LOGGER.info("VPA Detail Response is null");
        }
        PaytmVpaDetails vpaDetails = userProfileSarvatra.getResponse();
        List<SarvatraVpaDetails> vpaDetailsList = vpaDetails.getVpaDetails();
        List<PaytmBanksVpaDefaultDebitCredit> bankAccounts = vpaDetails.getBankAccounts();
        SarvatraVpaDetails primaryVPA = fetchPrimaryVpa(vpaDetailsList);
        if (primaryVPA != null) {
            formVpaDetailsFromBankAccounts(primaryVPA, vpaDetailsList, bankAccounts);
        } else {
            LOGGER.info("No Primary VPA Found for this User");
        }
    }

    public void formVpaDetailsFromBankAccounts(SarvatraVpaDetails primarySarvatraVpaDetails,
            List<SarvatraVpaDetails> vpaDetailsList, List<PaytmBanksVpaDefaultDebitCredit> bankAccounts) {
        for (PaytmBanksVpaDefaultDebitCredit paytmBanksVpaDefaultDebitCredit : bankAccounts) {
            if (primarySarvatraVpaDetails.getDefaultDebit() != null
                    && !paytmBanksVpaDefaultDebitCredit.getAccount().equals(
                            primarySarvatraVpaDetails.getDefaultDebit().getAccount())) {
                SarvatraVpaDetails newSarvatraVpa = new SarvatraVpaDetails();
                newSarvatraVpa.setPrimary(primarySarvatraVpaDetails.isPrimary());
                newSarvatraVpa.setName(primarySarvatraVpaDetails.getName());
                newSarvatraVpa.setDefaultCredit(null);

                // Set defaultdebitcredit-name null
                PaytmBanksVpaDefaultDebitCredit paytmBanksVpaDefaultDebitCredit1 = SerializationUtils
                        .clone(paytmBanksVpaDefaultDebitCredit);
                // paytmBanksVpaDefaultDebitCredit1.setName(null);
                newSarvatraVpa.setDefaultDebit(paytmBanksVpaDefaultDebitCredit1);

                // add into final vpa-list
                vpaDetailsList.add(newSarvatraVpa);
            }
        }
    }

    public SarvatraVpaDetails fetchPrimaryVpa(List<SarvatraVpaDetails> vpaDetailsList) {
        SarvatraVpaDetails primaryVPA = null;
        for (SarvatraVpaDetails sarvatraVpaDetails : vpaDetailsList) {
            if (sarvatraVpaDetails.isPrimary()) {
                primaryVPA = sarvatraVpaDetails;
                break;
            }
        }
        return primaryVPA;
    }

    public static String setMaskedMerchantVpa(String unmaskedVpa, String mid) {
        try {
            String maskedVpaMerchant = ConfigurationUtil.getTheiaProperty(UNMASKED_VPA_MERCHANT,
                    TheiaConstant.ExtraConstants.NONE);
            if (maskedVpaMerchant.contains(TheiaConstant.ExtraConstants.ALL)
                    || buildUnmaskedVpaMerchantList(maskedVpaMerchant).contains(mid)) {
                return unmaskedVpa;
            }
            return MaskingUtil.getMaskedVpa(unmaskedVpa);
        } catch (Exception e) {
            LOGGER.error("error while masking merchant VPA");
            return unmaskedVpa;
        }

    }

    private static Set<String> buildUnmaskedVpaMerchantList(String unmaskMerchantVpa) {
        Set<String> unmaskedMerchantVpaList;
        unmaskedMerchantVpaList = new HashSet<String>(Arrays.asList(unmaskMerchantVpa.trim().split(",")));
        return unmaskedMerchantVpaList;
    }
}
