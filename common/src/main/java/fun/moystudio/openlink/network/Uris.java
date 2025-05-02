package fun.moystudio.openlink.network;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris {
    public static final URI openidLoginUri,openFrpAPIUri,ipstackUri,weavatarUri,advertiseUri;
    static {
        try {
            openidLoginUri = new URI("https://account.naids.com/");
            openFrpAPIUri = new URI("https://api.openfrp.net/");
            ipstackUri = new URI("https://fcd09628a76b.aapq.net/ip");
            weavatarUri = new URI("https://weavatar.com/avatar/");
            advertiseUri = new URI("https://started.ink/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
