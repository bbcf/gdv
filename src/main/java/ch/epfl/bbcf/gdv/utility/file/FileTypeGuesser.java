package ch.epfl.bbcf.gdv.utility.file;

import java.io.File;
import java.io.IOException;

import ch.epfl.bbcf.gdv.config.Application;

public class FileTypeGuesser {

	/**
	 * Guess the extension of the file.
	 * Support gff,gff3,wig,wiggle,zip,gzip,gz.
	 * @param file
	 * @return
	 * @throws ExtensionNotRecognizedException 
	 */
	public static String guessExtension(File file) throws ExtensionNotRecognizedException{
		//Application.debug("guess extension");
		String fileName = file.getName();
		String tab[] = fileName.split("\\.");
		String ext = processExtension(tab[tab.length-1]);
		//Application.debug(ext);
		return ext;
	}

	/**
	 * Guess if the file is qualitative or quantitative,
	 * because process of the file will be differents.
	 * just test (if the file is gff) if there is score in the file
	 * TODO ALWAYS RETURN QUALITATIVE
	 * @param file
	 * @return 'qualitative' or 'quantitative'
	 * @throws ExtensionNotRecognizedException
	 * @throws IOException 
	 */
	public static String guessFileType(File file) throws ExtensionNotRecognizedException, IOException{
		String extension = guessExtension(file);
		if(extension.equalsIgnoreCase("gff") || extension.equalsIgnoreCase("bed")){
			return "qualitative";
		}
		return "quantitative";
	}





	private static String processExtension(String ext) throws ExtensionNotRecognizedException{
		String extension = null;
		if(ext.equalsIgnoreCase("gff")||ext.equalsIgnoreCase("gff3")){
			extension = "gff";
		}
		else if (ext.equalsIgnoreCase("wig")||ext.equalsIgnoreCase("wiggle")){
			extension = "wig";
		}
		else if (ext.equalsIgnoreCase("bed")){
			extension = "bed";
		}
		else if(ext.equalsIgnoreCase("zip")){
			extension = "zip";
		}
		else if(ext.equalsIgnoreCase("gzip")){
			extension = "gzip";
		}
		else if(ext.equalsIgnoreCase("gz")){
			extension = "gz";
		}
		else if(ext.equalsIgnoreCase("bedgraph")){
			extension = "bedgraph";
		}
		else {
			throw new ExtensionNotRecognizedException(ext);
		}
		return extension;
	}
}