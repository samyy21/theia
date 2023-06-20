package com.paytm.pgplus.theia.nativ.enums;

public enum BankDisplayNames {
    ABPB("ABPB"), ADVANCE_DEPOSIT("Advance Deposit"), AIRTEL_MONEY("Airtel Money"), AIRTEL_PAYMENTS_BANK(
            "AIRTEL PAYMENTS BANK"), ALLAHABAD_BANK("Allahabad Bank"), AMBIKA("AMBIKA"), AMBIKARECHARGE(
            "AMBIKARECHARGE"), AMEX("AMEX"), ANDHRA_BANK("Andhra Bank"), ANDHRA_BANK_DEBIT_CARD(
            "Andhra Bank Debit Card"), AXIS_BANK("Axis Bank"), AXIS_EDC("Axis EDC"), AXISDIRECT("AXISDIRECT"), AXSD(
            "AXSD"), BAJAJ_FINSERV_EMI_CARD("Bajaj Finserv EMI Card"), BANK_OF_BAHRAIN_AND_KUWAIT(
            "Bank of Bahrain and Kuwait"), BANK_OF_BARODA("Bank of Baroda"), BANK_OF_INDIA("Bank of India"), BANK_OF_MAHARASHTRA(
            "Bank of Maharashtra"), BANK_OF_RAJASTHAN("Bank of Rajasthan"), BASIXSUB("BASIXSub"), BHARAT("BHARAT"), BILLDESK(
            "BILLDESK"), BOBFSS("BOBFSS"), CANARA_BANK("Canara Bank"), CANARA_BANK_DEBIT_CARD("Canara Bank Debit Card"), CASH_ON_DELIVERY(
            "Cash On Delivery"), CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank"), CBI_TPV_BANK("CBI TPV Bank"), CCAVENUE(
            "CCAVENUE"), CCAVENUE_ADV("CCAVENUE ADV"), CENTRAL_BANK_OF_INDIA("Central Bank of India"), CERESINFOTECH(
            "CeresInfotech"), CITI_DINERS("Citi Diners"), CITI_DIRECT("Citi Direct"), CITIBANK("Citibank"), CITIBANK_DEBIT_CARD(
            "Citibank Debit Card"), CITY_UNION_BANK("City Union Bank"), COMPARK("Compark"), CORPORATION_BANK(
            "Corporation Bank"), COSMOS_BANK("Cosmos Bank"), DCB_BANK_PERSONAL("DCB Bank Personal"), DENA_BANK(
            "Dena Bank"), DEUTSCHE_BANK("Deutsche Bank"), DEVELOPMENT_BANKSINGAPORE("Development BankSingapore"), DFDFD(
            "dfdfd"), DHANLAXMI_BANK("Dhanlaxmi Bank"), EASYPAY("EASYPAY"), EMITRA("EMITRA"), EPAISA("EPAISA"), EZETAP(
            "EZETAP"), FEDERAL_BANK("Federal Bank"), FINO("FINO"), FISB("FISB"), FISN("FISN"), FISP2B("FISP2B"), FISR(
            "FISR"), GIFTVOUCHER("GIFTVOUCHER"), GP_PARSIK_BANK("GP Parsik Bank"), HDDO("HDDO"), HDFC("HDFC"), HDFC_CORPORATE(
            "HDFC Corporate"), HDFC_CYBER_SOURCE("HDFC Cyber Source"), HDFC_DEBIT_EMI("HDFC DEBIT EMI"), HDFC_IDEBIT(
            "HDFC IDEBIT"), HDFCDIRECT("HDFCDIRECT"), HDFS_SINGLE_CLICK("HDFS Single Click"), HSBC("HSBC"), ICICI_BANK(
            "ICICI Bank"), ICICI_EDC("ICICI EDC"), ICICI_EMI("ICICI EMI"), ICICIDIRECT("ICICIDIRECT"), ICICIIDEBIT(
            "ICICIIDEBIT"), ICICIPAY("ICICIPAY"), ICIO("ICIO"), IDBI_BANK("IDBI Bank"), IDFC_BANK("IDFC Bank"), INDIAN_BANK(
            "Indian Bank"), INDIAN_OVERSEAS_BANK("Indian Overseas Bank"), INDIAN_OVERSEAS_BANK_DEBIT_CARD(
            "Indian Overseas Bank Debit Card"), INDUSIND_BANK("IndusInd Bank"), ING_VYSYA_BANK(
            "ING Vysya Bank (now Kotak)"), INTERNAL_PAYTM_TRANSFER("Internal Paytm Transfer"), ITZ_CARD("ITZ Card"), J2JSOFTWARE(
            "J2JSoftware"), JAMMU_AND_KASHMIR_BANK("Jammu and Kashmir Bank"), JSB("JSB"), KARNATAKA_BANK(
            "Karnataka Bank"), KARUR_VYSYA_BANK("Karur Vysya Bank"), KOTAK_BANK("Kotak Bank"), LAKSHMI_VILAS_BANK(
            "Lakshmi Vilas Bank"), LOCALCUBE("LocalCube"), LOYALTY_REWARDZ("Loyalty Rewardz"), LOYALTYPOINT(
            "LOYALTYPOINT"), NEWBANK("Newbank"), NUMBERMALL("NUMBERMALL"), OFFLINE("OFFLINE"), ORIENTAL_BANK_OF_COMMERCE(
            "Oriental Bank of Commerce"), PAY_CASH_CARD("Pay Cash Card"), PAYEZEE("Payezee"), PAYTM_DIGITAL_CREDIT_CAPS(
            "Paytm Digital Credit"), PAYTM_DIGITAL_CREDIT("paytm digital credit"), PAYTM_INTERNAL_BANK(
            "Paytm Internal Bank"), PAYTM_NEBANKING("Paytm Nebanking"), PAYTM_PAYMENTS_BANK("Paytm Payments Bank"), PAYTM_WALLET(
            "Paytm Wallet"), PAYWORLD("PAYWORLD"), PERQULOYALTY_CAPS("PERQULOYALTY"), PERQULOYALTY("PerquLoyalty"), PGI(
            "PGI"), PPBI("PPBI"), PPBL_UPI_COLLECT("PPBL UPI Collect"), PPBL_UPI_EXPRESS("PPBL UPI Express"), PPBLPEDC(
            "PPBLPEDC"), PPBS("PPBS"), PUNJAB_SIND_BANK("Punjab & Sind Bank"), PUNJAB_MAHARASTRA_CO_BANK(
            "Punjab Maharastra Co Bank"), PUNJAB_NATIONAL_BANK("Punjab National Bank"), PUNJAB_NATIONAL_BANK_DEBIT_CARD(
            "Punjab National Bank Debit Card"), RBL_BANK("RBL Bank"), ROYAL_BANK_OF_SCOTLAND("Royal Bank of Scotland"), RUPAY(
            "RUPAY"), SARASWAT_BANK("Saraswat Bank"), SBI("SBI"), SBIFSS("SBIFSS"), SOJITRA("Sojitra"), SOUTH_INDIAN_BANK(
            "South Indian Bank"), STANDARD_CHARTERED_BANK("Standard Chartered Bank"), STATE_BANK_OF_BIKANER_AND_JAIPUR(
            "State Bank of Bikaner and Jaipur"), STATE_BANK_OF_HYDERABAD("State Bank Of Hyderabad"), UPI_PUSH(
            "Unified Payment Interface - PUSH Express"), UPI("Unified Payment Interface"), NET_BANKING("NET_BANKING");

    private String bankName;

    BankDisplayNames(String bankName) {
        this.bankName = bankName;
    }

    public String getBankName() {
        return bankName;
    }
}
