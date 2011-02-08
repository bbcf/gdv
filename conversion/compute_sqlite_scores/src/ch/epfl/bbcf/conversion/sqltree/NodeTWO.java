package ch.epfl.bbcf.conversion.sqltree;


public class NodeTWO extends Node{

	protected NodeTWO(ScoreTree tree,int tabWidth,int zoom) {
		super(tree,tabWidth,zoom);
	}

	
	public void fill(int position, float score,int currentImageNumber) {//the index passed in parameters do nothing
		//log.debug("node : "+this.view());
		int index = getTabIndex(position);
		if(first){
			this.imageNumber = currentImageNumber;
			this.first = false;
			this.previousPosition = position;
		}
		//log.debug("fill svg node2 : "+position +" index = "+index+" score = "+score+" cimgNb = "+currentImageNumber+" imgnb : "+imageNumber);
		if(currentImageNumber==imageNumber){
			tab[index]=score;
		} else {
			//log.debug("DRAW");
			tree.writeValues(this);
			this.reset();
			tab[index]=score;
		}
		imageNumber=currentImageNumber;
		this.previousPosition = position;
	}


	@Override
	protected void updateParents(Node position) {
		//no parents
	}
	


	@Override
	protected void endWriting() {
		tree.writeValues(this,true);
		
	}

}
