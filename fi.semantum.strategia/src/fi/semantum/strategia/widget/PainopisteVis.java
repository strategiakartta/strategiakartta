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
import java.util.Map;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;

public class PainopisteVis {

	public String uuid;
	public String id;
	public String text;
	public String color;
	public int tavoite;
	public int realIndex;
	public boolean leaf;
	public boolean hasInfo;
	public String leafMeterColor = "#000";
	public String leafMeterDesc = "";
	public String leafMeterPct = "";
	public PainopisteVis[] painopisteet = new PainopisteVis[0];
	
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];

	public PainopisteVis(Main main, Strategiakartta map, Tavoite t, boolean showTimes, int tavoiteIndex, Painopiste p, int realIndex) {
		
		final Database database = main.getDatabase();

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = t.hasRelatedTag(database, voimavarat);

		Map<String,String> resp = Utils.getResponsibilityMap(database, p);
		hasInfo = !resp.isEmpty();
		
		leaf = main.isLeaf(p);
		
		uuid = p.uuid;
		
		id = map.painopisteDescription;

		if(showTimes) {
			Property aika = Property.find(database, Property.AIKAVALI);
			String a = aika.getPropertyValue(p);
			if(a != null && a.length() > 0)
				id = id + " " + a;
		}
		
		text = p.text;
		color = map.painopisteColor;
		tavoite = tavoiteIndex;
		this.realIndex = realIndex;
		
		if(main.getUIState().showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : main.getUIState().shownTags) {
				double value = TagVis.computeCoverage(main, monitorTag, p);
				if(value > 0) tagNames.add(new TagVis(monitorTag, value));
			}
			this.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
       	if(main.getUIState().showMeters) {

    		boolean meterForecast = main.getUIState().forecastMeters;

       		if(leaf) {

       			meters = new MeterVis[0];
       			MeterVis[] vis = computeImplementationMeters(main, t, p);
       			if(vis.length == 1) {
       				leafMeterColor = vis[0].color;
       				leafMeterDesc = vis[0].desc;
       				leafMeterPct = vis[0].text;
       			} else {
           			leafMeterColor = "#0f0";
       			}

       		} else if(main.getUIState().useImplementationMeters) {
       			
       			Meter pm = p.getPossiblePrincipalMeterActive(main);
       			if(pm != null) {
	    			String color = pm.getTrafficColor(database, meterForecast);
           			double value = pm.value(database, meterForecast);
           			String id = "Arvio: " + (int)(100.0*value) + "%";
       				meters = new MeterVis[] { new MeterVis(id, id, color, tavoite, realIndex, 0, "") };
       			} else {
           			meters = computeImplementationMeters(main, t, p);
       			}
	    		
       		} else {
       			
				List<MeterDescription> descs = Meter.makeMeterDescriptions(main, p, true);
	    		meters = new MeterVis[descs.size()];
	    		for(int i=0;i<descs.size();i++) {
	    			MeterDescription desc = descs.get(i);
	    			Meter m = desc.meter;
	    			String color = m.getTrafficColor(database, meterForecast);
	    			String id = desc.meter.getId(database);
	    			if(id.isEmpty()) {
	           			double value = m.value(database, meterForecast);
	           			id = "" + (int)(100.0*value) + "%";
	    			}
	    			meters[i] = new MeterVis(id, id, color, tavoite, realIndex, i, m.link);
	    		}
       		}
       		
       	}
       	
       	if(isVoimavarat)
       		color = "#44ee44";

	}
	
	private MeterVis[] computeImplementationMeters(Main main, Tavoite t, Painopiste p) {
		
		Database database = main.getDatabase();
		boolean forecast = main.getUIState().forecastMeters;

		boolean allowLinks = !t.hasImplementationSubmap(database);
		
		ArrayList<MeterVis> meterList = new ArrayList<MeterVis>();
		List<Meter> descs = p.getImplementationMeters(main, forecast);
		if(descs.isEmpty()) {
			Meter prin = p.getPrincipalMeter(main, "", forecast);
			meterList.add(MeterVis.from(database, prin, forecast, allowLinks, tavoite, realIndex, 0));
		} else {
			for(int i=0;i<descs.size();i++) {
				meterList.add(MeterVis.from(database, descs.get(i), forecast, allowLinks, tavoite, realIndex, i));
			}
		}

		return meterList.toArray(new MeterVis[meterList.size()]);
		
	}
	
}
