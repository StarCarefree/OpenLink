package fun.moystudio.openlink.logic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.json.JsonCrowdinProject;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUrl;
import fun.moystudio.openlink.mixin.IClientLanguageAccessor;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrowdinTranslations {
    public static final String CROWDIN_TOKEN = "3961662dc82f862e89b33111e996f9a2fef2e4ab1659587347e88a642b2d08849280e3f4416bb855";
    public static final String CROWDIN_PROJECT_ID = "722843";
    public static Map<String,List<String>> HEADER;
    public static void init(){
        HEADER = getHeaderWithCrowdinAuthorization();
        new Thread(()->{
            JsonCrowdinProject project = getProject();
            if(project != null){
                File localeFolder = new File(OpenLink.EXECUTABLE_FILE_STORAGE_PATH+"assets/openlink/lang/");
                localeFolder.mkdirs();
                for(JsonCrowdinProject.Language language : project.targetLanguages){
                     String downloadUrl = getTranslationsDownloadUrl(language);
                     if(downloadUrl != null){
                         OpenLink.LOGGER.info("Downloading "+language.name+" translation file from "+downloadUrl);
                         try {
                             File file = new File(localeFolder,language.locale.toLowerCase()+".json");
                             URL url = new URL(downloadUrl);
                             BufferedInputStream is = new BufferedInputStream(url.openStream());
                             FileOutputStream os = new FileOutputStream(file);
                             os.write(is.readAllBytes());
                             os.close();
                             is.close();
//                             Minecraft.getInstance().getLanguageManager().
                         } catch (Exception e) {
                             OpenLink.LOGGER.warn("Error on downloading "+language.name+" translation file!");
                             e.printStackTrace();
                         }
                     }
                }
            }
        }, "Translations download thread").start();
    }
    private static JsonCrowdinProject getProject() {
        JsonCrowdinProject project = null;
        Gson gson = new Gson();
        String res="";
        try{
            JsonResponseWithData<JsonCrowdinProject> response = gson.fromJson(res=Request.GET(Uris.crowdinProjectsAPIUri+CROWDIN_PROJECT_ID, HEADER), new TypeToken<JsonResponseWithData<JsonCrowdinProject>>(){}.getType());
            project = response.data;
        } catch (Exception ignored) {
            OpenLink.LOGGER.warn("Error on getting project info! "+res);
        }
        return project;
    }
    private static String getTranslationsDownloadUrl(JsonCrowdinProject.Language language){
        Gson gson = new Gson();
        String res="";
        try {
            JsonResponseWithData<JsonUrl> response = gson.fromJson(res=Request.POST(Uris.crowdinProjectsAPIUri + CROWDIN_PROJECT_ID + "/translations/exports", HEADER, "{\"targetLanguageId\": \"" + language.id + "\"}", false).getFirst(), new TypeToken<JsonResponseWithData<JsonUrl>>(){}.getType());
            return response.data.url;
        } catch (Exception e){
            OpenLink.LOGGER.warn("Error on exporting "+language.name+" translation file! "+res);
            e.printStackTrace();
            return null;
        }
    }
    private static Map<String, List<String>> getHeaderWithCrowdinAuthorization(){
        Map<String,List<String>> header = Request.DEFAULT_HEADER;
        header.put("Authorization", Collections.singletonList("Bearer " + CROWDIN_TOKEN));
        return header;
    }
}
