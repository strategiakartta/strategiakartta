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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

public class PhantomJSDriver {

	public static String printHtml(String svgText) {

		try {
			File f = new File("WebContent/print.html");
			String template = new String(Files.readAllBytes(f.toPath()));
			template = template.replace("%%svg", svgText);
			return template;
		} catch (IOException e) {
			return null;
		}
		
	}

	public static String printCommand(String url, String outFile) {

		try {
			File f = new File("WebContent/print.js");
			String template = new String(Files.readAllBytes(f.toPath()));
			template = template.replace("%%url", url);
			template = template.replace("%%file", outFile.replace("\\", "/"));
			return template;
		} catch (IOException e) {
			return null;
		}
		
	}
	
	public static boolean execute(File javascript) {
		
		try {
		
	        File filePath = new File("WebContent/phantomjs.exe");
			
			String[] args = { filePath.getAbsolutePath(),
					javascript.getAbsolutePath()					
			};
			Process process = new ProcessBuilder(args).start();
			try {
				InputStream input = process.getInputStream();
				InputStreamReader reader = new InputStreamReader(input);
				StringBuilder sb = new StringBuilder();
				while (true) {
					try {
						while (reader.ready()) {
							int r = reader.read();
							if (r != -1) {
								char c = (char)r;
								if ((c != '\r') && (c != '\n')) {
									sb.append(c);
								} else if (c == '\n') {
									sb.setLength(0);
								}
							} else {
							}
						}
						
						int error = process.exitValue();
						
						sb.append(IOUtils.toString(input));
						
						reader.close();
						return error == 0;
						
					} catch (IllegalThreadStateException e) {
						Thread.sleep(100);
					}
				}
			} finally {
				process.destroy();
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;

	}
	
}
