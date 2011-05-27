package ch.epfl.bbcf.gdv.utility;

public interface Observable {

	public void addObserver(Observer obs);
	public void updateObserver(boolean wellDone,String message);
	public void delObservateur();
}
