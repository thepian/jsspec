package jsspec.extras;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.log.Log;
import org.mortbay.util.IO;

public class ProxyHandler extends AbstractHandler {

    protected HashSet<String> _DontProxyHeaders = new HashSet<String>();
    {
        _DontProxyHeaders.add("proxy-connection");
        _DontProxyHeaders.add("connection");
        _DontProxyHeaders.add("keep-alive");
        _DontProxyHeaders.add("transfer-encoding");
        _DontProxyHeaders.add("te");
        _DontProxyHeaders.add("trailer");
        _DontProxyHeaders.add("proxy-authorization");
        _DontProxyHeaders.add("proxy-authenticate");
        _DontProxyHeaders.add("upgrade");
    }
    
    protected int serverPort = 7001;
	
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

        Request base_request = request instanceof Request?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled()) return;
		
        if ("CONNECT".equalsIgnoreCase(request.getMethod()))
        {
            handleConnect(request,response);
        }
        else
        {
            String uri=request.getRequestURI();
            if (request.getQueryString()!=null)
                uri+="?"+request.getQueryString();
            URL url = new URL(request.getScheme(),
                    		  request.getServerName(),
                    		  serverPort,
                    		  uri);
            Log.debug("URL="+url);

            URLConnection connection = url.openConnection();
            connection.setAllowUserInteraction(false);
            
            // Set method
            HttpURLConnection http = null;
            if (connection instanceof HttpURLConnection)
            {
                http = (HttpURLConnection)connection;
                http.setRequestMethod(request.getMethod());
                http.setInstanceFollowRedirects(false);
            }

            // check connection header
            String connectionHdr = request.getHeader("Connection");
            if (connectionHdr!=null)
            {
                connectionHdr=connectionHdr.toLowerCase();
                if (connectionHdr.equals("keep-alive")||
                    connectionHdr.equals("close"))
                    connectionHdr=null;
            }
            
            // copy headers
            boolean xForwardedFor=false;
            boolean hasContent=false;
            Enumeration enm = request.getHeaderNames();
            while (enm.hasMoreElements())
            {
                // TODO could be better than this!
                String hdr=(String)enm.nextElement();
                String lhdr=hdr.toLowerCase();

                if (_DontProxyHeaders.contains(lhdr))
                    continue;
                if (connectionHdr!=null && connectionHdr.indexOf(lhdr)>=0)
                    continue;

                if ("content-type".equals(lhdr))
                    hasContent=true;

                Enumeration vals = request.getHeaders(hdr);
                while (vals.hasMoreElements())
                {
                    String val = (String)vals.nextElement();
                    if (val!=null)
                    {
                        connection.addRequestProperty(hdr,val);
                        Log.debug("req "+hdr+": "+val);
                        xForwardedFor|="X-Forwarded-For".equalsIgnoreCase(hdr);
                    }
                }
            }

            // Proxy headers
            connection.setRequestProperty("Via","1.1 (jetty)");
            if (!xForwardedFor)
                connection.addRequestProperty("X-Forwarded-For",
                                              request.getRemoteAddr());

            // a little bit of cache control
            String cache_control = request.getHeader("Cache-Control");
            if (cache_control!=null &&
                (cache_control.indexOf("no-cache")>=0 ||
                 cache_control.indexOf("no-store")>=0))
                connection.setUseCaches(false);

            // customize Connection
            
            try
            {    
                connection.setDoInput(true);
                
                // do input thang!
                InputStream in=request.getInputStream();
                if (hasContent)
                {
                    connection.setDoOutput(true);
                    IO.copy(in,connection.getOutputStream());
                }
                
                // Connect
                connection.connect();    
            }
            catch (Exception e)
            {
                Log.debug("proxy",e);
            }
            
            InputStream proxy_in = null;

            // handler status codes etc.
            int code=500;
            if (http!=null)
            {
                proxy_in = http.getErrorStream();
                
                code=http.getResponseCode();
                String message = http.getResponseMessage();
                response.sendError(code,message);
                Log.debug("response = "+http.getResponseCode());
            }
            
            if (proxy_in==null)
            {
                try {proxy_in=connection.getInputStream();}
                catch (Exception e)
                {
                    Log.debug("stream",e);
                    proxy_in = http.getErrorStream();
                }
            }
            
            // clear response defaults.
            response.setHeader("Date",null);
            response.setHeader("Server",null);
            
            // set response headers
            int h=0;
            String hdr=connection.getHeaderFieldKey(h);
            String val=connection.getHeaderField(h);
            while(hdr!=null || val!=null)
            {
                String lhdr = hdr!=null?hdr.toLowerCase():null;
                if (hdr!=null && val!=null && !_DontProxyHeaders.contains(lhdr))
                    response.addHeader(hdr,val);

                Log.debug("res "+hdr+": "+val);
                
                h++;
                hdr=connection.getHeaderFieldKey(h);
                val=connection.getHeaderField(h);
            }
            response.addHeader("Via","1.1 (jetty)");

            // Handle
            if (proxy_in!=null) {
            	
//                IO.copy(proxy_in,response.getOutputStream());
            	
            	OutputStream out = response.getOutputStream();
	            int bufferSize = 10000;
	            byte buffer[] = new byte[bufferSize];
	            int len=bufferSize;
            
                while (true)
                {
                    len=proxy_in.read(buffer,0,bufferSize);
                    if (len<0 )
                        break;
                    out.write(buffer,0,len);
                }
            }
            
        }

	}

    public void handleConnect(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        String uri = request.getRequestURI();
        
        Log.debug("CONNECT: "+uri);
        
        String port = "";
        String host = "";
        
        int c = uri.indexOf(':');
        if (c>=0)
        {
            port = uri.substring(c+1);
            host = uri.substring(0,c);
            if (host.indexOf('/')>0)
                host = host.substring(host.indexOf('/')+1);
        }

        
       

        InetSocketAddress inetAddress = new InetSocketAddress (host, Integer.parseInt(port));
        
        //if (isForbidden(HttpMessage.__SSL_SCHEME,addrPort.getHost(),addrPort.getPort(),false))
        //{
        //    sendForbid(request,response,uri);
        //}
        //else
        {
            InputStream in=request.getInputStream();
            OutputStream out=response.getOutputStream();
            
            Socket socket = new Socket(inetAddress.getAddress(),inetAddress.getPort());
            Log.debug("Socket: "+socket);
            
            response.setStatus(200);
            response.setHeader("Connection","close");
            response.flushBuffer();
            
            

            Log.debug("out<-in");
            IO.copyThread(socket.getInputStream(),out);
            Log.debug("in->out");
            IO.copy(in,socket.getOutputStream());
        }
    }

	public void setServerPort(int serverPort) {
		Log.info("Extras proxy to port "+serverPort);
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}
	
}
