package com.paytm.pgplus.theia.models.upiAccount.response;

import com.paytm.pgplus.response.BaseResponseBody;

@Deprecated
public class CheckUPIAccountResponseBody extends BaseResponseBody {

    private boolean upiAccountExist;

    public boolean isUpiAccountExist() {
        return upiAccountExist;
    }

    public void setUpiAccountExist(boolean upiAccountExist) {
        this.upiAccountExist = upiAccountExist;
    }

    @Override
    public String toString() {
        return "CheckUPIAccountResponseBody{" + "upiAccountExist=" + upiAccountExist + '}';
    }
}
