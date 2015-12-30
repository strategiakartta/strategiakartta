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

public class PainopisteVis {

	public String uuid;
	public String id;
	public String text;
	public String color;
	public int tavoite;
	public int realIndex;
	public PainopisteVis[] painopisteet = new PainopisteVis[0];
	
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];

	public PainopisteVis(Main main, Strategiakartta map, Tavoite t, int tavoiteIndex, Painopiste p, int realIndex) {
		
		final Database database = main.getDatabase();

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = t.hasRelatedTag(database, voimavarat);

		uuid = p.uuid;
		id = p.getId(database);
		text = p.text;
		color = map.painopisteColor;
		tavoite = tavoiteIndex;
		this.realIndex = realIndex;
		
		if(main.getUIState().showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : main.getUIState().shownTags) {
				double value = monitorTag.getCoverage(database, p);
				if(value > 0)
					tagNames.add(new TagVis(monitorTag, value));
			}
			this.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
       	if(main.getUIState().showMeters) {
       		
			List<MeterDescription> descs = Meter.makeMeterDescriptions(main, p, true);
    		meters = new MeterVis[descs.size()];
    		for(int i=0;i<descs.size();i++) {
    			MeterDescription desc = descs.get(i);
    			Meter m = desc.meter;
    			String color = m.getTrafficColor(database);
    			String id = desc.meter.getId(database);
    			if(id.isEmpty()) id = desc.numbering;
    			meters[i] = new MeterVis(id, color, tavoite, realIndex, i);
    		}
       		
       	}
       	
       	if(isVoimavarat)
       		color = "#44ee44";

	}
	
}
