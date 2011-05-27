package ch.epfl.bbcf.gdv.utility.file;

public class ExtensionNotRecognizedException extends Exception {

	public ExtensionNotRecognizedException(String extension){
		super("bad extension or not supported by GDV : "+extension);
	}
}
