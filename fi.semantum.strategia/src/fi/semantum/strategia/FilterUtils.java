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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class FilterUtils {

	public static String getObjectTypeUUID(Database database, Base base) {
		
		Strategiakartta map = database.getMap(base);
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		if(base instanceof Tavoite) {

			Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
			return goalTypeProperty.getPropertyValue(level);

		} else if (base instanceof Painopiste) {

			Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
			return focusTypeProperty.getPropertyValue(level);

		} else if (base instanceof Strategiakartta) {
			
			return level.uuid;
			
		} else {
			
			return null;
			
		}
	}
	

	
	public static <T extends Base> List<T> filterByType(Database database, Collection<T> path, String name) {
		ObjectType tp = ObjectType.find(database, name);
		ArrayList<T> result = new ArrayList<T>();
		for(T b : path) {
			String uuid = getObjectTypeUUID(database, b);
			if(tp.uuid.equals(uuid))
				result.add(b);
		}
		return result;
	}
	
	public static Strategiakartta getLastMap(Database database, List<Base> path) {
		for(int i=0;i<path.size();i++) {
			Base b = path.get(path.size()-1-i);
			Strategiakartta map = database.getMap(b);
			if(map != null) return map;
		}
		return null;
	}

	public static boolean inScope(Database database, List<Base> path, Strategiakartta scope) {
		Strategiakartta map = getLastMap(database, path);
		if(scope.isUnder(database, map)) return true;
		if(map.isUnder(database, scope)) return true;
		return false;
	}

	public static List<Base> getMaps(Database database, Strategiakartta map) {
		for(Linkki l : map.parents) {
			Strategiakartta p = database.find(l.uuid);
			ArrayList<Base> result = new ArrayList<Base>(getMaps(database, p));
			result.add(map);
			return result;
		}
		return Collections.<Base>singletonList(map);
	}
		
	public static List<Base> getMaps(List<Base> path) {
		ArrayList<Base> result = new ArrayList<Base>();
		for(int i=0;i<path.size();i++) {
			Base b = path.get(i);
			if(b instanceof Strategiakartta) result.add(b);
			else return result;
		}
		return result;
	}
	
	public static boolean isPrefix(List<Base> path, List<Base> prefix) {
		if(path.size() < prefix.size()) return false;
		for(int i=0;i<prefix.size();i++) {
			if(!path.get(i).equals(prefix.get(i))) return false;
		}
		return true;
	}
	
	public static boolean contains(List<Base> path, Base base) {
		return path.contains(base);
	}

	public static boolean lastMap(List<Base> path, Strategiakartta map) {
		int index = path.indexOf(map);
		if(index == -1) return false;
		if(path.size() < (index+2)) return true;
		return !(path.get(index+1) instanceof Strategiakartta);
	}
	
	public static Strategiakartta getMap(List<Base> path) {
		for(int i=0;i<path.size();i++) {
			Object o = path.get(path.size()-1-i); 
			if(o instanceof Strategiakartta) return (Strategiakartta)o; 
		}
		return null;
	}

}
