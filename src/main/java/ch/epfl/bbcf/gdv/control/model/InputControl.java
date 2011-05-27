package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.zip.ZipException;

import org.apache.wicket.markup.html.form.upload.FileUpload;

import ch.epfl.bbcf.bbcfutils.Utility;
import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.exception.TrackCreationFailedException;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.utility.file.Decompressor;
import ch.epfl.bbcf.gdv.utility.file.ExtensionNotRecognizedException;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.file.FileTypeGuesser;
import ch.epfl.bbcf.gdv.utility.thread.ManagerService;

public class InputControl extends Control{


	public enum Extension {GFF,GFF3,GTF,WIG,BEDGRAPH,BED,BAM,SAM,DB,ZIP,GZ,GZIP};
	public enum ZipExtension {};
	public InputControl(UserSession session) {
		super(session);
	}







	/**
	 * process your input - you can either provide an URL, a file upload or a system path
	 * @param project - the project the track belong to
	 * @param url - the URL
	 * @param fileUpload - the fileUpload
	 * @param systemPath - the system path
	 * @return
	 * @throws TrackCreationFailedException 
	 */
	public static boolean processUserInput(int userId,Project project,URL url,FileUpload fileUpload,String systemPath){
		int trackId = createTmpTrack(project.getId(),TrackControl.STATUS_UPLOADING);
		if(trackId!=-1){
			Handler handler = new Handler(project.getSequenceId(),project.getId(),trackId, url, fileUpload, systemPath, systemPath, userId, false);
			handler.start();
			return true;
		}
		return false;

	}
	/**
	 * process your input - you can either provide an URL, a file upload or a system path
	 * @param projectId - the project id the track belong to
	 * @param url - the URL
	 * @param fileUpload - the fileUpload
	 * @param systemPath - the system path
	 * @return
	 * @throws TrackCreationFailedException 
	 */
	public static boolean processUserInput(int userId,int projectId,URL url,FileUpload fileUpload,String systemPath){
		int trackId = createTmpTrack(projectId,TrackControl.STATUS_UPLOADING);
		if(trackId!=-1){
			Project project = ProjectControl.getProject(projectId);
			Handler handler = new Handler(project.getSequenceId(),projectId,trackId, url, fileUpload, systemPath, systemPath, userId, false);
			handler.start();
			return true;
		}
		return false;

	}


	/**
	 * process your input - WARNING : it will be an admin one, & will be visible by each user
	 * you can either provide an URL, a file upload or a system path
	 * @param url
	 * @param fileUpload
	 * @param systemPath
	 * @return
	 * @throws TrackCreationFailedException 
	 */
	public static boolean processAdminInput(int sequenceId,URL url,FileUpload fileUpload,String systemPath,String name){
		int trackId = createAdminTrack(TrackControl.STATUS_UPLOADING);
		if(trackId!=-1){
			Handler handler = new Handler(sequenceId,-1,trackId, url, fileUpload, systemPath, name, -1,true);
			handler.start();
			return true;
		}
		return false;

	}




	/**
	 * create tmp admin track
	 * @param status
	 * @return
	 */
	private static int createAdminTrack(String status) {
		return TrackControl.createTmpTrack(status);
	}

	/**
	 * create tmp track
	 * @param projectId
	 * @param status
	 * @return
	 */
	private static int createTmpTrack(int projectId, String status) {
		int trackId = TrackControl.createTmpTrack(status);
		//if(TrackControl.linkToUser(trackId, userId)){
		if(TrackControl.linkToProject(trackId, projectId)){
			return trackId;
		}
		//}
		return -1;
	}


	private static Map<String, File> uploadFile(String url, FileUpload fileUpload, int userId) {
		if(null==url || url.equalsIgnoreCase("")){
			Map<String, File> tmpDir = FileManagement.uploadFileFromUploadField(fileUpload,userId);
			if(tmpDir.isEmpty()){
				Application.error("-InputControl - fileUpload not uploaded : "+fileUpload.getClientFileName(),userId);
			}
			return tmpDir;
		}
		else {
			try {
				URL u = new URL(url);
				Map<String, File> tmpDir  = FileManagement.uploadFileFromURL(url,userId);
				if(tmpDir!=null){
					if(tmpDir.isEmpty()){
						Application.error("-InputControl - file from url not uploaded : "+url,userId);
					}
					return tmpDir;
				}
			} catch (MalformedURLException e) {
				//the url is a path in fact
				Map<String,File> m =new HashMap<String,File >();
				String path = url.substring(0, url.lastIndexOf("/"));
				m.put(path,new File(url));
				return m;
			}

		}
		Application.debug("end upload",userId);
		return null;
	}


