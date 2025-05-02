package fun.moystudio.openlink.json;

import java.util.List;

public class JsonDownloadFile {
    public String latest_full;
    public String latest_ver;
    public List<source> source;
    public static class source {
        public String label,value;
    }
}
