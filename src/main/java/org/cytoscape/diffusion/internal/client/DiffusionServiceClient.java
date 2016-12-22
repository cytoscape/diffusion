package org.cytoscape.diffusion.internal.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class DiffusionServiceClient {

	private static final String BASE_URL = "http://diffuse.cytoscape.io";

	private final String url;
	final HttpClient client;

	public DiffusionServiceClient() {
		this(BASE_URL);
	}

	public DiffusionServiceClient(final String serviceURL) {
		this.url = serviceURL;
		this.client = HttpClients.createDefault();
	}

	public String diffuse(final String cx, final String subnetId) throws ClientProtocolException, IOException {
		final String endpoinrUrl = String.format("%s/%s", url, subnetId);
		final HttpPost post = new HttpPost(endpoinrUrl);

		final StringEntity cxEntity = new StringEntity(cx);
		post.setEntity(cxEntity);
		post.setHeader("Content-type", "application/json");

		final HttpResponse response = client.execute(post);
		final HttpEntity entity = response.getEntity();
		return entity != null ? EntityUtils.toString(entity) : null;
	}
}