	private static int createNewAdminInput(String dbName) {
		InputDAO uidao = new InputDAO(Connect.getConnection());
		int inputId = -1;
		if(uidao.exist(dbName)){
			inputId = uidao.getUserInputByFileName(dbName).getId();
		} else {
			inputId = uidao.createNewInput(dbName);
		}

		return inputId;
	}
	/**
	 * create a new input in the GDV database
	 * @param md5
	 */
	private static int createNewUserInput(String dbName,int userId) {
		InputDAO uidao = new InputDAO(Connect.getConnection());
		int inputId = -1;
		if(uidao.exist(dbName)){
			inputId = uidao.getUserInputByFileName(dbName).getId();
		} else {
			inputId = uidao.createNewInput(dbName);
		}
		uidao.linkToUser(userId, inputId);
		return inputId;
	}


	/**
	 * remove an input from the db
	 * @param input - the name of the input (generally the md5)
	 */
	public static void removeInput(String input) {
		InputDAO idao = new InputDAO(Connect.getConnection());
		idao.remove(input);
	}



	/**
	 * 
	 * Will handle the upload, decompression of tracks, 
	 * before sending them to the transformation to 
	 * SQLites databases
	 *
	 */
	private static class Handler extends Thread {

		private URL url;
		private FileUpload fileUpload;
		private String systemPath;
		private int trackId;
		private int sequenceId;
		private String trackName;
		private int userId;
		private int projectId;
		private boolean admin;

		private Handler(int sequenceId, int projectId,final int trackId, final URL url,final FileUpload fileUpload,final String systemPath,String trackName,int userId,boolean admin){
			this.sequenceId = sequenceId;
			this.trackId = trackId;
			this.url = url;
			this.fileUpload = fileUpload;
			this.systemPath = systemPath;
			this.trackName = trackName;
			this.userId = userId;
			this.projectId = projectId;
			this.admin = admin;
		}

		
		
