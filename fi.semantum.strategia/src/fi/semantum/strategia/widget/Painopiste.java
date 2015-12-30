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

import java.io.Serializable;
import java.util.UUID;

public class Painopiste extends Base implements Serializable {

	private static final long serialVersionUID = 9064076890935334644L;

	public boolean copy;
	
	public static Painopiste create(Database database, Strategiakartta map, Tavoite goal, String text, ObjectType type) {
		return create(database, map, goal, text, text, type);
	}

	public static Painopiste create(Database database, Strategiakartta map, Tavoite goal, String text) {
		return create(database, map, goal, text, text, null);
	}

	public static Painopiste create(Database database, Strategiakartta map, Tavoite goal, String id, String text) {
		return create(database, map, goal, id, text, null);
	}

	public static Painopiste create(Database database, Strategiakartta map, Tavoite goal, String id, String text, ObjectType type) {
		Painopiste p = create(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addPainopiste(p);
		return p;
	}

	public static Painopiste createTransient(Database database, Strategiakartta map, Tavoite goal, String id, String text, ObjectType type) {
		Painopiste p = createTransient(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addPainopiste(p);
		return p;
	}

	public static Painopiste createTransient(Database database, Strategiakartta map, String id, String text, ObjectType type) {
		
		Painopiste p = new Painopiste(id, text);
		if(type != null) 
			p.properties.add(Pair.make(Property.find(database, Property.TYPE).uuid, type.uuid));
		
		Property.createProperties(database, map, p);
		
		ObjectType tulostavoite = ObjectType.find(database, ObjectType.TULOSTAVOITE);
		ObjectType toimenpide = ObjectType.find(database, ObjectType.TOIMENPIDE);
		ObjectType toimialanToimenpide = ObjectType.find(database, ObjectType.TOIMIALAN_TOIMENPIDE);
		ObjectType painopiste = ObjectType.find(database, ObjectType.PAINOPISTE);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);

		if(painopiste.uuid.equals(focusTypeUUID)) {
			Meter i = Meter.create(database, "Tulostavoitteet", "Tulostavoitteiden tila", null);
			p.addMeter(i);
		}
		
		if(tulostavoite.uuid.equals(focusTypeUUID)) {
			Meter i = Meter.create(database, "Toimenpiteet", "Toimenpiteiden tila", null);
			p.addMeter(i);
		}
		
		if(toimenpide.uuid.equals(focusTypeUUID) || toimialanToimenpide.uuid.equals(focusTypeUUID)) {
			Meter m = Meter.create(database, "Valmiusaste", "Toimenpiteen valmiusaste", null);
			m.setUserValue(null, 0.0);
			p.addMeter(m);
		}

		return p;
		
	}

	public static Painopiste create(Database database, Strategiakartta map, String id, String text, ObjectType type) {
		
		Painopiste p = createTransient(database, map, id, text, type);
		database.register(p);
		return p;
		
	}

	public static Painopiste createCopy(Database database, Strategiakartta map, Tavoite goal, Base ref) {
		String uuid = UUID.randomUUID().toString();
		Painopiste result = create(database, map, goal, uuid, "", null);
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);
		return result;
	}
	
	private Painopiste(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	@Override
	public String getDescription(Database database) {
		return getId(database) + " (" + database.getType(this) + ")";
	}
	
	@Override
	public Base getOwner(Database database) {
		return getGoal(database);
	}
	
	public Tavoite getGoal(Database database) {

		Tavoite result = null;
		for(Strategiakartta map : Strategiakartta.enumerate(database)) {
			for(Tavoite goal : map.tavoitteet) {
				for(Painopiste p : goal.painopisteet) {
					if(p.uuid.equals(uuid)) {
						if(result != null && result != goal) return null;
						result = goal;
					}
				}
			}
		}
		
		return result;
		
	}

	@Override
	public void remove(Database database) {
		Tavoite t = database.getTavoite(this);
		t.removePainopiste(database, this);
		super.remove(database);
	}
	
}
