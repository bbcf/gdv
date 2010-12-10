package ch.epfl.bbcf.gdv.html;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebRequestCycle;

import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Track;
import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.UserSession;
import ch.epfl.bbcf.gdv.control.model.TrackControl;
import ch.epfl.bbcf.gdv.mail.Sender;

public class TrackStatus extends WebPage{


	public TrackStatus(PageParameters p){
		int trackId = p.getInt("0");
		String status = p.getString("1");
		Application.debug("track status update => id : "+trackId+"  status : "+status);
		if(status.startsWith("+")){
			status = status.substring(1);
			TrackControl.updatePercentage(trackId, Integer.parseInt(status));
		} else if (status.equalsIgnoreCase(TrackControl.STATUS_FINISHED)){
			TrackControl.updateTrack(trackId,TrackControl.STATUS_FINISHED);
		//	TrackControl.updateTrack(trackId,Integer.parseInt(status));
		} else if (status.equalsIgnoreCase("computing")){
			TrackControl.updateTrack(trackId,TrackControl.STATUS_PROCESSING);
		}else if(status.equalsIgnoreCase(TrackControl.STATUS_ERROR)){
			TrackControl.updateTrack(trackId,TrackControl.STATUS_ERROR);
		} else {
			Application.error("status not recognized ");
		}
		
		
		
		
//		TrackControl vtc = new TrackControl((UserSession)getSession());
//		Track t = vtc.getViewTrackById(trackId);
//		if(null!=t){
//			String message = "";
//			boolean toAdmin = false;
//			if(status == 3){
//				vtc.updateTrack(trackId, status);
//				ViewControl vc = new ViewControl((UserSession)getSession());
//				vc.addToView(trackId);
//				message += "Track "+t.getName()+" successfully created ";
//			} else if(status == 4){
//				message += "Track "+t.getName()+" with id "+t.getId()+" not created because the is some error on the server.\n" +
//				"We have been notified ";
//				toAdmin = true;
//				vtc.deleteTrack(trackId);
//			}
//			Application.debug("track status : "+t.getStatus());
//			if(t.getStatus()==-2){
//				Application.debug("send a mail");
//				Users user = vtc.getUserFromTrackId(trackId);
//				Application.debug("user "+user);
//				Application.debug("user mail: "+user.getMail());
//				Sender.sendMessage("track creation",message,toAdmin,user);
//			}
//		}


		//		if(-1!=trackId && -1!=status){
		//			ViewTrackControl control = new ViewTrackControl((UserSession)getSession());
		//			control.updateTrack(trackId,status);
		//			ViewControl vc = new ViewControl((UserSession)getSession());
		//			if(status==1){
		//				vc.addToView(trackId);
		//			}
		//		}






	}
}
