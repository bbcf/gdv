//package ch.epfl.bbcf.gdv.formats.sqlite;
//
//import java.io.File;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import ch.epfl.bbcf.gdv.access.database.pojo.Users;
//import ch.epfl.bbcf.gdv.config.Application;
//import ch.epfl.bbcf.gdv.config.Configuration;
//import ch.epfl.bbcf.gdv.control.model.InputControl.Extension;
//import ch.epfl.bbcf.gdv.control.model.SpeciesControl;
//
//public class SQLiteProcessor implements Runnable{
//	public final static String FAST = "0";
//	public final static String SLOW = "1";
//
//	private File file;
//	private Users user;
//	private String extension;
//	private boolean sendMail;
//	//private String jbrowsorId;
//	private String tmpDir;
//	private boolean admin;
//	private int trackId;
//	private int speciesId;
//
//	/**
//	 * Submitter of process to the daemon 
//	 * SQLite database
//	 * @param trackId 
//	 * @param file
//	 * @param extension
//	 * @param extension 
//	 * @param user
//	 * @param jbrowsorId 
//	 * @param assemblyId2 
//	 * @param sendMail 
//	 * @param admin 
//	 */
//	public SQLiteProcessor(int trackId, File file, String tmpDir, Extension extension, Users user, int speciesId, boolean sendMail, boolean admin) {
//		this.file = file;
//		this.user = user;
//		this.extension = extension.toString();
//		this.sendMail = sendMail;
//		//this.jbrowsorId = jbrowsorId;
//		this.tmpDir = tmpDir;
//		this.admin = admin;
//		this.trackId = trackId;
//		this.speciesId = speciesId;
//	}
//
//	public void run() {
//		String mail = user.getMail();
//		if(!sendMail){
//			mail = "nomail";
//		}
//		int nrAssembly = SpeciesControl.getNrAssemblyBySpeciesIdForBuildingChrList(speciesId);
//		SQLiteAccess access = new SQLiteAccess(Configuration.getTransform_to_sqlite_daemon());
//		access.writeNewJobTransform(
//				file.getAbsolutePath(), trackId, tmpDir, extension, mail, nrAssembly, user.getId(),
//				Configuration.getFilesDir(),Configuration.getTracks_dir(),
//				Configuration.getJb_data_root(),
//				Configuration.getGdv_appli_proxy()+"/post");
//		access.close();
//		
//		
//		
//		
//	}
//}