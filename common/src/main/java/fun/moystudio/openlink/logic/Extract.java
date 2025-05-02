package fun.moystudio.openlink.logic;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Extract {
    public static void ExtractBySuffix(File file, File extractDir) throws Exception {//仅解压10MB以内的文件
        if(!file.exists()) {
            throw new RuntimeException("[OpenLink] The file to extract does not exist!");
        }
        FileInputStream fileInputStream=new FileInputStream(file);
        if(!extractDir.exists()){
            extractDir.mkdir();
        }
        if(file.getName().endsWith(".zip")){
            try {
                ZipInputStream zipInputStream=new ZipInputStream(fileInputStream);
                ZipEntry zipEntry=zipInputStream.getNextEntry();
                while (zipEntry!=null){
                    String path = extractDir.getAbsolutePath()+File.separator+zipEntry.getName();
                    if(zipEntry.isDirectory()){
                        File dir=new File(path);
                        dir.mkdir();
                    } else {
                        FileOutputStream fileOutputStream=new FileOutputStream(path);
                        fileOutputStream.write(zipInputStream.readAllBytes());
                        fileOutputStream.close();
                    }
                    zipEntry=zipInputStream.getNextEntry();
                }
                zipInputStream.closeEntry();
                zipInputStream.close();
                fileInputStream.close();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
            return;
        } else if (file.getName().endsWith(".tar.gz")) {
            try {
                GZIPInputStream gzipInputStream=new GZIPInputStream(fileInputStream);
                TarArchiveInputStream tarInputStream=new TarArchiveInputStream(gzipInputStream);
                TarArchiveEntry tarEntry=tarInputStream.getNextTarEntry();
                while (tarEntry!=null){
                    String path = extractDir.getAbsolutePath()+File.separator+tarEntry.getName();
                    if(tarEntry.isDirectory()) {
                        File dir=new File(path);
                        dir.mkdir();
                    } else {
                        FileOutputStream fileOutputStream=new FileOutputStream(path);
                        fileOutputStream.write(tarInputStream.readAllBytes());
                        fileOutputStream.close();
                    }
                    tarEntry=tarInputStream.getNextTarEntry();
                }
                tarInputStream.close();
                gzipInputStream.close();
                fileInputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (file.getName().endsWith(".gz")) {
            try {
                GZIPInputStream gzipInputStream=new GZIPInputStream(fileInputStream);
                FileOutputStream fileOutputStream=new FileOutputStream(extractDir.getAbsolutePath()+File.separator+"frpc");//IDK will it works
                fileOutputStream.write(gzipInputStream.readAllBytes());
                fileOutputStream.close();
                gzipInputStream.close();
                fileInputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (file.getName().endsWith(".tar")) {
            TarArchiveInputStream tarInputStream=new TarArchiveInputStream(fileInputStream);
            TarArchiveEntry tarEntry=tarInputStream.getNextTarEntry();
            while (tarEntry!=null){
                String path = extractDir.getAbsolutePath()+File.separator+tarEntry.getName();
                if(tarEntry.isDirectory()) {
                    File dir=new File(path);
                    dir.mkdir();
                } else {
                    FileOutputStream fileOutputStream=new FileOutputStream(path);
                    fileOutputStream.write(tarInputStream.readAllBytes());
                    fileOutputStream.close();
                }
                tarEntry=tarInputStream.getNextTarEntry();
            }
            tarInputStream.close();
            fileInputStream.close();
        }
        throw new Exception("[OpenLink] The suffix of the file to extract is unsupported!");
    }
    public static void ExtractBySuffix(File file) throws Exception {
        ExtractBySuffix(file,file.getParentFile());
    }
}
