package io.pivotal;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
@EnableAutoConfiguration
@SpringBootApplication
public class EmptyappApplication {

	public static void main(String[] args) {

		SpringApplication.run(EmptyappApplication.class, args);
	}

	public static String getApplicationName() throws IOException {
		String vcap_appication = System.getenv("VCAP_APPLICATION");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
		Map<String,String> map = mapper.readValue(vcap_appication,Map.class);
		System.out.println(map);

		return map.get("application_name");
	}

	public static String download(String appLocation, String destPath) throws Exception {
		System.out.println(appLocation);

		HttpGet httpGet = new HttpGet(appLocation);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		FileOutputStream fos;
		HttpResponse response = httpClient.execute(httpGet);
		String fileName=appLocation.substring(appLocation.lastIndexOf("/")+1);
		System.out.println(fileName);

		try (InputStream inputStream = response.getEntity().getContent()) {
			File path = new File(destPath);
			if (!path.exists()) {
				path.mkdirs();
			}
			File file = new File(fileName);
			if (file.exists()) {
				System.out.println(fileName + " exists");
				return fileName;
			}

			fos = new FileOutputStream(file);
			byte[] data = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(data)) != -1) {
				fos.write(data, 0, len);
			}
			fos.flush();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		httpClient.close();
		return fileName;
	}
}
