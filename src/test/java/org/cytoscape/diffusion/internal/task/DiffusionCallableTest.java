package org.cytoscape.diffusion.internal.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class DiffusionCallableTest {
    
    @Test
    public void testCallNullEntity() throws Exception {
        HttpClientFactory mockFac = mock(HttpClientFactory.class);
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockRes = mock(HttpResponse.class);
        StatusLine mockStatus = mock(StatusLine.class);

        
        when(mockRes.getEntity()).thenReturn(null);
        when(mockFac.getHttpClient()).thenReturn(mockClient);
        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockRes);
        DiffusionCallable diffy = new DiffusionCallable("cx", "inputHeatCol", 0.1,
                "http://foo");
        diffy.setAlternateHttpClientFactory(mockFac);

        try {
            diffy.call();
            fail("Expected exception");
        } catch(Exception ex){
            assertTrue(ex.getMessage().contains("Response from diffusion service"));
        }

    }
    
    @Test
    public void testCallSuccessful() throws Exception {
        HttpClientFactory mockFac = mock(HttpClientFactory.class);
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockRes = mock(HttpResponse.class);
        StatusLine mockStatus = mock(StatusLine.class);

        HttpEntity mockEntity = mock(HttpEntity.class);
        
        InputStream iStream = new ByteArrayInputStream("Hello".getBytes());
        when(mockEntity.getContent()).thenReturn(iStream);
        when(mockRes.getEntity()).thenReturn(mockEntity);
        when(mockFac.getHttpClient()).thenReturn(mockClient);
        
        // build parameters
        
        when(mockClient.execute(any(HttpPost.class))).thenReturn(mockRes);

        
        DiffusionCallable diffy = new DiffusionCallable("cx", "inputHeatCol", 0.1,
                "http://foo");
        diffy.setAlternateHttpClientFactory(mockFac);
        assertEquals("Hello", diffy.call());
    }
    
}
