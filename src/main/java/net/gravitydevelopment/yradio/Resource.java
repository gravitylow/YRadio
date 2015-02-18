package net.gravitydevelopment.yradio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Resource {
    
    private static InputStream input = null;
    
    public Resource(String name) {
        input = getClass().getClassLoader().getResourceAsStream(name);
    }
    
    public static void saveTo(File outFile) {
        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = input.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                input.close();
            }
        } catch (IOException ex) {
            YRadio.logError(ex.getMessage());
        }
        outFile.setReadable(true);
        outFile.setExecutable(true);
    }
    
}
