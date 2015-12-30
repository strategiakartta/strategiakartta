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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;

public class Tag extends Base {
	
	private static final long serialVersionUID = 844900680130018383L;
	
	public static final String LIIKENNE = "Liikenne";
	public static final String VIESTINTA = "Viestintä";
	public static final String VOIMAVARAT = "Voimavarat";
	
	public String color;

	public static Tag create(Database database, String id, String text, String color) {
		Tag p = new Tag(id, text, color);
		database.register(p);
		return p;
	}
	
	private Tag(String id, String text, String color) {
		super(UUID.randomUUID().toString(), id, text);
		this.color = color;
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static List<Tag> enumerate(Database database) {
		
		ArrayList<Tag> result = new ArrayList<Tag>();
		for(Base b : database.objects.values()) {
			if(b instanceof Tag) result.add((Tag)b);
		}
		return result;

	}
	
	@Override
	public String getDescription(Database database) {
		return getId(database) + " (Aihetunniste)";
	}
	
	public static void updateRelatedTags(final Main main, boolean canWrite) {
		
		final Database database = main.getDatabase();

		final Base base = main.getUIState().currentItem;
		
		Collection<Tag> tags = base.getRelatedTags(database);
		if(!tags.isEmpty() || canWrite) {

			HorizontalLayout tagHeader = new HorizontalLayout();
			tagHeader.setSpacing(true);

			Label header2 = new Label("Aihetunnisteet");
			header2.setHeight("32px");
			header2.addStyleName(ValoTheme.LABEL_HUGE);
			header2.addStyleName(ValoTheme.LABEL_BOLD);
			tagHeader.addComponent(header2);
			tagHeader.setComponentAlignment(header2, Alignment.BOTTOM_CENTER);

			if(canWrite) {
				final Image editTags = new Image("", new ThemeResource("tag_blue_edit.png"));
				editTags.setHeight("24px");
				editTags.setWidth("24px");
				editTags.addClickListener(new MouseEvents.ClickListener() {

					private static final long serialVersionUID = -6140867347404571880L;

					@Override
					public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
						Utils.loseFocus(editTags);
						Utils.editTags(main, "Muokkaa aihetunnisteita", main.getUIState().currentItem);
					}

				});
				tagHeader.addComponent(editTags);
				tagHeader.setComponentAlignment(editTags, Alignment.BOTTOM_CENTER);
			}

			main.properties.addComponent(tagHeader);
			main.properties.setComponentAlignment(tagHeader, Alignment.MIDDLE_CENTER);
			
			HorizontalLayout divider = new HorizontalLayout();
			main.properties.addComponent(divider);
			main.properties.setComponentAlignment(divider, Alignment.MIDDLE_CENTER);
			
			VerticalLayout left = new VerticalLayout();
			left.setSpacing(true);
			left.setWidth("400px");
			left.setMargin(true);
			divider.addComponent(left);
			VerticalLayout right = new VerticalLayout();
			right.setSpacing(true);
			right.setWidth("400px");
			right.setMargin(true);
			divider.addComponent(right);

			Set<Tag> monitoredTags = getMonitoredTags(database, base);
			
			int i = 0;
	        for(final Tag tag : tags) {
	        	
	        	final boolean monitor = base.hasMonitorTag(database, tag);
	        	String tagId = tag.getId(database);
	        	
	        	HorizontalLayout hl = new HorizontalLayout();
	        	hl.setSpacing(true);
	        	hl.setHeight("37px");
	        	
	        	Button tagButton = Utils.tagButton(database, "list", tagId, i++);
	        	left.addComponent(tagButton);
	        	left.setComponentAlignment(tagButton, Alignment.MIDDLE_RIGHT);

	        	if(canWrite) {
	        		Button b = new Button();
	    	        b.addStyleName(ValoTheme.BUTTON_BORDERLESS);
	            	b.setIcon(FontAwesome.TIMES_CIRCLE);
	            	b.addClickListener(new ClickListener() {
	            		
	    				private static final long serialVersionUID = -4473258383318654850L;

	    				@Override
	    				public void buttonClick(ClickEvent event) {
	    					base.removeRelatedTags(database, tag);
	    					base.removeMonitorTags(database, tag);
							Utils.loseFocus(main.properties);
							Updates.update(main, true);
	    				}
	    				
	    			});
		        	hl.addComponent(b);
		        	hl.setComponentAlignment(b, Alignment.MIDDLE_LEFT);
	        	}

	        	if(base instanceof Strategiakartta) {
	        	
		        	Button tagButton2 = new Button();
		        	tagButton2.setCaption(monitor ? "Seurataan toteutuksessa" : "Ei seurata toteutuksessa");
		        	tagButton2.addStyleName(monitor ? "greenButton" : "redButton");
		        	tagButton2.addStyleName(ValoTheme.BUTTON_SMALL);
		        	tagButton2.setWidth("200px");
		        	if(canWrite) {
			        	tagButton2.addClickListener(new ClickListener() {
		
							private static final long serialVersionUID = -1769769368034323594L;
		
							@Override
							public void buttonClick(ClickEvent event) {
								if(monitor) {
									base.removeMonitorTags(database, tag);
								} else {
									base.assertMonitorTags(database, tag);
								}
								Utils.loseFocus(main.properties);
								Updates.update(main, true);
							}
			        		
			        	});
		        		tagButton2.setEnabled(true);
		        	} else {
		        		tagButton2.setEnabled(false);
		        	}
		        	
		        	hl.addComponent(tagButton2);
		        	hl.setComponentAlignment(tagButton2, Alignment.MIDDLE_LEFT);
		        	
	        	} else {
	        	
		        	if(monitoredTags.contains(tag)) {
		        		Label l = new Label(" toteuttaa seurattavaa aihetta ");
			        	hl.addComponent(l);
			        	hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
		        	}
		        	
	        	}

	        	right.addComponent(hl);
	        	right.setComponentAlignment(hl, Alignment.MIDDLE_LEFT);

	        }

		}

	}
	
