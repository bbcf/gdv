package ch.epfl.bbcf.conversion.sqltree;




public abstract class Node {

	protected static final int TAB_WIDTH = ScoreTree.TAB_WIDTH;
	protected float[] tab;
	protected int previousPosition;
	protected int imageNumber;
	protected int zoom;
	protected ScoreTree tree;
	protected boolean first;
	protected int previousStart;

	
	protected Node(ScoreTree tree,int tabWidth,int zoom){
		this.first = true;
		this.zoom = zoom;
		this.tree = tree;
		tab = new float[tabWidth];
		for(int i=0;i<tab.length;i++){
			tab[i]=0;
		}
		this.previousPosition = -1;
	}

	protected void reset(){
		//log.debug("reset");
		for(int i=0;i<tab.length;i++){
			tab[i]=0;
		}
	}
	protected abstract void fill(int position, float score,int imageNumber);
	protected abstract void updateParents(Node node);
	protected abstract void endWriting();
	/**
	 * @return the zoom
	 */
	protected int getZoom() {
		return zoom;
	}
	protected float[] getTab(){
		return tab;
	}

	protected String view(){
		return this.getClass().getName()+" zoom "+this.zoom;
	}
	protected void setImageNumber(int imageNumber) {
		this.imageNumber = imageNumber;

	}
	protected int getImageNumber(){
		return this.imageNumber;
	}
	
	protected int getStartPosition(int position){
		int index = this.getTabIndex(position);
		int result = position-(index*zoom);
		if(result<=0){
			return 0;
		}
		return result;
	}
	
	protected int getTabIndex(int position) {
		
//		logger.debug("POSITION : "+position);
//		int ind = (int)Math.ceil((double)(position/zoom)%TAB_WIDTH);
//		int toto = ind-1;
//		logger.debug("ind : "+toto);
//		if(ind==0){
//			ind = 100;
//		}
//		toto = ind-1;
//		logger.debug("ind : "+toto);
//		//log.debug(ind);
//		return ind-1;
//	}
		
		int ind = (int)Math.ceil((double)(position/zoom)%TAB_WIDTH);
		if(ind==0){
			ind=100;
		}
		int toto = ind-1;
		//logger.debug("POSITION : "+position+" index = "+toto);
		//logger.debug("ind : "+ind);
		//log.debug(ind);
		return ind-1;
	}
	
}
