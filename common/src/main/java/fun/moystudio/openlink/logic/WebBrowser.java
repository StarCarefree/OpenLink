package fun.moystudio.openlink.logic;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import java.lang.reflect.Method;

public class WebBrowser {
    public boolean browserOpened=false;
    public Process browserProcess=null;
    public String url;
    public WebBrowser(String u){
        url=u;
    }

    public void openBrowser(){
        if(!browserOpened) {
            try {
                switch (Frpc.osName) {
                    case "windows":
                        browserProcess = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);//Windows自带的链接打开方式
                        break;
                    case "darwin": //百科搜的macOS没用过
                        Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                        Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                        openURL.invoke(null, url);
                        break;
                    case "linux":
                    case "freebsd":
                        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};//Linux常用浏览器


                        String browser = null;
                        for (String b : browsers) {
                            if (Runtime.getRuntime().exec(new String[]{"which", b}).waitFor() == 0) {
                                browser = b;
                                break;
                            }
                        }
                        if (browser == null) {
                            String errorMessage = "No available browser found. Please ensure that Firefox, Opera, Konqueror, Epiphany, Mozilla, or Netscape is installed.";
                            OpenLink.LOGGER.error(errorMessage);
                            throw new RuntimeException("[OpenLink] " + errorMessage);
                        }
                        browserProcess = Runtime.getRuntime().exec(browser + " " + url);
                        break;
                    default:
                        String errorMessage = "Unsupported operating system: " + Frpc.osName + ". Please use Windows, macOS, or Linux.";
                        OpenLink.LOGGER.error(errorMessage);
                        throw new RuntimeException("[OpenLink] " + errorMessage);
                }
            } catch (Exception e) {
                String errorMessage = "An error occurred while trying to open the browser: " + e.getMessage();
                OpenLink.LOGGER.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
            browserOpened = true;
        }
    }
}