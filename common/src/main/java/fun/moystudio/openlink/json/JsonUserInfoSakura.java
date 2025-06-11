package fun.moystudio.openlink.json;

import java.util.List;

public class JsonUserInfoSakura extends JsonBaseResponseSakura{
    public String name, avatar, token, speed;
    public long id, tunnels, realname;
    public group group;
    public List<Long> traffic;
    public static class group {
        public String name;
        public long level,expires;
    }
}
