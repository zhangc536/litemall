package org.linlinjava.litemall.core.express;

import com.fasterxml.jackson.core.type.TypeReference;
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

    private static final String REQ_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
    private static final String DIST_URL = "https://api.kdniao.com/api/dist";
    private static final String REQUEST_TYPE_TRACK = "1002";
    private static final String REQUEST_TYPE_QUERY = "8002";
    private static final String REQUEST_TYPE_MONITOR = "8001";

    private ExpressProperties properties;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ExpressProperties getProperties() {
        return properties;
    }

    public void setProperties(ExpressProperties properties) {
        this.properties = properties;
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
        return getExpressInfo(expCode, expNo, null);
    }

    public ExpressInfo getExpressInfo(String expCode, String expNo, String phoneTail) {
        if (!properties.isEnable()) {
            logger.warn("物流查询服务未启用");
            return createDisabledResult();
        }

        if (expCode == null || expCode.trim().isEmpty()) {
            logger.error("快递公司代码为空");
            return createErrorResult("快递公司代码为空");
        }

        if (expNo == null || expNo.trim().isEmpty()) {
            logger.error("快递单号为空");
            return createErrorResult("快递单号为空");
        }

        try {
            logger.info("开始查询物流信息：快递公司=" + expCode + "(" + getVendorName(expCode) + "), 快递单号=" + expNo + ", 手机尾号=" + phoneTail);

            String result = queryExpressApi(expCode, expNo, phoneTail);
            logger.info("快递鸟API返回：" + result);

            ExpressInfo expressInfo = objectMapper.readValue(result, ExpressInfo.class);

            if (expressInfo == null) {
                logger.error("物流信息解析失败：返回结果为空");
                return createErrorResult("物流信息解析失败");
            }

            expressInfo.setShipperName(getVendorName(expCode));

            if (!expressInfo.getSuccess()) {
                String reason = expressInfo.getReason();
                logger.error("快递鸟API返回失败：" + reason);
                return createErrorResult(reason != null ? reason : "查询失败");
            }

            List<Traces> traces = expressInfo.getTraces();
            if (traces == null || traces.isEmpty()) {
                logger.info("暂无物流轨迹信息");
            } else {
                logger.info("查询到 " + traces.size() + " 条物流轨迹");
            }

            return expressInfo;

        } catch (Exception e) {
            logger.error("物流查询异常：" + e.getMessage(), e);
            return createErrorResult("物流查询异常：" + e.getMessage());
        }
    }

    private String queryExpressApi(String expCode, String expNo, String phoneTail) throws Exception {
        if (phoneTail != null && !phoneTail.trim().isEmpty()) {
            return queryDistApi(expCode, expNo, phoneTail);
        } else {
            return queryTrackApi(expCode, expNo);
        }
    }

    private String queryDistApi(String expCode, String expNo, String phoneTail) throws Exception {
        Map<String, String> requestDataMap = new HashMap<>();
        requestDataMap.put("ShipperCode", expCode);
        requestDataMap.put("LogisticCode", expNo);
        requestDataMap.put("CustomerName", phoneTail);
        String requestData = objectMapper.writeValueAsString(requestDataMap);

        Map<String, String> params = new HashMap<>();
        params.put("RequestData", URLEncoder.encode(requestData, "UTF-8"));
        params.put("EBusinessID", properties.getAppId());
        params.put("RequestType", REQUEST_TYPE_QUERY);
        params.put("DataSign", URLEncoder.encode(generateDataSign(requestData), "UTF-8"));
        params.put("DataType", "2");

        logger.info("快递查询API(8002)请求：" + REQ_URL);
        return HttpUtil.sendPost(REQ_URL, params);
    }

    private String queryTrackApi(String expCode, String expNo) throws Exception {
        String requestData = buildRequestData(expCode, expNo);

        Map<String, String> params = new HashMap<>();
        params.put("RequestData", URLEncoder.encode(requestData, "UTF-8"));
        params.put("EBusinessID", properties.getAppId());
        params.put("RequestType", REQUEST_TYPE_TRACK);
        params.put("DataSign", URLEncoder.encode(generateDataSign(requestData), "UTF-8"));
        params.put("DataType", "2");

        logger.info("即时查询API(1002)请求：" + REQ_URL);
        return HttpUtil.sendPost(REQ_URL, params);
    }

    private String queryExpressApi(String expCode, String expNo) throws Exception {
        return queryTrackApi(expCode, expNo);
    }

    private String buildRequestData(String expCode, String expNo) {
        return "{'OrderCode':'','ShipperCode':'" + expCode + "','LogisticCode':'" + expNo + "'}";
    }

    private String generateDataSign(String requestData) throws Exception {
        String content = requestData + properties.getAppKey();
        return Base64Utils.encodeToString(md5(content).getBytes("UTF-8"));
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

    private ExpressInfo createDisabledResult() {
        ExpressInfo info = new ExpressInfo();
        info.setSuccess(false);
        info.setReason("物流查询服务未启用");
        info.setTraces(new ArrayList<>());
        return info;
    }

    private ExpressInfo createErrorResult(String reason) {
        ExpressInfo info = new ExpressInfo();
        info.setSuccess(false);
        info.setReason(reason);
        info.setTraces(new ArrayList<>());
        return info;
    }

    public Map<String, Object> getMonitorInfo(String expCode, String expNo) {
        if (!properties.isEnable()) {
            logger.warn("物流查询服务未启用");
            return null;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("OrderCode", "");
            request.put("ShipperCode", expCode);
            request.put("LogisticCode", expNo);
            String requestData = objectMapper.writeValueAsString(request);
            return doRequest(REQUEST_TYPE_MONITOR, requestData);
        } catch (Exception e) {
            logger.error("物流监控查询异常：" + e.getMessage(), e);
        }
        return null;
    }

    private Map<String, Object> doRequest(String requestType, String requestData) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("RequestData", URLEncoder.encode(requestData, "UTF-8"));
        params.put("EBusinessID", properties.getAppId());
        params.put("RequestType", requestType);
        params.put("DataSign", URLEncoder.encode(generateDataSign(requestData), "UTF-8"));
        params.put("DataType", "2");

        String result = HttpUtil.sendPost(REQ_URL, params);
        return objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {});
    }
}
