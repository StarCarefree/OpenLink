package fun.moystudio.openlink.json;

import java.util.List;

public class JsonNodeStatsSakura extends JsonBaseResponseSakura {
    public long time;
    public List<node_stat> nodes;
    public static class node_stat {
        public long id, online, uptime;
        public float load;
    }
}
