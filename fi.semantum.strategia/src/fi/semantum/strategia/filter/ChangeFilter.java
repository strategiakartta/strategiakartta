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
package fi.semantum.strategia.filter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class ChangeFilter extends AbstractNodeFilter {

	private HashMap<Base,Date> bases;
	private Date now;
	
	public ChangeFilter(Main main) {
		super(main);
		now = new Date();
	}

	@Override
	public Collection<Base> traverse(List<Base> path, FilterState filterState) {
		
		final Database database = main.getDatabase();
		Base last = path.get(path.size()-1);
		if(last instanceof Strategiakartta) {
			ArrayList<Base> result = new ArrayList<Base>();
			Strategiakartta map = (Strategiakartta)last;
			for(Tavoite t : map.tavoitteet) {
				result.add(t);
			}
			for (Linkki l : map.alikartat) {
				Strategiakartta child = database.find(l.uuid);
				result.add(child);
			}
			return result;
		} else if(last instanceof Tavoite) {
			ArrayList<Base> result = new ArrayList<Base>();
			Tavoite goal = (Tavoite)last;
			for(Painopiste p : goal.painopisteet) {
				result.add(p);
			}
			return result;
		}
		
		return super.traverse(path, filterState);
		
	}
	
	@Override
	public boolean filter(List<Base> path) {
		return FilterUtils.contains(path, main.getUIState().currentPosition);
	}
	
	@Override
	public void accept(List<Base> path, FilterState filterState) {
		
		if(filterState.countAccepted() > main.uiState.level * 3) return;
		
		if(!Account.canRead(main, path)) return;
		
		super.accept(path, filterState);
		
	}
	
	@Override
	public VisuSpec acceptNode(List<Base> path, FilterState state) {
		
		Base last = path.get(path.size()-1);
		
		if(bases.containsKey(last)) {
			Date d = bases.get(last);
			return new VisuSpec(main, path, "white", "white", Utils.describeDate(now, d));
		}
		else
			return null;
		
	}
	
	@Override
	public void refresh() {

		final Database database = main.getDatabase();
		TreeMap<Date, Base> changes = new TreeMap<Date, Base>();
		Property changed = Property.find(database, Property.CHANGED_ON);
		for(Base b : database.objects.values()) {
			String value = changed.getPropertyValue(b);
			if(value != null) {
				try {
					Date d = Utils.sdf.parse(value);
					changes.put(d, b);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		bases = new HashMap<Base,Date>();
		for(Map.Entry<Date,Base> b : changes.descendingMap().entrySet()) {
			bases.put(b.getValue(), b.getKey());
		}

	}
	
	@Override
	public String toString() {
		return "Viimeisimmät muutokset";
	}

}
