package ch.epfl.bbcf.gdv.html.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper.SortByDate;
import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper.SortByTrackName;

public class DataTrackProvider extends SortableDataProvider<TrackWrapper>{

	private UserSession session;
	private List<TrackWrapper> tracks;
	private ProjectWrapper project;
	private TrackControl tc;

	public DataTrackProvider(UserSession session, ProjectWrapper pw){
		this.session = session;
		this.project = pw;
		tc = new TrackControl(session);
		List<Track> t = tc.getTracksFromProjectId(pw.getId()); 
		tracks = getTrackWrappers(t);
		setSort("track_name", true);
	}


	private List<TrackWrapper> getTrackWrappers(List<Track> tracks) {
		if(tracks!=null){
			List<TrackWrapper> wrappers = new ArrayList<TrackWrapper>();
			for(Track t : tracks){
				TrackWrapper wrapper = new TrackWrapper(t);
				//get the date
				TrackControl tc = new TrackControl(session);
				wrapper.setDate(tc.getDate(t.getId()));
				//get species and assembly
				wrappers.add(wrapper);
			}
			return wrappers;	
		}
		return new ArrayList<TrackWrapper>();
	}




	public Iterator<? extends TrackWrapper> iterator(int first, int count) {
		SortParam sp = getSort();
		Comparator comp;
		if (sp.getProperty().equalsIgnoreCase("track_name")){
			comp = new TrackWrapper.SortByTrackName();
		} else if (sp.getProperty().equalsIgnoreCase("date")){
			comp = new TrackWrapper.SortByDate();
		}
		else {
			return tracks.iterator();
		}
		Collections.sort(tracks, comp);
		if(!sp.isAscending()){
			Collections.reverse(tracks);
		}
		return tracks.iterator();
	}

	//	public IModel<TrackWrapper> model(final TrackWrapper object) {
	//		return new LoadableDetachableModel<TrackWrapper>(){
	//			@Override
	//			protected TrackWrapper load() {
	//				return object;
	//			}
	//		};
	//	}

	public int size() {
		return tracks.size();
	}

	public void detach() {
		List<Track> t = tc.getTracksFromProjectId(project.getId()); 
		tracks = getTrackWrappers(t);
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

}
