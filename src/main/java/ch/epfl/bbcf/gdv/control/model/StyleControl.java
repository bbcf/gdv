package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class StyleControl {

	/**
	 * fetch the types of the qualitative extended track in the SQLite db
	 * @param trackId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static List<String> getTrackTypesFromLocalFile(int trackId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String file = TrackControl.getFileFromTrackId(trackId);
		if(null==file){
			return null;
		}
		SQLiteAccess  access = SQLiteAccess.getConnectionWithDatabase(Configuration.getFilesDir()+File.separator+file);
		return access.getTypes();
	}

	/**
	 * get the types of this track
	 * @param trackId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static List<String> getTrackTypesFromDatabase(int trackId) throws SQLException{
		StyleDAO dao = new StyleDAO(Conn.get());
		return dao.getTrackTypes(trackId);
	}




	public static List<Style> getStylesFromTrackId(int trackId){
		StyleDAO dao = new StyleDAO(Conn.get());
		return dao.getStylesForTrackId(trackId);
	}

	public static Style getStyleForTrackIdAndType(int trackId,String type){
		StyleDAO dao = new StyleDAO(Conn.get());
		return dao.getStyleForTrackIdAndType(trackId, type);
	}

	
	/**
	 * build randomly style for the differents tracks types
	 * @param trackId
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static List<String> buildRandomTypeForTrack(int trackId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		List<String> types = getTrackTypesFromLocalFile(trackId);
		if(null==types){
			Application.error("no types for this tracks !!!!!!!");
			return null;
		}
		StyleDAO dao = new StyleDAO(Conn.get());
		for(String t : types){
			dao.setStyleForTrackAndType(trackId,t,Style.randomStyle());
		}
		return types;
	}
	/**
	 * set the style given for the track given, and if the type exist in another track of the same project
	 * @param trackId
	 * @param type
	 * @param style
	 * @return
	 */
	public static boolean setStyleForTrackAndType(int trackId,String type,Style style){
		StyleDAO dao = new StyleDAO(Conn.get());
		return dao.setStyleForTrackAndType(trackId, type, style);
	}
	/**
	 * get all types (and their style associated) from an user
	 * @param userId
	 * @return
	 */
	public static Map<Type, Style> getTypesAndStyleFromUser(int userId) {
		StyleDAO dao = new StyleDAO(Conn.get());
		return dao.getTypesAndStyleFromUser(userId);
	}

}
