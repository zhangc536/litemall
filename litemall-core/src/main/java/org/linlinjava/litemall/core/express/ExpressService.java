package org.linlinjava.litemall.core.express;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.express.config.ExpressProperties;
import org.linlinjava.litemall.core.express.dao.ExpressInfo;
import org.linlinjava.litemall.core.express.dao.Traces;
import org.linlinjava.litemall.core.util.HttpUtil;
import org.springframework.util.Base64Utils;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class ExpressService {

    private final Log logger = LogFactory.getLog(ExpressService.class);

    private static final String API_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
    private static final String REQUEST_TYPE_QUERY = "8002";
    private static final String REQUEST_TYPE_MAP = "8003";

    private ExpressProperties properties;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ExpressProperties getProperties() {
        return properties;
    }

    public void setProperties(ExpressProperties properties) {
        this.properties = properties;
        logger.info("快递服务初始化：enable=" + properties.isEnable() + ", appId=" + properties.getAppId());
    }

    public String getVendorName(String vendorCode) {
        if (vendorCode == null) {
            return null;
        }
        for (Map<String, String> item : properties.getVendors()) {
            if (vendorCode.equals(item.get("code"))) {
                return item.get("name");
            }
        }
        return vendorCode;
    }

    public List<Map<String, String>> getVendors() {
        return properties.getVendors();
    }

    public ExpressInfo getExpressInfo(String expCode, String expNo) {
        return queryExpress(expCode, expNo, null);
    }

    public ExpressInfo queryExpress(String expCode, String expNo, String phoneTail) {
        if (!properties.isEnable()) {
            logger.warn("快递查询服务未启用");
            return createResult(false, "快递查询服务未启用", null);
        }

        if (expNo == null || expNo.trim().isEmpty()) {
            logger.error("快递单号为空");
            return createResult(false, "快递单号为空", null);
        }

        String appId = properties.getAppId();
        String appKey = properties.getAppKey();

        if (appId == null || appId.trim().isEmpty()) {
            logger.error("快递鸟appId未配置");
            return createResult(false, "快递查询服务配置错误", null);
        }

        if (appKey == null || appKey.trim().isEmpty()) {
            logger.error("快递鸟appKey未配置");
            return createResult(false, "快递查询服务配置错误", null);
        }

        try {
            Map<String, Object> requestData = new HashMap<>();
            if (expCode != null && !expCode.trim().isEmpty()) {
                requestData.put("ShipperCode", expCode);
            }
            requestData.put("LogisticCode", expNo);
            if (phoneTail != null && !phoneTail.trim().isEmpty()) {
                requestData.put("CustomerName", phoneTail);
            }

            String requestDataJson = objectMapper.writeValueAsString(requestData);
            logger.info("快递查询请求：expCode=" + expCode + ", expNo=" + expNo + ", phoneTail=" + phoneTail);

            String dataSign = generateSign(requestDataJson, appKey);

            Map<String, String> params = new HashMap<>();
            params.put("RequestData", URLEncoder.encode(requestDataJson, "UTF-8"));
            params.put("EBusinessID", appId);
            params.put("RequestType", REQUEST_TYPE_QUERY);
            params.put("DataSign", URLEncoder.encode(dataSign, "UTF-8"));
            params.put("DataType", "2");

            String response = HttpUtil.sendPost(API_URL, params);
            logger.info("快递鸟响应：" + response);

            ExpressInfo expressInfo = objectMapper.readValue(response, ExpressInfo.class);
            if (expressInfo != null) {
                expressInfo.setShipperName(getVendorName(expCode));
            }

            return expressInfo;

        } catch (Exception e) {
            logger.error("快递查询异常：" + e.getMessage(), e);
            return createResult(false, "快递查询异常：" + e.getMessage(), null);
        }
    }

    private String generateSign(String requestData, String appKey) throws Exception {
        String content = requestData + appKey;
        String md5Str = md5(content);
        return Base64Utils.encodeToString(md5Str.getBytes("UTF-8"));
    }

    private String md5(String content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(content.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder(32);
        for (byte b : digest) {
            int val = b & 0xff;
            if (val <= 0xf) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString().toLowerCase();
    }

    private ExpressInfo createResult(boolean success, String reason, List<Traces> traces) {
        ExpressInfo info = new ExpressInfo();
        info.setSuccess(success);
        info.setReason(reason);
        info.setTraces(traces != null ? traces : new ArrayList<>());
        return info;
    }

    public Map<String, Object> getMonitorInfo(String expCode, String expNo) {
        return null;
    }

    public Map<String, Object> getMapInfo(String expCode, String expNo, String senderCityName, 
            String receiverCityName, Integer isReturnCoordinates, Integer isReturnRouteMap) {
        if (!properties.isEnable()) {
            logger.warn("快递查询服务未启用");
            return createMapResult(false, "快递查询服务未启用", null);
        }

        if (expNo == null || expNo.trim().isEmpty()) {
            logger.error("快递单号为空");
            return createMapResult(false, "快递单号为空", null);
        }

        String appId = properties.getAppId();
        String appKey = properties.getAppKey();

        if (appId == null || appId.trim().isEmpty() || appKey == null || appKey.trim().isEmpty()) {
            logger.error("快递鸟配置错误");
            return createMapResult(false, "快递查询服务配置错误", null);
        }

        try {
            Map<String, Object> requestData = new HashMap<>();
            if (expCode != null && !expCode.trim().isEmpty()) {
                requestData.put("ShipperCode", expCode);
            }
            requestData.put("LogisticCode", expNo);
            if (senderCityName != null && !senderCityName.trim().isEmpty()) {
                requestData.put("SenderCityName", senderCityName);
            }
            if (receiverCityName != null && !receiverCityName.trim().isEmpty()) {
                requestData.put("ReceiverCityName", receiverCityName);
            }
            if (isReturnCoordinates != null) {
                requestData.put("IsReturnCoordinates", isReturnCoordinates);
            }
            if (isReturnRouteMap != null) {
                requestData.put("IsReturnRouteMap", isReturnRouteMap);
            }

            String requestDataJson = objectMapper.writeValueAsString(requestData);
            logger.info("物流轨迹地图查询请求：expCode=" + expCode + ", expNo=" + expNo);

            String dataSign = generateSign(requestDataJson, appKey);

            Map<String, String> params = new HashMap<>();
            params.put("RequestData", URLEncoder.encode(requestDataJson, "UTF-8"));
            params.put("EBusinessID", appId);
            params.put("RequestType", REQUEST_TYPE_MAP);
            params.put("DataSign", URLEncoder.encode(dataSign, "UTF-8"));
            params.put("DataType", "2");

            String response = HttpUtil.sendPost(API_URL, params);
            logger.info("物流轨迹地图响应：" + response);

            return objectMapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            logger.error("物流轨迹地图查询异常：" + e.getMessage(), e);
            return createMapResult(false, "查询异常：" + e.getMessage(), null);
        }
    }

    private Map<String, Object> createMapResult(boolean success, String reason, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("Success", success);
        result.put("Reason", reason);
        if (data != null) {
            result.put("Data", data);
        }
        return result;
    }

    public Map<String, Object> requestCustom(String requestType, Map<String, Object> requestData, String requestTarget) {
        logger.warn("requestCustom方法暂未实现");
        return null;
    }
}
