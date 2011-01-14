package ch.epfl.bbcf.gdv.utility.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.form.upload.FileUpload;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;


public class FileManagement {


//	public static Map<String, File> uploadTMPFileFromURL(String url,int userId){
//		Application.debug("uploadFileFromURL",userId);
//		URL u = null;
//		try {
//			u = new URL(url);
//		} catch (MalformedURLException e) {
//			Application.error(e,userId);
//			return null;
//		}
//		URLConnection connection = null;
//		try {
//			connection = u.openConnection();
//			Application.debug("-->length of content's connection : "+connection.getContentLength(), userId);
//		} catch (IOException e) {
//			Application.error(e,userId);
//			return null;
//		}
//		InputStream input = null;
//		try {
//			input = connection.getInputStream();
//		} catch (IOException e) {
//			Application.error(e,userId);
//			return null;
//		}
//		String fileName = u.getFile().substring(u.getFile().lastIndexOf('/') + 1);
//		FileOutputStream writeFile = null;
//		UUID uuid = UUID.randomUUID();
//		File dir = new File(Configuration.TMP_DIRECTORY+"/"+uuid);
//		if(!dir.mkdir()){
//			Application.error("cannot create directory :"+uuid);
//			return null;
//		}
//		else {
//			String name = fileName;
//			try {
//				writeFile = new FileOutputStream(Configuration.TMP_DIRECTORY+"/"+uuid+"/"+name);
//			} catch (FileNotFoundException e) {
//				Application.error(e,userId);
//				return null;
//			}
//			byte[] buffer = new byte[2048];
//			int read;
//			Application.debug("before reading", userId);
//			try {
//				copy(input, writeFile);
//			} catch (IOException e1) {
//				Application.error(e1,userId);
//			}
//			//			try {
//			//				while ((read = input.read(buffer)) > 0)
//			//					Application.debug("while reading", userId);
//			//					try {
//			//						writeFile.write(buffer, 0, read);
//			//					} catch (IOException e) {
//			//						Application.error(e,userId);
//			//						return null;
//			//					}
//			//					Application.debug("end reading", userId);
//			//			} catch (IOException e) {
//			//				Application.error(e,userId);
//			//				return null;
//			//			}
//			//			try {
//			//				writeFile.close();
//			//			} catch (IOException e) {
//			//				Application.error(e,userId);
//			//				return null;
//			//			}
//			Map<String,File> tmpDir = new HashMap<String, File>();
//			File file = new File(Configuration.TMP_DIRECTORY+"/"+uuid+"/"+name);
//			tmpDir.put(Configuration.TMP_DIRECTORY+"/"+uuid, file);
//			Application.debug("file : "+file.getAbsolutePath()+" uploaded",userId);
//			return tmpDir;
//		}
//	}

//	public static Map<String,File> uploadTMPFileFromUploadField(FileUpload fileUpload,int userId){
//		Application.debug("uploadFileFromUploadField",userId);
//		Map<String,File> tmpDir = new HashMap<String, File>();
//		String fileName = fileUpload.getClientFileName();
//		UUID uuid = UUID.randomUUID();
//		File dir = new File(Configuration.TMP_DIRECTORY+"/"+uuid);
//		if(!dir.mkdir()){
//			Application.error("cannot create directory :"+uuid);
//			return null;
//		}
//		else {
//			String name = fileName;
//			File tmpFile = new File(Configuration.TMP_DIRECTORY+"/"+uuid+"/"+name);
//			try {
//				fileUpload.writeTo(tmpFile);
//			} catch (IOException e) {
//				Application.error(e,userId);
//				return null;
//			}
//			tmpDir.put(Configuration.TMP_DIRECTORY+"/"+uuid, tmpFile);
//			return tmpDir;
//		}
//
//	}



