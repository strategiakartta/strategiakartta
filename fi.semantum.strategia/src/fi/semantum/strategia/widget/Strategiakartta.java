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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.semantum.strategia.Lucene;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;

public class Strategiakartta extends Base implements Serializable {

	private static final long serialVersionUID = 7736595146150973561L;
	
	public int width;
	public int extraRows;
	public String visio;
	public Tavoite[] tavoitteet = new Tavoite[0];
	public Linkki[] parents = new Linkki[0];
	public Linkki[] alikartat = new Linkki[0];
	public Tavoite voimavarat = null;
	
	public List<Tag> generators = new ArrayList<Tag>(); 
	
	public String ownGoalDescription = "";
	public String tavoiteDescription = "";
	public String painopisteDescription = "";
	public String characterColor = "";
	public String tavoiteColor = "";
	public String tavoiteTextColor = "";
	public String painopisteColor = "";
	
	private Strategiakartta(Main main, String id, String text, String visio, Collection<Pair> relations, Pair[] properties) {
		super(UUID.randomUUID().toString(), id, text);
		final Database database = main.getDatabase();
		this.visio = visio;
		this.relations.addAll(relations);
		for(Pair property : properties) {
			Property p = database.find(property.first);
			p.set(main, this, property.second);
		}
	}
	
	public static Strategiakartta create(Main main, String id, String text, String visio, Collection<Pair> relations, Pair[] properties) {
		Strategiakartta p = new Strategiakartta(main, id, text, visio, relations, properties);
		main.getDatabase().register(p);
		return p;
	}
	
	@Override
	public Base getOwner(Database database) {
		return getPossibleParent(database);
	}

	public void addTavoite(Tavoite t) {
		tavoitteet = Arrays.copyOf(tavoitteet, tavoitteet.length+1);
		tavoitteet[tavoitteet.length-1] = t;
		fixRows();
	}

	public void moveUp(Tavoite t) {
		int pos = findTavoite(t);
		if(pos != -1 && pos > 0) {
			Tavoite previous = tavoitteet[pos-1];
			tavoitteet[pos-1] = t;
			tavoitteet[pos] = previous;
		}
	}

	public void moveDown(Tavoite t) {
		int pos = findTavoite(t);
		if(pos != -1 && pos < tavoitteet.length-1) {
			Tavoite next = tavoitteet[pos+1];
			tavoitteet[pos+1] = t;
			tavoitteet[pos] = next;
		}
	}
	
	public int findTavoite(Tavoite t) {
		for(int i=0;i<tavoitteet.length;i++) {
			if(t.equals(tavoitteet[i])) return i;
		}
		return -1;
	}

	public void removeTavoite(Database database, Tavoite t) {
		int index = findTavoite(t);
		if(index == -1) throw new IllegalArgumentException("Not found: " + t.getId(database));
		removeTavoite(index);
	}
	
	public void removeTavoite(int index) {
		Tavoite[] old = tavoitteet;
		tavoitteet = new Tavoite[tavoitteet.length-1];
		for(int i=0,pos=0;i<old.length;i++) {
			if(i == index) continue;
			tavoitteet[pos++] = old[i];
		}
		fixRows();
	}

	public void addAlikartta(Strategiakartta map) {
		alikartat = Arrays.copyOf(alikartat, alikartat.length+1);
		alikartat[alikartat.length-1] = new Linkki(map.uuid);
		map.addParent(this);
	}

	public void removeAlikartta(String uuid) {
		Linkki[] old = alikartat;
		alikartat = new Linkki[alikartat.length-1];
		for(int i=0,pos=0;i<old.length;i++) {
			if(old[i].uuid.equals(uuid)) continue;
			alikartat[pos++] = old[i];
		}
	}

	public void addParent(Strategiakartta map) {
		parents = Arrays.copyOf(parents, parents.length+1);
		parents[parents.length-1] = new Linkki(map.uuid);
	}
	
	private static final int painopisteColumns = 2;
	
	public int extra(Tavoite t) {
		return Math.max(0, (t.painopisteet.length-1) / painopisteColumns);
	}
	
	public void fixRows() {
		
		this.extraRows = 0;
		
		for(int i=0;i<tavoitteet.length;i++) {
			Tavoite t = tavoitteet[i];
			if((i%2) == 0) {
				
				if(i==0) {
					t.rows = 0;
				} else {
					int l1 = extra(tavoitteet[i-2]);
					int l2 = extra(tavoitteet[i-1]);
					t.rows = tavoitteet[i-2].rows + Math.max(0, Math.max(l1, l2));
				}

				if(tavoitteet.length == (i+1)) {
					t.extraRows =  extra(t);
				} else {
					t.extraRows = Math.max(extra(t), extra(tavoitteet[i+1]));
				}

				this.extraRows += t.extraRows;

			} else {
				t.rows = tavoitteet[i-1].rows;
				t.extraRows = tavoitteet[i-1].extraRows;
			}
		}
	}
	
	@Override
	public String searchText(Database database) {
		return visio + " - " + super.searchText(database);
	}

	@Override
	public String getDescription(Database database) {
		return getId(database) + " (Strategiakartta)";
	}
	
