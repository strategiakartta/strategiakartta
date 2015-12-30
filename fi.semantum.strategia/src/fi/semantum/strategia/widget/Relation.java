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

public class Relation extends Base {

	private static final long serialVersionUID = -1598499411379047877L;

	public static final String IMPLEMENTS = "Toteuttaa";
	public static final String COPY = "Kopio";
	public static final String MEASURES = "Mittaa";
	public static final String ALLOWS_SUBMAP = "Sallittu alikartta";

	public static final String RELATED_TO_TAG = "Liittyy aihetunnisteeseen";
	public static final String MONITORS_TAG = "Monitoroi aihetunnistetta";

	public static Relation create(Database database, String name) {
		Relation p = new Relation(name);
		database.register(p);
		return p;
	}
	
	private Relation(String name) {
		super(UUID.randomUUID().toString(), name, name);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public static Relation find(Database database, String name) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof Relation) {
				Relation p = (Relation)b;
				if(name.equals(p.getText(database))) return p;
			}
		}
		return null;

	}
	
	public List<Pair> getRelations(Base b) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) {
				result.add(p);
			}
		}
		return result;
	}

	public Pair getPossibleRelation(Base b) {
		Pair result = null;
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) {
				if(result != null) return null;
				result = p;
			}
		}
		return result;
	}

	public boolean hasRelations(Base b) {
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) return true;
		}
		return false;
	}

}
