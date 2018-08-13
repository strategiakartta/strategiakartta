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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.PainopisteVis;
import fi.semantum.strategia.widget.Strategiakartta;

public class GenericImplementationFilter extends ImplementationFilter {

	private List<String> typeColumns;
	
	public GenericImplementationFilter(Main main, Base b, List<String> typeColumns) {
		super(main, b);
		this.typeColumns = typeColumns;
	}
	
	@Override
	public void accept(List<Base> path, FilterState state) {
		super.accept(path, state);
	}
	
	@Override
	public List<Base> prune(List<Base> path) {
		List<Base> result = new ArrayList<Base>();
		for(int i=0;i<path.size();i++) {
			if(i == 0) result.add(path.get(i));
			else {
				Base previous = path.get(i-1);
				Base current = path.get(i);
				Base possibleCopy = current.getPossibleCopy(database);
				if(previous.equals(possibleCopy)) {
					result.remove(previous);
				}
				result.add(current);
			}
		}
		return result;
	}
	
	Map<String,Map<Base,ReportCell>> cellCache = new HashMap<String, Map<Base,ReportCell>>();
	
	private ReportCell computeCell(String currentType, Base b) {
		
		if(currentType.equals("$Mittari")) {
			if(b instanceof Painopiste) {
				Painopiste p = (Painopiste)b;
				Strategiakartta map = p.getMap(database);
				PainopisteVis pv = new PainopisteVis(main, map, p.getGoal(database), false, 0, p, 0);
				if(pv.leaf) return new ReportCell(pv.leafMeterPct);
			}
		} else if(currentType.equals("$Kartta")) {
			Strategiakartta map = b.getMap(database);
			return new ReportCell(map.getShortText(database), map.getText(database));
		} else if(currentType.equals(database.getType(b))) {
			return reportCell(database, b);
		}
		
		return null;
		
	}

	private ReportCell cacheCell(String currentType, Base b) {
		Map<Base,ReportCell> cs = cellCache.get(currentType);
		ReportCell cell = cs.get(b);
		if(cell == null) {
			cell = computeCell(currentType, b);
			cs.put(b, cell);
		}
		return cell;
	}
	
	@Override
	public void report(Collection<List<Base>> paths, FilterState state) {

		super.report(paths, state);

		Set<Base> internals = new HashSet<Base>();
		for(List<Base> path : paths) {
			for(int i=0;i<path.size()-1;i++)
				internals.add(path.get(i));
		}

		if(typeColumns.isEmpty()) return;
		
		for(List<Base> path : paths) {

			Base last = path.get(path.size()-1);
			if(internals.contains(last)) continue;
			
			int columnPosition = 0;
			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			for(int i=0;i<path.size();) {
				Base b = path.get(i);
				String currentType = typeColumns.get(columnPosition);
				ReportCell cell = cacheCell(currentType, b);
				if(cell != null) {
					r.put(currentType, cell);
					columnPosition++;
					if(columnPosition == typeColumns.size()) break;
					continue;
				}
				
//				if(currentType.equals("$Mittari")) {
//					if(b instanceof Painopiste) {
//						Painopiste p = (Painopiste)b;
//						Strategiakartta map = p.getMap(database);
//						PainopisteVis pv = new PainopisteVis(main, map, p.getGoal(database), false, 0, p, 0);
//						if(pv.leaf) {
//							r.put(currentType, new ReportCell(pv.leafMeterPct));
//							columnPosition++;
//							if(columnPosition == typeColumns.size()) break;
//							continue;
//						}
//					}
//				} else if(currentType.equals("$Kartta")) {
//					Strategiakartta map = b.getMap(database);
//					r.put(currentType, new ReportCell(map.getShortText(database), map.getText(database)));
//					columnPosition++;
//					if(columnPosition == typeColumns.size()) break;
//					continue;
//				} else if(currentType.equals(database.getType(b))) {
//					r.put(currentType, reportCell(database, b));
//					columnPosition++;
//					if(columnPosition == typeColumns.size()) break;
//					continue;
//				}
				
				// Nothing was found from here - move on
				i++;
				
			}
			
			if(r.size() ==  typeColumns.size())
				state.report.add(r);
				
		}
		
	}

	private Map<Base,Strategiakartta> maps = new HashMap<Base,Strategiakartta>();

	private Strategiakartta mapCache(Database database, Base b) {
		Strategiakartta map = maps.get(b);
		if(map == null) {
			map = b.getMap(database);
			maps.put(b, map);
		}
		return map;
	}

	@Override
	public boolean reject(List<Base> path) {
		
		// Level
		if(super.reject(path)) return true;
		
		for(Base b : main.getUIState().requiredItems) {
			if(b instanceof Strategiakartta) {
				Strategiakartta map = (Strategiakartta)b;
				boolean found = false;
				for(Base b2 : path) {
					if(map.equals(mapCache(database, b2))) {
						found = true;
						continue;
					}
				}
				if(!found) return true;
			} else {
				if(!path.contains(b))
					return true;
			}
		}
		
		return false;
		
	}
	
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		maps.clear();
		state.reportColumns.addAll(typeColumns);
		cellCache.clear();
		for(String col : typeColumns)
			cellCache.put(col, new HashMap<Base, ReportCell>());
	}

	@Override
	public String toString() {
		return "Toteutusraportti";
	}
	
}
