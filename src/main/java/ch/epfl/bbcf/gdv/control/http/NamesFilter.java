package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.config.Configuration;
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
			out = response.getWriter();
		} catch (IOException e1) {
			log.error(e1);
		}
		if(out!=null && params.getId()!=null && params.getDb()!=null && params.getName()!=null && params.getChr()!=null){
			try {
				SQLiteAccess access = new SQLiteAccess(Configuration.getFilesDir()+"/"+params.getDb());
				response.setContentType("application/json");
				JSONObject json_result = new JSONObject();
				// ##search for exact match
				if(params.getId().equalsIgnoreCase("exact_match")){
					List<Integer> positions = access.searchForGeneNameOnChromosome(
							params.getDb(),params.getChr(), params.getName());
					if(positions.size()>0){
						Collections.sort(positions);
						JSONArray array = new JSONArray(positions);
						json_result.put("perfect", array);
					} 
					// ##search for suggest names
				} else if(params.getId().equalsIgnoreCase("search_name")){
					JSONArray suggest_json = new JSONArray();
					JSONObject pos_json = new JSONObject();
					Map<String,List<Integer>> suggests = access.suggestGeneNamesAndPositionsForChromosome(
							params.getDb(),params.getChr(), params.getName());
					log.debug("size of result "+suggests.size());
					for(Map.Entry<String,List<Integer>> entry : suggests.entrySet()){
						String key = entry.getKey();
						List<Integer> list = entry.getValue();
						Collections.sort(list);
						JSONArray array_json = new JSONArray(list);
						suggest_json.put(key);
						pos_json.put(key, array_json);
					}
					json_result.put("suggest",suggest_json);
					json_result.put("pos", pos_json);
				}
				access.close();
				out.write(json_result.toString());
				
			} catch (SQLException e) {
				StackTraceElement[] els = e.getStackTrace();
				log.error(e.getMessage());
				for(StackTraceElement el : els){
					log.error(el.getFileName()+":"+el.getClassName()+"."+el.getMethodName()+"."+el.getLineNumber());
				}
			} catch (JSONException e) {
				log.error(e.getMessage());
				StackTraceElement[] els = e.getStackTrace();
				for(StackTraceElement el : els){
					log.error(el.getFileName()+":"+el.getClassName()+"."+el.getMethodName()+"."+el.getLineNumber());
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
