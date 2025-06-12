package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebTextureResourceLocation {
    public String url;
    public ResourceLocation location;
    public WebTextureResourceLocation(String url, ResourceLocation def){
        this.url=url;
        this.location=def;
        this.load();
    }
    public void load(){
        try{
            URL url1=new URL(url);
            HttpURLConnection connection= (HttpURLConnection) url1.openConnection();
            InputStream stream=connection.getInputStream();
            NativeImage image=NativeImage.read(stream);
            ResourceLocation location1 = Utils.createResourceLocation("openlink","avatar.png");
            Minecraft.getInstance().getTextureManager().register(location1,new SelfCleaningDynamicTexture(image));
            location = location1;
            stream.close();
        } catch (Exception e){
            OpenLink.LOGGER.error("", e);
            OpenLink.LOGGER.error("Error on loading avatar web texture");
        }
    }
}
