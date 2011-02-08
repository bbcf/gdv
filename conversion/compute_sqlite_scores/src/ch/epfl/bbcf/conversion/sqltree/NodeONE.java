package ch.epfl.bbcf.conversion.sqltree;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.ManagerService;


public class NodeONE extends Node{

	private static final Logger logger = ManagerService.logger;

	private NodeTWO parent2;
	private NodeFIVE parent5;
	private boolean last;
	private boolean isLeaf;



	public NodeONE(ScoreTree tree,int tabWidth,int zoom, NodeTWO node2, NodeFIVE node5,boolean last) {
		super(tree,tabWidth,zoom);
		this.parent2 = node2;
		this.parent5 = node5;
		this.last = last;
	}




	public void fill(int position, float score,int currentImageNumber) {
		if(first){
			this.imageNumber = currentImageNumber;
			this.first = false;
			this.previousPosition = position;
		}

		int index = getTabIndex(position);
		//logger.warn("fill "+position+"index : "+index+" score : "+score+" img = "+currentImageNumber+"  (zoom = "+zoom+")");
//		if(true){
//			log.debug("NODE "+this.zoom+" position : "+position+" index : "+index+" previousIndex : "
//					+" curr img nb :"+currentImageNumber+"   img nb : "+imageNumber
//					+"previous Start : "+previousStart);
//		}
		if(currentImageNumber==imageNumber){//fill the tab
			////log.debug("fill");
			tab[index]=score;
		} else {//draw the tab then reset it then fill it
			//log.debug("DRAW");
			//logger.warn("write____value");
			tree.writeValues(this);
			if(!last){
				//log.debug("update parents from : "+this.view());
				updateParents(this);
			}
			//log.debug("reset");
			this.reset();
			tab[index]=score;
			this.previousStart = index;
			this.previousPosition = position;
			imageNumber=currentImageNumber;
		}
		
		//log.debug("END FILL");
	}

	@Override
	protected void updateParents(Node node) {
		//logger.warn("upparents");
		//log.debug("up parents from  "+this.view());
		int position = previousPosition;
		int ind = getTabIndex(position);
		int startPosition = getStartPosition(position);
		//log.debug("index : "+ind);
		//logger.warn("upParents : index : "+ind+" tab.length : "+tab.length+" start position : "+startPosition+" position : "+position);
		float max2 = tab[ind];
		float max5 = tab[ind];
		for(int i=ind;i<tab.length;i++){
			ind++;
			startPosition+=zoom;
			max2 = Math.max(max2, tab[i]);
			max5 = Math.max(max5, tab[i]);


			if(i%2==1){//update parent 2
				parent2.fill(startPosition,max2,(int)Math.ceil((double)imageNumber/2));
				if(i<tab.length-1){
					max2=tab[i+1];
				}
			}
			if(i%5==4){//update parent 5
				parent5.fill(startPosition, max5,(int)Math.ceil((double)imageNumber/5));
				if(i<tab.length-1){
					max5=tab[i+1];
				}
			}


		}
		//logger.debug("end up parents");
	}


	public NodeTWO getParentTWO(){
		return parent2;
	}

	public NodeFIVE getParentFIVE(){
		return parent5;
	}

	




	public void setLeaf(boolean b) {
		this.isLeaf = b;

	}




	@Override
	protected void endWriting() {
		tree.writeValues(this,true);
		if(!this.last){
			this.updateParents(this);
			this.parent2.endWriting();
			this.parent5.endWriting();
		}
	}
















}
