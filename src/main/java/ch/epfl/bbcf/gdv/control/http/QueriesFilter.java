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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;

public class QueriesFilter implements Filter{

	private static Logger log = Logs.initQueriesLogger();
	public void doGet(ServletRequest request, ServletResponse response) { 
		try{
			
			Map<String, String[]> map = request.getParameterMap();
			Params params = new Params(map);
//			String post = "POST : ";
//			for (Entry<String, String[]> entry : map.entrySet()){
//				post+=" key : "+entry.getKey();
//				for(String str : entry.getValue()){
//					post+="\t"+str+"\n";
//				}
//			}
//			Application.debug(post);

			PrintWriter out = null;
			try {
				out = response.getWriter();
			} catch (IOException e1) {
				log.error(e1);
			}

			if(out!=null && params.getId()!=null){
				if(params.getId().equalsIgnoreCase("db_scores")){
					if(params.getImgs()!=null && params.getDb()!=null){
						String[]idList = params.getImgs().split(",");
						String result=params.getDb();
						result+=SQLiteAccess.getScoresForDatabaseByIdList(params.getDb(),idList);
						//Application.debug(result);
//						for(String id:idList){
//							result+="$"+id+"="+SQLiteAccess.getScoresForDatabase(params.getDb(),id);
//							Application.debug(result);
//						}
						response.setContentType("text/plain");
						out.write(result);
					}
				}
			}	
		} catch (Exception e){
			log.debug(e);
			for(StackTraceElement ste :e.getStackTrace()){
				Application.error(ste.getClassName()+"_"+ste.getMethodName()+" :line("+ste.getLineNumber()+")");
			}
		}



		//		if(out!=null && params.getId()!=null){
		//			if(params.getId().equalsIgnoreCase("dbscores")){
		//				
		//				Map<String, String[]> map = ((WebRequestCycle) RequestCycle.get()).getRequest().getParameterMap();
		//				Application.debug("GETMAP");
		//				if(params.getDb()!=null && map!=null){
		//					Application.debug("!=null");
		//					String ids = null;
		//					try{
		//					ids = map.get("imgs")[0];
		//					Application.debug(ids);
		//					} catch (NullPointerException e){};
		//					if(null!=ids){
		//						String[]idList = ids.split(",");
		//						String result="";
		//						for(String id:idList){
		//							result+="$"+id+"$"+SQLiteAccess.getScoresForDatabase(params.getDb(),id);
		//							Application.debug(result);
		//						}
		//						response.setContentType("text/plain");
		//						out.write(result);
		//					}
		//				}



		//				if(params.getDb()!=null && params.getImg()!=null){
		//					String result = SQLiteAccess.getScoresForDatabase(params.getDb(),params.getImg());
		//					if(result!=null && !result.equalsIgnoreCase("")){
		//						response.setContentType("text/plain");
		//						out.write(result);
		//					}
		//				}



		//	}
		//		} else {
		//			out.write(HttpServletResponse.SC_BAD_REQUEST);
		//			out.write("id not provided");
		//		}
	}





	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{

		doGet(request,response);

	}



	private class Params {

		private String id;
		private String db;
		private String imgs;
		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.id = map.get("id")[0];
				} catch (NullPointerException e){};
				try{
					this.db  = map.get("db")[0];
				} catch (NullPointerException e){};
				try{
					this.imgs = map.get("imgs")[0];
				} catch (NullPointerException e){};
			}
		}
		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		public void setDb(String db) {
			this.db = db;
		}
		public String getDb() {
			return db;
		}
		public void setImgs(String imgs) {
			this.imgs = imgs;
		}
		public String getImgs() {
			return imgs;
		}
	}


	public void destroy() {
		// TODO Auto-generated method stub

	}





	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		doGet(request,response);

	}





	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}



}
