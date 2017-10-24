package org.cytoscape.diffusion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cytoscape.diffusion.internal.rest.RemoteLogger;
import org.cytoscape.property.CyProperty;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;

import com.cloudbees.syslog.SDElement;
import com.cloudbees.syslog.SDParam;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;

/**
 * This class is temporarily in place to support RemoteLogger, which is in turn temporarily in place until CyREST has 
 * its own centralized logging.
 * 
 * If you're seeing this, and CyREST is taking care of its own logging, this should be deleted.
 * 
 * @author David Otasek (dotasek.dev@gmail.com)
 *
 */
public class LoggingTests {
	
	static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
	static String jvmName = runtimeBean.getName();
	
	SyslogMessage lastSyslogMessage;
	
	UdpSyslogMessageSender mockSender;
	
	String mockSenderAddressServiceServer = "0.0.0.0";
	
	@BeforeClass
	public static void checkDefaultSetting() {
		assertFalse(RemoteLogger.isEnabled());
	}
	
	public LoggingTests() throws IOException {
		mockSender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				lastSyslogMessage = (SyslogMessage)invocation.getArguments()[0];
				return null;
			}
		}).when(mockSender).sendMessage(any(SyslogMessage.class));
	}
	
	@Before
	public void clearSyslogMessage() {
		RemoteLogger.resetDefaultLogger();
		lastSyslogMessage = null;
	}
	
	@Test
	public void sdParamSanityCheck() {
		assertFalse(new SDParam("a", "b").equals(new SDParam("a", "c")));
		assertTrue(new SDParam("a", "b").equals(new SDParam("a", "b")));
	}
	
	@Test 
	public void exceptionInConstructorTest() {
		UdpSyslogMessageSender mockSender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Exception {
				throw new Exception("UdpSend setDefaultAppName exception");
			}
		}).when(mockSender).setDefaultAppName(anyString());
		
		RemoteLogger logger = new RemoteLogger(mockSender, "fake ip", 664, "fakeService");
		assertFalse(logger.messageSenderNotNull());
		assertFalse(logger.canSend());
	}
	
	@Test 
	public void exceptionInOtherConstructorTest() {
		UdpSyslogMessageSender mockSender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Exception {
				throw new Exception("UdpSend setDefaultAppName exception");
			}
		}).when(mockSender).setSyslogServerHostname(anyString());
		
		RemoteLogger logger = new RemoteLogger(mockSender, "fake ip", 664, "fakeService");
		assertFalse(logger.messageSenderNotNull());
		assertFalse(logger.canSend());
	}
	
	// Simple message send
	@Test
	public void successfulResponseMessageSend() throws Exception {
		assertNull(lastSyslogMessage);

		RemoteLogger logger = new RemoteLogger(mockSender, mockSenderAddressServiceServer);
		assertTrue(logger.messageSenderNotNull());
		
		RemoteLogger.setEnabled(true);
		assertTrue(logger.canSend());
		
		long systemTime = System.nanoTime();
		logger.logResourceResponse("dummyHttpMethod", "dummyPath", 664);
		long elapsedTime = System.nanoTime() - systemTime;
		System.out.println("Successful request time: " + elapsedTime);
		
		assertNotNull(lastSyslogMessage);
		
		Set<SDElement> elements = lastSyslogMessage.getSDElements();
		assertEquals(1, elements.size());
		SDElement element = elements.iterator().next();
		assertEquals("diffusion@cytoscape", element.getSdID());
		List<SDParam> params = element.getSdParams();
		assertEquals(5, params.size());
		for (SDParam sdParam : params) {
			System.out.println("\""+sdParam.getParamName() + "\", \"" + sdParam.getParamValue() + "\"");
		}
		assertTrue(params.contains(new SDParam("jvmName",jvmName)));
		assertTrue(params.contains(new SDParam("publicIP", "0.0.0.0")));
		assertTrue(params.contains(new SDParam("httpMethod", "dummyHttpMethod")));
		assertTrue(params.contains(new SDParam("path", "dummyPath")));
		assertTrue(params.contains(new SDParam("responseCode", "664")));
	}
	
	// Simple message send
	@Test
	public void failedResponseMessageSend() throws Exception {
		assertNull(lastSyslogMessage);

		RemoteLogger logger = new RemoteLogger(mockSender, mockSenderAddressServiceServer);
		RemoteLogger.setEnabled(true);
			
		long systemTime = System.nanoTime();
		logger.logResourceError("dummyHttpMethod",  "dummyPath", 664, "urn:dummyurn");
		long elapsedTime = System.nanoTime() - systemTime;
		System.out.println("Successful error report time: " + elapsedTime);
		assertNotNull(lastSyslogMessage);
		
		Set<SDElement> elements = lastSyslogMessage.getSDElements();
		assertEquals(1, elements.size());
		SDElement element = elements.iterator().next();
		assertEquals("diffusion@cytoscape", element.getSdID());
		List<SDParam> params = element.getSdParams();
		assertEquals(6, params.size());
		for (SDParam sdParam : params) {
			System.out.println("\""+sdParam.getParamName() + "\", \"" + sdParam.getParamValue() + "\"");
		}
		assertTrue(params.contains(new SDParam("jvmName",jvmName)));
		assertTrue(params.contains(new SDParam("publicIP", "0.0.0.0")));
		assertTrue(params.contains(new SDParam("httpMethod", "dummyHttpMethod")));
		assertTrue(params.contains(new SDParam("path", "dummyPath")));
		assertTrue(params.contains(new SDParam("responseCode", "664")));
		assertTrue(params.contains(new SDParam("errorType", "urn:dummyurn")));

	}
	
	// Simple message send
		@Test
		public void failedServiceResponseMessageSend() throws Exception {
			assertNull(lastSyslogMessage);

			RemoteLogger logger = new RemoteLogger(mockSender, mockSenderAddressServiceServer);
			RemoteLogger.setEnabled(true);
				
			long systemTime = System.nanoTime();
			logger.logServiceError("dummyServiceUrl",  "dummyHttpMethod", 664, "urn:dummyurn");
			long elapsedTime = System.nanoTime() - systemTime;
			System.out.println("Successful error report time: " + elapsedTime);
			assertNotNull(lastSyslogMessage);
			
			systemTime = System.nanoTime();
			logger.logServiceError("dummyServiceUrl",  "dummyHttpMethod", 664, "urn:dummyurn");
			elapsedTime = System.nanoTime() - systemTime;
			System.out.println("Successful error report time: " + elapsedTime);
			assertNotNull(lastSyslogMessage);
			
			Set<SDElement> elements = lastSyslogMessage.getSDElements();
			assertEquals(1, elements.size());
			SDElement element = elements.iterator().next();
			assertEquals("diffusion@cytoscape", element.getSdID());
			List<SDParam> params = element.getSdParams();
			assertEquals(6, params.size());
			for (SDParam sdParam : params) {
				System.out.println("\""+sdParam.getParamName() + "\", \"" + sdParam.getParamValue() + "\"");
			}
			assertTrue(params.contains(new SDParam("jvmName",jvmName)));
			assertTrue(params.contains(new SDParam("publicIP", "0.0.0.0")));
			assertTrue(params.contains(new SDParam("httpMethod", "dummyHttpMethod")));
			assertTrue(params.contains(new SDParam("serviceUrl", "dummyServiceUrl")));
			assertTrue(params.contains(new SDParam("responseCode", "664")));
			assertTrue(params.contains(new SDParam("errorType", "urn:dummyurn")));
		}
	
	@Test
	public void testNoSendIfNotEnabled() throws Exception 
	{
		assertNull(lastSyslogMessage);

		RemoteLogger logger = new RemoteLogger(mockSender, mockSenderAddressServiceServer);
		RemoteLogger.setEnabled(false);
		assertTrue(logger.messageSenderNotNull());
		assertFalse(logger.canSend());
		
		
		logger.logResourceResponse("dummyHttpMethod", "dummyPath", 664);
		
		assertNull(lastSyslogMessage);
	}
	
	@Test
	public void testNoSendIfBadlyConfigured() throws Exception 
	{
		assertNull(lastSyslogMessage);
		UdpSyslogMessageSender sender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Exception {
				throw new Exception("UdpSend Exception");
			}
		}).when(sender).setSyslogServerHostname(anyString());
		
		RemoteLogger logger = new RemoteLogger(sender, mockSenderAddressServiceServer);
		
		long systemTime = System.nanoTime();
		RemoteLogger.setEnabled(true);
		logger.logResourceResponse("dummyHttpMethod", "dummyPath", 664);
		long elapsedTime = System.nanoTime() - systemTime;
		System.out.println("Failed request time: " + elapsedTime);
		assertNull(lastSyslogMessage);
	}
	
	@Test
	public void testNoSendIfUdpSendFails() throws Exception 
	{
		assertNull(lastSyslogMessage);

		UdpSyslogMessageSender sender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Exception {
				throw new Exception("UdpSend Exception");
			}
		}).when(sender).sendMessage(any(SyslogMessage.class));
		
		RemoteLogger logger = new RemoteLogger(sender, mockSenderAddressServiceServer);
		
		long systemTime = System.nanoTime();
		RemoteLogger.setEnabled(true);
		logger.logResourceResponse("dummyHttpMethod", "dummyPath", 664);
		long elapsedTime = System.nanoTime() - systemTime;
		System.out.println("Failed request time: " + elapsedTime);
		assertNull(lastSyslogMessage);
	}
	
	@Test
	public void testNoSendIfUdpSendFailsClient() throws Exception 
	{
		assertNull(lastSyslogMessage);

		UdpSyslogMessageSender sender = mock(UdpSyslogMessageSender.class);
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Exception {
				throw new Exception("UdpSend Exception");
			}
		}).when(sender).sendMessage(any(SyslogMessage.class));
		
		RemoteLogger logger = new RemoteLogger(sender, mockSenderAddressServiceServer);
		
		long systemTime = System.nanoTime();
		RemoteLogger.setEnabled(true);
		logger.logServiceError("dummyHttpMethod", "dummyPath", 664, "urn:dummyurn");
		long elapsedTime = System.nanoTime() - systemTime;
		System.out.println("Failed request time: " + elapsedTime);
		assertNull(lastSyslogMessage);
	}
	
	@Test 
	public void testConfigFromProperties() {
		CyProperty<Properties> cyProps =  mock(CyProperty.class);
		Properties properties = mock(Properties.class);
		
		when(properties.getProperty("installoptions.shareStatistics")).thenReturn("true");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT)).thenReturn("668");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER)).thenReturn("1.2.3.4");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SENDERADDRESSSERVICEHOSTNAME)).thenReturn("1.2.3.5");
		when(cyProps.getProperties()).thenReturn(properties);
		
		RemoteLogger.configureFromCyProperties(cyProps);
		assertEquals("1.2.3.4", RemoteLogger.getDefaultLogger().getSyslogServerHostname());
		assertEquals(668, RemoteLogger.getDefaultLogger().getSyslogServerPort());
		assertEquals("1.2.3.5", RemoteLogger.getDefaultLogger().getSenderAddressServiceHostname());
	}
	
	@Test 
	public void testConfigFromPropertiesFailIfBadSyslogServer() throws UnknownHostException {
		CyProperty<Properties> cyProps =  mock(CyProperty.class);
		Properties properties = mock(Properties.class);
		
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT)).thenReturn("668");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER)).thenReturn("notavalidserver");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SENDERADDRESSSERVICEHOSTNAME)).thenReturn("1.2.3.5");
		
		when(cyProps.getProperties()).thenReturn(properties);
		
		RemoteLogger.configureFromCyProperties(cyProps);
		assertEquals(java.net.InetAddress.getByName(RemoteLogger.DEFAULT_SYSLOG_SERVER_HOSTNAME).getHostName(), RemoteLogger.getDefaultLogger().getSyslogServerHostname());
		assertEquals(RemoteLogger.DEFAULT_SYSLOG_SERVER_PORT, RemoteLogger.getDefaultLogger().getSyslogServerPort());
		assertEquals(RemoteLogger.DEFAULT_SENDER_ADDRESS_SERVICE_HOSTNAME, RemoteLogger.getDefaultLogger().getSenderAddressServiceHostname());
	}
	
	@Test 
	public void testConfigFromPropertiesFailIfBadPort() throws UnknownHostException {
		CyProperty<Properties> cyProps =  mock(CyProperty.class);
		Properties properties = mock(Properties.class);
		
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT)).thenReturn("not a parsable number");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER)).thenReturn("1.2.3.4");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SENDERADDRESSSERVICEHOSTNAME)).thenReturn("1.2.3.5");
		when(cyProps.getProperties()).thenReturn(properties);
		
		RemoteLogger.configureFromCyProperties(cyProps);
		assertEquals(java.net.InetAddress.getByName(RemoteLogger.DEFAULT_SYSLOG_SERVER_HOSTNAME).getHostName(), RemoteLogger.getDefaultLogger().getSyslogServerHostname());
		assertEquals(RemoteLogger.DEFAULT_SYSLOG_SERVER_PORT, RemoteLogger.getDefaultLogger().getSyslogServerPort());
		assertEquals(RemoteLogger.DEFAULT_SENDER_ADDRESS_SERVICE_HOSTNAME, RemoteLogger.getDefaultLogger().getSenderAddressServiceHostname());
	}
	
	@Test 
	public void testConfigFromPropertiesFailIfBadSenderAddressService() throws UnknownHostException, MalformedURLException {
		CyProperty<Properties> cyProps =  mock(CyProperty.class);
		Properties properties = mock(Properties.class);
	
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVERPORT)).thenReturn("668");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SYSLOGSERVER)).thenReturn("1.2.3.4");
		when(properties.getProperty(RemoteLogger.CYTOSCAPE_REMOTELOGGING_SENDERADDRESSSERVICEHOSTNAME)).thenReturn("notavalidserver:-9");
		
		when(cyProps.getProperties()).thenReturn(properties);
		
		RemoteLogger.configureFromCyProperties(cyProps);
		assertEquals(java.net.InetAddress.getByName(RemoteLogger.DEFAULT_SYSLOG_SERVER_HOSTNAME).getHostName(), RemoteLogger.getDefaultLogger().getSyslogServerHostname());
		assertEquals(RemoteLogger.DEFAULT_SYSLOG_SERVER_PORT, RemoteLogger.getDefaultLogger().getSyslogServerPort());
		assertEquals(RemoteLogger.DEFAULT_SENDER_ADDRESS_SERVICE_HOSTNAME, RemoteLogger.getDefaultLogger().getSenderAddressServiceHostname());

	}
	
	@Test 
	public void testSetServer() throws UnknownHostException {
		RemoteLogger.getDefaultLogger().setSyslogServerHostname("1.2.3.4");
		assertEquals("1.2.3.4", RemoteLogger.getDefaultLogger().getSyslogServerHostname());
	
	}
	
	@Test 
	public void testSetServerFailIfInvalid() throws UnknownHostException {
		RemoteLogger.getDefaultLogger().setSyslogServerHostname("notavalidserver");
		assertEquals(java.net.InetAddress.getByName(RemoteLogger.DEFAULT_SYSLOG_SERVER_HOSTNAME).getHostName(), RemoteLogger.getDefaultLogger().getSyslogServerHostname());
	
	}
}
