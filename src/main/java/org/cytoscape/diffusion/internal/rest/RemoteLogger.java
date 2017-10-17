package org.cytoscape.diffusion.internal.rest;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.UnknownHostException;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.SDElement;
import com.cloudbees.syslog.SDParam;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;

/**
 * This is a temporary implementation of remote logging, intended to be taken over by CyREST endpoint logging.
 * 
 * If CyREST has remote logging, this class shouldn't be here at all.
 * 
 * @author David Otasek (dotasek.dev@gmail.com)
 *
 */
public class RemoteLogger {

	public static final String INSTALLOPTIONS_SHARESTATISTICS = "installoptions.shareStatistics";

	public static final String CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER = "cytoscape.remotelogging.syslogserver";

	public static final String CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT = "cytoscape.remotelogging.syslogserverport";

	private static final Logger logger = LoggerFactory.getLogger(RemoteLogger.class);

	static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
	static String jvmName = runtimeBean.getName();

	private static boolean enabled = false;

	private UdpSyslogMessageSender messageSender;

	public static void configureFromCyProperties(CyProperty<Properties> cyProps) 
	{
		try 
		{
			String shareStatistics = cyProps.getProperties().getProperty(RemoteLogger.INSTALLOPTIONS_SHARESTATISTICS);
			if (shareStatistics != null && shareStatistics.equalsIgnoreCase("true")) {
				RemoteLogger.setEnabled(true);
			}
			String syslogServerport = cyProps.getProperties().getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT);
			if (syslogServerport != null) {
				try {
					Integer portNumber = Integer.valueOf(syslogServerport);
					RemoteLogger.getDefaultLogger().messageSender.setSyslogServerPort(portNumber);
				} catch (Throwable e) {
					logger.error("Could not set remote logging syslog server port from properties");
					throw e;
				}
			}
			String syslogServer = cyProps.getProperties().getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER);
			if (syslogServer != null) {
				try {
					setSyslogServerHostname(RemoteLogger.getDefaultLogger().messageSender, syslogServer);
				} catch (Throwable e) {	
					logger.error("Could not set remote logging syslog server from properties");
					throw e;
				}
			}
		} catch (Throwable e) {
			RemoteLogger.resetDefaultLogger();
			logger.error("Could not configure syslog server from properties", e);
		}
	}

	public RemoteLogger(UdpSyslogMessageSender messageSender) {
		try {	
			this.messageSender = messageSender;
			this.messageSender.setDefaultAppName("cytoscape");
			this.messageSender.setDefaultFacility(Facility.USER);
			this.messageSender.setDefaultSeverity(Severity.INFORMATIONAL);
			this.messageSender.setMessageFormat(MessageFormat.RFC_5424); // optional, default is RFC 3164
		} catch (Throwable e) {
			logger.error("Error instantiating UdpSyslogMessageSender", e);
			this.messageSender = null;
		}
	}

	public static final String DEFAULT_SYSLOG_SERVER_HOSTNAME = "35.197.10.101";
	public static final int DEFAULT_SYSLOG_SERVER_PORT = 3333;

	public RemoteLogger() {
		this(DEFAULT_SYSLOG_SERVER_HOSTNAME, DEFAULT_SYSLOG_SERVER_PORT);
	}

	private static final RemoteLogger defaultLogger = new RemoteLogger();
	public static final RemoteLogger getDefaultLogger() {
		return defaultLogger;
	}

	public void setSyslogServerHostname(String syslogServerHostname) {
		try {
			setSyslogServerHostname(messageSender, syslogServerHostname);
		} catch (UnknownHostException e) {
			logger.error("Could not set syslog server host name: unknown host", e);
		}
	}

	private static void setSyslogServerHostname(UdpSyslogMessageSender messageSender, String syslogServerHostname) throws UnknownHostException {
		java.net.InetAddress.getByName(syslogServerHostname);
		messageSender.setSyslogServerHostname(syslogServerHostname);	
	}

	public String getSyslogServerHostname() {
		return messageSender.getSyslogServerHostname();
	}

	public void setSyslogServerPort(int syslogServerPort) {
		messageSender.setSyslogServerPort(syslogServerPort);
	}

	public int getSyslogServerPort() {
		return messageSender.getSyslogServerPort();
	}

	public static void resetDefaultLogger() {
		defaultLogger.messageSender.setSyslogServerHostname(DEFAULT_SYSLOG_SERVER_HOSTNAME);
		defaultLogger.messageSender.setSyslogServerPort(DEFAULT_SYSLOG_SERVER_PORT);
	}

	public RemoteLogger(UdpSyslogMessageSender messageSender, String syslogServerHostname, int syslogServerPort) {
		this(messageSender);
		try {	
			this.messageSender.setSyslogServerHostname(syslogServerHostname);
			this.messageSender.setSyslogServerPort(syslogServerPort);
		} catch (Throwable e) {
			this.messageSender = null;
			logger.error("Error instantiating UdpSyslogMessageSender", e);
		}
	}
	public RemoteLogger(String syslogServerHostname, int syslogServerPort) {
		this(new UdpSyslogMessageSender(), syslogServerHostname, syslogServerPort);
	}

	private static String publicIP = null;

	public static String getPublicIP() {
		try {
			if (publicIP != null) {
				return publicIP; 
			} else {
				String ip = null;
				if (ip != null) {
					publicIP = ip;
				}
				return publicIP;
			}
		}
		finally {
			publicIP = null;
			return "0.0.0.0";
		}
	}

	// Main method to manually send a Syslog message for testing.
	public static void main(String[] args) {
		try {
			setEnabled(true);
			RemoteLogger logger = RemoteLogger.defaultLogger;
			long systemTime = System.currentTimeMillis();
			logger.logResourceError("dummyHttpMethod",  "dummyPath", 664, "dummy:urn");
			long elapsedTime = System.currentTimeMillis() - systemTime;

			System.out.println(elapsedTime);
			System.out.println(logger.messageSender.getSendDurationInMillis());
		} catch (Throwable e) {
			System.out.println("Caught throwable.");
			e.printStackTrace();
		}
	}

	public static void setEnabled(boolean enabled) {
		RemoteLogger.enabled = enabled;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	private SyslogMessage getBaseMessage() {
		SyslogMessage message = new SyslogMessage()
				.withMsg("-")
				.withTimestamp(System.currentTimeMillis())
				.withFacility(Facility.USER)
				.withSeverity(Severity.INFORMATIONAL);
		return message;
	}

	private SDParam getSDParam(String paramName, String paramValue) {
		return new SDParam(paramName, paramValue != null ? paramValue : "");
	}

	private void sendMessage(SDParam... params) throws Throwable {
		if (canSend()) {
			SDParam[] newParams = new SDParam[params.length + 2];
			newParams[0] = getSDParam("jvmName", jvmName);
			newParams[1] = getSDParam("publicIP", getPublicIP());
			System.arraycopy(params, 0, newParams, 2, params.length);
			SyslogMessage message = getBaseMessage()
					.withSDElement(
							new SDElement(
									"diffusion@cytoscape", 
									newParams));
			messageSender.sendMessage(message);
		}
	}

	public static final String HTTP_METHOD = "httpMethod";
	public static final String PATH = "path";
	public static final String RESPONSE_CODE = "responseCode";
	public static final String ERROR_TYPE = "errorType";
	public static final String SERVICE_URL = "serviceUrl";

	public void logResourceResponse(String httpMethod, String path, int responseCode) {

		try {
			sendMessage(
					getSDParam(HTTP_METHOD, httpMethod),
					getSDParam(PATH, path),
					getSDParam(RESPONSE_CODE, Integer.toString(responseCode))
					);
		} catch (Throwable e) {
			logger.error("Error sending message", e);
		}

	}

	public void logResourceError(String httpMethod, String path, int responseCode, String errorUrn) {

		try {
			sendMessage(
					getSDParam(HTTP_METHOD, httpMethod),
					getSDParam(PATH, path),
					getSDParam(RESPONSE_CODE, Integer.toString(responseCode)),
					getSDParam(ERROR_TYPE, errorUrn)
					);

		} catch (Throwable e) {
			logger.error("Error sending message", e);
		}
	}	

	public boolean messageSenderNotNull() {
		return messageSender != null;
	}

	public final boolean canSend() {
		return messageSender != null && isEnabled();
	}

	public void logServiceError(String serviceUrl, String httpMethod, int responseCode, String errorUrn) {
		try {
			sendMessage(
					getSDParam(SERVICE_URL, serviceUrl),
					getSDParam(HTTP_METHOD, httpMethod),
					getSDParam(RESPONSE_CODE, Integer.toString(responseCode)),
					getSDParam(ERROR_TYPE, errorUrn)
					);

		} catch (Throwable e) {
			logger.error("Error sending message", e);
		}
	}
}
