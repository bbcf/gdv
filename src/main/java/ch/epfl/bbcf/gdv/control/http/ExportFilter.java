package ch.epfl.bbcf.gdv.control.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Logs;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.control.model.UserControl;

public class ExportFilter implements Filter{

	private static Logger log = Logs.initLogger("export.log",ExportFilter.class);

	@Override
	public void destroy() {
	}
	@Override
	public void init(FilterConfig config) throws ServletException {

	}



	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filter) throws IOException, ServletException {
		doGet(request,response);
	}



	private void doGet(ServletRequest request, ServletResponse response) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(out!=null){
			Map<String, String[]> map = request.getParameterMap();
			Params p = new Params(map);
			if(p.getTrackId()!=null && p.getOutput()!=null && p.getUserkey()!=null){
				
				Track t = TrackControl.getTrackById(p.getTrackId());
				//TODO is user auth to export this track
				//switch output
				//export it
				
				
				
			} else {
				out.write("missing param(s) : "+p.toString());
			}

			out.close();
		}



	}
	
	
	
	
	
	
	
	
	
	
	private class Params {
		
		private int trackId;
		private String output;
		private String userkey;
		

		public Params(Map<String, String[]> map) {
			if(map!=null){
				try{
					this.setTrackId(Integer.parseInt(map.get("id")[0]));
				} catch (NullPointerException e){} 
				catch (NumberFormatException nfe){};
			
				try{
					this.setOutput(map.get("out")[0]);
				} catch (NullPointerException e){};
				try{
					this.setUserkey(map.get("ukey")[0]);
				} catch (NullPointerException e){};
			}
		}
		public void setTrackId(int trackId) {
			this.trackId = trackId;
		}
		public Integer getTrackId() {
			return trackId;
		}
		public void setOutput(String output) {
			this.output = output;
		}
		public String getOutput() {
			return output;
		}
		public void setUserkey(String userkey) {
			this.userkey = userkey;
		}
		public String getUserkey() {
			return userkey;
		}
		public String toString(){
			return super.toString()+"  id : "+this.trackId+"  out : "+this.output+" ukey : "+this.userkey;
		}
	}
	
}
