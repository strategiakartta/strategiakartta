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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.Page.Styles;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;

public class Meter extends Base {

	private static final long serialVersionUID = 7697435820705311629L;

	private TrafficValuation trafficValuation;
	
	@Deprecated
	final public double[] limits;
	@Deprecated
	private double userValue = 0;
	@Deprecated
	private boolean userDefined = false;
	
	private boolean showInMap = true;

	public static Meter create(Database database, String id, String text,
			TrafficValuation trafficValuation) {
		Meter p = new Meter(id, text, trafficValuation);
		database.register(p);
		return p;
	}

	public static Meter create(Database database, Base container, String id, String text,
			TrafficValuation valuation) {
		Meter result = create(database, id, text, valuation);
		container.addMeter(result);
		return result;
	}

	private Meter(String id, String text, TrafficValuation valuation) {
		super(UUID.randomUUID().toString(), id, text);
		this.trafficValuation = valuation;
		limits = null;
	}

	@Override
	public Base getOwner(Database database) {
		for(Base b : database.enumerate()) {
			if(b.getMeters(database).contains(this))
				return b;
		}
		return null;
	}
	
	public static Map<Base,List<Meter>> metersByParent(Database database, Collection<Meter> meters) {
		Map<Base,List<Meter>> result = new HashMap<Base,List<Meter>>();
		for(Meter m : meters) {
			Base owner = m.getOwner(database);
			List<Meter> exist = result.get(owner);
			if(exist == null) {
				exist = new ArrayList<Meter>();
				result.put(owner, exist);
			}
			exist.add(m);
		}
		return result;
	}
	
	public List<Meter> getSubmeters(Database database) {

		// User-defined meter
		if(userDefined) return Collections.emptyList();
		
		// Indicator-defined meter
		if(limits != null) return Collections.emptyList();

		Base owner = getOwner(database);
		if(owner == null) return Collections.emptyList();
		
		ArrayList<Meter> result = new ArrayList<Meter>();
		collectSubmeters(database, owner, result);
		
		return result;

	}
	
	private static void collectSubmeters(Database db, Base b, List<Meter> result) {

		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
		for(Base implementor : db.getInverse(b, implementsRelation)) {
			Collection<Meter> meters = implementor.getMeters(db);
			if(meters.isEmpty()) {
				collectSubmeters(db, implementor, result);
			} else {
				result.addAll(meters);
			}
		}

	}

	public Indicator getPossibleIndicator(Database database) {

		Indicator result = null;
		Relation measures = Relation.find(database, Relation.MEASURES);
		for (Pair p : measures.getRelations(this)) {
			Base b = database.find(p.second);
			if (b instanceof Indicator) {
				if(result != null) return null;
				result = (Indicator) b;
			}
		}
		return result;

	}

	public String explain(Database database) {

		Indicator i = getPossibleIndicator(database);
		if(i != null)
			return "" + i.getValue();
		else
			return "" + (int)(100.0 * value(database)) + "%";

	}
	
	public String describe(Database database) {

		Indicator i = getPossibleIndicator(database);
		if(i != null) {
			Object value = i.getValue();
			if(value == null) return "arvoa ei ole annettu";
			Datatype datatype = i.getDatatype(database);
			return datatype.format(value) + " " + i.getUnit();
		} else {
			if(userDefined) {
				if(userValue < 0.4) return "Ei toteutunut";
				else if(userValue > 0.6) return "Toteutunut";
				else return "Osittain toteutunut";
			} else {
				Collection<Meter> ms = getSubmeters(database);
				double value = value(database);
				int pct = (int)(100.0*value);
				if(ms.size() == 0) {
					return "" + pct + "% (ei mittareita)";
				} else if (ms.size() == 1) {
					return "" + pct + "% (1 mittari)";
				} else {
					return "" + pct + "% (" + ms.size() + " mittaria)";
				}
			}
			
		}

	}

