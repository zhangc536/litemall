package org.linlinjava.litemall.admin.util;

import java.util.HashMap;
import java.util.Map;

public class ResponseV1Util {
    public static Object ok() {
        Map<String, Object> obj = new HashMap<>(3);
        obj.put("code", 0);
        obj.put("message", "success");
        obj.put("data", null);
        return obj;
    }

    public static Object ok(Object data) {
        Map<String, Object> obj = new HashMap<>(3);
        obj.put("code", 0);
        obj.put("message", "success");
        obj.put("data", data);
        return obj;
    }

    public static Object fail(int code, String message) {
        Map<String, Object> obj = new HashMap<>(2);
        obj.put("code", code);
        obj.put("message", message);
        return obj;
    }
}
