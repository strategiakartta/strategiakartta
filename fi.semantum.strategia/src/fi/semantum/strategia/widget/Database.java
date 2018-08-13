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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import fi.semantum.strategia.Lucene;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.filter.FilterUtils;

public class Database implements Serializable {

	public static final String[] hues = new String[] {

		"#E03774",
		"#7BEB35",
		"#5CDBE8",
		"#323517",
		"#7A6CE7",
		"#EE9626",
		"#E4B8AF",
		"#4A214B",
		"#E5E67C",
		"#597EAE",
		"#792518",
		"#3FA348",
		"#DA3EE1",
		"#E44125",
		"#A761AB",
		"#87E5AC",
		"#838B23",
		"#BD8456",
		"#74625F",
		"#CAB9E5",
		"#C6E536",
		"#57999A",
		"#8AA978",
		"#D885A3",
		"#D1E2C5",
		"#4C5CAD",
		"#78B224",
		"#61E485",
		"#47705A",
		"#A03167",
		"#D67064",
		"#45262E",
		"#5D5A1A",
		"#E038B9",
		"#406578",
		"#A4D881",
		"#AE4F21",
		"#BB9223",
		"#86513B",
		"#9E9F90",
		"#A13EAD",
		"#9E87DC",
		"#3B762F",
		"#887B95",
		"#80734E",
		"#37C033",
		"#47A77B",
		"#C07AE9",
		"#E23697",
		"#51AAD8",
		"#DBD9DE",
		"#2E314C",
		"#273733",
		"#5882EA",
		"#DE6CCB",
		"#822738",
		"#895264",
		"#D63853",
		"#374F82",
		"#DBDCA0",
		"#D1AD83",
		"#B8E163",
		"#DB6094",
		"#2D5121",
		"#815C8D",
		"#372C68",
		"#936123",
		"#E85E4F",
		"#95CFCC",
		"#6C4EB3",
		"#D9C06E",
		"#B1AD26",
		"#89914E",
		"#962B7D",
		"#64DDC3",
		"#A858E2",
		"#E37A39",
		"#D89084",
		"#7097DE",
		"#A82B26",
		"#E9DE3A",
		"#491C14",
		"#CE6477",
		"#9FBCDB",
		"#703C88",
		"#EAAB67",
		"#A68D3E",
		"#EDBA38",
		"#CF98DD",
		"#E1A9CC",
		"#689A38",
		"#B6949C",
		"#7DE45E",
		"#641C42",
		"#E066EA",
		"#6C3F11",
		"#C6873D",
		"#DE73B4",
		"#C8C454",
		"#B97A1B"

	};

	private static final long serialVersionUID = 7219126520246069099L;

	public int tagNumber = 0;

	public Map<String,Base> objects = new HashMap<String,Base>();

	transient private Date lastModified;
	transient private Map<String,Tag> tagMap;
	transient private String databaseId = "database";

	public Account guest;
	public Account system;
	
	public Database(String databaseId) {
		this.databaseId = databaseId;
	}

	public String getDatabaseId() {
		return databaseId;
	}
	
	private void updateTags() {
		tagMap = new HashMap<String, Tag>();
		for(Tag t : Tag.enumerate(this)) tagMap.put(t.getId(this), t);
	}

	public String createHue() {
		String color = hues[tagNumber % hues.length];
		tagNumber++;
		return color;
	}
	
	public Tag getOrCreateTag(String id) {
		updateTags();
		Tag result = tagMap.get(id);
		if(result == null) {
			String color = createHue();
			result = Tag.create(this, id, id, color);
			updateTags();
		}
		return result;
	}

