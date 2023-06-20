package com.paytm.pgplus.session.serializer;

import java.io.Serializable;

public class SerializerWrapper implements Serializable {

    private static final long serialVersionUID = -8820707194085359081L;
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
