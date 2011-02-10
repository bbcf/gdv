package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.ZipException;

import org.apache.wicket.markup.html.form.upload.FileUpload;

import ch.epfl.bbcf.gdv.access.database.Connect;
import ch.epfl.bbcf.gdv.access.database.dao.TrackDAO;
import ch.epfl.bbcf.gdv.access.database.dao.InputDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Input;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.InputControl.InputType;
import ch.epfl.bbcf.gdv.formats.json.JSONProcessor;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteProcessor;
import ch.epfl.bbcf.gdv.utility.ProcessLauncher;
import ch.epfl.bbcf.gdv.utility.ProcessLauncherError;
import ch.epfl.bbcf.gdv.utility.file.Decompressor;
import ch.epfl.bbcf.gdv.utility.file.ExtensionNotRecognizedException;
import ch.epfl.bbcf.gdv.utility.file.FileManagement;
import ch.epfl.bbcf.gdv.utility.file.FileTypeGuesser;
import ch.epfl.bbcf.gdv.utility.thread.ManagerService;

public class InputControl extends Control{

	public enum InputType { NEW_FILE,NEW_SQLITE};

	public enum Extension {GFF,GFF3,GTF,WIG,BEDGRAPH,BED,BAM,SAM,DB,ZIP,GZ,GZIP};
	public enum ZipExtension {};
	public InputControl(UserSession session) {
		super(session);
	}

	/**
	 * processing inputss from user - you can set projectId to -1 if you want an admin track
	 * @param projectId - the project
	 * @param url - url of the file if one
	 * @param fileUpload - the fileupload if one
	 * @param speciesId - the species id
	 * @param sendMail - if the user want feedback
	 * @param admin - if it's an admin track
	 * @param datatype 
	 * @param List<Group> - the list of group the track to share with
	 * 
	 * @return
	 */
	public boolean processInputs(int projectId, String url, 
			FileUpload fileUpload, int sequenceId,int speciesId,boolean sendMail, boolean admin,
			List<Group> groups,InputType type, String datatype,String name) {
		int trackId =  -1;
		if(admin){
			trackId = createAdminTrack(TrackControl.STATUS_UPLOADING);
		} else {
			trackId = createTmpTrack(projectId,TrackControl.STATUS_UPLOADING);
		}
		Application.info("processing input - InputControl - for project : "+projectId, session.getUserId());
		Uploader up = new Uploader(projectId,trackId,session.getUser(),url,
				fileUpload,sequenceId,speciesId,sendMail,admin,type,datatype,name);
		if(trackId!=-1){
			up.start();
			return true;
		}
		Application.error("not uploading create tmp track failed : "+projectId, session.getUserId());
		return false;
	}

	/**
	 * create tmp admin track
	 * @param status
	 * @return
	 */
	private int createAdminTrack(String status) {
		return TrackControl.createTmpTrack(status);
	}

	/**
	 * create tmp track
	 * @param projectId
	 * @param status
	 * @return
	 */
	private int createTmpTrack(int projectId, String status) {
		int trackId = TrackControl.createTmpTrack(status);
		//if(TrackControl.linkToUser(trackId, userId)){
		if(TrackControl.linkToProject(trackId, projectId)){
			return trackId;
		}
		//}
		return -1;
	}


	private Map<String, File> uploadFile(String url, FileUpload fileUpload, int userId) {
		Map<String, File> tmpDir = new HashMap<String, File>();
		if(null==url || url.equalsIgnoreCase("")){
			tmpDir = FileManagement.uploadFileFromUploadField(fileUpload,userId);
			if(tmpDir.isEmpty()){
				Application.error("-InputControl - fileUpload not uploaded : "+fileUpload.getClientFileName(),userId);
			}
		}
		else {
			tmpDir = FileManagement.uploadFileFromURL(url,userId);
			if(tmpDir!=null){
				if(tmpDir.isEmpty()){
					Application.error("-InputControl - file from url not uploaded : "+url,userId);
				}
			}
		}
		Application.debug("end upload",userId);
		return tmpDir;
	}


	/**
	 * create a new input in the GDV database
	 * @param md5
	 */
	private static int createNewUserInput(String md5,int userId,boolean admin) {
		InputDAO uidao = new InputDAO(Connect.getConnection());
		int inputId = -1;
		if(uidao.exist(md5)){
			inputId = uidao.getUserInputByFileName(md5).getId();
		} else {
			inputId = uidao.createNewInput(md5);
		}
		if(!admin){
			uidao.linkToUser(userId, inputId);
		}

		return inputId;
	}

	private class Uploader extends Thread{

