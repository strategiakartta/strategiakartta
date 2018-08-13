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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Strategiakartta;

public class TulostavoiteToimenpideFilter extends ImplementationFilter {

	public TulostavoiteToimenpideFilter(Main main, Base b) {
		super(main, b);
	}
	
	@Override
	public void accept(List<Base> path, FilterState state) {
		super.accept(path, state);
	}
	
	@Override
	public void report(Collection<List<Base>> paths, FilterState state) {

		final Database database = main.getDatabase();

		super.report(paths, state);

		Set<Base> internals = new HashSet<Base>();
		for(List<Base> path : paths) {
			for(int i=0;i<path.size()-1;i++)
				internals.add(path.get(i));
		}
		
		for(List<Base> path : paths) {

			Base last = path.get(path.size()-1);
			if(internals.contains(last)) continue;

			Strategiakartta map = database.getMap(last);

			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
			List<Base> painopisteet = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);

//			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);
//			List<Base> toimenpiteet = FilterUtils.filterByType(database, path, ObjectType.TOIMENPIDE);

			List<Base> maps = map.getMaps(database);

//			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);
//			List<Base> osastot = FilterUtils.filterByType(database, maps, ObjectType.LVM_OSASTO);
//			osastot.addAll(FilterUtils.filterByType(database, maps, ObjectType.VIRASTO_OSASTO));
//			List<Base> yksikot = FilterUtils.filterByType(database, maps, ObjectType.KOKOAVA_YKSIKKO);
//			yksikot.addAll(FilterUtils.filterByType(database, maps, ObjectType.TOTEUTTAVA_YKSIKKO));
//			List<Base> vastuuhenkilot = FilterUtils.filterByType(database, path, ObjectType.VASTUUHENKILO);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			r.put("Strateginen tavoite", firstReportCell(database, strategiset));
			r.put("Painopiste", firstReportCell(database, painopisteet));
//			r.put("Virasto", firstReportCell(database, virastot));
//			r.put("Tulostavoite", firstReportCell(database, tulostavoitteet));
//			r.put("Osasto", firstReportCell(database, osastot));
//			r.put("Yksikkö", firstReportCell(database, yksikot));
//			r.put("Toimenpide", firstReportCell(database, toimenpiteet));
//			r.put("Vastuuhenkilö", firstReportCell(database, vastuuhenkilot));
			state.report.add(r);
				
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
		state.reportColumns.add("Yksikkö");
		state.reportColumns.add("Vastuuhenkilö");
	}

	@Override
	public String toString() {
		return "Strategian toteutus";
	}
	
}
