package fun.moystudio.openlink.json;

import java.util.Map;

public class JsonFrpcSakura extends JsonBaseResponseSakura{
    public client frpc;
    public static class client {
        public String ver;
        public long time;
        public Map<String,arch> archs;
        public static class arch {
            public String url;
        }
    }
}
