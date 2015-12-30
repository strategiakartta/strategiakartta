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
import java.util.TreeSet;
import java.util.UUID;

import javax.mail.MessagingException;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
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
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import elemental.events.KeyboardEvent.KeyCode;
import fi.semantum.strategia.action.MoveDown;
import fi.semantum.strategia.action.MoveUp;
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
import fi.semantum.strategia.widget.Right;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;
import fi.semantum.strategia.widget.Tavoite;

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

	
	public static Collection<String> extractTags(String text) {
		
		Set<String> result = new HashSet<String>();
		
		int pos = 0;
		while(true) {
			int start = text.indexOf('#', pos)+1;
			if(start == 0) break;
			pos = text.indexOf(' ', start+1);
			if(pos == -1) {
				result.add(text.substring(start));
				break;
			} else {
				result.add(text.substring(start, pos));
			}
		}
		
		return result;
		
	}

	static interface GoalCallback {
		
		public void selected(String uuid);
		
	}
	
	public static void defineImplementors(final Main main, final Base base) {

		final Database database = main.getDatabase();
		
		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);

		Label desc = new Label("Valitse listasta kaikki m‰‰rityst‰ toteuttavat aliorganisaatiot.");
		desc.addStyleName(ValoTheme.LABEL_TINY);
		
        content.addComponent(desc);
        content.setComponentAlignment(desc, Alignment.MIDDLE_LEFT);
        content.setExpandRatio(desc, 0.0f);

        final OptionGroup table = new OptionGroup();
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setCaption("Aliorganisaatiot");
        
        final Strategiakartta map = database.getMap(base);

        final Map<Strategiakartta,Tavoite> implementationMap = new HashMap<Strategiakartta,Tavoite>();
        final Set<String> currentlyImplemented = new HashSet<String>();
        final Map<String,String> baseCaptions = new HashMap<String,String>();
        
        for(Linkki l : map.alikartat) {
            Strategiakartta child = database.find(l.uuid);
            if(child.generators.isEmpty()) {
            	
            	boolean found = false;
            	for(Tavoite t : child.tavoitteet) {
            		if(Utils.doesImplement(database, t, base)) {
            			implementationMap.put(child, t);
    		   			currentlyImplemented.add(child.uuid);
            			found = true;
            		}
            	}
            	
		   		String s = child.getText(database) + " " + child.getId(database);
		   		baseCaptions.put(child.uuid, s);
		   			
		   		table.addItem(child.uuid);
		   		if(found) table.select(child.uuid);
		   		
            }
        }
        
        ValueChangeListener l = new ValueChangeListener() {
			
			private static final long serialVersionUID = -5659750354602332507L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				
				for(Object o : table.getItemIds()) {
					String baseCaption = baseCaptions.get(o);
					if(selected.contains(o)) {
						if(currentlyImplemented.contains(o)) {
							table.setItemCaption(o, baseCaption);
						} else {
							table.setItemCaption(o, baseCaption + " (LISƒTƒƒN)");
						}
					} else {
						if(currentlyImplemented.contains(o)) {
							table.setItemCaption(o, baseCaption + " (POISTETAAN)");
						} else {
							table.setItemCaption(o, baseCaption);
						}
					}
				}
				
			}
			
		};
        
		l.valueChange(null);
		
        table.addValueChangeListener(l);
        
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);
        
		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
        Button copy = new Button("M‰‰rit‰", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -2067411013142475955L;

			public void buttonClick(ClickEvent event) {
            	
            	Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				
				Set<Base> denied = new HashSet<Base>();
				
				for(Object o : table.getItemIds()) {
		            Strategiakartta child = database.find((String)o);
					if(selected.contains(o)) {
						if(!currentlyImplemented.contains(o)) {
							Tavoite.createCopy(database, child, base);
						}
					} else {
						if(currentlyImplemented.contains(o)) {
							Tavoite current = implementationMap.get(child);
							if(current.canRemove(database)) {
								current.remove(database);
							} else {
								denied.add(current);
							}
						}
					}
				}
				
				Updates.updateJS(main, true);
				
				main.closeDialog();
				
				if(!denied.isEmpty()) {

					VerticalLayout la = new VerticalLayout();
					Label l = new Label("Seuraavat m‰‰ritykset ovat k‰ytˆss‰, eik‰ niit‰ voida poistaa:");
					l.addStyleName(ValoTheme.LABEL_H3);
					la.addComponent(l);
					for(Base b : denied) {
						Strategiakartta map = database.getMap(b);
						Label l2 = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + b.getId(database) + " - " + b.getText(database) + " (" + map.getId(database) + ")");
						l2.setContentMode(ContentMode.HTML);
						la.addComponent(l2);
					}

					errorDialog(main, "Poistaminen ei onnistu", la);
					
				}
				
            }
            
        });
        
        buttons.addComponent(copy);

		makeDialog(main, "M‰‰rit‰ toteuttajat", "Peruuta", content, buttons);

	}

	public static void selectGoalType(final Main main, Strategiakartta map, final GoalCallback callback) {

		final Database database = main.getDatabase();

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(true);

		Button ok = new Button("Lis‰‰ oma " + map.ownGoalDescription.toLowerCase(), new Button.ClickListener() {
        	
			private static final long serialVersionUID = 6054297133724400131L;

			// inline click-listener
            public void buttonClick(ClickEvent event) {
            	main.closeDialog();
            	callback.selected(null);
            }
            
        });
        
        hl1.addComponent(ok);
        hl1.setExpandRatio(ok, 0.0f);

		Label uusiOma = new Label("Omat m‰‰ritykset eiv‰t perustu ylempien tasojen m‰‰rityksiin");
		uusiOma.addStyleName(ValoTheme.LABEL_TINY);
		
        hl1.addComponent(uusiOma);
        hl1.setComponentAlignment(uusiOma, Alignment.MIDDLE_LEFT);
        hl1.setExpandRatio(uusiOma, 1.0f);

        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        Set<Base> alreadyImplemented = new HashSet<Base>();
    	Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
        for(Tavoite t : map.tavoitteet) {
        	Pair p = implementsRelation.getPossibleRelation(t);
        	if(p != null) {
        		Base targetPP = database.find(p.second);
        		alreadyImplemented.add(targetPP);
        	}
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
        Strategiakartta parent_ = map.getPossibleParent(database); 

        if(parent_ != null) {
        
	        final ListSelect table = new ListSelect();
	        table.setSizeFull();
	        table.setNullSelectionAllowed(false);
	        table.setMultiSelect(false);
	        table.setCaption("Lis‰‰ " + map.tavoiteDescription.toLowerCase() + " perustuen ylemm‰n tason (" + parent_.getId(database) + ") m‰‰ritykseen. Valitse m‰‰ritys listasta:");
	        for(Linkki l : map.parents) {
	            Strategiakartta parent = database.find(l.uuid);
	            for(Tavoite t : parent.tavoitteet) {
	            	for(Painopiste p : t.painopisteet) {
	            		if(alreadyImplemented.contains(p)) continue;
	            		table.addItem(p.uuid);
	            		table.setItemCaption(p.uuid, p.getId(database) + ": " + p.getText(database));
	            	}
	            }
	        }
	        
	        final Button copy = new Button("Lis‰‰ toteuttava " + map.tavoiteDescription.toLowerCase(), new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1L;
	
				// inline click-listener
	            public void buttonClick(ClickEvent event) {
	            	Object selected = table.getValue();
	            	if(selected == null) return;
	            	main.closeDialog();
	            	callback.selected((String)selected);
	            }
	            
	        });
	        
	        ValueChangeListener l = new ValueChangeListener() {
				
				private static final long serialVersionUID = 192004471077387400L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
	
					Object selection = table.getValue();
					if(selection == null) {
						copy.setEnabled(false);
					} else {
						copy.setEnabled(true);
					}
					
				}
				
			};
	        
			l.valueChange(null);
			
	        table.addValueChangeListener(l);
	        
	        content.addComponent(table);
	        content.setExpandRatio(table, 1.0f);

	        copy.setEnabled(false);
	        
	        buttons.addComponent(copy);

        }

		makeDialog(main, "M‰‰rit‰ uusi " + map.tavoiteDescription.toLowerCase(), "Peruuta", content, buttons);

	}

	public static void selectMonitorTagsDialog(final Main main, Strategiakartta map, final DialogCallback<Collection<Tag>> callback) {

		final Database database = main.getDatabase();
		
		HashSet<Tag> monitorTags = new HashSet<Tag>();
		for(Base b : map.getOwners(database)) {
			monitorTags.addAll(b.getMonitorTags(database));
		}
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

        final OptionGroup table = new OptionGroup();
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        
        for(Tag t : monitorTags) {
        	table.addItem(t);
        	table.setItemCaption(t, t.getId(database));
        }
        
        content.addComponent(table);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
		Button ok = new Button("Valitse", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1657687721482107951L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
            	main.closeDialog();
            	callback.finished((Collection<Tag>)table.getValue());
            }
            
        });
        
        buttons.addComponent(ok);
        buttons.setExpandRatio(ok, 0.0f);

		makeDialog(main, "300px", "600px", "Valitse n‰ytett‰v‰t", "Peruuta", content, buttons);

	}

	public static void selectFocusType(final Main main, Strategiakartta map, Tavoite goal, String desc, final GoalCallback callback) {

		final Database database = main.getDatabase();

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(true);

		Button ok = new Button("Luo uusi " + desc.toLowerCase(), new Button.ClickListener() {
        	
			private static final long serialVersionUID = 2929104928507341296L;

			// inline click-listener
            public void buttonClick(ClickEvent event) {
            	main.closeDialog();
            	callback.selected(null);
            }
            
        });
        
        hl1.addComponent(ok);
        hl1.setExpandRatio(ok, 0.0f);

		Label uusiOma = new Label("Uusi " + desc.toLowerCase() + " luodaan t‰m‰n strategiakartan alle.");
		uusiOma.addStyleName(ValoTheme.LABEL_TINY);
		
        hl1.addComponent(uusiOma);
        hl1.setComponentAlignment(uusiOma, Alignment.MIDDLE_LEFT);
        hl1.setExpandRatio(uusiOma, 1.0f);

        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
        Strategiakartta parent_ = map.getPossibleParent(database);
        
        Set<Painopiste> avail = new HashSet<Painopiste>();
        // Add all
        for(Tavoite t : map.tavoitteet) {
        	for(Painopiste p : t.painopisteet) {
        		avail.add(p);
        	}
        }
        // Remove current
    	for(Painopiste p : goal.painopisteet) {
    		avail.remove(p);
    	}

        if(parent_ != null) {
        
	        final ListSelect table = new ListSelect();
	        table.setSizeFull();
	        table.setNullSelectionAllowed(false);
	        table.setMultiSelect(false);
	        table.setCaption("Lis‰‰ kartan alle m‰‰ritelty " + desc.toLowerCase() + ":");
	        for(Painopiste p : avail) {
	        	table.addItem(p.uuid);
	        	table.setItemCaption(p.uuid, p.getText(database));
	        }
	        
	        final Button copy = new Button("Lis‰‰ olemassaoleva " + desc.toLowerCase(), new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1L;
	
				// inline click-listener
	            public void buttonClick(ClickEvent event) {
	            	Object selected = table.getValue();
	            	if(selected == null) return;
	            	main.closeDialog();
	            	callback.selected((String)selected);
	            }
	            
	        });
	        
	        ValueChangeListener l = new ValueChangeListener() {
				
				private static final long serialVersionUID = 192004471077387400L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
	
					Object selection = table.getValue();
					if(selection == null) {
						copy.setEnabled(false);
					} else {
						copy.setEnabled(true);
					}
					
				}
				
			};
	        
			l.valueChange(null);
			
	        table.addValueChangeListener(l);
	        
	        content.addComponent(table);
	        content.setExpandRatio(table, 1.0f);

	        copy.setEnabled(false);
	        
	        buttons.addComponent(copy);

        }

		makeDialog(main, "M‰‰rit‰ " + desc.toLowerCase(), "Peruuta", content, buttons);

	}
	
	private static void doLogin(Main main, Window subwindow, Label l, String usr, String pass) {

		Database database = main.getDatabase();
    	String hash = Utils.hash(pass);
    	Account acc = Account.find(database, usr);
    	if(acc != null) {
    		if(hash.equals(acc.getHash())) {
            	main.removeWindow(subwindow);
            	main.account = acc;
            	main.hallinnoi.setVisible(acc.isAdmin());
            	main.tili.setVisible(true);
            	Updates.update(main, false);
            	main.login.setCaption("Kirjaudu ulos " + acc.getId(database));
            	main.saveState.setEnabled(true);
    			return;
    		}
    	}
    	l.setVisible(true);

	}
		
	public static void login(final Main main) {

        final Window subwindow = new Window("Anna k‰ytt‰j‰tunnus ja salasana", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("295px");
        subwindow.setResizable(false);

        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        final TextField tf = new TextField();
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.setCaption("K‰ytt‰j‰tunnus:");
        tf.setId("loginUsernameField");
        winLayout.addComponent(tf);

        final PasswordField pf = new PasswordField();
        pf.setCaption("Salasana:");
        pf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf.setWidth("100%");
        pf.setId("loginPasswordField");
        winLayout.addComponent(pf);

        final Label l = new Label("V‰‰r‰ k‰ytt‰j‰tunnus tai salasana");
        l.addStyleName(ValoTheme.LABEL_FAILURE);
        l.addStyleName(ValoTheme.LABEL_TINY);
        l.setVisible(false);
        winLayout.addComponent(l);

        pf.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = -2708082203576343391L;

        	@Override
			public void valueChange(ValueChangeEvent event) {
        		doLogin(main, subwindow, l, tf.getValue(), pf.getValue());
			}
		});

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        
        Button ok = new Button("Kirjaudu", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5148036024457593062L;

			public void buttonClick(ClickEvent event) {
        		doLogin(main, subwindow, l, tf.getValue(), pf.getValue());
            }
            
        });
        
        hl.addComponent(ok);

        Button close = new Button("Peruuta", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5719853213838228457L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
            
        });
        
        hl.addComponent(close);
        
        winLayout.addComponent(hl);
        winLayout.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
		main.addWindow(subwindow);
		
		tf.setCursorPosition(tf.getValue().length());

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

        Button apply = new Button("Tee muutokset", new Button.ClickListener() {
        	
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
    			main.closeDialog();
            	
            }
            
        });
        
        buttons.addComponent(apply);
        
        makeDialog(main, "450px", "480px", "K‰ytt‰j‰tilin asetukset", "Poistu", content, buttons);

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
				table.addItem(new Object[] { r.map.getId(database), r.write ? "Muokkaus" : "Luku", r.recurse ? "Alikartat" : "Valittu kartta" }, i+1);
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
        
		final Button save = new Button("Luo", new Button.ClickListener() {

			private static final long serialVersionUID = -6053708137324681886L;

			public void buttonClick(ClickEvent event) {

				if(!tf.isValid()) return;
				
				String pass = Long.toString(Math.abs( UUID.randomUUID().getLeastSignificantBits()), 36);
				Account.create(database, tf.getValue(), "", Utils.hash(pass));

				Updates.update(main, true);

		        makeAccountCombo(main, accountMap, users);
		        makeAccountTable(database, users, accountMap, table);

				infoDialog(main, "Uusi k‰ytt‰j‰ '" + tf.getValue() + "' luotu", "K‰ytt‰j‰n salasana on " + pass + "", null);

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
		tf.setValue("Uusi k‰ytt‰j‰");
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
				if(accountMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo k‰ytˆss‰");
			}
			
		});
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
		propagateSelect.addItem("Valittu kartta");
		propagateSelect.addItem("Alikartat");
		propagateSelect.select("Valittu kartta");

		final Button addRight = new Button("Lis‰‰ rivi", new Button.ClickListener() {

			private static final long serialVersionUID = -4841787792917761055L;

			public void buttonClick(ClickEvent event) {
				
				Object user = users.getValue();
				Account state = accountMap.get(user);
				
				String mapUUID = (String)mapSelect.getValue();
				Strategiakartta map = database.find(mapUUID);
				String right = (String)rightSelect.getValue();
				String propagate = (String)propagateSelect.getValue();

				Right r = new Right(map, right.equals("Muokkaus"), propagate.equals("Alikartat"));
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

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
		makeDialog(main, "Hallinnoi strategiakarttaa", "Sulje", content, buttons);

	}

	public static void setUserMeter(final Main main, Base base, final Meter m) {

		final Database database = main.getDatabase();

        final Window subwindow = new Window("Aseta mittarin arvo", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("350px");
        subwindow.setResizable(false);

        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        final Label header = new Label(m.getCaption(database));
        header.addStyleName(ValoTheme.LABEL_LARGE);
        winLayout.addComponent(header);
        
		Indicator indicator = m.getPossibleIndicator(database);
		if(indicator == null) return;
		
		Datatype dt = indicator.getDatatype(database);
		if(!(dt instanceof EnumerationDatatype)) return;
		
		AbstractField<?> field = dt.getEditor(main, base, indicator);
		field.setWidth("100%");
		
		winLayout.addComponent(field);
		
		Label l = new Label(indicator.getValueShortComment());
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
        
        hl.addComponent(ok);
		hl.setComponentAlignment(ok, Alignment.BOTTOM_LEFT);

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
        
		final Button remove = new Button("Poista valitut n‰kym‰t", new Button.ClickListener() {

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
				
				main.closeDialog();

			}

		});
		
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
        
		makeDialog(main, "N‰kymien hallinta", "Sulje",  content, buttons);

	}
	
	public static void addMap(final Main main, Strategiakartta map) {

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
		combo.setCaption("Organisaatiotaso:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		for(Base b : level.getRelatedObjects(database, Relation.find(database, Relation.ALLOWS_SUBMAP))) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getId(database));
			combo.select(b.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button("Lis‰‰", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String id = tf.getValue();
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				database.newMap(main, main.uiState.current, id, name, type);
				Updates.updateJS(main, true);
				main.closeDialog();
				

			}
		});
		buttons.addComponent(ok);

		makeDialog(main, "450px", "340px", "Lis‰‰ aliorganisaatio", "Peruuta", content, buttons);
		
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
				
				temp = new File("printing", uuid + ".pdf");

				if(svgText != null) {

					File htmlFile = new File("printing", uuid + ".html");
					File script = new File("printing", uuid + ".js");
					
		        	try {
		        		
		        		String html = PhantomJSDriver.printHtml(svgText);
						Files.write(htmlFile.toPath(), html.getBytes(Charset.forName("UTF-8")));
		        		
						String browserUrl = htmlFile.toURI().toURL().toString();
		        		
			        	String printCommand = PhantomJSDriver.printCommand(browserUrl, temp.getName());
			        	
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

		makeDialog(main, "420px", "135px", "Haluatko ladata kartan PDF-muodossa?", "Sulje", content, buttons);
		
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
		combo.setCaption("Organisaatiotaso:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		content.addComponent(combo);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		for(Base b : level.getRelatedObjects(database, Relation.find(database, Relation.ALLOWS_SUBMAP))) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getId(database));
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
		
		Button ok = new Button("Lis‰‰", new Button.ClickListener() {
			
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
				main.closeDialog();

			}
			
		});
		buttons.addComponent(ok);

		makeDialog(main, "450px", "380px", "Lis‰‰ n‰kym‰", "Peruuta", content, buttons);
		
	}

	public static void makeDialog(final Main main, String caption, String back, Component content_, HorizontalLayout buttons) {
		makeDialog(main, main.dialogWidth(), main.dialogHeight(), caption, back, content_, buttons);
	}

	public static void makeDialog(final Main main, String w, String h, String caption, String back, Component content_, HorizontalLayout buttons) {

		if(buttons == null) {
			buttons = new HorizontalLayout();
	        buttons.setSpacing(true);
	        buttons.setMargin(true);
		}
		
		if(back != null) {
	        Button close = new Button(back, new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1992235622970234624L;
	
	            public void buttonClick(ClickEvent event) {
	    			main.closeDialog();
	            }
	            
	        });
	        buttons.addComponent(close);
		}
		
        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth(w);
        subwindow.setHeight(h);
        subwindow.setResizable(true);
        
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Panel content = new Panel();
        content.setSizeFull();
        content.addStyleName(ValoTheme.PANEL_BORDERLESS);
        content.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
        content.setContent(content_);
        
        winLayout.addComponent(content);
        winLayout.setExpandRatio(content, 1.0f);
        winLayout.setComponentAlignment(content, Alignment.BOTTOM_CENTER);

        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
        main.closeDialog();

		main.addWindow(subwindow);

		main.modalDialog = subwindow;
		
	}

	public static void errorDialog(final Main main, String caption, Component content_) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth(main.dialogWidth());
        subwindow.setHeight(main.dialogHeight());
        subwindow.setResizable(false);

		Button close = new Button("Jatka", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });
        
        buttons.addComponent(close);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Panel content = new Panel();
        content.addStyleName(ValoTheme.PANEL_BORDERLESS);
        content.setSizeFull();
        
        content.setContent(content_);
        
        winLayout.addComponent(content);
        winLayout.setExpandRatio(content, 1.0f);
        winLayout.setComponentAlignment(content, Alignment.BOTTOM_CENTER);

        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
        main.closeDialog();

		main.addWindow(subwindow);
		
	}

	public static void confirmDialog(final Main main, String text, String ok, String cancel, final Runnable runnable) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window("Vahvistus", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("150px");
        subwindow.setResizable(true);

		Button okButton = new Button(ok, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
    			runnable.run();
            }
            
        });
        
		Button cancelButton = new Button(cancel, new Button.ClickListener() {
			
			private static final long serialVersionUID = -5227602164819268383L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });

		buttons.addComponent(okButton);
		buttons.addComponent(cancelButton);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Label l = new Label(text);
        winLayout.addComponent(l);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
        main.closeDialog();

		main.addWindow(subwindow);
		
	}

	public static void infoDialog(final Main main, String caption, String text, final Runnable runnable) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("150px");
        subwindow.setResizable(false);
        
		Button okButton = new Button("Jatka", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
    			if(runnable != null)
    				runnable.run();
            }
            
        });

		buttons.addComponent(okButton);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Label l = new Label(text);
        l.addStyleName(ValoTheme.LABEL_LARGE);
        winLayout.addComponent(l);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
        main.closeDialog();

		main.addWindow(subwindow);
		
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
	
	public static void commentDialog(final Main main, String caption, String okCaption, String nullCaption, final CommentCallback runnable) {

        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("450px");
        
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        final TextField s = new TextField("");
        s.setCaption("Anna lyhyt m‰‰re n‰ytett‰v‰ksi arvon yhteydess‰");
        s.addStyleName(ValoTheme.TEXTFIELD_TINY);
        s.setWidth("100%");
        winLayout.addComponent(s);
        winLayout.setExpandRatio(s, 0.0f);
        
        final TextArea l = new TextArea("");
        l.setCaption("Anna vapaamuotoinen kuvaus p‰ivitykseen liittyen");
        l.addStyleName(ValoTheme.TEXTAREA_TINY);
        l.setSizeFull();
        winLayout.addComponent(l);
        winLayout.setExpandRatio(l, 1.0f);

		Button okButton = new Button(okCaption, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
            	String shortComment = s.getValue();
            	String comment = l.getValue();
    			main.removeWindow(subwindow);
    			if(runnable != null)
    				runnable.runWithComment(shortComment, comment);
            }
            
        });

		Button nullButton = new Button(nullCaption, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
    			if(runnable != null)
    				runnable.runWithComment(null, null);
            }
            
        });

		Button cancelButton = new Button("Peruuta", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
		buttons.addComponent(okButton);
		buttons.addComponent(nullButton);
		buttons.addComponent(cancelButton);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
		main.addWindow(subwindow);
		
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
        	
			private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
            	String idValue = tf.getValue();
            	String value = ta.getValue();
            	main.removeWindow(subwindow);
            	container.modifyId(main, idValue);
            	container.modifyText(main, value);
            	Collection<String> tags = Utils.extractTags(value);
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
		
		private String customFilterString;
		
		@Override
		public void changeVariables(Object source, Map<String, Object> variables) {
			super.changeVariables(source, variables);
			customFilterString = (String) variables.get("filter");
		}
		
	}
	
	static class CustomLazyContainer extends IndexedContainer {
		
		private static final long serialVersionUID = -4520139213434183947L;
		
		private Database database;
		private String filterString;
		private TagCombo combo;
		
		final private List<Tag> tags;

		public CustomLazyContainer(Database database, TagCombo combo, List<Tag> tags) {
			this.database = database;
			this.tags = tags;
			this.combo = combo;
			addContainerProperty("id", String.class, "");
			doFilter();
		}
		
		public String getFilterString() {
			return filterString;
		}

		@Override
		public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
			if (filter == null)
			{
				removeAllItems();
				filterString = null;
				return;
			}

			removeAllItems();

			if (filter instanceof SimpleStringFilter)
			{
				String newFilterString = combo.customFilterString;

				if (newFilterString == null)
					return;

				if (newFilterString.equals(filterString))
					return;

				filterString = newFilterString;

				if (filterString.length() < 1)
					return;

				doFilter();
				super.addContainerFilter(filter);
			}
		}

		@SuppressWarnings("unchecked")
		private void doFilter() {
			for(Tag t : tags) {
				Item item = addItem(t.getId(database));
				item.getItemProperty("id").setValue(t.getId(database));
			}
			if(filterString != null) {
				Item item = addItem(filterString);
				item.getItemProperty("id").setValue(filterString);
			}
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
	
	public abstract static class Action implements Runnable {
		
		public String caption;
		
		public Action(String caption) {
			this.caption = caption;
		}
		
		public boolean accept() {
			return true;
		}

		
	}
	
	private static String formatPageName(String pageName) {
		String formatted = pageName.replaceAll(" ", "_").replaceAll("\\.", "_"); 
		return formatted;
	}
	
	public static String makeTavoitePageName(Database database, Strategiakartta map, Tavoite base) {

		String pageName = Main.wikiPrefix;
		
		if(map != null) pageName += map.getId(database) + "_";

		pageName += base.getId(database);
		
		return formatPageName(pageName);

	}
	
	public static String makeWikiPageName(Database database, Base base) {

		String pageName = Main.wikiPrefix;
		
		if(base instanceof Strategiakartta) {
			
			pageName += base.getId(database);
			return formatPageName(pageName);
			
		} else if (base instanceof Tavoite) {

			return makeTavoitePageName(database, database.getMap(base), (Tavoite)base);
			
		} else if (base instanceof Painopiste) {
			
			Painopiste p = (Painopiste)base;

			Strategiakartta map = p.getMap(database);
			if(map != null) pageName += map.getId(database) + "_";

			Tavoite t = p.getGoal(database);
			if(t != null) pageName += t.getId(database) + "_";

			pageName += base.getId(database);
			return formatPageName(pageName);

		} else {

			Base owner = base.getOwner(database);
			if(owner != null) {
				pageName = makeWikiPageName(database, owner);
				pageName += "_" + base.getText(database);
				return formatPageName(pageName);
			}
			
		}

		return null;
		
	}
	
	public static void openWiki(Main main, Base base) {
		
		final Database database = main.getDatabase();

		String pageName = makeWikiPageName(database, base);
		if(pageName == null) return;

		main.wiki.setSource(new ExternalResource("https://www.simupedia.com/strategiakartta/index.php/"+ pageName));
		main.wikiPage = pageName;
		main.wikiBase = base;

		UIState s = main.uiState.duplicate(main);
		main.setTabState(s, 2);
		main.setFragment(s, true);

	}
	
	public static void selectAction(final Main main, Double x, Double y, final Base container, final Base base) {

		final Database database = main.getDatabase();

		List<Action> actions = new ArrayList<Action>();
		if(base instanceof Meter) return;
		
		Strategiakartta baseMap = database.getMap(base);
		boolean generated = !baseMap.generators.isEmpty();

		if(!generated) {
			
			actions.add(new Action("Strategian toteutus") {
	
				@Override
				public void run() {
	
					main.getUIState().level = 3;
					main.getUIState().setCurrentFilter(new TulostavoiteToimenpideFilter(main, base, database.getMap(base)));
					
					main.setCurrentItem(base, database.getMap(base));
					main.switchToBrowser();
					
				}
				
			});
		
			actions.add(new Action("Valmiusasteet") {
	
				@Override
				public void run() {
					
					Strategiakartta map = database.getMap(base);
					main.getUIState().currentPosition = map;
	
					main.getUIState().setCurrentFilter(new MeterFilter(main, base, main.getUIState().currentPosition));
					if(base instanceof Tavoite) {
						main.getUIState().level = 4;
						main.less.setEnabled(true);
					} else {
						main.getUIState().level = 2;
						main.less.setEnabled(false);
					}
					
					main.setCurrentItem(base, main.getUIState().currentPosition);
					main.switchToBrowser();
	
				}
				
			});

			actions.add(new Action("Tulostavoitteet") {
				
				@Override
				public void run() {
					
					Strategiakartta map = database.getMap(base);
					main.getUIState().currentPosition = map;
	
					main.getUIState().setCurrentFilter(new TulostavoiteFilter(main, base, main.getUIState().currentPosition));
					main.getUIState().level = 10;
					
					main.setCurrentItem(base, main.getUIState().currentPosition);
					main.switchToBrowser();
	
				}
				
			});
			
			actions.add(new Action("Tausta-asiakirja") {
	
				@Override
				public void run() {
	
					openWiki(main, base);
					
				}
				
			});
	
			actions.add(new Action("Ominaisuudet") {
	
				@Override
				public void run() {
					
					main.setCurrentItem(base, main.getUIState().currentPosition);
					main.switchToProperties();
					
				}
				
			});
			
		}
		
		if(main.getUIState().tabState != 0) {
			actions.add(new Action("Avaa strategiakartassa") {

				@Override
				public void run() {

					Strategiakartta map = database.getMap(base);
					
					main.getUIState().current = map;
					Updates.updateJS(main, false);
					main.switchToMap();
					
				}
				
			});
		}

		if(base instanceof Tavoite) {
			
			Tavoite goal = (Tavoite)base;

			if(!generated) {
				actions.add(new Action("Voimavarat") {
	
					@Override
					public void run() {
						
						openWiki(main, base);
						
					}
					
				});
			}
			
			final Strategiakartta map = database.getMap(base);
			final String desc = goal.getFocusDescription(database);

			if(Account.canWrite(main, base) && !generated) {

				actions.add(new Action("Lis‰‰ " + desc.toLowerCase()) {

					@Override
					public void run() {
						
						ObjectType ppType = getFocusType(database, map);
						boolean manyImplements = getManyImplements(database, ppType);
						
						if(manyImplements) {

							Utils.selectFocusType(main, map, (Tavoite)base, desc, new GoalCallback() {
								
								@Override
								public void selected(String uuid) {

									if(uuid != null) {
										Painopiste exist = database.find(uuid);
										Tavoite t = (Tavoite)base;
										t.add(database, exist);
									} else {
										Painopiste.create(database, map, (Tavoite)base, "Uusi " + desc);
									}
									Updates.updateJS(main, true);
									
								}
								
							});
							
						} else {

							Painopiste.create(database, map, (Tavoite)base, "Uusi " + desc);
							Updates.updateJS(main, true);

						}

					}
					
				});

				
				MoveUp moveUp = new MoveUp(main, (Tavoite)base);
				if(moveUp.accept()) actions.add(moveUp);
				MoveDown moveDown = new MoveDown(main, (Tavoite)base);
				if(moveDown.accept()) actions.add(moveDown);
				
				actions.add(new Action("Poista") {
					
					@Override
					public void run() {

						Tavoite t = (Tavoite)base;
						if(t.painopisteet.length > 0) {

							VerticalLayout la = new VerticalLayout();
							Label l = new Label("Vain tyhj‰n m‰‰rityksen voi poistaa. Poista ensin jokainen " + desc.toLowerCase() +  " ja yrit‰ sen j‰lkeen uudestaan.");
							l.addStyleName(ValoTheme.LABEL_H3);
							la.addComponent(l);
							Utils.errorDialog(main, "Poisto estetty", la);
							return;
							
						}
						
						Utils.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + base.getId(database) + " ?", "Poista", "Peruuta", new Runnable() {

							@Override
							public void run() {
								
								Tavoite t = (Tavoite)base;
								t.remove(database);
								map.fixRows();
								Updates.updateJS(main, true);
							}
							
						});

					}

				});

			}
			
		}
		
		if(base instanceof Painopiste) {
			
			if(Account.canWrite(main, base) && !generated) {

				actions.add(new Action("M‰‰rit‰ toteuttavat organisaatiot") {

					@Override
					public void run() {
						
						Utils.defineImplementors(main,base);					
						
					}
					
				});

				final Tavoite t = database.getTavoite((Painopiste)base);
				int pos = t.findPainopiste((Painopiste)base);
				if(pos > 0) {
					actions.add(new Action("Siirr‰ ylemm‰s") {
	
						@Override
						public void run() {
	
							t.moveUp((Painopiste)base);
							Updates.updateJS(main, true);
	
						}
	
					});
				}
				if(pos < t.painopisteet.length - 1) {
					actions.add(new Action("Siirr‰ alemmas") {
	
						@Override
						public void run() {
	
							t.moveDown((Painopiste)base);
							Updates.updateJS(main, true);
	
						}
	
					});
				}

				if(container != null) {
				
					actions.add(new Action("Poista") {
						
						@Override
						public void run() {
	
							Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
							Collection<Base> implementors = database.getInverse(base, implementsRelation);
							if(!implementors.isEmpty()) {
	
								VerticalLayout la = new VerticalLayout();
								Label l = new Label("Poista ensin t‰h‰n viittaavat m‰‰ritykset:");
								l.addStyleName(ValoTheme.LABEL_H3);
								la.addComponent(l);
								for(Base b : implementors) {
									Strategiakartta map = database.getMap(b);
									Label l2 = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + b.getId(database) + " - " + b.getText(database) + " (" + map.getId(database) + ")");
									l2.setContentMode(ContentMode.HTML);
									la.addComponent(l2);
								}
								Utils.errorDialog(main, "M‰‰ritys on k‰ytˆss‰, eik‰ sit‰ voida poistaa", la);
								return;
								
							}
							
							Utils.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + base.getId(database) + " ?", "Poista", "Peruuta", new Runnable() {
	
								@Override
								public void run() {
									
									Strategiakartta map = database.getMap(base);
									Painopiste p = (Painopiste)base;
									
									int refs = 0;
									for(Tavoite t : map.tavoitteet) {
										for(Painopiste p2 : t.painopisteet) {
											if(p2.equals(base)) refs++;
										}
									}
									
									Tavoite t = (Tavoite)container;
									if(refs == 1) p.remove(database);
									else t.removePainopiste(database, p);
									map.fixRows();
									Updates.updateJS(main, true);
									
								}
								
							});
	
						}
	
					});
					
				}
				
			}
			
		}

		if(base instanceof Strategiakartta) {
			
			final Strategiakartta map = (Strategiakartta)base;
			if(!map.generators.isEmpty()) {

				actions.add(new Action("P‰ivit‰ n‰kym‰") {

					@Override
					public void run() {
						
						map.generate(main);
						Updates.updateJS(main, false);
						
					}
					
				});

			}
			
			if(Account.canWrite(main, base) && !generated) {
				
				actions.add(new Action("Lis‰‰ " + map.tavoiteDescription.toLowerCase()) {

					@Override
					public void run() {
						
						Utils.selectGoalType(main, map, new GoalCallback() {
							
							@Override
							public void selected(String uuid) {
								if(uuid != null) {
									Base copy = database.find(uuid);
									Tavoite.createCopy(database, main.uiState.current, copy);
								} else {
									Tavoite.create(database, main.uiState.current, "Oma " + map.ownGoalDescription.toLowerCase());
								}
								Updates.updateJS(main, true);
							}
							
						});
						
					}
					
				});

				actions.add(new Action("Lis‰‰ aliorganisaatio") {

					@Override
					public void run() {
						
						Utils.addMap(main, (Strategiakartta)base);
						
					}
					
				});

				actions.add(new Action("Lis‰‰ n‰kym‰") {

					@Override
					public void run() {
						
						Utils.addView(main, (Strategiakartta)base);
						
					}
					
				});
				
			}
			
			if(Account.canWrite(main, base)) {

				actions.add(new Action("Poista") {
					
					@Override
					public void run() {

						if(map.tavoitteet.length > 0 && map.generators.isEmpty()) {

							VerticalLayout la = new VerticalLayout();
							Label l = new Label("Vain tyhj‰n kartan voi poistaa. Poista ensin jokainen " + map.tavoiteDescription.toLowerCase() +  " ja yrit‰ sen j‰lkeen uudestaan.");
							l.addStyleName(ValoTheme.LABEL_H3);
							la.addComponent(l);
							Utils.errorDialog(main, "Poisto estetty", la);
							return;
							
						}
						
						Utils.confirmDialog(main, "Haluatko varmasti poistaa kartan " + base.getId(database) + " ?", "Poista", "Peruuta", new Runnable() {

							@Override
							public void run() {
								
								Strategiakartta parent = map.getPossibleParent(database);
								database.remove(map);
								if(parent != null)
									main.getUIState().current = parent;
								
								Updates.updateJS(main, true);
							}
							
						});

					}

				});
				
				
			}
			
			if(main.getUIState().tabState == 1) {
				
				actions.add(new Action("Rajaa tarkastelu t‰m‰n alle") {
	
					@Override
					public void run() {
						main.setCurrentItem(main.getUIState().currentItem, (Strategiakartta)base);
					}
					
				});
				
			}

		}
		
		if(main.getUIState().showTags) {
			
			actions.add(new Action("Piilota aihetunnisteet") {
				
				@Override
				public void run() {

					UIState s = main.getUIState().duplicate(main);
					s.showTags = false;
					main.setFragment(s, true);

				}
				
			});

			if(base instanceof Strategiakartta) {

				actions.add(new Action("Valitse aihetunnisteet") {

					@Override
					public void run() {

						Utils.selectMonitorTagsDialog(main, (Strategiakartta)base, new DialogCallback<Collection<Tag>>() {

							@Override
							public void finished(Collection<Tag> result) {

								UIState s = main.getUIState().duplicate(main);
								s.shownTags = new ArrayList<Tag>(result);
								main.setFragment(s, true);

								Updates.update(main, true);

							}

							@Override
							public void canceled() {
							}

						});

					}

				});

			}
			
		} else {
			
			actions.add(new Action("N‰yt‰ aihetunnisteet") {
				
				@Override
				public void run() {
					
					UIState s = main.getUIState().duplicate(main);
					s.showTags = true;
					main.setFragment(s, true);
					
				}
				
			});
			
		}
		
		if(main.getUIState().showMeters) {
			
			actions.add(new Action("Piilota mittarit") {
				
				@Override
				public void run() {

					UIState s = main.getUIState().duplicate(main);
					s.showMeters = false;
					main.setFragment(s, true);

				}
				
			});

			
		} else {
			
			actions.add(new Action("N‰yt‰ mittarit") {
				
				@Override
				public void run() {
					
					UIState s = main.getUIState().duplicate(main);
					s.showMeters = true;
					main.setFragment(s, true);
					
				}
				
			});
			
		}

		if(main.getUIState().showVoimavarat) {
			
			actions.add(new Action("Piilota voimavarat") {
				
				@Override
				public void run() {

					UIState s = main.getUIState().duplicate(main);
					s.showVoimavarat = false;
					main.setFragment(s, true);

				}
				
			});

			
		} else {
			
			actions.add(new Action("N‰yt‰ voimavarat") {
				
				@Override
				public void run() {
					
					UIState s = main.getUIState().duplicate(main);
					s.showVoimavarat = true;
					main.setFragment(s, true);
					
				}
				
			});
			
		}
		
		Action cancel = new Action("Peruuta") {

			@Override
			public void run() {
				
			}
			
		}; 
		
		actions.add(cancel);
		
		if(actions.size() == 0) return;
		if(actions.size() == 1) {
			actions.get(0).run();
			return;
		}
		
		VerticalLayout menu = new VerticalLayout();
		final PopupView openerButton = new PopupView("", menu);
		
		menu.setWidth("500px");
		
		Label header = new Label(base.getId(database));
		header.addStyleName(ValoTheme.LABEL_HUGE);
		header.setSizeUndefined();
		menu.addComponent(header);
		menu.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

		Label header2 = new Label("valitse toiminto");
		header2.addStyleName(ValoTheme.LABEL_LIGHT);
		header2.addStyleName(ValoTheme.LABEL_SMALL);
		header2.setSizeUndefined();
		menu.addComponent(header2);
		menu.setComponentAlignment(header2, Alignment.MIDDLE_CENTER);

		for(Action a : actions)
			addAction(main, openerButton, menu, a.caption, a);
		
		openerButton.setHideOnMouseOut(false);
		openerButton.setPopupVisible(true);
		
		main.abs.addComponent(openerButton, "left: " + x.intValue() + "px; top: " + y.intValue() + "px");

	}
	
	private static void addAction(final Main main, final PopupView openerButton, final VerticalLayout menu, String caption, final Action r) {

		Button b1 = new Button(caption);
		b1.addStyleName(ValoTheme.BUTTON_QUIET);
		b1.addStyleName(ValoTheme.BUTTON_LARGE);
		b1.setWidth("100%");
		b1.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 7150528981216406326L;

			@Override
			public void buttonClick(ClickEvent event) {
				r.run();
				main.abs.removeComponent(openerButton);
			}
			
		});
		if("Peruuta".equals(caption)) {
			b1.setClickShortcut(KeyCode.ESC);
		}
		menu.addComponent(b1);

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
	
	public static boolean getManyImplements(Database database, ObjectType type) {

		Property p = Property.find(database, Property.MANY_IMPLEMENTS);
		return Boolean.parseBoolean(p.getPropertyValue(type));

	}
	
	public static boolean getManyImplementor(Database database, ObjectType type) {

		Property p = Property.find(database, Property.MANY_IMPLEMENTOR);
		return Boolean.parseBoolean(p.getPropertyValue(type));

	}

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

	public static ObjectType getFocusType(Database database, Strategiakartta map) {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);
		
		return database.find(focusTypeUUID);

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
		for(Base b2 : imp) {
			result.add(b2);
			Set<Base> bs = getImplementationSet(database, b2);
			result.addAll(bs);
		}
		return result;
	}
	
	public static Collection<Base> getDirectImplementors(Database database, Base b) {
		return database.getInverse(b, Relation.find(database, Relation.IMPLEMENTS));
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

	
}