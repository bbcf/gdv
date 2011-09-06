package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;
public class PostAccessFilter implements Filter{

	private static Logger log = Logs.initPostLogger(PostAccessFilter.class.getName());

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain arg2) throws IOException, ServletException {
		doGet(request,response);

	}

	private void doGet(ServletRequest request, ServletResponse response) {
		String post = "RemoteAccess :  ";
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> entry : map.entrySet()){
			post+="- "+entry.getKey();
			for(String str : entry.getValue()){
				post+=" : "+str+" ";
			}
		}
		log.debug(post);
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(out!=null){
			RequestParameters params = new RequestParameters(map);
			FrontController fc = new FrontController(params,out);
			fc.doRequest();
		} else {
			throw new AbortWithHttpStatusException(400, true);
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
