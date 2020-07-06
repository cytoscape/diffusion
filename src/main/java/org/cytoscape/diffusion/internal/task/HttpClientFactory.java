package org.cytoscape.diffusion.internal.task;

import org.apache.http.client.HttpClient;

/**
 * Factory to create {@link org.apache.http.client.HttpClient} objects
 * @author churas
 */
public interface HttpClientFactory {
    
    /**
     * Create {@link org.apache.http.client.HttpClient} 
     * @param config
     * @return 
     */
    public HttpClient getHttpClient();
}
