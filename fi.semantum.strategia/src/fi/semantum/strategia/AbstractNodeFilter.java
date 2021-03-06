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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fi.semantum.strategia.FilterState.ReportCell;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;

abstract public class AbstractNodeFilter implements NodeFilter {

	protected Main main;
	
	AbstractNodeFilter(Main main) {
		this.main = main;
	}
	
	protected VisuSpec defaultSpec(List<Base> path, boolean condition) {
		return VisuSpec.getDefault(main, path, condition);
	}
	
	@Override
	public Collection<Base> traverse(List<Base> path, FilterState filterState) {
		return Collections.emptyList();
	}
	
	public boolean filter(List<Base> path) {
		return true;
	}
	
	@Override
	public void accept(List<Base> path, FilterState filterState) {

		if(!filter(path)) return;
		
		VisuSpec spec = acceptNode(path, filterState);
		if(spec != null) {
			filterState.accept(this, path);
		}
		
	}
	
	@Override
	public void report(List<List<Base>> path, FilterState state) {
		
	}
	
	@Override
	public void refresh() {
		
	}
	
	@Override
	public void reset(FilterState state) {
		refresh();
	}
	
	private ReportCell reportCell(Database database, Base base) {
		return new ReportCell(base.getId(database), base.getCaption(database));
	}

	protected ReportCell firstReportCell(Database database, List<Base> bases) {
		if(!bases.isEmpty()) {
			return reportCell(database, bases.get(0));
		} else {
			return ReportCell.EMPTY;
		}
	}
	
}
