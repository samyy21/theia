package com.paytm.pgplus.theia.nativ.model.fetchpspapps;

import com.paytm.pgplus.cache.model.PspSchemaInfo;
import com.paytm.pgplus.common.model.ResultInfo;

import java.util.List;

public class FetchPspAppsResponseBody {

    private List<PspSchemaInfo> pspSchemas;

    private ResultInfo resultInfo;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public List<PspSchemaInfo> getPspSchemas() {
        return pspSchemas;
    }

    public void setPspSchemas(List<PspSchemaInfo> pspSchemas) {
        this.pspSchemas = pspSchemas;
    }
}
