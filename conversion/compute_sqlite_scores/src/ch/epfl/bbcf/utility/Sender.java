package ch.epfl.bbcf.utility;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import ch.epfl.bbcf.conversion.daemon.ManagerService;


public class Sender {

private static final String ADMIN_ADRESS = "yohan.jarosz@epfl.ch";
public static final Logger logger = ManagerService.logger;

	public static MimeMessage setSubject(MimeMessage msg,String subject){
		String str = "[GDV]"+subject;
		try {
			msg.setSubject(str);
		} catch (MessagingException e) {
			logger.error(e);
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
			msg.setFrom(new InternetAddress("yohan.jarosz@epfl.ch"));
			msg.setRecipients(Message.RecipientType.TO,mail);	
			return msg;
		} catch (MessagingException e) {
			logger.error(e);
			return null;
		}
	}




	private static boolean sendMessage(Session mailSession, MimeMessage msg, String mail, boolean toAdmin) {
		logger.debug("sending message to "+mail);
		
		Transport tr;
		try {
			tr = mailSession.getTransport("smtps");
			tr.connect("mail.epfl.ch", "jarosz", "7%CSTlmrn");
			Address[] addresses = null;
			if(toAdmin){
				addresses = new Address[2];
				addresses[1]=new InternetAddress(ADMIN_ADRESS);
			}
			else {
				addresses = new Address[1];
			}
			Address adress = new InternetAddress(mail);
			addresses[0] = adress;
			msg.saveChanges();
			tr.sendMessage(msg, addresses);
			logger.debug("message sent");
			return true;
		} catch (NoSuchProviderException e) {
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		}
		return false;
	}

	private static Session setSessionProperties() {
		// Get system properties
		Properties props = System.getProperties();
		// Setup mail server
		props.put("mail.smtp.host", "smtp.mail.yahoo.fr");
		props.put("mail.smtp.ssl.enable", true);
		props.put("mail.smtp.auth", true);
		// Get session
		Session mailSession = Session.getDefaultInstance(props, null);
		return mailSession;
	}
	


	public static void sendMessage(String subject,String message, boolean toAdmin,String mail) {
		Session mailSession = setSessionProperties();
		MimeMessage msg = prepareMail(mailSession,mail,message);
		msg = setSubject(msg,subject);
		sendMessage(mailSession,msg,mail,toAdmin);
	}
}
