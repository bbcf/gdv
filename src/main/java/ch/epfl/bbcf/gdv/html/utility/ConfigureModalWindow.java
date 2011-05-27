package ch.epfl.bbcf.gdv.html.utility;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;

import ch.epfl.bbcf.gdv.html.wrapper.TrackWrapper;

public class ConfigureModalWindow extends ModalWindow{

	
	private TrackWrapper track;
	
	public ConfigureModalWindow(String id) {
		super(id);
	}

	public void setTrack(TrackWrapper track) {
		this.track = track;
	}

	public TrackWrapper getTrack() {
		return track;
	}

}
