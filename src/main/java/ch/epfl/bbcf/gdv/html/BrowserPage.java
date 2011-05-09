package ch.epfl.bbcf.gdv.html;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.bbcf.bbcfutils.access.genrep.GenrepWrapper;
import ch.epfl.bbcf.bbcfutils.access.genrep.MethodNotFoundException;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Assembly;
import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
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
		int projectId = -1;	
		String userKey = null;
		String publicKey = null;
		String err ="";
		
		
		//get request parameters
		for(Entry<String, String[]> entry : p.toRequestParameters().entrySet()){
			if(entry.getKey().equalsIgnoreCase("id")){
				try{
					projectId = Integer.parseInt(entry.getValue()[0]);
				} catch(NumberFormatException nfe){
					err="project id (id) must be an Integer";
					PageParameters params = new PageParameters();
					params.put("err", err);
					throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
				}
			} else if(entry.getKey().equalsIgnoreCase("ukey")){
				userKey = entry.getValue()[0];
			} else if(entry.getKey().equalsIgnoreCase("pkey")){
				publicKey = entry.getValue()[0];
			}
		}
		//check project id
		if(projectId<0){
			err="no project id (id) in the request, you must log in";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}
		
		
		ProjectControl pc = new ProjectControl((UserSession)getSession());
		final Project project = pc.getProject(projectId);
		if(null==project){
			err="project id ("+projectId+") doesn't exist or you must login before";
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(new ErrorPage(params));
		}
		boolean canView = false;//if the user can see the page
		boolean isAdmin =false;//if the user provide uKey & pKey but he don't
							   //need them because he own the page
		UserSession session = (UserSession)getSession();
		Users user = session.getUser();
		
		//it can be a public view
		if(userKey!=null && publicKey!=null){
			if(pc.isProjectPublic(projectId)){
				String pKey = pc.getPublicKeyFromProjectId(projectId);
				if(publicKey.equalsIgnoreCase(pKey)){
					String uKey = pc.getUserKeyFromProjectId(projectId);
					if(userKey.equalsIgnoreCase(uKey)){
						canView = true;
					} else {err="wrong uKey";};
				}else {err="wrong pKey";};
			} else {err="not a public project";};
			//else the user must have the rights to view this 
		} 	
		if(user!=null && pc.userAuthorized(project)){
			canView = true;
			isAdmin=true;
		} else {err="not authorized to browse this view";};
		
		if(!canView){
			PageParameters params = new PageParameters();
			params.put("err", err);
			throw new RestartResponseAtInterceptPageException(ErrorPage.class);
		}
	
		//change the display of the menu if
		//it's public or admin
		if(isAdmin){
			add(new MenuPage("menu",Configuration.getNavigationLinks()));
		} else {
			MenuElement[] els = {new MenuElement(LoginPage.class, "import in my profile",true,projectId)};
			add(new MenuPage("menu",Arrays.asList(els)));
		}
		
		
	


		//adding speciesName on the view
		SequenceControl sc = new SequenceControl((UserSession)getSession());
		//String [] tmp = sc.getSpeciesNameAndAssemblyNameFromAssemblyId(project.getSpeciesId());
		//String species = tmp[0];
		add(new Label("species",project.getSpecies().getName()));
		add(new Label("nrAssemblyId",Integer.toString(project.getSequenceId())));
		add(new Label("gdv_project_id",Integer.toString(project.getId())));
		
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
		//get tracks
		TrackControl tc = new TrackControl((UserSession)getSession());
		Set<Track> tracks = tc.getCompletedTracksFromProjectId(project.getId());
		Set<Track> adminTrack = tc.getAdminTracksFromSpeciesId(project.getSequenceId());
		tracks.addAll(adminTrack);

		Set<Track> formattedTracks = getFormattedTracks(tc,tracks);
		final String trackInfo = getTrackInfo(formattedTracks,tc,project.getSpecies().getName());
		//Application.debug("get track Info :"+trackInfo);
		//get names
		String tracksNames = getTrackNames(formattedTracks);
		//get refseq.js