	public void save() {
		synchronized(Database.class) {
			try {
				File f = new File(Main.baseDirectory(), databaseId);
				FileOutputStream fileOut = new FileOutputStream(f);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this);
				out.close();
				fileOut.close();
				lastModified = new Date(f.lastModified());
				System.out.println("Serialized data is saved in " + f.getAbsolutePath() + " " + lastModified);
				
				touchBackup();
				
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
	}

	// 5 min backup
	private static final long BACKUP_CYCLE_MS = 5*60*1000;
	
	private void touchBackup() {
	
		try {
			
			File f = new File(Main.baseDirectory(), "backup-data-" + databaseId);
			if(!f.exists()) {
				f.createNewFile();
			}

			long now = new Date().getTime();
			long last = f.lastModified();
			
			if(now-last > BACKUP_CYCLE_MS) {

				f.setLastModified(now);

				String fileName = databaseId + "-" + UUID.randomUUID().toString();
				
				java.nio.file.Files.copy( 
						new java.io.File(Main.baseDirectory(), databaseId).toPath(), 
						new java.io.File(Main.baseDirectory(), fileName).toPath(),
						java.nio.file.StandardCopyOption.REPLACE_EXISTING,
						java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
						java.nio.file.LinkOption.NOFOLLOW_LINKS );

			}
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} catch (Throwable t) {

			t.printStackTrace();

		}

	}
	
	public static void validate(Main main) {

		boolean changed = false;
		Database database = main.getDatabase();
		List<Base> toBeRemoved = new ArrayList<Base>();
		for(Base b : database.enumerate()) {
			if(b instanceof ObjectType) continue;
			if(b instanceof Property) continue;
			if(b instanceof Relation) continue;
			if(b instanceof Tag) continue;
			if(b instanceof Datatype) continue;
			if(b instanceof Account) continue;
			if(b instanceof ResponsibilityModel) continue;
			if(b instanceof ResponsibilityInstance) continue;
			if(b instanceof TimeConfiguration) continue;
			try {
				database.getMap(b);
			} catch (Exception e) {
				toBeRemoved.add(b);
				changed = true;
			}
		}
		
		Set<String> enumerationNamesToRemove = new HashSet<String>();
		enumerationNamesToRemove.add("Hallituksen vuosikertomus");
		enumerationNamesToRemove.add("Kyllä/Ei");
		enumerationNamesToRemove.add("Valtion tulostietojärjestelmä");
		Set<Base> datatypesToRemove = new HashSet<Base>();
		
        List<Datatype> types = Datatype.enumerate(database);
        for(Datatype dt : types) {
        	if(dt instanceof EnumerationDatatype) {
        		String id = dt.getId(database);
        		if(enumerationNamesToRemove.contains(id)) {
        			datatypesToRemove.add(dt);
					changed = true;
        		}
        	}
        }
        
        for(Meter m : Meter.enumerate(database)) {
			Indicator i = m.getPossibleIndicator(database);
			if(i != null) {
				Datatype dt = i.getDatatype(database);
				if(datatypesToRemove.contains(dt)) {
					toBeRemoved.add(m);
					changed = true;
				}
			} else {
				toBeRemoved.add(m);
				changed = true;
			}
			Base owner = m.getOwner(database);
			if(owner instanceof Strategiakartta) {
				toBeRemoved.add(m);
				changed = true;
			}
        }
        
		for(Indicator i : Indicator.enumerate(database)) {
			Datatype datatype = i.getDatatype(database);
			if(datatypesToRemove.contains(datatype)) {
				for(Meter meter : Meter.enumerate(database)) {
					Indicator indicator = meter.getPossibleIndicator(database);
					if(i.equals(indicator)) {
						toBeRemoved.add(i);
						toBeRemoved.add(meter);
						changed = true;
					}
				}
			}
		}
		
		for(Base b : toBeRemoved) {
			database.remove(b);
		}
		
		for(Base b : datatypesToRemove) {
			database.remove(b);
		}

		if(changed) {
			database.save();
		}
		
	}
		
	public static void migrate(Main main, Map<String, EnumerationDatatype> enumerations) {
		
		Database database = main.getDatabase();
		
		main.account = Account.find(database, "System");
		
		TimeConfiguration tc = TimeConfiguration.getInstance(database);
		if(tc == null) {
			TimeConfiguration.create(database, "2016-2020");
		}
		
		boolean migrated = false;

		if(Relation.find(database, Relation.RESPONSIBILITY_INSTANCE) == null) {
			Relation.create(database, Relation.RESPONSIBILITY_INSTANCE);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.RESPONSIBILITY_MODEL) == null) {
			Relation.create(database, Relation.RESPONSIBILITY_MODEL);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.TAVOITE_SUBMAP) == null) {
			Relation.create(database, Relation.TAVOITE_SUBMAP);
			migrated = true;
		}


		
		if(Property.find(database, Property.CHARACTER_DESCRIPTION) == null) {
			Property.create(database, Property.CHARACTER_DESCRIPTION, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.GOAL_DESCRIPTION) == null) {
			Property.create(database, Property.GOAL_DESCRIPTION, null, false, Collections.<String>emptyList());
			migrated = true;
		}
		
		if(Property.find(database, Property.LINK_WITH_PARENT) == null) {
			Property.create(database, Property.LINK_WITH_PARENT, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.LINK_GOALS_AND_SUBMAPS) == null) {
			Property.create(database, Property.LINK_GOALS_AND_SUBMAPS, null, false, Collections.<String>emptyList());
			migrated = true;
		}
		
		if(Relation.find(database, Relation.RELATED_TO_TAG) == null) {
			Relation.create(database, Relation.RELATED_TO_TAG);
			migrated = true;
		}

		if(Property.find(database, Property.OWN_GOAL_TYPE) == null) {
			
			Property ownGoalTypeProperty = Property.create(database, Property.OWN_GOAL_TYPE, null, false, Collections.<String>emptyList());
			ObjectType painopiste = ObjectType.find(database, ObjectType.PAINOPISTE);
			migrated = true;
			
		}

		
		if(Relation.find(database, Relation.MONITORS_TAG) == null) {
			Relation.create(database, Relation.MONITORS_TAG);
			migrated = true;
		}
		
		if(Datatype.enumerate(database).isEmpty()) {
			createDatatypes(database);
			migrated = true;
		}

		// Existing datatypes
        List<Datatype> types = Datatype.enumerate(database);
        for(Datatype dt : types) {
        	if(dt instanceof EnumerationDatatype) {
        		EnumerationDatatype edt = (EnumerationDatatype)dt; 
        		EnumerationDatatype vals = enumerations.get(dt.getId(database));
	        	if(vals != null) {
	        		if(!vals.getValues().equals(edt.getValues())) {
	        			edt.replace(vals);
	        			migrated = true;
	        		}
	        		enumerations.remove(dt.getId(database));
	        	}
        	}
        }
        
