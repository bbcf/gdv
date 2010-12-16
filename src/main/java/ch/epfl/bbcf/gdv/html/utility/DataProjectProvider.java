package ch.epfl.bbcf.gdv.html.utility;

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
import ch.epfl.bbcf.gdv.access.generep.AssembliesAccess;
import ch.epfl.bbcf.gdv.access.generep.GeneRepAccess;
import ch.epfl.bbcf.gdv.access.generep.SpeciesAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;

public class DataProjectProvider extends SortableDataProvider<ProjectWrapper>{

	private UserSession session;
	private List<ProjectWrapper> projects;

	public DataProjectProvider(UserSession session){
		this.session = session;
		
		ProjectControl pc = new ProjectControl(session);
		List<Project> p = pc.getProjectsFromUser();
		projects = getProjectsWrappers(pc,p);
		setSort("name", true);
	}


	private List<ProjectWrapper> getProjectsWrappers(ProjectControl controller, List<Project> projects) {
		List<ProjectWrapper> wrappers = new ArrayList<ProjectWrapper>();
		for(Project project : projects){
			ProjectWrapper wrapper = new ProjectWrapper(project);
			//-> get species & assembly
			JSONObject assembly = AssembliesAccess.getAssemblyById(Integer.toString(project.getSequenceId()));
			JSONObject species = SpeciesAccess.getOrganismByAssemblyJSON(assembly);
			try {
				wrapper.setSpecies(species.getJSONObject(SpeciesAccess.ORGANISM_KEY).getString(SpeciesAccess.NAME_KEY));
				wrapper.setVersion(assembly.getString(AssembliesAccess.NAME_KEY));
				wrapper.setVersionId(assembly.getString(SpeciesAccess.ID_KEY));
			} catch (JSONException e) {
				Application.error("error setting the project wrapper : "+e);
			}
			//->get tracks number
			int tn = controller.tracksNumberUnderProject(project.getId());
			wrapper.setTracksNumber(tn);
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
	}

}
