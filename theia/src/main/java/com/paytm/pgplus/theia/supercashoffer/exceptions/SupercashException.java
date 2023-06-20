package com.paytm.pgplus.theia.supercashoffer.exceptions;

public class SupercashException extends Exception {

    public SupercashException(String s) {
        super(s);
    }

    public static class SupercashIllegalParamException extends SupercashException {

        public SupercashIllegalParamException(String s) {
            super(s);
        }
    }

    public static class SupercashServiceException extends SupercashException {

        public SupercashServiceException(String s) {
            super(s);
        }
    }

    public static class SupercashMerchantNotEligibleException extends SupercashException {

        public SupercashMerchantNotEligibleException(String s) {
            super(s);
        }
    }

    public static class SupercashMerchantDetailsFetchMappingException extends SupercashException {

        public SupercashMerchantDetailsFetchMappingException(String s) {
            super(s);
        }
    }

    public static class InvalidSsoToken extends SupercashException {

        public InvalidSsoToken(String s) {
            super(s);
        }
    }
}
