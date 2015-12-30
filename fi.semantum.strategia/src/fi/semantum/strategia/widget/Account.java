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
import java.util.UUID;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;

public class Account extends Base {

	private static final long serialVersionUID = -8882871866781578456L;
	
	private boolean admin = false;
	public String hash;
	public String email;
	public List<UIState> uiStates = new ArrayList<UIState>();
	public List<Right> rights = new ArrayList<Right>();

	public static Account create(Database database, String userName, String email, String hash) {
		Account p = new Account(userName, email, hash);
		database.register(p);
		return p;
	}
	
	private Account(String name, String email, String hash) {
		super(UUID.randomUUID().toString(), name, name);
		this.hash = hash;
		this.email = email;
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean value) {
		this.admin = value;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getEmail() {
		return email;
	}
	
	public static Account find(Database database, String name) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof Account) {
				Account p = (Account)b;
				if(name.equals(p.getId(database))) return p;
			}
		}
		return null;

	}
	
	public static boolean canRead(Main main, List<Base> path) {
		
		Account account = main.getAccountDefault();
		Base last = path.get(path.size()-1);
		return account.canRead(main.getDatabase(), last);

	}
	
	public static boolean canRead(Main main, Base b) {
		
		Account account = main.getAccountDefault();
		return account.canRead(main.getDatabase(), b);

	}

	public static boolean canWrite(Main main, Base b) {
		
		Account account = main.getAccountDefault();
		return account.canWrite(main.getDatabase(), b);

	}

	public boolean canRead(Database database, Base b) {
		Strategiakartta map = database.getMap(b);
		if(map == null) return false;
		for(Right r : rights) {
			if(r.recurse) {
				if(r.map.isUnder(database, map)) return true;
			} else {
				if(r.map.equals(map)) return true;
			}
		}
		return false;
	}

	public boolean canWrite(Database database, Base b) {
		Strategiakartta map = database.getMap(b);
		if(map == null) return false;
		for(Right r : rights) {
			if(!r.write) continue;
			if(r.recurse) {
				if(r.map.isUnder(database, map)) return true;
			} else {
				if(r.map.equals(map)) return true;
			}
		}
		return false;
	}

	public static List<Account> enumerate(Database database) {
		ArrayList<Account> result = new ArrayList<Account>();
		for(Base b : database.objects.values()) {
			if(b instanceof Account) result.add((Account)b);
		}
		return result;
	}
	
}
