package org.openaudible.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public enum HTTPGet {
    instance;

    public JSONObject getJSON(String url) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(url);
            try (CloseableHttpResponse httpResponse = httpclient.execute(httpget)) {

                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String entity = EntityUtils.toString(httpEntity);
                    return new JSONObject(entity);

                }
                throw new IOException(httpResponse.getStatusLine().getReasonPhrase());
            }
        }
    }

}