//		SequenceControl seqc = new SequenceControl((UserSession)getSession());
//		Sequence seq = seqc.getSequenceFromId(project.getSequenceId());
		final String refseq = buildRefseq(project.getSequenceId());//JbrowsoRAccess.getRefseq(seq.getJbrowsoRId());



		//
		//adding final javascript
		final String jsControl = " var b = new Browser({" +
		"containerID: \"GenomeBrowser\",\n" +
		"refSeqs: refSeqs,\n" +
		//"browserRoot: \""+ JbrowsoRAccess.JBROWSE_DATA+"\"+browserRoot,\n" +//+JbrowsoRAccess.SERV+"/\"+browserRoot," +
		"browserRoot: \""+ Configuration.getJb_browser_root()+   "\",\n" +
		//"dataRoot: \"/jbdata/\",\n"+
		//		"dataRoot: \""+JbrowsoRAccess.JBROWSE_DATA+"\"+dataRoot,\n" +
		"dataRoot: \""+Configuration.getJb_data_root()+"/"+"\",\n" +
		"styleRoot: \""+"../../"+Configuration.getJbrowse_static_files_url()+"/"+"\",\n" +
		"trackData: trackInfo,\n" +
		"defaultTracks : "+tracksNames+"" +
		"});" +
		"b.showTracks();" +
		//"b.showTracks();" +
				"initGDV_browser();";

		add(new AbstractBehavior() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderJavascript(refseq,"refseq_"+project.getId());
				response.renderJavascript(trackInfo,"js_view_"+project.getId());
				response.renderJavascript(jsControl,"js_control_"+project.getId());
			}
		}); 

		//adding tabbedPanel
//		List<ITab> tabs = new ArrayList<ITab>();
//		tabs.add(new AbstractTab(new Model("view")){
//			@Override
//			public Panel getPanel(String id) {
//				return new BrowserPanel(id);
//			}
//		});
//
//		tabs.add(new AbstractTab(new Model("gMiner")){
//			@Override
//			public Panel getPanel(String id) {
//				return new GFeatMinerPanel(id);
//			}
//		});
//		add(new TabbedPanel("tabs", tabs));

		//building gFeatMenu
		//add(new GFeatMinerMenu("gfeat_menu",tracks));









	}
	/**
	 * build the refseq needed for the view
	 * @param jbrowsoRId
	 * @return
	 */
	private String buildRefseq(int seq_id) {
		String refSeq="refSeqs = ";
		JSONArray array = null;
		try {
			Assembly ass = GenrepWrapper.getAssemblyFromNrAssemblyId(seq_id);
			List<Chromosome> chromosomes = ass.getChromosomes();
			array = new JSONArray();
			for(Chromosome chromosome : chromosomes){
				JSONObject json = new JSONObject();
				json.put("length", chromosome.getLength());
				json.put("name", chromosome.getName());
				json.put("seqDir", "SEQDIR/");
				//TODO generate the chunk of fasta sequences
				json.put("start",0);
				json.put("end", chromosome.getLength());
				json.put("seqChunkSize",20000);
				
				array.put(json);
			}
			refSeq+= array.toString();
		} catch (MethodNotFoundException e) {
			Application.error(e);
		} catch (IOException e) {
			Application.error(e);
		} catch (JSONException e) {
			Application.error(e);
		}
		if(null==array){
			refSeq+="[]";
		}
		return refSeq;
	}
	/**
	 * If two track names are the same, jBrowse will bug
	 * so this method will update the track name & params
	 * @param tc 
	 * @param tracks
	 * @return
	 */
	private Set<Track> getFormattedTracks(TrackControl tc, Set<Track> tracks) {
		Set<String> nameSet = new HashSet<String>();
		Set<Track> formattedTracks = new HashSet<Track>();
		for(Track t : tracks){
			String nameToAdd = protect(t.getName());
			if(!nameSet.contains(nameToAdd)){
				nameSet.add(nameToAdd);
				formattedTracks.add(t);
			} else {
				int cpt = 1;
				String altName = nameToAdd;
				while(nameSet.contains(altName)){
					altName=nameToAdd+"_"+cpt;
					cpt++;
				}
				nameSet.add(altName);
				tc.renameTrack(t.getId(), altName);
				t.setName(altName);
				formattedTracks.add(t);
			}
		}
		return formattedTracks;
	}

	/**
	 * build the track names that will be showed in the browser
	 * @param tracks
	 * @return
	 */
	private String getTrackNames(Set<Track> tracks) {
		String names = "\"DNA,";
		for(Track t : tracks){
			String nameToAdd = protect(t.getName());
			names+=nameToAdd+",";
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
	private String getTrackInfo(Set<Track> tracks, TrackControl tc,String species) {
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
				Application.debug("TRACK ID "+t.getId());

				String directory = tc.getFileFromTrackId(t.getId());
				Application.debug("TRACK ID "+directory);

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
