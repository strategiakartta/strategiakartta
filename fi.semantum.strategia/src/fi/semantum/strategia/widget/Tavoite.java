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

import java.util.Arrays;
import java.util.UUID;

import fi.semantum.strategia.Utils;
import fi.semantum.strategia.Wiki;

public class Tavoite extends Base {

	private static final long serialVersionUID = 2404810943743110180L;
	
	public Painopiste[] painopisteet = new Painopiste[0];
	public int rows = 0;
	public int extraRows = 0;
	public boolean copy;
	
	private Tavoite(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}

	public static Tavoite create(Database database, Strategiakartta map, String text) {
		return create(database, map, text, text);
	}

	public static Tavoite createCopy(Database database, Strategiakartta map, Base ref) {
		String uuid = UUID.randomUUID().toString();
		Tavoite result = create(database, map, uuid, "");
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);
		return result;
	}

	public static Tavoite createImpl(Database database, Strategiakartta map, Base ref, String id, String text) {
		Tavoite result = create(database, map, id, text);
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		return result;
	}

	public static Tavoite createTransient(Database database, Strategiakartta map, String id, String text) {

		Tavoite p = new Tavoite(id, text);
		Property.createProperties(database, map, p);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);

		ObjectType strateginen = ObjectType.find(database, ObjectType.STRATEGINEN_TAVOITE);

		if(strateginen.uuid.equals(goalTypeUUID)) {
			String pageName = Utils.makeTavoitePageName(database, map, p);
			Wiki.edit(pageName, "=Strategisen tavoitteen määritys=\n==Kuvaus tavoitetilasta==\n==Onnistumisen kriteerit==\n==Lähtöoletukset==\n==Riskit==\n=Voimavarat=\n==Henkiset voimavarat==\n==Fyysiset/aineelliset voimavarat==\n==Määrärahat==\n");
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
		if(pos != -1 && pos > 0) {
			Painopiste previous = painopisteet[pos-1];
			painopisteet[pos-1] = t;
			painopisteet[pos] = previous;
		}
	}

	public void moveDown(Painopiste t) {
		int pos = findPainopiste(t);
		if(pos != -1 && pos < painopisteet.length-1) {
			Painopiste next = painopisteet[pos+1];
			painopisteet[pos+1] = t;
			painopisteet[pos] = next;
		}
	}
	
	public int findPainopiste(Painopiste t) {
		for(int i=0;i<painopisteet.length;i++) {
			if(t.equals(painopisteet[i])) return i;
		}
		return -1;
	}
	
	public void addPainopiste(Painopiste p) {
		painopisteet = Arrays.copyOf(painopisteet, painopisteet.length+1);
		painopisteet[painopisteet.length-1] = p;
	}

	public void removePainopiste(Database database, Painopiste p) {
		int index = findPainopiste(p);
		if(index == -1) throw new IllegalArgumentException("Not found: " + p.getId(database) );
		removePainopiste(index);
	}

	public void removePainopiste(int index) {
		Painopiste[] old = painopisteet;
		painopisteet = new Painopiste[painopisteet.length-1];
		for(int i=0,pos=0;i<old.length;i++) {
			if(i == index) continue;
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
		Strategiakartta map = database.getMap(this);
		map.removeTavoite(database, this);
		super.remove(database);
	}
	
	public String getFocusDescription(Database database) {
		
		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if(hasRelatedTag(database, voimavarat)) {
			return "Keino";
		} else {
			final Strategiakartta map = database.getMap(this);
			return map.painopisteDescription;
		}
		
	}
	
}
