package cn.liberg.coder.tool.util;

import java.util.HashMap;
import java.util.Map;

public class Copier {

    public static Map<String, String> clone(Map<String, String> map) {
        Map<String, String> rt = null;
        if(map!=null) {
            rt = new HashMap<>();
            for(Map.Entry<String, String> entry : map.entrySet()) {
                rt.put(entry.getKey(), entry.getValue());
            }
        }
        return rt;
    }
}
