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

import ch.epfl.bbcf.bbcfutils.access.genrep.GenRepAccess;
import ch.epfl.bbcf.bbcfutils.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.bbcfutils.access.genrep.MethodNotFoundException;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;

public class LinkFilter implements Filter{

	private static Logger log = Logs.initLogger("link.log",LinkFilter.class);

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
			Params params = new Params(map);
			if(null!=params.getNrAssemblyId()&&null!=params.getGeneName()){
				try {
					String res = 
						GenrepWrapper.getLinksAsString(params.getGeneName(), Integer.parseInt(
								params.getNrAssemblyId()));
					out.write(res);
				} catch (NumberFormatException e) {
					out.write(e.getMessage());
				} catch (MethodNotFoundException e) {
					out.write(e.getMessage());
				} catch (IOException e) {
					out.write(e.getMessage());
				} 
				finally {
					out.close();
				}
			} else {
				out.write("missing param : nr_assembly_id or gene_name");
				throw new AbortWithHttpStatusException(400, true);
			}
		}
		out.close();
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	private class Params {
		private String nrAssemblyId;
		private String geneName;

		public Params(String nrAssemblyId,String geneName){
			this.nrAssemblyId=nrAssemblyId;
			this.geneName=geneName;
		}
		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.nrAssemblyId = map.get("nr_assembly_id")[0];
				} catch (NullPointerException e){};
				try{
					this.geneName = map.get("gene_name")[0];
				} catch (NullPointerException e){};
			}
		}
		public void setNrAssemblyId(String nrAssemblyId) {
			this.nrAssemblyId = nrAssemblyId;
		}
		public String getNrAssemblyId() {
			return nrAssemblyId;
		}
		public void setGeneName(String geneName) {
			this.geneName = geneName;
		}
		public String getGeneName() {
			return geneName;
		}
	}


}
