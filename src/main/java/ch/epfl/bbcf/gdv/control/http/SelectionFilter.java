package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
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

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.model.SelectionControl;

public class SelectionFilter implements Filter{

	private static Logger log = Logs.initLogger("selection_filter.log",SelectionFilter.class);
	private static final String ID="7";
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filter) throws IOException, ServletException {
		doGet(request,response);
		
	}

	
	private void doGet(ServletRequest request, ServletResponse response) {
		String post = "SelectionFilter :  ";
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> entry : map.entrySet()){
			post+="- "+entry.getKey();
			for(String str : entry.getValue()){
				post+=" : "+str+" ";
			}
		}
		log.debug(post);
		
		
		if(map.get("id")[0].equalsIgnoreCase(ID)){
			String selections=map.get("sels")[0];
			String projectId=map.get("pid")[0];
			if(selections!=null && projectId!=null){
				SelectionControl.createNewSelection(selections,projectId);
				log.debug(selections);
				log.debug(projectId);
			} else {
				log.error("wrong parameters");
				throw new AbortWithHttpStatusException(400, true);
			}
		}
		
		
	}
	
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
