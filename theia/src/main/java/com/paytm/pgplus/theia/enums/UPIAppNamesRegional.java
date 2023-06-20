package com.paytm.pgplus.theia.enums;

import java.util.Arrays;
import java.util.List;

public enum UPIAppNamesRegional {

    en("Paytm", "PhonePe", "Google Pay", "UPI Linked Bank/ UPI"), hi("पेटीएम", "फोनपे", "गूगल पे",
            "UPI से लिंक बैंक/UPI"), bn("পেটিএম", "ফোনপে", "গুগল পে", "UPI লিঙ্ক করা ব্যাঙ্ক/UPI"), or("ପେଟିଏମ",
            "ଫୋନ୍-ପେ", "ଗୁଗୁଲ ପେ", "UPI ଲିଙ୍କ୍ ହୋଇଥିବା ବ୍ୟାଙ୍କ/ UPI"), mr("पेटीएम", "फोनपे", "गूगल पे",
            "UPI लिंक केलेली बँक/UPI"), ml("പേടിഎം", "ഫോൺപേ", "ഗൂഗിൾ പ്ലേ", "UPI ലിങ്ക്‍ഡ് ബാങ്ക്/ UPI"), kn("ಪೇಟಿಎಂ",
            "ಫೋನ್‌ಪೇ", "ಗೂಗಲ್ ಪೇ", "UPI ಲಿಂಕ್ ಆದ ಬ್ಯಾಂಕ್/UPI"), ta("பேடிஎம்", "போன்பே", "கூகுள் பே",
            "UPI இணைக்கப்பட்ட வங்கி/ UPI"), te("పేటిఎం", "ఫోన్‌పే", "గూగల్ పే", "UPI అనుసంధానించబడిన బ్యాంక్/UPI"), gu(
            "પેટીએમ", "ફોનપે", "ગૂગલ પે", "UPI લિંક કરેલ બેંક/ UPI"), pa("ਪੇਟੀਐਮ", "ਫੋਨਪੇ", "ਗੂਗਲ ਪੇ",
            "UPI ਨਾਲ ਲਿੰਕ ਬੈਂਕ/ UPI");

    private final List<String> values;

    UPIAppNamesRegional(String... values) {
        this.values = Arrays.asList(values);
    }

    public List<String> getValues() {
        return values;
    }
}
