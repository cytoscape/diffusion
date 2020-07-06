package org.cytoscape.diffusion.internal.task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link java.util.concurrent.Callable} task to run Diffusion. 
 * Using a {@link java.util.concurrent.Callable}
 * allows caller to cancel the operation. 
 * @author churas
 */
public class DiffusionCallable implements Callable {

    public static final String HEAT_COLUMN_PARAM = "input_attribute_name";
    public static final String TIME_PARAM = "time";
    
    private final static Logger logger = LoggerFactory.getLogger(DiffusionCallable.class);

    private HttpClient _client = null;
    private final String _cx;
    private final String _inputHeatCol;
    private final Double _time;
    private final String _diffusionURL;
    private HttpClientFactory _httpClientFactory;
    
    /**
     * Constructor
     * @param cx
     * @param inputHeatCol
     * @param time
     * @param diffusionURL 
     */
    public DiffusionCallable(final String cx, final String inputHeatCol, final Double time,
            final String diffusionURL){
        _cx = cx;
        _inputHeatCol = inputHeatCol;
        _time = time;
        _diffusionURL = diffusionURL;
        _httpClientFactory = new HttpClientFactoryImpl();
    }
    
    /**
      * Sets alternate {@link org.apache.http.client.HttpClient}
      * to be used by this object. 
      * @param client Alternate client to use
    */
    protected void setAlternateHttpClientFactory(HttpClientFactory clientFactory){
        _httpClientFactory = clientFactory;
    }
        
    /**
     * Call Diffusion service to run diffusion
     * @return String of result from web request
     * @throws Exception If there was an error with request
     */
    @Override
    public String call() throws Exception {
        try {
			final URI uri = getRequestURI(_inputHeatCol, _time);
			final HttpPost post = new HttpPost(uri.toString());

			final StringEntity cxEntity = new StringEntity(_cx);
			post.setEntity(cxEntity);
			post.setHeader("Content-type", "application/json");
			final HttpResponse response = _httpClientFactory.getHttpClient().execute(post);
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
    
    /**
     * Builds request URL
     * @param inputHeatCol
     * @param time
     * @return
     * @throws URISyntaxException 
     */
    private URI getRequestURI(final String inputHeatCol, final Object time) throws URISyntaxException {
		final List<NameValuePair> postParams = new ArrayList<>();

		if (time != null) {
			postParams.add(new BasicNameValuePair(TIME_PARAM, time.toString()));
		}

		if (inputHeatCol != null) {
			postParams.add(new BasicNameValuePair(HEAT_COLUMN_PARAM, inputHeatCol));
		}

		postParams.add(new BasicNameValuePair("threshold", "-1"));
		URIBuilder uriBuilder = new URIBuilder(_diffusionURL);	
		uriBuilder.addParameters(postParams);
		return uriBuilder.build();
	}
    
    /**
     * Builds error message
     * @param errorMessage
     * @return 
     */
    private String createConnectionError(String errorMessage) {
		return String.format("Oops! An error occurred while connecting to the heat diffusion service in the cloud.\n\n "
				+ "You may be disconnected from the internet, the service may be unavailable, or some other problem with the connection might have occurred.\n"
				+ "Check your connection, check that you can resolve the host name diffuse.cytoscape.io, and check your firewall settings before retying.\n"
				+ "After checking that everything is ok, wait a while and try again, or if the problem persists, contact the app author for help.\n\n"
				+ "Cytoscape encountered this error while connecting:\n %s", errorMessage);
	}
    
    
    
}
