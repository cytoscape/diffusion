package org.cytoscape.diffusion.internal.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffusionServiceClient {

	private static final String BASE_URL = "http://v3.heat-diffusion.cytoscape.io/";

	private static final String HEAT_COLUMN_PARAM = "input_attribute_name";
	private static final String TIME_PARAM = "time";

	private final CyProperty<Properties> props;
	private HttpClient client = null;
	private final static Logger logger = LoggerFactory.getLogger(DiffusionServiceClient.class);
	
	public DiffusionServiceClient(CyProperty<Properties> props) {
		this.props = props;
		
	}

	private HttpClient getHttpClient() {
		if (client == null) {
			client = HttpClients.createDefault();
		}
		return client;
	}
	
	public String diffuse(final String cx, final String inputHeatCol, final Double time) throws IOException {
		try {
			final URI uri = getReqeuestURI(inputHeatCol, time);
			final HttpPost post = new HttpPost(uri.toString());

			final StringEntity cxEntity = new StringEntity(cx);
			post.setEntity(cxEntity);
			post.setHeader("Content-type", "application/json");
			final HttpResponse response = getHttpClient().execute(post);
			final HttpEntity entity = response.getEntity();
			if (entity == null) {
				throw new IOException(createConnectionError("Response from diffusion service is null."));
			}
			final String result = EntityUtils.toString(entity);
			return result;
		} catch (Exception e) {
			logger.error("Connection error contacting the heat diffusions service");
			logger.error(createConnectionError(e.toString()), new IOException());
			throw new IOException(createConnectionError(e.toString()));
		}
	}

	private URI getReqeuestURI(final String inputHeatCol, final Object time) throws URISyntaxException {
		final List<NameValuePair> postParams = new ArrayList<>();

		if (time != null) {
			postParams.add(new BasicNameValuePair(TIME_PARAM, time.toString()));
		}

		if (inputHeatCol != null) {
			postParams.add(new BasicNameValuePair(HEAT_COLUMN_PARAM, inputHeatCol));
		}

		postParams.add(new BasicNameValuePair("threshold", "-1"));

		final Object serviceUrlProp = props.getProperties().get("diffusion.url");
		URIBuilder uriBuilder;
		if (serviceUrlProp != null) {
			uriBuilder = new URIBuilder((String) serviceUrlProp);
		} else {
			uriBuilder = new URIBuilder(BASE_URL);
		}		
		uriBuilder.addParameters(postParams);
		return uriBuilder.build();
	}

	private String createConnectionError(String errorMessage) {
		return String.format("Oops! An error occurred while connecting to the heat diffusion service in the cloud.\n\n "
				+ "You may be disconnected from the internet, the service may be unavailable, or some other problem with the connection might have occurred.\n"
				+ "Check your connection, check that you can resolve the host name diffuse.cytoscape.io, and check your firewall settings before retying.\n"
				+ "After checking that everything is ok, wait a while and try again, or if the problem persists, contact the app author for help.\n\n"
				+ "Cytoscape encountered this error while connecting:\n %s", errorMessage);
	}
}
