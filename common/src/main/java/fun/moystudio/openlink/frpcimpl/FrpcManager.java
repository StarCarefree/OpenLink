package fun.moystudio.openlink.frpcimpl;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.EventCallbacks;
import fun.moystudio.openlink.logic.Extract;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.SSLUtils;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FrpcManager {
    private final Map<String, Pair<String, ? extends Frpc>> frpcImplInstances = new HashMap<>();
    private final Map<String, Path> frpcExecutableFiles = new HashMap<>();
    private String currentFrpcId = null;
    private Process frpcProcess = null;
    private final static Logger LOGGER = LogManager.getLogger("OpenLink/FrpcManager");
    private static FrpcManager INSTANCE = null;
    private String currentIP = null;
    public boolean initialized = false;

    public static FrpcManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FrpcManager();
        }
        return INSTANCE;
    }

    public void init() {
        this.currentFrpcId = OpenLink.PREFERENCES.get("frpc_id", "openfrp");
        ServiceLoader<Frpc> loader = ServiceLoader.load(Frpc.class);
        for(Frpc instance:loader) {
            try {
                instance.init();
            } catch (SSLHandshakeException e) {
                LOGGER.error("", e);
                OpenLink.LOGGER.error("SSL Handshake Error! Ignoring SSL(Not Secure)");
                try {
                    SSLUtils.ignoreSsl();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (SocketException e){
                LOGGER.error("", e);
                OpenLink.disabled = true;
                OpenLink.LOGGER.error("Socket Error! Are you still connecting to the network? All the features will be disabled!");
            } catch (IOException e) {
                LOGGER.error("", e);
                OpenLink.disabled = true;
                OpenLink.LOGGER.error("IO Error! Are you still connecting to the network? All the features will be disabled!");
            } catch (Exception e) {
                LOGGER.error("", e);
                LOGGER.error("Cannot load {}: cannot initialize this frpc implementation.", instance.id());
                continue;
            }
            this.frpcImplInstances.put(instance.id(), Pair.of(instance.name(),instance));
        }
        for (String id : this.frpcImplInstances.keySet()) {
            Path path = this.getFrpcExecutableFileByDirectory(this.getFrpcStoragePathById(id));
            if(path!=null){
                frpcExecutableFiles.put(id, path);
            }
        }
        if(!this.frpcImplInstances.containsKey(currentFrpcId)){
            LOGGER.error("Cannot load frpc id from user preferences: cannot find {}",currentFrpcId);
            currentFrpcId = "openfrp";
        }
        StringBuilder sb = new StringBuilder("Loaded " + this.frpcImplInstances.size() + " FrpcImpls:");
        for (Map.Entry<String, Pair<String, ? extends Frpc>> pair : this.frpcImplInstances.entrySet()) {
            sb.append(String.format("\n\t- %s %s",pair.getKey(),pair.getValue().getSecond()));
        }
        LOGGER.info(sb.toString());
        initialized = true;
    }

    public Path getFrpcImplExecutableFile(String id) {
        return isExecutableFileExist(id)?frpcExecutableFiles.get(id):null;
    }

    public String getCurrentFrpcId() {
        return this.currentFrpcId;
    }

    public Frpc getCurrentFrpcInstance() {
        return this.frpcImplInstances.get(currentFrpcId).getSecond();
    }

    public String getCurrentFrpcName() {
        return this.frpcImplInstances.get(currentFrpcId).getFirst();
    }

    public void setCurrentFrpcId(String id) {
        if(this.frpcImplInstances.containsKey(id)){
            this.currentFrpcId = id;
            OpenLink.PREFERENCES.put("frpc_id",id);
            EventCallbacks.hasUpdate = getFrpcImplDetail(getCurrentFrpcId()).getSecond().getSecond();
        } else {
            LOGGER.error("Cannot set the current frpc id to {}: this frpc implementation is not loaded.", id);
        }
    }

    public List<Pair<Pair<String, String>, Pair<String,Boolean>>> getFrpcImplDetailList() {
        List<Pair<Pair<String, String>, Pair<String,Boolean>>> list = new ArrayList<>();
        this.frpcImplInstances.forEach((id, nameAndInstance) -> {
            list.add(Pair.of(Pair.of(id, nameAndInstance.getFirst()), Pair.of(frpcExecutableFiles.containsKey(id)?nameAndInstance.getSecond().getFrpcVersion(frpcExecutableFiles.get(id)):null, nameAndInstance.getSecond().isOutdated(this.getFrpcImplExecutableFile(id)))));
        });
        return list;
    }

    public Pair<String, Pair<String,Boolean>> getFrpcImplDetail(String id) {
        Pair<String, ? extends Frpc> nameAndInstance = this.frpcImplInstances.get(id);
        return Pair.of(nameAndInstance.getFirst(), Pair.of(frpcExecutableFiles.containsKey(id)?nameAndInstance.getSecond().getFrpcVersion(frpcExecutableFiles.get(id)):null, nameAndInstance.getSecond().isOutdated(this.getFrpcImplExecutableFile(id))));
    }

    public void updateFrpcByIds(String... ids) {
        for (String id : ids) {
            if(frpcImplInstances.containsKey(id)) {
                Frpc instance = this.frpcImplInstances.get(id).getSecond();
                boolean overridden = instance.downloadFrpc(this.getFrpcStoragePathById(id));
                if(overridden) {
                    Path path = this.getFrpcExecutableFileByDirectory(this.getFrpcStoragePathById(id));
                    if(path == null) {
                        LOGGER.error("Cannot use the downloading logic override of {}: cannot find the frpc executable file in storage directory.", this.frpcImplInstances.get(id).getFirst());
                        continue;
                    }
                    frpcExecutableFiles.put(id, path);
                    LOGGER.info("Downloaded {}'s frpc executable file(download logic is overridden).", this.frpcImplInstances.get(id).getFirst());
                    continue;
                }
                Path executableFile = this.downloadFrpcById(id);
                if(executableFile == null) {
                    LOGGER.error("Frpc '{}' cannot be downloaded.", this.frpcImplInstances.get(id).getFirst());
                    continue;
                }
                frpcExecutableFiles.put(id,executableFile);
                LOGGER.info("Downloaded {}'s frpc executable file automatically.",this.frpcImplInstances.get(id).getFirst());
            }
        }
    }

    private Path downloadFrpcById(String id) {
        if(this.frpcImplInstances.containsKey(id)){
            List<String> urls = this.frpcImplInstances.get(id).getSecond().getUpdateFileUrls();
            if(urls==null) {
                LOGGER.error("Cannot download frpc by id '{}': no download urls provided by frpc implementation.", id);
                return null;
            }
            boolean flag = false;
            Path executableFilePath = null;
            for(String s:urls){
                try {
                    URL url = new URL(s);
                    BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
                    executableFilePath = this.getFrpcStoragePathById(id).resolve(url.getFile().substring(url.getFile().lastIndexOf('/')+1));
                    FileOutputStream outputStream = new FileOutputStream(executableFilePath.toFile());
                    outputStream.write(inputStream.readAllBytes());
                    inputStream.close();
                    outputStream.close();
                    LOGGER.info("Frpc downloaded successfully!");
                    flag = true;
                    break;
                } catch (Exception e){
                    LOGGER.error("", e);
                    LOGGER.info("An error occurred while downloading frpc by url '{}'",s);
                }
            }
            if(!flag) {
                LOGGER.error("Cannot download frpc by id '{}': all the frpc download urls cannot use.", id);
                return null;
            }
            if(this.frpcImplInstances.get(id).getSecond().isArchive()){
                try {
                    Extract.ExtractBySuffix(executableFilePath.toFile().getAbsoluteFile());
                } catch (Exception e) {
                    LOGGER.error("", e);
                    LOGGER.error("Cannot extract frpc archive by id '{}'!", id);
                    return null;
                }
                executableFilePath.toFile().getAbsoluteFile().delete();
                executableFilePath = this.getFrpcExecutableFileByDirectory(this.getFrpcStoragePathById(id));
            }
            if(executableFilePath == null){
                LOGGER.error("An error occurred while getting the frpc executable file path!");
            } else {
                LOGGER.info("Frpc executable file path is found successfully!");
            }
            return executableFilePath;
        } else {
            LOGGER.error("Cannot download frpc by id '{}': this frpc implementation is not loaded.", id);
            return null;
        }
    }

    public Path getFrpcStoragePathById(String id) {
        if(this.frpcImplInstances.containsKey(id)){
            Path override = this.frpcImplInstances.get(id).getSecond().frpcDirPathOverride(Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH + id));
            if(override!=null) override.toFile().mkdirs();
            else Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH + id).toFile().mkdirs();
            return override!=null?override:Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH + id);
        }
        LOGGER.error("Cannot get frpc storage path by id '{}': this frpc implementation is not loaded.", id);
        return null;
    }

    public Path getFrpcExecutableFileByDirectory(Path dir) {
        final Path[] res = {null};
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>(){
                @Override
                public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
                    if(file.toFile().getName().lastIndexOf('.')==-1||file.toFile().getName().endsWith(".exe")){
                        res[0]=file;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            return null;
        }
        return res[0];
    }

    public boolean isExecutableFileExist(String id) {
        return frpcExecutableFiles.containsKey(id)&&frpcExecutableFiles.get(id)!=null;
    }

    public void stop() {
        if(!initialized) return;
        this.getCurrentFrpcInstance().stopFrpcProcess(this.frpcProcess);
        this.frpcProcess = null;
        this.currentIP = null;
    }

    public boolean start(int i, String val) {
        Frpc frpc = this.getCurrentFrpcInstance();
        String ip;
        try{
            ip = frpc.createProxy(i, val);
            if(frpcExecutableFiles.containsKey(this.currentFrpcId)) {
                LocalTime localTime = LocalTime.now();
                LocalDate localDate = LocalDate.now();
                File logFile=new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"logs"+File.separator+
                        localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"_"+
                        localTime.getHour()+"."+localTime.getMinute()+"."+localTime.getSecond()+"_"+
                        Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()+".log");
                logFile.createNewFile();
                LOGGER.info("Frpc Log File Path:"+logFile);
                new FileOutputStream(logFile).write((Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()+"\n"+
                        localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"\n"+
                        localTime.format(DateTimeFormatter.ofPattern("hh:mm:ss"))+"\n"+
                        i+"\n"+
                        this.getCurrentFrpcName()+"\n"
                ).getBytes(StandardCharsets.UTF_8));
                this.frpcProcess = frpc.createFrpcProcess(this.frpcExecutableFiles.get(this.currentFrpcId), i, val);
                new Thread(()-> {
                    try {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(frpcProcess.getInputStream()))) {
                            String line;
                            FileOutputStream fo=new FileOutputStream(logFile,true);
                            while ((line = reader.readLine()) != null) {
                                fo.write("\n".getBytes(StandardCharsets.UTF_8));
                                fo.write(line.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                },"Frpc logger").start();

            } else {
                LOGGER.error("Cannot start frpc: cannot find the frpc executable file.");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            Minecraft.getInstance().gui.getChat().addMessage(Utils.literalText("§4[OpenLink] "+e.getClass().getName()+":"+e.getMessage()));
            Minecraft.getInstance().gui.getChat().addMessage(Utils.proxyRestartText());
            return false;
        }
        Minecraft.getInstance().gui.getChat().addMessage(Utils.proxyStartText(ip));
        this.currentIP = ip;
        return true;
    }
    public String getCurrentIP() {
        return this.currentIP;
    }
    public Process getFrpcProcess() {
        return this.frpcProcess;
    }
}
