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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.vaadin.server.ExternalResource;

import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class Wiki {
	
	private static String wikiAddress = null;
	
	public static String wikiAddress() {
		if(wikiAddress == null) {
			wikiAddress = System.getenv("strategia_wiki_address"); 
			if(wikiAddress == null) {
				wikiAddress = "https://www.digitulosohjaus.fi/strategiakartta";
			}
		}
		return wikiAddress;
	}
	

	public static void edit(String pageName, String text) {

		try {

			CloseableHttpClient httpClient = makeClient();
			
			apiLogin(httpClient);
			
			String editToken = "";
			
			String action1 = "api.php?action=query&prop=info%7Crevisions&intoken=edit&rvprop=timestamp&titles="+pageName;
			{
				HttpPost httpPost = new HttpPost(wikiAddress() + "/" + action1);
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
				HttpPost httpPost = new HttpPost(wikiAddress() + "/" + action);
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
				HttpPost httpPost = new HttpPost(wikiAddress() + "/" + action1);
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

	public static void apiLogin(CloseableHttpClient client) throws ClientProtocolException, IOException {

		HttpGet httpPost = new HttpGet(wikiAddress() + "/api.php?action=query&meta=tokens&type=login&format=json");

		HttpResponse response = client.execute(httpPost);
		HttpEntity respEntity = response.getEntity();

		if (respEntity != null) {
			// EntityUtils to get the response content
			String content =  EntityUtils.toString(respEntity);

			int start = content.indexOf("logintoken\":\"");
			String prefix = content.substring(start + "logintoken\":\"".length()); 
			int end = prefix.indexOf("\"");
			String token = URLEncoder.encode(prefix.substring(0, end));

			HttpPost httpPost2 = new HttpPost(wikiAddress() + "/api.php");
			StringEntity xmlEntity = new StringEntity("?action=login&lgname=Testing&format=json&lgpassword=test&lgtoken=" + token);
			httpPost2.setEntity(xmlEntity );

			client.execute(httpPost2);
			
		}

	}
	
	public static CloseableHttpClient makeClient() throws NoSuchAlgorithmException {

//		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
//		CookieStore cookieStore = new BasicCookieStore();
//		HttpClientContext context = HttpClientContext.create();
//		context.setCookieStore(cookieStore);
//
//		 return HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
		 
		return HttpClients.createDefault();

	}
	
	public static void login(Main main) {

		try {

			CookieStore cookieStore = new BasicCookieStore();
			HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
			
			CloseableHttpClient client = makeClient();

			{

				HttpGet httpPost = new HttpGet(wikiAddress() + "/api.php?action=query&meta=tokens&type=login&format=json");

				HttpResponse response = client.execute(httpPost, httpContext);
				HttpEntity respEntity = response.getEntity();
				
				if (respEntity != null) {
					// EntityUtils to get the response content
					String content =  EntityUtils.toString(respEntity);

					int start = content.indexOf("logintoken\":\"");
					String prefix = content.substring(start + "logintoken\":\"".length()); 
					int end = prefix.indexOf("\"");
					String token = prefix.substring(0, end).replace("\\\\", "\\");
					String token2 = URLEncoder.encode(token);

					HttpPost httpPost2 = new HttpPost(wikiAddress() + "/api.php?action=login");
					httpPost2.addHeader("Content-Type", "application/x-www-form-urlencoded");
					StringEntity xmlEntity = new StringEntity("lgname=Testing&lgpassword=test&lgtoken=" + token2);
					httpPost2.setEntity(xmlEntity );

					HttpResponse response2 = client.execute(httpPost2, httpContext);
					HttpEntity respEntity2 = response2.getEntity();
					
					if (respEntity2 != null) {
						// EntityUtils to get the response content
						String content2 =  EntityUtils.toString(respEntity2);

						Header[] headers = response2.getAllHeaders();
						for (Header header : headers) {
							if("Set-Cookie".equals(header.getName())) {

								String value = header.getValue();
								String[] ss = value.split(";");
								if(ss.length > 0) {
									String[] parts = ss[0].split("=");
									if(parts.length == 2) {
//										// Create a new cookie
//										Cookie myCookie = new Cookie(parts[0], parts[1]);
//
//										// Make cookie expire in 24 hours
//										myCookie.setMaxAge(60*60*24);

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
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static String formatPageName(String pageName) {
		String formatted = pageName.replaceAll(" ", "_").replaceAll("\\.", "_"); 
		return formatted;
	}
	
	public static String makeWikiPageName(Database database, Base base) {

		String pageName = Main.getWikiPrefix(database);
		
		if(base instanceof Strategiakartta) {
			
			pageName += base.getId(database);
			return formatPageName(pageName);
			
		} else if (base instanceof Tavoite) {

			pageName += base.getId(database);
			return formatPageName(pageName);
			
		} else if (base instanceof Painopiste) {
			
			Painopiste p = (Painopiste)base;

			pageName += base.getId(database);
			return formatPageName(pageName);

		} else {

			Base owner = base.getOwner(database);
			if(owner != null) {
				pageName = makeWikiPageName(database, owner);
				pageName += "_" + base.getText(database);
				return formatPageName(pageName);
			}
			
		}

		return null;
		
	}

	
	public static void openWiki(Main main, Base base) {
		
		final Database database = main.getDatabase();

		String pageName = makeWikiPageName(database, base);
		if(pageName == null) return;

		main.wiki.setSource(new ExternalResource(wikiAddress() + "/index.php/"+ pageName));
		main.wikiPage = pageName;
		main.wikiBase = base;

		UIState s = main.uiState.duplicate(main);
		main.setTabState(s, 2);
		main.setFragment(s, true);

	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		
		HttpClient client = HttpClients.createDefault();
		new URL("https://www.digitulosohjaus.fi").openConnection().connect();
		//HttpGet httpPost = new HttpGet(wikiAddress() + "/api.php?action=login&lgname=Testing&lgpassword=test");
		HttpGet httpPost = new HttpGet(wikiAddress() + "/api.php?action=query&meta=tokens&type=login&format=json");
		
		
		HttpResponse response = client.execute(httpPost);
		HttpEntity respEntity = response.getEntity();

		if (respEntity != null) {
			// EntityUtils to get the response content
			String content =  EntityUtils.toString(respEntity);
			
			int start = content.indexOf("logintoken\":\"");
			String prefix = content.substring(start + "logintoken\":\"".length()); 
			int end = prefix.indexOf("\"");
			String token = URLEncoder.encode(prefix.substring(0, end));

			HttpPost httpPost2 = new HttpPost(wikiAddress() + "/api.php");
			StringEntity xmlEntity = new StringEntity("?action=login&lgname=Testing&format=json&lgpassword=test&lgtoken=" + token);
			httpPost2.setEntity(xmlEntity );
			
			HttpResponse response2 = client.execute(httpPost2);
			HttpEntity respEntity2 = response2.getEntity();

			if (respEntity2 != null) {
				// EntityUtils to get the response content
				String content2 =  EntityUtils.toString(respEntity2);
				
				Header[] headers = response.getAllHeaders();
				for (Header header : headers) {
					if("Set-Cookie".equals(header.getName())) {
						String value = header.getValue();
						String[] ss = value.split(";");
						if(ss.length > 0) {
							String[] parts = ss[0].split("=");
							if(parts.length == 2) {
//								// Create a new cookie
//								Cookie myCookie = new Cookie(parts[0], parts[1]);
//
//								// Make cookie expire in 24 hours
//								myCookie.setMaxAge(60*60*24);
								
								Date d = new Date();
								d.setTime(d.getTime() + 100000);
								
								String key = parts[0];
								String va = parts[1];
								
//								main.getUI().getPage().getJavaScript().execute("document.cookie='"+key+"="+va+";path=/'");
//								main.getUI().getPage().getJavaScript().execute("document.cookie='strategiakartta_wikiUserID=2;path=/'");
//								main.getUI().getPage().getJavaScript().execute("document.cookie='strategiakartta_wikiUserName=Testing;path=/'");
								
							}
						}
					}
				}
				
			}
			
		}
		
	}
	
}
