package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;


public class NamesFilter implements Filter{

	private static Logger log = Logs.initNamesLogger();

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
		PrintWriter out = null;
		Params params = null;
		try{
			Map<String, String[]> map = request.getParameterMap();
			params = new Params(map);
			log.debug("gettt");
			out = response.getWriter();
		} catch (IOException e1) {
			log.error(e1);
		}
		if(out!=null && params.getId()!=null && params.getDb()!=null && params.getName()!=null && params.getChr()!=null){
			log.debug("all!=null");
			if(params.getId().equalsIgnoreCase("find_name")){
				log.debug("find_name");
				try {
					List<Integer> positions = SQLiteAccess.searchForGeneNameOnChromosome(
							params.getDb(),params.getChr(), params.getName());
					log.debug("sql result");
					response.setContentType("text/plain");
					String result="";
					for(int i : positions){
						result+=i+",";
					}
					if(!result.equalsIgnoreCase("")){
						result=result.substring(0, result.length()-1);
					}
					log.debug(result);
					out.write(result);
					log.debug("writed");
				} catch (SQLException e) {
					StackTraceElement[] els = e.getStackTrace();
					for(StackTraceElement el : els){
						log.error(el.getFileName()+":"+el.getClassName()+"."+el.getMethodName()+"."+el.getLineNumber());
					}
				}
			}
		}
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	private class Params {
		private String id,name,db,chr;
		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.id = map.get("id")[0];
				} catch (NullPointerException e){};
				try{
					this.db = map.get("db")[0];
				} catch (NullPointerException e){};
				try{
					this.name = map.get("name")[0];
				} catch (NullPointerException e){};
				try{
					this.chr = map.get("chr")[0];
				} catch (NullPointerException e){};
			}
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getId() {
			return id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setDb(String db) {
			this.db = db;
		}
		public String getDb() {
			return db;
		}
		public void setChr(String chr) {
			this.chr = chr;
		}
		public String getChr() {
			return chr;
		}

	}

}
