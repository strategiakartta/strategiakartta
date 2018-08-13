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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.mail.MessagingException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main.TimeInterval;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Datatype;
import fi.semantum.strategia.widget.EnumerationDatatype;
import fi.semantum.strategia.widget.Indicator;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Pair;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Relation;
import fi.semantum.strategia.widget.ResponsibilityInstance;
import fi.semantum.strategia.widget.ResponsibilityModel;
import fi.semantum.strategia.widget.Right;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;
import fi.semantum.strategia.widget.Tavoite;
import fi.semantum.strategia.widget.TimeConfiguration;

public class Utils {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String hash(String plainText) {
	
    	Formatter formatter = new Formatter();

		try {
	    	
	    	MessageDigest md5  = MessageDigest.getInstance("MD5");
	    	byte[] digest = md5.digest(plainText.getBytes());
	        for (byte b : digest) {
	            formatter.format("%02x", b);
	        }
	        return formatter.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	    	// Nothing to do
	    	e.printStackTrace();
	    	return null;
	    } finally {
	        formatter.close();
	    }
	    
	}

	


		
	
	public static void modifyAccount(final Main main) {

		final Database database = main.getDatabase();
		
		FormLayout content = new FormLayout();
		content.setSizeFull();

		final Label l = new Label(main.account.getId(database));
        l.setCaption("K‰ytt‰j‰n nimi:");
        l.setWidth("100%");
        content.addComponent(l);
		
        final TextField tf = new TextField();
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.setCaption("K‰ytt‰j‰n nimi:");
        tf.setId("loginUsernameField");
        tf.setValue(main.account.getText(database));
        content.addComponent(tf);

        final TextField tf2 = new TextField();
        tf2.setWidth("100%");
        tf2.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf2.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf2.setCaption("S‰hkˆpostiosoite:");
        tf2.setId("loginUsernameField");
        tf2.setValue(main.account.getEmail());
        content.addComponent(tf2);

        final PasswordField pf = new PasswordField();
        pf.setCaption("Vanha salasana:");
        pf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf.setWidth("100%");
        pf.setId("loginPasswordField");
        content.addComponent(pf);
        
        final PasswordField pf2 = new PasswordField();
        pf2.setCaption("Uusi salasana:");
        pf2.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf2.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf2.setWidth("100%");
        pf2.setId("loginPasswordField");
        content.addComponent(pf2);

        final PasswordField pf3 = new PasswordField();
        pf3.setCaption("Uusi salasana uudestaan:");
        pf3.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf3.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf3.setWidth("100%");
        pf3.setId("loginPasswordField");
        content.addComponent(pf3);

        final Label err = new Label("V‰‰r‰ k‰ytt‰j‰tunnus tai salasana");
        err.addStyleName(ValoTheme.LABEL_FAILURE);
        err.addStyleName(ValoTheme.LABEL_TINY);
        err.setVisible(false);
        content.addComponent(err);
		
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);

        Button apply = new Button("Tee muutokset");
        
        buttons.addComponent(apply);
        
        final Window dialog = Dialogs.makeDialog(main, "450px", "480px", "K‰ytt‰j‰tilin asetukset", "Poistu", content, buttons);
        apply.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	
            	String valueHash = Utils.hash(pf.getValue());
            	if(!valueHash.equals(main.account.getHash())) {
            		err.setValue("V‰‰r‰ salasana");
            		err.setVisible(true);
            		return;
            	}
            	
            	if(pf2.isEmpty()) {
            		err.setValue("Tyhj‰ salasana ei kelpaa");
            		err.setVisible(true);
            		return;
            	}
            	
            	if(!pf2.getValue().equals(pf3.getValue())) {
            		err.setValue("Uudet salasanat eiv‰t t‰sm‰‰");
            		err.setVisible(true);
            		return;
            	}
            	
            	main.account.text = tf.getValue();
            	main.account.email = tf2.getValue();
            	main.account.hash = Utils.hash(pf2.getValue());

            	Updates.update(main, true);
            	
