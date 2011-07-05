package ch.epfl.bbcf.gdv.access.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.pojo.Job;
import ch.epfl.bbcf.gdv.access.database.pojo.Status;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;

public class StyleDAO extends DAO<Style>{

	public enum STYLE_HEIGHT {small,medium,big} ;
	public enum STYLE_COLOR {blue,green,red,yellow,
		pink,black,BlueViolet,Chocolate,
		Orange,CornflowerBlue,Crimson,
		Cyan,DarkOliveGreen,DarkOrchid,
		DarkSalmon,Gold,LawnGreen,
		Magenta,NavajoWhite,Orchid,
		SaddleBrown,SteelBlue,YellowGreen} ;

		public StyleDAO(Connection connection) {
			super(connection);
		}


		private Style getStyle(ResultSet r){
			Style sty = new Style();
			if(this.databaseConnected()){
				try {
					sty.setId(r.getInt("id"));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					sty.setStyle_color(r.getString("style_color"));
				} catch (SQLException e) {
					logger.error(e);
				}
				try {
					sty.setStyle_height(r.getString("style_height"));
				} catch (SQLException e) {
					logger.error(e);
				}
				return sty;
			}
			return null;
		}

		private List<Style> getStyles(ResultSet r){
			List<Style> styles = new ArrayList<Style>();
			try {
				while(r.next()){
					styles.add(getStyle(r));
				}
			} catch (SQLException e) {
				logger.error(e);
			}
			return styles;
		}


		public Style getStyleForTrackIdAndType(int trackId,String type){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select t1.* from styles as t1 " +
					"inner join trackToStyle as t2 on t1.id = t2.style_id " +
					"where t2.track_id= ? and t2.type = ? limit 1;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1,trackId);
					statement.setString(2,type);
					ResultSet resultSet = this.executeQuery(statement);
					Style style = getStyle(resultSet);
					this.endQuery(true);
					return style;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		public List<Style> getStylesForTrackId(int trackId){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select t1.* from styles as t1 " +
					"inner join trackToStyle as t2 on t1.id = t2.style_id " +
					"where t2.track_id= ? ;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1,trackId);
					ResultSet resultSet = this.executeQuery(statement);
					List<Style> styles = getStyles(resultSet);
					this.endQuery(true);
					return styles;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		public boolean setStyleForTrackAndType(int trackId,String type,Style style){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "insert into trackToStyle values (?,?,?)";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1,trackId);
					statement.setString(2,type);
					statement.setInt(3, style.getId());
					this.executer(statement);
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return false;
		}



		public List<String> getStylesColors(){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select distinct color from styles;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet resultSet = this.executeQuery(statement);
					List<String> colors = new ArrayList<String>();
					while(resultSet.next()){
						colors.add(resultSet.getString(1));
					}
					this.endQuery(true);
					return colors;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		public List<String> getStylesHeight(){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select distinct height from styles;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet resultSet = this.executeQuery(statement);
					List<String> colors = new ArrayList<String>();
					while(resultSet.next()){
						colors.add(resultSet.getString(1));
					}
					this.endQuery(true);
					return colors;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		public List<String> getTrackTypes(int trackId){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select type from trackToStyle " +
					"where track_id = ? ;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1,trackId);
					ResultSet resultSet = this.executeQuery(statement);
					List<String> types = new ArrayList<String>();
					while(resultSet.next()){
						types.add(resultSet.getString(1));
					}
					this.endQuery(true);
					return types;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		public Style getStyleByStyle(STYLE_COLOR color,STYLE_HEIGHT height){
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select * from style " +
					"where color = ?::style_color and height= ?::style_height limit 1;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setString(1, color.toString());
					statement.setString(2, height.toString());
					ResultSet resultSet = this.executeQuery(statement);
					if(resultSet.next()){
						Style s =  getStyle(resultSet);
						this.endQuery(true);
						return s;
					}
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}

		/**
		 * get all types and associated styles belonging to an user
		 * @param userId
		 * @return
		 */
		public Map<Type, Style> getTypesAndStyleFromUser(int userId) {
			if(this.databaseConnected()){
				this.startQuery();
				Map<Type, Style> map = new HashMap<Type, Style>();
				try {
					String query = "select type_id,style_id from user_style " +
					"where user_id = ? ;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1, userId);
					ResultSet resultSet = this.executeQuery(statement);

					while(resultSet.next()){
						int type_id = resultSet.getInt(1);
						int style_id = resultSet.getInt(2);
						Style s =  getStyleById(style_id);
						TypeDAO dao = new TypeDAO(Conn.get());
						Type t = dao.getTypeById(type_id);
						map.put(t, s);
					}
					this.endQuery(true);
					return map;
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}


		private Style getStyleById(int style_id) {
			if(this.databaseConnected()){
				this.startQuery();
				try {
					String query = "select * from style " +
					"where id = ? ;";
					PreparedStatement statement = this.prepareStatement(query,
							ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					statement.setInt(1, style_id);
					ResultSet resultSet = this.executeQuery(statement);
					if(resultSet.next()){
						Style s =  getStyle(resultSet);
						this.endQuery(true);
						return s;
					}
				} catch (SQLException e) {
					logger.error(e);
					this.endQuery(false);
				}
			}
			return null;
		}
}