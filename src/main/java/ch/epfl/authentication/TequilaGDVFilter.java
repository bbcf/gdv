	package ch.epfl.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.tequila.client.model.TequilaPrincipal;
import ch.epfl.tequila.client.system.TequilaFilter;


public class TequilaGDVFilter extends TequilaFilter{


	public TequilaGDVFilter(){
		super();
	}

	@Override
	public void init(FilterConfig config) throws ServletException
	{

	}

	@Override
	public void doFilter (ServletRequest servletRequest,
			ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpSession session = request.getSession();
		boolean getnewkey = false;
		String uri = request.getRequestURI();
		System.out.println("TEQUILA  : "+uri);
		if(uri.endsWith("/new")){
			getnewkey = true;
		}
		
		
		TequilaPrincipal principal = null;
		String key = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				System.out.println(cookies[i].getName()+"    "+cookies[i].getValue());
				if (cookies[i].getName().equals(TEQUILA_KEY)){
					key = cookies[i].getValue();
				}
			}
		} 
		if (key != null && !getnewkey) { // the user has a Tequila key, check it.
			response.sendRedirect(Configuration.getGdvProxyUrl()+"/login?key="+key);//+Configuration.GDV_MOUNT_PATH+"/login?key="+key);
		}
		else {
			String requestKey = TequilaAuthentication._tequilaService.createRequest (TequilaAuthentication._clientConfig, Configuration.getGdvProxyUrl()+"/login");//+Configuration.GDV_MOUNT_PATH+"/login");
			Cookie cook = new Cookie(TEQUILA_KEY,requestKey);
			cook.setMaxAge(160);
			response.addCookie(cook);
			response.sendRedirect ("https://" +TequilaAuthentication._clientConfig.getHost() +"/cgi-bin/tequila/requestauth?requestkey=" +requestKey);
		}
	}
}
