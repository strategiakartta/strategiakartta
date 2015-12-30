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

import fi.semantum.strategia.Main;

public class MapVis {

	public String uuid;
	public int width;
	public String visio;
	public String id;
	public String text;
	public boolean showNavigation;

	public TavoiteVis[] tavoitteet = new TavoiteVis[0];
	public Linkki[] parents = new Linkki[0];
	public Linkki[] alikartat = new Linkki[0];
	
	public String[] meterData = new String[0];

	public String tavoiteDescription = "";
	public String painopisteDescription = "";
	public String characterColor = "";
	public String tavoiteColor = "";
	public String tavoiteTextColor = "";
	public String painopisteColor = "";

	public MapVis(Main main, Strategiakartta map, boolean showNavigation) {
		
		final Database database = main.getDatabase();

		this.showNavigation = showNavigation;
		uuid = map.uuid;
		id = map.getId(database);
		text = map.text;
		visio = map.visio;
		
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
			if(Account.canRead(main, child))
				alikartta.add(l);
			
		}
		
		alikartat = alikartta.toArray(new Linkki[alikartta.size()]);
		
		tavoiteDescription = map.tavoiteDescription;
		painopisteDescription = map.painopisteDescription;
		characterColor = map.characterColor;
		tavoiteColor = map.tavoiteColor;
		tavoiteTextColor = map.tavoiteTextColor;
		painopisteColor = map.painopisteColor;
		
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
