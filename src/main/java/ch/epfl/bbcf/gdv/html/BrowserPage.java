package ch.epfl.bbcf.gdv.html;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import ch.epfl.bbcf.gdv.access.database.pojo.Project;
import ch.epfl.bbcf.gdv.access.database.pojo.Sequence;
import ch.epfl.bbcf.gdv.access.database.pojo.Track;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.access.jbrowsor.JbrowsoRAccess;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.ProjectControl;
import ch.epfl.bbcf.gdv.control.model.SequenceControl;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.html.utility.MenuElement;


public class BrowserPage extends WebPage{

	public BrowserPage(PageParameters p) {
		super(p);

		//getting parameters
		int projectId = p.getInt("id");

		ProjectControl pc = new ProjectControl((UserSession)getSession());
		final Project project = pc.getProject(projectId);
		if(null==project || (null!=project && !pc.userAuthorized(project))){
			return;
		}

		//change the display of the top menu if the user is logged as a group
		Users user = ((UserSession)getSession()).getUser();
		if(null!=user && Configuration.getGdv_types_access().contains(user.getType())){
			MenuElement[] els = {new MenuElement(AlternativeProjectPage.class, "Limited Profile"),
					new MenuElement(AlternativeProjectPage.class, "Projects")};
			add(new MenuPage("menu",Arrays.asList(els)));
		} else {
			add(new MenuPage("menu",Configuration.getNavigationLinks()));
		}


		//adding speciesName on the view
		SequenceControl sc = new SequenceControl((UserSession)getSession());
		//String [] tmp = sc.getSpeciesNameAndAssemblyNameFromAssemblyId(project.getSpeciesId());
		//String species = tmp[0];
		add(new Label("species",project.getSpecies().getName()));

		//adding static javascript and css
		for(String cp : Configuration.getGDVCSSFiles()){
			add(CSSPackageResource.getHeaderContribution(cp));
		}
		for (String cp : Configuration.getJbrowseCSSFiles()){
			add(CSSPackageResource.getHeaderContribution(cp));
		}
		for(String cp : Configuration.getJavascriptFiles()){
			add(JavascriptPackageResource.getHeaderContribution(cp));
		}


		//get trackInfo.js 
		TrackControl tc = new TrackControl((UserSession)getSession());
		List<Track> tracks = tc.getCompletedTracksFromProjectId(project.getId());
		List<Track> adminTrack = tc.getAdminTracksFromSpeciesId(project.getSequenceId());
		tracks.addAll(adminTrack);
		final String trackInfo = getTrackInfo(tracks,tc,project.getSpecies().getName());
		//Application.debug("get track Info :"+trackInfo);
		//get names
		String names = getTrackNames(tracks);
		//get refseq.js
		SequenceControl seqc = new SequenceControl((UserSession)getSession());
		Sequence seq = seqc.getSequenceFromId(project.getSequenceId());
		final String refseqs = JbrowsoRAccess.getRefseq(seq.getJbrowsoRId());
		//String refseq = InternetConnection.sendGETConnection("http://ptbbpc1.epfl.ch/jbrowsor/jbrowse/data/67/data/refSeqs.js");
		//
		//adding final javascript
		final String jsControl = " var b = new Browser({" +
		"containerID: \"GenomeBrowser\",\n" +
		"refSeqs: refSeqs,\n" +
		//"browserRoot: \""+ JbrowsoRAccess.JBROWSE_DATA+"\"+browserRoot,\n" +//+JbrowsoRAccess.SERV+"/\"+browserRoot," +
		"browserRoot: \""+ JbrowsoRAccess.JBROWSE_DATA+   "\",\n" +
		//"dataRoot: \"/jbdata/\",\n"+
		//		"dataRoot: \""+JbrowsoRAccess.JBROWSE_DATA+"\"+dataRoot,\n" +
		"dataRoot: \""+JbrowsoRAccess.JBROWSE_DATA+"/"+"\",\n" +
		"styleRoot: \""+"../../"+Configuration.getJbrowse_Images_url()+"/"+"\",\n" +
		"trackData: trackInfo,\n" +
		"defaultTracks : "+names+"" +
		"});" +
		"b.showTracks("+names+");";

		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderJavascript(refseqs,"refseq_"+project.getId());
				response.renderJavascript(trackInfo,"js_view_"+project.getId());
				response.renderJavascript(jsControl,"js_control_"+project.getId());
			}
		}); 

		//adding tabbedPanel
		List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model("view")){
			@Override
			public Panel getPanel(String id) {
				return new BrowserPanel(id);
			}
		});

		tabs.add(new AbstractTab(new Model("gMiner")){
			@Override
			public Panel getPanel(String id) {
				return new GFeatMinerPanel(id);
			}
		});
		add(new TabbedPanel("tabs", tabs));
		
		//building gFeatMenu
		add(new GFeatMinerMenu("gfeat_menu",tracks));
		








	}

	/**
	 * build the track names that will be showed in the browser
	 * @param tracks
	 * @return
	 */
	private String getTrackNames(List<Track> tracks) {
		String names = "\"DNA,";
		for(Track t : tracks){
			names+=protect(t.getName())+",";
		}
		names=names.substring(0, names.length()-1);
		names+="\"";
		return names;
	}

	/**
	 * build the trackInfo needed by jbrowse
	 * @param tracks
	 * @param tc
	 * @return
	 */
	private String getTrackInfo(List<Track> tracks, TrackControl tc,String species) {
		String result ="trackInfo = [{"+
		"\"url\" : \"data/seq/{refseq}/\","+
		"\"args\" : {"+
		"\"chunkSize\" : 20000"+
		"},"+
		"\"label\" : \"DNA\","+
		"\"type\" : \"SequenceTrack\","+
		"\"key\" : \"DNA\""+
		"},";
		for(Track t : tracks){
			String parameters ="";
			if(t.getParameters().equalsIgnoreCase("params") || t.getName().equalsIgnoreCase("in process")){
				String directory = tc.getFileFromTrackId(t.getId());
				String imageType = null;
				if(t.getType().equalsIgnoreCase("quantitative")){
					imageType="ImageTrack";
				} else if(t.getType().equalsIgnoreCase("qualitative")){
					imageType="FeatureTrack";
				} else {
					Application.error("datatype not recognized : "+t.getId());
				}

				String params = "{\n\"url\" : \"../"+directory+"/{refseq}.json\",\n" +
				"\"label\" : \""+protect(t.getName())+"\",\n"+
				"\"type\" : \""+imageType+"\",\n"+
				"\"key\" : \""+protect(t.getName())+"\"\n}";
				tc.setParams(t.getId(),params);
				parameters = params;
			} else {
				parameters = t.getParameters();
			}
			result+=parameters+",";
		}
		result = result.substring(0, result.length()-1);
		return result+"]";
	}

	/**
	 * protect char " with a backslash
	 * if there is one in the name
	 * @param name
	 * @return
	 */
	private static String protect(String name) {
		return name.replaceAll("\"", "\\\\\"");
	}


}
