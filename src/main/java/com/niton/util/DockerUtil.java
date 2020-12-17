package com.niton.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DockerUtil {
	private static boolean scanned = false;
	private static boolean res;
	public static boolean isDocker() {
		Logging.log(Logging.Level.INFO,"Scan if process is running in docker ...");
		if (scanned)
			return res;
		scanned = true;
		try {
			Process p = Runtime.getRuntime().exec("cat /proc/1/cgroup");
			if (parseResult(p)) return res=true;
		} catch (Exception ignored) {
		}
		try {
			Process p = Runtime.getRuntime().exec("sudo cat /proc/1/cgroup");
			if (parseResult(p)) return res=true;
		} catch (Exception ignored) {
		}
		return res=false;
	}

	private static boolean parseResult(Process p) throws IOException {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = stdInput.readLine()) != null) {
			if (line.contains("docker")) {
				p.destroy();
				return true;
			}
		}
		p.destroy();
		return false;
	}
}
