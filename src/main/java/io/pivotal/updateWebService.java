package io.pivotal;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@RestController
/**
 * update web application jar
 * Created by ezhang on 16/5/30.
 */
public class updateWebService {
    @RequestMapping(value= "/update" , method = {RequestMethod.POST})
    public void updateWebFiles(@RequestBody Map<String, String> updateWebFile) throws  Exception{

        String applicationName = "paasapp";//PaasappApplication.getApplicationName();

        String domainPath = "/tmp/staged/app/APP-INF/wlsInstall/domains/"+applicationName+"Domain/autodeploy/";
        String fileName = EmptyappApplication.download(updateWebFile.get("appLocation"), domainPath);
        //check for buildpack type, now only support weblogic
        unzipJar(".",EmptyappApplication.getApplicationName());
    }

    private void unzipJar(String destinationDir, String appFileName) throws IOException {
        File file = new File(appFileName);
        JarFile jar = new JarFile(file);

        // fist get all directories,
        // then make those directory on the destination Path
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = enums.nextElement();
            String fileName = destinationDir + File.separator + entry.getName();

            File f = new File(fileName);
            if (fileName.endsWith("/")) {
                boolean resultflag = f.mkdirs();
            }

        }

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);

                // write contents of 'is' to 'fos'
                while (is.available() > 0) {
                    fos.write(is.read());
                }

                fos.close();
                is.close();
            }
        }
    }
}