	public static Set<Tag> getMonitoredTags(Database database, Base base) {

		Set<Tag> monitoredTags = new HashSet<Tag>();
		Strategiakartta map = database.getMap(base);
		for(Base b : map.getOwners(database)) {
			monitoredTags.addAll(b.getMonitorTags(database));
		}
		return monitoredTags;

	}
	
	public static void updateMonitoredTags(final Main main, boolean canWrite) {
		
		final Database database = main.getDatabase();

		final Base base = main.getUIState().currentItem;

		if(!(base instanceof Tavoite  || base instanceof Painopiste)) return;

		Set<Tag> monitoredTags = getMonitoredTags(database, base);
		monitoredTags.removeAll(base.getRelatedTags(database));
		for(Base impl : Utils.getImplementationSet(database, base)) {
			monitoredTags.removeAll(impl.getRelatedTags(database));
		}
		
		if(!monitoredTags.isEmpty() || canWrite) {

			HorizontalLayout tagHeader = new HorizontalLayout();
			tagHeader.setSpacing(true);
			
			Label header2 = new Label("Seurattavat aihetunnisteet");
			header2.setHeight("32px");
			header2.addStyleName(ValoTheme.LABEL_HUGE);
			header2.addStyleName(ValoTheme.LABEL_BOLD);
			tagHeader.addComponent(header2);
			tagHeader.setComponentAlignment(header2, Alignment.BOTTOM_CENTER);

			main.properties.addComponent(tagHeader);
			main.properties.setComponentAlignment(tagHeader, Alignment.MIDDLE_CENTER);
			
			HorizontalLayout divider = new HorizontalLayout();
			main.properties.addComponent(divider);
			main.properties.setComponentAlignment(divider, Alignment.MIDDLE_CENTER);
			
			VerticalLayout left = new VerticalLayout();
			left.setSpacing(true);
			left.setWidth("400px");
			left.setMargin(true);
			divider.addComponent(left);
			VerticalLayout right = new VerticalLayout();
			right.setSpacing(true);
			right.setWidth("400px");
			right.setMargin(true);
			divider.addComponent(right);

			int i = 0;
	        for(final Tag tag : monitoredTags) {
	        	
	        	String tagId = tag.getId(database);
	        	
	        	HorizontalLayout hl = new HorizontalLayout();
	        	hl.setSpacing(true);
	        	hl.setHeight("37px");
	        	
	        	Button tagButton = Utils.tagButton(database, "inferred", tagId, i++);
	        	left.addComponent(tagButton);
	        	left.setComponentAlignment(tagButton, Alignment.MIDDLE_RIGHT);

	        	Button tagButton2 = new Button();
	        	tagButton2.setCaption("Merkitse toteuttajaksi");
	        	tagButton2.addStyleName("redButton");
	        	tagButton2.addStyleName(ValoTheme.BUTTON_SMALL);
	        	tagButton2.setWidth("200px");
	        	if(canWrite) {
	        		tagButton2.addClickListener(new ClickListener() {

	        			private static final long serialVersionUID = -1769769368034323594L;

	        			@Override
	        			public void buttonClick(ClickEvent event) {
	        				base.assertRelatedTags(database, tag);
	        				Utils.loseFocus(main.properties);
	        				Updates.update(main, true);
	        			}

	        		});
	        		tagButton2.setEnabled(true);
	        	} else {
	        		tagButton2.setEnabled(false);
	        	}
	        	hl.addComponent(tagButton2);
	        	hl.setComponentAlignment(tagButton2, Alignment.MIDDLE_LEFT);

	        	right.addComponent(hl);
	        	right.setComponentAlignment(hl, Alignment.MIDDLE_LEFT);

	        }

		}

	}

	public double getCoverage(Database database, Base owner) {
		
		if(owner.hasRelatedTag(database, this)) return 1.0;
		
		Collection<Base> imp = Utils.getDirectImplementors(database, owner);
		if(imp.isEmpty()) return 0.0;
		
		double result = 0.0;
		double coeff = 1.0 / imp.size();
		for(Base b : imp) {
			result += coeff*getCoverage(database, b);
		}
		return result;
		
	}
	
}
