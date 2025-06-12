package fun.moystudio.openlink.frpcimpl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.LoginScreenSakura;
import fun.moystudio.openlink.gui.NodeSelectionScreenSakura;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class SakuraFrpFrpcImpl implements Frpc {
    private final static Logger LOGGER = LogManager.getLogger(SakuraFrpFrpcImpl.class);
    private String osArch,osName,downloadUrl,latestVersion,frpcVersion;
    private long proxyId;
    public static String token;
    public static long nodeId = -1;
    public static final int MAX_TRAFFIC_STORAGE = 4;

    @Override
    public String id() {
        return "sakurafrp";
    }

    @Override
    public String name() {
        return "SakuraFrp";
    }

    @Override
    public ResourceLocation getIcon() {
        return Utils.createResourceLocation("openlink", "textures/gui/sakurafrp_icon.png");
    }

    @Override
    public void init() throws Exception{
        String os_arch=System.getProperty("os.arch").toLowerCase(),os_name=System.getProperty("os.name");
        if(os_arch.contains("i386")){
            os_arch="386";
        }
        if(os_name.contains("Windows")) {
            osName="windows";
        } else if (os_name.contains("OS X")) {
            osName="darwin";
            os_arch=os_arch.equals("x86_64")?"amd64":"arm64";
        } else if (os_name.contains("Linux")||os_name.contains("Unix")) {
            osName="linux";
        } else if (os_name.contains("FreeBSD")){
            osName="freebsd";
        } else {
            LOGGER.error("Unsupported operating system detected!");
            throw new Exception("[OpenLink] Unsupported operating system detected!");
        }
        osArch = os_arch;
        readSession();
    }

    @Override
    public boolean isOutdated(@Nullable Path frpcExecutableFilePath) {
        return checkUpdate(frpcExecutableFilePath);
    }

    @Override
    public List<String> getUpdateFileUrls() {
        return List.of(downloadUrl);
    }

    public static void readSession() {
        token=OpenLink.PREFERENCES.get("token_sakura",null);
        if(token==null||token.equals("null")){
            token=null;
            LOGGER.warn("The session does not exists in user preferences!");
            return;
        }
        try{
            JsonUserInfoSakura responseWithData = getUserInfo();
            if(responseWithData==null){
                token=null;
                writeSession();
                LOGGER.warn("The session has been expired!");
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLoggedIn() {
        return token!=null;
    }

    @Override
    public Screen getLoginScreen(Screen last) {
        return new LoginScreenSakura(last);
    }

    public static JsonUserInfoSakura getUserInfo() {
        Gson gson=new Gson();
        JsonUserInfoSakura response;
        try {
            response = gson.fromJson(Request.GET(Uris.sakuraFrpAPIUri+"user/info", getTokenHeader()).getFirst(), new TypeToken<JsonUserInfoSakura>(){}.getType());
            if(isBadResponse(response)) {
                LOGGER.error("Incorrect token!");
                return null;
            }
            if(!token.equals(response.token)) token=response.token;
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        }
        return response;
    }

    public static List<Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> getNodeList() {
        Gson gson = new Gson();
        Pair<String, Map<String, List<String>>> response;
        List<Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> result = new ArrayList<>();
        try {
            response=Request.GET(Uris.sakuraFrpAPIUri+"nodes", getTokenHeader());
            JsonNodesSakura nodes = gson.fromJson(response.getFirst(), new TypeToken<JsonNodesSakura>(){}.getType());
            response=Request.GET(Uris.sakuraFrpAPIUri+"node/stats", getTokenHeader());
            JsonNodeStatsSakura nodeStats = gson.fromJson(response.getFirst(), new TypeToken<JsonNodeStatsSakura>(){}.getType());
            Map<Long, Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> idToNode = new HashMap<>();
            for(Map.Entry<String,JsonNodesSakura.node> n:nodes.entrySet()) {
                long id = Long.parseLong(n.getKey());
                idToNode.put(id, Pair.of(n.getValue(), null));
            }
            for(JsonNodeStatsSakura.node_stat stat:nodeStats.nodes) {
                idToNode.put(stat.id,Pair.of(idToNode.get(stat.id).getFirst(),stat));
            }
            for(Map.Entry<Long,Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> entry:idToNode.entrySet()) {
                if(entry.getValue().getFirst()==null||entry.getValue().getSecond()==null) {
                    continue;
                }
                result.add(entry.getValue());
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void writeSession() {
        OpenLink.PREFERENCES.put("token_sakura", Objects.requireNonNullElse(token, "null"));
    }

    @Override
    public Screen getNodeSelectionScreen(Screen last) {
        return new NodeSelectionScreenSakura(last);
    }

    @Override
    public void logOut() {
        token = null;
        writeSession();
    }

    @Override
    public String getPanelUrl() {
        return "https://www.natfrp.com/user/";
    }

    public static boolean isBadResponse(JsonBaseResponseSakura response) {
        return response.code >= 400;
    }

    private boolean checkUpdate(Path path) {
        Gson gson=new Gson();
        JsonFrpcSakura response;
        try {
            response = gson.fromJson(Request.GET(Uris.sakuraFrpAPIUri+"system/clients", Request.DEFAULT_HEADER).getFirst(),new TypeToken<JsonFrpcSakura>(){}.getType());
            if(isBadResponse(response)) {
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }
        downloadUrl = response.frpc.archs.get(osName+"_"+osArch).url;
        boolean result = false;
        latestVersion=response.frpc.ver;
        if(path == null || !path.toFile().exists()){
            LOGGER.warn("The frpc executable file does not exist!");
            result = true;
        } else {
            getFrpcVersion(path);
            if(frpcVersion==null||!frpcVersion.equals(latestVersion)){
                LOGGER.info("A frpc update was found! Latest version:{} Old version:{}", latestVersion, frpcVersion);
                result = true;
            }
        }
        return result;
    }

    @Override
    public Process createFrpcProcess(Path frpcExecutableFilePath, int localPort, @Nullable String remotePort) throws Exception {
        nodeId=-1;
        List<String> list=new ArrayList<>(List.of(OpenLink.PREFERENCES.get("traffic_storage_sakura", "").split(";")));
        while(list.size()>=MAX_TRAFFIC_STORAGE){
            list.remove(0);
        }
        list.add(String.format(Locale.getDefault(),"%tD %tT",new Date(),new Date())+","+((long)(getUserInfo().traffic.get(1)/1048576F)));
        OpenLink.PREFERENCES.put("traffic_storage_sakura", String.join(";", list));
        return new ProcessBuilder(frpcExecutableFilePath.toFile().getAbsolutePath(), "-f",token+":"+proxyId,"-n","--disable_log_color").redirectErrorStream(true).start();
    }

    @Override
    public void stopFrpcProcess(Process process){
        if(process!=null) {
            process.destroy();
            if(token!=null) {
                Gson gson = new Gson();
                Pair<String, Map<String, List<String>>> response= null;
                try {
                    response = Request.GET(Uris.sakuraFrpAPIUri+"tunnels", getTokenHeader());
                } catch (Exception e) {
                    return;
                }
                JsonUserProxySakura userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonUserProxySakura>(){}.getType());
                if(JsonUserProxySakura.isBadResponse(userProxies)) {
                    return;
                }
                //OpenLink隧道命名规则：openlink_mc_[本地端口号]
                for(JsonUserProxySakura.tunnel userTunnel:userProxies) {
                    if(userTunnel.name.contains("openlink_mc_")){
                        try {
                            Request.POST(Uris.sakuraFrpAPIUri+"tunnel/delete", getTokenHeader(), "{\"ids\":\""+userTunnel.id+"\"}");
                            LOGGER.info("Deleted tunnel: {}",userTunnel.name);
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String createProxy(int localPort, @Nullable String remotePort) throws Exception {
        Gson gson = new Gson();
        if(SSLUtils.sslIgnored){
            //SSL警告
            Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.sslwarning"));
        }
        Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.creatingproxy"));
        Pair<String, Map<String, List<String>>> response=Request.GET(Uris.sakuraFrpAPIUri+"tunnels", getTokenHeader());
        JsonUserProxySakura userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonUserProxySakura>(){}.getType());
        if(JsonUserProxySakura.isBadResponse(userProxies)) {
            LOGGER.error("Cannot get the user tunnel list!");
            throw new Exception("Cannot get the user tunnel list!");
        }
        //OpenLink隧道命名规则：openlink_mc_[本地端口号]
        for(JsonUserProxySakura.tunnel userTunnel:userProxies) {
            if(userTunnel.name.contains("openlink_mc_")){
                try {
                    Request.POST(Uris.sakuraFrpAPIUri+"tunnel/delete", getTokenHeader(), "{\"ids\":\""+userTunnel.id+"\"}");
                    LOGGER.info("Deleted tunnel: {}",userTunnel.name);
                } catch (Exception e) {
                    break;
                }
            }
        }
        Thread.sleep(1000);
        response=Request.GET(Uris.sakuraFrpAPIUri+"tunnels", getTokenHeader());
        userProxies=gson.fromJson(response.getFirst(), new TypeToken<JsonUserProxySakura>(){}.getType());
        JsonUserInfoSakura userInfo = getUserInfo();
        if(userInfo.tunnels == userProxies.size()) {
            throw new Exception(Utils.translatableText("text.openlink.userproxieslimited").getString());
        }
        List<Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> nodeList = getNodeList();

        Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> nodePairToUse = null;
        for(Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> nodePair:nodeList) {
            if(nodePair.getSecond().id==nodeId)
                nodePairToUse = nodePair;
        }
        if(nodePairToUse == null) {
            LOGGER.info("Selecting node...");
            List<Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat>> canUseNodes = new ArrayList<>();
            for(Pair<JsonNodesSakura.node, JsonNodeStatsSakura.node_stat> nodePair:nodeList) {
                if(nodePair.getFirst().vip>userInfo.group.level||nodePair.getSecond().online!=0||(nodePair.getFirst().flag&(1<<2))==0) {
                    continue;
                }
                canUseNodes.add(nodePair);
            }
            if(canUseNodes.isEmpty()){
                throw new Exception("Unable to use any node???");
            }
            canUseNodes.sort((((o1, o2) -> {
                long inland = OpenLink.PREFER_CLASSIFY == 1 ? 1 : 0;
                inland<<=3;
                if(OpenLink.PREFER_CLASSIFY!=-1&&(o1.getFirst().flag&(1<<3))!=(o2.getFirst().flag&(1<<3)))
                    return inland==(o1.getFirst().flag&(1<<3))?-1:1;
                if(o1.getFirst().vip!=o2.getFirst().vip)
                    return o1.getFirst().vip>o2.getFirst().vip?-1:1;
                if(Math.abs(o1.getSecond().load-o2.getSecond().load)>1e-5)
                    return o1.getSecond().load>o2.getSecond().load?1:-1;
                return 0;
            })));
            nodePairToUse = canUseNodes.get(0);
        }
        LOGGER.info("Selected node: id:{} vip:{}",nodePairToUse.getSecond().id, nodePairToUse.getFirst().vip);
        JsonNewProxySakura newProxy = new JsonNewProxySakura();
        newProxy.name="openlink_mc_"+localPort;
        newProxy.node=nodePairToUse.getSecond().id;
        newProxy.local_port=String.valueOf(localPort);
        if(remotePort!=null) {
            newProxy.remote=remotePort;
        }
        response = Request.POST(Uris.sakuraFrpAPIUri+"tunnels",getTokenHeader(),gson.toJson(newProxy));
        JsonNewProxyResponseSakura newProxyResponse = gson.fromJson(response.getFirst(),new TypeToken<JsonNewProxyResponseSakura>(){}.getType());
        if(isBadResponse(newProxyResponse)) {
            throw new Exception("Cannot create the proxy! msg:"+newProxyResponse.msg);
        }
        proxyId = newProxyResponse.id;
        return nodePairToUse.getFirst().host+":"+newProxyResponse.remote;
    }

    public static Map<String,List<String>> getTokenHeader() {
        return Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER,"Bearer "+token);
    }

    @Override
    public String getFrpcVersion(Path frpcExecutableFilePath) {
        try {
            String version = new String(Runtime.getRuntime().exec(new String[]{frpcExecutableFilePath.toFile().getAbsolutePath(),"-v"}).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return frpcVersion = version.substring(0,version.length()-1);
        } catch (Exception e) {
            return "does not exists";
        }
    }
}
