package ch.epfl.bbcf.gdv.config.utility;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;

import ch.epfl.bbcf.gdv.config.Application;


public class RessourceLocator extends ResourceStreamLocator
{

	 public RessourceLocator() {	        
	    }
	 
	 public IResourceStream locate(final Class<?> clazz, final String path) {
	        IResourceStream located = super.locate(clazz, trimFolders(path));
	        if (located != null) {
	            return located;
	        }
	        return super.locate(clazz, path);
	    }

	    private String trimFolders(String path) {
	        String p = path.substring(path.lastIndexOf("/") + 1);
	        return p;
	    }

}
