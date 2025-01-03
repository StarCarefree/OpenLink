package fun.moystudio.openlink.frpc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.*;
import fun.moystudio.openlink.logic.Extract;
import fun.moystudio.openlink.logic.LanConfig;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.SSLUtils;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Frpc {
    public static final String DEFAULT_FOLDER_NAME = "OpenFRP_0.61.0_f4d251cc_20241126/";
    public static final int MAX_BUFFER_SIZE = 10485760;
    public static final int MAX_TRAFFIC_STORAGE = 4;
    private static String suffix = "";
    private static String zsuffix = ".tar.gz";
    public static String osName;
    public static String osArch;
    public static boolean hasUpdate = false;
    public static int frpcVersionDate = 0;
    public static String folderName = DEFAULT_FOLDER_NAME;
    public static final File frpcVersionFile = new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc.json");
    public static File frpcExecutableFile;
    public static File frpcArchiveFile;
    public static int latestVersionDate = 0;
    public static String latestVersion = "0";
    public static String FRPC_VERSION="0";
    public static Process runtimeProcess = null;


    public static void init() throws Exception {
        Gson gson=new Gson();
        if(!frpcVersionFile.exists()){
            OpenLink.LOGGER.warn("frpc.json(frpc version file) does not exist, creating...");
            frpcVersionFile.createNewFile();
            try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
                frpcVersionFileOutput.write("{\"versiondate\":0,\"version\":\"0\"}".getBytes());
            }
            OpenLink.LOGGER.info("Created frpc.json(frpc version file)!");
        }
        try(FileInputStream frpcVersionFileInput = new FileInputStream(frpcVersionFile)){
            JsonFrpcVersion frpcVersion=gson.fromJson(new String(frpcVersionFileInput.readAllBytes(),"utf-8"), JsonFrpcVersion.class);
            if(frpcVersion.version==null){
                try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
                    frpcVersionFileOutput.write("{\"versiondate\":0,\"version\":\"0\"}".getBytes());
                }
                frpcVersion.version="0";
                frpcVersion.versiondate=0;
            }
            frpcVersionDate=frpcVersion.versiondate;
            FRPC_VERSION=frpcVersion.version;
        }

        String os_name=System.getProperty("os.name");
        osArch=System.getProperty("os.arch").toLowerCase();
        if(osArch.contains("i386")){
            osArch="386";
        }
        if(os_name.contains("Windows")) {
            osName="windows";
        } else if (os_name.contains("OS X")) {
            osName="darwin";
        } else if (os_name.contains("Linux")||os_name.contains("Unix")) {
            osName="linux";
        } else if (os_name.contains("FreeBSD")){
            osName="freebsd";
        } else {
            OpenLink.LOGGER.error("Unsupported operating system detected!");
            throw new RuntimeException("[OpenLink] Unsupported operating system detected!");
        }
        if(osName.equals("windows")){
            suffix=".exe";
            zsuffix=".zip";
        }
        frpcExecutableFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc_"+osName+"_"+osArch+suffix);
        frpcArchiveFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"frpc"+zsuffix);
        if(checkUpdate()){
            OpenLink.LOGGER.info("The update screen will show after the main game screen loaded.");
        }
    }

    public static void update() throws Exception {
        boolean oofcd=downloadFrpcByUrl(Uris.frpcDownloadUri.toString()+folderName+"frpc_"+osName+"_"+osArch+zsuffix);
        if(!oofcd){
            boolean zyghit=downloadFrpcByUrl(Uris.frpcDownloadUri1.toString()+folderName+"frpc_"+osName+"_"+osArch+zsuffix);
            if(!zyghit){
                OpenLink.LOGGER.error("Can not download frpc! Stopping...");
                throw new RuntimeException("[OpenLink] Can not download frpc!");
            }
        }
        OpenLink.LOGGER.info("Extracting frpc archive file...");
        Extract.ExtractBySuffix(frpcArchiveFile.getAbsoluteFile(),zsuffix);
        OpenLink.LOGGER.info("Extracted frpc archive file sucessfully!");
        frpcArchiveFile.delete();
        OpenLink.LOGGER.info("Deleted frpc archive file!");
        frpcVersionDate=latestVersionDate;
        FRPC_VERSION=latestVersion;
        try (FileOutputStream frpcVersionFileOutput = new FileOutputStream(frpcVersionFile)){
            frpcVersionFileOutput.write(("{\"versiondate\":"+ Integer.toString(latestVersionDate) +",\"version\":\""+FRPC_VERSION+"\"}").getBytes());
        }
        hasUpdate=false;
    }

    public static int getLatestVersionDate() throws Exception{//这玩意是手写的POST(暂时不用后面写的logic包里的POST，因为这个是检测用的)
        Gson gson=new Gson();
        JsonResponseWithData<JsonDownloadFile> versiondateres = gson.fromJson(Request.GET(Uris.openFrpAPIUri+"commonQuery/get?key=software",Request.DEFAULT_HEADER),new TypeToken<JsonResponseWithData<JsonDownloadFile>>(){}.getType());
        String version=versiondateres.data.latest_full.substring(24);
        folderName=versiondateres.data.latest_full+"/";
        latestVersion=versiondateres.data.latest_ver;
        return Integer.parseInt(version);
    }

    public static boolean checkUpdate() throws Exception {
        latestVersionDate=getLatestVersionDate();

        if(!frpcExecutableFile.exists()||frpcVersionDate<latestVersionDate){
            hasUpdate=true;
            if(!frpcExecutableFile.exists()){
                OpenLink.LOGGER.warn("Frpc Executable File does not exist!");
            } else {
                OpenLink.LOGGER.info("A frpc update was found! Latest version date:"+Integer.toString(latestVersionDate)+" Old version date:"+Integer.toString(frpcVersionDate));
            }
            return true;
        }
        hasUpdate=false;
        return false;
    }

    private static boolean downloadFrpcByUrl(String str) throws InterruptedException {
        AtomicBoolean success= new AtomicBoolean(true);
        Thread thread=new Thread(()->{
            OpenLink.LOGGER.info("Downloading/Updating frpc from "+str+"...");
            try {
                URL url = new URL(str);
                String fileName = "frpc" + zsuffix;
                BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
                FileOutputStream outputStream = new FileOutputStream(frpcArchiveFile);
                byte[] buffer = new byte[MAX_BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer, 0, MAX_BUFFER_SIZE)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
                outputStream.close();
                OpenLink.LOGGER.info("Download/Update frpc sucessfully!");
            } catch (Exception e){
                success.set(false);
                e.printStackTrace();
            }
        },"Frpc download thread");
        thread.start();
        thread.join();
        return success.get();
    }

    public static void runFrpc(long proxyid) throws Exception {
        // 世界名称
        // 日期
        // 启动时间
        // 隧道ID
        // Frp服务提供商名称
        LocalTime localTime = LocalTime.now();
        LocalDate localDate = LocalDate.now();
        File logFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"logs"+File.separator+
                localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"_"+
                localTime.getHour()+"."+localTime.getMinute()+"."+localTime.getSecond()+"_"+
                Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()+".log");
        OpenLink.LOGGER.info("Frpc Log File Path:"+logFile);
        logFile.createNewFile();
        new FileOutputStream(logFile).write((Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()+"\n"+
                localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"\n"+
                localTime.format(DateTimeFormatter.ofPattern("hh:mm:ss"))+"\n"+
                proxyid+"\n"+
                "OpenFrp"+"\n"
        ).getBytes("utf-8"));
        Request.getUserInfo();
        runtimeProcess=new ProcessBuilder(new String[]{frpcExecutableFile.getAbsolutePath(),"-u",Request.token,"-p",String.valueOf(proxyid)}).redirectErrorStream(true).start();
        new Thread(()-> {
            try {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()))) {
                    String line;
                    FileOutputStream fo=new FileOutputStream(logFile,true);
                    while ((line = reader.readLine()) != null) {
                        fo.write("\n".getBytes("utf-8"));
                        String[] parts = line.split("\u001B\\[");
                        for(String part:parts) {
                            if(part.isEmpty()) {
                                continue;
                            }
                            String text = part.substring(part.indexOf("m") + 1);
                            fo.write(text.getBytes("utf-8"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"Frpc logger").start();
    }

    public static void stopFrpc(){
        if(runtimeProcess!=null){
            runtimeProcess.destroy();
            runtimeProcess=null;
        }
    }
    public static boolean openFrp(int i, String val){
        new Thread(()->{
            String finalval=val;
            Gson gson=new Gson();
            try {
                if(SSLUtils.sslIgnored){
                    //SSL警告
                    Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.sslwarning"));
                }
                Minecraft.getInstance().gui.getChat().addMessage(Utils.translatableText("text.openlink.creatingproxy"));
                Pair<String, Map<String, List<String>>> response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                JsonResponseWithData<JsonTotalAndList<JsonUserProxy>> userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                //OpenLink隧道命名规则：openlink_mc_[本地端口号]
                for (JsonUserProxy jsonUserProxy : userProxies.data.list) {
                    if (jsonUserProxy.proxyName.contains("openlink_mc_")) {
                        try {
                            Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/forceOff", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                            Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/removeProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), "{\"proxy_id\":" + String.valueOf(jsonUserProxy.id) + "}");
                            OpenLink.LOGGER.info("Deleted proxy: "+jsonUserProxy.proxyName);
                        } catch (Exception e) {
                            break;
                        }
                    }
                }//删除以前用过的隧道
                Thread.sleep(1000);
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                JsonResponseWithData<JsonUserInfo> userinfo=Request.getUserInfo();
                if(userinfo.data.proxies==userProxies.data.total){
                    throw new Exception(Utils.translatableText("text.openlink.userproxieslimited").getString());
                }
                JsonResponseWithData<JsonTotalAndList<JsonNode>> nodelist=Request.getNodeList();
                List<JsonNode> canUseNodes=new ArrayList<>();
                for(JsonNode now:nodelist.data.list){
                    int groupnumber1=5,usergroupnumber;
                    if(now.group.contains("svip")){
                        groupnumber1=3;
                    }
                    if(now.group.contains("vip")){
                        groupnumber1=2;
                    }
                    if(now.group.contains("normal")){
                        groupnumber1=1;
                    }
                    if(userinfo.data.group.contains("svip")){
                        usergroupnumber=3;
                    }else if(userinfo.data.group.contains("vip")){
                        usergroupnumber=2;
                    }else{
                        usergroupnumber=1;
                    }
                    if(groupnumber1>usergroupnumber||!now.protocolSupport.tcp||now.status!=200||now.fullyLoaded||(now.needRealname&&!userinfo.data.realname)){
                        continue;
                    }
                    canUseNodes.add(now);
                }
                if(canUseNodes.isEmpty()){
                    throw new Exception("Unable to use any node???");
                }
                int preferClasify = -1;
                try {
                    String json = Request.POST(Uris.ipstackUri.toString(), Request.DEFAULT_HEADER, "{}").getFirst();
                    JsonIP jsonIP = gson.fromJson(json, JsonIP.class);

                    if (jsonIP.country.equals("CN")) {
                        preferClasify = 1;
                    } else if (jsonIP.country.equals("HK") || jsonIP.country.equals("TW") || jsonIP.country.equals("MO")) {
                        preferClasify = 2;
                    } else {
                        preferClasify = 3;
                    }
                    OpenLink.LOGGER.info("User Country Code: " + jsonIP.country + ", Prefer Classify: " + preferClasify);
                } catch (Exception ignored) {
                    OpenLink.LOGGER.warn("Can not get user country! Ignoring...");
                }
                int finalPreferClasify = preferClasify;
                canUseNodes.sort(((o1, o2) -> {
                    if(finalPreferClasify !=-1&&o1.classify!=o2.classify&&(o1.classify== finalPreferClasify)!=(o2.classify== finalPreferClasify))
                        return o1.classify== finalPreferClasify ?-1:1;
                    if(!o1.group.equals(o2.group)){
                        int first=5,second=5;
                        if(o1.group.contains("svip")){
                            first=3;
                        }
                        if(o1.group.contains("vip")){
                            first=2;
                        }
                        if(o1.group.contains("normal")){
                            first=1;
                        }
                        if(o2.group.contains("svip")){
                            second=3;
                        }
                        if(o2.group.contains("vip")) {
                            second=2;
                        }
                        if(o2.group.contains("normal")){
                            second=1;
                        }
                        return first>second?-1:1;
                    }
                    if(Math.abs(o1.bandwidth*o1.bandwidthMagnification-o2.bandwidth*o2.bandwidthMagnification)<1e-5)
                        return o2.bandwidth*o2.bandwidthMagnification>o1.bandwidth*o1.bandwidthMagnification?1:-1;
                    if(userinfo.data.realname&&o1.needRealname!=o2.needRealname)
                        return o1.needRealname?-1:1;
                    return 0;
                }));
                JsonNode node=canUseNodes.get(0);//选取最优节点
                OpenLink.LOGGER.info("Selected node: id:"+node.id+" allow_port:"+node.allowPort+" group:"+node.group);
                JsonNewProxy newProxy=new JsonNewProxy();
                newProxy.name="openlink_mc_"+String.valueOf(i);
                newProxy.local_port= String.valueOf(i);
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
                    if(finalval!=null&&!finalval.isBlank()&&j==1){
                        newProxy.remote_port=Integer.parseInt(finalval);
                    }
                    response=Request.POST(Uris.openFrpAPIUri.toString() + "frp/api/newProxy", Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER), gson.toJson(newProxy));
                    OpenLink.LOGGER.info("Try "+j+": remote_port:"+newProxy.remote_port+" flag:"+gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag+" msg:"+gson.fromJson(response.getFirst(), JsonResponseWithData.class).msg);
                    if(gson.fromJson(response.getFirst(), JsonResponseWithData.class).flag){
                        found=true;
                        break;
                    }
                }//创建隧道
                if(!found) throw new Exception(Utils.translatableText("text.openlink.remoteportnotfound").getString());
                LanConfig.cfg.last_port_value=String.valueOf(newProxy.remote_port).equals(val)?val:"";
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                JsonUserProxy runningproxy=null;
                for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                    if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                        runningproxy=jsonUserProxy;
                        break;
                    }
                }
                if(runningproxy==null) throw new Exception("Can not find the proxy???");
                //启动Frpc
                runFrpc(runningproxy.id);
                //check
                Thread.sleep(5000);
                response=Request.POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserProxies",Request.getHeaderWithAuthorization(Request.DEFAULT_HEADER),"{}");
                userProxies = gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonUserProxy>>>(){}.getType());
                runningproxy=null;
                for(JsonUserProxy jsonUserProxy:userProxies.data.list){
                    if(jsonUserProxy.proxyName.equals("openlink_mc_"+String.valueOf(i))){
                        runningproxy=jsonUserProxy;
                        break;
                    }
                }
                if(runningproxy==null) throw new Exception("Can not find the proxy???");
                JsonUserProxy finalRunningproxy = runningproxy;
                Minecraft.getInstance().gui.getChat().addMessage(Utils.proxyStartText(finalRunningproxy.connectAddress));
                List<String> list=new ArrayList<>(List.of(OpenLink.PREFERENCES.get("traffic_storage", "").split(";")));
                while(list.size()>=MAX_TRAFFIC_STORAGE){
                    list.remove(0);
                }
                list.add(String.format(Locale.getDefault(),"%tD %tT",new Date(),new Date())+","+userinfo.data.traffic);
                OpenLink.PREFERENCES.put("traffic_storage", String.join(";", list));
            } catch (Exception e) {
                e.printStackTrace();
                Minecraft.getInstance().gui.getChat().addMessage(Utils.literalText("§4[OpenLink] "+e.getClass().getName()+":"+e.getMessage()));
                Minecraft.getInstance().gui.getChat().addMessage(Utils.proxyRestartText());
            }
        },"Proxy startup thread").start();

        return true;
    }

}
