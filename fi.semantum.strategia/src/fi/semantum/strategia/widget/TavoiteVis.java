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

import java.util.ArrayList;
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Main.TimeInterval;
import fi.semantum.strategia.Utils;

public class TavoiteVis {

	public String uuid;
	public String id;
	public String parentText;
	public String text;
	public String color;
	public int realIndex;
	public boolean startNewRow = false;
	public double xOffset = 0.0; // In %
	public double yOffset = 0.0; // In pixels
	public boolean copy;
	public boolean stripes;
	public boolean drill = false;
	public PainopisteVis[] painopisteet = new PainopisteVis[0];
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];

	public TavoiteVis(Main main, Strategiakartta map, Tavoite t, int index) {
		
		final Database database = main.getDatabase();

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = t.hasRelatedTag(database, voimavarat);
		
		uuid = t.uuid;
		
		id = map.tavoiteDescription;
		TimeInterval ti = TimeInterval.parse(main.getUIState().time);
		boolean showTimes = ti.startYear != ti.endYear;
		
		if(showTimes) {
			Property aika = Property.find(main.getDatabase(), Property.AIKAVALI);
			String a = aika.getPropertyValue(t);
			id += " " + a;
		}
		
		text = t.text;
		color = map.tavoiteColor;
		realIndex = index;
		
		if(isVoimavarat) {
			id = "";
			text = "Voimavarat";
		}
		
		try {
			Strategiakartta k = t.getPossibleImplementationMap(main.getDatabase());
			drill = (k != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<PainopisteVis> pps = new ArrayList<PainopisteVis>();
		
		Property aika = Property.find(database, Property.AIKAVALI);
		
		if(main.getUIState().showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : main.getUIState().shownTags) {
				double value = TagVis.computeCoverage(main, monitorTag, t);
				if(value > 0) tagNames.add(new TagVis(monitorTag, value));
			}
			this.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
		for(int i=0;i<t.painopisteet.length;i++) {
			Painopiste p = t.painopisteet[i];
			String a = aika.getPropertyValue(p);
			if(a != null) {
				if(main.acceptTime(a))
					pps.add(new PainopisteVis(main, map, t, showTimes, realIndex, p, i));
			} else {
				pps.add(new PainopisteVis(main, map, t, showTimes, realIndex, p, i));
			}
		}

		painopisteet = pps.toArray(new PainopisteVis[pps.size()]);
		
    	Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
       	Pair p = implementsRelation.getPossibleRelation(t);
       	copy = p != null;
       	
       	String content = t.getText(database);
       	if(content.isEmpty()) content = t.getId(database);

       	text = content;
       	
       	stripes = map.linkWithParent && !copy;
       	
       	if(database.getRoot().equals(map))
       		stripes = false;
       	
       	if(main.getUIState().showMeters) {

           	boolean forecast = main.getUIState().forecastMeters;

       		if(main.getUIState().useImplementationMeters) {
       			
       			Meter m = t.getPrincipalMeter(main, "", forecast);
       			if(m.isPrincipal) {
           			meters = new MeterVis[2];
           			Meter imp = t.getImplementationMeter(main, "", forecast);
           			double value = imp.value(database, forecast);
           			meters[0] = new MeterVis("Arvio", m.getTrafficColor(database), realIndex, -1, 0, "");
           			meters[1] = new MeterVis("" + (int)(100.0*value) + "%", imp.getTrafficColor(database), realIndex, -1, 1, "");
       			} else {
           			meters = new MeterVis[1];
           			double value = m.value(database, forecast);
           			String id = "" + (int)(100.0*value) + "%";
           			String color = m.getTrafficColor(database, forecast);
           			meters[0] = new MeterVis(id, color, realIndex, -1, 0, "");
       			}
       		} else {
       			List<MeterDescription> descs = Meter.makeMeterDescriptions(main, t, true);
        		meters = new MeterVis[descs.size()];
        		for(int i=0;i<descs.size();i++) {
        			MeterDescription desc = descs.get(i);
        			Meter m = desc.meter;
        			String color = m.getTrafficColor(database, forecast);
        			String id = desc.meter.getId(database);
        			if(id.isEmpty()) id = desc.numbering;
        			meters[i] = new MeterVis(id, color, realIndex, -1, i, "");
        		}
       		}
			
       	}
       	
		if(isVoimavarat) {
			startNewRow = true;
			xOffset = 0.5;
			yOffset = 10;
			color = "#11BB11";
			stripes = false;
		}
		
	}
	
}
