package ch.epfl.bbcf.conversion.sqltree;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.ManagerService;


public class NodeFIVE extends Node{

	private static final Logger log = ManagerService.logger;
	public NodeFIVE(ScoreTree tree,int tabWidth,int zoom, NodeONE upperLevel) {
		super(tree,tabWidth,zoom);
		this.parent1 = upperLevel;
	}

	private NodeONE parent1;




	public void fill(int position, float score,int currentImageNumber) {
		if(first){
			this.imageNumber = currentImageNumber;
			this.first = false;
			this.previousPosition = position;
		}
		int index = getTabIndex(position);		
//				log.debug("NODE "+this.zoom+" index : "+index+" previous : "+previousPosition+" from position " +
//						""+position+"   img nb = "+imageNumber);
		if(currentImageNumber==imageNumber){
			tab[index]=score;
		} else {
			//log.debug("DRAW");
			tree.writeValues(this);
			this.updateParents(this);
			this.reset();
			tab[index]=score;
			previousStart = index;
		}
		previousPosition=position;
		imageNumber=currentImageNumber;




	}

	@Override
	protected void updateParents(Node node) {
		//log.debug("up parent : "+node.previousPosition);
		int position = previousPosition;
		int startPosition = getStartPosition(position);
		float max1 = tab[0];
		for(int i=1;i<tab.length;i++){
			startPosition+=zoom;
			max1 = Math.max(max1, tab[i]);

			if(i%2==1){//update parent 1
				parent1.fill(startPosition, max1,(int)Math.ceil((double)imageNumber/2));
				//parent1.fill(previousPosition,max1,(int)Math.ceil(imageNumber/2));
				if(i<tab.length-1){
					max1=tab[i+1];
				}
			}

		}
	}

	

	public NodeONE getParentONE(){
		return parent1;
	}

	public int getPreviousPosition(){
		return previousPosition;
	}

	@Override
	protected void endWriting() {
		this.updateParents(this);
		tree.writeValues(this,true);
		this.parent1.endWriting();

	}
}
