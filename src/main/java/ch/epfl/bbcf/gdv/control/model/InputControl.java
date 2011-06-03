package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipException;

import org.apache.wicket.markup.html.form.upload.FileUpload;

import ch.epfl.bbcf.bbcfutils.Utility;
import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.exception.TrackCreationFailedException;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.utility.file.Decompressor;
import ch.epfl.bbcf.gdv.utility.file.ExtensionNotRecognizedException;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.file.FileTypeGuesser;

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
	public static boolean processUserInput(int job_id,int userId,Project project,URL url,FileUpload fileUpload,String systemPath){
		int trackId = createTmpTrack(project.getId(),TrackControl.STATUS_UPLOADING,job_id);
		if(trackId!=-1){
			Handler handler = new Handler(project.getSequenceId(),project.getId(),job_id, url, fileUpload, systemPath, systemPath, userId, false);
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
	public static boolean processUserInput(int job_id,int userId,int projectId,URL url,FileUpload fileUpload,String systemPath,String trackName){
		int trackId = createTmpTrack(projectId,TrackControl.STATUS_UPLOADING,job_id);
		if(trackId!=-1){
			Project project = ProjectControl.getProject(projectId);
			Handler handler = new Handler(project.getSequenceId(),projectId,job_id, url, fileUpload, systemPath, trackName, userId, false);
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
	public static boolean processAdminInput(int job_id,int sequenceId,URL url,FileUpload fileUpload,String systemPath,String name){
		int trackId = createAdminTrack(TrackControl.STATUS_UPLOADING,job_id);
		if(trackId!=-1){
			Handler handler = new Handler(sequenceId,-1,job_id, url, fileUpload, systemPath, name, -1,true);
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
	private static int createAdminTrack(String status,int job_id) {
		return TrackControl.createTmpTrack(job_id,status);
	}

	/**
	 * create tmp track
	 * @param projectId
	 * @param status
	 * @return
	 */
	private static int createTmpTrack(int projectId, String status,int job_id) {
		int trackId = TrackControl.createTmpTrack(job_id,status);
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
		private int jobId;
		private int sequenceId;
		private String trackName;
		private int userId;
		private int projectId;
		private boolean admin;

		private Handler(int sequenceId, int projectId,final int jobId, final URL url,final FileUpload fileUpload,final String systemPath,String trackName,int userId,boolean admin){
			this.sequenceId = sequenceId;
			this.jobId = jobId;
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
			
			Track track = TrackControl.getTrackIdWithJobId(jobId);
			
			//TODO errors & return false & track statuses updates
			String error="";
			System.out.println("UPLOAD");
			// ## UPLOAD
			
			TrackControl.updateTrack(track.getId(),TrackControl.STATUS_UPLOADING);
			//get a temporary directory
			String tmp_dir = Configuration.getTmp_dir()+"/"+UUID.randomUUID().toString();
			new File(tmp_dir).mkdir();
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
					TrackControl.updateTrack(track.getId(),error);
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
					TrackControl.updateTrack(track.getId(),error);
					return;
				}



				//get file from system path
			} else if(null!=systemPath){
				String name = systemPath.substring(systemPath.lastIndexOf('/') + 1);
				try {
					uploaded = FileManagement.getFileFromSystemPath(systemPath, tmp_dir, name);
				} catch (IOException e) {
					error+="error while getting file from file system : ";
					error+=e.getMessage();
					e.printStackTrace();
					TrackControl.updateTrack(track.getId(),error);
					return;
				}


			} else {
				Application.error("cannot process inputs, no file are provided");
				TrackControl.updateTrack(track.getId(),"file(s) not provided");
				return;
			}

			if(null==uploaded){
				TrackControl.updateTrack(track.getId(),"file(s) not uploaded");
				return;
			}

			System.out.println("DECOMP");
			// ## DECOMPRESSING
			TrackControl.updateTrack(track.getId(),TrackControl.STATUS_DECOMPRESS);
			List<File> files = null;
			try {
				files = Decompressor.decompress(uploaded);
			} catch (ZipException e) {
				error+="error while decompressing file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(track.getId(),error);
				return;
			} catch (ExtensionNotRecognizedException e) {
				error+="error while reading file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(track.getId(),error);
				return;
			} catch (IOException e) {
				error+="error while uploading file : ";
				error+=e.getMessage();
				e.printStackTrace();
				TrackControl.updateTrack(track.getId(),error);
				return;
			}

			if(null==files){
				error+="decompression failed";
				TrackControl.updateTrack(track.getId(),error);
				return;
			}
			// ## PROCESSING
			System.out.println("PROCESS");
			TrackControl.updateTrack(track.getId(),TrackControl.STATUS_SHA);
			for(File file:files){
				//get the shasum that will identify the file, so the database name
				String sha1 = null;
				try {
					sha1 = Utility.getFileDigest(file.getAbsolutePath(), "SHA1");
				} catch (NoSuchAlgorithmException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(track.getId(),error);
					e.printStackTrace();
					return;
				} catch (IOException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(track.getId(),error);
					e.printStackTrace();
					return;
				}

				//guess the extension
				TrackControl.updateTrack(track.getId(),TrackControl.STATUS_EXTENSION);
				Extension extension = null;
				try {
					extension = FileTypeGuesser.guessExtension(file);
				} catch (ExtensionNotRecognizedException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(track.getId(),error);
					e.printStackTrace();
					return;
				}
				//guess the file type
				String filetype = null;
				try {
					filetype = FileTypeGuesser.guessFileType(file, extension);
				} catch (ExtensionNotRecognizedException e) {
					error+=e.getMessage();
					TrackControl.updateTrack(track.getId(),error);
					return;
				} catch (IOException e) {
					e.printStackTrace();
					error+=e.getMessage();
					TrackControl.updateTrack(track.getId(),error);
					return;
				}
				if(null==filetype){
					error+="cannot guess file type";
					TrackControl.updateTrack(track.getId(),error);
					return;
				}


				String databaseName = sha1+".db";
				if(trackName==null){
					trackName=file.getName();
				}
				//look if the file already exist
				if(SQLiteAccess.dbAlreadyCreated(databaseName)){
					TrackControl.updateTrackFields(track.getId(),trackName,filetype,TrackControl.STATUS_FINISHED);
					return;
				}
				//create a new input
				if(admin){
					int inputId = createNewAdminInput(databaseName);
					TrackControl.linkToInput(track.getId(), inputId);
					TrackControl.createAdminTrack(sequenceId, track.getId());
				} else {
					int inputId = createNewUserInput(databaseName,userId);
					TrackControl.linkToInput(track.getId(), inputId);
					TrackControl.linkToProject(track.getId(), projectId);
				}

				//process
				//if it'a an SQL the first step is already done, so move file from
				//tmp directory to file directory
				if(extension.equals(Extension.DB)){
					System.out.println(file.getAbsolutePath());
					file = FileManagement.moveFile(file, Configuration.getFilesDir(),databaseName);
					Application.debug(" deleting : "+tmp_dir);
					FileManagement.deleteDirectory(new File(tmp_dir));
				} 

				//submit process to first daemon
				if(file==null){
					error+="error while moving the file";
					TrackControl.updateTrack(track.getId(),error);
					return;
				}
				System.out.println("write job");
				TrackControl.updateTrackFields(track.getId(),trackName,filetype,TrackControl.STATUS_PROCESSING);
				SQLiteAccess access = new SQLiteAccess(Configuration.getTransform_to_sqlite_daemon());
				access.writeNewJobTransform(
						file.getAbsolutePath(), jobId, tmp_dir, extension.toString(), "nomail", sequenceId,
						Configuration.getFilesDir(),Configuration.getTracks_dir(),
						Configuration.getJb_data_root(),
						Configuration.getGdv_appli_proxy()+"/post");
				access.close();
			}
		}
		public String toString(){
			return super.toString()+"\n"+
			"sequenceId  " +sequenceId+
			"\njobId " +jobId+
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
