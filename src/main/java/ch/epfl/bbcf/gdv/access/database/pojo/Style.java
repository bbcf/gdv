package ch.epfl.bbcf.gdv.access.database.pojo;

import java.io.Serializable;
import java.util.Random;

import ch.epfl.bbcf.gdv.access.database.Conn;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_COLOR;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_HEIGHT;

public class Style implements Serializable{

	private int id;
	private STYLE_HEIGHT style_height;
	private STYLE_COLOR style_color;
	
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	
	public void setStyle_height(String style_height) {
		this.style_height = STYLE_HEIGHT.valueOf(STYLE_HEIGHT.class,style_height);
	}
	public STYLE_HEIGHT getStyle_height() {
		return style_height;
	}
	public void setStyle_color(String style_color) {
		this.style_color =  STYLE_COLOR.valueOf(STYLE_COLOR.class,style_color);
	}
	public void setStyle_color(STYLE_COLOR style_color) {
		this.style_color =  style_color;
	}
	public STYLE_COLOR getStyle_color() {
		return style_color;
	}
	public static Style randomStyle() {
		Random generator = new Random();
		STYLE_COLOR styleColor = STYLE_COLOR.values()[generator.nextInt(STYLE_COLOR.values().length)];
		STYLE_HEIGHT styleHeight = STYLE_HEIGHT.values()[generator.nextInt(STYLE_HEIGHT.values().length)];
		StyleDAO dao = new StyleDAO();
		Style style =  dao.getStyleByStyle(styleColor, styleHeight);
		dao.release();
		return style;
	}
	public void setStyle_height(STYLE_HEIGHT style_height2) {
		this.style_height=style_height2;
	}
}
