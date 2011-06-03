package ch.epfl.bbcf.gdv.formats.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class SQLiteAccess {



	public static ResultSet getResultSet(Statement stat,String query) throws SQLException{
		return stat.executeQuery(query);
	}

	public Map<String, Integer> getChromosomesAndLength() {
		Map<String,Integer> result = new HashMap<String,Integer>();
		try {
			Statement stat = this.conn.createStatement();
			String query = "SELECT * FROM chrNames;";
			ResultSet rs = getResultSet(stat, query);
			while (rs.next()) {
				result.put(rs.getString("name"),
						rs.getInt("length"));
			}
			rs.close();
		} catch (SQLException e) {
			Application.error(e);
		}
		return result;
	}


	public static boolean 	dbAlreadyCreated(String database) {
		File test = new File(Configuration.getFilesDir()+"/"+database);
		return test.exists();
	}



	public void writeNewJobCalculScores(int trackId, String indb,
			String inPath, String outdb, String outPath,
			int fast, String usermail,String feedback_url,String tmp_dir) {
		try {
			PreparedStatement stat = conn.prepareStatement("insert into jobs values (?,?,?,?,?,?,?,?,?); ");
			stat.setInt(1,trackId);
			stat.setString(2,indb);
			stat.setString(3,inPath);
			stat.setString(4,outdb);
			stat.setString(5,outPath);
			stat.setString(6,feedback_url);
			stat.setString(7,tmp_dir);
			stat.setInt(8,fast);
			stat.setString(9,usermail);
			conn.setAutoCommit(false);
			stat.execute();
			conn.commit();
			Application.debug(trackId+" "+indb+" "+inPath+" "+outdb+" "+outPath+" "+fast+" "+usermail);
		} catch (SQLException e) {
			Application.error(e);
			SQLiteAccess access = new SQLiteAccess(Configuration.getCompute_scores_daemon());
			access.writeNewJobCalculScores(trackId,indb,inPath,outdb,outPath,fast,usermail,
					feedback_url,tmp_dir);
		}

	}



	public void writeNewJobTransform(String filePath,int trackId, String tmpDir,
			String extension,String mail, int nrAssemblyId,String outdir,
			String jbrowse_outdir,String jbrowse_ressource_url,String feedback_url) {
		Application.info("write new sqlite job : file("+filePath+"),trackId("+trackId+"),tmpDir("+tmpDir+")," +
				"extension("+extension+"),mail("+mail+")," +
				"nrAssembly("+nrAssemblyId+"),outDir("+outdir+"),jbrowse_outdir("+jbrowse_outdir+")" +
						"jbrowse_ressource_url("+jbrowse_ressource_url+"),feedback_url("+feedback_url+")");
		try {
			PreparedStatement stat = conn.prepareStatement("insert into jobs values (?,?,?,?,?,?,?,?,?,?); ");
			stat.setString(1, filePath);
			stat.setInt(2, trackId);
			stat.setString(3,tmpDir);
			stat.setString(4,extension);
			stat.setString(5,mail);
			stat.setInt(6,nrAssemblyId);
			stat.setString(7,outdir);
			stat.setString(8, jbrowse_outdir);
			stat.setString(9,jbrowse_ressource_url);
			stat.setString(10,feedback_url);
			conn.setAutoCommit(false);
			stat.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			Application.error(e);
		}
	}



	public String getScoresForDatabaseByIdList(String[] idList) {
		String result = "";
		try {
			PreparedStatement stat = conn.prepareStatement("select pos,score from sc where number= ? order by pos asc");
			for(String img:idList){
				boolean isData = false;
				result+="$"+img+"={";
				stat.setString(1, img);
				ResultSet rs = stat.executeQuery();
				while (rs.next()) {
					isData=true;
					result+=""+rs.getString(1)+":"+rs.getString(2)+",";
				}
				rs.close();
				if(isData){
					result=result.substring(0, result.length()-1);
				}
				if(img.length()>0){
					result+="}";
				}
			}
			return result;
		} catch (SQLException e) {
			Application.error(e);
		}
		return null;

	}


	private Connection conn;
	public SQLiteAccess(String database){
		Application.debug("CONNECTION ON : "+database);
		this.conn = getConnection(database);
	}

	public void close(){
		try {
			this.conn.close();
		} catch (SQLException e) {
			Application.error(e);
		}
	}
	private Connection getConnection(String database) {
		try {
			Class.forName("org.sqlite.JDBC").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+database);
			return conn;
		} catch (InstantiationException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		} catch (ClassNotFoundException e) {
			Application.error(e);
		} catch (SQLException e) {
			Application.error(e);
		} catch (NullPointerException e) {
			Application.error(e);
			return getConnection(database);
		} catch (UnsatisfiedLinkError e){
			Application.error("UnsatisfiedLinkError :"+e);
		}
		return null;
	}




	/**
	 * try to find coordinates (start,end) of a gene by it's name
	 * @param chr - the chromosome
	 * @param name - the gene name
	 * @param tracks - the tracks, commas separated
	 * @return a list of coordinates (start,end,start,end,start,end,....)
	 * @throws SQLException
	 */
	public List<Integer> searchForGeneNameOnChromosome(String tracks,String chr,String name) throws SQLException {
		List<Integer> result = new ArrayList<Integer>();
		String query = "SELECT start,end FROM \""+chr+"\" where name = ?; ";
		PreparedStatement prep = this.conn.prepareStatement(query);
		prep.setString(1, name);
		prep.execute();
		ResultSet r = prep.getResultSet();
		while(r.next()){
			result.add(r.getInt(1));
			result.add(r.getInt(2));
		}
		r.close();
		return result;
	}
	/**
	 * trying to find names that match more or less the
	 * input of the user (limited to 2 entries)
	 * @param tracks - the tracks,commas separated
	 * @param chr - the chromosome
	 * @param name - the string to match
	 * @param log 
	 * @return
	 * @throws SQLException 
	 */
	public Map<String, List<Integer>> suggestGeneNamesAndPositionsForChromosome(
			String tracks, String chr, String name) throws SQLException {
		Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
		String query = "SELECT name,start,end FROM \""+chr+"\" where name like ? limit 2; ";
		PreparedStatement prep = this.conn.prepareStatement(query);
		prep.setString(1,"%"+name+"%");
		prep.execute();
		ResultSet r = prep.getResultSet();
		while(r.next()){
			String rname = r.getString(1);
			int start = r.getInt(2);
			int end = r.getInt(3);
			List<Integer> list;
			if(result.containsKey(rname)){
				list = result.get(rname);
			} else {
				list = new ArrayList<Integer>();
			}
			list.add(start);
			list.add(end);
			result.put(rname, list);
		}
		r.close();
		return result;
	}




}
