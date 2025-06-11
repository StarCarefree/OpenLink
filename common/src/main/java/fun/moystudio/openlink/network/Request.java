package fun.moystudio.openlink.network;

import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.frpcimpl.OpenFrpFrpcImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    public final static Map<String,List<String>> DEFAULT_HEADER=new HashMap<>(){{
        put("Content-Type", Collections.singletonList("application/json"));
        put("Accept", Collections.singletonList("application/json"));
    }};

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body) throws Exception {
        Pair<String, Map<String, List<String>>> returnval=POST(url, header, body, false);
        if(returnval!=null&&(returnval.getSecond().containsKey("Authorization")||returnval.getSecond().containsKey("authorization"))){
            String temp;
            if(returnval.getSecond().containsKey("Authorization")) temp=returnval.getSecond().get("Authorization").get(0);
            else temp=returnval.getSecond().get("authorization").get(0);
            if(OpenFrpFrpcImpl.Authorization==null||!OpenFrpFrpcImpl.Authorization.equals(temp)){
                OpenFrpFrpcImpl.Authorization=temp;
                OpenFrpFrpcImpl.writeSession();
            }
        }
        return returnval;
    }

    public static Pair<String,Map<String, List<String>>> POST(String url, Map<String,List<String>> header, String body, boolean _skip) throws Exception {
        URL postUrl=new URL(url);
        HttpURLConnection connection=(HttpURLConnection) postUrl.openConnection();
        connection.setRequestMethod("POST");
        if(!_skip) {
            header.forEach(((s, strings) -> strings.forEach(s1 -> connection.addRequestProperty(s, s1))));
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder re = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    re.append(line.trim());
                }
                return new Pair<>(re.toString(), connection.getHeaderFields());
            } catch (Exception e) {
                if (connection.getResponseCode() >= 400) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder re = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        re.append(line.trim());
                    }
                    return new Pair<>(re.toString(), connection.getHeaderFields());
                }
                throw e;
            }
        }
        else {
            return null;
        }
    }

    public static Pair<String,Map<String, List<String>>> GET(String url, Map<String,List<String>> header) throws Exception{
        URL postUrl=new URL(url);
        HttpURLConnection connection=(HttpURLConnection) postUrl.openConnection();
        connection.setRequestMethod("GET");
        header.forEach(((s, strings) -> strings.forEach(s1 -> connection.addRequestProperty(s,s1))));
        try(BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))){
            StringBuilder re=new StringBuilder();
            String line;
            while((line=br.readLine())!=null){
                re.append(line.trim());
            }
            return Pair.of(re.toString(), connection.getHeaderFields());
        }catch (Exception e){
            if(connection.getResponseCode()>=400){
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder re=new StringBuilder();
                String line;
                while((line=br.readLine())!=null){
                    re.append(line.trim());
                }
                return Pair.of(re.toString(), connection.getHeaderFields());
            }
            throw e;
        }

    }

    public static Map<String,List<String>> getHeaderWithCookieByResponse(Pair<String,Map<String, List<String>>> response,Map<String,List<String>> header){
        if(!response.getSecond().containsKey("Set-Cookie")){
            return header;
        }
        List<String> cookie=response.getSecond().get("Set-Cookie");
        header.put("Cookie",cookie);
        return header;
    }

    public static Map<String,List<String>> getHeaderWithAuthorization(Map<String,List<String>> header,String authorization){
        header.put("Authorization", Collections.singletonList(authorization));
        return header;
    }

//    public static void writeSession() {
//        OpenLink.PREFERENCES.put("authorization", Objects.requireNonNullElse(Authorization, "null"));
//    }
//
//    public static void readSession() {
//        Authorization=OpenLink.PREFERENCES.get("authorization",null);
//
//        if(Authorization==null||Authorization.equals("null")){
//            Authorization=null;
//            OpenLink.LOGGER.warn("The session does not exists in user preferences!");
//            return;
//        }
//        try{
//            JsonResponseWithData<JsonUserInfo> responseWithData = getUserInfo();
//            if(responseWithData==null||!responseWithData.flag){
//                Authorization=null;
//                writeSession();
//                OpenLink.LOGGER.warn("The session has been expired!");
//            }
//        } catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static JsonResponseWithData<JsonUserInfo> getUserInfo() throws Exception {
//        if(Authorization==null) return null;
//        Gson gson=new Gson();
//        Pair<String, Map<String, List<String>>> response=POST(Uris.openFrpAPIUri.toString()+"frp/api/getUserInfo",getHeaderWithAuthorization(DEFAULT_HEADER),"{}");
//        JsonResponseWithData<JsonUserInfo> res=gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonUserInfo>>(){}.getType());
//        if(res.data!=null)
//            Request.token=res.data.token;
//        return res;
//    }
//
//    public static JsonResponseWithData<JsonTotalAndList<JsonNode>> getNodeList() throws Exception {
//        if(Authorization==null) return null;
//        Gson gson=new Gson();
//        Pair<String, Map<String, List<String>>> response=POST(Uris.openFrpAPIUri.toString()+"frp/api/getNodeList",getHeaderWithAuthorization(DEFAULT_HEADER),"{}");
//        return gson.fromJson(response.getFirst(), new TypeToken<JsonResponseWithData<JsonTotalAndList<JsonNode>>>(){}.getType());
//    }
}