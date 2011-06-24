package ch.epfl.bbcf.gdv.html.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class DataAdminTrackProvider extends SortableDataProvider<TrackWrapper>{

	private List<TrackWrapper> tracks;
	
	public DataAdminTrackProvider(UserSession session){
		tracks = getTrackWrappers(TrackControl.getAllAdminTracks());
	}
	private List<TrackWrapper> getTrackWrappers(Set<Track> allAdminTracks) {
		List<TrackWrapper> wrappers = new ArrayList<TrackWrapper>();
		for(Track t : allAdminTracks){
			TrackWrapper wrapper = new TrackWrapper(t);
			wrappers.add(wrapper);
		}
		return wrappers;
	}
	@Override
	public Iterator<? extends TrackWrapper> iterator(int arg0, int arg1) {
		return tracks.iterator();
	}

	@Override
	public IModel<TrackWrapper> model(final TrackWrapper object) {
		return new LoadableDetachableModel<TrackWrapper>(){
			@Override
			protected TrackWrapper load() {
				return object;
			}
		};
	}

	@Override
	public int size() {
		return tracks.size();
	}
	
	public void detach() {
		tracks = getTrackWrappers(TrackControl.getAllAdminTracks());
	}

}
