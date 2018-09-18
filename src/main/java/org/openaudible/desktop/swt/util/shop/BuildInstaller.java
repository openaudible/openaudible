package org.openaudible.desktop.swt.util.shop;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.openaudible.desktop.swt.manager.Version;
import org.openaudible.util.EventTimer;
import org.openaudible.util.SimpleProcess;
import org.openaudible.util.Util;

import java.io.File;
import java.util.ArrayList;

public class BuildInstaller {
	public static void main(String[] args) throws Exception {
		JSONObject json = build();
	}
	
	public static JSONObject build() throws Exception {
		EventTimer evt = new EventTimer();
		
		File d = new File(".." + File.separator + "install4j");
		if (!d.isDirectory())
			d = new File("install4j");
		
		if (!d.isDirectory()) throw new Exception("Expected install4j dir:" + d.getAbsolutePath());
		File installDir = new File(d, "installers");
		if (!installDir.isDirectory())
			throw new Exception("Expected dir:" + d.getAbsolutePath());
		
		boolean ok;
		for (File f : installDir.listFiles()) {
			ok = f.delete();
			if (!ok) throw new Exception("Unable to delete installer file: " + f.getAbsolutePath());
		}
		
		if (installDir.list().length != 0) {
			throw new Exception("expected empty dir:" + installDir.list());
			
		}
		
		FileUtils.writeByteArrayToFile(new File(installDir, "version.txt"), Version.appVersion.getBytes());
		
		ArrayList<String> cmd = new ArrayList<>();
		cmd.add("install4jc");
		cmd.add("-v");
		cmd.add("-r");
		cmd.add(Version.appVersion);
		File f = new File(d, "openaudible.install4j");
		if (!f.exists())
			throw new Exception("File not found: " + f.getAbsolutePath());
		cmd.add(f.getAbsolutePath());
		
		SimpleProcess simpleProcess = new SimpleProcess(cmd);
		SimpleProcess.Results r = simpleProcess.getResults();
		if (false)
			System.out.println(r.getOutputString());
		System.err.println(r.getErrorString());
		if (installDir.list().length == 0)
			throw new Exception("Expected installers:");
		
		String md5text = FileUtils.readFileToString(new File(installDir, "md5sums"), "utf-8");
		System.out.println(md5text);
		md5text = md5text.replace("*", "");
		
		JSONObject json = new JSONObject();
		
		json.put("version", Version.appVersion);
		
		// JSONArray platforms = new JSONArray();
		JSONObject platforms = new JSONObject();
		
		String downloadDir = "https://github.com/openaudible/openaudible/releases/download/v" + Version.appVersion + "/";
		
		for (String line : md5text.split("\n")) {
			String md5 = line.split(" ")[0].trim();
			String fn = line.split(" ")[1].trim();
			// if (fn.startsWith("*")) fn = fn.substring(1);
			fn = fn.trim();
			System.out.println(line);
			System.out.println(fn + "=" + md5);
			
			File installerFile = new File(installDir, fn);
			if (!installerFile.exists())
				throw new Exception("Expected file:" + fn + " at " + installerFile.getAbsolutePath());
			String kind = "";
			String ext = fn.substring(fn.lastIndexOf("."));
			String kind2 = "";
			
			switch (ext) {
				case ".sh":
					kind = "liunx";
					kind2 = "Linux GTK";
					break;
				case ".exe":
					kind = "win";
					kind2 = "Windows 64x";
					
					break;
				case ".dmg":
					kind = "mac";
					kind2 = "Mac OS X";
					break;
				default:
					throw new Exception("invalid ext:" + fn);
			}
			String newName = "OpenAudible_" + kind + "_" + Version.appVersion + ext;
			File newFile = new File(installDir, newName);
			ok = installerFile.renameTo(newFile);
			if (!ok) throw new Exception("rename failed");
			installerFile = newFile;
			
			
			JSONObject platform = new JSONObject();
			platform.put("file", newName);
			platform.put("md5", md5);
			platform.put("url", downloadDir + newName);
			platform.put("size", installerFile.length());
			platform.put("mb", Util.instance.byteCountToString(installerFile.length()) + "b");
			platform.put("version", Version.appVersion);
			platform.put("platform", kind2);
			
			platforms.put(kind, platform);
		}
		
		json.put("platforms", platforms);
		
		json.put("download_dir", downloadDir);
		json.put("old_news", Version.news);
		json.put("pre_release_news", "");   // normally blank
		json.put("current_news", "");        // normally blank
		System.err.println(evt.toString());
		
		System.out.println(json.toString(2));
		
		File jsonFile = new File(installDir, "swt_version.json");
		new File(installDir, "output.txt").delete();
		FileUtils.writeByteArrayToFile(jsonFile, json.toString(1).getBytes());
		return json;
	}
	
	
	public static JSONObject newReleaseJSON() {
		JSONObject json = new JSONObject();
		json.put("tag_name", "v" + Version.appVersion);
		json.put("name", "v" + Version.appVersion);
		json.put("target_commitish", "master");
		json.put("body", "OpenAudible release version " + Version.appVersion + ".");
		json.put("draft", false);
		json.put("prerelease", false);
		return json;
	}
	
	// POST https://<upload_url>/repos/:owner/:repo/releases/:release_id/assets?name=foo.zip
	public JSONObject addAssetJSON(File f, String contentType) {
		JSONObject json = new JSONObject();
		return json;
	}
	
}

