package ch.epfl.bbcf.gdv.config.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.value.ValueMap;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;



/**
 * A resource that provides files from the holding dir for an user
 *
 * 
 */
public class FileResource extends DynamicWebResource
{
	// CONSTANTS

	private static final long serialVersionUID = 1L;

	// CONSTRUCTORS

	public FileResource()
	{
		super();
	}

	public FileResource(Locale local)
	{
		super(local);
	}

	// METHODS

	// MEMBERS
	/**
	 * get the ressources shared for an user
	 */
	protected ResourceState getResourceState(UserSession session)
	{
		ValueMap params = getParameters();
		String fileName = params.getString("file");
		String userId = params.getString("id");
		//int userId = session.getUserId();
		FileResourceState state =
			new FileResourceState();

		if (null!= fileName && null!= userId) {
			String path = Configuration.getFilesDir();
			state.setContentType("text");
			File file = new File(path);
			byte[] b = new byte[(int) file.length()];
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				fileInputStream.read(b);
			} catch (FileNotFoundException e) {
				Application.error(e, session.getUserId());
			} catch (IOException e) {
				Application.error(e, session.getUserId());
			}
			state.setData(b);
		}


		return state;
	}


	class FileResourceState extends ResourceState   {
		// CONSTRUCTORS
		FileResourceState()       {
			super();
		}

		// MEMBERS

		private String contentType;
		@Override
		public String getContentType()
		{
			return contentType;
		}
		void setContentType(String contentType)
		{
			this.contentType = contentType;
		}

		private byte[] data;
		@Override
		public byte[] getData(){
			return data;
		}
		void setData(byte[] data){
			this.data = data;
		}

		@Override
		public int getLength(){
			return data.length;
		}

		private Time lastModified;
		@Override
		public Time lastModifiedTime(){
			return lastModified;
		}
	}


	@Override
	protected ResourceState getResourceState() {
		return getResourceState((UserSession) Session.get());
	}

}
