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

import ch.epfl.bbcf.gdv.access.database.pojo.Group;
import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Species;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.wrapper.ProjectWrapper;

public class DataProjectProvider extends SortableDataProvider<ProjectWrapper>{

	private UserSession session;
	private List<ProjectWrapper> projects;
	private Users user;

	public DataProjectProvider(UserSession session){
		this.session = session;
		this.user = session.getUser();
		//List<Project> p = pc.getProjectsFromUser();
		List<Project> p = ProjectControl.getAllProjectFromUser(session.getUser());
		projects = getProjectsWrappers(p);
		setSort("name", true);
	}


	private List<ProjectWrapper> getProjectsWrappers(List<Project> projects) {
		List<ProjectWrapper> wrappers = new ArrayList<ProjectWrapper>();
		for(Project project : projects){
			ProjectWrapper wrapper = new ProjectWrapper(project);
			Species species = ProjectControl.getSpeciesFromProjectId(project.getId());
			// #species
			wrapper.setSpeciesName(species.getName());
			wrapper.setSpeciesId(species.getId());
			wrapper.setSequences(ProjectControl.getSequencesFromSpeciesIdSO(species.getId()));
			// #groups
			List<Group> groups = ProjectControl.getGroupNameFromProjectId(project.getId(),user.getMail());
			String groupNames = "";
			for(Group group:groups){
				groupNames+=group.getName()+",";
			}
			if(groupNames.length()>0){
				groupNames = groupNames.substring(0, groupNames.length()-1);
				wrapper.setAdmin(false);
			} else {
				wrapper.setAdmin(true);
			}
			wrapper.setGroupName(groupNames);
			// #tracks number
			int tn = ProjectControl.tracksNumberUnderProject(project.getId());
			wrapper.setTracksNumber(tn);
			
			// #public url
			if(project.isPublic()){
				String url = ProjectControl.getPublicUrlFromProjectId(session.getUser(),project.getId());
				wrapper.setPublicUrl(url);
			}
			
			// #adding wrapper
			wrappers.add(wrapper);
		}
		return wrappers;
	}




	public Iterator<? extends ProjectWrapper> iterator(int first, int count) {
		return projects.iterator();
	}

	public IModel<ProjectWrapper> model(final ProjectWrapper object) {
		return new LoadableDetachableModel<ProjectWrapper>(){
			@Override
			protected ProjectWrapper load() {
				return object;
			}
		};
	}

	public int size() {
		return projects.size();
	}

	public void detach() {
		List<Project> p = ProjectControl.getAllProjectFromUser(session.getUser());
		projects = getProjectsWrappers(p);
	}

}
