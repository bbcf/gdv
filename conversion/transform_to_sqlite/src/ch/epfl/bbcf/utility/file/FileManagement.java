package ch.epfl.bbcf.utility.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.Launcher;






public class FileManagement {
	
	static final Logger log = Launcher.initLogger(FileManagement.class.getName());
	static final int BUFF_SIZE = 2048;
	static final byte[] buffer = new byte[BUFF_SIZE];
	private OutputStream out;

	public FileManagement(File file){
		try {
			this.out = new FileOutputStream(file,true);
		} catch (FileNotFoundException e) {
			log.error(e);
		}
	}
	public void writeTo(String from) throws IOException{
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(from.getBytes("UTF-8"));
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			} 
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void close() throws IOException{
		if (out != null) {
			out.close();
		}
	}



	public static void writeTo(String from, File to) throws IOException {
		InputStream in = null;
		OutputStream out = null; 
		try {
			in = new ByteArrayInputStream(from.getBytes("UTF-8"));
			out = new FileOutputStream(to,true);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			} 
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	public static boolean deleteDirectory(File directory) {
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
		return(directory.delete());
	}
	public static boolean deleteInTMPDirectory(String dirName) {
		File directory = new File(dirName);
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
		return(directory.delete());
		
	}
	
	
	public static void copy(FileInputStream in, FileOutputStream out) throws IOException{
		try {
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			} 
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	
	
}