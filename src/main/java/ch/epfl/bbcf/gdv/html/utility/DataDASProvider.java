package ch.epfl.bbcf.gdv.html.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import ch.epfl.bbcf.gdv.formats.das.DAS;

public class DataDASProvider implements IDataProvider<String>{

	private DAS das;
	private List<String> types;//mapmaster:list of types

	public DataDASProvider(String mapmaster){
		das= new DAS();
		types = das.getTypes_UCSC(mapmaster);
	}

	public Iterator<? extends String> iterator(int first, int count) {
		return types.iterator();
	}

//	public IModel<String> model(final String object) {
//		return new LoadableDetachableModel<String>(){
//			@Override
//			protected String load() {
//				return object;
//			}
//		};
//	}

	public int size() {
		return types.size();
	}

	public void detach() {
		
	}

	@Override
	public IModel<String> model(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}