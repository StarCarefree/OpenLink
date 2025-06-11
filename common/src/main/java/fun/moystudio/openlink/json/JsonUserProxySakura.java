package fun.moystudio.openlink.json;

import java.util.ArrayList;

public class JsonUserProxySakura extends ArrayList<JsonUserProxySakura.tunnel>  {
    public static class tunnel {
        public long id, node, status;
        public String remote,type,name;
        public boolean online;
    }
    public int code;
    public String msg;
    public static boolean isBadResponse(JsonUserProxySakura sakura) {//因为不能扩展JsonBaseResponseSakura，只能用这个isBadResponse
        return sakura.code >= 400;
    }
}
