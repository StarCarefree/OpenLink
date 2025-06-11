package fun.moystudio.openlink.frpc;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Frpc interface.
 * @author Terry_MC
 */
public interface Frpc {
    /**
     * Return {@code false} by default.<br>
     * We recommend you not to override the download logic. OpenLink will automatically download frpc file and extract(if you return {@code true} in the method {@link #isArchive()}).<br>
     * If you really want to override the download logic, implement this method and return {@code true}.<br>
     * If you just want to do something before the frpc downloading, implement this method and return {@code false}
     * @param frpcDownloadDir the download directory of the frpc executable file.
     * @return whether this method override frpc downloading.
     * @implNote If you return {@code true} in this method, you have to download frpc to {@code frpcDownloadDir} and make sure there is a frpc executable file if you do not implement {@link #frpcDirPathOverride(Path)}(OpenLink will scan the directory to find an executable file).
     */
    default boolean downloadFrpc(Path frpcDownloadDir) {
        return false;
    }
    /**
     * Return {@code null} by default.<br>
     * You can override this method to use another frpc executable file.
     * @param frpcStorageDirPath the old path of the frpc storage directory.
     * @return the new path of the frpc storage directory.
     */
    default Path frpcDirPathOverride(Path frpcStorageDirPath) {
        return null;
    }
    /**
     * Return {@code false} by default.
     * @return whether the frpc file is an archive.
     */
    default boolean isArchive() {
        return false;
    }
    /**
     * Return {@code null} by default.<br>
     * Get the url list of the frpc file if there is an update. The order in the list determines the url's priority.
     * @return the full url(with http:// or https://) list of the new frpc file. The higher the position of the url in the list, the higher the priority. If there is not an update, return {@code null}.
     */
    default List<String> getUpdateFileUrls() {
        return null;
    }
    /**
     * Initialize your Frpc Impl
     * @throws Exception All exceptions that can throw when initiating.
     */
    default void init() throws Exception{
    }
    /**
     * Get the unique id of your frpc implementation.
     * @return your id.
     */
    String id();
    /**
     * Get the unique name of your frpc implementation.
     * @return your name.
     */
    String name();
    /**
     * Get whether there is a frpc update.
     * @return whether there is a frpc update.
     * @param frpcExecutableFilePath the path of the frpc executable file.
     */
    boolean isOutdated(@Nullable Path frpcExecutableFilePath);
    /**
     * Create the frpc process.
     * @param frpcExecutableFilePath the path of the frpc executable file.
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @return the frpc process.
     * @throws Exception All exceptions that can throw when creating frpc process.
     */
    Process createFrpcProcess(Path frpcExecutableFilePath, int localPort, @Nullable String remotePort) throws Exception;
    /**
     * Create the remote proxy(tunnel).
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @return the remote ip of the remote proxy for players to join.
     * @throws Exception All exceptions that can throw when creating proxy.
     * @implNote You can ignore {@code remotePort} when you cannot use that port to create the remote proxy. You should only create the frpc tunnel in this method. DO NOT START FRPC IN THIS METHOD!
     */
    String createProxy(int localPort, @Nullable String remotePort) throws Exception;
    /**
     * Get the frpc version.
     * @param frpcExecutableFilePath the path of the frpc executable file.
     * @return the version string of the frpc.
     */
    String getFrpcVersion(Path frpcExecutableFilePath);
    /**
     * Stop the frpc process.
     * @param frpcProcess the process of the frpc executable file.
     */
    default void stopFrpcProcess(@Nullable Process frpcProcess) {
        if(frpcProcess!=null){
            frpcProcess.destroy();
        }
    }
    /**
     * Return {@code null} by default.<br>
     * Get the node selection screen of your frp service.
     * @param lastScreen the parent screen(can be {@code null}).
     * @return the screen instance of the node selection screen.
     * @implNote If there is not any node selection screen, do not implement this method.
     */
    default Screen getNodeSelectionScreen(@Nullable Screen lastScreen) {
        return null;
    }
    /**
     * Return {@code null} by default.<br>
     * Get the login screen of your frp service.
     * @param lastScreen the parent screen(can be {@code null}).
     * @return the screen instance of the login screen.
     * @implNote If your frp service do not have to log in, do not implement this method.
     */
    default Screen getLoginScreen(@Nullable Screen lastScreen) {
        return null;
    }
    /**
     * Get the ResourceLocation(or Identifier) of the icon of your frp service.
     * @return the ResourceLocation(or Identifier) of the icon of your frp service.
     * @implNote If there is not any icon, do not implement this method.
     */
    default ResourceLocation getIcon() {
        return null;
    }
    /**
     * Return {@code true} by default.<br>
     * @return whether user is logged into your frp service.
     * @implNote If your frp service do not have to log in, do not implement this method.
     */
    default boolean isLoggedIn() {
        return true;
    }
    /**
     * Log out user's account.
     * @implNote If your frp service do not have to log out, do not implement this method.
     */
    default void logOut(){
    }
    /**
     * Get the web panel url
     * @return the url of your frp service web panel
     * @implNote If your frp service do not have a web panel, do not implement this method.
     */
    default String getPanelUrl(){
        return null;
    }
}