	/**
	 * upload a file from an url and return a HashMap
	 * with key : the path, value : the file
	 */
	public static Map<String, File> uploadFileFromURL(String url,int userId){
		//Application.debug("uploadFileFromURL",userId);
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			Application.error(e,userId);
			return null;
		}
		URLConnection connection = null;
		try {
			connection = u.openConnection();
			//Application.debug("-->length of content's connection : "+connection.getContentLength(), userId);
		} catch (IOException e) {
			Application.error(e,userId);
			return null;
		}
		InputStream input = null;
		try {
			input = connection.getInputStream();
		} catch (IOException e) {
			Application.error(e,userId);
			return null;
		}
		String fileName = u.getFile().substring(u.getFile().lastIndexOf('/') + 1);
		FileOutputStream writeFile = null;
		UUID uuid = UUID.randomUUID();
		File dir = new File(Configuration.getTmp_dir()+"/"+uuid);
		if(!dir.mkdir()){
			Application.error("cannot create directory uploadFileFromURL :"+uuid);
			return null;
		}
		else {
			String name = fileName;
			try {
				writeFile = new FileOutputStream(Configuration.getTmp_dir()+"/"+uuid+"/"+name);
			} catch (FileNotFoundException e) {
				Application.error(e,userId);
				return null;
			}
			byte[] buffer = new byte[2048];
			int read;
			//Application.debug("before reading", userId);
			try {
				copy(input, writeFile);
			} catch (IOException e1) {
				Application.error(e1,userId);
			}
			Map<String,File> tmpDir = new HashMap<String, File>();
			File file = new File(Configuration.getTmp_dir()+"/"+uuid+"/"+name);
			tmpDir.put(Configuration.getTmp_dir()+"/"+uuid, file);
			Application.debug("file : "+file.getAbsolutePath()+" uploaded",userId);
			return tmpDir;
		}
	}

	public static Map<String,File> uploadFileFromUploadField(FileUpload fileUpload,int userId){
		Application.debug("uploadFileFromUploadField",userId);
		Map<String,File> tmpDir = new HashMap<String, File>();
		String fileName = fileUpload.getClientFileName();
		UUID uuid = UUID.randomUUID();
		File dir = new File(Configuration.getTmp_dir()+"/"+uuid);
		if(!dir.mkdir()){
			Application.error("cannot create directory uploadFileFromUploadField :"+uuid);
			return null;
		}
		else {
			String name = fileName;
			File tmpFile = new File(Configuration.getTmp_dir()+"/"+uuid+"/"+name);
			try {
				fileUpload.writeTo(tmpFile);
			} catch (IOException e) {
				Application.error(e,userId);
				return null;
			}
			tmpDir.put(Configuration.getTmp_dir()+"/"+uuid, tmpFile);
			return tmpDir;
		}

	}

	public static boolean moveFile(File from,String to){
		File dir = new File(to);
		return from.renameTo(new File(dir, from.getName()));
	}

	public static boolean deleteDirectory(File directory) {
		Application.debug("deleting : "+directory.getAbsolutePath());
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
	//	
	//	public static File uploadFileToUserDirectory(String url,Users user) {
	//		URL u = null;
	//		try {
	//			u = new URL(url);
	//		} catch (MalformedURLException e) {
	//			Application.error(e);
	//		}
	//		URLConnection connection = null;
	//		try {
	//			connection = u.openConnection();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		InputStream input = null;
	//		try {
	//			input = connection.getInputStream();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		String fileName = u.getFile().substring(u.getFile().lastIndexOf('/') + 1);
	//		FileOutputStream writeFile = null;
	//		UUID uuid = UUID.randomUUID();
	//		String name = fileName+"$$$"+uuid;
	//		try {
	//			writeFile = new FileOutputStream(user.getFilesDirectory()+"/"+name);
	//		} catch (FileNotFoundException e) {
	//			Application.error(e);
	//		}
	//		byte[] buffer = new byte[1024];
	//		int read;
	//		try {
	//			while ((read = input.read(buffer)) > 0)
	//				try {
	//					writeFile.write(buffer, 0, read);
	//				} catch (IOException e) {
	//					Application.error(e);
	//				}
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		try {
	//			writeFile.close();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		File file = new File(user.getFilesDirectory()+"/"+name);
	//		Application.debug("file : "+file.getAbsolutePath()+" uploaded");
	//		return file;
	//		
	//	}
	//	public static File uploadTMPFile(String url) {
	//		Application.debug("uploading file from : "+url);
	//		URL u = null;
	//		try {
	//			u = new URL(url);
	//		} catch (MalformedURLException e) {
	//			Application.error(e);
	//		}
	//		URLConnection connection = null;
	//		try {
	//			connection = u.openConnection();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		InputStream input = null;
	//		try {
	//			input = connection.getInputStream();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		String fileName = u.getFile().substring(u.getFile().lastIndexOf('/') + 1);
	//		FileOutputStream writeFile = null;
	//		UUID uuid = UUID.randomUUID();
	//		String name = fileName+"$$$"+uuid;
	//		try {
	//			writeFile = new FileOutputStream(Configuration.TMP_DIRECTORY+"/"+name);
	//		} catch (FileNotFoundException e) {
	//			Application.error(e);
	//		}
	//		byte[] buffer = new byte[1024];
	//		int read;
	//		try {
	//			while ((read = input.read(buffer)) > 0)
	//				try {
	//					writeFile.write(buffer, 0, read);
	//				} catch (IOException e) {
	//					Application.error(e);
	//				}
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		try {
	//			writeFile.close();
	//		} catch (IOException e) {
	//			Application.error(e);
	//		}
	//		File file = new File(Configuration.TMP_DIRECTORY+"/"+name);
	//		Application.debug("file : "+file.getAbsolutePath()+" uploaded");
	//		return file;
	//		
	//	}
	//	
	//	
//	public static String processExtension(String ext) {
//		String extension = null;
//		if(ext.equalsIgnoreCase("gff")||ext.equalsIgnoreCase("gff3")){
//			extension = "gff";
//		}
//		else if (ext.equalsIgnoreCase("wig")||ext.equalsIgnoreCase("wiggle")){
//			extension = "wig";
//		}
//		else if(ext.equalsIgnoreCase("zip")){
//			extension = "zip";
//		}
//		else if(ext.equalsIgnoreCase("gzip")){
//			extension = "gzip";
//		}
//		else if(ext.equalsIgnoreCase("gz")){
//			extension = "gz";
//		}
//		else {
//			Application.error("extension : "+ext+" not recognized");
//		}
//		return extension;
//	}
	//	
	//	
	//	public static String getURLFromLocalFile(File file,int userId){
	//		Application.error("getURLFromLocalFile ");
	//		ResourceReference fileResource = new ResourceReference("fileResource");
	//		String url = Configuration.SERV_URL+"/"+RequestCycle.get().urlFor(fileResource)+
	//		"?file="+file.getName()+"&id="+userId;
	//		Application.debug("getURL from local file : "+url);
	//		Application.error("getURLFromLocalFile : "+url);
	//		return url;
	//	}
	//	
	//	
	static final int BUFF_SIZE = 2048;
	static final byte[] buffer = new byte[BUFF_SIZE];

	public static void copy(InputStream in, FileOutputStream out) throws IOException{
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
//	public static void writeHeader(String header, File inFile, File outFile) throws IOException{
//		InputStream in1 = new ByteArrayInputStream(header.getBytes());
//		InputStream in2 = new FileInputStream(inFile);
//		OutputStream out = new FileOutputStream(outFile);
//		try {
//			while (true) {
//				synchronized (buffer) {
//					int amountRead = in1.read(buffer);
//					if (amountRead == -1) {
//						break;
//					}
//					out.write(buffer, 0, amountRead); 
//				}
//			} 
//			while (true) {
//				synchronized (buffer) {
//					int amountRead = in2.read(buffer);
//					if (amountRead == -1) {
//						break;
//					}
//					out.write(buffer, 0, amountRead); 
//				}
//			} 
//		} finally {
//			if (in1 != null) {
//				in1.close();
//			}
//			if (in2 != null) {
//				in2.close();
//			}
//			if (out != null) {
//				out.close();
//			}
//		}
//	}
	//
	//	public static void writeTo(String from, File to) throws IOException{
	//		   InputStream in = null;
	//		   OutputStream out = null; 
	//		   try {
	//		      in = new ByteArrayInputStream(from.getBytes("UTF-8"));
	//		      out = new FileOutputStream(to,true);
	//		      while (true) {
	//		         synchronized (buffer) {
	//		            int amountRead = in.read(buffer);
	//		            if (amountRead == -1) {
	//		               break;
	//		            }
	//		            out.write(buffer, 0, amountRead); 
	//		         }
	//		      } 
	//		   } finally {
	//		      if (in != null) {
	//		         in.close();
	//		      }
	//		      if (out != null) {
	//		         out.close();
	//		      }
	//		   }
	//		}
	//
	//
	//	
//	public static List<File> decompressFiles(File tmpFile, String extension) throws FileNotFoundException {
//		int BUFFER = 2048;
//		List<File> files = new ArrayList<File>();
//		if(extension.equalsIgnoreCase("zip")){
//			try {
//				BufferedOutputStream dest = null;
//				BufferedInputStream is = null;
//				ZipEntry entry;
//				ZipFile zipfile = new ZipFile(tmpFile);
//				Enumeration e = zipfile.entries();
//				while(e.hasMoreElements()) {
//					entry = (ZipEntry) e.nextElement();
//					is = new BufferedInputStream(zipfile.getInputStream(entry));
//					int count;
//					byte data[] = new byte[BUFFER];
//					FileOutputStream fos = new FileOutputStream(entry.getName());
//					dest = new BufferedOutputStream(fos, BUFFER);
//					while ((count = is.read(data, 0, BUFFER)) != -1) {
//						dest.write(data, 0, count);
//					}
//					dest.flush();
//					dest.close();
//					is.close();
//					Application.debug("decompressing : "+entry.getName());
//					files.add(new File(entry.getName()));
//				}
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
//		else if(extension.equalsIgnoreCase("gz")||extension.equalsIgnoreCase("gzip")){
//			String zipname, source;
//			zipname = tmpFile.getName();
//			source = zipname.substring(0, zipname.length()-extension.length()+1);
//			GZIPInputStream zipin = null;
//			try {
//				FileInputStream in = new FileInputStream(zipname);
//				zipin = new GZIPInputStream(in);
//			}
//			catch (IOException e) {
//				Application.error("Couldn't open " + zipname + ".");
//			}
//			byte[] buffer = new byte[BUFFER];
//			// decompress the file
//			try {
//				FileOutputStream out = new FileOutputStream(source);
//				int length;
//				while ((length = zipin.read(buffer, 0, BUFFER)) != -1){
//					out.write(buffer, 0, length);
//				}
//				out.close();
//			}
//			catch (IOException e) {
//				Application.error("Couldn't decompress " + zipname + ".");
//			}
//			try { 
//				zipin.close(); 
//				files.add(new File(source));
//			}catch (IOException e) {
//				Application.error(e);
//			}
//		}
//		return files;
//
//	}

//	public static void write(String toWrite, File file) throws IOException {
//		InputStream in = null;
//		OutputStream out = null; 
//		in = new ByteArrayInputStream(toWrite.getBytes("UTF-8"));
//		out = new FileOutputStream(file,true);
//		try {
//			while (true) {
//				synchronized (buffer) {
//					int amountRead = in.read(buffer);
//					if (amountRead == -1) {
//						break;
//					}
//					out.write(buffer, 0, amountRead); 
//				}
//			} 
//		} finally {
//			if (in != null) {
//				in.close();
//			}
//			if (out != null) {
//				out.close();
//			}
//		}
//	}
//
//	public static List<File> decompress(File tmpFile, String extension,int userId) {
//		Application.debug("decompressing files",userId);
//		List<File> files = null;
//		try {
//			files = FileManagement.decompressFiles(tmpFile,extension);
//		} catch (FileNotFoundException e) {
//			Application.error("failed to decompress files : "+e,userId);
//		}
//		return files;
//	}

//	public static String guessExtension(File file,int userId) {
//		Application.debug("guessing extension",userId);
//		String fileName = file.getName();
//		String tab[] = fileName.split("\\.");
//		String ext = FileManagement.processExtension(tab[tab.length-1]);
//		if(null==ext){
//			Application.error("extension not guessed ", userId);
//		}
//		return ext;
//	}

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
			Application.error(e);
		} catch (UnsupportedEncodingException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public static InputStream getInputStreamFromURL(String url) {
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			Application.error(e);
		}
		URLConnection connection = null;
		try {
			connection = u.openConnection();
		} catch (IOException e) {
			Application.error(e);
		}
		InputStream input = null;
		try {
			input = connection.getInputStream();
		} catch (IOException e) {
			Application.error(e);
		}
		return input;
	}
}