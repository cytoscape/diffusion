package org.cytoscape.diffusion.internal.task;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author churas
 */
public class HttpClientFactoryImpl implements HttpClientFactory {

    private RequestConfig _config;
    public HttpClientFactoryImpl(final int connectTimeOutMillis,
            final int connectRequestTimeoutMillis,
            final int socketTimeoutMillis){
        _config = RequestConfig.custom().setConnectTimeout(connectTimeOutMillis)
                .setConnectionRequestTimeout(connectRequestTimeoutMillis)
                .setSocketTimeout(socketTimeoutMillis).build();
    }
    
    public HttpClientFactoryImpl(){
        _config = null;
    }
    
    @Override
    public HttpClient getHttpClient() {
	if (_config == null){
	    return HttpClientBuilder.create().build();
	}
	return HttpClientBuilder.create().setDefaultRequestConfig(_config).build();
    }
    
}
