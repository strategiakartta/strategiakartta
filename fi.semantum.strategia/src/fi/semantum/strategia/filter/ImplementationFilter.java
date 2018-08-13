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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Relation;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class ImplementationFilter extends AbstractNodeFilter {

	protected Database database;
	//protected Strategiakartta scope;
	private Relation implementsRelation;
	protected Set<Base> visited = new HashSet<Base>();
	
	public ImplementationFilter(Main main, Base target) {
		super(main);
		this.database = main.getDatabase();
//		this.scope = scope;
		this.implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
//		Set<Base> tgts = new HashSet<Base>();
//		if(target instanceof Strategiakartta) {
//			Strategiakartta map = (Strategiakartta)target;
//			tgts = new HashSet<Base>();
//			for(Tavoite t : map.tavoitteet) {
//				tgts.add(t);
//			}
//			
//		} else {
//			tgts = Collections.singleton(target);
//		}
//		setTargets(tgts);
	}
	
	@Override
	public Collection<Base> traverse(List<Base> path, FilterState filterState) {
		Base last = path.get(path.size()-1);
		visited.add(last);
		if(last instanceof Strategiakartta) {
			ArrayList<Base> result = new ArrayList<Base>();
			Strategiakartta map = (Strategiakartta)last;
			for(Tavoite t : map.tavoitteet) {
				if(!visited.contains(t))
					result.add(t);
			}
			for (Linkki l : map.alikartat) {
				Strategiakartta child = database.find(l.uuid);
				result.add(child);
			}
			return result;
		} else {
			return database.getInverse(last, implementsRelation);
		}
	}
	
	
	public Base getLastMapObject(List<Base> path) {
		for(int i=0;i<path.size();i++) {
			Base b = path.get(path.size()-1-i);
			if(b instanceof Strategiakartta) return b;
			else if(b instanceof Tavoite) return b;
			else if(b instanceof Painopiste) return b;
		}
		return null;
	}
	
//	public boolean hasTargets(List<Base> path) {
//		for(Base target : getTargets()) {
//			for(int i=0;i<path.size();i++) {
//				if(path.get(i).equals(target))
//					continue;
//			}
//			return false;
//		}
//		return true;
//	}
	
	public List<Base> prune(List<Base> path) {
		return path;
	}
	
	@Override
	public void accept(List<Base> path_, FilterState state) {
		List<Base> path = prune(path_);
		//if(!FilterUtils.inScope(database, path, scope)) return;
		if(!Account.canRead(main, path)) return;
		//if(!hasTargets(path)) return;
		//if(path.size() - pos <= main.getUIState().level) {
			state.accept(this, path);
		//}
	}
	
	@Override
	public boolean reject(List<Base> path) {
		if(path.size() > main.getUIState().level) return true;
		return false;
	}
	
	@Override
	public VisuSpec acceptNode(List<Base> path, FilterState state) {
		
		Base last = path.get(path.size()-1);
		return makeSpec(path, state, last);
		
	}
	
	protected VisuSpec makeSpec(List<Base> path, FilterState state, Base base) {
		return defaultSpec(path, true);
	}
	
	@Override
	public String toString() {
		return "Toteutushierarkia";
	}
	
	@Override
	public void reset(FilterState state) {
		refresh();
		visited.clear();
	}

}
