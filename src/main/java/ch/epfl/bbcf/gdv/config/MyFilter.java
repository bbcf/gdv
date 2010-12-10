package ch.epfl.bbcf.gdv.config;

import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WicketFilter;


public class MyFilter extends WicketFilter{

	protected  void	createRequestContext(WebRequest request, WebResponse response) {
		super.createRequestContext(request, response);
	}
	public boolean doGet(javax.servlet.http.HttpServletRequest servletRequest, 
			javax.servlet.http.HttpServletResponse servletResponse) 
	throws ServletException,IOException{
	//	Application.debug("doGet"+servletRequest.getRequestURI());
		return super.doGet(servletRequest, servletResponse);
	}

	protected  WebApplication	getWebApplication() {
		return super.getWebApplication();

	}
	public void	init(javax.servlet.FilterConfig filterConfig) throws ServletException{
		super.init(filterConfig);
	}
	
	
	
	
	public void destroy(){
	
		Application.info("destroying JDBC drivers");
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements()){
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				Application.debug("DEREGISTER DRIVER");
			} catch (SQLException e) {
				Application.error("FAILED DEREGISTER DRIVER : "+e);
			}
		}
		Application.destruct();
		super.destroy();
	}
}

