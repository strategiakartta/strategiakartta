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
	public PainopisteVis[] painopisteet = new PainopisteVis[0];
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];

	public TavoiteVis(Main main, Strategiakartta map, Tavoite t, int index) {
		
		final Database database = main.getDatabase();

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = t.hasRelatedTag(database, voimavarat);
		
		uuid = t.uuid;
		id = t.getId(database);
		text = t.text;
		color = map.tavoiteColor;
		realIndex = index;
		
		if(isVoimavarat) {
			id = "";
			text = "Voimavarat";
		}
		
		ArrayList<PainopisteVis> pps = new ArrayList<PainopisteVis>();
		
		Property aika = Property.find(database, Property.AIKAVALI);
		
		if(main.getUIState().showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : main.getUIState().shownTags) {
				double value = monitorTag.getCoverage(database, t);
				if(value > 0)
					tagNames.add(new TagVis(monitorTag, value));
				else {
					for(Base b : Utils.getImplementationSet(database, t)) {
						if(b.getRelatedTags(database).contains(monitorTag)) {
							tagNames.add(new TagVis(monitorTag, 1.0));
							continue;
						}
					}
				}
					
			}
			this.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
		for(int i=0;i<t.painopisteet.length;i++) {
			Painopiste p = t.painopisteet[i];
			String a = aika.getPropertyValue(p);
			if(a != null) {
				if(main.acceptTime(a))
					pps.add(new PainopisteVis(main, map, t, realIndex, p, i));
			} else {
				pps.add(new PainopisteVis(main, map, t, realIndex, p, i));
			}
		}

		painopisteet = pps.toArray(new PainopisteVis[pps.size()]);
		
    	Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
       	Pair p = implementsRelation.getPossibleRelation(t);
       	copy = p != null;
       	
       	if(p != null) {
       		Base owner = database.find(p.second);
           	if(owner instanceof Painopiste) {
           		Tavoite ownerT = database.getTavoite((Painopiste)owner);
           		id = ownerT.getId(database) + ": " + id;
           	}
       	}
       	
       	stripes = !copy;
       	
       	if(database.getRoot().equals(map))
       		stripes = false;
       	
       	if(main.getUIState().showMeters) {
			List<MeterDescription> descs = Meter.makeMeterDescriptions(main, t, true);
    		meters = new MeterVis[descs.size()];
    		for(int i=0;i<descs.size();i++) {
    			MeterDescription desc = descs.get(i);
    			Meter m = desc.meter;
    			String color = m.getTrafficColor(database);
    			String id = desc.meter.getId(database);
    			if(id.isEmpty()) id = desc.numbering;
    			meters[i] = new MeterVis(id, color, realIndex, -1, i);
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
