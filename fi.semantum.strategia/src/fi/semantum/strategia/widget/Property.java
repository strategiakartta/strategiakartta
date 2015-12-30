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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;

public class Property extends Base {

	private static final long serialVersionUID = -1598499411379047877L;

	public static final String LEVEL = "Organisaatiotaso";
	public static final String AIKAVALI = "Voimassaolo";
	public static final String OWNER = "Vastuuhenkilö";
	public static final String EMAIL = "Seuraajat (email)";
	public static final String CHANGED_ON = "Viimeisin muutos";
	public static final String TTL = "Päivitysvaatimus (päivää)";

	public static final String OWN_GOAL_TYPE = "Own Goal Type";
	public static final String GOAL_TYPE = "Goal Type";
	public static final String FOCUS_TYPE = "Focus Type";
	public static final String CHARACTER_COLOR = "Characteristic Color";
	public static final String CHARACTER_TEXT_COLOR = "Characteristic Text Color";

	public static final String MANY_IMPLEMENTOR = "Many Implementor";
	public static final String MANY_IMPLEMENTS = "Many Implements";

	public static final String TYPE = "Tyyppi";

	public static final String AIKAVALI_KAIKKI = "-";

	final public boolean readOnly;
	final private String objectType;
	private List<String> enumeration;

	public static Property create(Database database, String id, String text, String objectType, boolean readOnly, List<String> enumeration) {
		Property p = new Property(id, text, objectType, readOnly, enumeration);
		database.register(p);
		return p;
	}

	public static Property create(Database database, String name, String objectType, boolean readOnly, List<String> enumeration) {
		Property p = new Property(name, name, objectType, readOnly, enumeration);
		database.register(p);
		return p;
	}

	private Property(String id, String text, String objectType, boolean readOnly, List<String> enumeration) {
		super(UUID.randomUUID().toString(), id, text);
		this.objectType = objectType;
		this.readOnly = readOnly;
		this.enumeration = enumeration;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}

	private List<Pair> getObjectEnumeration(Database database) {
		Property type = Property.find(database, Property.TYPE);
		ArrayList<Pair> result = new ArrayList<Pair>();
		for(Base b : database.objects.values()) {
			String uuid = type.getPropertyValue(b);
			if(uuid == null) continue;
			if(objectType.equals(uuid))
				result.add(Pair.make(b.uuid, b.getId(database)));
		}
		return result;

	}

	public List<String> getEnumeration(Database database) {
		if(objectType != null) {
			ArrayList<String> result = new ArrayList<String>();
			for(Pair p : getObjectEnumeration(database)) {
				result.add(p.second);
			}
			return result;
		}
		return enumeration;
	}

	public String getEnumerationValue(Database database, String value) {
		if(objectType != null) {
			Base b = database.find(value);
			if(b == null) return "Invalid enumeration value " + value;
			return b.getId(database);
		} else {
			return value;
		}
	}

	public static Property find(Database database, String name) {

		for(Base b : database.objects.values()) {
			if(b instanceof Property) {
				Property p = (Property)b;
				if(name.equals(p.getText(database))) return p;
			}
		}
		return null;

	}

	public boolean hasProperty(Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) return true;
		}
		return false;
	}

	public boolean hasPropertyValue(Base b, String value) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				if(value.equals(p.second))
					return true;
			}
		}
		return false;
	}

	public String getPropertyValue(Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				return p.second;
			}
		}
		return null;
	}

	public <T extends Base> T getPropertyValueObject(Database database, Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				return database.find(p.second);
			}
		}
		return null;
	}

	public boolean set(Main main, Base b, String value) {
		return set(main, main.getDatabase(), b, value);
	}

	public boolean set(Main main, Database db, Base b, String value) {

		Pair exist = null;
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				exist = p;
			}
		}

		if(exist != null && exist.equals(value)) return false;

		if(main != null) {
			if(!b.modified(main)) return false;
		}

		if(exist != null) b.properties.remove(exist);
		if(objectType != null) {
			for(Pair p : getObjectEnumeration(db)) {
				if(p.second.equals(value)) {
					b.properties.add(Pair.make(uuid, p.first));
					return true;
				}
			}
		} else {
			b.properties.add(Pair.make(uuid, value));
		}

		return true;

	}

	public Pair make(String value) {
		return Pair.make(uuid, value);
	}

	public void setEnumeration(List<String> values) {
		enumeration = values;
	}

	public static void updateProperties(final Main main, final Base base, boolean canWrite) {

		final Database database = main.getDatabase();
		
		String headerText = main.getUIState().currentItem.getCaption(database);
		main.propertyCells.add(Utils.excelRow(headerText));
		Label header = new Label(headerText);
		header.setWidth("800px");
		header.addStyleName("propertyHeader");
		header.addStyleName(ValoTheme.LABEL_HUGE);
		header.addStyleName(ValoTheme.LABEL_BOLD);
		main.properties.addComponent(header);
		main.properties.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

		ArrayList<Pair> sorted = new ArrayList<Pair>(main.getUIState().currentItem.properties);
		Collections.sort(sorted, new Comparator<Pair>() {

			@Override
			public int compare(Pair arg0, Pair arg1) {

				final Property p0 = database.find(arg0.first);
				final Property p1 = database.find(arg1.first);
				return p0.getId(database).compareTo(p1.getId(database));

			}

		});

		Property typeProperty = Property.find(database, Property.TYPE);

		for (Pair pair : sorted) {

			// Skip type
			if(typeProperty.uuid.equals(pair.first)) continue;

			final Property p = database.find(pair.first);
			String value = pair.second;
			final HorizontalLayout hl = new HorizontalLayout();
			hl.setSpacing(true);
			String label = p.getText(database);
			main.propertyCells.add(Utils.excelRow(label, value));
			Label l = new Label(label);
			l.setWidth("450px");
			l.addStyleName("propertyName");
			hl.addComponent(l);
			List<String> enumeration = p.getEnumeration(database);
			if (enumeration.isEmpty()) {
				final TextField tf = new TextField();
				tf.setValue(value);
				tf.setWidth("350px");
				hl.addComponent(tf);
				hl.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
				tf.setReadOnly(p.readOnly);
				tf.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = 7729833503749464603L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						Utils.loseFocus(hl);
						if(p.set(main, main.getUIState().currentItem, tf.getValue()))
							Updates.update(main, true);
					}
				});
				tf.setReadOnly(!canWrite);
			} else {
				final ComboBox combo = new ComboBox();
				combo.setWidth("350px");
				combo.setInvalidAllowed(false);
				combo.setNullSelectionAllowed(false);
				for (String e : enumeration) {
					combo.addItem(e);
				}
				combo.select(p.getEnumerationValue(database, value));
				combo.setPageLength(0);
				combo.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = 3511164709346832901L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						Utils.loseFocus(hl);
						if(p.set(main, main.getUIState().currentItem, combo.getValue().toString()))
							Updates.update(main, true);
					}
				});
				combo.setReadOnly(!canWrite);
				hl.addComponent(combo);
				hl.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
			}
			hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
			main.properties.addComponent(hl);
			main.properties.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
		}

	}
	

	public static void createProperties(Database database, Strategiakartta map, Base b) {
		if (b instanceof Tavoite) {
			for (Pair p : Database.goalProperties(database, map))
				b.properties.add(p);
		} else if (b instanceof Painopiste) {
			for (Pair p : Database.focusProperties(database, map))
				b.properties.add(p);

		}
	}	

}
