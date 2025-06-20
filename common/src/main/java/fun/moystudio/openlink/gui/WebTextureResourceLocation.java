package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.OpenLink;
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
    public InputStream stream;
    public WebTextureResourceLocation(String url, ResourceLocation def){
        this.url=url;
        this.location=def;
        this.load();
    }
    public void load(){
        try{
            URL url1=new URL(url);
            HttpURLConnection connection= (HttpURLConnection) url1.openConnection();
            stream=connection.getInputStream();
        } catch (Exception e){
            OpenLink.LOGGER.error("", e);
            OpenLink.LOGGER.error("Error on loading avatar web texture");
        }
    }

    public void read() {
        if(this.stream==null) {
            OpenLink.LOGGER.error("{} cannot be read before loading!", this);
            return;
        }
        NativeImage image=convertJpegToPng(stream);
        if(image == null) return;
        location=Minecraft.getInstance().getTextureManager().register("avatar",new SelfCleaningDynamicTexture(image));
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
