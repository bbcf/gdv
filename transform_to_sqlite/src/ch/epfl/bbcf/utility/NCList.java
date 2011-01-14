package ch.epfl.bbcf.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.daemon.Launcher;
import ch.epfl.bbcf.formats.parser.CustomGFFHandler.GFFFeat;

public class NCList {

	public static final Logger logger = Launcher.initLogger(NCList.class.getName());
	public static void sort( List<GFFFeat> sortables){
		Collections.sort(sortables);
	}

	public static List<GFFFeat> arrange(List<GFFFeat> list){
		List<GFFFeat> endList = new ArrayList<GFFFeat>();
		for(int i=0;i<list.size();i++){
			GFFFeat curFeat = list.get(i);
			i = includeNextFeat(list,i,curFeat,true);
			endList.add(curFeat);
		}
		return endList;
	}





	private static int includeNextFeat(List<GFFFeat> list, int i, GFFFeat curFeat,boolean first) {
		if(first){
			curFeat.initFeature();
		}
		if(i+1<list.size()){
			GFFFeat nextFeat = list.get(i+1);
			if(curFeat.getEnd()>nextFeat.getEnd()){
				i++;
				i = includeNextFeat(list,i,nextFeat,true);
				curFeat.nesting(nextFeat);
				i = includeNextFeat(list,i,curFeat,false);
			}
		}
		return i;
	}




}
