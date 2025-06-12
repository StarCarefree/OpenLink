package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            NativeImage image=convertJpegToPng(stream);
            if(image == null) return;
            ResourceLocation location1 = Utils.createResourceLocation("openlink","avatar.png");
            Minecraft.getInstance().getTextureManager().register(location1,new SelfCleaningDynamicTexture(image));
            location = location1;
        } catch (Exception e){
            OpenLink.LOGGER.error("", e);
            OpenLink.LOGGER.error("Error on loading avatar web texture");
        }
    }

    private NativeImage convertJpegToPng(InputStream in) {
        NativeImage nativeImage;
        ByteArrayOutputStream byteArrayOut;
        try {
            BufferedImage bufferedImage = ImageIO.read(in);
            byteArrayOut = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOut);
            nativeImage = NativeImage.read(new ByteArrayInputStream(byteArrayOut.toByteArray()));
        } catch (Exception e) {
            OpenLink.LOGGER.error("", e);
            return null;
        }
        try {
            in.close();
            byteArrayOut.close();
        } catch (IOException e) {
            OpenLink.LOGGER.error("", e);
            return null;
        }
        return nativeImage;
    }
}