		private int projectId;
		private String url;
		private FileUpload fileUpload;
		private boolean sendmail;
		private boolean admin;
		private int trackId;
		private Users user;
		private int speciesId;
		private InputType inputType;
		private String datatype;
		private int sequenceId;
		private String name;
		/**
		 * 
		 * @param projectId - the project id
		 * @param trackId - the track id
		 * @param user - the user
		 * @param url - the url if one
		 * @param fileUpload - the uploadfield if one
		 * @param sequenceId - the sequence id 
		 * @param speciesId - the species id
		 * @param sendMail - the boolean to send a feedback mail to user  
		 * @param admin - the boolean if it's an admin track 
		 * @param type - the input type (file or sqlite)
		 * @param datatype - the datataype (qualitative or quantitative)
		 * @param name - the name to give to the track
		 */
		public Uploader(int projectId, int trackId,Users user,String url, FileUpload fileUpload,
				int sequenceId,int speciesId,boolean sendMail, boolean admin,InputType type, String datatype,String name) {
			this.projectId = projectId;
			this.sequenceId = sequenceId;
			this.url = url;
			this.fileUpload = fileUpload;
			this.sendmail = sendMail;
			this.admin = admin;
			this.trackId = trackId;
			this.user = user;
			this.speciesId = speciesId;
			this.inputType = type;
			this.datatype=datatype;
			this.name=name;
		}

		public void run(){
			Application.debug("RUN() "+trackId);
			Application.debug("upload file",user.getId());
			Map<String, File> tmpDir  = uploadFile(url,fileUpload,user.getId());
			Application.debug("upload file : done",user.getId());
			Iterator<String> it = tmpDir.keySet().iterator();
			File tmpFile = null;
			String dir = null;
			while(it.hasNext()){
				dir = it.next();
				tmpFile=tmpDir.get(dir);
				dir = dir.substring(dir.lastIndexOf("/")+1,dir.length());
			}
			if(null==tmpFile){
				Application.error("failed to process inputs",user.getId());
				String err = "failed to process inputs : file  not uploaded ";
				if(null==url){
					err+=fileUpload.getClientFileName();
				}
				else {
					err+=url;
				}
				TrackControl.updateTrack(trackId, err);
			}
			switch(inputType){
			case NEW_FILE:
				Application.debug("NEW_FILE "+trackId);
				try {
					Application.debug("decompressing",user.getId());
					List<File> files = Decompressor.decompress(trackId,tmpFile);
					Application.debug("decompressing : done",user.getId());
					List<Future> futures = new ArrayList<Future>();
					for(File file:files){
						TrackControl.updateTrack(trackId, TrackControl.STATUS_MD5);
						String md5 = ProcessLauncher.getFileMD5(file);
						TrackControl.updateTrack(trackId, TrackControl.STATUS_FILETYPE);
						String filetype = FileTypeGuesser.guessFileType(file);
						TrackControl.updateTrack(trackId, TrackControl.STATUS_EXTENSION);
						Extension extension = FileTypeGuesser.guessExtension(file);

						//PROCESSING
						String database = md5+".db";
						int inputId = createNewUserInput(database,user.getId(),admin);
						if(inputId!=-1){
							TrackControl.linkToInput(trackId, inputId);
						}
						Application.debug("user input created ",user.getId());
						if(!SQLiteAccess.dbAlreadyCreated(database)){
							Application.debug("enter sqlite processing ",user.getId());
							if(null==name){
								name = file.getName();
							}
							TrackControl.updateTrackFields(trackId,name,filetype,TrackControl.STATUS_PROCESSING);
							TrackControl.linkToInput(trackId,inputId);
							if(admin){
								TrackControl.createAdminTrack(sequenceId,trackId);
							} else {
								TrackControl.linkToProject(trackId,projectId);
							}
							//String jbrowsorId = Integer.toString(SequenceControl.getJbrowsorIdFromSequenceId(sequenceId));
							SQLiteProcessor processor = new SQLiteProcessor(trackId,file,dir,extension,user,speciesId,sendmail,admin);
							Future task = ManagerService.submitPricipalProcess(processor);
							futures.add(task);

						} else {//File already created link to user
							Application.debug("file already processed ",user.getId());
							TrackControl.linkToProject(trackId,projectId);
							TrackControl.updateTrackFields(trackId,file.getName(),filetype,TrackControl.STATUS_FINISHED);
						}
					}
				} catch (ZipException e) {
					Application.error(e);
					TrackControl.updateTrack(trackId, "not valid zip file");
				} catch (IOException e) {
					Application.error(e);
					TrackControl.updateTrack(trackId, "not valid file");
				} catch (ExtensionNotRecognizedException e) {
					Application.error(e);
					TrackControl.updateTrack(trackId, "not valid extension, must be part of ");
				} catch (ProcessLauncherError e) {
					TrackControl.updateTrack(trackId, "the server encountered an error");
					return ;
				}	







				break;
			case NEW_SQLITE:
				Application.debug("NEW_SQLITE "+trackId);
				String database = tmpFile.getName();
				FileManagement.moveFile(tmpFile, Configuration.getFilesDir());
				int inputId = createNewUserInput(database,user.getId(),admin);
				if(inputId!=-1){
					TrackControl.linkToInput(trackId, inputId);
				}
				if(null==name){
					name = tmpFile.getName();
				}
				TrackControl.updateTrackFields(trackId,name,datatype,TrackControl.STATUS_PROCESSING);
				if(datatype.equalsIgnoreCase("qualitative")){
					TrackControl.updateTrack(trackId,"completed");
				} else if(datatype.equalsIgnoreCase("quantitative")){
					SQLiteAccess.writeNewJobCalculScores(
							Integer.toString(trackId),database,Configuration.getFilesDir(),
							database,Configuration.getTracks_dir(),"0","nomail");
				}
				break;
			}




		}
	}



}
