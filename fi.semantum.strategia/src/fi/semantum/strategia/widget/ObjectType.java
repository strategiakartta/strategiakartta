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

public class ObjectType extends Base {

	/*
	 * LVM
	 * -osasto   tulostavoite/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * LIVI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * -osasto     toimialan toimenpide/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * VIVI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * (-yksikkö)     toimialan toimenpide/toimenpide
	 * -ryhmä  toimenpide/vastuuhenkilö
	 * 
	 * TRAFI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * -osasto     toimialan toimenpide/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * 
	 */
	
	public static final String HALLINNONALA = "Hallinnonala";
	public static final String VIRASTO = "Virasto";
	
	public static final String TOIMIALA = "Toimiala";
	
	public static final String LVM_OSASTO = "LVM:n osasto";
	public static final String VIRASTO_OSASTO = "Viraston osasto";

	public static final String KOKOAVA_YKSIKKO = "Vivin yksikkö";
	public static final String TOTEUTTAVA_YKSIKKO = "Yksikkö";
	
	public static final String RYHMA = "Ryhmä";
	
	public static final String ACCOUNT = "Account";
	public static final String LEVEL_TYPE = "LevelType";
	public static final String FOCUS_TYPE = "FocusType";
	public static final String GOAL_TYPE = "GoalType";

	public static final String STRATEGINEN_TAVOITE = "Strateginen tavoite";
	public static final String PAINOPISTE = "Painopiste";
	public static final String TULOSTAVOITE = "Tulostavoite";
	public static final String HALLINNONALAN_PAINOPISTE = "Hallinnonalan painopiste";
	public static final String TOIMENPIDE = "Toimenpide";
	public static final String TOIMIALAN_TOIMENPIDE = "Toimialan toimenpide";
	public static final String VASTUUHENKILO = "Vastuuhenkilö";
	
	private static final long serialVersionUID = -1598499411379047877L;

	public static ObjectType create(Database database, String name) {
		ObjectType p = new ObjectType(name, name);
		database.register(p);
		return p;
	}
	
	public static ObjectType create(Database database, String id, String text) {
		ObjectType p = new ObjectType(id, text);
		database.register(p);
		return p;
	}

	private ObjectType(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public static ObjectType find(Database database, String id) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof ObjectType) {
				ObjectType p = (ObjectType)b;
				if(id.equals(p.getId(database))) return p;
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
	
}