        for(String newEnumeration : enumerations.keySet()) {
        	
        	EnumerationDatatype edt = enumerations.get(newEnumeration);
    		database.register(edt);
			migrated = true;

        }
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		for(Strategiakartta map : Strategiakartta.enumerate(database)) {
			ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
			if(level == null) {
				database.remove(map);
				migrated = true;
			}
		}
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property focusType = Property.find(database, Property.FOCUS_TYPE);
		
		for(Base levelType : Strategiakartta.availableLevels(database)) {
			String focusDescription = characterDescriptionP.getPropertyValue(levelType);
			if(focusDescription == null) {
				Base painopisteType = (Base)database.find(focusType.getPropertyValue(levelType));
				if(painopisteType != null) {
					String painopisteDescription = painopisteType.getText(database);
					characterDescriptionP.set(null, database, levelType, painopisteDescription);
					goalDescriptionP.set(null, database, levelType, "");
					String painopisteColor = characterColorP.getPropertyValue(painopisteType);
					characterColorP.set(null, database, levelType, painopisteColor);
					String painopisteTextColor = characterTextColorP.getPropertyValue(painopisteType);
					characterTextColorP.set(null, database, levelType, painopisteTextColor);
					migrated = true;
				}
			}
		}
		
		for(Strategiakartta map : Strategiakartta.enumerate(database)) {
			
			List<Linkki> ls = new ArrayList<Linkki>();
			for(Linkki l : map.alikartat) {
				Strategiakartta k = database.find(l.uuid);
				if(k == null) {
					ls.add(l);
				}
			}
			
			for(Linkki l : ls) {
				map.removeAlikartta(l.uuid);
				migrated = true;
			}

		}
		
