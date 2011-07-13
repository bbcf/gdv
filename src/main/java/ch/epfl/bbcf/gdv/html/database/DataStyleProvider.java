package ch.epfl.bbcf.gdv.html.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.access.database.pojo.Type;
import ch.epfl.bbcf.gdv.control.model.StyleControl;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;

public class DataStyleProvider extends SortableDataProvider<StyleWrapper>{


	private List<StyleWrapper> styles;
	private List<Type> types;
	private int userId;

	public DataStyleProvider(int userId,List<Type> types){
		this.userId = userId;
		this.types = types;
		styles = new ArrayList<StyleWrapper>();
		/* get the user styles of these type */
		styles = StyleControl.getStyleFromUserIdAndTypes(userId,types);
	}
	@Override
	public Iterator<StyleWrapper> iterator(int arg0, int arg1) {
		return styles.iterator();
	}

	@Override
	public int size() {
		return styles.size();
	}

	@Override
	public IModel<StyleWrapper> model(final StyleWrapper object) {
		return new LoadableDetachableModel<StyleWrapper>(){
			@Override
			protected StyleWrapper load() {
				return object;
			}

		};
	}

	@Override
	public void detach() {
		styles = StyleControl.getStyleFromUserIdAndTypes(userId,types);
	}

}
