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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Indicator;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;

public class TulostavoiteFilter extends ImplementationFilter {

	public TulostavoiteFilter(Main main, Base target) {
		super(main, target);
	}
	
	@Override
	public Collection<Base> traverse(List<Base> path, FilterState filterState) {
		
		
		ArrayList<Base> result = new ArrayList<Base>();
		Base last = path.get(path.size()-1);
		
//		if(isTulostavoite(last)) {
//			return result; 
//		}
//		
//		if(isVirasto(last)) {
//			Strategiakartta map = (Strategiakartta)last;
//			for(Tavoite t : map.tavoitteet) {
//				if(!visited.contains(t))
//					result.add(t);
//			}
//		} else {
			result.addAll(super.traverse(path, filterState));
//		}
		
		return result;
		
	}
	
//	private boolean isTulostavoite(Base base) {
//		
//		ObjectType tp = ObjectType.find(database, ObjectType.TULOSTAVOITE);
//		String uuid = FilterUtils.getObjectTypeUUID(database, base);
//		return tp.uuid.equals(uuid);
//
//	}

//	private boolean isVirasto(Base base) {
//		
//		ObjectType tp = ObjectType.find(database, ObjectType.VIRASTO);
//		String uuid = FilterUtils.getObjectTypeUUID(database, base);
//		return tp.uuid.equals(uuid);
//
//	}

	@Override
	public void report(Collection<List<Base>> paths, FilterState state) {
		
		final Database database = main.getDatabase();

		super.report(paths, state);
		
		for(List<Base> path : paths) {

			Base last = path.get(path.size()-1);
			
//			if(!isTulostavoite(last)) continue;
			
			Strategiakartta map = database.getMap(last);

			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
			List<Base> painopisteet = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);
//			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);

			List<Base> maps = map.getMaps(database);
//			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);
			
			Property aika = Property.find(database, Property.AIKAVALI);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			r.put("Strateginen tavoite", firstReportCell(database, strategiset));
			r.put("Painopiste", firstReportCell(database, painopisteet));
//			r.put("Virasto", firstReportCell(database, virastot));
//			r.put("Tulostavoite", firstReportCell(database, tulostavoitteet));
			
			r.put("Indikaattori", ReportCell.EMPTY);
			r.put("Arvo", ReportCell.EMPTY);
			r.put("2016", ReportCell.EMPTY);
			r.put("2017", ReportCell.EMPTY);
			r.put("2018", ReportCell.EMPTY);
			r.put("2019", ReportCell.EMPTY);

			for(Meter m : last.getMeters(database)) {
				Indicator in = m.getPossibleIndicator(database);
				if(in != null) {
					String year = aika.getPropertyValue(m);
					if("2016".equals(year) || "2017".equals(year) || "2018".equals(year) || "2019".equals(year)) {
						r.put(year, new ReportCell(m.trafficValuationDescription()));
					}
					r.put("Arvo", new ReportCell("" + in.getValue()));
					r.put("Indikaattori", new ReportCell(in.getCaption(database)));
				}
			}
			
			state.report.add(r);
				
		}
		
	}
	
	@Override
	protected VisuSpec makeSpec(List<Base> path, FilterState state, Base base) {

		if(base instanceof Meter) {
			Meter m = (Meter)base;
			double value = m.value(database);
			String color = Utils.trafficColor(value);
			return new VisuSpec(main, path, "black", color, m.describe(database, main.getUIState().forecastMeters));
		}
		
		return defaultSpec(path, true);
		
	}
	
	@Override
	public void accept(List<Base> path, FilterState state) {
		
		Base last = path.get(path.size()-1);
//		if(!isTulostavoite(last)) return;
		super.accept(path, state);
		
	}
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		state.reportColumns.add("Strateginen tavoite");
		state.reportColumns.add("Painopiste");
		state.reportColumns.add("Virasto");
		state.reportColumns.add("Tulostavoite");
		state.reportColumns.add("Indikaattori");
		state.reportColumns.add("Arvo");
		state.reportColumns.add("2016");
		state.reportColumns.add("2017");
		state.reportColumns.add("2018");
		state.reportColumns.add("2019");
	}
	
	@Override
	public String toString() {
		return "Tulostavoitteet";
	}

}
