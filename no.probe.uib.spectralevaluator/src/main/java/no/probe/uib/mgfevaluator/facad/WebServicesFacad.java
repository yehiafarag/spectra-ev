package no.probe.uib.mgfevaluator.facad;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static javax.print.attribute.standard.ReferenceUriSchemesSupported.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Interface for uniProt web services
 *
 * @author Yehia Farag
 */
public class WebServicesFacad {

    public String doPost(String uri, List< NameValuePair> form) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            System.out.println("Executing request " + httpPost.getRequestLine());
            // Create a custom response handler
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity responseEntity = response.getEntity();
                    return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                } else {
                    System.err.println("error " + response);
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            return httpclient.execute(httpPost, responseHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public String doGetCurl(String uri) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            //HTTP GET method
            HttpGet httpget = new HttpGet(uri);
            // Create a custom response handler
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    System.out.println("error in response code " + status);
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            return httpclient.execute(httpget, responseHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public File downloadFileFromZipFolder(String uri, String entryName, File file) {
        FileOutputStream fos = null;
        try {

            URL downloadableFile = new URL(uri);
            URLConnection conn = downloadableFile.openConnection();
            conn.addRequestProperty("Accept", "*/*");
            conn.setDoInput(true);
            ZipInputStream Zis = new ZipInputStream(conn.getInputStream());
            ZipEntry entry = Zis.getNextEntry();

            while (entry != null) {
                if (entry.getName().contains(entryName)) {
                    try (ReadableByteChannel rbc = Channels.newChannel(Zis)) {
                        fos = new FileOutputStream(file);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        fos.close();
                        rbc.close();
                        Zis.close();
                        break;
                    }
                }

                entry = Zis.getNextEntry();
            }
        } catch (MalformedURLException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } catch (IOException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
                }
            }

        }
        return file;

    }

    public File FTPDownloadFile(String uri, File file) {
        System.out.println("to store ftp into "+file.getName());
        FileOutputStream fos = null;
        FTPClient ftpClient = new FTPClient();
         String server = "ftp.pride.ebi.ac.uk";
         uri = uri.replace(server, "");
        int port = 21;
        String user = "anonymous";
        String pass = "";
        System.out.println("start ftp ");
        try {
           
            System.out.println("at ftp uri "+uri);
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            // APPROACH #1: using retrieveFile(String, OutputStream)
         
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(file));
            boolean success = ftpClient.retrieveFile(uri, outputStream1);
            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");

            }else{
                return null;
            }

        } catch (MalformedURLException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } catch (IOException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } finally {

            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(WebServicesFacad.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
                }
            }

        }
        return file;

    }

    public File downloadFile(String uri, File file) {
        FileOutputStream fos = null;
        try {

            URL downloadableFile = new URL(uri);
            URLConnection conn = downloadableFile.openConnection();
            conn.addRequestProperty("Accept", "*/*");
            conn.setDoInput(true);
            InputStream in = conn.getInputStream();
            try (ReadableByteChannel rbc = Channels.newChannel(in)) {
                fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
                in.close();

            } catch (MalformedURLException ex) {
                System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
                    }
                }

            }

        } catch (MalformedURLException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } catch (IOException ex) {
            System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    System.err.println("at Error: " + this.getClass().getName() + " : " + ex);
                }
            }

        }
        return file;

    }

}
