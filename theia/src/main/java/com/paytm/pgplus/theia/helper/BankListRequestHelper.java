package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.BankListRequest;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.viewmodel.BankInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.common.enums.ETerminalType.getTerminalTypeByTerminal;

public class BankListRequestHelper {

    private static final String PARAM_IDENTIFIER = "@";
    private static final String REQ_PARMTR_JSON = "JsonData";
    private static final String VAL_PF_MID = "VAER-PF101";

    private static String getJSONDecodedData(Map<String, String[]> requestParams) throws UnsupportedEncodingException {

        String jsondata = getParameter(REQ_PARMTR_JSON, requestParams);
        String urlDecodedData = null;
        if (jsondata != null)
            urlDecodedData = URLDecoder.decode(jsondata, TheiaConstant.ExtraConstants.ENCODING_SCHEME_UTF_8);
        return urlDecodedData;

    }

    public static BankListRequest createBankListRequest(HttpServletRequest request) throws IOException {
        Map<String, String[]> requestParams = constructRequestParams(request);
        String jsonData = getJSONDecodedData(requestParams);
        BankListRequest bankListRequest = new BankListRequest();

        if (jsonData == null)
            return bankListRequest;

        JSONObject jsonObject = new JSONObject(jsonData);

        if (jsonObject.has("MID")) {

            String mId = jsonObject.getString("MID");
            if (null != mId && mId.length() > 0) {
                bankListRequest.setMid(mId);
            } else {
                bankListRequest.setErrorMsg("PF_MID IS REQUIRED");
                bankListRequest.setErrorCode(VAL_PF_MID);
            }
        } else {
            bankListRequest.setErrorMsg("PF_MID IS REQUIRED");
            bankListRequest.setErrorCode(VAL_PF_MID);

        }

        if (jsonObject.has("INDUSTRY")) {

            String industry = jsonObject.getString("INDUSTRY");
            if (null != industry && industry.length() > 0) {
                bankListRequest.setIndustry(industry);
            } else {
                bankListRequest.setErrorMsg("INDUSTRY IS REQUIRED");
            }

        }
        if (jsonObject.has("CHANNEL")) {

            String channel = jsonObject.getString("CHANNEL");
            if (null != channel && channel.length() > 0) {
                bankListRequest.setChannel(channel);
            } else {
                bankListRequest.setErrorMsg("CHANNEL IS REQUIRED");
            }

        } else {
            bankListRequest.setErrorMsg("CHANNEL IS REQUIRED");
        }

        return bankListRequest;

    }

    private static Map<String, String[]> constructRequestParams(HttpServletRequest httpServletRequest)
            throws IOException {

        Map<String, String[]> requestParams = new HashMap<>();

        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            requestParams.put(headerName, new String[] { httpServletRequest.getHeader(headerName) });
        }

        for (Map.Entry<String, String[]> entry : httpServletRequest.getParameterMap().entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            key = URLEncoder.encode(key, TheiaConstant.ExtraConstants.ENCODING_SCHEME_UTF_8);
            value[0] = URLEncoder.encode(value[0], TheiaConstant.ExtraConstants.ENCODING_SCHEME_UTF_8);
            requestParams.put(PARAM_IDENTIFIER.concat(key), value);
        }

        // Request body params
        String requestBody;
        ServletInputStream inputStream = httpServletRequest.getInputStream();
        if (inputStream != null) {
            requestBody = IOUtils.toString(httpServletRequest.getInputStream());

            if (StringUtils.isNotBlank(requestBody)) {

                String[] params = requestBody.split("\\&");
                for (String param : params) {
                    String[] paramValue = new String[1];
                    String[] tokens = param.split("=", 2);
                    switch (tokens.length) {
                    case 0:
                        requestParams.put(PARAM_IDENTIFIER.concat(param), new String[] {});
                        break;
                    case 1:
                        requestParams.put(PARAM_IDENTIFIER.concat(tokens[0]), new String[] {});
                        break;
                    default:
                        String paramName = PARAM_IDENTIFIER.concat(tokens[0]);
                        if (requestParams.containsKey(paramName)) {
                            paramValue = requestParams.get(paramName);
                            String[] tmp = new String[paramValue.length + 1];
                            for (int j = 0; j < paramValue.length; j++) {
                                tmp[j] = paramValue[j];
                            }
                            paramValue = tmp;
                        }
                        paramValue[paramValue.length - 1] = tokens[1].trim();
                        requestParams.put(paramName, paramValue);
                    }
                }

            }

        }

        return requestParams;

    }

    private static String getParameter(String name, Map<String, String[]> requestParams) {
        String[] param = requestParams.get("@".concat(name));
        if (param != null && param.length > 0) {
            return param[0] == null ? "" : param[0];
        }
        return null;
    }

    public static EnvInfoRequestBean createEnvInfo(HttpServletRequest request, String channel) {
        EnvInfoRequestBean envInfoRequestBean = EnvInfoUtil.fetchEnvInfo(request);
        ETerminalType terminalType = getTerminalTypeByTerminal(channel);
        if (envInfoRequestBean != null && terminalType != null) {
            envInfoRequestBean.setTerminalType(terminalType);
        }

        return envInfoRequestBean;
    }

    public static String getBankList(List<BankInfo> bankInfos) {
        JSONObject jsonObject = getJSONBankList(bankInfos);
        return jsonObject.toString();
    }

    private static JSONObject getJSONBankList(List<BankInfo> bankInfos) {
        JSONObject jsonObject = new JSONObject();
        JSONObject bankJsonObj;
        JSONArray jsonArray = new JSONArray();

        if (bankInfos == null || bankInfos.isEmpty()) {
            bankJsonObj = new JSONObject();
            bankJsonObj.accumulate("BANK_CODE", "");
            bankJsonObj.accumulate("BANK_NAME", "");
            bankJsonObj.accumulate("IS_ATM", "");
            bankJsonObj.accumulate("ACTIVE", "");
            jsonArray.put(bankJsonObj);
        } else {
            for (BankInfo bankInfo : bankInfos) {
                bankJsonObj = new JSONObject();
                bankJsonObj.accumulate("BANK_CODE", bankInfo.getBankName());
                bankJsonObj.accumulate("BANK_NAME", bankInfo.getDisplayName());
                bankJsonObj.accumulate("IS_ATM", BooleanUtils.toBoolean((int) bankInfo.getIsAtm()));
                jsonArray.put(bankJsonObj);
            }
        }
        jsonObject.put("BANK_LIST", jsonArray);
        return jsonObject;
    }

}
