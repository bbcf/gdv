package ch.epfl.bbcf.gdv.config.utility;

import javax.servlet.http.Cookie;

import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebRequest;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;


public class RolesAuthorization implements IRoleCheckingStrategy{
    public RolesAuthorization(){}
    /**
     * @see org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy#hasAnyRole(Roles)
     */
    public boolean hasAnyRole(Roles roles){
        UserSession authSession = (UserSession)Session.get();
        int userId = authSession.getUserId();
        if(0 == userId){
        	return false;
        }
        
        if(roles.hasRole(Roles.ADMIN)){
        	return authSession.isAdmin();
        }
        else {
        	return true;
        }
        
    }

}