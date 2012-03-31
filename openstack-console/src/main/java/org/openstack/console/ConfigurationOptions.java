package org.openstack.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Option;
import org.openstack.client.OpenStackClient;
import org.openstack.console.common.CliOptions;


public class ConfigurationOptions extends CliOptions {
	@Option(name = "-u", aliases = { "--username", "--user" }, usage = "login username")
	String username;

	@Option(name = "-t", aliases = "--tenant", usage = "login tenant")
	String tenantId;

	@Option(name = "-p", aliases = "--password", usage = "login password")
	String password;

	@Option(name = "-s", aliases = "--server", usage = "specify authentication server")
	String server;

	@Option(name = "-debug", usage = "enable debug output")
	boolean debug;

	@Option(name = "-c", aliases = "--config", usage = "specify configuration file")
	String configFile;

	// public OpenstackImageClient buildImageClient() throws OpenstackException {
	// return getOpenstackSession().getImageClient();
	// }

	OpenStackClient service = null;

	public OpenStackClient getOpenstackService() {
		if (service == null) {
			if (configFile == null) {
				Properties properties = new Properties();
				
				properties.setProperty("verbose", "true");
				properties.setProperty("auth.endpoint", server);
				properties.setProperty("auth.username", username);
				properties.setProperty("auth.password", password);
				properties.setProperty("auth.tenant.name", tenantId);
				properties.setProperty("identity.endpoint.publicURL", String.format("http://%s:5000/v2.0", server));
				properties.setProperty("identity.endpoint.internalURL", String.format("http://%s:5000/v2.0", server));
				properties.setProperty("identity.endpoint.adminURL", String.format("http://%s:35357/v2.0", server));
				properties.setProperty("identity.admin.token", "secret0");
				
				service = OpenStackClient.authenticate(properties);
			} else {
				InputStream is = null;
				try {
					if (configFile.equals("-")) {
						// Read from stdin
						// Don't auto-close it, and that terminates nailgun
						// is = new NoCloseInputStream(System.in);
						is = System.in;
					} else {
						if (isServerMode()) {
							throw new IllegalArgumentException("Must pass config file over stdin in server mode");
						}
						File file = new File(configFile);
						if (!file.exists())
							throw new FileNotFoundException("Configuration file not found: " + file);

						is = new FileInputStream(file);
					}

					Properties properties = new Properties();
					try {
						properties.load(is);
					} catch (IOException e) {
						throw new IOException("Error reading configuration file", e);
					}
//					String server = properties.getProperty("openstack.auth");
//					String username = properties.getProperty("openstack.username");
//					String password = properties.getProperty("openstack.password");
//					String tenantId = properties.getProperty("openstack.tenant");

					service = OpenStackClient.authenticate(properties);
				} catch (IOException e) {
					throw new IllegalArgumentException("Error reading configuration file", e);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		}

		return service;
	}


}