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

public class MapVis {

	public String uuid;
	public int width;
	public String visio;
	public String id;
	public String elementId;
	public String text;
	public boolean showNavigation;
	public boolean showVision;
	public String meterStatus;
	public int scrollFocus;

	public PathVis[] path = new PathVis[0];
	public TavoiteVis[] tavoitteet = new TavoiteVis[0];
	public Linkki[] parents = new Linkki[0];
	public Linkki[] alikartat = new Linkki[0];
	
	public String[] meterData = new String[0];

	public String tavoiteDescription = "";
	public String painopisteDescription = "";
	public String tavoiteColor = "";
	public String painopisteColor = "";

	public String tavoiteTextColor = "";
	public String painopisteTextColor = "";

	public MapVis(Main main, Strategiakartta map, String elementId, boolean showNavigation) {
		
		final Database database = main.getDatabase();

		this.showNavigation = showNavigation;
		this.showVision = map.showVision;
		this.elementId = elementId;

		if(main.getUIState().showMeters) {
			if(main.getUIState().forecastMeters) {
				this.meterStatus = "Ennusteet";
			} else {
				this.meterStatus = "Toteumat";
			}
		} else {
			this.meterStatus = "";
		}
		
		uuid = map.uuid;
		id = map.getId(database);
		text = map.getText(database);
		if(text.isEmpty()) text = map.getId(database);
		if(text.isEmpty()) text = "<anna teksti tai lyhytnimi kartalle>";
		
		visio = map.visio;

		List<Base> pathBases = database.getDefaultPath(map);
		path = new PathVis[pathBases.size()];
		for(int i=0;i<pathBases.size();i++) {
			
			Strategiakartta b = (Strategiakartta)pathBases.get(i);
			b.prepare(main);
			
			String desc = b.getText(database);
			Base b2 = b.getImplemented(database);
			if(b2 instanceof Tavoite) {
				Strategiakartta p = database.getMap(b2);
				if(p != null) {
					p.prepare(main);
					desc = p.tavoiteDescription + ": " + desc;
				}
			}
			
			path[i] = new PathVis(b.uuid, desc, b.tavoiteTextColor, b.tavoiteColor);
			
		}
		
		text = path[path.length-1].text;
		
		path[path.length-1] = new PathVis("", text, "#000", "#eee");
		
		ArrayList<TavoiteVis> pps = new ArrayList<TavoiteVis>();
		
		Property aika = Property.find(database, Property.AIKAVALI);
		
		for(int i=0;i<map.tavoitteet.length;i++) {
			Tavoite p = map.tavoitteet[i];
			String a = aika.getPropertyValue(p);
			if(a != null) {
				if(main.acceptTime(a))
					pps.add(new TavoiteVis(main, map, p, i));
			} else {
				pps.add(new TavoiteVis(main, map, p, i));
			}
		}
		
		if(map.voimavarat != null && main.getUIState().showVoimavarat)
			pps.add(new TavoiteVis(main, map, map.voimavarat, map.tavoitteet.length));

		tavoitteet = pps.toArray(new TavoiteVis[pps.size()]);
		
		parents = map.parents;
		
		ArrayList<Linkki> alikartta = new ArrayList<Linkki>();
		for(Linkki l : map.alikartat) {
			Strategiakartta child = database.find(l.uuid);
			if(Account.canRead(main, child)) {
//				if(main.getUIState().input) {
//					alikartta.add(l);
//				} else {
					// If implemented, navigation is possible from goal
					Base imp = child.getImplemented(database);
					if(imp == null) {
						boolean found = false;
						// Otherwise it might be possible to navigate from meter
						if(main.getUIState().showMeters) {
							for(Tavoite t : child.tavoitteet) {
								Base imp2 = t.getPossibleImplemented(database);
								if(imp2 != null) {
									if(imp2.getMap(database).equals(map)) {
										found = true;
										break;
									}
								}
							}
						}
						if(!found)
							alikartta.add(l); 
//					}
					
				}
			}
			
		}
		
		alikartat = alikartta.toArray(new Linkki[alikartta.size()]);
		
		tavoiteDescription = map.tavoiteDescription;
		painopisteDescription = map.painopisteDescription;

		tavoiteColor = map.tavoiteColor;
		painopisteColor = map.painopisteColor;
		
		tavoiteTextColor = map.tavoiteTextColor;
		painopisteTextColor = map.painopisteTextColor;
		
		scrollFocus = 2;
		
	}
	
	private static final int painopisteColumns = 2;

	public int extra(TavoiteVis t) {
		return Math.max(0, (t.painopisteet.length-1) / painopisteColumns);
	}

	public void fixRows() {
		
		for(int i=0;i<tavoitteet.length;i++) {
			TavoiteVis t = tavoitteet[i];
			if((i&1) == 0) t.startNewRow = true;
		}
			
	}
	
}
