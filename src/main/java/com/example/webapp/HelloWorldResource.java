package com.example.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableList;

@Path("/hello")
public class HelloWorldResource {
	
	private static ExecutorService executor = Executors.newCachedThreadPool();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
    	String output = runCmdOld();
        return output;
    }
    
    
    private static String runCmdOld() {
    	StringBuilder stringBuilder = new StringBuilder();
        System.out.println("--------------------------------------------");
        System.out.println("Current gws command:");
        stringBuilder.append("--------------------------------------------\n");
        stringBuilder.append("Current gws command:\n");
        String http_proxy = System.getenv("http_proxy");
        String gws_temp_dir = System.getenv("gws_temp_dir");
        Map<String, String> env = new HashMap<>();
        if(null != http_proxy) {
            env.put("http_proxy", http_proxy);
            env.put("https_proxy", http_proxy);	
        }
        java.nio.file.Path destinationDir = Paths.get(gws_temp_dir);
        List<String> args2 = new ArrayList<>();
//        args.add("git");
        args2.add("ls-remote");
        args2.add("--heads");
        args2.add("--tags");
        args2.add("https://acellere.visualstudio.com/Java/_git/Java");
        try {
            String output = launchCommand2(destinationDir, env, args2);
            System.out.println("cmd output : " + output);
            stringBuilder.append("cmd output : " + output);
        } catch (Exception e) {
            System.err.println("Exception : " + e);
            stringBuilder.append("Exception : " + e);
        }
        System.out.println("--------------------------------------------");
        stringBuilder.append("--------------------------------------------");
        return stringBuilder.toString();
    }

    public static String launchCommand2(java.nio.file.Path workDir, Map<String, String> envVars, List<String> args) {

        List<String> cmd = null;

        cmd = ImmutableList.<String>builder().add("git").addAll(args).build();

        ProcessBuilder pb = new ProcessBuilder(cmd).directory(workDir.toFile());
        Map<String, String> env = pb.environment();
        env.putAll(envVars);

        // If we don't have credentials, but the requested URL requires them,
        // it is possible for Git to hang forever waiting for interactive
        // credential input. Prevent this by setting GIT_ASKPASS to "echo"
        // if we haven't already set it.
        if (!env.containsKey("GIT_ASKPASS")) {
            env.put("GIT_ASKPASS", "echo");
        }

        try {
            Process p = pb.start();

            Future<StringBuilder> out = executor.submit(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //LOGGER.info("GIT: {}", line);
                        sb.append(line).append("\n");
                    }
                }
                return sb;
            });

            Future<StringBuilder> error = executor.submit(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb;
            });

            int code = p.waitFor();
            System.out.println("The exit code of launchCommand: {}" + code);
            if (code != 200) {
                String msg = "code: " + code + ", " + error.get().toString();
                System.out.println("Error:" + error.get().toString());
//                LOGGER.warn("Running launchCommand -> finished with code {}, error: '{}'", code, msg);
//                throw new GITServiceException(msg);
            }

            return out.get().toString();
        } catch (ExecutionException | IOException | InterruptedException e) {
            System.out.println("Running launchCommand [] -> error" + e);
//            throw new GITServiceException("git operation error: " + e);
        }
        return null;
    }
}
