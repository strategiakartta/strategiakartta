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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fi.semantum.strategia.Lucene;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.MapVisitor;
import fi.semantum.strategia.Utils;

/*
 * Common base class for all objects in a database
 * 
 */
abstract public class Base implements Serializable, Comparable<Base> {
	
	private static final long serialVersionUID = -4482518287842093370L;
	
	// GUID
	final public String uuid;
	// Short name for graphical visualization
	protected String id;
	// Full name for listing
	public String text;
	// A compact description (shown in text areas)
	public String description = "";
	// Wiki markup related to this
	public String markup = "";
	
	List<Pair> relations = new ArrayList<Pair>();
	List<Pair> properties = new ArrayList<Pair>();
	ArrayList<Indicator> indicators = new ArrayList<Indicator>();
	List<Meter> meters = new ArrayList<Meter>();

	protected Base(String uuid, String id, String text) {
		this.uuid = uuid;
		this.id = id;
		this.text = text;
	}
	
	protected void setId(Database database, String id) {
		this.id = id;
	}
	
	public boolean canRemove(Database database) {
		Collection<Base> implementors = database.getInverse(this, Relation.find(database, Relation.IMPLEMENTS));
		return implementors.isEmpty();
	}

	abstract public Base getOwner(Database database);

	public List<Base> getOwners(Database database) {
		ArrayList<Base> result = new ArrayList<Base>();
		Base owner = this;
		while(owner != null) {
			result.add(owner);
			owner = owner.getOwner(database);
		}
		return result;
	}

	public void remove(Database database) {
		for(Meter m : meters) m.remove(database);
		for(Indicator i : indicators) i.remove(database);
		database.remove(this);
	}
	
	public Map<String,String> searchMap(Database database) {
		HashMap<String,String> result = new HashMap<String,String>();
		result.put("Wiki", markup.toLowerCase());
		StringBuilder tagString = new StringBuilder();
		for(Tag t : getRelatedTags(database)) {
			tagString.append(" ");
			tagString.append(t.getId(database));
		}
		result.put("Aihetunnisteet", tagString.toString().toLowerCase());
		result.put("Sovellus", text.toLowerCase() + " " + id.toLowerCase());
		return result;
	}

