package ch.epfl.bbcf.gdv.utility;

import java.util.ArrayList;
import java.util.List;

public class ObservableThread extends Thread implements Observable{

	protected List<Observer> observers;
	
	public ObservableThread(){
		this.observers = new ArrayList<Observer>();
	}
	
	public void addObserver(Observer obs) {
		this.observers.add(obs);
		
	}
	public void delObservateur() {
		this.observers = new ArrayList<Observer>();
		
	}
	public void updateObserver(boolean wellDone,String message) {
		for(Observer obs : this.observers){
			obs.notify(wellDone,message);
		}
		
	}

}
