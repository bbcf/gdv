package ch.epfl.bbcf.gdv.config;

import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WicketFilter;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;


public class MyFilter extends WicketFilter{

	protected  void	createRequestContext(WebRequest request, WebResponse response) {
		super.createRequestContext(request, response);
	}
	public boolean doGet(javax.servlet.http.HttpServletRequest servletRequest, 
			javax.servlet.http.HttpServletResponse servletResponse) 
	throws ServletException,IOException{
		//	Application.debug("doGet"+servletRequest.getRequestURI());
		boolean doget =  super.doGet(servletRequest, servletResponse);
		if (!servletResponse.isCommitted()){
			HttpSession s = servletRequest.getSession(true);
			Connect.removeConnection(s.getId());
		}
		return doget;
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

