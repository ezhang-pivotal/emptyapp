/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@Configuration
@EnableAutoConfiguration
/**
 * Created by ezhang on 16/5/25.
 */
public class RunTaskService {
    @Value("${paasapp.uri}")
    private String uri;
    @RequestMapping(value= "/run-task" , method = {RequestMethod.POST})
    public void runTask(@RequestBody Map<String, String> runTaskRequest) throws  Exception{
        System.out.println("Run task request: "+runTaskRequest);
        //setTaskApp
        String applicationName = EmptyappApplication.getApplicationName();
        String fileName = EmptyappApplication.download(runTaskRequest.get("appLocation"),"./");
        doRunTask(runTaskRequest.get("cmd"),fileName);
    }

    public void doRunTask(String cmd, String appFileName) throws Exception {
        try {

            String[] env = new String[2];
            env[0]= ("PATH=/home/vcap/app/.java-buildpack/open_jdk_jre/bin");
            env[1]=("CLASSPATH="+appFileName);
            Process ps = Runtime.getRuntime().exec("/home/vcap/app/.java-buildpack/open_jdk_jre/bin/"+cmd,env);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            HashMap<String, String> finTaskRequest = new HashMap<String,String>();
            finTaskRequest.put("result",result);
            finTaskRequest.put("name",EmptyappApplication.getApplicationName());
            callBack(finTaskRequest);
            System.out.println(result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callBack(Map<String, String>finTaskRequest){
        System.out.println("callback finish task..."+uri);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Map> entity = new HttpEntity<Map>(finTaskRequest, headers);

        restTemplate.postForObject(uri, entity, Map.class,finTaskRequest);
    }


}
