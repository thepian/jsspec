package jsspec.extras;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.WriterOutputStream;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.ContextHandler.SContext;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.URIUtil;

public class MultipleResourcesHandler extends AbstractHandler {

    ContextHandler _context;
    String[] _welcomeFiles={"index.html"};
    MimeTypes _mimeTypes = new MimeTypes();
    ByteArrayBuffer _cacheControl;

    PathMap contextMap = new PathMap();
    
    boolean detectNotModified = false;

    public void setDetectedNotModified(boolean setto) {
    	detectNotModified = setto;
    }
    
    public void doStart()
    throws Exception
    {
        SContext scontext = ContextHandler.getCurrentContext();
        _context = (scontext==null?null:scontext.getContextHandler());
        super.doStart();
    }

    
    public void addResourceBase(String contextPath,String resourceBase) 
    {
        try
        {
        	contextMap.put(contextPath,Resource.newResource(resourceBase));
        }
        catch (Exception e)
        {
            Log.warn(e);
            throw new IllegalArgumentException(resourceBase);
        }
    }

    

    /**
     * @return the cacheControl header to set on all static content.
     */
    public String getCacheControl()
    {
        return _cacheControl.toString();
    }

    /**
     * @param cacheControl the cacheControl header to set on all static content.
     */
    public void setCacheControl(String cacheControl)
    {
        _cacheControl=cacheControl==null?null:new ByteArrayBuffer(cacheControl);
    }

    /* 
     */
    public Resource getResource(String path,Resource baseResource) throws MalformedURLException
    {
        if (path==null || !path.startsWith("/"))
            throw new MalformedURLException(path);
        
        try
        {
            path=URIUtil.canonicalPath(path);
            Resource resource=baseResource.addPath(path);
            return resource;
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }
                    
        return null;
    }

    public String[] getWelcomeFiles()
    {
        return _welcomeFiles;
    }

    public void setWelcomeFiles(String[] welcomeFiles)
    {
        _welcomeFiles=welcomeFiles;
    }
    
    protected Resource getWelcome(Resource directory) throws MalformedURLException, IOException
    {
        for (int i=0;i<_welcomeFiles.length;i++)
        {
            Resource welcome=directory.addPath(_welcomeFiles[i]);
            if (welcome.exists() && !welcome.isDirectory())
                return welcome;
        }

        return null;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        Request base_request = request instanceof Request?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled() || !request.getMethod().equals(HttpMethods.GET))
            return;
     
    	PathMap.Entry match = contextMap.getMatch(target);
    	if (match ==null || match.getValue() == null)
    		return;
    	Resource resource;
    	Resource value = (Resource) match.getValue();
    	if (match.getMapped() == null) {
            resource=getResource(target,value);
    	}
    	else {
        	int mappedLen = match.getMapped().length();
            resource=getResource(target.substring(mappedLen),value);
    	}
        
        if (resource==null || !resource.exists())
            return;

        // We are going to server something
        base_request.setHandled(true);
        
        if (resource.isDirectory())
        {
            if (!request.getPathInfo().endsWith(URIUtil.SLASH))
            {
                response.sendRedirect(URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH));
                return;
            }
            resource=getWelcome(resource);

            if (resource==null || !resource.exists() || resource.isDirectory())
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        // set some headers
        long last_modified=resource.lastModified();
        if (last_modified>0 && detectNotModified)
        {
            long if_modified=request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (if_modified>0 && last_modified/1000<=if_modified/1000)
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        Buffer mime=_mimeTypes.getMimeByExtension(resource.toString());
        if (mime==null)
            mime=_mimeTypes.getMimeByExtension(request.getPathInfo());
        
        // set the headers
        doResponseHeaders(response,resource,mime!=null?mime.toString():null);

        // Send the content
        OutputStream out =null;
        try {out = response.getOutputStream();}
        catch(IllegalStateException e) {out = new WriterOutputStream(response.getWriter());}
        
        // See if a short direct method can be used?
        if (out instanceof HttpConnection.Output)
        {
            // TODO file mapped buffers
            response.setDateHeader(HttpHeaders.LAST_MODIFIED,last_modified);
            ((HttpConnection.Output)out).sendContent(resource.getInputStream());
        }
        else
        {
            // Write content normally
            response.setDateHeader(HttpHeaders.LAST_MODIFIED,last_modified);
            resource.writeTo(out,0,resource.length());
        }

	}

    /** Set the response headers.
     * This method is called to set the response headers such as content type and content length.
     * May be extended to add additional headers.
     * @param response
     * @param resource
     * @param mimeType
     */
    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType)
    {
        if (mimeType!=null)
            response.setContentType(mimeType);

        long length=resource.length();
        
        if (response instanceof Response)
        {
            HttpFields fields = ((Response)response).getHttpFields();

            if (length>0)
                fields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER,length);
                
            if (_cacheControl!=null)
                fields.put(HttpHeaders.CACHE_CONTROL_BUFFER,_cacheControl);
        }
        else
        {
            if (length>0)
                response.setHeader(HttpHeaders.CONTENT_LENGTH,TypeUtil.toString(length));
                
            if (_cacheControl!=null)
                response.setHeader(HttpHeaders.CACHE_CONTROL,_cacheControl.toString());
        }
        
    }

    
}
