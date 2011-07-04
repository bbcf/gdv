package ch.epfl.bbcf.gdv.html.wrapper;

import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_COLOR;
import ch.epfl.bbcf.gdv.access.database.dao.StyleDAO.STYLE_HEIGHT;
import ch.epfl.bbcf.gdv.access.database.pojo.Style;

public class StyleWrapper {

	private Style style;
	private String name;
	public StyleWrapper(String t, Style style) {
		this.name = t;
		this.style = style;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setStyle_height(String style_height) {
		this.style.setStyle_height(style_height);
	}
	public STYLE_HEIGHT getStyle_height() {
		return style.getStyle_height();
	}
	public void setStyle_color(String style_color) {
		this.style.setStyle_color(style_color);
	}
	public STYLE_COLOR getStyle_color() {
		return style.getStyle_color();
	}
	public Style getStyleObject() {
		return style;
	}
}
