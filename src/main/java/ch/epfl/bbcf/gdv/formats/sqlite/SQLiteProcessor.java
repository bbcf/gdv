package ch.epfl.bbcf.gdv.formats.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;

public class SQLiteProcessor implements Runnable{
	public final static String FAST = "0";
	public final static String SLOW = "1";

	private File file;
	private Users user;
	private String extension;
	private boolean sendMail;
	private String jbrowsorId;
	private String tmpDir;
	private boolean admin;
	private int trackId;
	private String nrAssemblyId;

	/**
	 * will transform GFF(quantitative) or WIG file into a 
	 * SQLite database
	 * @param trackId 
	 * @param file
	 * @param extension
	 * @param extension 
	 * @param user
	 * @param jbrowsorId 
	 * @param assemblyId2 
	 * @param sendMail 
	 * @param admin 
	 */
	public SQLiteProcessor(int trackId, File file, String tmpDir, String extension, Users user, String jbrowsorId, String nrAssemblyId, boolean sendMail, boolean admin) {
		this.file = file;
		this.user = user;
		this.extension = extension;
		this.sendMail = sendMail;
		this.jbrowsorId = jbrowsorId;
		this.tmpDir = tmpDir;
		this.admin = admin;
		this.trackId = trackId;
		this.nrAssemblyId = nrAssemblyId;
	}

	public void run() {
		String mail = user.getMail();
		if(!sendMail){
			mail = "nomail";
		}
		SQLiteAccess.writeNewJobTransform(file.getAbsolutePath(), trackId, tmpDir, extension, mail, nrAssemblyId, user.getId());
	}

	public static Map<String, String> getJSONDescriptor(String database,
			Map<String, String> altsNames) {
		try {
			int id = 0;
			Connection conn = SQLiteAccess.getConnectionOnFileDirectory(database);
			Map<String,String> result = new HashMap<String, String>();
			List<String> chromosomes = SQLiteAccess.getChromosomesNames(conn);
			for(String chr:chromosomes){
				int featureCount = SQLiteAccess.getFeatureCountForChromosome(conn,chr);
				String params="[";
				ResultSet r = SQLiteAccess.getValuesForChromosome(conn,chr);
				try {
					while(r.next()){
						id++;
						String name = r.getString("name");
						if(altsNames!=null){
							//alt name
							if(altsNames.get(name)!=null){
								String[] names = altsNames.get(name).split(",");
								name = names[0];
							}
						}
						params+="["+r.getInt("start")+","+r.getInt("end")+","+r.getInt("strand")+","+id+",\""+name+"\"],";
					}
					r.close();
					params=params.substring(0,params.length()-1);
					params+="]";
				} catch (SQLException e) {
					Application.error(e);
				}

				String finalParam = "{\"headers\":[\"start\",\"end\",\"strand\",\"id\",\"name\"]," +
				"\"subfeatureClasses\":null," +
				"\"featureCount\":"+featureCount+"," +
				"\"key\":\""+database+"\"," +
				"\"featureNCList\":[" +params+
				",\"className\":\"feature2\"," +
				"\"clientConfig\":null," +
				"\"rangeMap\":[]," +
				"\"arrowheadClass\":null," +
				"\"subfeatureHeaders\":[\"start\",\"end\",\"strand\",\"id\",\"type\"]," +
				"\"type\":\"FeatureTrack\"," +
				"\"label\":\"Alignments\"," +
				"\"sublistIndex\":5}";

				result.put(chr, finalParam);	

			}
			try {
				conn.close();
			} catch (SQLException e) {
				Application.error(e);
			}

			return result;
		}catch(Exception e){
			Application.debug(e);
			for (StackTraceElement el : e.getStackTrace()){
				Application.debug(el.getMethodName()+"   "+el.getFileName()+"  "+el.getLineNumber());
			}
		}
		return null;

	}
}