		public void run(){
			System.out.println(this.toString());
			
			
			
			//TODO errors & return false & track statuses updates
			String error="";
			System.out.println("UPLOAD");
			// ## UPLOAD
			TrackControl.updateTrack(trackId,TrackControl.STATUS_UPLOADING);
			//get a temporary directory
			String tmp_dir = Configuration.getTmp_dir()+"/"+UUID.randomUUID().toString();
			
			System.out.println("tmp dir : "+tmp_dir);
			
			File uploaded = null;
			//get from URL
			if(null!=url){
				String name = FileManagement.findNameOfFileInURL(url);
				try {
					uploaded = FileManagement.uploadFileFromURL(url,tmp_dir,name);
				} catch (IOException e) {
					error+="error while uploading file from url : ";
					error+=e.getMessage();
					e.printStackTrace();
					TrackControl.updateTrack(trackId,error);
					return;
				}



				//get from file upload
			} else if(null!=fileUpload){
				try {
					uploaded = FileManagement.uploadFileFromFileUpload(fileUpload, tmp_dir, fileUpload.getClientFileName());
				} catch (IOException e) {
					error+="error while uploading file file upload : ";
					error+=e.getMessage();
					e.printStackTrace();
					TrackControl.updateTrack(trackId,error);
					return;
				}



				//get file from system path
			} else if(null!=systemPath){
				String name = systemPath.substring(systemPath.lastIndexOf('/') + 1);
				try {
					uploaded = FileManagement.getFileFromSystemPath(systemPath, tmp_dir, name);
				} catch (IOException e) {
					error+="error while gtting file from file system : ";
					error+=e.getMessage();
					e.printStackTrace();
					TrackControl.updateTrack(trackId,error);
					return;
				}


			} else {
				Application.error("cannot process inputs, no file are provided");
				TrackControl.updateTrack(trackId,"file(s) not provided");
				return;
			}

			if(null==uploaded){
				TrackControl.updateTrack(trackId,"file(s) not uploaded");
				return;
			}

			System.out.println("DECOMP");
			// ## DECOMPRESSING
			TrackControl.updateTrack(trackId,TrackControl.STATUS_DECOMPRESS);
			List<File> files = null;
			try {
				files = Decompressor.decompress(uploaded);
			} catch (ZipException e) {
				error+="error while decompressing file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(trackId,error);
				return;
			} catch (ExtensionNotRecognizedException e) {
				error+="error while reading file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(trackId,error);
				return;
			} catch (IOException e) {
				error+="error while uploading file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(trackId,error);
				return;
			}

			if(null==files){
				error+="decompression failed";
				TrackControl.updateTrack(trackId,error);
				return;
			}
			System.out.println("PROCESS");
			// ## PROCESSING
			TrackControl.updateTrack(trackId,TrackControl.STATUS_SHA);
			for(File file:files){
				//get the shasum that will identify the file, so the database name
				String sha1 = null;
				try {
					sha1 = Utility.getFileDigest(file.getAbsolutePath(), "SHA1");
				} catch (NoSuchAlgorithmException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(trackId,error);
					return;
				} catch (IOException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(trackId,error);
					return;
				}


				//guess the extension
				TrackControl.updateTrack(trackId,TrackControl.STATUS_EXTENSION);
				Extension extension = null;
				try {
					extension = FileTypeGuesser.guessExtension(file);
				} catch (ExtensionNotRecognizedException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(trackId,error);
					return;
				}

				//guess the file type
				String filetype = null;
				try {
					filetype = FileTypeGuesser.guessFileType(file, extension);
				} catch (ExtensionNotRecognizedException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(trackId,error);
					return;
				} catch (IOException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(trackId,error);
					return;
				}

				if(null==filetype){
					error+="cannot guess file type";
					TrackControl.updateTrack(trackId,error);
					return;
				}


				String databaseName = sha1+".db";
				if(trackName==null){
					trackName=file.getName();
				}
				//look if the file already exist
				if(SQLiteAccess.dbAlreadyCreated(databaseName)){
					TrackControl.updateTrackFields(trackId,trackName,filetype,TrackControl.STATUS_FINISHED);
					return;
				}

				//create a new input
				if(admin){
					int inputId = createNewAdminInput(databaseName);
					TrackControl.linkToInput(trackId, inputId);
				} else {
					int inputId = createNewUserInput(databaseName,userId);
					TrackControl.linkToInput(trackId, inputId);
					TrackControl.linkToProject(trackId, projectId);
				}


				//process
				//if it'a an SQL the first step is already done, so move file from
				//tmp directory to file directory
				if(extension.equals(Extension.DB)){
					file = FileManagement.moveFile(file, Configuration.getFilesDir());
				} 

				//submit process to first daemon
				if(file==null){
					error+="error while moving the file";
					TrackControl.updateTrack(trackId,error);
					return;
				}

				TrackControl.updateTrackFields(trackId,trackName,filetype,TrackControl.STATUS_PROCESSING);
				TrackControl.updateTrack(trackId,TrackControl.STATUS_PROCESSING);
				SQLiteAccess access = new SQLiteAccess(Configuration.getTransform_to_sqlite_daemon());
				access.writeNewJobTransform(
						file.getAbsolutePath(), trackId, tmp_dir, extension.toString(), "nomail", sequenceId,
						Configuration.getFilesDir(),Configuration.getTracks_dir(),
						Configuration.getJb_data_root(),
						Configuration.getGdv_appli_proxy()+"/post");
				access.close();
			}
		}
		public String toString(){
			return super.toString()+"\n"+
			"sequenceId  " +sequenceId+
			"\ntrackId " +trackId+
			"\nurl " +url+
			"\nfileUpload " +fileUpload+
			"\nsystemPath " +systemPath+
			"\ntrackName " +trackName+
			"\nuserId " +userId+
			"\nprojectId " +projectId+
			"\nadmin " +admin;
		}
	}
	
}
