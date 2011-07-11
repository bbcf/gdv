package ch.epfl.bbcf.gdv.control.model;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.bbcfutils.sqlite.SQLiteAccess;
import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO;
import ch.epfl.bbcf.gdv.access.database.dao.TypeDAO;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;

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

	
	
	public static Type getType(String type){
		TypeDAO dao = new TypeDAO();
		Type t = dao.getType(type);
		dao.release();
		return t;
	}
	
	/**
	 * create the type if not exist,
	 * else return the existing one
	 * @param type
	 * @return
	 */
	public static Type createType(String type){
		TypeDAO dao = new TypeDAO();
		Type t = dao.getType(type);
		if(null==t){
			t = dao.createType(type);			
		}
		return t;
	}


	/**
	 * get the style for the user and for the type
	 * if none, it will be created
	 * @param userId the user
	 * @param types the types
	 * @return
	 */
	public static List<StyleWrapper> getStyleFromUserIdAndTypes(
			int userId, List<Type> types) {
		List<StyleWrapper> wrappers = new ArrayList<StyleWrapper>();
		StyleDAO sdao = new StyleDAO();
		for(Type type : types){
			Style userStyle = sdao.getUserStyleFromTypeId(userId,type.getId());
			if(null==userStyle){
				userStyle = createStyleForUserAndType(userId,type);
			}
			wrappers.add(new StyleWrapper(type, userStyle));
		}
		sdao.release();
		return wrappers;
	}

	private static Style createStyleForUserAndType(int userId, Type type) {
		Style style = buildStyleForType(type);
		StyleDAO sdao = new StyleDAO();
		sdao.createStyleForUserAndType(userId, type.getId(),style.getId());
		sdao.release();
		return style;
	}

	public static boolean setStyleForUserAndType(int userId, Type type,Style style){
		StyleDAO sdao = new StyleDAO();
		boolean b = sdao.setStyleForUserAndType(userId, type.getId(),style.getId());
		sdao.release();
		return b;
	}
	/**
	 * create a style for this type. It can be random, or 
	 * admin defined for certain types
	 * @param type
	 * @return
	 */
	private static Style buildStyleForType(Type type) {
		return Style.randomStyle();
	}
	
	public static List<String> getTypesColors(){
		StyleDAO sdao = new StyleDAO();
		List<String> colors = sdao.getStylesColors();
		sdao.release();
		return colors;
	}
}
