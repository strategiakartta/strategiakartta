/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
	
	public static void main(String[] args) throws Exception {
		String[] emails = {"stkartta@gmail.com"}; 
		send(emails, "Tiedote", "T‰rke‰‰ tietoa!");
	}

	private static String smtp_localhost = null;
	public static String smtpLocalhost() {
		if(smtp_localhost == null) {
			smtp_localhost = System.getenv("strategia_smtp_localhost"); 
			if(smtp_localhost == null) {
				smtp_localhost = "www.digitulosohjaus.fi";
			}
		}
		return smtp_localhost;
	}
	
	private static String smtp_host = null;
	public static String smtpHost() {
		if(smtp_host == null) {
			smtp_host = System.getenv("strategia_smtp_host"); 
			if(smtp_host == null) {
				smtp_host = "-";
			}
		}
		return smtp_host;
	}

	private static String smtp_from = null;
	public static String smtpFrom() {
		if(smtp_from == null) {
			smtp_from = System.getenv("strategia_smtp_from"); 
			if(smtp_from == null) {
				smtp_from = "strategia@digitulosohjaus.fi";
			}
		}
		return smtp_from;
	}

	public static void send(String[] emails, String subject, String body) throws MessagingException {
	
		  Properties mailProps = new Properties();
		  
		  mailProps.put("mail.smtp.localhost", smtpLocalhost());
	      mailProps.put("mail.smtp.from", smtpFrom());
	      mailProps.put("mail.smtp.host", smtpHost());
	      mailProps.put("mail.smtp.port", 25);
	      mailProps.put("mail.smtp.auth", false);
	      
	      Session mailSession = Session.getDefaultInstance(mailProps);

	      MimeMessage message = new MimeMessage(mailSession);
	      message.setFrom(new InternetAddress("strategiakartta@simupedia.com"));
	      InternetAddress dests[] = new InternetAddress[emails.length];
	      for (int i = 0; i < emails.length; i++) {
	          dests[i] = new InternetAddress(emails[i].trim().toLowerCase());
	      }
	      message.setRecipients(Message.RecipientType.TO, dests);
	      message.setSubject(subject, "UTF-8");
	      Multipart mp = new MimeMultipart();
	      MimeBodyPart mbp = new MimeBodyPart();
	      mbp.setContent(body, "text/html;charset=utf-8");
	      mp.addBodyPart(mbp);
	      message.setContent(mp);
	      message.setSentDate(new java.util.Date());

	      Transport.send(message);
		
	}

}
