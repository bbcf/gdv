package ch.epfl.bbcf.gdv.html.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.access.database.pojo.Style;
import ch.epfl.bbcf.gdv.access.database.pojo.Type;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.control.model.StyleControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.wrapper.StyleWrapper;

public class DataStyleProvider extends SortableDataProvider<StyleWrapper>{


	private List<StyleWrapper> styles;
	private int trackId;

	public DataStyleProvider(int userId){
		this.trackId = trackId;
		try {
			styles = new ArrayList<StyleWrapper>();
			Map<Type,Style> types = StyleControl.getTypesAndStyleFromUser(userId);
			if(null==types){
				types = StyleControl.buildRandomTypeForTrack(trackId);
			}

			for(String t: types){
				StyleWrapper sw = new StyleWrapper(t,TrackControl.getStyleForTrackIdAndType(trackId, t));
				styles.add(sw);
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
		super.detach();
		List<String> types;
		try {
			types = TrackControl.getTrackTypesFromDatabase(trackId);
			for(String t: types){
				StyleWrapper sw = new StyleWrapper(t,TrackControl.getStyleForTrackIdAndType(trackId, t));
				styles.add(sw);
			}
		} catch (SQLException e) {
			Application.error(e);
		}


	}

}
