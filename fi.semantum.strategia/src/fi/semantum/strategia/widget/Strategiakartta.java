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
	
	public String tavoiteDescription = "";
	public String painopisteDescription = "";
	//public String characterColor = "";
	public String tavoiteColor = "";
	public String tavoiteTextColor = "";
	public String painopisteColor = "";
	public String painopisteTextColor = "";
	public boolean linkWithParent = true;
	public boolean linkGoalsAndSubmaps = false;
	
	public boolean showVision = true;
	
	private Strategiakartta(Main main, String id, String text, String visio, Collection<Pair> relations, Pair[] properties) {
		super(UUID.randomUUID().toString(), id, text);
		final Database database = main.getDatabase();
		this.visio = visio;
		this.relations.addAll(relations);
		for(Pair property : properties) {
			Property p = database.find(property.first);
			p.set(null, database, this, property.second);
		}
	}
	
	public static Strategiakartta create(Main main, String id, String text, String visio, Collection<Pair> relations, Pair[] properties) {
		Strategiakartta p = new Strategiakartta(main, id, text, visio, relations, properties);
		main.getDatabase().register(p);
		return p;
	}

	
	@Override
	public String getId(Database database) {
		Base imp = getImplemented(database);
		if(imp != null) return imp.getId(database);
		return super.getId(database);
	}
	
	@Override
	public String getText(Database database) {
		Base imp = getImplemented(database);
		if(imp != null) return imp.getText(database);
		return super.getText(database);
	}

	@Override
	public boolean modifyText(Main main, Account account, String text) {
		
		Database database = main.getDatabase();
		Base imp = getImplemented(database);
		if(imp != null)
			return imp.modifyText(main, account, text);

		return super.modifyText(main, account, text);
		
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
				
				Lucene.startWrite(database.getDatabaseId());
			
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
						Painopiste p2 = Painopiste.create(main, this, t, p.getId(database) + " (" + database.getMap(p).getId(database) + ")", p.getText(database), null);
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
	
	static class CharacterInfo {
		public String goalDescription;
		public String focusDescription;
		public String color;
		public String textColor;
		public boolean linkWithParent;
		public boolean linkGoalsAndSubmaps;
		public ObjectType goalSubmapType;
		
		public CharacterInfo getGoalSubmapInfo(Database database) {
			if(goalSubmapType != null) {
				CharacterInfo ci = Strategiakartta.getCharacterInfo(database, goalSubmapType);
				if(ci.focusDescription.isEmpty()) {
					ci.focusDescription = goalDescription;
				}
				return ci;
			} else {
				return null;
			}
		}
		
	}
	
	public static CharacterInfo getCharacterInfo(Database database, ObjectType mt) {

		CharacterInfo result = new CharacterInfo();

		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		result.color = characterColorP.getPropertyValue(mt);
		if(result.color == null) result.color = "#034ea2";
		result.goalDescription = goalDescriptionP.getPropertyValue(mt);
		if(result.goalDescription == null) result.goalDescription = "";
		result.focusDescription = characterDescriptionP.getPropertyValue(mt);
		if(result.focusDescription == null) result.focusDescription = "";
		result.textColor = characterTextColorP.getPropertyValue(mt);
		result.linkWithParent = !"false".equals(linkWithParentP.getPropertyValue(mt));
		result.linkGoalsAndSubmaps = "true".equals(goalSubmapP.getPropertyValue(mt));

		Collection<Base> subTypes = mt.getRelatedObjects(database, goalSubmapTypeR);
		if(subTypes.size() == 1) result.goalSubmapType = (ObjectType)subTypes.iterator().next();
		
		return result;
		
	}
	
	public ObjectType getLevelType(Database database) {
		Property level = Property.find(database, Property.LEVEL);
		String typeUUID = level.getPropertyValue(this);
		if(typeUUID != null) {
			return (ObjectType)database.find(typeUUID);
		} else {
			return null;
		}
	}
	
	public CharacterInfo getCharacterInfo(Database database) {
		
		Property level = Property.find(database, Property.LEVEL);
		String typeUUID = level.getPropertyValue(this);
		if(typeUUID != null) {
			ObjectType mt = (ObjectType)database.find(typeUUID);
			CharacterInfo ci = getCharacterInfo(database, mt);
			if(ci.goalDescription.isEmpty()) {
				Strategiakartta parent = getPossibleParent(database);
				if(parent != null) {
					CharacterInfo parentInfo = parent.getCharacterInfo(database);
					ci.goalDescription = parentInfo.focusDescription;
				}
			}
			return ci; 
		} else {
			CharacterInfo result = new CharacterInfo();
			result.focusDescription = "Strateginen tavoite";
			result.goalDescription = "";
			result.color = "#034ea2";
			result.textColor = "#fff";
			result.linkWithParent = false;
			result.linkGoalsAndSubmaps = false;
			return result;
		}
		
	}
	
	public void prepare(Main main) {
		
		Database database = main.getDatabase();
		
		for(Linkki l : alikartat) {
			Strategiakartta k = database.find(l.uuid);
			String text = k.getText(database);
			if(text.isEmpty()) text = k.getId(database);
			if(text.isEmpty()) text = "<anna teksti tai lyhytnimi kartalle>";
			l.text = text;
		}
		for(Linkki l : parents) {
			Strategiakartta k = database.find(l.uuid);
			String text = k.getText(database);
			if(text.isEmpty()) text = k.getId(database);
			if(text.isEmpty()) text = "<anna teksti tai lyhytnimi kartalle>";
			l.text = text;
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
		
		CharacterInfo info = getCharacterInfo(database);
		painopisteDescription = info.focusDescription;
		painopisteColor = info.color;
		painopisteTextColor = info.textColor;
		linkWithParent = info.linkWithParent;
		linkGoalsAndSubmaps = info.linkGoalsAndSubmaps;

		Strategiakartta parent = getPossibleParent(database);
		if(parent != null) {
			CharacterInfo parentInfo = parent.getCharacterInfo(database);
			tavoiteDescription = info.goalDescription.isEmpty() ? parentInfo.focusDescription : info.goalDescription;
			tavoiteColor = parentInfo.color;
			tavoiteTextColor = parentInfo.textColor;
		} else {
			tavoiteDescription = info.goalDescription.isEmpty() ? "Strateginen tavoite" : info.goalDescription;
			tavoiteColor = "#034ea2";
			tavoiteTextColor = "#fff";
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
	
	public Base currentLevel(Database database) {
		Property levelProperty = Property.find(database, Property.LEVEL);
		return levelProperty.getPropertyValueObject(database, this);
	}
	
	public void setCurrentLevel(Main main, Base level) {
		Property levelProperty = Property.find(main.getDatabase(), Property.LEVEL);
		levelProperty.set(main, this, level.uuid);
	}

	public static Collection<Base> availableLevels(Database database) {
        ObjectType levelType = ObjectType.find(database, ObjectType.LEVEL_TYPE);
        return database.instances(levelType);
	}

	public boolean linkGoalsToSubmaps(Database database) {

		Base level = currentLevel(database);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		String goalSubmapValue = goalSubmapP.getPropertyValue(level);
		boolean tavoiteSubmap = false;
		if("true".equals(goalSubmapValue)) tavoiteSubmap = true;
		return tavoiteSubmap;

	}
	
	public Base getPossibleSubmapType(Database database) throws Exception {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(this));

		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		Collection<Base> subTypes = level.getRelatedObjects(database, goalSubmapTypeR);
		if(subTypes.size() == 1) return subTypes.iterator().next();
		else if(subTypes.size() == 0) return null;
		else throw new Exception("Multiple submap types.");
		
	}
	

}
