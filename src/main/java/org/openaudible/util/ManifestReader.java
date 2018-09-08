package org.openaudible.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public enum ManifestReader {
	instance;
	private static final Log LOG = LogFactory.getLog(ManifestReader.class);
	
	public Manifest getManifest() {
		return getManifest(null, null);
	}
	
	public Manifest getManifest(String key, String value) {
		
		try {
			Enumeration<URL> resources = getClass().getClassLoader()
					.getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				try {
					Manifest manifest = new Manifest(resources.nextElement().openStream());
					
					// check that this is your manifest and do what you need or get the next one
					if (key == null) {
						
						return manifest;    // first if key is null.
					}
					
					Attributes a = manifest.getMainAttributes();
					if (a != null) {
						String v = a.getValue(key);
						if (v != null) {
							if (value == null) return manifest;
							if (value.equals(v))
								return manifest;
						}
					}
					
					
				} catch (Throwable th) {
					// handle
					LOG.error("manifest 1", th);
					
				}
			}
		} catch (Throwable th) {
			LOG.error("manifest 2", th);
			
		}
		return null;
	}
	
	public String getBuildVersion() {
		Manifest m = getManifest("Application-Name", "OpenAudible");
		if (m != null) {
			String out = m.getMainAttributes().getValue("Build-Date");
			if (out == null) out = "";
			return out;
		}
		return "";
	}
}

