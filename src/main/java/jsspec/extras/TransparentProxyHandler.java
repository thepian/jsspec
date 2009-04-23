package jsspec.extras;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.log.Log;
import sun.misc.BASE64Encoder;

public class TransparentProxyHandler extends AbstractHandler {
    private static final java.lang.String METHOD_POST = "POST";
    protected static String AUTH_HEADER = "Authorization";

    protected Map<String,String> extraHeaders = new HashMap<String,String>();
    {
        extraHeaders.put("Cache-Control","no-cache");
        extraHeaders.put("Pragma","no-cache");
        extraHeaders.put ("Expires", "0");
    }

    protected HashSet<String> _DontProxyHeaders = new HashSet<String>();

    {
        _DontProxyHeaders.add("keep-alive");
    }

    protected boolean handleNotFound = false;
    protected int serverPort = 7001;
    protected String serverHost;
    protected String user = null;
    protected String pass;

    /**
     * note that if the server sends a redirect the redirect will fail or bypass this proxy
     */
    public void handle(String target, HttpServletRequest request,
                       HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        String authString = null;
        String requestMethod;
        String queryString;
        String requestPath;
        byte[] postData = null;
        byte[] responseBody = null;
        URL url;
        String uri;
        List<HeaderPair> clientRequestHeaders = new LinkedList<HeaderPair>();
        List<HeaderPair> serverResponseHeaders = new LinkedList<HeaderPair>();

        Request base_request = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled()) return;

        // Read in the method, path, headers, queryString and contents
        requestMethod = request.getMethod();
        queryString = request.getQueryString();
        //requestPortStr = "" + request.getServerPort();
        requestPath = request.getRequestURI();
        readHeaders(clientRequestHeaders, request, null);
        uri = requestPath;
        if (queryString != null)
            uri += "?" + queryString;

        if (requestMethod.equals(METHOD_POST)) {
            postData = readHTTPContent(null, request);
        }

        // perform the server hit and just read the whole lot into memory
        url = new URL(request.getScheme(),
                request.getServerName(),
                serverPort,
                uri);
        Log.debug("URL=" + url);
        if (user != null) {
            authString = user + ":" + pass;
            BASE64Encoder encoder = new BASE64Encoder();
            authString = encoder.encode(authString.getBytes());
            authString = "Basic " + authString;
            Log.debug("will authenticate with server using " + user + ":" + pass);
        }

        try {
            URLConnection connection = url.openConnection();
            connection.setAllowUserInteraction(false);
            HttpURLConnection http = null;
            if (connection instanceof HttpURLConnection) {
                http = (HttpURLConnection) connection;
                http.setRequestMethod(request.getMethod());
                http.setInstanceFollowRedirects(false);
                http.setDoOutput(true);
                for (HeaderPair pair : clientRequestHeaders) {
                    if (!_DontProxyHeaders.contains(pair.name)) {
                        http.addRequestProperty(pair.name, pair.value);
                    }
                }
                if (authString != null) {
                    http.addRequestProperty(AUTH_HEADER, authString);
                }
                for ( String name : extraHeaders.keySet() ) {
                    http.addRequestProperty(name,extraHeaders.get(name));
                }
            }
            if (postData != null) {
                writePostData(connection, postData);
            }
            // weve made the request, now start read back then send to client
            readHeaders(serverResponseHeaders, null, http);
            responseBody = readHTTPContent(connection, null);

            // write response back to client.
            for (HeaderPair pair : serverResponseHeaders) {
                response.setHeader(pair.name, pair.value);
            }
            for ( String name : extraHeaders.keySet() ) {
                response.setHeader(name,extraHeaders.get(name)); 
            }
            writeResponseData(response, responseBody);
            base_request.setHandled(true);
        } catch (FileNotFoundException fe) {
            Log.warn("URL "+target+" not found at port"+serverPort);
            if (handleNotFound) {
            	response.sendError(HttpServletResponse.SC_NOT_FOUND);
                base_request.setHandled(true);
            }
        }
    }


    private void writeResponseData(HttpServletResponse response, byte[] data) throws IOException {
        BufferedOutputStream ostream = new BufferedOutputStream(response.getOutputStream());
        ostream.write(data);
        ostream.flush();
        ostream.close();
    }

    private void writePostData(URLConnection connection, byte[] data) throws IOException {
        BufferedOutputStream ostream = new BufferedOutputStream(connection.getOutputStream());
        ostream.write(data);
        ostream.flush();
        ostream.close();
    }

    /**
     * 
     * 
     * @param connection
     * @param request
     * @return
     * @throws FileNotFoundException if the URL isn't known at the location
     */
    private byte[] readHTTPContent(URLConnection connection, HttpServletRequest request) throws IOException {
        BufferedInputStream istream;
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 5];
        int read, bytesRead = 0;
        int contentLength = 0;

		if (connection != null) {
		    istream = new BufferedInputStream(connection.getInputStream());
		    contentLength = connection.getContentLength();
		} else {
		    istream = new BufferedInputStream(request.getInputStream());
		    contentLength = request.getContentLength();
		}

        while (true) {
            try {
                read = istream.read(buffer);
                bytesRead += read;
                if (read > 1) {
                    content.write(buffer, 0, read);
                } else {
                    break;
                }
                if (contentLength >= 0 && bytesRead >= contentLength) {
                    break;
                }
            } catch (IOException ioe) {
            	Log.warn(ioe);
            }
            //(read = istream.read(buffer)) > 0
        }
        istream.close();
        return content.toByteArray();
    }

    @SuppressWarnings("unchecked")
	private void readHeaders(List list, HttpServletRequest request, HttpURLConnection connection) {
        if (request != null) {
            Enumeration enNames = request.getHeaderNames();
            while (enNames.hasMoreElements()) {
                String name = (String) enNames.nextElement();
                Enumeration enValues = request.getHeaders(name);
                while (enValues.hasMoreElements()) {
                    String value = (String) enValues.nextElement();
                    HeaderPair pair = new HeaderPair(name, value);
                    list.add(pair);
                }

            }
            return;
        }
        // must be response
        Map<String, List<String>> headers = connection.getHeaderFields();
        Set<String> headerSet = headers.keySet();

        for (String name : headerSet) {
            if (name == null)
                continue;

            List<String> values = headers.get(name);
            for (String value : values) {
                HeaderPair pair = new HeaderPair(name, value);
                list.add(pair);
            }
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(int serverPort) {
        Log.info("Extras proxy to port " + serverPort);
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    class HeaderPair {
        public String name;
        public String value;

        public HeaderPair(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
    	Log.debug("Extras proxy user " + user);
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
    	Log.debug("Extras proxy pass " + pass);
        this.pass = pass;
    }
}

