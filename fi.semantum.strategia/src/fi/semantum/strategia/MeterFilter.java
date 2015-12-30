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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.semantum.strategia.FilterState.ReportCell;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Strategiakartta;

public class MeterFilter extends ImplementationFilter {

	public MeterFilter(Main main, Base target, Strategiakartta scope) {
		super(main, target, scope);
	}
	
	@Override
	public Collection<Base> traverse(List<Base> path, FilterState filterState) {
		
		Base last = path.get(path.size()-1);

		ArrayList<Base> result = new ArrayList<Base>();
		result.addAll(last.getMetersActive(main));
		result.addAll(super.traverse(path, filterState));
		return result;
		
	}

	@Override
	public void report(List<List<Base>> paths, FilterState state) {
		
		final Database database = main.getDatabase();

		super.report(paths, state);
		
		for(List<Base> path : paths) {

			Base last = path.get(path.size()-1);

			Meter meter = (last instanceof Meter) ? (Meter)last : null;
			Base last2 = path.get(path.size()-2);
			
			Strategiakartta map = database.getMap(last2);

			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
			List<Base> painopisteet = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);
			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);
			List<Base> toimenpiteet = FilterUtils.filterByType(database, path, ObjectType.TOIMENPIDE);

			List<Base> maps = map.getMaps(database);

			List<Base> osastot = FilterUtils.filterByType(database, maps, ObjectType.LVM_OSASTO);
			osastot.addAll(FilterUtils.filterByType(database, maps, ObjectType.VIRASTO_OSASTO));
			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			r.put("Strateginen tavoite", firstReportCell(database, strategiset));
			r.put("Painopiste", firstReportCell(database, painopisteet));
			r.put("Virasto", firstReportCell(database, virastot));
			r.put("Tulostavoite", firstReportCell(database, tulostavoitteet));
			r.put("Toimenpide", firstReportCell(database, toimenpiteet));
			r.put("Osasto", firstReportCell(database, osastot));
			r.put("Selite", meter == null ? ReportCell.EMPTY : new ReportCell(meter.getCaption(database)));
			r.put("Arvo", meter == null ? ReportCell.EMPTY : new ReportCell(meter.explain(database)));
			state.report.add(r);
				
		}
		
	}
	
	@Override
	protected VisuSpec makeSpec(List<Base> path, FilterState state, Base base) {

		if(base instanceof Meter) {
			Meter m = (Meter)base;
			double value = m.value(database);
			String color = base.trafficColor(value);
			return new VisuSpec(main, path, "black", color, m.describe(database));
		} else {
			
			double value = 0;
			List<Meter> meters = base.getMetersActive(main);
			for(Meter m : meters) {
				value += m.value(database);
			}
			
			if(meters.isEmpty()) {
				String color = base.trafficColor(value);
				return new VisuSpec(main, path, "black", color);
			} else {
				value /= meters.size();
				String color = base.trafficColor(value);
				return new VisuSpec(main, path, "black", color);
			}
			
		}
		
	}
	
	@Override
	public void accept(List<Base> path, FilterState state) {
		
		Base last = path.get(path.size()-1);
		if((last instanceof Meter) || !state.isPrefix(path)) {
			super.accept(path, state);	
		}
		
	}
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		state.reportColumns.add("Strateginen tavoite");
		state.reportColumns.add("Painopiste");
		state.reportColumns.add("Virasto");
		state.reportColumns.add("Tulostavoite");
		state.reportColumns.add("Toimenpide");
		state.reportColumns.add("Osasto");
		state.reportColumns.add("Selite");
		state.reportColumns.add("Arvo");
	}
	
	@Override
	public String toString() {
		return "Valmiusasteet";
	}

}
