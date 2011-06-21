package ch.epfl.bbcf.gdv.utility;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.DynamicWebResource.ResourceState;

import ch.epfl.bbcf.gdv.config.Application;

public class ImageRessource extends DynamicWebResource {

	private static final long serialVersionUID = 1L;

	static int BUFFER_SIZE = 10*1024;

	private String path;
	
	/**
	 * 
	 */
	public ImageRessource(String path) {
		this.path = path;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.wicket.markup.html.DynamicWebResource#getResourceState()
	 */
	@Override
	protected ResourceState getResourceState() {
		return new ResourceState() {
			@Override
			public String getContentType() {
				return "image/png";
			}
			@Override
			public byte[] getData() {
				try {
					return bytes(new FileInputStream(path));
				} catch (Exception e) {
					Application.error(e);
					return null;
				}
			}
		};
	}
	
	public static  byte[] bytes(InputStream is) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(is, out);
		return out.toByteArray();
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];
		while (true) {
			int tam = is.read(buf);
			if (tam == -1) {
				return;
			}
			os.write(buf, 0, tam);
			os.close();
		}
	}
}
