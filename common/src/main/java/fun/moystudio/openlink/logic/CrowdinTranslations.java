package fun.moystudio.openlink.logic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import fun.moystudio.openlink.json.JsonCrowdinProject;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUrl;
import fun.moystudio.openlink.network.Request;
import fun.moystudio.openlink.network.Uris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CrowdinTranslations {
    public static final String CROWDIN_TOKEN = "e9e8c0beee4f7347123161b674b7806e0a3c70afcfc139f24fe5eb3429a4669c743e9dfa85aeac53";
    public static final String CROWDIN_PROJECT_ID = "722843";
    public static Map<String,List<String>> HEADER;
    public static void init(){
        HEADER = getHeaderWithCrowdinAuthorization();
        new Thread(()->{
            JsonCrowdinProject project = getProject();
            if(project != null){
                for(JsonCrowdinProject.Language language : project.targetLanguages){
                     getTranslationsDownloadUrl(language.locale.toLowerCase());
                }
            }
        }, "Crowdin translations thread").start();
    }
    private static JsonCrowdinProject getProject() {
        JsonCrowdinProject project = null;
        Gson gson = new Gson();
        try{
            JsonResponseWithData<JsonCrowdinProject> response = gson.fromJson(Request.GET(Uris.crowdinProjectsAPIUri+CROWDIN_PROJECT_ID, HEADER), new TypeToken<JsonResponseWithData<JsonCrowdinProject>>(){}.getType());
            project = response.data;
        } catch (Exception ignored) {
        }
        return project;
    }
    private static String getTranslationsDownloadUrl(String locale){
        //TODO: get url and download
        return null;
    }
    private static Map<String, List<String>> getHeaderWithCrowdinAuthorization(){
        Map<String,List<String>> header = Request.DEFAULT_HEADER;
        header.put("Authorization", Collections.singletonList("Bearer " + CROWDIN_TOKEN));
        return header;
    }
}
