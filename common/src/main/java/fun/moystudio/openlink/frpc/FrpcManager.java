package fun.moystudio.openlink.frpc;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.logic.Extract;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FrpcManager {
    private final Map<String, Pair<String, ? extends Frpc>> frpcImplInstances = new HashMap<>();
    private final Map<String, Path> frpcExecutableFiles = new HashMap<>();
    private String currentFrpcId = null;
    private final static Logger LOGGER = LogManager.getLogger("OpenLink/FrpcManager");
    private static FrpcManager INSTANCE = null;
    public static FrpcManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FrpcManager();
        }
        return INSTANCE;
    }

    public void init() {//TODO: use this method to init
        List<String> modPrefixes = OpenLink.GET_ALL_MOD_PREFIX.get();
        for (String prefix : modPrefixes) {
            this.frpcImplInstances.putAll(getFrpcImplInstanceByPrefix(prefix));
        }
        //TODO: 扫描每个frpc目录下的可执行文件，放到frpcExecutableFiles里面
    }

    public Map<String, Pair<String, ? extends Frpc>> getFrpcImplInstanceByPrefix(String prefix){
        Set<Class<?>> classes = new Reflections(prefix).getTypesAnnotatedWith(OpenLinkFrpcImpl.class);
        Map<String, Pair<String, ? extends Frpc>> res = new HashMap<>();
        for(Class<?> clazz:classes){
            OpenLinkFrpcImpl annotation = clazz.getAnnotation(OpenLinkFrpcImpl.class);
            if(Frpc.class.isAssignableFrom(clazz)){
                Class<? extends Frpc> clazz2 = clazz.asSubclass(Frpc.class);
                try {
                    Frpc frpcInstance = (Frpc) clazz2.getMethod("getInstance", new Class<?>[]{}).invoke(null);
                    if(frpcInstance == null) {
                        LOGGER.error("Frpc implementation '{}' is annotated with @OpenLinkFrpcImpl, but the static method getInstance() returns null!",annotation.name());
                        continue;
                    }
                    res.put(annotation.id(), Pair.of(annotation.name(), frpcInstance));
                    LOGGER.info("Frpc implementation {} is loaded.", annotation.name());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                }
            } else {
                LOGGER.error("Frpc implementation '{}' is annotated with @OpenLinkFrpcImpl, but it does not implement Frpc!",annotation.name());
            }
        }
        return res;
    }

    public String getCurrentFrpcId() {//TODO: use this method to create a screen
        return this.currentFrpcId;
    }

    public Frpc getCurrentFrpcInstance() {//TODO: use this method to write frp tunnel create and start frpc logic
        return this.frpcImplInstances.get(currentFrpcId).getSecond();
    }

    public String getCurrentFrpcName() {//TODO: use this method to create a screen
        return this.frpcImplInstances.get(currentFrpcId).getFirst();
    }

    public void setCurrentFrpcId(String id) {//TODO: use this method to create a screen
        if(this.frpcImplInstances.containsKey(id)){
            this.currentFrpcId = id;
        } else {
            LOGGER.error("Cannot set the current frpc id to {}: this frpc implementation is not loaded.", id);
        }
    }

    public List<Pair<Pair<String, String>, Pair<String,Boolean>>> getFrpcImplDetailList(){//TODO: use this method to create a screen
        List<Pair<Pair<String, String>, Pair<String,Boolean>>> list = new ArrayList<>();
        this.frpcImplInstances.forEach((id, nameAndInstance) -> {
            list.add(Pair.of(Pair.of(id, nameAndInstance.getFirst()), Pair.of(frpcExecutableFiles.containsKey(id)?nameAndInstance.getSecond().getFrpcVersion(frpcExecutableFiles.get(id)):null, nameAndInstance.getSecond().isOutdated())));
        });
        return list;
    }

    public void updateFrpcByIds(String... ids) {//TODO: use this method to create a screen
        for (String id : ids) {
            if(frpcImplInstances.containsKey(id)) {
                Frpc instance = this.frpcImplInstances.get(id).getSecond();
                boolean overridden = instance.downloadFrpcLogicOverride(this.getFrpcStoragePathById(id));
                if(overridden) {
                    //TODO: find the executable file when override the downloading logic
                    LOGGER.info("Downloaded {}'s frpc executable file(download logic is overridden).",this.frpcImplInstances.get(id).getFirst());
                    continue;
                }
                Path executableFile = this.downloadFrpcById(id);
                if(executableFile == null) {
                    LOGGER.error("Frpc '{}' cannot be downloaded.", this.frpcImplInstances.get(id).getFirst());
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
                    executableFilePath = this.getFrpcStoragePathById(id).resolve(url.getFile());
                    FileOutputStream outputStream = new FileOutputStream(executableFilePath.toFile());
                    outputStream.write(inputStream.readAllBytes());
                    inputStream.close();
                    outputStream.close();
                    OpenLink.LOGGER.info("Frpc downloaded successfully!");
                    flag = true;
                    break;
                } catch (Exception e){
                    e.printStackTrace();
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
                    e.printStackTrace();
                    LOGGER.error("Cannot extract frpc archive by id '{}'!", id);
                    return null;
                }
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

    public Path getFrpcStoragePathById(String id) {//TODO: use this method to create a screen
        if(this.frpcImplInstances.containsKey(id)){
            Path override = this.frpcImplInstances.get(id).getSecond().frpcDirPathOverride(Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH + id));
            return override!=null?override:Path.of(OpenLink.EXECUTABLE_FILE_STORAGE_PATH + id);
        }
        LOGGER.error("Cannot get frpc storage path by id '{}': this frpc implementation is not loaded.", id);
        return null;
    }

    private Path getFrpcExecutableFileByDirectory(Path dir) {
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

}
