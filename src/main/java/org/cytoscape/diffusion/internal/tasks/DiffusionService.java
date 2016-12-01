package org.cytoscape.diffusion.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class DiffusionService {

  private static final String baseURL = "http://diffuse.cytoscape.io";

  public static String diffuse(String cx, String subnetId) throws ClientProtocolException, IOException {
      HttpClient client = HttpClients.createDefault();
      String URL = String.format("%s/%s", baseURL, subnetId);
      HttpPost post = new HttpPost(URL);
      StringEntity cxEntity = new StringEntity(cx);
      post.setEntity(cxEntity);
      post.setHeader("Content-type", "application/json");
      HttpResponse  response = client.execute(post);
      HttpEntity entity = response.getEntity();
      String output = entity != null ? EntityUtils.toString(entity) : null;
      System.out.println(output);
      return output;
  }



}
