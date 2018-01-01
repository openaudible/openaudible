package org.openaudible.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openaudible.desktop.swt.manager.Version;

import java.io.IOException;

public enum HTTPGet {
    instance;

    public JsonObject getJSON(String url) throws IOException
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(Version.versionLink);
            CloseableHttpResponse httpResponse = httpclient.execute(httpget);
            try {

                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                {
                    String entity = EntityUtils.toString(httpEntity);
                    JsonParser parser = new JsonParser();
                    JsonObject obj = parser.parse(entity).getAsJsonObject();
                    return obj;
                }
                throw new IOException(httpResponse.getStatusLine().getReasonPhrase());
            } finally {
                httpResponse.close();
            }
        } finally
        {
            httpclient.close();
        }
    }

}
