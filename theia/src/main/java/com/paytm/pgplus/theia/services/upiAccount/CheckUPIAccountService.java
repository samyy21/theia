package com.paytm.pgplus.theia.services.upiAccount;

import com.paytm.pgplus.theia.models.upiAccount.request.CheckUPIAccountRequest;
import com.paytm.pgplus.theia.models.upiAccount.response.CheckUPIAccountResponse;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public interface CheckUPIAccountService {
    CheckUPIAccountResponse checkIfUPIAccountExistForUser(CheckUPIAccountRequest checkUPIAccountRequest);

}
