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

import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Wiki {

	public static void edit(String pageName, String text) {

		try {

			CloseableHttpClient httpClient = makeClient();
			
			apiLogin(httpClient);
			
			String editToken = "";
			
			String action1 = "api.php?action=query&prop=info%7Crevisions&intoken=edit&rvprop=timestamp&titles="+pageName;
			{
				HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/" + action1);
				try {
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity respEntity = response.getEntity();

					if (respEntity != null) {
						// EntityUtils to get the response content
						String content =  EntityUtils.toString(respEntity);
						int start = content.indexOf("edittoken=");
						String prefix = content.substring(start + "edittoken=&quot;".length()); 
						int end = prefix.indexOf("+");
						editToken = prefix.substring(0, end);
					}
				} catch (ClientProtocolException e) {
					// writing exception to log
					e.printStackTrace();
				} catch (IOException e) {
					// writing exception to log
					e.printStackTrace();
				}
			}
					
					
			@SuppressWarnings("deprecation")
			String action = "api.php?action=edit&contentmodel=wikitext&title=" + pageName + "&text=" + URLEncoder.encode(text) + "&token=" + editToken + "%2B%5C";
			
			{
				HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/" + action);
				try {
					httpClient.execute(httpPost);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static String get(String pageName) {

		try {

			CloseableHttpClient httpClient = makeClient();
			
			apiLogin(httpClient);
			
			String action1 = "api.php?action=query&prop=revisions&rvlimit=1&rvprop=content&format=xml&titles="+pageName;
			{
				HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/" + action1);
				try {
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity respEntity = response.getEntity();
					if (respEntity != null) {
						String content =  EntityUtils.toString(respEntity);
						return content;
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	public static void apiLogin(CloseableHttpClient httpClient) {

		String token = "";

		{
			HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/api.php?action=login&lgname=Testing&lgpassword=test");
			try {
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity respEntity = response.getEntity();

				if (respEntity != null) {
					// EntityUtils to get the response content
					String content =  EntityUtils.toString(respEntity);
					int start = content.indexOf("token=");
					String prefix = content.substring(start + "token=&quot;".length()); 
					int end = prefix.indexOf("&quot;");
					token = prefix.substring(0, end);
				}
			} catch (ClientProtocolException e) {
				// writing exception to log
				e.printStackTrace();
			} catch (IOException e) {
				// writing exception to log
				e.printStackTrace();
			}
		}

		{
			HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/api.php?action=login&lgname=Testing&lgpassword=test&lgtoken=" + token );
			try {
				httpClient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static CloseableHttpClient makeClient() throws NoSuchAlgorithmException {

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContext.getDefault();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext,
				new String[] { "TLSv1" },
				null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		return HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.build();

	}
	
	public static void login(Main main) {

		try {
			
			CloseableHttpClient httpClient = makeClient();

			String token = "";

			{
				HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/api.php?action=login&lgname=Testing&lgpassword=test");
				try {
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity respEntity = response.getEntity();

					if (respEntity != null) {
						// EntityUtils to get the response content
						String content =  EntityUtils.toString(respEntity);
						int start = content.indexOf("token=");
						String prefix = content.substring(start + "token=&quot;".length()); 
						int end = prefix.indexOf("&quot;");
						token = prefix.substring(0, end);
						Header[] headers = response.getAllHeaders();
						for (Header header : headers) {
							if("Set-Cookie".equals(header.getName())) {
								String value = header.getValue();
								String[] ss = value.split(";");
								if(ss.length > 0) {
									String[] parts = ss[0].split("=");
									if(parts.length == 2) {
										// Create a new cookie
										Cookie myCookie = new Cookie(parts[0], parts[1]);

										// Make cookie expire in 24 hours
										myCookie.setMaxAge(60*60*24);
										
										Date d = new Date();
										d.setTime(d.getTime() + 100000);
										
										String key = parts[0];
										String va = parts[1];
										
										main.getUI().getPage().getJavaScript().execute("document.cookie='"+key+"="+va+";path=/'");
										main.getUI().getPage().getJavaScript().execute("document.cookie='strategiakartta_wikiUserID=2;path=/'");
										main.getUI().getPage().getJavaScript().execute("document.cookie='strategiakartta_wikiUserName=Testing;path=/'");
										
									}
								}
							}
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			{
				HttpPost httpPost = new HttpPost("https://www.simupedia.com/strategiakartta/api.php?action=login&lgname=Testing&lgpassword=test&lgtoken=" + token );
				try {
					httpClient.execute(httpPost);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
