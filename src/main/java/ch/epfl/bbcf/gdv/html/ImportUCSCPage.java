//package ch.epfl.bbcf.gdv.html;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//
//import org.apache.wicket.PageParameters;
//import org.apache.wicket.ajax.AjaxEventBehavior;
//import org.apache.wicket.ajax.AjaxRequestTarget;
//import org.apache.wicket.ajax.markup.html.AjaxLink;
//import org.apache.wicket.markup.html.WebMarkupContainer;
//import org.apache.wicket.markup.html.basic.Label;
//import org.apache.wicket.markup.html.form.ChoiceRenderer;
//import org.apache.wicket.markup.html.form.DropDownChoice;
//import org.apache.wicket.markup.html.form.Form;
//import org.apache.wicket.markup.html.form.IChoiceRenderer;
//import org.apache.wicket.markup.html.image.Image;
//import org.apache.wicket.markup.html.list.ListItem;
//import org.apache.wicket.markup.html.list.ListView;
//import org.apache.wicket.markup.html.panel.FeedbackPanel;
//import org.apache.wicket.markup.repeater.Item;
//import org.apache.wicket.markup.repeater.data.DataView;
//import org.apache.wicket.model.IModel;
//import org.apache.wicket.model.LoadableDetachableModel;
//import org.apache.wicket.model.Model;
//
//import ch.epfl.bbcf.bbcfutils.access.genrep.json_pojo.Chromosome;
//import ch.epfl.bbcf.gdv.access.genrep.GenrepWrapper;
//import ch.epfl.bbcf.gdv.config.Application;
//import ch.epfl.bbcf.gdv.config.UserSession;
//import ch.epfl.bbcf.gdv.control.model.SequenceControl;
//import ch.epfl.bbcf.gdv.formats.das.DAS;
////import ch.epfl.bbcf.gdv.formats.sqlite.SQLiteAccessManager;
//import ch.epfl.bbcf.gdv.html.utility.DataDASProvider;
//import ch.epfl.bbcf.gdv.html.utility.SelectOption;
//import ch.epfl.bbcf.gdv.html.wrapper.DASWrapper;
//
//public class ImportUCSCPage extends BasePage{
//
//	private Form form;
//	private DropDownChoice<SelectOption> ddcSpecies;
//	private DropDownChoice<SelectOption> ddcVersion;
//	private int speciesId = -1;
//	protected int assemblyId = -1;
//	protected String assemblyName;
//	private WebMarkupContainer dasContainer;
//	private ListView<DASWrapper> sources;
//	private Image image;
//
//
//	public ImportUCSCPage(PageParameters p) {
//		super(p);
//		Application.debug("import UCSC page",((UserSession)getSession()).getUserId());
//		form = new Form("form"){
//
//
//		};
//		form.add(new FeedbackPanel("feedback"));
//
//
//		//->Species
//		SelectOption[] spOptions = GenrepWrapper.getOrganismsSO();
//		IChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("value", "key");
//		ddcSpecies = new DropDownChoice<SelectOption>("species",new Model(),Arrays.asList(spOptions),choiceRenderer){
//			protected boolean wantOnSelectionChangedNotifications() {
//				return true;
//			}
//			protected void onSelectionChanged(final SelectOption newSelection){
//				speciesId = newSelection.getKey();
//				ddcVersion.updateModel();
//
//				sources.setDefaultModelObject(new ArrayList<DASWrapper>());
//
//			}
//		};
//		form.add(ddcSpecies);
//		//->Assembly
//		ddcVersion = new DropDownChoice<SelectOption>("version",new Model(),
//				new LoadableDetachableModel<List<SelectOption>>() {
//			@Override
//			protected List<SelectOption> load() {
//				if(-1==speciesId){
//					return new ArrayList<SelectOption>();
//				}
//				else {
//					List<SelectOption> allAssemblies = Arrays.asList(GenrepWrapper.getNRAssembliesByOrganismIdSO(speciesId));
//					List<SelectOption> addedAssemblies = new ArrayList<SelectOption>();
//					SequenceControl gC = new SequenceControl((UserSession)getSession());
//					for (SelectOption so : allAssemblies){
//						if(gC.isCreatedOnJBrowsoR(so.getKey(),so.getValue())){
//							addedAssemblies.add(so);
//						}
//					}
//					return addedAssemblies;
//				}
//			}
//		},
//		choiceRenderer){
//			protected boolean wantOnSelectionChangedNotifications() {
//				return true;
//			}
//			protected void onSelectionChanged(final SelectOption newSelection){
//				Application.debug("newselection");
//				assemblyId = newSelection.getKey();
//				assemblyName = newSelection.getValue();
//				//sources.setModelObject(getDASSources(assemblyName,new AjaxRequestTarget(getPage())));
//				sources.modelChanged();
//			}
//		};
//
//		ddcVersion.add(new AjaxEventBehavior("onclick"){
//			@Override
//			protected void onEvent(AjaxRequestTarget target) {
//				Application.debug("onclick");
//				image.setVisible(true);		
//				target.addComponent(image);
//			}
//
//		});
//		form.add(ddcVersion);
//
//		//->loading image
//		image = new Image("das_loader","/blue-loader.gif");
//		image.setOutputMarkupPlaceholderTag(true);
//		image.setVisible(false);
//		form.add(image);
//
//		//->DAS list
//		IModel dasModel =  new LoadableDetachableModel()
//		{
//			protected Object load() {
//				return getDASSources(assemblyName,new AjaxRequestTarget(getPage()));
//			}
//		};
//		dasContainer = new WebMarkupContainer("das_container");
//		sources = new ListView<DASWrapper>("das_sources",dasModel){
//			@Override
//			protected void populateItem(ListItem<DASWrapper> item) {
//				DASWrapper das = item.getModelObject();
//				item.add(new DataView<String>("types",new DataDASProvider(das.getMapmaster())){
//					@Override
//					protected void populateItem(final Item<String> item) {
//						AjaxLink link = new AjaxLink("type_link"){
//							@Override
//							public void onClick(AjaxRequestTarget target) {
//								Application.debug("click on : "+item.getModelObject());
//								form.info("fetching data for "+item.getModelObject());
//								List<Chromosome> chrList = GenrepWrapper.getChromosomesByNRassemblyId(assemblyId);
//								//GET THE DAS SOURCE
//								DAS.getAnnotations((UserSession)getSession(),"http://genome.ucsc.edu/cgi-bin/das/"+assemblyName,chrList,item.getModelObject(),assemblyId,form);
//
//							}
//						};
//						link.add(new Label("type_label",item.getModelObject()));
//						item.add(link);
//					}
//
//				});
//
//			}
//
//		};
//		sources.setOutputMarkupId(true);
//		dasContainer.add(sources);
//		dasContainer.setOutputMarkupPlaceholderTag(true);
//		form.add(dasContainer);
//		add(form);
//
//
//
//
//
//
//
//
//
//
//
//
//		//		DAS das = new DAS();
//		//		das.getSources_UCSC("http://genome.ucsc.edu/cgi-bin/das/dsn", "mm9");
//		//		//onclick=> load detachablmodel
//		//		das.getTypes_UCSC("http://genome.ucsc.edu/cgi-bin/das/mm9");
//		//
//		//		List<String> chrList = ChromosomeAccess.getChromosomesListByAssemblyId("7");
//		//		for(String str : chrList){
//		//			Application.debug("--> "+str);
//		//		}
//		//		String type="refGene";
//		//		das.getAnnotations("http://genome.ucsc.edu/cgi-bin/das/mm9",chrList,type);
//	}
//
//
//	private List<DASWrapper> getDASSources(String assemblyName, AjaxRequestTarget target) {
//		if(null==assemblyName){
//			return new ArrayList<DASWrapper>();
//		} else {
//			Application.debug("else =>  get das sources for : "+assemblyName);
//			DAS das = new DAS();
//			List<DASWrapper> list = das.getSources_UCSC("http://genome.ucsc.edu/cgi-bin/das/dsn",assemblyName);
//			image.setVisible(false);
//			target.addComponent(image);
//			return list;
//		}
//	}
//
//}
