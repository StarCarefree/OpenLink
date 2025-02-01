package fun.moystudio.openlink.network;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris {
    public static final URI frpcDownloadUri;
    public static final URI frpcDownloadUri1;
    public static final URI openidLoginUri;
    public static final URI openFrpAPIUri;
    public static final URI ipstackUri;
    public static final URI weavatarUri;
    public static final URI advertiseUri;
    static {
        try {
            frpcDownloadUri = new URI("https://o.of.cd/client/");
            frpcDownloadUri1 = new URI("https://r.zyghit.cn/client/");
            openidLoginUri = new URI("https://openid.17a.ink/");
            openFrpAPIUri = new URI("https://api.openfrp.net/");
            ipstackUri = new URI("https://fcd09628a76b.aapq.net/ip");
            weavatarUri = new URI("https://weavatar.com/avatar/");
            advertiseUri = new URI("https://started.ink/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