	public static List<Strategiakartta> enumerate(Database database) {
		ArrayList<Strategiakartta> result = new ArrayList<Strategiakartta>();
		for(Base b : database.enumerate()) {
			if(b instanceof Strategiakartta) result.add((Strategiakartta)b);
		}
		return result;
	}
	
	public boolean genTest(Database database, Base b) {
		for(Tag req : generators) {
			if(!b.hasRelatedTag(database, req)) return false;
		}
		return true;
	}
	
	public void generate(Main main) {

		final Database database = main.getDatabase();

		if(!generators.isEmpty()) {
			
			try {
				
				Lucene.startWrite();
			
				Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
				
				Strategiakartta parent = getPossibleParent(database);
	
				Map<Base,List<Painopiste>> tavoitteet = new HashMap<Base,List<Painopiste>>();
				
				Map<Base,Boolean> implCache = new HashMap<Base,Boolean>();
				
				for(Linkki l : parent.alikartat) {
					Strategiakartta child = database.find(l.uuid);
					for(Tavoite t : child.tavoitteet) {
						boolean tGen = genTest(database, t);
						Pair p = implementsRelation.getPossibleRelation(t);
						if(p != null) {
							Base targetPP = database.find(p.second);
							List<Painopiste> curr = tavoitteet.get(targetPP);
							if(curr == null) {
								curr = new ArrayList<Painopiste>();
								tavoitteet.put(targetPP, curr);
							}
							loop:for(Painopiste pp : t.painopisteet) {
								if(tGen) {
									curr.add(pp);
									continue loop;
								}
								for(Base b : Utils.getImplementors(database, pp, implCache)) {
									if(genTest(database, b)) {
										curr.add(pp);
										continue loop;
									}
								}
							}
						}
					}
				}
	
				this.tavoitteet = new Tavoite[0];
				
				for(Map.Entry<Base,List<Painopiste>> entry : tavoitteet.entrySet()) {
					if(entry.getValue().isEmpty()) continue;
					
					Tavoite t = Tavoite.create(database, this, entry.getKey().getId(database), entry.getKey().getText(database));
					for(Painopiste p : entry.getValue()) {
						Painopiste p2 = Painopiste.create(database, this, t, p.getId(database) + " (" + database.getMap(p).getId(database) + ")", p.getText(database), null);
						p2.meters.clear();
						for(Meter m : p.getMeters(database))
							p2.addMeter(m);
					}
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			} finally {
			
				Lucene.endWrite();
				
			}
			
		}
		
	}
	
	public void prepare(Main main) {
		
		Database database = main.getDatabase();
		
		for(Linkki l : alikartat) {
			Strategiakartta k = database.find(l.uuid);
			l.text = k.getText(database);
		}
		for(Linkki l : parents) {
			Strategiakartta k = database.find(l.uuid);
			l.text = k.getText(database);
		}
		for(Tavoite t : tavoitteet) {
			t.synchronizeCopy(main);
			t.copy = t.isCopy(database);
			for(Painopiste p : t.painopisteet) {
				p.synchronizeCopy(main);
				p.copy = p.isCopy(database);
			}
			
		}

		fixRows();
		
		Property level = Property.find(database, Property.LEVEL);
		Property ownGoalType = Property.find(database, Property.OWN_GOAL_TYPE);
		Property goalType = Property.find(database, Property.GOAL_TYPE);
		Property focusType = Property.find(database, Property.FOCUS_TYPE);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		
		String typeUUID = level.getPropertyValue(this);
		if(typeUUID != null) {
			ObjectType mt = (ObjectType)database.find(typeUUID);
			Base tavoiteType = (Base)database.find(goalType.getPropertyValue(mt));
			Base painopisteType = (Base)database.find(focusType.getPropertyValue(mt));
			tavoiteDescription = tavoiteType.getText(database);
			painopisteDescription = painopisteType.getText(database);
			characterColor = characterColorP.getPropertyValue(mt);
			if(characterColor == null) characterColor = "#CA6446";
			tavoiteColor = characterColorP.getPropertyValue(tavoiteType);
			tavoiteTextColor = characterTextColorP.getPropertyValue(tavoiteType);
			painopisteColor = characterColorP.getPropertyValue(painopisteType);

			String ownGoalUUID = ownGoalType.getPropertyValue(mt);
			if(ownGoalUUID != null) {
				Base ownGoal = (Base)database.find(ownGoalUUID);
				ownGoalDescription = ownGoal.getText(database);
			} else {
				ownGoalDescription = tavoiteDescription;
			}

		}
		
	}
	
	public Strategiakartta getPossibleParent(Database database) {
		for(Linkki l : parents) {
			return database.find(l.uuid);
		}
		return null;
	}
	
	public boolean isUnder(Database database, Strategiakartta map) {
		if(this.equals(map)) return true;
		Strategiakartta parent = map.getPossibleParent(database);
		if(parent == null) return false;
		return isUnder(database, parent);
	}
	
	public int getLevel(Database database) {
		Strategiakartta parent = getPossibleParent(database);
		if(parent == null) return 0;
		return 1 + parent.getLevel(database);
	}
	
	public List<Base> getMaps(Database database) {
		Strategiakartta parent = getPossibleParent(database);
		if(parent == null) {
			List<Base> result = new ArrayList<Base>();
			result.add(this);
			return result;
		} else {
			List<Base> result = parent.getMaps(database);
			result.add(this);
			return result;
		}
	}
	
}
