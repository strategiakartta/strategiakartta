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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;

public class Painopiste extends Base implements Serializable, Moveable  {

	private static final long serialVersionUID = 9064076890935334644L;

	public boolean copy;
	
	/*public static Painopiste create(Main main, Strategiakartta map, Tavoite goal, String text, ObjectType type) {
		return create(main, map, goal, text, text, type);
	}*/

	public static Painopiste create(Main main, Strategiakartta map, Tavoite goal, String text) {
		return create(main, map, goal, "", text, null);
	}

	public static Painopiste create(Main main, Strategiakartta map, Tavoite goal, String id, String text) {
		return create(main, map, goal, id, text, null);
	}

	public static Painopiste create(Main main, Strategiakartta map, Tavoite goal, String id, String text, ObjectType type) {
		
		Database database = main.getDatabase();
		Painopiste p = create(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addPainopiste(p);
		
		Property time = Property.find(database, Property.AIKAVALI);
		String currentTime = main.getUIState().time;
		time.set(null, database, p, currentTime);
		
		return p;
		
	}

	public static Painopiste createTransient(Main main, Strategiakartta map, Tavoite goal, String id, String text, ObjectType type) {
		
		Database database = main.getDatabase();
		Painopiste p = createTransient(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addPainopiste(p);
		
		Property time = Property.find(database, Property.AIKAVALI);
		String currentTime = main.getUIState().time;
		time.set(null, database, p, currentTime);
		
		return p;
	}

	public static Painopiste createTransient(Database database, Strategiakartta map, String id, String text, ObjectType type) {
		
		Painopiste p = new Painopiste(id, text);
		if(type != null) 
			p.properties.add(Pair.make(Property.find(database, Property.TYPE).uuid, type.uuid));
		
		Property.createProperties(database, map, p);
		
//		ObjectType tulostavoite = ObjectType.find(database, ObjectType.TULOSTAVOITE);
//		ObjectType toimenpide = ObjectType.find(database, ObjectType.TOIMENPIDE);
//		ObjectType toimialanToimenpide = ObjectType.find(database, ObjectType.TOIMIALAN_TOIMENPIDE);
//		ObjectType painopiste = ObjectType.find(database, ObjectType.PAINOPISTE);
		
//		Property levelProperty = Property.find(database, Property.LEVEL);
//		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
//		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
//		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);
//
//		if(painopiste.uuid.equals(focusTypeUUID)) {
//			Meter i = Meter.create(database, "Tulostavoitteet", "Tulostavoitteiden tila", null);
//			p.addMeter(i);
//		}
		
//		if(tulostavoite.uuid.equals(focusTypeUUID)) {
//			Meter i = Meter.create(database, "Toimenpiteet", "Toimenpiteiden tila", null);
//			p.addMeter(i);
//		}
//		
//		if(toimenpide.uuid.equals(focusTypeUUID) || toimialanToimenpide.uuid.equals(focusTypeUUID)) {
//			Meter m = Meter.create(database, "Valmiusaste", "Toimenpiteen valmiusaste", null);
//			m.setUserValue(null, 0.0);
//			p.addMeter(m);
//		}

		return p;
		
	}

	public static Painopiste create(Database database, Strategiakartta map, String id, String text, ObjectType type) {
		
		Painopiste p = createTransient(database, map, id, text, type);
		database.register(p);
		return p;
		
	}

	public static Painopiste createCopy(Main main, Strategiakartta map, Tavoite goal, Base ref) {
		
		Database database = main.getDatabase();
		String uuid = UUID.randomUUID().toString();
		Painopiste result = create(main, map, goal, uuid, "", null);
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);
		
		Property time = Property.find(database, Property.AIKAVALI);
		String currentTime = main.getUIState().time;
		time.set(null, database, result, currentTime);

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
	
	@Override
	public void moveUp(Main main) {
		Tavoite t = main.getDatabase().getTavoite(this);
		t.moveUp(this);
		Updates.updateJS(main, true);
	}
	
	@Override
	public void moveDown(Main main) {
		Tavoite t = main.getDatabase().getTavoite(this);
		t.moveDown(this);
		Updates.updateJS(main, true);
	}
		
	public List<Meter> getImplementationMeters(Main main, boolean forecast) {
		
		Database database = main.getDatabase();

		List<Meter> result = new ArrayList<Meter>();

		int counter = 1;

		Strategiakartta map = getMap(database);
		if(map == null) return Collections.emptyList();
		
		Property aika = Property.find(database, Property.AIKAVALI);

		if(map.linkGoalsAndSubmaps) {

			Collection<Base> impSet = Utils.getDirectImplementors(database, this, main.getUIState().time);
			for(Base imp : impSet) {
				Tavoite t = (Tavoite)imp;
				for(Painopiste p : t.painopisteet) {
					String a = aika.getPropertyValue(p);
					if(main.acceptTime(a)) {
						String pid = p.getId(database);
						Meter m = p.getPrincipalMeter(main, pid.isEmpty() ? "" + counter : pid, forecast);
						if(m.isTransient()) {
							m.description = p.getText(database);
						}
						result.add(m);
						counter++;
					}
				}
			}
			
		} else {

			Collection<Base> impSet = Utils.getDirectImplementors(database, this, main.getUIState().time);
			for(Base b : impSet) {
				String a = aika.getPropertyValue(b);
				if(main.acceptTime(a)) {
					if(b instanceof Tavoite) {
						Tavoite imp = (Tavoite)b;
						String tid = imp.getMap(database).getId(database);
						result.add(imp.getPrincipalMeter(main, tid.isEmpty() ? "" + counter : tid, forecast));
						counter++;
					}
				}
			}
			
		}

		return result;

	}
	
	public Meter getPrincipalMeter(Main main, String id, boolean forecast) {
		
		Database database = main.getDatabase();
		
		Meter pm = getPossiblePrincipalMeterActive(main);
		if(pm != null) return pm;
		
		Collection<Meter> imps = getImplementationMeters(main, forecast); 
		if(imps.size() == 1) {
			Meter m = imps.iterator().next();
			if(m.isPrincipal) {
				return m;
			}
		}
		if(imps.size() > 0) {
			double value = 0;
			for(Meter m : imps) {
				value += m.value(database, forecast);
			}
			value = value / (double)imps.size();
   			String id2 = "" + (int)(100.0*value) + "%";
			return Meter.transientMeter(id2, getMap(database).uuid, value);
		} else {
			if(forecast)
				return Meter.transientMeter("100%", getMap(database).uuid, 1.0);
			else
				return Meter.transientMeter("0%", getMap(database).uuid, 0.0);
		}
	}
	
	public Tavoite getPossibleImplementationGoal(Database database) {
		Set<Tavoite> result = new HashSet<Tavoite>();
		for(Base b : Utils.getDirectImplementors(database, this)) {
			if(b instanceof Tavoite) result.add((Tavoite)b);
		}
		if(result.size() == 1) return result.iterator().next();
		return null;
	}
	
}