	public double value(Database database) {

		if(userDefined) return userValue;

		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			String color = getTrafficColor(database);
			if(TrafficValuation.GREEN.equals(color)) return 1.0;
			else if(TrafficValuation.YELLOW.equals(color)) return 0.5;
			else return 0.0;
		}

		double result = 0.0;
		int contributions = 0;
		Collection<Meter> submeters = getSubmeters(database);
		if(submeters.isEmpty()) return 0.0;
		
		for (Meter m : submeters) {
			result += m.value(database);
			contributions++;
		}
		
		if(contributions > 0)
			result /= contributions;
		
		return result;
		
	}

	public static List<Meter> enumerate(Database database) {

		ArrayList<Meter> result = new ArrayList<Meter>();
		for (Base b : database.objects.values()) {
			if (b instanceof Meter)
				result.add((Meter) b);
		}
		return result;

	}
	
	public void setUserValue(Main main, double value) {
		userDefined = true;
		userValue = value;
		if(main != null) {
			Base b = getOwner(main.getDatabase());
			if(b != null) {
				b.modified(main);
			}
		}
	}
	
	public boolean isUserDefined() {
		return userDefined;
	}
	
	public double getUserValue() {
		return userValue;
	}
	
	@Override
	public boolean modifyId(Main main, Account account, String id) {

		Database database = main.getDatabase();
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyId(main, account, id);
			}
		}
		return super.modifyId(main, account, id);
	}

	@Override
	public boolean modifyText(Main main, Account account, String text) {
		Database database = main.getDatabase();
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyText(main, account, text);
			}
		}
		return super.modifyText(main, account, text);
	}

	@Override
	public boolean modifyDescription(Main main, Account account, String text) {
		Database database = main.getDatabase();
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyDescription(main, account, text);
			}
		}
		return super.modifyDescription(main, account, text);
	}

	public String getTrafficColor(Database database) {
		
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {

			Object value = indicator.getValue();
			if(value == null) return TrafficValuation.RED;
			
			String s = trafficValuation.getTrafficValue(value);
			if(s != null) return s;
			
			return TrafficValuation.RED;
			
		}
		
		return trafficColor(value(database));

	}

	@Override
	public boolean migrate(Main main) {
		
		boolean result = true;
		
		Database database = main.getDatabase();
		
		if(getOwner(database) == null) {
			remove(database);
			return true;
		}
		
		Property aika = Property.find(database, Property.AIKAVALI);

		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			
			result |= indicator.migrate(main);

			if(trafficValuation == null && limits != null) {
				trafficValuation = new NumberTrafficValuation(BigDecimal.valueOf(limits[0]), BigDecimal.valueOf(limits[1]));
				result = true;
			}
			
		} else {
			
			if(userDefined) {
				
				Datatype dt = Datatype.find(database, "Toteuma");
				Indicator ind = Indicator.create(database, getId(database), dt);
				
				String year = aika.getPropertyValue(this);
				if(year == null) year = Property.AIKAVALI_KAIKKI;
				
				Base owner = getOwner(database);
				addIndicatorMeter(main, owner, ind, year);
				owner.removeMeter(this);
				remove(database);
				result = true;
				
			}

		}
		
		if(description == null) {
			modifyDescription(main, getText(database));
			modifyText(main, getId(database));
			modifyId(main, "");
			result = true;
		}

		result |= super.migrate(main);
		
		return result;
		
	}

	public static void addIndicatorMeter(Main main, Base b, Indicator indicator, String year) {

		Database database = main.getDatabase();
		Relation measures = Relation.find(database, Relation.MEASURES);

		Datatype type = indicator.getDatatype(database);
		TrafficValuation defaultValuation = type.getDefaultTrafficValuation();
		
		Meter m = Meter.create(database, b, indicator.getId(database), indicator.getText(database), defaultValuation);
		m.addRelation(measures, indicator);
		
		Utils.modifyValidity(main, m, year);
		
	}

	public static String updateMeterValue(final Main main, HorizontalLayout hl, final Base base, final Meter meter, boolean canWrite) {
		
		final Database database = main.getDatabase();

		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setWidth("300px");
		hl2.setHeight("100%");
		hl.addComponent(hl2);
		hl.setComponentAlignment(hl2, Alignment.MIDDLE_LEFT);

		final Indicator i = meter.getPossibleIndicator(database);
		if(i != null) {
			
			Datatype datatype = i.getDatatype(database);
			if(datatype instanceof EnumerationDatatype) {

				EnumerationDatatype enu = (EnumerationDatatype)datatype;
				
				Object value = i.getValue();
				AbstractField<?> combo = enu.getEditor(main, base, i);
				
				hl2.addComponent(combo);
				hl2.setComponentAlignment(combo, Alignment.MIDDLE_CENTER);

				return value != null ? value.toString() : "null";
				
			}
			
		}

		final Label label = new Label(meter.describe(database));
		label.setWidthUndefined();

		hl2.addComponent(label);
		hl2.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		hl2.setExpandRatio(label, 1.0f);
		
		return label.getValue();

	}

	public static void updateMeters(final Main main, boolean canWrite) {

		if(main.getUIState().currentItem instanceof Strategiakartta) return;

		final Database database = main.getDatabase();
		
		Base base = main.getUIState().currentItem;
		
		List<MeterDescription> descs = makeMeterDescriptions(main, base, false);
		if(!descs.isEmpty() || canWrite) {

			HorizontalLayout meterHeader = new HorizontalLayout();
			meterHeader.setSpacing(true);

			Label header = new Label("Mittarit");
			main.propertyCells.add(Utils.excelRow(header.getValue()));
			header.setHeight("32px");
			header.addStyleName(ValoTheme.LABEL_HUGE);
			header.addStyleName(ValoTheme.LABEL_BOLD);
			meterHeader.addComponent(header);
			meterHeader.setComponentAlignment(header, Alignment.BOTTOM_CENTER);

			if(canWrite) {

				final Image editMeters = new Image(null, new ThemeResource("chart_bar_edit.png"));
				editMeters.setHeight("24px");
				editMeters.setWidth("24px");
				editMeters.addClickListener(new MouseEvents.ClickListener() {

					private static final long serialVersionUID = 2661060702097338722L;

					@Override
					public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
						Utils.loseFocus(editMeters);
						manageMeters(main, main.getUIState().currentItem);
					}

				});

				meterHeader.addComponent(editMeters);
				meterHeader.setComponentAlignment(editMeters, Alignment.BOTTOM_CENTER);

			}

			main.properties.addComponent(meterHeader);
			main.properties.setComponentAlignment(meterHeader, Alignment.MIDDLE_CENTER);

			VerticalLayout meters = new VerticalLayout();				

			boolean showYears = main.getUIState().time.equals(Property.AIKAVALI_KAIKKI);

			Property time = Property.find(database, Property.AIKAVALI);

			int index = 0;
			for (final MeterDescription desc : descs) {
				
				ArrayList<String> excelRow = new ArrayList<String>();

				Meter meter = desc.meter;

				final HorizontalLayout hl = new HorizontalLayout();
				hl.addStyleName((((index++)&1) == 0) ? "evenProperty" : "oddProperty");
				hl.setSpacing(true);
				hl.setHeight("42px");
				
				Label l = new Label(desc.caption);
				excelRow.add(l.getValue().replace("%nbsp",""));
				l.setContentMode(ContentMode.HTML);
				l.setWidth("450px");
				l.addStyleName("propertyName");
				l.setData(desc);
				hl.addComponent(l);
				hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);

				String value = updateMeterValue(main, hl, base, meter, canWrite);
				excelRow.add(value);

				String shortComment = "";
				Indicator indicator = meter.getPossibleIndicator(database);
				if(indicator != null) shortComment = indicator.getValueShortComment();
				Label comment = new Label(shortComment);
				comment.setWidth("150px");
				hl.addComponent(comment);
				hl.setComponentAlignment(comment, Alignment.MIDDLE_LEFT);
				excelRow.add(comment.getValue());

				if(showYears) {

					HorizontalLayout hl2 = new HorizontalLayout();
					hl2.setWidth("70px");
					hl2.setHeight("100%");
					hl.addComponent(hl2);
					hl.setComponentAlignment(hl2, Alignment.MIDDLE_LEFT);

					String years = time.getPropertyValue(meter);
					if(years == null) years = Property.AIKAVALI_KAIKKI;

					final Label region = new Label(years);
					region.setWidthUndefined();

					excelRow.add(region.getValue());

					hl2.addComponent(region);
					hl2.setComponentAlignment(region, Alignment.MIDDLE_CENTER);

				}

				AbsoluteLayout image = new AbsoluteLayout();
				image.setWidth("32px");
				image.setHeight("32px");
				image.addStyleName("meterColor" + index);

				String color = meter.getTrafficColor(database);

				Styles styles = Page.getCurrent().getStyles();
		        styles.add(".fi_semantum_strategia div." + "meterColor"+ index + " { background: " + color + "; }");

				hl.addComponent(image);
				hl.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
				hl.setExpandRatio(image, 0.0f);
				
				meters.addComponent(hl);
				meters.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);

				ThemeResource res = desc.meter.showInMap ? new ThemeResource("zoom.png") : new ThemeResource("zoom_out.png"); 
				
				final Image show = new Image();
				show.setSource(res);
				show.setHeight("24px");
				show.setWidth("24px");
				if(canWrite) {
					show.setDescription("Klikkaamalla voit valita, n‰ytet‰‰nkˆ mittaria strategiakartassa");
					show.addClickListener(new MouseEvents.ClickListener() {
	
						private static final long serialVersionUID = 7156984656942915939L;
	
						@Override
						public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
							desc.meter.setShowInMap(!desc.meter.showInMap);
							Updates.update(main, true);
						}
	
					});
				}

				hl.addComponent(show);
				hl.setComponentAlignment(show, Alignment.MIDDLE_CENTER);

				final Image wiki = new Image();
				wiki.setSource(new ThemeResource("table_edit.png"));
				wiki.setHeight("24px");
				wiki.setWidth("24px");
				wiki.setDescription("Klikkaamalla voit siirty‰ tausta-asiakirjaan");
				wiki.addClickListener(new MouseEvents.ClickListener() {

					private static final long serialVersionUID = 7156984656942915939L;

					@Override
					public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
						Utils.openWiki(main, desc.meter);
					}

				});

				hl.addComponent(wiki);
				hl.setComponentAlignment(wiki, Alignment.MIDDLE_CENTER);

				main.propertyCells.add(excelRow);

			}
			
			meters.addLayoutClickListener(new LayoutClickListener() {
				
				private static final long serialVersionUID = 3295743025581923380L;

				private String extractData(Component c) {
					if(c instanceof AbstractComponent) {
						Object data = ((AbstractComponent)c).getData();
						if(data instanceof MeterDescription) {
							MeterDescription desc = (MeterDescription)data;
							return desc.meter.getDescription(database);
						}
					}
					return null;
				}
				
				@Override
				public void layoutClick(LayoutClickEvent event) {
				
					String desc = extractData(event.getClickedComponent());
					if(desc == null) return;
					
					String content = "<div style=\"width: 700px; border: 2px solid; padding: 5px\">";
					content += "<div style=\"text-align: center; white-space:normal; font-size: 36px; padding: 10px\">" + desc + "</div>";
					content += "</div>";

					Notification n = new Notification(content, Notification.Type.HUMANIZED_MESSAGE);
					n.setHtmlContentAllowed(true);
					n.show(Page.getCurrent());
					
				}
				
			});
			
			main.properties.addComponent(meters);
			main.properties.setComponentAlignment(meters, Alignment.MIDDLE_CENTER);

		}

	}

	public static List<MeterDescription> makeMeterDescriptions(Main main, Base base, boolean filterShow) {
		List<MeterDescription> descs = new ArrayList<MeterDescription>();
		fillMeterDescriptions(main, base, "", "", filterShow, descs);
		return descs;
	}
	
	private static void fillMeterDescriptions(Main main, Base base, String indent, String numbering, boolean filterShow, List<MeterDescription> result) {

		final Database database = main.getDatabase();
		List<Meter> meters = base.getMeters(database); 
		for(int i=0;i<meters.size();i++) {
			Meter meter = meters.get(i);
			if(!meter.isActive(main)) continue;
			if(filterShow && !meter.showInMap) continue;
			String nr = numbering.isEmpty() ?  Integer.toString(i+1) : numbering + "." + Integer.toString(i+1);
			String desc = indent + nr + ". " + meter.getCaption(database);
			result.add(new MeterDescription(meter, nr, desc));
			fillMeterDescriptions(main, meter, makeIndent(indent), nr, filterShow, result);
		}

	}
	
	private static void makeMeterTable(Main main, Base base, final Table table) {
		
		@SuppressWarnings("unchecked")
		Collection<Meter> selection = (Collection<Meter>)table.getValue();
		
		table.removeAllItems();
		List<MeterDescription> descs = makeMeterDescriptions(main, base, false);
		fillMeterTable(main, base, table, descs);
		
		for(Meter m : selection)
			table.select(m);

	}
	
	private static void fillMeterTable(Main main, Base base, final Table table, List<MeterDescription> descs) {

		boolean showYears = main.getUIState().time.equals(Property.AIKAVALI_KAIKKI);

		final Database database = main.getDatabase();

		Property aika = Property.find(database, Property.AIKAVALI);
		
		for(int i=0;i<descs.size();i++) {
			MeterDescription desc = descs.get(i);
			Meter meter = desc.meter;
			Label text = new Label(desc.caption);
			text.setContentMode(ContentMode.HTML);
			String year = aika.getPropertyValue(meter);
			if(showYears)
				table.addItem(new Object[] { text, year }, meter);
			else
				table.addItem(new Object[] { text }, meter);
		}

	}
	
	private static String makeIndent(String indent) {
		return "&nbsp;&nbsp;" + indent; 
	}
	
	static class ImplementationMeter extends Base {

		private static final long serialVersionUID = 7707700206812885773L;

		protected ImplementationMeter() {
			super("", "Toteutuksen tila", "");
		}

		@Override
		public Base getOwner(Database database) {
			return null;
		}
		
	}
	
	static class MeterSpec {
		
		public static Base IMPLEMENTATION = new ImplementationMeter();
		
		private Object source;
		private String text;
		
		public MeterSpec(Database database, Base source) {
			this.source = source;
			text = source.getText(database);
			if(source instanceof Indicator) text = text + " [Indikaattori]"; 
			else if(source instanceof EnumerationDatatype) text = text + " [Monivalinta]"; 
		}
		
		public String toString() {
			return text;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getSource() {
			return (T)source;
		}
		
	}
	
	
	public static void manageMeters(final Main main, final Base base) {

		String currentTime = main.getUIState().time; 
		boolean showYears = currentTime.equals(Property.AIKAVALI_KAIKKI);

		final Database database = main.getDatabase();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        final Table table = new Table();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.addStyleName(ValoTheme.TABLE_COMPACT);

        table.addContainerProperty("Mittari", Label.class, null);
        if(showYears)
        	table.addContainerProperty("Vuosi", String.class, null);
        
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);

		table.setEditable(false);
		table.setColumnExpandRatio("Mittari", 2.0f);
		if(showYears)
			table.setColumnExpandRatio("Vuosi", 0.0f);

        makeMeterTable(main, base, table);
        
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);

        abstract class MeterButtonListener implements Button.ClickListener {

			private static final long serialVersionUID = -6640950006518632633L;
			
			protected Meter getPossibleSelection() {
    			Object selection = table.getValue();
    			Collection<?> selected = (Collection<?>)selection;
    			if(selected.size() != 1) return null;
    			return (Meter)selected.iterator().next();
        	}
			
			@SuppressWarnings("unchecked")
			protected Collection<Meter> getSelection() {
    			return (Collection<Meter>)table.getValue();
			}

			protected  Map<Base,List<Meter>> getSelectionByParent(Database database) {
				return metersByParent(database, getSelection());
			}
        	
        }

		final Button removeMeters = new Button("Poista", new MeterButtonListener() {

			private static final long serialVersionUID = 2957964892664902859L;

			public void buttonClick(ClickEvent event) {
				
				for(Meter r : getSelection()) {
					Base owner = r.getOwner(database);
					owner.removeMeter(r);
				}
				
				makeMeterTable(main, base, table);
				Updates.update(main, true);

			}

		});
		removeMeters.addStyleName(ValoTheme.BUTTON_TINY);

		final Button moveUp = new Button("Siirr‰ ylemm‰s", new MeterButtonListener() {

			private static final long serialVersionUID = 8434251773337788784L;

			public void buttonClick(ClickEvent event) {

				for(Map.Entry<Base, List<Meter>> entry : getSelectionByParent(database).entrySet()) {
					entry.getKey().moveMetersUp(entry.getValue());
				}
				
				makeMeterTable(main, base, table);
				Updates.update(main, true);

			}

		});
		moveUp.addStyleName(ValoTheme.BUTTON_TINY);

		final Button moveDown = new Button("Siirr‰ alemmas", new MeterButtonListener() {

			private static final long serialVersionUID = -5382367112305541842L;

			public void buttonClick(ClickEvent event) {

				for(Map.Entry<Base, List<Meter>> entry : getSelectionByParent(database).entrySet()) {
					entry.getKey().moveMetersDown(entry.getValue());
				}
				
				makeMeterTable(main, base, table);
				Updates.update(main, true);

			}

		});
		moveDown.addStyleName(ValoTheme.BUTTON_TINY);

		final Button modify = new Button("M‰‰rit‰");
		modify.addClickListener(new MeterButtonListener() {
			
			private static final long serialVersionUID = -7109999546516429095L;

			public void buttonClick(ClickEvent event) {

				Meter meter = getPossibleSelection();
				if(meter == null) return;
				
				editMeter(main, base, meter);

			}

		});
		modify.addStyleName(ValoTheme.BUTTON_TINY);
		
		final ComboBox indicatorSelect = new ComboBox();
		indicatorSelect.setWidth("100%");
		indicatorSelect.setNullSelectionAllowed(false);
		indicatorSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		indicatorSelect.setCaption("M‰‰ritt‰j‰");
		final Strategiakartta map = database.getMap(base);
		
		// Indikaattorit
		for(Indicator i : map.getIndicators(database)) {
			MeterSpec spec = new MeterSpec(database, i);
			indicatorSelect.addItem(spec);
			indicatorSelect.select(spec);
		}
		// Enumeraatiot
		for(Datatype enu : Datatype.enumerate(database)) {
			if(enu instanceof EnumerationDatatype) {
				MeterSpec spec = new MeterSpec(database, enu);
				indicatorSelect.addItem(spec);
				indicatorSelect.select(spec);
			}
		}
		// Sis‰‰nrakennetut
		{
			MeterSpec spec = new MeterSpec(database, MeterSpec.IMPLEMENTATION);
			indicatorSelect.addItem(spec);
			indicatorSelect.select(spec);
		}
		
		indicatorSelect.setTextInputAllowed(false);

		final Button addMeter = new Button("Lis‰‰ p‰‰tasolle", new Button.ClickListener() {

			private static final long serialVersionUID = -5178621686299637238L;

			public void buttonClick(ClickEvent event) {
				
				MeterSpec spec = (MeterSpec)indicatorSelect.getValue();
				Object source = spec.getSource();
				if(source instanceof Indicator) {
					Indicator ind = (Indicator)source;
					Meter.addIndicatorMeter(main, base, ind, Property.AIKAVALI_KAIKKI);
				} else if (source instanceof EnumerationDatatype) {
					EnumerationDatatype dt = (EnumerationDatatype)source;
					Indicator ind = Indicator.create(database, "Uusi " + dt.getId(database), dt);
					ind.modifyValue(main, base, dt.getDefaultValue(), "", "Alkuarvo");
					Meter.addIndicatorMeter(main, base, ind, Property.AIKAVALI_KAIKKI);
				}
				
				makeMeterTable(main, base, table);
				Updates.update(main, true);

			}

		});
		addMeter.addStyleName(ValoTheme.BUTTON_TINY);

		final Button addSubmeter = new Button("Lis‰‰ valitun alle", new MeterButtonListener() {

			private static final long serialVersionUID = -1250285092312682737L;

			public void buttonClick(ClickEvent event) {

				Meter meter = getPossibleSelection();
				if(meter == null) return;

				MeterSpec spec = (MeterSpec)indicatorSelect.getValue();
				Object source = spec.getSource();
				if(source instanceof Indicator) {
					Indicator ind = (Indicator)source;
					Meter.addIndicatorMeter(main, meter, ind, Property.AIKAVALI_KAIKKI);
				} else if (source instanceof EnumerationDatatype) {
					EnumerationDatatype dt = (EnumerationDatatype)source;
					Indicator ind = Indicator.create(database, "Uusi " + dt.getId(database), dt);
					ind.modifyValue(main, base, dt.getDefaultValue(), "", "Alkuarvo");
					Meter.addIndicatorMeter(main, meter, ind, Property.AIKAVALI_KAIKKI);
				}
				
				makeMeterTable(main, base, table);
				Updates.update(main, true);

			}

		});
		addSubmeter.addStyleName(ValoTheme.BUTTON_TINY);

		final Runnable setStates = new Runnable() {

			@Override
			public void run() {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					removeMeters.setEnabled(true);
					moveUp.setEnabled(true);
					moveDown.setEnabled(true);
					if(selected.size() == 1) {
						modify.setEnabled(true);
						addSubmeter.setEnabled(true);
					} else {
						addSubmeter.setEnabled(false);
						modify.setEnabled(false);
					}
				} else {
					moveUp.setEnabled(false);
					moveDown.setEnabled(false);
					removeMeters.setEnabled(false);
					addSubmeter.setEnabled(false);
					modify.setEnabled(false);
				}
				
			}
			
		};
		
		table.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				setStates.run();
			}
			
		});
		
		setStates.run();
        
        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        hl2.setWidthUndefined();

        hl2.addComponent(modify);
        hl2.setComponentAlignment(modify, Alignment.TOP_LEFT);
        hl2.setExpandRatio(modify, 0.0f);

        hl2.addComponent(removeMeters);
        hl2.setComponentAlignment(removeMeters, Alignment.TOP_LEFT);
        hl2.setExpandRatio(removeMeters, 0.0f);

        hl2.addComponent(moveUp);
        hl2.setComponentAlignment(moveUp, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveUp, 0.0f);

        hl2.addComponent(moveDown);
        hl2.setComponentAlignment(moveDown, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveDown, 0.0f);

        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);
        hl3.setWidth("100%");
        
        hl3.addComponent(addMeter);
        hl3.setComponentAlignment(addMeter, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addMeter, 0.0f);

        hl3.addComponent(addSubmeter);
        hl3.setComponentAlignment(addSubmeter, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addSubmeter, 0.0f);

        hl3.addComponent(indicatorSelect);
        hl3.setComponentAlignment(indicatorSelect, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(indicatorSelect, 1.0f);

        content.addComponent(hl2);
        content.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(hl2, 0.0f);

        content.addComponent(hl3);
        content.setComponentAlignment(hl3, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(hl3, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
		Utils.makeDialog(main, "450px", "800px", "Hallitse mittareita", "Sulje", content, buttons);

	}

	public static void editMeter(final Main main, final Base base, final Meter meter) {

		Database database = main.getDatabase();

		final VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeightUndefined();
        content.setSpacing(true);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setMargin(false);

		final TextField tf = new TextField();
		tf.setCaption("Lyhytnimi");
		tf.setValue(meter.getId(database));
		tf.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf.setWidth("100%");
		
		hl.addComponent(tf);
		hl.setComponentAlignment(tf, Alignment.TOP_CENTER);
		hl.setExpandRatio(tf, 1.0f);
		
		final TextField tf1 = new TextField();
		tf1.setCaption("Teksti");
		tf1.setValue(meter.getText(database));
		tf1.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf1.setWidth("100%");
		
		hl.addComponent(tf1);
		hl.setComponentAlignment(tf1, Alignment.TOP_CENTER);
		hl.setExpandRatio(tf1, 2.0f);

		content.addComponent(hl);
		content.setComponentAlignment(hl, Alignment.TOP_CENTER);
		content.setExpandRatio(hl, 0.0f);

		final TextField tf2 = new TextField();
		tf2.setCaption("Voimassaolo");
		tf2.setValue(Utils.getValidity(database, meter));
		tf2.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf2.setWidth("100%");
		
		content.addComponent(tf2);
		content.setComponentAlignment(tf2, Alignment.TOP_CENTER);
		content.setExpandRatio(tf2, 0.0f);

		final TextArea ta = new TextArea();
		ta.setCaption("M‰‰ritys");
		ta.setValue(meter.getText(database));
		ta.addStyleName(ValoTheme.TEXTAREA_TINY);
		ta.setHeight("100%");
		ta.setWidth("100%");

		content.addComponent(ta);
		content.setComponentAlignment(ta, Alignment.TOP_CENTER);
		content.setExpandRatio(ta, 1.0f);
        
        final TrafficValuation valuation = meter.trafficValuation;
        final Runnable onOK = valuation != null ? valuation.getEditor(content, main, meter) : null;

        Indicator indicator = meter.getPossibleIndicator(database);
        if(indicator != null) {
	        final Label ta2 = Indicator.makeHistory(database, indicator);     
			content.addComponent(ta2);
	        content.setComponentAlignment(ta2, Alignment.MIDDLE_CENTER);
	        content.setExpandRatio(ta2, 1.0f);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);

        Button ok = new Button("Tallenna", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	if(onOK != null) onOK.run();
            	meter.modifyId(main, tf.getValue());
            	meter.modifyText(main, tf1.getValue());
            	Utils.modifyValidity(main, meter, tf2.getValue());
            	meter.modifyDescription(main, ta.getValue());
				Updates.update(main, true);
				manageMeters(main, main.getUIState().currentItem);
            }
            
        });
        buttons.addComponent(ok);

        Button close = new Button("Sulje", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -8065367213523520602L;

			public void buttonClick(ClickEvent event) {
    			main.closeDialog();
				manageMeters(main, main.getUIState().currentItem);
            }
            
        });
        buttons.addComponent(close);
        
		Utils.makeDialog(main, "500px", "800px", "M‰‰rit‰ mittaria", null, content, buttons);

	}
	
	public void setShowInMap(boolean value) {
		this.showInMap = value;
	}
	
	public String trafficValuationDescription(){
		return trafficValuation.toString();
	}
	
}
