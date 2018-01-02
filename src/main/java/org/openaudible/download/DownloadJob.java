package org.openaudible.download;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openaudible.Directories;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.util.CopyWithProgress;
import org.openaudible.util.Util;
import org.openaudible.util.queues.IQueueJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DownloadJob implements IQueueJob {
    private static final Log LOG = LogFactory.getLog(DownloadJob.class);
    /*

    GET /download?user_id=uuuu&product_id=BK_HACH_000001&codec=LC_64_22050_ster&awtype=AAX&cust_id=xxx HTTP/1.1
    User-Agent: Audible ADM 6.6.0.19;Windows Vis
     *
     */
    static HttpClientBuilder bld = HttpClients.custom();
    final Book b;
    final File destFile;
    volatile boolean quit = false;

    public DownloadJob(Book b, File destFile) {
        this.b = b;
        this.destFile = destFile;
        assert (!destFile.exists());
    }

    public void download() throws IOException {
        String cust_id = b.get(BookElement.cust_id);
        String user_id = b.get(BookElement.user_id);

        if (cust_id.length() == 0)
            throw new IOException("cust_id required");
        if (user_id.length() == 0)
            throw new IOException("user_id required");

        String awtype = "AAX";

        String codec = b.getCodec();
        if (codec.isEmpty())
            codec = "LC_64_22050_stereo";

        String url = "http://cds.audible.com/download";
        url += "?user_id=" + user_id;
        url += "&product_id=" + b.getProduct_id();
        url += "&codec=" + codec;
        url += "&awtype=" + awtype;
        url += "&cust_id=" + cust_id;

        LOG.info("Download book: " + b + " url=" + url);

        File tmp = null;
        long start = System.currentTimeMillis();
        FileOutputStream fos = null;
        boolean success = false;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Audible ADM 6.6.0.19;Windows Vista  Build 9200");

        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {

            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(30000)
                    .build();


            bld.setDefaultRequestConfig(defaultRequestConfig);
            httpclient = bld.build();


            response = httpclient.execute(httpGet);

            int code = response.getStatusLine().getStatusCode();
            if (code != 200)
                throw new IOException(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            Header ctyp = entity.getContentType();
            if (ctyp != null) {
                if (!ctyp.getValue().contains("audio")) {
                    String err = "Download error:";

                    if (entity.getContentLength() < 256) {
                        err += EntityUtils.toString(entity);
                    }
                    err += " for " + b;
                    throw new IOException(err); //
                }
            }

            tmp = new File(Directories.getTmpDir(), destFile.getName() + ".part");

            if (tmp.exists()) {
                boolean v = tmp.delete();
                assert (v);
            }

            fos = new FileOutputStream(tmp);

            CopyWithProgress.copyWithProgress(getByteReporter(), 15000, entity.getContent(), fos);

            /// IO.copy(entity.getContent(), fos);

            if (quit) {
                success = false;
                throw new IOException("quit");
            }
            success = true;

        } finally {

            response.close();
            if (fos != null)
                fos.close();

            if (httpclient != null)
                httpclient.close();

            if (success) {
                if (tmp != null) {
                    boolean ok = tmp.renameTo(destFile);
                    if (!ok)
                        throw new IOException("failed to rename." + tmp.getAbsolutePath() + " to " + destFile.getAbsolutePath());
                }

                long time = System.currentTimeMillis() - start;
                long bytes = destFile.length();
                double bps = bytes / (time / 1000.0);
                LOG.info("Downloaded " + destFile.getName() + " bytes=" + bytes + " time=" + time + " Kbps=" + (int) (bps / 1024.0));

            } else {
                if (tmp != null)
                    tmp.delete();
                destFile.delete();
            }

        }
    }

    private CopyWithProgress.ByteReporter getByteReporter() {

        CopyWithProgress.ByteReporter br = new CopyWithProgress.ByteReporter() {
            long startTime = System.currentTimeMillis();

            public void bytesCopied(long total) throws IOException {
                double seconds = (System.currentTimeMillis() - startTime) / 1000.0;
                double bps = total / seconds;
                //bps = ((long)(bps*1000))/1000.0;
                String rate = Util.instance.byteCountToString((long) bps) + "/sec";

                LOG.info("Downloading " + Util.instance.byteCountToString(total) + " bytes " + b + " @ " + rate);
                if (quit) {
                    throw new IOException("quit");
                }

            }

        };

        return br;
    }

    public void quit() {
    }

    @Override
    public void processJob() throws Exception {
        download();
    }

    @Override
    public void quitJob() {
        quit = true;
    }

    @Override
    public String toString() {
        return "Download " + b;
    }

}
