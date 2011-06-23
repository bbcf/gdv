//package ch.epfl.bbcf.gdv.html.utility;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.apache.wicket.PageParameters;
//import org.apache.wicket.RequestCycle;
//import org.apache.wicket.markup.html.WebPage;
//
//import ch.epfl.bbcf.gdv.access.database.pojo.Project;
//import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
//import ch.epfl.bbcf.gdv.access.database.pojo.Track;
//import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
//import ch.epfl.bbcf.gdv.config.UserSession;
//import ch.epfl.bbcf.gdv.control.model.ProjectControl;
//import ch.epfl.bbcf.gdv.control.model.SequenceControl;
//import ch.epfl.bbcf.gdv.control.model.TrackControl;
//import ch.epfl.bbcf.gdv.html.BrowserPage;
//import ch.epfl.bbcf.gdv.html.HomePage;
//
//public class BrowserPageBuilder {
//
//	public static void buildPage(UserSession session,RequestCycle requestCycle, int projectId) {
//		
//		
//		PageParameters params = new PageParameters();
//	
//		//check if authorized
//		ProjectControl pc = new ProjectControl(session);
//		final Project project = pc.getProject(projectId);
//		if(null==project || (null!=project && !pc.userAuthorized(project))){
//			requestCycle.setResponsePage(HomePage.class,params);
//		}
//				
//		//get tracks
//		TrackControl tc = new TrackControl(session);
//		Set<Track> tracks = tc.getCompletedTracksFromProjectId(project.getId());
//		Set<Track> adminTrack = tc.getAdminTracksFromSpeciesId(project.getSequenceId());
//		tracks.addAll(adminTrack);
//		
//		Set<Track> formattedTracks = getFormattedTracks(tc,tracks);
//		String names = getTrackNames(formattedTracks);
//		
//
//		//build parameters & url
//		params.add("id", Integer.toString(projectId));
//		params.add("tn",names);
//		
//
//		requestCycle.setResponsePage(BrowserPage.class,params);
//	}
//	
//	
//	/**
//	 * If two track names are the same, jBrowse will bug
//	 * so this method will update the track name & params
//	 * @param tc 
//	 * @param tracks
//	 * @return
//	 */
//	private static Set<Track> getFormattedTracks(TrackControl tc, Set<Track> tracks) {
//		Set<String> nameSet = new HashSet<String>();
//		Set<Track> formattedTracks = new HashSet<Track>();
//		for(Track t : tracks){
//			String nameToAdd = protect(t.getName());
//			if(!nameSet.contains(nameToAdd)){
//				nameSet.add(nameToAdd);
//				formattedTracks.add(t);
//			} else {
//				int cpt = 1;
//				String altName = nameToAdd;
//				while(nameSet.contains(altName)){
//					altName=nameToAdd+"_"+cpt;
//					cpt++;
//				}
//				nameSet.add(altName);
//				tc.renameTrack(t.getId(), altName);
//				t.setName(altName);
//				formattedTracks.add(t);
//			}
//		}
//		return formattedTracks;
//	}
//
//	/**
//	 * build the track names that will be showed in the browser
//	 * @param tracks
//	 * @return
//	 */
//	private static String getTrackNames(Set<Track> tracks) {
//		String names = "\"DNA,";
//		for(Track t : tracks){
//			String nameToAdd = protect(t.getName());
//			names+=nameToAdd+",";
//		}
//		names=names.substring(0, names.length()-1);
//		names+="\"";
//		return names;
//	}
//	/**
//	 * protect char " with a backslash
//	 * if there is one in the name
//	 * @param name
//	 * @return
//	 */
//	private static String protect(String name) {
//		return name.replaceAll("\"", "\\\\\"");
//	}
//
//
//}
