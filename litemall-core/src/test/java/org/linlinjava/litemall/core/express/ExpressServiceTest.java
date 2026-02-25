package org.linlinjava.litemall.core.express;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Base64Utils;

public class ExpressServiceTest {

    private static final String REQ_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
    private static final String APP_ID = "1912788";
    private static final String APP_KEY = "3e1ce226-8749-4266-8cef-bb3e6234ec4c";

    public static void main(String[] args) {
        try {
            String expCode = "ZTO";
            String expNo = "73596830956390";

            System.out.println("========== 快递鸟API测试 ==========");
            System.out.println("快递公司代码: " + expCode);
            System.out.println("快递单号: " + expNo);
            System.out.println("APP ID: " + APP_ID);
            System.out.println("APP Key: " + APP_KEY);
            System.out.println("==================================");

            String requestData = "{'OrderCode':'','ShipperCode':'" + expCode + "','LogisticCode':'" + expNo + "'}";
            System.out.println("请求数据: " + requestData);

            String dataSign = generateDataSign(requestData);
            System.out.println("数据签名: " + dataSign);

            Map<String, String> params = new HashMap<>();
            params.put("RequestData", URLEncoder.encode(requestData, "UTF-8"));
            params.put("EBusinessID", APP_ID);
            params.put("RequestType", "1002");
            params.put("DataSign", URLEncoder.encode(dataSign, "UTF-8"));
            params.put("DataType", "2");

            System.out.println("\n请求参数:");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                System.out.println("  " + entry.getKey() + " = " + entry.getValue());
            }

            System.out.println("\n发送请求...");
            String result = HttpUtil.sendPost(REQ_URL, params);
            System.out.println("\n========== 响应结果 ==========");
            System.out.println(result);
            System.out.println("==============================");

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateDataSign(String requestData) throws Exception {
        String content = requestData + APP_KEY;
        System.out.println("签名原文: " + content);
        String md5Str = md5(content);
        System.out.println("MD5结果: " + md5Str);
        return Base64Utils.encodeToString(md5Str.getBytes("UTF-8"));
    }

    private static String md5(String content) throws Exception {
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
}
