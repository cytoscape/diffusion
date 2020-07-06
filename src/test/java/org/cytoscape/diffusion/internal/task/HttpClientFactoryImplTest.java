package org.cytoscape.diffusion.internal.task;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author churas
 */
public class HttpClientFactoryImplTest {
    
     @Test
    public void testGetClient(){
        HttpClientFactoryImpl fac = new HttpClientFactoryImpl();
        HttpClient client = fac.getHttpClient();
        assertNotNull("client is null", client);
    }
}