            	main.removeWindow(dialog);
            	
            }
            
        });

	}
	
	private static void makeAccountCombo(Main main, final Map<String,Account> accountMap, ComboBox users) {
		
		final Database database = main.getDatabase();

		accountMap.clear();
		String exist = (String)users.getValue();
		users.removeAllItems();
		for(Account a : Account.enumerate(database)) {
			accountMap.put(a.getId(database), a);
			users.addItem(a.getId(database));
			if(exist != null) {
				if(a.getId(database).equals(exist))
					users.select(a.getId(database));
			} else {
				users.select(a.getId(database));
			}
		}
		
	}

	private static void makeAccountTable(Database database, ComboBox users, final Map<String,Account> accountMap, final Table table) {
		
		table.removeAllItems();
		Object selection = users.getValue();
		Account state = accountMap.get(selection);
		if(state != null) {
			for(int i=0;i<state.rights.size();i++) {
				Right r = state.rights.get(i);
				table.addItem(new Object[] { r.map.getId(database), r.write ? "Muokkaus" : "Luku", r.recurse ? ALATASON_KARTAT : VALITTU_KARTTA }, i+1);
			}
		}

	}

	public static final String ALATASON_KARTAT = "Alatason kartat";
	public static final String VALITTU_KARTTA = "Valittu kartta";
	
	public static String findFreshUserName(Validator validator) {
		
		String proposal = "Uusi k‰ytt‰j‰";
		
		try {
			
			validator.validate(proposal);
			return proposal;
			
		} catch (InvalidValueException e) {

			int counter = 2;
			while(true) {
				proposal = "Uusi k‰ytt‰j‰ " + counter++;
				try {
					validator.validate(proposal);
					return proposal;
				} catch (InvalidValueException e2) {
				}
			}
			
		}
		
	}
	
	public static void manage(final Main main) {

		final Database database = main.getDatabase();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setSpacing(true);
        hl1.setWidth("100%");

		final ComboBox users = new ComboBox();
		users.setWidth("100%");
		users.setNullSelectionAllowed(false);
        users.addStyleName(ValoTheme.COMBOBOX_SMALL);
        users.setCaption("Valitse k‰ytt‰j‰:");
        
		final Map<String,Account> accountMap = new HashMap<String,Account>();
        makeAccountCombo(main, accountMap, users);

		for(Account a : Account.enumerate(database)) {
			accountMap.put(a.getId(database), a);
			users.addItem(a.getId(database));
			users.select(a.getId(database));
		}

        final Table table = new Table();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.addStyleName(ValoTheme.TABLE_COMPACT);

		users.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 5036991262418844060L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				users.removeValueChangeListener(this);
		        makeAccountCombo(main, accountMap, users);
		        makeAccountTable(database, users, accountMap, table);
		        users.addValueChangeListener(this);
			}
			
		});

        final TextField tf = new TextField();
        
        Validator nameValidator = new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				if(s.isEmpty())
					throw new InvalidValueException("Nimi ei saa olla tyhj‰");
				if(accountMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo k‰ytˆss‰");
			}
			
		};
        
		final Button save = new Button("Luo", new Button.ClickListener() {

			private static final long serialVersionUID = -6053708137324681886L;

			public void buttonClick(ClickEvent event) {

				if(!tf.isValid()) return;
				
				String pass = Long.toString(Math.abs( UUID.randomUUID().getLeastSignificantBits()), 36);
				Account.create(database, tf.getValue(), "", Utils.hash(pass));

				Updates.update(main, true);

		        makeAccountCombo(main, accountMap, users);
		        makeAccountTable(database, users, accountMap, table);

		        Dialogs.infoDialog(main, "Uusi k‰ytt‰j‰ '" + tf.getValue() + "' luotu", "K‰ytt‰j‰n salasana on " + pass + "", null);

			}

		});
		save.addStyleName(ValoTheme.BUTTON_SMALL);

		final Button remove = new Button("Poista", new Button.ClickListener() {

			private static final long serialVersionUID = -5359199320445328801L;

			public void buttonClick(ClickEvent event) {

				Object selection = users.getValue();
				Account state = accountMap.get(selection);
				
				// System cannot be removed
				if("System".equals(state.getId(database))) return;
				
				state.remove(database);

				Updates.update(main, true);

				makeAccountCombo(main, accountMap, users);
				makeAccountTable(database, users, accountMap, table);
				
			}

		});
		remove.addStyleName(ValoTheme.BUTTON_SMALL);
		
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setCaption("Luo uusi k‰ytt‰j‰ nimell‰:");
		tf.setValue(findFreshUserName(nameValidator));
		tf.setCursorPosition(tf.getValue().length());
		tf.setValidationVisible(true);
		tf.setInvalidCommitted(true);
		tf.setImmediate(true);
		tf.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				tf.setValue(event.getText());
				try {
					tf.validate();
				} catch (InvalidValueException e) {
					save.setEnabled(false);
					return;
				}
				save.setEnabled(true);
			}
			
		});
		tf.addValidator(nameValidator);
		if(!tf.isValid()) save.setEnabled(false);
		
		hl1.addComponent(users);
		hl1.setExpandRatio(users, 1.0f);
		hl1.setComponentAlignment(users, Alignment.BOTTOM_CENTER);

		hl1.addComponent(tf);
		hl1.setExpandRatio(tf, 1.0f);
		hl1.setComponentAlignment(tf, Alignment.BOTTOM_CENTER);
		
		hl1.addComponent(save);
		hl1.setExpandRatio(save, 0.0f);
		hl1.setComponentAlignment(save, Alignment.BOTTOM_CENTER);

		hl1.addComponent(remove);
		hl1.setExpandRatio(remove, 0.0f);
		hl1.setComponentAlignment(remove, Alignment.BOTTOM_CENTER);

        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        table.addContainerProperty("Kartta", String.class, null);
        table.addContainerProperty("Oikeus", String.class, null);
        table.addContainerProperty("Laajuus", String.class, null);
        
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setCaption("K‰ytt‰j‰n oikeudet");
        
        makeAccountTable(database, users, accountMap, table);
        
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);
        
		final Button removeRights = new Button("Poista valitut rivit", new Button.ClickListener() {

			private static final long serialVersionUID = 4699670345358079045L;

			public void buttonClick(ClickEvent event) {

				Object user = users.getValue();
				Account state = accountMap.get(user);

				Object selection = table.getValue();
				Collection<?> sel = (Collection<?>)selection;

				List<Right> toRemove = new ArrayList<Right>();
				
				for(Object s : sel) {
					Integer index = (Integer)s;
					toRemove.add(state.rights.get(index-1));
				}
				
				for(Right r : toRemove)
					state.rights.remove(r);

				Updates.update(main, true);

				makeAccountTable(database, users, accountMap, table);

			}

		});
		removeRights.addStyleName(ValoTheme.BUTTON_SMALL);

        table.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1188285609779556446L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				Object selection = table.getValue();
				Collection<?> sel = (Collection<?>)selection;
				if(sel.isEmpty()) removeRights.setEnabled(false);
				else removeRights.setEnabled(true);
				
			}
        	
        });
        
		
		
		final ComboBox mapSelect = new ComboBox();
		mapSelect.setWidth("100%");
		mapSelect.setNullSelectionAllowed(false);
		mapSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
		mapSelect.setCaption("Valitse kartta:");
		for(Strategiakartta a : Strategiakartta.enumerate(database)) {
			mapSelect.addItem(a.uuid);
			mapSelect.setItemCaption(a.uuid, a.getText(database));
			mapSelect.select(a.uuid);
		}

		final ComboBox rightSelect = new ComboBox();
		rightSelect.setWidth("100px");
		rightSelect.setNullSelectionAllowed(false);
		rightSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
		rightSelect.setCaption("Valitse oikeus:");
		rightSelect.addItem("Muokkaus");
		rightSelect.addItem("Luku");
		rightSelect.select("Luku");

		final ComboBox propagateSelect = new ComboBox();
		propagateSelect.setWidth("130px");
		propagateSelect.setNullSelectionAllowed(false);
		propagateSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
		propagateSelect.setCaption("Valitse laajuus:");
		propagateSelect.addItem(VALITTU_KARTTA);
		propagateSelect.addItem(ALATASON_KARTAT);
		propagateSelect.select(VALITTU_KARTTA);

		final Button addRight = new Button("Lis‰‰ rivi", new Button.ClickListener() {

			private static final long serialVersionUID = -4841787792917761055L;

			public void buttonClick(ClickEvent event) {
				
				Object user = users.getValue();
				Account state = accountMap.get(user);
				
				String mapUUID = (String)mapSelect.getValue();
				Strategiakartta map = database.find(mapUUID);
				String right = (String)rightSelect.getValue();
				String propagate = (String)propagateSelect.getValue();

				Right r = new Right(map, right.equals("Muokkaus"), propagate.equals(ALATASON_KARTAT));
				state.rights.add(r);

				Updates.update(main, true);

				makeAccountTable(database, users, accountMap, table);

			}

		});
		addRight.addStyleName(ValoTheme.BUTTON_SMALL);

        table.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					removeRights.setEnabled(true);
				} else {
					removeRights.setEnabled(false);
				}
				
			}
			
		});
        
        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        hl2.setWidth("100%");

        hl2.addComponent(removeRights);
        hl2.setComponentAlignment(removeRights, Alignment.TOP_LEFT);
        hl2.setExpandRatio(removeRights, 0.0f);

        hl2.addComponent(addRight);
        hl2.setComponentAlignment(addRight, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(addRight, 0.0f);

        hl2.addComponent(mapSelect);
        hl2.setComponentAlignment(mapSelect, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(mapSelect, 1.0f);

        hl2.addComponent(rightSelect);
        hl2.setComponentAlignment(rightSelect, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(rightSelect, 0.0f);

        hl2.addComponent(propagateSelect);
        hl2.setComponentAlignment(propagateSelect, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(propagateSelect, 0.0f);

        content.addComponent(hl2);
        content.setComponentAlignment(hl2, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(hl2, 0.0f);

        final VerticalLayout vl = new VerticalLayout();

        final Panel p = new Panel();
        p.setWidth("100%");
        p.setHeight("200px");
        p.setContent(vl);
        
        final TimeConfiguration tc = TimeConfiguration.getInstance(database); 
        
        final TextField tf2 = new TextField();
        tf2.setWidth("200px");
        tf2.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf2.setCaption("Strategiakartan m‰‰ritysaika:");
		tf2.setValue(tc.getRange());
		tf2.setCursorPosition(tf.getValue().length());
		tf2.setValidationVisible(true);
		tf2.setInvalidCommitted(true);
		tf2.setImmediate(true);
		tf2.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				tf2.setValue(event.getText());
				try {
					tf2.validate();
					tc.setRange(event.getText());
					updateYears(database, vl);
					Updates.update(main, true);
				} catch (InvalidValueException e) {
					return;
				}
			}
			
		});
		tf2.addValidator(new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				TimeInterval ti = TimeInterval.parse(s);
				long start = ti.startYear;
				long end = ti.endYear;
				if(start < 2015)
					throw new InvalidValueException("Alkuvuosi ei voi olla aikaisempi kuin 2015.");
				if(end > 2025)
					throw new InvalidValueException("P‰‰ttymisvuosi ei voi olla myˆh‰isempi kuin 2025.");
				if(end-start > 9)
					throw new InvalidValueException("Strategiakartta ei tue yli 10 vuoden tarkasteluja.");
			}
			
		});

		content.addComponent(tf2);
        content.setComponentAlignment(tf2, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(tf2, 0.0f);
        
        updateYears(database, vl);
        
        content.addComponent(p);
        content.setComponentAlignment(p, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(p, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        Dialogs.makeDialog(main, main.dialogWidth(), main.dialogHeight(0.8), "Hallinnoi strategiakarttaa", "Sulje", content, buttons);

	}
	
	public static void updateYears(final Database database, final VerticalLayout vl) {

		vl.removeAllComponents();
		
		final TimeConfiguration tc = TimeConfiguration.getInstance(database);
        TimeInterval ti = TimeInterval.parse(tc.getRange());
        for(int i=ti.startYear;i<=ti.endYear;i++) {
        	final int year = i;
        	HorizontalLayout hl = new HorizontalLayout();
        	hl.setSpacing(true);
        	String caption = Integer.toString(i) + (tc.isFrozen(i) ? " Muutokset estetty" : " Muokattavissa");
        	Label l = new Label(caption);
        	l.setWidth("250px");
        	l.setHeight("100%");
        	hl.addComponent(l);
        	Button b = new Button(tc.isFrozen(i) ? "Avaa muokattavaksi" : "Est‰ muutokset");
        	b.addStyleName(ValoTheme.BUTTON_SMALL);
        	b.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 556680407448842136L;

				@Override
				public void buttonClick(ClickEvent event) {
					if(tc.isFrozen(year)) {
						tc.unfreeze(year);
					} else {
						tc.freeze(year);
					}
					updateYears(database, vl);
				}
				
			});
        	b.setWidth("200px");
        	hl.addComponent(b);
        	//hl.setWidth("100%");
        	vl.addComponent(hl);
        }
		
	}

	public static void setUserMeter(final Main main, final Base base, final Meter m) {

		final Database database = main.getDatabase();

        final Window subwindow = new Window("Aseta mittarin arvo", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("350px");
        subwindow.setResizable(false);

        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        String caption = m.getCaption(database);
        if(caption != null && !caption.isEmpty()) {
            final Label header = new Label(caption);
            header.addStyleName(ValoTheme.LABEL_LARGE);
            winLayout.addComponent(header);
        }
        
		final Indicator indicator = m.getPossibleIndicator(database);
		if(indicator == null) return;
		
		Datatype dt = indicator.getDatatype(database);
		if(!(dt instanceof EnumerationDatatype)) return;

		final Label l = new Label("Selite: " + indicator.getValueShortComment());

		AbstractField<?> forecastField = dt.getEditor(main, base, indicator, true, new CommentCallback() {
			
			@Override
			public void runWithComment(String shortComment, String comment) {
				l.setValue("Selite: " + indicator.getValueShortComment());
			}
			
			@Override
			public void canceled() {
			}
			
		});
		forecastField.setWidth("100%");
		forecastField.setCaption("Ennuste");
		winLayout.addComponent(forecastField);
		
		AbstractField<?> currentField = dt.getEditor(main, base, indicator, false, new CommentCallback() {
			
			@Override
			public void runWithComment(String shortComment, String comment) {
				l.setValue("Selite: " + indicator.getValueShortComment());
			}
			
			@Override
			public void canceled() {
			}
			
		});
		currentField.setWidth("100%");
		currentField.setCaption("Toteuma");
		winLayout.addComponent(currentField);
		
		winLayout.addComponent(l);
		
		l.setWidth("100%");
		winLayout.setComponentAlignment(l, Alignment.BOTTOM_CENTER);
        
        HorizontalLayout hl = new HorizontalLayout();
		winLayout.addComponent(hl);
		winLayout.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);

        Button ok = new Button("Sulje", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1364802814012491490L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
            
        });
        
        Button define = new Button("M‰‰rit‰", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1364802814012491490L;

			public void buttonClick(ClickEvent event) {
				Meter.editMeter(main, base, m);
            }
            
        });

        hl.addComponent(ok);
		hl.setComponentAlignment(ok, Alignment.BOTTOM_LEFT);
        hl.addComponent(define);
		hl.setComponentAlignment(define, Alignment.BOTTOM_LEFT);

		main.addWindow(subwindow);
		
	}

	public static void saveCurrentState(final Main main) {
        
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setSpacing(true);
        hl1.setWidth("100%");
        
		final Map<String,UIState> stateMap = new HashMap<String,UIState>();
		for(UIState s : main.account.uiStates)
			stateMap.put(s.name, s);

        final TextField tf = new TextField();
        
		final Button save = new Button("Tallenna n‰kym‰", new Button.ClickListener() {

			private static final long serialVersionUID = 2449606920686729881L;

			public void buttonClick(ClickEvent event) {

				if(!tf.isValid()) return;
				
				String name = tf.getValue();

				Page.getCurrent().getJavaScript().execute("doSaveBrowserState('" + name + "');");
				
			}

		});
        
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setCaption("Tallenna nykyinen n‰kym‰ nimell‰:");
		tf.setValue("Uusi n‰kym‰");
		tf.setCursorPosition(tf.getValue().length());
		tf.setValidationVisible(true);
		tf.setInvalidCommitted(true);
		tf.setImmediate(true);
		tf.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				tf.setValue(event.getText());
				try {
					tf.validate();
				} catch (InvalidValueException e) {
					save.setEnabled(false);
					return;
				}
				save.setEnabled(true);
			}
			
		});
		tf.addValidator(new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				if(s.isEmpty())
					throw new InvalidValueException("Nimi ei saa olla tyhj‰");
				if(stateMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo k‰ytˆss‰");
			}
			
		});
		if(!tf.isValid()) save.setEnabled(false);
		
		hl1.addComponent(tf);
		hl1.setExpandRatio(tf, 1.0f);
		
		hl1.addComponent(save);
		hl1.setExpandRatio(save, 0.0f);
		hl1.setComponentAlignment(save, Alignment.BOTTOM_CENTER);
		
        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        final ListSelect table = new ListSelect();
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setCaption("Tallennetut n‰kym‰t");
        for(UIState state : main.account.uiStates) {
        	table.addItem(state.name);
        }
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);
        
		final Button remove = new Button("Poista valitut n‰kym‰t");
		
        table.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					remove.setEnabled(true);
				} else {
					remove.setEnabled(false);
				}
				
			}
			
		});
        
        remove.setEnabled(false);

        content.addComponent(remove);
        content.setComponentAlignment(remove, Alignment.MIDDLE_LEFT);
        content.setExpandRatio(remove, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        final Window dialog = Dialogs.makeDialog(main, "N‰kymien hallinta", "Sulje",  content, buttons);
        
        remove.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4680588998085550908L;

			public void buttonClick(ClickEvent event) {

				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				
				if(!selected.isEmpty()) {
					
					for(Object o : selected) {
						UIState state = stateMap.get(o);
						if(state != null)
							main.account.uiStates.remove(state);
					}
					Updates.update(main, true);
				}
				
				main.removeWindow(dialog);

			}

		});

	}
	
	public static void addMap(final Main main, final Strategiakartta parent) {

		final Database database = main.getDatabase();
		
		FormLayout content = new FormLayout();
		content.setSizeFull();

//		final TextField tf = new TextField();
//		tf.setCaption("Kartan tunniste:");
//		tf.setValue("tunniste");
//		tf.setWidth("100%");
//		content.addComponent(tf);

		final TextField tf2 = new TextField();
		tf2.setCaption("Kartan nimi:");
		tf2.setValue("Uusi kartta");
		tf2.setWidth("100%");
		content.addComponent(tf2);
		
		final ComboBox combo = new ComboBox();
		combo.setCaption("Kartan tyyppi:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Collection<Base> subs = Strategiakartta.availableLevels(database);
		for(Base b : subs) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getText(database));
			combo.select(b.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button("Lis‰‰");
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "340px", "Lis‰‰ alatason kartta", "Peruuta", content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				database.newMap(main, parent, "", name, type);
				Updates.updateJS(main, true);
				main.removeWindow(dialog);
				

			}
		});
		
	}
	
	public static void addImplementationMap(final Main main, final Tavoite goal) {

		final Database database = main.getDatabase();

		try {
			Base subType = goal.getPossibleSubmapType(database);
			if(subType != null) {
				goal.ensureImplementationMap(main);
				Updates.updateJS(main, true);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		FormLayout content = new FormLayout();
		content.setSizeFull();
		
		final ComboBox combo = new ComboBox();
		combo.setCaption("Kartan tyyppi:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Collection<Base> subs = Strategiakartta.availableLevels(database);
		for(Base b : subs) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getText(database));
			combo.select(b.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button("Lis‰‰");
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "340px", "Lis‰‰ alatason kartta", "Peruuta", content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				Strategiakartta parent = database.getMap(goal);
				
				Strategiakartta newMap = database.newMap(main, parent, "", "", type);
				newMap.addRelation(Relation.find(database, Relation.IMPLEMENTS), goal);
				
				for(Painopiste pp : goal.painopisteet) {
					Tavoite.createCopy(main, newMap, pp);
				}
				
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
		});
		
	}


	
	public static void insertRootMap(final Main main, final Strategiakartta currentRoot) {

		final Database database = main.getDatabase();

		FormLayout content = new FormLayout();
		content.setSizeFull();

		final TextField tf = new TextField();
		tf.setCaption("Kartan tunniste:");
		tf.setValue("tunniste");
		tf.setWidth("100%");
		content.addComponent(tf);

		final TextField tf2 = new TextField();
		tf2.setCaption("Kartan nimi:");
		tf2.setValue("Uusi kartta");
		tf2.setWidth("100%");
		content.addComponent(tf2);
		
		final ComboBox combo = new ComboBox();
		combo.setCaption("Kartan tyyppi:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Collection<Base> subs = Strategiakartta.availableLevels(database);
		for(Base b : subs) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getText(database));
			combo.select(b.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button("Lis‰‰");
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "340px", "Lis‰‰ alatason kartta", "Peruuta", content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String id = tf.getValue();
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				Strategiakartta uusi = database.newMap(main, null, id, name, type);
				uusi.addAlikartta(currentRoot);
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
		});
		
	}
	
	public static void print(final Main main) {

		VerticalLayout content = new VerticalLayout();

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
		
		final Button ok = new Button("Lataa");
		ok.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 5350320436995864012L;

			@Override
			public void buttonClick(ClickEvent event) {
				ok.setEnabled(false);
			}
			
		});
		buttons.addComponent(ok);
		
		final String svgText = main.pdf.svg;
		
		final OnDemandFileDownloader dl = new OnDemandFileDownloader(new OnDemandStreamSource() {
			
			private static final long serialVersionUID = -3866190768030858133L;
			File temp;
			Date date = new Date();
			
			@Override
			public InputStream getStream() {
				
				String uuid = UUID.randomUUID().toString(); 
				
				File printing = new File(Main.baseDirectory(), "printing");
				
				temp = new File(printing, uuid + ".pdf");

				if(svgText != null) {

					File htmlFile = new File(printing, uuid + ".html");
					File script = new File(printing, uuid + ".js");
					
		        	try {
		        		
		        		String html = PhantomJSDriver.printHtml(svgText, Main.getAppFile("print.html").getParentFile().getAbsolutePath());
						Files.write(htmlFile.toPath(), html.getBytes(Charset.forName("UTF-8")));
		        		
						String browserUrl = htmlFile.toURI().toURL().toString();
		        		
			        	String printCommand = PhantomJSDriver.printCommand(browserUrl, temp.getAbsolutePath());
			        	
						Files.write(script.toPath(), printCommand.getBytes());
						
						PhantomJSDriver.execute(script);
						
						return new FileInputStream(temp);

		        	} catch (IOException e) {
						e.printStackTrace();
					}

				}
				
				throw new IllegalStateException();
				
			}
			
			@Override
			public void onRequest() {
			}
			
			@Override
			public long getFileSize() {
				return temp.length();
			}
			
			@Override
			public String getFileName() {
				return "Strategiakartta_" + Utils.dateString(date) + ".pdf";
			}
			
		});
		
		dl.getResource().setCacheTime(0);
		dl.extend(ok);

		final Window dialog = Dialogs.makeDialog(main, "420px", "135px", "Haluatko ladata kartan PDF-muodossa?", "Sulje", content, buttons);
		
	}

	public static void addView(final Main main, final Strategiakartta map) {

		final Database database = main.getDatabase();

		FormLayout content = new FormLayout();
		content.setSizeFull();

		final TextField tf = new TextField();
		tf.setCaption("N‰kym‰n tunniste:");
		tf.setValue("tunniste");
		tf.setWidth("100%");
		content.addComponent(tf);

		final TextField tf2 = new TextField();
		tf2.setCaption("N‰kym‰n nimi:");
		tf2.setValue("Uusi n‰kym‰");
		tf2.setWidth("100%");
		content.addComponent(tf2);

		final ComboBox combo = new ComboBox();
		combo.setCaption("Kartan tyyppi:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		Collection<Base> subs = Strategiakartta.availableLevels(database);
		for(Base b : subs) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getText(database));
			combo.select(b.uuid);
		}

		final ComboBox combo2 = new ComboBox();
		combo2.setCaption("Aihetunniste:");
		combo2.setNullSelectionAllowed(false);
		combo2.setWidth("100%");
		content.addComponent(combo2);
		
		for(Tag t : Tag.enumerate(database)) {
			combo2.addItem(t.uuid);
			combo2.setItemCaption(t.uuid, t.getId(database));
			combo2.select(t.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button("Lis‰‰");
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "380px", "Lis‰‰ n‰kym‰", "Peruuta", content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String id = tf.getValue();
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				String tagUUID = (String)combo2.getValue();
				Tag tag = (Tag)database.find(tagUUID);
				
				Strategiakartta newMap = database.newMap(main, main.uiState.current, id, name + " (n‰kym‰)", type);
				newMap.generators.add(tag);
				
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
			
		});
		
	}

	public static interface CommentCallback {
		
		public void runWithComment(String shortComment, String comment);
		public void canceled();
		
	}
	
	public static class AbstractCommentCallback implements CommentCallback {

		@Override
		public void runWithComment(String shortComment, String comment) {
		}

		@Override
		public void canceled() {
		}
		
	}
	
	public static void editTextAndId(final Main main, String title, final Base container) {
		
		final Database database = main.getDatabase();

        final Window subwindow = new Window(title, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("500px");
        subwindow.setResizable(false);

        VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        final TextField tf = new TextField();
        tf.setCaption("Lyhytnimi:");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setValue(container.getId(database));
        tf.setWidth("100%");
        winLayout.addComponent(tf);

        final TextArea ta = new TextArea();
        ta.setCaption("Teksti:");
        ta.setValue(container.getText(database));
        ta.setWidth("100%");
        ta.setHeight("290px");
        winLayout.addComponent(ta);

        Button save = new Button("Tallenna", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 6641880870005364983L;

			public void buttonClick(ClickEvent event) {
            	String idValue = tf.getValue();
            	String value = ta.getValue();
            	main.removeWindow(subwindow);
            	container.modifyId(main, idValue);
            	container.modifyText(main, value);
            	Collection<String> tags = Tag.extractTags(value);
            	database.assertTags(tags);
            	ArrayList<Tag> tagObjects = new ArrayList<Tag>();
            	for(String s : tags)
            		tagObjects.add(database.getOrCreateTag(s));
            	container.assertRelatedTags(database, tagObjects);
        		Updates.update(main, true);
        		Property emails = Property.find(database, Property.EMAIL);
        		String addr = emails.getPropertyValue(container);
        		if(addr != null && !addr.isEmpty()) {
        			String[] addrs = addr.split(",");
        			if(addrs.length > 0) {
        				try {
							Email.send(addrs, "Muutos strategiakartassa: " + container.getId(database), "K‰ytt‰j‰ " + main.account.getId(database) + " on muuttanut strategiakarttaa.<br/><br/>Lyhytnimi: " + container.getId(database) + "<br/><br/>Teksti: " + container.getText(database));
						} catch (MessagingException e) {
							e.printStackTrace();
						}
        			}
        		}
            }
            
        });
        
        Button discard = new Button("Peru muutokset", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -784522457615993823L;

            public void buttonClick(ClickEvent event) {
            	Updates.update(main, true);
            	main.removeWindow(subwindow);
            }
            
        });

    	HorizontalLayout hl2 = new HorizontalLayout();
    	hl2.setSpacing(true);
    	hl2.addComponent(save);
    	hl2.addComponent(discard);
    	winLayout.addComponent(hl2);
    	winLayout.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
		main.addWindow(subwindow);
        
		ta.setCursorPosition(ta.getValue().length());

	}

	public static Button tagButton(final Database database, String prefix, String tag, final int index) {

    	Tag t = database.getOrCreateTag(tag);

        Styles styles = Page.getCurrent().getStyles();

        styles.add(".fi_semantum_strategia div.v-button." + prefix + "tag"+ index + " { opacity: 1.0; box-shadow:none; color: #000000; background-image: linear-gradient(" + t.color + " 0%, " + t.color + " 100%); }");
    	
    	Button tagButton = new Button();
    	tagButton.setEnabled(false);
    	tagButton.setCaption(tag);
    	tagButton.addStyleName(prefix + "tag" + index);
    	
    	return tagButton;

	}
	
	public static void fillTagEditor(final Database database, final AbstractLayout layout, final List<String> tags, final boolean allowRemove) {

		layout.removeAllComponents();
		
        for(int i=0;i<tags.size();i++) {

        	String tag = tags.get(i);
        	
        	HorizontalLayout hl = new HorizontalLayout();

        	if(allowRemove) {
        		final int index = i;
            	Button b = new Button();
    	        b.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            	b.setIcon(FontAwesome.TIMES_CIRCLE);
            	b.addClickListener(new ClickListener() {
            		
    				private static final long serialVersionUID = -4473258383318654850L;

    				@Override
    				public void buttonClick(ClickEvent event) {
    					tags.remove(index);
    					fillTagEditor(database, layout, tags, allowRemove);
    				}
    			});
            	hl.addComponent(b);
        	}
        	
        	Button tagButton = tagButton(database, "dialog", tag, i);
        	hl.addComponent(tagButton);
        	hl.setComponentAlignment(tagButton, Alignment.MIDDLE_LEFT);

        	layout.addComponent(hl);

        }

	}

	
	static class TagCombo extends ComboBox {
		
		private static final long serialVersionUID = 5930055496801663683L;
		
		String customFilterString;
		
		@Override
		public void changeVariables(Object source, Map<String, Object> variables) {
			super.changeVariables(source, variables);
			customFilterString = (String) variables.get("filter");
		}
		
	}
	
	public static void editTags(final Main main, String title, final Base container) {
		
		final Database database = main.getDatabase();

        final Window subwindow = new Window(title, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("360px");
        subwindow.setResizable(true);

        VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        // Add some content; a label and a close-button
        final List<String> tags = new ArrayList<String>();
        for(Tag t : container.getRelatedTags(database))
        	tags.add(t.getId(database));

        final CssLayout vl = new CssLayout();
        vl.setCaption("K‰ytˆss‰ olevat aihetunnisteet:");
        fillTagEditor(database, vl, tags, Account.canWrite(main, container));
        winLayout.addComponent(vl);

    	HorizontalLayout hl = new HorizontalLayout();
    	hl.setWidth("100%");
    	hl.setSpacing(true);
    	
    	final TagCombo combo= new TagCombo();
    	final CustomLazyContainer comboContainer = new CustomLazyContainer(database, combo, Tag.enumerate(database));
    	combo.setWidth("100%");
    	combo.setCaption("Uusi aihetunniste:");
    	combo.setInputPrompt("valitse listasta tai kirjoita");
    	combo.setFilteringMode(FilteringMode.STARTSWITH);
    	combo.setTextInputAllowed(true);
    	combo.setImmediate(true);
    	combo.setNullSelectionAllowed(false);
    	combo.setInvalidAllowed(true);
    	combo.setInvalidCommitted(true);
    	combo.setItemCaptionMode(ItemCaptionMode.PROPERTY);
    	combo.setItemCaptionPropertyId("id"); //should set
    	combo.setContainerDataSource(comboContainer);
    	
        hl.addComponent(combo);
    	hl.setExpandRatio(combo, 1.0f);
        
        Button add = new Button("Lis‰‰", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -2848576385076605664L;

            public void buttonClick(ClickEvent event) {
            	String filter = (String)combo.getValue();
				if(filter != null && filter.length() > 0) {
					Tag t = database.getOrCreateTag(filter);
					if(tags.contains(t.getId(database))) return;
					tags.add(t.getId(database));
					fillTagEditor(database, vl, tags, main.account != null);
					combo.clear();
				}
            }
        });
        hl.addComponent(add);
    	hl.setComponentAlignment(add, Alignment.BOTTOM_LEFT);
    	hl.setExpandRatio(add, 0.0f);

        winLayout.addComponent(hl);

        Button close = new Button("Tallenna", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -451523776456589591L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            	List<Tag> newTags = new ArrayList<Tag>();
            	for(String s : tags)
            		newTags.add(database.getOrCreateTag(s));
            	container.setRelatedTags(database, newTags);
            	Updates.update(main, true);
            }
        });
        Button discard = new Button("Peru muutokset", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -2387057110951581993L;

            public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
        });

    	HorizontalLayout hl2 = new HorizontalLayout();
    	hl2.setSpacing(true);
    	hl2.addComponent(close);
    	hl2.addComponent(discard);
    	winLayout.addComponent(hl2);
    	winLayout.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
    	
		main.addWindow(subwindow);
		
	}
	
	public static void loseFocus(Component parent) {
		while (parent != null) {
			if(parent instanceof Component.Focusable) {
				((Component.Focusable) parent).focus();
				break;
			} else {
				parent = parent.getParent();
			}
		}			
	}
	

	

	
	

	public static Base getPossibleImplemented(Database db, Base b) {
		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
	   	Pair p = implementsRelation.getPossibleRelation(b);
	   	if(p != null) {
	   		return db.find(p.second);
	   	}
	   	return null;
	}

	public static boolean doesImplement(Database db, Base b, Base target) {
		return doesImplement(db, b, target, new HashMap<Base, Boolean>());
	}

	private static boolean doesImplement(Database db, Base b, Base target, Map<Base,Boolean> cache) {
		Boolean c = cache.get(b);
		if(c != null) return c;
		if(b.equals(target)) {
			cache.put(b, true);
			return true;
		}
		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
		for(Base b2 : b.getRelatedObjects(db, implementsRelation)) {
			if(doesImplement(db, b2, target, cache)) {
				cache.put(b, true);
				return true;
			}
		}
		cache.put(b, false);
		return false;
	}
	
	public static Set<Base> getImplementors(Database db, Base target, Map<Base,Boolean> cache) {
		Set<Base> result = new HashSet<Base>();
		for(Base b : db.enumerate()) {
			if(doesImplement(db, b, target, cache))
				result.add(b);
		}
		return result;
	}
	
