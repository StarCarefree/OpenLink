package fun.moystudio.openlink.frpcimpl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.LoginScreen;
import fun.moystudio.openlink.gui.NodeSelectionScreen;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.logic.LanConfig;
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

public class OpenFrpFrpcImpl implements Frpc{
    private String frpcVersion = null, latestVersion = null, latestFolderName = "OF_0.61.1_4df06100_250122/";
    private String osArch,osName, archiveSuffix;
    private long proxyId;
    private final static Logger LOGGER = LogManager.getLogger(OpenFrpFrpcImpl.class);
    public static long nodeId = -1;
    public static final int MAX_TRAFFIC_STORAGE = 4;
    public static String Authorization = null,token = null;
    public List<String> downloadUrls = new ArrayList<>();

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
        if(osName.equals("windows")){
            archiveSuffix=".zip";
        } else {
            archiveSuffix=".tar.gz";
        }
        Gson gson = new Gson();
        JsonResponseWithData<JsonDownloadFile> response = gson.fromJson(Request.GET(Uris.openFrpAPIUri+"commonQuery/get?key=software", Request.DEFAULT_HEADER).getFirst(),new TypeToken<JsonResponseWithData<JsonDownloadFile>>(){}.getType());
        response.data.source.forEach(source -> {
            downloadUrls.add(source.value+"/");
        });
        readSession();
    }

    @Override
    public String id() {
        return "openfrp";
    }

    @Override
    public String name() {
        return "OpenFrp";
    }

    @Override
    public boolean isArchive() {
        return true;
    }

    @Override
    public boolean isOutdated(Path frpcExecutablePath) {
        return checkUpdate(frpcExecutablePath);
    }

    @Override
    public List<String> getUpdateFileUrls() {
        List<String> list = new ArrayList<>();
        downloadUrls.forEach(url -> list.add(url+latestFolderName+"frpc_"+osName+"_"+osArch+ archiveSuffix));
        return list;
    }

    @Override
    public Process createFrpcProcess(Path frpcExecutableFilePath, int localPort, @Nullable String remotePort) throws Exception{
        nodeId=-1;
        List<String> list=new ArrayList<>(List.of(OpenLink.PREFERENCES.get("traffic_storage", "").split(";")));
        while(list.size()>=MAX_TRAFFIC_STORAGE){
            list.remove(0);
        }
        list.add(String.format(Locale.getDefault(),"%tD %tT",new Date(),new Date())+","+getUserInfo().data.traffic);
        OpenLink.PREFERENCES.put("traffic_storage", String.join(";", list));
        return new ProcessBuilder(frpcExecutableFilePath.toFile().getAbsolutePath(),"-u",token,"-p",String.valueOf(proxyId),"-n","--disable_log_color").redirectErrorStream(true).start();
    }

    @Override
    public String createProxy(int localPort, @Nullable String remotePort) throws Exception {
        Gson gson=new Gson();
        if(SSLUtils.sslIgnored){
            //SSL警告
            Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.sslwarning"));
        }
        Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.creatingproxy"));
        Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
        JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
        //OpenLink隧道命名规则：openlink_mc_[本地端口号]
        for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
            if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                try {
                    Request.POST(Uris.openFrpAPIUri + "frp/api/forceOff", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization), "{\"proxy_id\":" + jsonUserProxy.id + "}");
                    Request.POST(Uris.openFrpAPIUri + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization), "{\"proxy_id\":" + jsonUserProxy.id + "}");
                    LOGGER.info("Deleted proxy: {}",jsonUserProxy.proxyName);
                } catch (Exception e) {
                    break;
                }
            }
        }//删除以前用过的隧道
        Thread.sleep(1000);
        response=Request.POST(Uris.openFrpAPIUri+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
        userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
        JsonResponseWithData<JsonUserInfo> userinfo=getUserInfo();
        if(userinfo.data.proxies==userProxies.data.total){
            throw new Exception(Utils.translatableText("text.openlink.userproxieslimited").getString());
        }
        JsonResponseWithData<JsonTotalAndList<JsonNode>> nodelist=getNodeList();
        JsonNode node=null;
        for (JsonNode node1:nodelist.data.list){
            if(node1.id==nodeId){
                node=node1;
                break;
            }
        }
        if(node==null){
            LOGGER.info("Selecting node...");
            List<JsonNode> canUseNodes=new ArrayList<>();
            for(JsonNode now:nodelist.data.list){
                int groupnumber1=0,usergroupnumber=0;
                if(now.group.contains("svip")){
                    groupnumber1=3;
                }
                else if(now.group.contains("vip")){
                    groupnumber1=2;
                }
                else if(now.group.contains("normal")){
                    groupnumber1=1;
                }
                if(userinfo.data.group.contains("svip")){
                    usergroupnumber=3;
                }else if(userinfo.data.group.contains("vip")){
                    usergroupnumber=2;
                }else if(userinfo.data.group.contains("normal")){
                    usergroupnumber=1;
                }else if(userinfo.data.group.contains("dev")){
                    usergroupnumber=5;
                }
                if(groupnumber1>usergroupnumber||!now.protocolSupport.tcp||now.status!=200||now.fullyLoaded||(now.needRealname&&!userinfo.data.realname)){
                    continue;
                }
                canUseNodes.add(now);
            }
            if(canUseNodes.isEmpty()){
                throw new Exception("Unable to use any node???");
            }
            canUseNodes.sort(((o1, o2) -> {
                if(OpenLink.PREFER_CLASSIFY!=-1&&o1.classify!=o2.classify&&(o1.classify== OpenLink.PREFER_CLASSIFY)!=(o2.classify==OpenLink.PREFER_CLASSIFY))
                    return o1.classify==OpenLink.PREFER_CLASSIFY?-1:1;
                if(!o1.group.equals(o2.group)){
                    int first=0,second=0;
                    if(o1.group.contains("svip")){
                        first=3;
                    }
                    else if(o1.group.contains("vip")){
                        first=2;
                    }
                    else if(o1.group.contains("normal")){
                        first=1;
                    }
                    if(o2.group.contains("svip")){
                        second=3;
                    }
                    else if(o2.group.contains("vip")) {
                        second=2;
                    }
                    else if(o2.group.contains("normal")){
                        second=1;
                    }
                    if(first != second){
                        return first>second?-1:1;
                    }
                }
                if(Math.abs(o1.bandwidth*o1.bandwidthMagnification-o2.bandwidth*o2.bandwidthMagnification)>1e-5)
                    return Double.compare(o2.bandwidth*o2.bandwidthMagnification,o1.bandwidth*o1.bandwidthMagnification);
                if(userinfo.data.realname&&o1.needRealname!=o2.needRealname)
                    return o1.needRealname?-1:1;
                return 0;
            }));
            node=canUseNodes.get(0);//选取最优节点
        }
        LOGGER.info("Selected node: id:{} allow_port:{} group:{}",node.id,node.allowPort,node.group);
        JsonNewProxy newProxy=new JsonNewProxy();
        newProxy.name="openlink_mc_"+localPort;
        newProxy.local_port=String.valueOf(localPort);
        newProxy.node_id=node.id;
        Random random=new Random();
        int start,end;
        if(node.allowPort==null||node.allowPort.isBlank()){
            start=30000;
            end=60000;
        }
        else{
            start=Integer.parseInt(node.allowPort.substring(1,6));
            end=Integer.parseInt(node.allowPort.substring(7,12));
        }
        boolean found=false;
        for (int j = 1; j <= 5; j++) {
            newProxy.remote_port = random.nextInt(end - start + 1) + start;
            if(remotePort !=null&&!remotePort.isBlank()&&j==1){
                newProxy.remote_port=Integer.parseInt(remotePort);
            }
            response=Request.POST(Uris.openFrpAPIUri+ "frp/api/newProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization), gson.toJson(newProxy));
            LOGGER.info("Try {}: remote_port:{} flag:{} msg:{}",j,remotePort,gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag,gson.fromJson(response.getFirst(), JsonResponseWithData.class).msg);
            if(gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag){
                found=true;
                break;
            }
        }//创建隧道
        if(!found) throw new Exception(Utils.translatableText("text.openlink.remoteportnotfound").getString() + "last msg:" +gson.fromJson(response.getFirst(), JsonResponseWithData.class).msg);
        LanConfig.cfg.last_port_value=String.valueOf(newProxy.remote_port).equals(remotePort)?remotePort:"";
        response=Request.POST(Uris.openFrpAPIUri+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
        userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
        JsonUserProxy runningproxy=null;
        for(JsonUserProxy jsonUserProxy:userProxies.data.list){
            if(jsonUserProxy.proxyName.equals("openlink_mc_"+localPort)){
                runningproxy=jsonUserProxy;
                break;
            }
        }
        if(runningproxy==null) throw new Exception("Can not find the proxy???");
        nodeId=node.id;
        proxyId=runningproxy.id;
        return runningproxy.connectAddress;
    }

    @Override
    public String getFrpcVersion(Path frpcExecutableFilePath) {
        try {
            return frpcVersion = new String(Runtime.getRuntime().exec(new String[]{frpcExecutableFilePath.toFile().getAbsolutePath(),"-v"}).getInputStream().readAllBytes(), StandardCharsets.UTF_8).split("_")[1];
        } catch (Exception e) {
            return "does not exists";
        }
    }

    @Override
    public void logOut() {
        Authorization = null;
        writeSession();
    }

    private boolean checkUpdate(Path path) {
        Gson gson=new Gson();
        JsonResponseWithData<JsonDownloadFile> frpcVersionJson;
        try {
            frpcVersionJson = gson.fromJson(Request.GET(Uris.openFrpAPIUri+"commonQuery/get?key=software",Request.DEFAULT_HEADER).getFirst(),new TypeToken<JsonResponseWithData<JsonDownloadFile>>(){}.getType());
        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }
        boolean result = false;
        latestVersion=frpcVersionJson.data.latest_ver;
        latestFolderName=frpcVersionJson.data.latest_full+"/";
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
    public void stopFrpcProcess(Process process){
        if(process!=null){
            process.destroy();
            if (Authorization != null){
                try {
                    Pair<String, Map<String, List<String>>> response = Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
                    Gson gson=new Gson();
                    JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                    for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                        if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                            try {
                                Request.POST(Uris.openFrpAPIUri + "frp/api/forceOff", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization), "{\"proxy_id\":" + jsonUserProxy.id + "}");
                                Request.POST(Uris.openFrpAPIUri + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization), "{\"proxy_id\":" + jsonUserProxy.id + "}");
                                LOGGER.info("Deleted proxy: {}",jsonUserProxy.proxyName);
                            } catch (Exception e) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    @Override
    public String getPanelUrl() {
        return "https://console.openfrp.net/fastlogin?auth="+Authorization;
    }

    @Override
    public Screen getNodeSelectionScreen(Screen lastScreen) {
        return new NodeSelectionScreen(lastScreen);
    }

    @Override
    public Screen getLoginScreen(Screen lastScreen) {
        return new LoginScreen(lastScreen);
    }

    @Override
    public ResourceLocation getIcon() {
        return Utils.createResourceLocation("openlink", "textures/gui/openfrp_icon.png");
    }

    @Override
    public boolean isLoggedIn() {
        return Authorization!=null;
    }

    public static void writeSession() {
        OpenLink.PREFERENCES.put("authorization", Objects.requireNonNullElse(Authorization, "null"));
    }

    public static void readSession() {
        Authorization=OpenLink.PREFERENCES.get("authorization",null);

        if(Authorization==null||Authorization.equals("null")){
            Authorization=null;
            LOGGER.warn("The session does not exists in user preferences!");
            return;
        }
        try{
            JsonResponseWithData<JsonUserInfo> responseWithData = getUserInfo();
            if(responseWithData==null||!responseWithData.flag){
                Authorization=null;
                writeSession();
                LOGGER.warn("The session has been expired!");
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static JsonResponseWithData<JsonUserInfo> getUserInfo() throws Exception {
        if(Authorization==null) return null;
        Gson gson=new Gson();
        Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserInfo",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
        JsonResponseWithData<JsonUserInfo> res=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonUserInfo>>(){}.getType());
        if(res.data!=null)
            token=res.data.token;
        return res;
    }

    public static JsonResponseWithData<JsonTotalAndList<JsonNode>> getNodeList() throws Exception {
        if(Authorization==null) return null;
        Gson gson=new Gson();
        Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getNodeList",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER, OpenFrpFrpcImpl.Authorization),"{}");
        return gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonNode>>>(){}.getType());
    }
}
