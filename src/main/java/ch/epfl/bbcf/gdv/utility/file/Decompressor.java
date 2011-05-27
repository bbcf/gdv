package ch.epfl.bbcf.gdv.utility.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.model.InputControl.Extension;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class Decompressor {

	private static final int BUFFER = 2048;



	/**
	 * Decompress a file. Support zip,gz and gzip extensions
	 * If the file has none of this extensions,it will return the file
	 * (in a list) untouched
	 * @param trackId 
	 * @param file - the file to decompress
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 * @throws ExtensionNotRecognizedException 
	 */
	public static List<File> decompress(int trackId, File file) throws ZipException, IOException, ExtensionNotRecognizedException{
		Extension extension = FileTypeGuesser.guessExtension(file);
		switch(extension){
		case ZIP :
			TrackControl.updateTrack(trackId, TrackControl.STATUS_DECOMPRESS);
			return unzip(file);
		case GZ : case GZIP:
			TrackControl.updateTrack(trackId, TrackControl.STATUS_DECOMPRESS);
			return ungunzip(file,extension.toString());
		default:
			List<File> files = new ArrayList<File>();
			files.add(file);
			return files;
		}
	}

	/**
	 * Decompress a file to a list of files in the same directory
	 * @param file
	 * @return
	 * @throws ExtensionNotRecognizedException
	 * @throws ZipException
	 * @throws IOException
	 */
	public static List<File> decompress(File file) throws ExtensionNotRecognizedException, ZipException, IOException{
		Extension extension = FileTypeGuesser.guessExtension(file);
		switch(extension){
		case ZIP :
			return unzip(file);
		case GZ : case GZIP:
			return ungunzip(file,extension.toString());
		default:
			List<File> files = new ArrayList<File>();
			files.add(file);
			return files;
		}
	}
	
	
	
	
	private static List<File> unzip(File file) throws ZipException, IOException {
		List<File> files = new ArrayList<File>();
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(file);
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while(e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			is = new BufferedInputStream(zipfile.getInputStream(entry));
			int count;
			byte data[] = new byte[BUFFER];
			FileOutputStream fos = new FileOutputStream(entry.getName());
			Application.debug("fos : "+fos.toString());
			dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
			is.close();
			files.add(new File(entry.getName()));
		}
		return files;
	}

	private static List<File> ungunzip(File file,String extension) throws IOException {

		String zipname, source;
		List<File> files = new ArrayList<File>();
		zipname = file.getAbsolutePath();
		source = zipname.substring(0, zipname.length()-extension.length()-1);
		GZIPInputStream zipin = null;
		FileInputStream in = new FileInputStream(zipname);
		zipin = new GZIPInputStream(in);
		byte[] buffer = new byte[BUFFER];
		// decompress the file
		FileOutputStream out = new FileOutputStream(source);
		Application.debug(out);
		int length;
		while ((length = zipin.read(buffer, 0, BUFFER)) != -1){
			out.write(buffer, 0, length);
		}
		out.close();
		zipin.close(); 
		files.add(new File(source));
		return files;
	}
}
