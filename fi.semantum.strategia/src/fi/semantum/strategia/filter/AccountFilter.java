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
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class AccountFilter extends AbstractNodeFilter {

	private final Account account;
	private final Property owner;
	private final Property email;
	
	public AccountFilter(Main main, Account account) {
		super(main);
		final Database database = main.getDatabase();
		this.account = account;
		this.owner = Property.find(database, Property.OWNER);
		this.email = Property.find(database, Property.EMAIL);
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
		Base b = (Base)last;
		
		String uuid = owner.getPropertyValue(b);
		if(account.uuid.equals(uuid)) {
			return new VisuSpec(main, path, "white", "white", "Vastuu");
		}
		
		String es = email.getPropertyValue(b);
		if(es.contains(account.getEmail())) {
			return new VisuSpec(main, path, "white", "white", "Seurannassa");
		}
		
		return null;
		
	}

	
	@Override
	public String toString() {
		return "Omat oliot";
	}
	
}
