package com.hopebaytech.hcfsmgmt.utils;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Vince
 *         Created by Vince on 2016/8/15.
 */

public class ZipUtils {

    private static final String CLASSNAME = "ZipUtils";

    public static boolean zip(String sourcePath, String targetPath){
        boolean isSuccess = false;
        File source = new File(sourcePath);
        File target = new File(targetPath);
        try {
            if(target.exists()){
                target.delete();
            }
            target.createNewFile();
        } catch (IOException e) {
            Logs.e(CLASSNAME, "zip", Log.getStackTraceString(e));
            return isSuccess;
        }

        if(source.exists()){
            try{
                FileOutputStream f = new FileOutputStream(targetPath);
                CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
                ZipOutputStream out = new ZipOutputStream(
                        new BufferedOutputStream(ch));
                if (zipFile(out, source)) {
                    isSuccess = true;
                }
                out.closeEntry();
                out.close();
            } catch(IOException e){
                Logs.e(CLASSNAME, "zip", Log.getStackTraceString(e));
            }
        } else{
            Logs.e(CLASSNAME, "zip", "Source File doesn't exist! " + source.getAbsolutePath());
        }
        return isSuccess;
    }

    protected static boolean zipFile(ZipOutputStream out,File f) throws IOException{
        boolean isSuccess = false;
        if(f.exists()){
            if (f.isDirectory()) {
                try {
                    File[] files = f.listFiles();
                    if(files.length == 0){
                        Logs.e(CLASSNAME, "zipFile", "Empty folder");
                        out.putNextEntry(new ZipEntry(f.getAbsoluteFile() + "/"));
                        return isSuccess;
                    } else{
                        Logs.d(CLASSNAME, "zipFile", String.valueOf(files.length));
                    }
                    isSuccess = true;
                    for (int i = 0; i < files.length; i++) {
                        //zipFile(out, files[i]);
                        if (!zipFile(out, files[i])) {
                            isSuccess = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logs.e(CLASSNAME, "zipFile", Log.getStackTraceString(e));
                }
            } else {
                out.putNextEntry(new ZipEntry(f.getName()));
                byte[] buf = new byte[1024];
                int len;
                FileInputStream fis = new FileInputStream(f);
                while ((len = fis.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                fis.close();
                isSuccess = true;
            }
            return isSuccess;
        } else{
            Logs.e(CLASSNAME, "zipFile", f.getAbsolutePath() + " not exist.");
        }

        return isSuccess;
    }
}
