package fun.moystudio.openlink.network;

import java.net.URI;
import java.net.URISyntaxException;

public class Uris {
    public static final URI openidLoginUri,openFrpAPIUri,ipstackUri,weavatarUri,advertiseUri,wikiUri,sakuraFrpAPIUri;
    static {
        try {
            openidLoginUri = new URI("https://account.naids.com/");
            openFrpAPIUri = new URI("https://api.openfrp.net/");
            ipstackUri = new URI("http://ip-api.com/json/");
            weavatarUri = new URI("https://weavatar.com/avatar/");
            advertiseUri = new URI("https://started.ink/");
            wikiUri = new URI("https://scarefree.wiki/");
            sakuraFrpAPIUri = new URI("https://api.natfrp.com/v4/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
