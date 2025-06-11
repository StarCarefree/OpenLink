package fun.moystudio.openlink.json;

import java.util.HashMap;

public class JsonNodesSakura extends HashMap<String, JsonNodesSakura.node> {
    public static class node {
        public String name,host,band,description;
        public long vip,flag;
    }
    public int code;
    public String msg;
    public static boolean isBadResponse(JsonNodesSakura sakura) {//因为不能扩展JsonBaseResponseSakura，只能用这个isBadResponse
        return sakura.code >= 400;
    }
}
