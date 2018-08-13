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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.Wiki;

public class Tavoite extends Base implements Moveable {

	private static final long serialVersionUID = 2404810943743110180L;

	public Painopiste[] painopisteet = new Painopiste[0];
	public int rows = 0;
	public int extraRows = 0;
	public boolean copy;

	private Tavoite(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}

	public static Tavoite create(Database database, Strategiakartta map, String text) {
		return create(database, map, "", text);
	}

	public static Tavoite createCopy(Main main, Strategiakartta map, Base ref) {

		Database database = main.getDatabase();

		String uuid = UUID.randomUUID().toString();
		Tavoite result = create(database, map, uuid, "");
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);

		Property time = Property.find(database, Property.AIKAVALI);
		String currentTime = main.getUIState().time;
		time.set(null, database, result, currentTime);

		return result;

	}

	public static Tavoite createTransient(Database database, Strategiakartta map, String id, String text) {

		Tavoite p = new Tavoite(id, text);
		Property.createProperties(database, map, p);

		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String) levelProperty.getPropertyValue(map));

		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);

		ObjectType strateginen = ObjectType.find(database, ObjectType.STRATEGINEN_TAVOITE);

		if (strateginen.uuid.equals(goalTypeUUID)) {
			String pageName = Wiki.makeWikiPageName(database, p);
			Wiki.edit(pageName,
					"=Strategisen tavoitteen määritys=\n==Kuvaus tavoitetilasta==\n==Onnistumisen kriteerit==\n==Lähtöoletukset==\n==Riskit==\n=Voimavarat=\n==Henkiset voimavarat==\n==Fyysiset/aineelliset voimavarat==\n==Määrärahat==\n");
		}

		map.addTavoite(p);

		return p;

	}

	public static Tavoite create(Database database, Strategiakartta map, String id, String text) {

		Tavoite t = createTransient(database, map, id, text);
		database.register(t);
		return t;

	}

	@Override
	public Base getOwner(Database database) {
		return getMap(database);
	}

	public void add(Database database, Painopiste painopiste) {
		painopiste.addRelation(Relation.find(database, Relation.IMPLEMENTS), this);
		addPainopiste(painopiste);
	}

	public void moveUp(Painopiste t) {
		int pos = findPainopiste(t);
		if (pos != -1 && pos > 0) {
			Painopiste previous = painopisteet[pos - 1];
			painopisteet[pos - 1] = t;
			painopisteet[pos] = previous;
		}
	}

	public void moveDown(Painopiste t) {
		int pos = findPainopiste(t);
		if (pos != -1 && pos < painopisteet.length - 1) {
			Painopiste next = painopisteet[pos + 1];
			painopisteet[pos + 1] = t;
			painopisteet[pos] = next;
		}
	}

	public int findPainopiste(Painopiste t) {
		for (int i = 0; i < painopisteet.length; i++) {
			if (t.equals(painopisteet[i]))
				return i;
		}
		return -1;
	}

	public void addPainopiste(Painopiste p) {
		painopisteet = Arrays.copyOf(painopisteet, painopisteet.length + 1);
		painopisteet[painopisteet.length - 1] = p;
	}

	public void removePainopiste(Database database, Painopiste p) {
		int index = findPainopiste(p);
		if (index == -1)
			throw new IllegalArgumentException("Not found: " + p.getId(database));
		removePainopiste(index);
	}

	public void removePainopiste(int index) {
		Painopiste[] old = painopisteet;
		painopisteet = new Painopiste[painopisteet.length - 1];
		for (int i = 0, pos = 0; i < old.length; i++) {
			if (i == index)
				continue;
			painopisteet[pos++] = old[i];
		}
	}

	@Override
	public String getDescription(Database database) {
		return getId(database) + " (" + database.getType(this) + ")";
	}

	@Override
	public boolean canRemove(Database database) {
		return painopisteet.length == 0;
	}

	@Override
	public void remove(Database database) {
		try {
			Strategiakartta implementationMap = getPossibleImplementationMap(database);
			if(implementationMap != null) {
				implementationMap.remove(database);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Strategiakartta map = database.getMap(this);
		map.removeTavoite(database, this);
		super.remove(database);
	}

	public String getFocusDescription(Database database) {

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if (hasRelatedTag(database, voimavarat)) {
			return "Keino";
		} else {
			final Strategiakartta map = database.getMap(this);
			return map.painopisteDescription;
		}

	}

	@Override
	public void moveUp(Main main) {
		Strategiakartta map = main.getDatabase().getMap(this);
		map.moveUp(this);
		Updates.updateJS(main, true);
	}

	@Override
	public void moveDown(Main main) {
		Strategiakartta map = main.getDatabase().getMap(this);
		map.moveDown(this);
		Updates.updateJS(main, true);
	}

	public Meter getImplementationMeter(Main main, String id, boolean forecast) {

		Database database = main.getDatabase();

		Property aika = Property.find(database, Property.AIKAVALI);

		double value = 0;
		int acceptedCounter = 0;
		for (Painopiste p : painopisteet) {
			String a = aika.getPropertyValue(p);
			if (main.acceptTime(a)) {
				Meter m = p.getPrincipalMeter(main, id, forecast);
				value += m.value(database, forecast);
				acceptedCounter++;
			}
		}
		if (acceptedCounter > 0) {
			value = value / (double) acceptedCounter;
			return Meter.transientMeter(id, getMap(database).uuid, value);
		} else {
			return Meter.transientMeter(id, getMap(database).uuid, 1.0);
		}

	}

	public Meter getPrincipalMeter(Main main, String id, boolean forecast) {
		Database database = main.getDatabase();
		for (Meter m : getMetersActive(main)) {
			if (m.isPrincipal) {
				Meter result = Meter.transientMeter(id, getMap(database).uuid, m.value(database, forecast));
				result.isPrincipal = true;
				return result;
			}
		}
		return getImplementationMeter(main, id, forecast);
	}

	/*public Strategiakartta getImplementingSubmap(Database database) {
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		for (Base b : database.getInverse(this, implementsRelation)) {
			if (b instanceof Strategiakartta) {
				return (Strategiakartta) b;
			}
		}
		return null;
	}*/
	
	public Base getPossibleSubmapType(Database database) throws Exception {
		return getMap(database).getPossibleSubmapType(database);
	}
	
	public boolean hasImplementationSubmap(Database database) {
		try {
			return getPossibleSubmapType(database) != null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<Tavoite> enumerate(Database database) {
		ArrayList<Tavoite> result = new ArrayList<Tavoite>();
		for(Base b : database.enumerate()) {
			if(b instanceof Tavoite) result.add((Tavoite)b);
		}
		return result;
	}

	public Set<Strategiakartta> getImplementationMaps(Database database) {
		HashSet<Strategiakartta> result = new HashSet<Strategiakartta>();
		for(Base b : Utils.getDirectImplementors(database, this, false, Property.AIKAVALI_KAIKKI)) {
			if(b instanceof Strategiakartta) result.add((Strategiakartta)b);
		}
		return result;
	}
	
	public Strategiakartta getPossibleImplementationMap(Database database) throws Exception {
		Set<Strategiakartta> maps = getImplementationMaps(database);
		if(maps.size() == 1) return maps.iterator().next();
		else if(maps.size() == 0) return null;
		else throw new Exception("Multiple implementation maps.");
	}

	public boolean ensureImplementationMap(final Main main) throws Exception {
	
		boolean didSomething = false;
		
		if("Voimavarat".equals(id)) return false;
		
		Database database = main.getDatabase();
		Base subType = getPossibleSubmapType(database);
		if(subType != null) {
			Strategiakartta implementationMap = getPossibleImplementationMap(database);
			if(implementationMap == null) {
				implementationMap = getPossibleImplementationMap(database);
				Strategiakartta parent = getMap(database) ;
				implementationMap = database.newMap(main, parent, "", "", subType);
				implementationMap.addRelation(Relation.find(database, Relation.IMPLEMENTS), this);
				didSomething = true;
			}
			for(Painopiste pp : painopisteet) {
				Tavoite goal = pp.getPossibleImplementationGoal(database);
				if(goal == null) {
					Tavoite.createCopy(main, implementationMap, pp);
					didSomething = true;
				}
			}
		}
		
		return didSomething;
		
	}	
}