	public String searchText(Database database) {
		
		Map<String,String> map = searchMap(database);
		
		StringBuilder b = new StringBuilder();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			b.append(" ");
			b.append(entry.getValue());
		}
		return b.toString();
		
	}
	
	public String getCaption(Database database) {
		String id = getId(database);
		String caption = getText(database);
		if(caption.isEmpty()) {
			return id;
		} else {
			if(id.isEmpty()) {
				return caption;
			} else {
				return caption + " (" + id + ")";
			}
		}
	}
	
	public String getDescription(Database database) {
		if(description == null || description.isEmpty()) return "ei m‰‰rityst‰";
		return description;
	}
	
	public List<Indicator> getIndicators(Database database) {
		return new ArrayList<Indicator>(indicators);
	}
	
	public List<Indicator> getIndicatorsActive(Main main) {
		
		ArrayList<Indicator> pps = new ArrayList<Indicator>();

		Property aika = Property.find(main.getDatabase(), Property.AIKAVALI);

		for(Indicator m : indicators) {
			String a = aika.getPropertyValue(m);
			if(a != null) {
				if(main.acceptTime(a))
					pps.add(m);
			} else {
				pps.add(m);
			}
		}

		return pps;

	}
	
	public void moveMetersUp(Collection<Meter> selectedMeters) {
		TreeMap<Integer,Meter> map = sortMeters(selectedMeters, 1);
		meters = new ArrayList<Meter>(map.values());
	}
	
	public void moveMetersDown(Collection<Meter> selectedMeters) {
		ArrayList<Meter> sel = new ArrayList<Meter>(selectedMeters);
		Collections.reverse(sel);
		TreeMap<Integer,Meter> map = sortMeters(sel, -1);
		meters = new ArrayList<Meter>(map.descendingMap().values());
	}
	
	private TreeMap<Integer,Meter> sortMeters(Collection<Meter> selectedMeters, int direction) {
		TreeMap<Integer,Meter> map = new TreeMap<Integer,Meter>();
		Map<Meter,Integer> map2 = new HashMap<Meter,Integer>();
		int index = 0;
		for(Meter m : meters) {
			map.put(index, m);
			map2.put(m, index);
			index += direction;
		}
		for(Meter m : selectedMeters) {
			Integer currentIndex = map2.get(m);
			if(currentIndex != null) {
				Meter m2 = map.get(currentIndex-1);
				if(m2 != null) {
					map.put(currentIndex, m2);
					map2.put(m2, currentIndex);
				} else {
					map.remove(currentIndex);
				}
				map.put(currentIndex-1, m);
				map2.put(m, currentIndex-1);
			}
		}
		return map;
	}

	public void moveIndicatorsUp(Collection<Indicator> selectedIndicators) {
		TreeMap<Integer,Indicator> map = sortIndicators(selectedIndicators, 1);
		indicators = new ArrayList<Indicator>(map.values());
	}
	
	public void moveIndicatorsDown(Collection<Indicator> selectedIndicators) {
		ArrayList<Indicator> sel = new ArrayList<Indicator>(selectedIndicators);
		Collections.reverse(sel);
		TreeMap<Integer,Indicator> map = sortIndicators(sel, -1);
		indicators = new ArrayList<Indicator>(map.descendingMap().values());
	}
	
	private TreeMap<Integer,Indicator> sortIndicators(Collection<Indicator> selectedIndicators, int direction) {
		TreeMap<Integer,Indicator> map = new TreeMap<Integer,Indicator>();
		Map<Indicator,Integer> map2 = new HashMap<Indicator,Integer>();
		int index = 0;
		for(Indicator m : indicators) {
			map.put(index, m);
			map2.put(m, index);
			index += direction;
		}
		for(Indicator m : selectedIndicators) {
			Integer currentIndex = map2.get(m);
			if(currentIndex != null) {
				Indicator m2 = map.get(currentIndex-1);
				if(m2 != null) {
					map.put(currentIndex, m2);
					map2.put(m2, currentIndex);
				} else {
					map.remove(currentIndex);
				}
				map.put(currentIndex-1, m);
				map2.put(m, currentIndex-1);
			}
		}
		return map;
	}

	public void addIndicator(Indicator indicator) {
		indicators.add(indicator);
	}
	
	public void removeIndicator(Indicator indicator) {
		indicators.remove(indicator);
	}
	
	public void addMeter(Meter meter) {
		meters.add(meter);
	}
	
	public void removeMeter(Meter meter) {
		meters.remove(meter);
	}

	public List<Meter> getMeters(Database database) {
		return new ArrayList<Meter>(meters);
	}

	public List<Meter> getMetersActive(Main main) {
			
		ArrayList<Meter> pps = new ArrayList<Meter>();

		Property aika = Property.find(main.getDatabase(), Property.AIKAVALI);

		for(Meter m : meters) {
			String a = aika.getPropertyValue(m);
			if(a != null) {
				if(main.acceptTime(a))
					pps.add(m);
			} else {
				pps.add(m);
			}
		}

		return pps;

	}
	
	public boolean isActive(Main main) {
		Property aika = Property.find(main.getDatabase(), Property.AIKAVALI);
		String a = aika.getPropertyValue(this);
		if(a != null) {
			if(main.acceptTime(a))
				return true;
		} else {
			return true;
		}
		return false;
	}

	public List<Tag> getRelatedTags(Database database) {
		Relation r = Relation.find(database, Relation.RELATED_TO_TAG);
		if(r == null) return Collections.emptyList();
		return new ArrayList<Tag>(this.<Tag>getRelatedObjects(database, r));
	}

	public List<Tag> getMonitorTags(Database database) {
		Relation r = Relation.find(database, Relation.MONITORS_TAG);
		if(r == null) return Collections.emptyList();
		return new ArrayList<Tag>(this.<Tag>getRelatedObjects(database, r));
	}
	
	public void removeRelatedTags(Database database, Collection<Tag> tags) {
		for(Tag t : tags)
			denyRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void removeRelatedTags(Database database, Tag ...tags) {
		for(Tag t : tags)
			denyRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void removeMonitorTags(Database database, Tag ...tags) {
		for(Tag t : tags)
			denyRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}

	public void setRelatedTags(Database database, Collection<Tag> newTags) {
		removeRelatedTags(database, getRelatedTags(database));
		assertRelatedTags(database, newTags);
	}
	
	public void assertRelatedTags(Database database, Collection<Tag> newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void assertRelatedTags(Database database, Tag ...newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void assertMonitorTags(Database database, Collection<Tag> newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}
	
	public void assertMonitorTags(Database database, Tag ... newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}

	public boolean hasRelatedTag(Database database, Tag tag) {
		return hasRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), tag);
	}

	public boolean hasMonitorTag(Database database, Tag tag) {
		return hasRelation(database, Relation.find(database, Relation.MONITORS_TAG), tag);
	}

	public String getText(Database database) {
		return text;
	}
	
	public void setText() {
		throw new IllegalStateException();
	}
	
	public boolean modified(Main main) {

		final Database database = main.getDatabase();

		Account account = main.getAccountDefault();
		if(!account.canWrite(database, this))
			return false;

		Property changedOn = Property.find(database, Property.CHANGED_ON);
		String acc = account.getId(database);
		changedOn.set(null, database, this, Utils.sdf.format(new Date()) + " " + acc);
		
		return true;
		
	}

	public boolean modifyText(Main main, String text) {
		
		Account account = main.getAccountDefault();
		return modifyText(main, account, text);
		
	}

	public boolean modifyText(Main main, Account account, String text) {

		assert(text != null);

		final Database database = main.getDatabase();

		if(!account.canWrite(database, this))
			return false;
		
		if(!text.equals(this.text)) {
			modified(main);
			this.text = text;
			try {
				Lucene.set(uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}

	public boolean modifyDescription(Main main, String text) {
		
		Account account = main.getAccountDefault();
		return modifyDescription(main, account, text);
		
	}

	boolean modifyDescription(Main main, Account account, String text) {
		
		assert(text != null);
		
		final Database database = main.getDatabase();

		if(!account.canWrite(database, this))
			return false;
		
		if(!text.equals(this.description)) {
			modified(main);
			this.description = text;
			try {
				Lucene.set(uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}

	public boolean modifyId(Main main, String text) {
		
		Account account = main.getAccountDefault();
		return modifyId(main, account, text);
		
	}

	public boolean modifyId(Main main, Account account, String id) {
		
		assert(id != null);
		
		final Database database = main.getDatabase();

		if(!account.canWrite(database, this))
			return false;
		
		if(!id.equals(getId(database))) {
			modified(main);
			setId(database, id);
			try {
				Lucene.set(uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}


	public boolean modifyMarkup(Main main, String markup) {
		
		Account account = main.getAccountDefault();
		return modifyMarkup(main, account, markup);
		
	}
	
	public boolean modifyMarkup(Main main, Account account, String markup) {
		
		final Database database = main.getDatabase();

		if(!account.canWrite(database, this))
			return false;
		
		if(!this.markup.equals(markup)) {
			modified(main);
			this.markup = markup;
			try {
				Lucene.set(uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public String getId(Database database) {
		return id;
	}
	
	public Base getPossibleCopy(Database database) {
		Relation copy = Relation.find(database, Relation.COPY);
		Pair p = copy.getPossibleRelation(this);
		if(p == null) return null;
		return database.find(p.second);
	}
	
	public boolean isCopy(Database database) {
		Relation copy = Relation.find(database, Relation.COPY);
		return copy.hasRelations(this);
	}
	
	public void synchronizeCopy(Main main) {
		final Database database = main.getDatabase();
		Base copy = getPossibleCopy(database);
		if(copy == null) return;
		copy.synchronizeCopy(main);
		setId(database, copy.getId(database));
		modifyText(main, database.system, copy.getText(database));
	}

	public String hexI(int i) {
		if(i < 10) return "" + i;
		else {
			char offset = (char) (i-10);
			char c = (char) ('A' + offset);
			return "" + c;
		}
	}
	
	public String hex(double value) {
		int i = (int)value;
		int upper = i >> 4;
		int lower = i & 15;
		return hexI(upper) + hexI(lower);
	}
	
	public String trafficColor(double value) {
		
		double redR = 218.0;
		double redG = 37.0;
		double redB = 29.0;
		
		double yellowR = 244.0;
		double yellowG = 192.0;
		double yellowB = 0.0;

		double greenR = 0.0;
		double greenG = 146.0;
		double greenB = 63.0;
		
		if(value < 0.5) {
			double r = (1-2.0*value)*redR + (2.0*value)*yellowR;
			double g = (1-2.0*value)*redG + (2.0*value)*yellowG;
			double b = (1-2.0*value)*redB + (2.0*value)*yellowB;
			return "#" + hex(r) + hex(g) + hex(b);
		} else {
			double r = (2.0*value - 1)*greenR + (2 - 2.0*value)*yellowR;
			double g = (2.0*value - 1)*greenG + (2 - 2.0*value)*yellowG;
			double b = (2.0*value - 1)*greenB + (2 - 2.0*value)*yellowB;
			return "#" + hex(r) + hex(g) + hex(b);
		}
		
	}
	
	public Strategiakartta getMap(Database database) {
		return database.getMap(this);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Base> Collection<T> getRelatedObjects(Database database, Relation r) {
		Set<T> result = new TreeSet<T>();
		for(Pair p : r.getRelations(this)) {
			Base found = database.find(p.second);
			if(found != null)
				result.add((T)found);
		}
		return result;
	}
	
	public boolean hasRelation(Database database, Relation r, Base b) {
		for(Pair p : r.getRelations(this)) {
			Base found = database.find(p.second);
			if(found.equals(b)) return true;
		}
		return false;
	}
	
	public void addRelation(Relation r, Base b) {
		relations.add(new Pair(r.uuid, b.uuid));
	}

	public void assertRelation(Database database, Relation r, Base b) {
		if(!hasRelation(database, r, b)) addRelation(r, b);
	}

	public void denyRelation(Database database, Relation r, Base b) {
		relations.remove(new Pair(r.uuid, b.uuid));
	}
	
	final public void accept(MapVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int compareTo(Base o) {
		return uuid.compareTo(o.uuid);
	}
	
	public boolean migrate(Main main) {
		
		boolean result = false;

		if(description == null) {
			description = "";
			result = true;
		}
		
		Database database = main.getDatabase();
		
		Property aika = Property.find(database, Property.AIKAVALI);
		aika.setEnumeration(Collections.<String>emptyList());
		String validity = aika.getPropertyValue(this);
		if(validity == null || "Kaikki".equals(validity)) {
			aika.set(null, database, this, Property.AIKAVALI_KAIKKI);
		}

		return result;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Base other = (Base) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
}
