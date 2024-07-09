package com.example.webapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import com.google.common.collect.ImmutableList;

@Path("/hello")
public class HelloWorldResource {
	
	private static ExecutorService executor = Executors.newCachedThreadPool();

    @GET
    @Path("/1")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello1() {
    	String output = runCmdOld("https://acellere.visualstudio.com/Java/_git/Java");
        return output;
    }
    
    @GET
    @Path("/2")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello2() {
    	String output = runCmdOld("https://github.com/NileshVirkar/githubdemo");
        return output;
    }
    
    @GET
    @Path("/3")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello3() {
    	String output = runCmdWithProcessExce();
        return output;
    }
    
    private static String runCmdOld(String url) {
    	StringBuilder stringBuilder = new StringBuilder();
        System.out.println("--------------------------------------------");
        System.out.println("Current gws command:");
        stringBuilder.append("--------------------------------------------\n");
        stringBuilder.append("URL : " + url + "\n");
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
        args2.add(url);
        try {
            String output = launchCommand2(destinationDir, env, stringBuilder, args2);
            System.out.println("cmd output : " + output);
            stringBuilder.append("cmd output : " + output + "\n");
        } catch (Exception e) {
            System.err.println("Exception : " + e);
            stringBuilder.append("Exception : " + e);
        }
        System.out.println("--------------------------------------------");
        stringBuilder.append("--------------------------------------------\n");
        return stringBuilder.toString();
    }

    public static String launchCommand2(java.nio.file.Path workDir, Map<String, String> envVars, StringBuilder stringBuilder, List<String> args) {
    	stringBuilder.append("--------------------------------------------\n");
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
        	stringBuilder.append("The exit code of launchCommand:" + code + "\n");
            if (code != 200) {
                String msg = "code: " + code + ", " + error.get().toString();
                System.out.println("Error:" + error.get().toString());
            	stringBuilder.append("Error:" + error.get().toString() + "\n");
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
    
	private static String runCmdWithProcessExce() {
		StringBuilder stringBuilder = new StringBuilder();
		System.out.println("--------------------------------------------");
		stringBuilder.append("--------------------------------------------");
		System.out.println("runCmdWithProcessExce");
		stringBuilder.append("runCmdWithProcessExce");

		String http_proxy = System.getenv("http_proxy");
		String gws_temp_dir = System.getenv("gws_temp_dir");
		stringBuilder.append("http_proxy" + http_proxy);
		stringBuilder.append("gws_temp_dir" + gws_temp_dir);
		System.out.println("Current gws command:");
		stringBuilder.append("Current gws command:");
		Map<String, String> env = new HashMap<>();
		if(null != http_proxy) {
			env.put("http_proxy", http_proxy);
			env.put("https_proxy", http_proxy);	
		}

		List<String> arg = new ArrayList<>();
		arg.add("ls-remote");
		arg.add("--heads");
		arg.add("--tags");
		arg.add("https://acellere.visualstudio.com/Java/_git/Java");
		String[] arr = new String[arg.size()];
		String command = "git";
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			executeProcessSync(command, arg.toArray(arr), outputStream, env, stringBuilder);
			String output = outputStream.toString();
			System.out.println("output:" + output);
			stringBuilder.append("output:" + output);
		} catch (Exception e) {
			System.err.println("Exception : " + e);
			stringBuilder.append("Exception : " + e);
		}
		return stringBuilder.toString();
	}
	
	public static void executeProcessSync(String executable, String[] args, OutputStream os, Map<String, String> env, StringBuilder stringBuilder){
		try {
			stringBuilder.append("Executing synchronous command " + executable + " with args: " + Arrays.toString(args));
			Map<String, String> envs = new HashMap<String, String>();
			envs.putAll(env);
			envs.putAll(System.getenv());
			CommandLine cmdLine = new CommandLine(executable);
			cmdLine.addArguments(args, true);
			PumpStreamHandler streamHandler = new PumpStreamHandler(os);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(streamHandler);
			executor.execute(cmdLine, envs);
			stringBuilder.append("*******************");
		} catch (IOException e) {
			stringBuilder.append("Exception : " + e);
		}
	}
}
