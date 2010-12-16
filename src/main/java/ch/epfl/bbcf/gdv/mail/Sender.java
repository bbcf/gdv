package ch.epfl.bbcf.gdv.mail;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//import ch.epfl.bbcf.gdv.access.gdv_prod.pojo.TMPUsers;
import ch.epfl.bbcf.gdv.access.database.pojo.Users;
import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.bbcf.gdv.config.Configuration;

public class Sender {


	public static MimeMessage setSubject(MimeMessage msg,String subject){
		String str = "[GDV]"+subject;
		try {
			msg.setSubject(str);
		} catch (MessagingException e) {
			Application.error(e);
		}
		return msg;
	}


	public static MimeMessage prepareMail(Session mailSession,String mail,String message){
		try {
			MimeMultipart content = new MimeMultipart();
			MimeBodyPart html = new MimeBodyPart();
			html.setHeader("MIME-Version", "1.0");
			html.setHeader("Content-Type", " multipart/mixed");
			html.setContent(message, "text/html");
			html.setDescription("Mail from GDV");
			content.addBodyPart(html);
			MimeMessage msg = new MimeMessage(mailSession);
			msg.setContent(content);
			msg.setHeader("MIME-Version", "1.0");
			msg.setHeader("Content-Type", content.getContentType());
			msg.setSentDate(new Date());
			msg.setFrom(new InternetAddress(Configuration.getConf().getMailAdress()));
			msg.setRecipients(Message.RecipientType.TO,mail);	
			return msg;
		} catch (MessagingException e) {
			e.printStackTrace();
			Application.error("message not sent ");
			return null;
		}
	}


	

	private static boolean sendMessage(Session mailSession, MimeMessage msg, String mail, boolean toAdmin) {
		Transport tr;
		try {
			tr = mailSession.getTransport(Configuration.getConf().getMailTransport());
			tr.connect(Configuration.getConf().getMailHost(),Configuration.getConf().getMailUser(), Configuration.getConf().getMailPasswd());
			Address[] addresses = null;
			if(toAdmin){
				addresses = new Address[2];
				addresses[1]=new InternetAddress(Configuration.getConf().getMailAdress());
			}
			else {
				addresses = new Address[1];
			}
			Address adress = new InternetAddress(mail);
			addresses[0] = adress;
			msg.saveChanges();
			tr.sendMessage(msg, addresses);
			return true;
		} catch (NoSuchProviderException e) {
			Application.error(e);
		} catch (MessagingException e) {
			//Application.error(e);
			System.out.println(e);
			for(StackTraceElement el :e.getStackTrace()){
				System.out.println(el.getClassName()+" "+el.getMethodName()+" "+el.getLineNumber());
			}
		}
		return false;
	}

	private static Session setSessionProperties() {
		// Get system properties
		Properties props = System.getProperties();
		// Setup mail server
		//props.put("mail.transport.protocol", Configuration.getConf().getMailTransport());
		props.put("mail.smtp.host", Configuration.getConf().getMailHost());
		props.put("mail.smtp.port", "465");
	//	props.put(Configuration.getConf().getMailHost(), Configuration.getConf().getMailTransport());
		//props.put("mail.smtp.ssl.enable", true);
		props.put("mail.smtp.auth", true);
		Authenticator authenticator = new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(Configuration.getConf().getMailAdress(), Configuration.getConf().getMailPasswd());
		    }
		};
		// Get session
		Session mailSession = Session.getDefaultInstance(props, authenticator);
		return mailSession;
	}
	public static boolean sendConfirmationMessage(String mail){
		Application.debug(Sender.class+" sending confirmation message");
		Session mailSession = setSessionProperties();
		MimeMessage msg = prepareMail(mailSession,mail,"confirm");
		//try {
			msg = setSubject(msg,"Comfirm subscribtion");
			//String text = "confirm your subscribtion by clicking this link (or copy/paste in your favorite browser) : \n"+
			//Configuration.SERV_URL+"/confirm/"+person.getHash();
			//msg.setText();
			return sendMessage(mailSession,msg,mail,false);
	}
	public static boolean sendFilesProcessed(List<File> files, Users user,boolean wellProcessed, String error2, long time) {
		Session mailSession = setSessionProperties();
		MimeMessage msg = prepareMail(mailSession,user.getMail(),"");
		String text=user.getId()+" - "+user.getMail()+" - "+new Date()+"\n";
		if(wellProcessed){
			msg = setSubject(msg,"Files processed "+time/1000+" s.");
		}
		else {
			msg = setSubject(msg,"File not processed");
			text+="ADMIN CONTACTED\n";

		}
		text += "your file(s) : ";
		for(File file:files){
			text+=file.getName()+"\n";
		}
		text+=error2;
		try {
			msg.setText(text);
		} catch (MessagingException e) {
			Application.error(e);
		}
		return sendMessage(mailSession,msg,user.getMail(),!wellProcessed);
	}


	public static void sendMessage(String subject,String message, boolean toAdmin,Users user) {
		Session mailSession = setSessionProperties();
		MimeMessage msg = prepareMail(mailSession,user.getMail(),message);
		msg = setSubject(msg,subject);
		sendMessage(mailSession,msg,user.getMail(),toAdmin);
	}
	public static void sendMessage(String subject, String message,
			boolean toAdmin, String usermail) {
		Session mailSession = setSessionProperties();
		MimeMessage msg = prepareMail(mailSession,usermail,message);
		msg = setSubject(msg,subject);
		sendMessage(mailSession,msg,usermail,toAdmin);
		
	}

	public static void main(String[]args){
		Configuration.init();
		sendMessage("test subject", "test message",false, "yohan.jarosz@epfl.ch");
	}
	
}
