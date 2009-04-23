package jsspec.extras;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.log.Log;
//import org.mortbay.util.MultiMap;

class TestResult {
	
	String name = "";
	String hostname = "";//TODO
	int errors = 0;
	int failures = 0;
	int tests = 0;
	float time = 0;
	Date timestamp = new Date();
	
	Map<String,String> contextCaption = new HashMap<String,String>();
	
	Map<String,String> properties = new HashMap<String, String>();
	Map<String,TestCase> testcases = new HashMap<String, TestCase>();
	List<String> systemOut = new ArrayList<String>();
	List<String> systemErr = new ArrayList<String>();
	
	public TestResult() {
		
	}
	
	public void setPageName(String name) {
		this.name = name;
		
	}
	
	public String getFileName() {
		return "TEST-"+name+".xml";
	}

	public File getJUnitFile(String dir) {
		return new File(dir + (dir.endsWith("/")? "" : File.separator) + "TEST-"+name+".xml");
	}
	
	public File getFlagFile(String dir) {
		return new File(dir + (dir.endsWith("/")? "" : File.separator) + (failures+errors>0?"FAILURE-":"SUCCESS-")+name+".xml");
	}
	
	public void addContext(String id,String caption) {
		contextCaption.put(id, caption);
	}

	public void addExample(String id,String caption) {
		TestCase testCase = new TestCase();
		testCase.classname = this.name; //TODO consider if they should differ
		testCase.name = caption;
		testCase.id = id;
		//TODO time
		
		testcases.put(id, testCase) ;
		++tests;
	}
	public void addOutput(String id,String output) {
		int lastUnderscore = id.lastIndexOf('_');
		String contextId = id.substring(0,lastUnderscore);
		String cc = contextCaption.get(contextId);
		TestCase testCase = testcases.get(id);
		if (testCase != null) {
			testCase.failure = cc +": " + testCase.name + " (" + id.substring(lastUnderscore+1) + ")";
			testCase.output = output; //TODO separate map
			++failures;
		}
		
		//systemOut.add(output);
	}
	
	public void outputJUnitXML(OutputStream out) {
		PrintWriter writer = new PrintWriter(out);
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		writer.println(
				"<testsuite tests=\""+this.tests+"\" errors=\""+this.errors+"\""+
				" failures=\""+this.failures+"\" hostname=\""+this.hostname+"\" name=\""+this.name+"\""+
				" time=\""+this.time+"\" timestamp=\""+this.timestamp.toString()+"\""+ //TODO format YYYY-MM-DD\Thh:mm:ss
				">");
		Iterator<Entry<String, String>> propertyEntries = properties.entrySet().iterator();
		while(propertyEntries.hasNext()) {
			Entry<String,String> entry = propertyEntries.next();
			writer.println("<property name=\""+entry.getKey()+"\" value=\""+entry.getValue()+"\" />");
		}
		Iterator<Entry<String,TestCase>> testcaseEntries = testcases.entrySet().iterator();
		while(testcaseEntries.hasNext()) {
			Entry<String,TestCase> entry = testcaseEntries.next();
			TestCase testCase = entry.getValue();
			if (testCase.error != null) {
				writer.println("<testcase classname=\""+testCase.classname+"\" name=\""+testCase.name+"\" time=\""+testCase.time+"\">");
				writer.println("<error message=\""+testCase.getErrorMessage()+"\" type=\"Error\">");
				writer.println("<![CDATA["+testCase.output+"]]>");
				writer.println("</error>");
				writer.println("</testcase>");
			} else if (testCase.failure != null) {
				writer.println("<testcase classname=\""+testCase.classname+"\" name=\""+testCase.name+"\" time=\""+testCase.time+"\">");
				writer.println("<failure message=\""+testCase.getFailureMessage()+"\" type=\"ExampleFailure\">");
				writer.println("<![CDATA["+testCase.output+"]]>");
				writer.println("</failure>");
				writer.println("</testcase>");
			} else {
				writer.println("<testcase classname=\""+testCase.classname+"\" name=\""+testCase.name+"\" time=\""+testCase.time+"\" />");
			}
		}
		
		writer.print("<system-out><![CDATA[");
		ListIterator<String> outs = systemOut.listIterator();
		while(outs.hasNext()) writer.println(outs.next());
		writer.println("]]></system-out>");
		writer.print("<system-err><![CDATA[");
		ListIterator<String> errs = systemErr.listIterator();
		while(errs.hasNext()) writer.println(errs.next());
		writer.println("]]></system-err>");
		writer.println("</testsuite>");
		writer.flush();
	}
}

class TestCase {
	String id = "";
	String classname = "";
	String name = "";
	float time = 0;
	String error = null;
	String failure = null;
	String output = "";
	
	public TestCase() {
		
	}
	
	public String getErrorMessage() {
		return error.replace('\"', '\u201D');
	}
	public String getFailureMessage() {
		return failure.replace('\"', '\u201D');
	}
}

public class JSSpecHandler extends AbstractHandler {
	
	protected String uploadUrlPath = null;
	protected String reportsDirectory = null;

	public void setUploadUrlPath(String urlPath) {
		Log.info("upload path: "+urlPath);
		this.uploadUrlPath = urlPath;
	}

	public void setReportsDirectory(String dir) {
		Log.info("reports directory: "+dir);
		this.reportsDirectory = dir;
	}
	/**
	 * Only handle this upload url path
	 * @return String
	 */
	public String getUploadUrlPath() {
		return uploadUrlPath;
	}

	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

        Request base_request = request instanceof Request?(Request)request:HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled()) return;

        if (target.equals(uploadUrlPath) /*&& request.getMethod().equals("POST")*/) {
    		TestResult testResult = new TestResult();
    		
    		Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
    		while(names.hasMoreElements()) {
    			String name = names.nextElement();
    			String[] vals = request.getParameterValues(name);
    			for(int i=0,l=vals.length;i<l;++i) {
    				if ("_pagename".equals(name)) {
    					testResult.setPageName(vals[i]);
    				} else if ("_pathname".equals(name)) {
    					
    				} else if ("_top_pathname".equals(name)) {
    					
    				} else if ("_done".equals(name)) {
    					//TODO queue test results until done is received, then flush
    				} else if (name.startsWith("context_")) {
    					String contextId = name.substring(8);
    					testResult.addContext(contextId,URLDecoder.decode(vals[i],"UTF-8"));
    					
    				} else if (name.startsWith("example_")) {
    					String exampleId = name.substring(8);
    					testResult.addExample(exampleId,URLDecoder.decode(vals[i],"UTF-8"));
    				} else if (name.startsWith("status_")) {
    					
    				} else if (name.startsWith("output_")) {
    					String outputId = name.substring(7);
    					testResult.addOutput(outputId,URLDecoder.decode(vals[i],"UTF-8"));
    				} else {
        				System.out.print(name + ":");
        				System.out.println(vals[i]);
    				}
    			}
    		}
    		Log.info("Results uploaded for "+testResult.name+" to "+reportsDirectory);
    		if (reportsDirectory != null) {
        		File d = new File(reportsDirectory);
        		d.mkdirs();
        		File f = new File(reportsDirectory + (reportsDirectory.endsWith("/")? "" : File.separator) + testResult.getFileName());
    			testResult.outputJUnitXML(new FileOutputStream(f));
    			testResult.getFlagFile(reportsDirectory).createNewFile();
    		}
    	
    		base_request.setHandled(true);
    		response.setContentType("text/html");
    		PrintWriter writer = response.getWriter();
    		writer.println("<html><body>OK</body></html>");
        }
	}

}