//	public static boolean getManyImplementor(Database database, ObjectType type) {
//
//		Property p = Property.find(database, Property.MANY_IMPLEMENTOR);
//		return Boolean.parseBoolean(p.getPropertyValue(type));
//
//	}

	public static ObjectType getOwnGoalType(Database database, Strategiakartta map) {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property goalTypeProperty = Property.find(database, Property.OWN_GOAL_TYPE);
		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);
		
		return database.find(goalTypeUUID);

	}

	public static ObjectType getGoalType(Database database, Strategiakartta map) {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);
		
		return database.find(goalTypeUUID);

	}

	public static String describeDate(Date d) {
		return describeDate(new Date(), d);
	}

	public static String describeDate(Date now, Date d) {

		long diff = now.getTime()-d.getTime();
		String date = "";
		if(diff < 1000*60) {
			date = "Hetki sitten";
		} else if(diff < 1000*60*60) {
			date = "" + (diff/(1000*60)) + " min sitten";
		} else if(diff < 1000*60*60*24) {
			date = "" + (diff/(1000*60*60)) + " h sitten";
		} else if(diff < 1000*60*60*24*7) {
			date = "" + (diff/(1000*60*60*24)) + " p‰iv‰‰ sitten";
		} else {
			date = Utils.sdf.format(d);
		}
		return date;
		
	}
	
	public static void modifyValidity(Main main, Base base, String newValidity) {

		Property aika = Property.find(main.getDatabase(), Property.AIKAVALI);
		aika.set(main, base, newValidity);

	}

	public static String getValidity(Database database, Base base) {

		Property aika = Property.find(database, Property.AIKAVALI);
		String result = aika.getPropertyValue(base);
		if(result == null) result = Property.AIKAVALI_KAIKKI;
		return result;

	}
	
	public static Set<Base> getImplementationSet(Database database, Base b) {
		
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> imp = b.getRelatedObjects(database, implementsRelation);
		if(imp.isEmpty()) return Collections.emptySet();
		Set<Base> result = new TreeSet<Base>();
		
		if(b instanceof Painopiste) {
			result.add(((Painopiste)b).getGoal(database));
		}
		
		for(Base b2 : imp) {
			result.add(b2);
			Set<Base> bs = getImplementationSet(database, b2);
			result.addAll(bs);
		}
		return result;
	}
	
	public static Collection<Base> getDirectImplementors(Database database, Base b) {
		return getDirectImplementors(database, b, Property.AIKAVALI_KAIKKI);
	}

	public static Collection<Base> getDirectImplementors(Database database, Base b, String requiredValidityPeriod) {
		return getDirectImplementors(database, b, true, requiredValidityPeriod);
	}

	public static Collection<Base> getDirectImplementors(Database database, Base b, boolean filterMaps, String requiredValidityPeriod) {
		
		ArrayList<Base> implementors = new ArrayList<Base>(database.getInverse(b, Relation.find(database, Relation.IMPLEMENTS)));
		Collections.sort(implementors);
		
		if(implementors.isEmpty()) return Collections.emptyList();

		TreeMap<Double,Base> sorting = new TreeMap<Double,Base>();

		Strategiakartta map = database.getMap(b);
		
		Property aika = Property.find(database, Property.AIKAVALI);

		for(int i=0;i<implementors.size();i++) {
			
			Base b2 = implementors.get(i);
			if(filterMaps && b2 instanceof Strategiakartta) continue;
			
			String a = aika.getPropertyValue(b2);
			if(Utils.acceptTime(a, requiredValidityPeriod)) {
				Strategiakartta child = database.getMap(b2);
				double key = 1.0 / Double.valueOf(i+1);
				for(int j=0;j<map.alikartat.length;j++) {
					Linkki l = map.alikartat[j];
					if(l.uuid.equals(child.uuid))
						key = j+2;
				}
				sorting.put(key, b2);
			}
			
		}
		
		return sorting.values();
		
	}
	
	public static List<String> excelRow(String ... cells) {
		ArrayList<String> result = new ArrayList<String>();
		for(String s : cells) result.add(s);
		return result;
	}
	
	public static String dateString(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		return "Strategiakartta_" + day + "_" + month + "_" + year + "__" + hours + "_" + minutes + "_" + seconds;
	}

	public static String hexI(int i) {
		if(i < 10) return "" + i;
		else {
			char offset = (char) (i-10);
			char c = (char) ('A' + offset);
			return "" + c;
		}
	}
	
	public static String hex(double value) {
		int i = (int)value;
		int upper = i >> 4;
		int lower = i & 15;
		return hexI(upper) + hexI(lower);
	}

	public static String trafficColor(double value) {
		
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
	
	public static String printPath(Database database, Base b) {
		StringBuilder result = new StringBuilder();
		Strategiakartta map = b.getMap(database);
		result.append(b.getText(database));
		Strategiakartta parent = map.getPossibleParent(database);
		while(parent != null) {
			result.append(" / ");
			result.append(parent.getText(database));
			parent = parent.getPossibleParent(database);
		}
		return result.toString();
		
	}
	
	public static boolean acceptTime(String itemValidityPeriod, String requiredValidityPeriod) {

		if (Property.AIKAVALI_KAIKKI.equals(requiredValidityPeriod))
			return true;

		TimeInterval itemInterval = TimeInterval.parse(itemValidityPeriod);
		TimeInterval requiredInterval = TimeInterval.parse(requiredValidityPeriod);
		
		return itemInterval.intersects(requiredInterval);

	}
	
	public static Set<String> getResponsibilityFields(Database database, Base b) {
		
		Set<String> result = new TreeSet<String>();
		buildResponsibilityFieldsInternal(database, b, result);
		return result;

	}

	private static void buildResponsibilityFieldsInternal(Database database, Base b, Set<String> result) {
		
		Strategiakartta map = database.getMap(b);
		
		Base currentLevel = map.currentLevel(database);

		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, currentLevel);
		if(model != null) {
			List<String> fields = model.getFields();
			result.addAll(fields);
		}
		
		Base implemented = Utils.getPossibleImplemented(database, b);
		if(implemented != null)
			buildResponsibilityFieldsInternal(database, implemented, result);
		
	}

	public static Map<String,String> getResponsibilityMap(Database database, Base b) {
		
		TreeMap<String,String> result = new TreeMap<String,String>();
		buildResponsibilityMapInternal(database, b, result);
		List<String> overrides = new ArrayList<String>();
		for(Map.Entry<String, String> e : result.entrySet())
			if("-".equals(e.getValue()))
				overrides.add(e.getKey());
		for(String override : overrides)
			result.remove(override);
		return result;

	}

	private static void buildResponsibilityMapInternal(Database database, Base b, Map<String,String> result) {
		
		Strategiakartta map = database.getMap(b);

		Base currentLevel = map.currentLevel(database);
		
		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, currentLevel);
		if(model == null) return;
		
		Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
		ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, b);
		ResponsibilityInstance defaultInstance = instanceRelation.getPossibleRelationObject(database, map);

		Set<String> fields = getResponsibilityFields(database, b);
		for(String field : fields) {
			if(result.containsKey(field)) continue;
			String value = null;
			if(instance != null) {
				value = instance.getValue(field);
				if(value != null && !value.isEmpty())
					result.put(field, value);
			} else if(defaultInstance != null) {
				value = defaultInstance.getValue(field);
				if(value != null && !value.isEmpty())
					result.put(field, value);
			}
		}
		
		Base implemented = Utils.getPossibleImplemented(database, b);
		if(implemented != null)
			buildResponsibilityMapInternal(database, implemented, result);
		
	}
	
}
