package com.paytm.pgplus.biz.workflow.service.factory;

import com.paytm.pgplus.biz.workflow.service.ICreateOrderAndPay;
import com.paytm.pgplus.biz.workflow.service.IPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("payAndCopFactory")
public class PayAndCopFactory {

    private static final Map<String, IPay> payImplMap = new HashMap<>();
    private static final Map<String, ICreateOrderAndPay> copImplMap = new HashMap<>();

    @Autowired
    private List<IPay> payServices;

    @Autowired
    private List<ICreateOrderAndPay> copServices;

    @PostConstruct
    public void initPayAndCopFactory() {
        for (IPay service : payServices) {
            payImplMap.put(service.serviceType(), service);
        }
        for (ICreateOrderAndPay service : copServices) {
            copImplMap.put(service.serviceType(), service);
        }
    }

    public IPay getPayService(String type) {
        return payImplMap.get(type);
    }

    public ICreateOrderAndPay getCopService(String type) {
        return copImplMap.get(type);
    }
}
