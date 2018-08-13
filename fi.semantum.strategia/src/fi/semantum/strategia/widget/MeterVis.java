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
package fi.semantum.strategia.widget;

public class MeterVis {

	public String text;
	public String desc;
	public String color;
	public int tavoite;
	public int painopiste;
	public int realIndex;
	public String link;
	public String value = "";

	MeterVis(String text, String color, int tavoite, int painopiste, int realIndex, String link) {
		this(text, text, color, tavoite, painopiste, realIndex, link);
	}

	MeterVis(String text, String desc, String color, int tavoite, int painopiste, int realIndex, String link) {
		this.text = text;
		this.desc = desc;
		this.color = color;
		this.tavoite = tavoite;
		this.painopiste = painopiste;
		this.realIndex = realIndex;
		this.link = link;
	}
	
	public static MeterVis from(Database database, Meter m, boolean forecast, boolean allowLinks, int tavoite, int realIndex, int i) {
		String color = m.getTrafficColor(database, forecast);
		String id = m.getId(database);
		String desc = m.getVerboseDescription(database, forecast);
		if(id.isEmpty()) id = m.getDescription(database, forecast);
		if(id.isEmpty()) id = "" + (i+1);
		return new MeterVis(id, desc, color, tavoite, realIndex, i, allowLinks ? m.link : "");
	}

}