		for(Tavoite t : Tavoite.enumerate(database)) {
			try {
				if(t.ensureImplementationMap(main))
					migrated = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
		for(Strategiakartta map : Strategiakartta.enumerate(database)) {
			try {
				Base sub = map.getPossibleSubmapType(database);
				if(sub != null) {
					Set<Strategiakartta> proper = new HashSet<Strategiakartta>();
					for(Tavoite t : map.tavoitteet) {
						try {
							Strategiakartta imp = t.getPossibleImplementationMap(database);
							if(imp != null) proper.add(imp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					for(Linkki l : map.alikartat) {
						Strategiakartta map2 = database.find(l.uuid);
						if(!proper.contains(map2)) {
							database.remove(map2);
							migrated = true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
//		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
//		for(Strategiakartta map : FilterUtils.filterByType(database, Strategiakartta.enumerate(database), ObjectType.VIRASTO)) {
//			if(map.voimavarat == null) {
//				Tavoite t = Tavoite.create(database, map, "Voimavarat");
//				map.removeTavoite(database, t);
//				t.assertRelatedTags(database, voimavarat);
//				map.voimavarat = t;
//				migrated = true;
//			}
//		}
		
		// Migration
		for(Base b : new ArrayList<Base>(database.enumerate())) {
			if(b.migrate(main))
				migrated = true;
//			if(migrated) {
//				b.migrate(main);
//			}
		}

		if(migrated) {
			database.save();
		}
		
		main.account = null;
		
	}
	
	public static Database load(Main main, String databaseId) {

		Database result = null;

		synchronized(Database.class) {

			try {
				
				Map<String,EnumerationDatatype> enumerations = new HashMap<String, EnumerationDatatype>();
				
				try {
					File file = new File(Main.baseDirectory(), "database.xlsx");
					FileInputStream fis = new FileInputStream(file);
					Workbook book = WorkbookFactory.create(fis);
					fis.close();
					Sheet sheet = book.getSheetAt(0);
					for(int rowN = sheet.getFirstRowNum();rowN<=sheet.getLastRowNum();rowN++) {
						Row row = sheet.getRow(rowN);
						Cell cell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
						if(cell != null) {
							if("Monivalinta".equals(cell.toString())) {
								Cell id = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
								if(id == null) continue;
								Cell traffic = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
								if(traffic == null) continue;
								int count = row.getLastCellNum()-3;
								if(traffic.toString().length() != count) continue;
								
								List<String> values = new ArrayList<String>();
								for(int i=0;i<count;i++) {
									Cell val = row.getCell(3+i, Row.RETURN_BLANK_AS_NULL);
									if(val != null)
										values.add(val.toString());
								}
								enumerations.put(id.toString(), new EnumerationDatatype(result, id.toString(), values, traffic.toString()));
								
							}
						}
					}
					
				} catch (Exception e) {
				}
				
				File f = new File(Main.baseDirectory(), databaseId);
				FileInputStream fileIn = new FileInputStream(f);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				result = (Database) in.readObject();
				in.close();
				fileIn.close();

				result.databaseId = databaseId;

				main.setDatabase(result);
				
				migrate(main, enumerations);
				validate(main);
				
				result.lastModified = new Date(f.lastModified());
				
			} catch(IOException i) {
				
				i.printStackTrace();
				result = create(main, databaseId);
				
			} catch(ClassNotFoundException c) {
				
				System.out.println("Database class not found");
				c.printStackTrace();
				result = create(main, databaseId);
				
			}
			
			result.touchBackup();

			result.updateTags();

			try {
			
				if(!Lucene.indexExists(databaseId)) {
					
					Lucene.startWrite(databaseId);
					for(Base b : result.enumerate()) {
						Lucene.set(databaseId, b.uuid, b.searchText(result));
					}
					Lucene.endWrite();
					
				}
				
			} catch (Throwable t) {
				
				t.printStackTrace();
			
			}

		}
		
		return result;

	}

	public boolean checkChanges() {
		
		File f = new File(Main.baseDirectory(), databaseId);
		Date d = new Date(f.lastModified());
		
		int comp = d.compareTo(lastModified);
		
		if(comp > 0) {
			//System.err.println("Changed: " + lastModified + " vs. " + d);
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T find(String uuid) {
		Base b = objects.get(uuid);
		if(b != null) return (T)b;
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T findByI(String id) {
		for(Base b : objects.values()) {
			if(id.equals(b.getId(this))) return (T)b;
		}
		return null;
	}

	public Collection<Base> instances(ObjectType type) {
		Property typeProperty = Property.find(this, Property.TYPE);
		ArrayList<Base> result = new ArrayList<Base>();
		for(Base b : objects.values()) {
			Base t = typeProperty.getPropertyValueObject(this, b);
			if(t != null) {
				if(type.uuid == t.uuid) {
					result.add(b);
				}
			}
		}
		return result;
	}
	
	public Collection<Base> enumerate() {
		return objects.values();
	}

	public Strategiakartta getRoot() {
		for(Strategiakartta map : Strategiakartta.enumerate(this)) {
			if(map.parents.length == 0) return map;
		}
		return null;
	}

	private static final Collection<Pair> EMPTY = Collections.emptyList();

	public static Pair[] mapProperties(Database database, Base level) {

		Account owner = Account.find(database, "System");

		Pair p1 = Pair.make(Property.find(database, Property.LEVEL).uuid, level.uuid);
		Pair p3 = Pair.make(Property.find(database, Property.OWNER).uuid, owner.getId(database));
		Pair p4 = Pair.make(Property.find(database, Property.EMAIL).uuid, "");
		Pair p5 = Pair.make(Property.find(database, Property.CHANGED_ON).uuid, Utils.sdf.format(new Date()));
		Pair p6 = Pair.make(Property.find(database, Property.TTL).uuid, "30");

		return new Pair[] { p1, p3, p4, p5, p6 };

	}

	public static Pair[] goalProperties(Database database, Strategiakartta map) {

		Account owner = Account.find(database, "System");

//		ObjectType toimenpide = ObjectType.find(database, ObjectType.TOIMENPIDE);
//		ObjectType toimialanToimenpide = ObjectType.find(database, ObjectType.TOIMIALAN_TOIMENPIDE);
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);

		ArrayList<Pair> result = new ArrayList<Pair>();

//		if(toimialanToimenpide.uuid.equals(goalTypeUUID) || toimenpide.uuid.equals(goalTypeUUID))

    	result.add(Pair.make(Property.find(database, Property.AIKAVALI).uuid, "Kaikki"));
		result.add(Pair.make(Property.find(database, Property.OWNER).uuid, owner.uuid));
		result.add(Pair.make(Property.find(database, Property.EMAIL).uuid, ""));
		result.add(Pair.make(Property.find(database, Property.CHANGED_ON).uuid, Utils.sdf.format(new Date())));
		result.add(Pair.make(Property.find(database, Property.TTL).uuid, "30"));

		return result.toArray(new Pair[result.size()]);
		
	}

	public static Pair[] focusProperties(Database database, Strategiakartta map) {

		Account owner = Account.find(database, "System");

//		ObjectType toimenpide = ObjectType.find(database, ObjectType.TOIMENPIDE);
//		ObjectType toimialanToimenpide = ObjectType.find(database, ObjectType.TOIMIALAN_TOIMENPIDE);
//		ObjectType vastuuhenkilo = ObjectType.find(database, ObjectType.VASTUUHENKILO);
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

//		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
//		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);

		ArrayList<Pair> result = new ArrayList<Pair>();

//		if(toimenpide.uuid.equals(focusTypeUUID) || toimialanToimenpide.uuid.equals(focusTypeUUID) || vastuuhenkilo.uuid.equals(focusTypeUUID))
		
		result.add(Pair.make(Property.find(database, Property.AIKAVALI).uuid, Property.AIKAVALI_KAIKKI));
		result.add(Pair.make(Property.find(database, Property.OWNER).uuid, owner.uuid));
		result.add(Pair.make(Property.find(database, Property.EMAIL).uuid, ""));
		result.add(Pair.make(Property.find(database, Property.CHANGED_ON).uuid, Utils.sdf.format(new Date())));
		result.add(Pair.make(Property.find(database, Property.TTL).uuid, "30"));

		return result.toArray(new Pair[result.size()]);

	}
	
	private static void createDatatypes(Database database) {
		database.register(new NumberDatatype(database));
	}
	
	private static Database create(Main main, String databaseId) {

		try {
			
			Lucene.startWrite(databaseId);

			Strategiakartta hallinnonala;

			Database result = new Database(databaseId);
			result.databaseId = databaseId;
			main.setDatabase(result);

			TimeConfiguration tc = TimeConfiguration.getInstance(result);
			if(tc == null) {
				TimeConfiguration.create(result, "2016-2020");
			}

			Relation.create(result, Relation.RELATED_TO_TAG);

			Relation.create(result, Relation.IMPLEMENTS);
			Relation.create(result, Relation.COPY);
			Relation.create(result, Relation.MEASURES);
			Relation.create(result, Relation.RESPONSIBILITY_INSTANCE);
			Relation.create(result, Relation.RESPONSIBILITY_MODEL);
			
			Relation allowsSubmap = Relation.create(result, Relation.ALLOWS_SUBMAP);
			Relation.create(result, Relation.MONITORS_TAG);
			
			Relation.create(result, Relation.TAVOITE_SUBMAP);

			Property goalTypeProperty = Property.create(result, Property.GOAL_TYPE, null, false, Collections.<String>emptyList());
			Property ownGoalTypeProperty = Property.create(result, Property.OWN_GOAL_TYPE, null, false, Collections.<String>emptyList());
			Property focusTypeProperty = Property.create(result, Property.FOCUS_TYPE, null, false, Collections.<String>emptyList());
			Property characterColor = Property.create(result, Property.CHARACTER_COLOR, null, false, Collections.<String>emptyList());
			Property characterTextColor = Property.create(result, Property.CHARACTER_TEXT_COLOR, null, false, Collections.<String>emptyList());

			Property.create(result, Property.CHARACTER_DESCRIPTION, null, false, Collections.<String>emptyList());
			Property.create(result, Property.GOAL_DESCRIPTION, null, false, Collections.<String>emptyList());
			Property.create(result, Property.LINK_WITH_PARENT, null, false, Collections.<String>emptyList());
			Property.create(result, Property.LINK_GOALS_AND_SUBMAPS, null, false, Collections.<String>emptyList());			
			
//			Property manyImplements = Property.create(result, Property.MANY_IMPLEMENTS, null, true, Collections.<String>emptyList());
//			Property manyImplementor = Property.create(result, Property.MANY_IMPLEMENTOR, null, true, Collections.<String>emptyList());
			
			Property type = Property.create(result, Property.TYPE, null, false, Collections.<String>emptyList());

			ObjectType levelType = ObjectType.create(result, ObjectType.LEVEL_TYPE);

			ObjectType strateginenTavoite = ObjectType.create(result, ObjectType.STRATEGINEN_TAVOITE);
			strateginenTavoite.properties.add(characterColor.make("#034ea2"));
			strateginenTavoite.properties.add(characterTextColor.make("#fff"));
//			strateginenTavoite.properties.add(manyImplements.make("false")); // toteuttaa useampaa
//			strateginenTavoite.properties.add(manyImplementor.make("true")); // useampi toteuttaa
			
			ObjectType painopiste = ObjectType.create(result, ObjectType.PAINOPISTE);
			painopiste.properties.add(characterColor.make("#3681D5"));
			painopiste.properties.add(characterTextColor.make("#000"));
//			painopiste.properties.add(manyImplements.make("false")); // toteuttaa useampaa
//			painopiste.properties.add(manyImplementor.make("true")); // useampi toteuttaa

			/*
			ObjectType tulostavoite = ObjectType.create(result, ObjectType.TULOSTAVOITE);
			tulostavoite.properties.add(characterColor.make("#69B4ff"));
			tulostavoite.properties.add(characterTextColor.make("#000"));
			tulostavoite.properties.add(manyImplements.make("false")); // toteuttaa useampaa
			tulostavoite.properties.add(manyImplementor.make("true")); // useampi toteuttaa
			
			ObjectType hallinnonalanPainopiste = ObjectType.create(result, ObjectType.HALLINNONALAN_PAINOPISTE);
			hallinnonalanPainopiste.properties.add(characterColor.make("#3681D5"));
			hallinnonalanPainopiste.properties.add(characterTextColor.make("#000"));
			hallinnonalanPainopiste.properties.add(manyImplements.make("false")); // toteuttaa useampaa
			hallinnonalanPainopiste.properties.add(manyImplementor.make("true")); // useampi toteuttaa
			
			ObjectType toimenpide = ObjectType.create(result, ObjectType.TOIMENPIDE);
			toimenpide.properties.add(characterColor.make("#9ce7ff"));
			toimenpide.properties.add(characterTextColor.make("#000"));
			toimenpide.properties.add(manyImplements.make("false")); // toteuttaa useampaa
			toimenpide.properties.add(manyImplementor.make("true")); // useampi toteuttaa

			ObjectType toimialanToimenpide = ObjectType.create(result, ObjectType.TOIMIALAN_TOIMENPIDE);
			toimialanToimenpide.properties.add(characterColor.make("#82CDFF"));
			toimialanToimenpide.properties.add(characterTextColor.make("#000"));
			toimialanToimenpide.properties.add(manyImplements.make("false")); // toteuttaa useampaa
			toimialanToimenpide.properties.add(manyImplementor.make("true")); // useampi toteuttaa
			
			ObjectType vastuuhenkilo = ObjectType.create(result, ObjectType.VASTUUHENKILO);
			vastuuhenkilo.properties.add(characterColor.make("#cfffff"));
			vastuuhenkilo.properties.add(characterTextColor.make("#000"));
			vastuuhenkilo.properties.add(manyImplements.make("true")); // toteuttaa useampaa
			vastuuhenkilo.properties.add(manyImplementor.make("true")); // useampi toteuttaa
			*/
			
			ObjectType typeHallinnonala = ObjectType.create(result, ObjectType.HALLINNONALA);
			typeHallinnonala.properties.add(type.make(levelType.uuid));
			typeHallinnonala.properties.add(goalTypeProperty.make(strateginenTavoite.uuid));
			typeHallinnonala.properties.add(focusTypeProperty.make(painopiste.uuid));
			typeHallinnonala.properties.add(characterColor.make("#3681D5"));
			typeHallinnonala.properties.add(characterTextColor.make("#000"));
			
			Property characterDescriptionP = Property.find(result, Property.CHARACTER_DESCRIPTION);
			Property goalDescriptionP = Property.find(result, Property.GOAL_DESCRIPTION);

			typeHallinnonala.properties.add(characterDescriptionP.make("Painopiste"));
			typeHallinnonala.properties.add(goalDescriptionP.make("Strateginen tavoite"));

			
			/*
			ObjectType typeVirasto = ObjectType.create(result, ObjectType.VIRASTO);
			typeVirasto.properties.add(type.make(levelType.uuid));
			typeVirasto.properties.add(ownGoalTypeProperty.make(painopiste.uuid));
			typeVirasto.properties.add(goalTypeProperty.make(hallinnonalanPainopiste.uuid));
			typeVirasto.properties.add(focusTypeProperty.make(tulostavoite.uuid));
			typeVirasto.properties.add(characterColor.make("#BA6446"));
			typeVirasto.properties.add(characterTextColor.make("#000"));

			// Muissa virastoissa kuin LVM
			ObjectType typeToimiala = ObjectType.create(result, ObjectType.TOIMIALA);
			typeToimiala.properties.add(type.make(levelType.uuid));
			typeToimiala.properties.add(goalTypeProperty.make(tulostavoite.uuid));
			typeToimiala.properties.add(focusTypeProperty.make(toimialanToimenpide.uuid));
			typeToimiala.properties.add(characterColor.make("#AA6446"));
			typeToimiala.properties.add(characterTextColor.make("#000"));

			// LVM:n osasto
			ObjectType typeLVMOsasto = ObjectType.create(result, ObjectType.LVM_OSASTO);
			typeLVMOsasto.properties.add(type.make(levelType.uuid));
			typeLVMOsasto.properties.add(goalTypeProperty.make(tulostavoite.uuid));
			typeLVMOsasto.properties.add(focusTypeProperty.make(toimenpide.uuid));
			typeLVMOsasto.properties.add(characterColor.make("#9A6446"));
			typeLVMOsasto.properties.add(characterTextColor.make("#000"));

			// Muiden virastojen osasto
			ObjectType typeVirastoOsasto = ObjectType.create(result, ObjectType.VIRASTO_OSASTO);
			typeVirastoOsasto.properties.add(type.make(levelType.uuid));
			typeVirastoOsasto.properties.add(ownGoalTypeProperty.make(toimenpide.uuid));
			typeVirastoOsasto.properties.add(goalTypeProperty.make(toimialanToimenpide.uuid));
			typeVirastoOsasto.properties.add(focusTypeProperty.make(toimenpide.uuid));
			typeVirastoOsasto.properties.add(characterColor.make("#9A6446"));
			typeVirastoOsasto.properties.add(characterTextColor.make("#000"));

			// Yksikkö keskellä organisaatiota
			ObjectType typeKokoavaYksikko = ObjectType.create(result, ObjectType.KOKOAVA_YKSIKKO);
			typeKokoavaYksikko.properties.add(type.make(levelType.uuid));
			typeKokoavaYksikko.properties.add(ownGoalTypeProperty.make(toimenpide.uuid));
			typeKokoavaYksikko.properties.add(goalTypeProperty.make(toimialanToimenpide.uuid));
			typeKokoavaYksikko.properties.add(focusTypeProperty.make(toimenpide.uuid));
			typeKokoavaYksikko.properties.add(characterColor.make("#8A6446"));
			typeKokoavaYksikko.properties.add(characterTextColor.make("#000"));

			// Yksikkö organisaation pohjalla
			ObjectType typeToteuttavaYksikko = ObjectType.create(result, ObjectType.TOTEUTTAVA_YKSIKKO);
			typeToteuttavaYksikko.properties.add(type.make(levelType.uuid));
			typeToteuttavaYksikko.properties.add(goalTypeProperty.make(toimenpide.uuid));
			typeToteuttavaYksikko.properties.add(focusTypeProperty.make(vastuuhenkilo.uuid));
			typeToteuttavaYksikko.properties.add(characterColor.make("#8A6446"));
			typeToteuttavaYksikko.properties.add(characterTextColor.make("#000"));

			ObjectType typeRyhma = ObjectType.create(result, ObjectType.RYHMA);
			typeRyhma.properties.add(type.make(levelType.uuid));
			typeRyhma.properties.add(goalTypeProperty.make(toimenpide.uuid));
			typeRyhma.properties.add(focusTypeProperty.make(vastuuhenkilo.uuid));
			typeRyhma.properties.add(characterColor.make("#AA6446"));
			typeRyhma.properties.add(characterTextColor.make("#000"));
			*/

//			typeHallinnonala.addRelation(allowsSubmap, typeVirasto);
//			
//			typeVirasto.addRelation(allowsSubmap, typeLVMOsasto);
//			typeVirasto.addRelation(allowsSubmap, typeToimiala);
//			
//			typeToimiala.addRelation(allowsSubmap, typeVirastoOsasto);
//			typeToimiala.addRelation(allowsSubmap, typeKokoavaYksikko);
//			typeToimiala.addRelation(allowsSubmap, typeRyhma);
//
//			typeLVMOsasto.addRelation(allowsSubmap, typeToteuttavaYksikko);
//			
//			typeVirastoOsasto.addRelation(allowsSubmap, typeToteuttavaYksikko);
//
//			typeKokoavaYksikko.addRelation(allowsSubmap, typeRyhma);

			createDatatypes(result);

			ObjectType accountType = ObjectType.create(result, ObjectType.ACCOUNT);

			Property.create(result, Property.LEVEL, levelType.uuid, false, Collections.<String>emptyList());
			Property.create(result, Property.AIKAVALI, null, false, Collections.<String>emptyList());
			Property.create(result, Property.EMAIL, null, false, Collections.<String>emptyList());
			Property.create(result, Property.OWNER, accountType.uuid, false, Collections.<String>emptyList());
			Property.create(result, Property.CHANGED_ON, null, true, Collections.<String>emptyList());
			Property.create(result, Property.TTL, null, false, Collections.<String>emptyList());

			result.guest = Account.create(result, "Guest", "", Utils.hash(""));
			result.system = Account.create(result, "System", "contact@semantum.fi", Utils.hash(""));
			result.system.setAdmin(true);

			Tag LIIKENNE = result.getOrCreateTag(Tag.LIIKENNE);
			LIIKENNE.modifyText(main, result.system, "Liikenne");
			Tag VIESTINTA = result.getOrCreateTag(Tag.VIESTINTA);
			VIESTINTA.modifyText(main, result.system, "Viestintä");

			hallinnonala = Strategiakartta.create(main, "hallinnonala",
					"LVM:n hallinnonala",
					"Hyvinvointia ja kilpailukykyä hyvillä yhteyksillä", EMPTY, mapProperties(result, typeHallinnonala));

			result.system.rights.add(new Right(hallinnonala, true, true));
			result.guest.rights.add(new Right(hallinnonala, false, false));

			//addTemplate(main, result, hallinnonala);

			result.prepareAll(main);
			result.save();

			Lucene.endWrite();

			return result;

		} catch (IOException e) {
			
			throw new Error(e);
			
		}

	}

	public static class TagMatch {
		public Strategiakartta kartta;
		public Tavoite tavoite;
		public Painopiste painopiste;
		public Set<String> tags;
	} 

	private Set<String> matchTags(Set<String> any, Collection<String> all, Base container) {

		// First check all required tags
		Set<String> contained = new HashSet<String>();
		for(Tag t : container.getRelatedTags(this))
			contained.add(t.uuid);

		contained.retainAll(all);
		if(contained.size() != all.size()) return Collections.emptySet();

		// Then collect overlapping tags
		Set<String> overlap = new HashSet<String>();
		for(Tag t : container.getRelatedTags(this)) {
			if(any.contains(t.uuid)) overlap.add(t.uuid);
		}
		return overlap;

	}

	public Collection<TagMatch> findByTags(Collection<String> atLeastOne, Collection<String> all) {

		Set<String> tagSet = new HashSet<String>();
		for(String tag : atLeastOne) tagSet.add(tag);
		List<TagMatch> result = new ArrayList<TagMatch>();
		for(Strategiakartta map : Strategiakartta.enumerate(this)) {

			for(Tavoite t : map.tavoitteet) {

				Set<String> overlap = matchTags(tagSet, all, t);
				if(!overlap.isEmpty()) {
					TagMatch m = new TagMatch();
					m.kartta = map;
					m.tavoite = t;
					m.painopiste = null;
					m.tags = overlap;
					result.add(m);
				}

				for(Painopiste p : t.painopisteet) {

					overlap = matchTags(tagSet, all, p);
					if(!overlap.isEmpty()) {
						TagMatch m = new TagMatch();
						m.kartta = map;
						m.tavoite = t;
						m.painopiste = p;
						m.tags = overlap;
						result.add(m);
					}

				}
			}
		}

		return result;

	}

	public Strategiakartta newMap(Main main, Strategiakartta parent, String id, String name, Base level) {

		Strategiakartta newMap = Strategiakartta.create(main, id, name, "", EMPTY, mapProperties(this, level));
		if(parent != null) {
			parent.addAlikartta(newMap);
			newMap.showVision = parent.showVision; 
		}
		return newMap;

	}

	public void remove(Strategiakartta map) {
		if(map.parents.length == 1) {
			Strategiakartta parent = find(map.parents[0].uuid);
			parent.removeAlikartta(map.uuid);
		}
		for(Linkki l : map.alikartat) {
			Strategiakartta child = find(l.uuid);
			remove(child);
		}
		objects.remove(map.uuid);
	}
	
	public void remove(Base base) {
		objects.remove(base.uuid);
	}

	public void assertTags(Collection<String> tagName) {
		for(String tag : tagName) getOrCreateTag(tag);
	}

	public void register(Base b) {

		String className = b.getClass().getSimpleName();
		ObjectType t = ObjectType.find(this, className);
		if(t != null)
			b.properties.add(Pair.make(Property.find(this, Property.TYPE).uuid, t.uuid));

		Property aika = Property.find(this, Property.AIKAVALI);
		String validity = aika.getPropertyValue(b);
		if(validity == null)
			aika.set(null, this, b, Property.AIKAVALI_KAIKKI);

		objects.put(b.uuid, b);
		try {
			Lucene.set(databaseId, b.uuid, b.searchText(this));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void prepareAll(Main main) {
		for(Strategiakartta map : Strategiakartta.enumerate(this)) {
			map.prepare(main);
		}
	}

	public Tavoite getTavoite(Painopiste b) {
		for(Strategiakartta map : Strategiakartta.enumerate(this)) {
			for(Tavoite t : map.tavoitteet) {
				for(Painopiste p : t.painopisteet) {
					if(p.uuid.equals(b.uuid)) {
						return t;
					}
				}
			}
			if(map.voimavarat != null) {
				for(Painopiste p : map.voimavarat.painopisteet) {
					if(p.uuid.equals(b.uuid)) {
						return map.voimavarat;
					}
				}
			}
		}
		return null;
	}
	
	public Strategiakartta getMap(Base b) throws RuntimeException {
		if (b instanceof Strategiakartta) {
			return (Strategiakartta)b;
		}
		if (b instanceof Tavoite) {
			for(Strategiakartta map : Strategiakartta.enumerate(this)) {
				for(Tavoite t : map.tavoitteet) {
					if(t.uuid.equals(b.uuid))
						return map;
				}
				if(map.voimavarat != null && map.voimavarat.uuid.equals(b.uuid))
					return map;
			}
		} else if (b instanceof Painopiste) {
			Strategiakartta result = null;
			for(Strategiakartta map : Strategiakartta.enumerate(this)) {
				for(Tavoite t : map.tavoitteet) {
					for(Painopiste p : t.painopisteet) {
						if(p.uuid.equals(b.uuid)) {
							if(result != null && result != map) return null;
							result = map;
						}
					}
				}
				if(map.voimavarat != null) {
					for(Painopiste p : map.voimavarat.painopisteet) {
						if(p.uuid.equals(b.uuid)) {
							if(result != null && result != map) return null;
							result = map;
						}
					}
				}
			}
			return result;
		} else if (b instanceof Meter) {
			for(Base b2 : enumerate()) {
				for(Meter m : b2.getMeters(this)) {
					if(m.equals(b))
						return getMap(b2);
				}
			}
		} else if (b instanceof Indicator) {
			for(Base b2 : enumerate()) {
				for(Indicator i : b2.getIndicators(this)) {
					if(i.equals(b)) {
						return getMap(b2);
					}
				}
			}
			for(Meter m : Meter.enumerate(this)) {
				Indicator i = m.getPossibleIndicator(this);
				if(b.equals(i)) {
					return getMap(m);
				}
			}
		}
		throw new RuntimeException("No map for " + b);
	}

	public String getType(Base b) {
		if(b instanceof Strategiakartta) {
			return "Strategiakartta";
		} else if (b instanceof Tavoite) {
			Strategiakartta map = getMap(b);
			return map.tavoiteDescription;
		} else if (b instanceof Painopiste) {
			Strategiakartta map = getMap(b);
			return map.painopisteDescription;
		} else {
			return b.getClass().getSimpleName(); 
		}
	}

	public Collection<Base> getInverse(Base b, Relation r) {
		Set<Base> result = new HashSet<Base>();
		for(Base b2 : enumerate()) {
			for(Base b3 : b2.getRelatedObjects(this, r))
				if(b3.equals(b))
					result.add(b2);
		}
		return result;
	}
	
	public Base getDefaultParent(Base b) {
		if(b instanceof Strategiakartta) {
			Strategiakartta s = (Strategiakartta)b;
			return s.getPossibleParent(this);
		} else if (b instanceof Tavoite) {
			return getMap(b);
		} else if (b instanceof Painopiste) {
			return getTavoite((Painopiste)b);
		} else {
			return null;
		}
	}
	
	public List<Base> getDefaultPath(Base b) {
		Base parent = getDefaultParent(b);
		if(parent == null) {
			ArrayList<Base> result = new ArrayList<Base>();
			result.add(b);
			return result;
		} else {
			List<Base> parentPath = getDefaultPath(parent);
			parentPath.add(b);
			return parentPath;
		}
	}

}
