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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Lucene;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.Utils.AbstractCommentCallback;
import fi.semantum.strategia.Utils.CommentCallback;
import fi.semantum.strategia.Wiki;

public class Indicator extends Base {
	
	private static final long serialVersionUID = -5122160460824254403L;

	private double value = 0;
	public TimeSeries values;
	
	public String unit = "";
	
	public static Indicator create(Database database, String text, Datatype datatype) {
		Indicator p = new Indicator("", text, datatype);
		database.register(p);
		return p;
	}
	
	private Indicator(String id, String text, Datatype datatype) {
		super(UUID.randomUUID().toString(), id, text);
		this.value = Double.NaN;
		values = new TimeSeries(datatype);
	}
	
	public Object getValue(boolean forecast) {
		if(forecast) return getForecast();
		else return getValue();
	}
	
	public Object getValue() {
		if(values != null) {
			return values.getLastValue();
		}
		else return value;
	}
	
	public Object getForecast() {
		if(values != null) {
			return values.getLastForecast();
		}
		else return value;
	}

	public String getValueShortComment() {
		if(values != null) {
			TimeSeriesEntry entry = values.getLastValueEntry();
			if(entry == null) return "";
			String shortComment = entry.getShortComment();
			return shortComment != null ? shortComment : "";
		}
		else return "";
	}
	
	public String getUnitAndComment() {
		return getUnit() + " " + getValueShortComment();
	}
	
	public String getUnit() {
		return unit;
	}
	
	public static List<Indicator> enumerate(Database database) {
		
		ArrayList<Indicator> result = new ArrayList<Indicator>();
		for(Base b : database.objects.values()) {
			if(b instanceof Indicator) result.add((Indicator)b);
		}
		return result;

	}
	
	public void update(Main main, Base owner, Object value, boolean forecast, String shortComment, String comment) {
		
		final Database database = main.getDatabase();

		Account account = main.getAccountDefault();
		if(!value.equals(forecast ? getForecast() : getValue())) {
			modified(main);
			values.addValue(forecast ? getValue() : value, forecast ? value : getForecast(), account, shortComment, comment);
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class IndicatorDescription {
		public String caption;
		public Indicator indicator;
		public IndicatorDescription(Indicator indicator, String caption) {
			this.indicator = indicator;
			this.caption = caption;
		}
	}

	private static void fillIndicatorDescriptions(Main main, Base base, String indent, List<IndicatorDescription> result) {

		final Database database = main.getDatabase();
		List<Indicator> indicators = base.getIndicatorsActive(main); 
		for(int i=0;i<indicators.size();i++) {
			Indicator indicator = indicators.get(i);
			String desc = indent + Integer.toString(i+1) + ". " + indicator.getCaption(database);
			result.add(new IndicatorDescription(indicator, desc));
			fillIndicatorDescriptions(main, indicator, makeIndent(indent, i), result);
		}

	}
	
	private static void makeIndicatorTable(Main main, Base base, final Table table) {
		
		@SuppressWarnings("unchecked")
		Collection<Indicator> selection = (Collection<Indicator>)table.getValue();
		
		table.removeAllItems();
		List<IndicatorDescription> descs = new ArrayList<IndicatorDescription>();
		fillIndicatorDescriptions(main, base, "", descs);
		fillIndicatorTable(main, base, table, descs);
		
		for(Indicator i : selection)
			table.select(i);

	}
	
	private static void fillIndicatorTable(Main main, Base base, final Table table, List<IndicatorDescription> descs) {

		boolean showYears = main.getUIState().time.equals(Property.AIKAVALI_KAIKKI);

		final Database database = main.getDatabase();

		Property aika = Property.find(database, Property.AIKAVALI);
		
		for(int i=0;i<descs.size();i++) {
			IndicatorDescription desc = descs.get(i);
			Indicator meter = desc.indicator;
			Label text = new Label(desc.caption);
			text.setContentMode(ContentMode.HTML);
			String year = aika.getPropertyValue(meter);
			if(showYears)
				table.addItem(new Object[] { text, year }, meter);
			else
				table.addItem(new Object[] { text }, meter);
		}

	}
	
	private static String makeIndent(String indent, int index) {
		return "&nbsp;&nbsp;" + indent + Integer.toString(index+1) + "."; 
	}
	
	public static void manageIndicators(final Main main, final Base base) {

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

        table.addContainerProperty("Indikaattori", Label.class, null);
        if(showYears)
        	table.addContainerProperty("Vuosi", String.class, null);
        
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);
        
		table.setEditable(false);
		table.setColumnExpandRatio("Indikaattori", 2.0f);
		if(showYears)
			table.setColumnExpandRatio("Vuosi", 0.0f);

        makeIndicatorTable(main, base, table);
        
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);
        
        abstract class IndicatorButtonListener implements Button.ClickListener {

			private static final long serialVersionUID = -7551250112503063540L;

			protected Indicator getPossibleSelection() {
    			Object selection = table.getValue();
    			Collection<?> selected = (Collection<?>)selection;
    			if(selected.size() != 1) return null;
    			return (Indicator)selected.iterator().next();
        	}
			
			@SuppressWarnings("unchecked")
			protected Collection<Indicator> getSelection() {
    			return (Collection<Indicator>)table.getValue();
			}
        	
			protected  Map<Base,List<Indicator>> getSelectionByParent(Database database) {
				return indicatorsByParent(database, getSelection());
			}
			
        }
        
		final Button removeIndicators = new Button("Poista", new IndicatorButtonListener() {

			private static final long serialVersionUID = -2538054127519468282L;

			public void buttonClick(ClickEvent event) {

				Collection<Indicator> selection = getSelection();
				for(Indicator i : selection) {
					Base owner = i.getOwner(database);
					if(owner == null) continue;
					owner.removeIndicator(i);
				}

				final Set<Base> bases = new HashSet<Base>();
				final Strategiakartta map = database.getMap(base);
				bases.add(map);
				for(Tavoite t : map.tavoitteet) {
					bases.add(t);
					for(Painopiste p : t.painopisteet) {
						bases.add(p);
					}
				} 
				for(Base b : bases) {
					for(Meter meter : b.getMeters(database)) {
						Indicator indicator = meter.getPossibleIndicator(database);
						if(selection.contains(indicator)) {
							b.removeMeter(meter);
						}
					}
				}
				
				makeIndicatorTable(main, base, table);
				
				Updates.update(main, true);

			}

		});
		removeIndicators.addStyleName(ValoTheme.BUTTON_TINY);

		final Button moveUp = new Button("Siirr� ylemm�s", new IndicatorButtonListener() {
			
			private static final long serialVersionUID = -635232943884881464L;

			public void buttonClick(ClickEvent event) {

				for(Map.Entry<Base, List<Indicator>> entry : getSelectionByParent(database).entrySet()) {
					entry.getKey().moveIndicatorsUp(entry.getValue());
				}

				makeIndicatorTable(main, base, table);
				Updates.update(main, true);

			}

		});
		moveUp.addStyleName(ValoTheme.BUTTON_TINY);
		
		final Button moveDown = new Button("Siirr� alemmas", new IndicatorButtonListener() {
			
			private static final long serialVersionUID = 2779521990166604444L;

			public void buttonClick(ClickEvent event) {

				for(Map.Entry<Base, List<Indicator>> entry : getSelectionByParent(database).entrySet()) {
					entry.getKey().moveIndicatorsDown(entry.getValue());
				}

				makeIndicatorTable(main, base, table);
				Updates.update(main, true);

			}

		});
		moveDown.addStyleName(ValoTheme.BUTTON_TINY);

		final Button modify = new Button("M��rit�");
		modify.addClickListener(new IndicatorButtonListener() {
			
			private static final long serialVersionUID = 5149432436059288486L;

			public void buttonClick(ClickEvent event) {

				Indicator indicator = getPossibleSelection();
				if(indicator == null) return;

				editIndicator(main, base, indicator);

			}

		});
		modify.addStyleName(ValoTheme.BUTTON_TINY);

		final TextField indicatorText = new TextField();
		indicatorText.setWidth("100%");
		indicatorText.addStyleName(ValoTheme.TEXTFIELD_TINY);
		indicatorText.setCaption("Tunniste");

        List<Datatype> types = Datatype.enumerate(database);

		final ComboBox datatypeSelect = new ComboBox();
		datatypeSelect.setWidth("100%");
		datatypeSelect.setNullSelectionAllowed(false);
		datatypeSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		datatypeSelect.setCaption("Tietotyyppi");
		for(Datatype dt : types)
			datatypeSelect.addItem(dt);
		
		final Button addIndicator = new Button("Lis�� p��tasolle", new Button.ClickListener() {

			private static final long serialVersionUID = -2395147866745115337L;

			public void buttonClick(ClickEvent event) {
				
				String text = indicatorText.getValue();
				Object dt = datatypeSelect.getValue();
				if(text.isEmpty() || dt == null || dt.equals(datatypeSelect.getNullSelectionItemId()))
					return;
				
				base.addIndicator(Indicator.create(database, text, (Datatype)dt));
				makeIndicatorTable(main, base, table);
				Updates.update(main, true);

			}

		});
		addIndicator.addStyleName(ValoTheme.BUTTON_TINY);

		final Button addSubIndicator = new Button("Lis�� valitun alle", new IndicatorButtonListener() {

			private static final long serialVersionUID = -2395147866745115337L;

			public void buttonClick(ClickEvent event) {
				
				Indicator indicator = getPossibleSelection();
				if(indicator == null) return;

				String text = indicatorText.getValue();
				Object dt = datatypeSelect.getValue();
				if(text.isEmpty() || dt == null || dt.equals(datatypeSelect.getNullSelectionItemId()))
					return;
				
				indicator.addIndicator(Indicator.create(database, text, (Datatype)dt));
				makeIndicatorTable(main, base, table);
				
				Updates.update(main, true);

			}

		});
		addSubIndicator.addStyleName(ValoTheme.BUTTON_TINY);

		final Runnable setStates = new Runnable() {

			@Override
			public void run() {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					removeIndicators.setEnabled(true);
					moveUp.setEnabled(true);
					moveDown.setEnabled(true);
					if(selected.size() == 1) {
						modify.setEnabled(true);
						addSubIndicator.setEnabled(true);
					} else {
						addSubIndicator.setEnabled(false);
						modify.setEnabled(false);
					}
				} else {
					moveUp.setEnabled(false);
					moveDown.setEnabled(false);
					removeIndicators.setEnabled(false);
					addSubIndicator.setEnabled(false);
					modify.setEnabled(false);
				}
				
			}
			
		};
		
        table.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1188285609779556446L;

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
        hl2.setComponentAlignment(modify, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(modify, 0.0f);

        hl2.addComponent(removeIndicators);
        hl2.setComponentAlignment(removeIndicators, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(removeIndicators, 0.0f);

        hl2.addComponent(moveUp);
        hl2.setComponentAlignment(moveUp, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveUp, 0.0f);

        hl2.addComponent(moveDown);
        hl2.setComponentAlignment(moveDown, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveDown, 0.0f);

        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);
        hl3.setWidth("100%");

        hl3.addComponent(addIndicator);
        hl3.setComponentAlignment(addIndicator, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addIndicator, 0.0f);

        hl3.addComponent(addSubIndicator);
        hl3.setComponentAlignment(addSubIndicator, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addSubIndicator, 0.0f);

        hl3.addComponent(indicatorText);
        hl3.setComponentAlignment(indicatorText, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(indicatorText, 1.0f);

        hl3.addComponent(datatypeSelect);
        hl3.setComponentAlignment(datatypeSelect, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(datatypeSelect, 2.0f);

        content.addComponent(hl2);
        content.setComponentAlignment(hl2, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(hl2, 0.0f);

        content.addComponent(hl3);
        content.setComponentAlignment(hl3, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(hl3, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        final Window dialog = Dialogs.makeDialog(main, "650px", "800px", "Hallitse indikaattoreita", "Sulje", content, buttons);

	}

	public Datatype getDatatype(Database database) {
		return values.getDatatype();
	}
	
	public void setDatatype(Datatype datatype) {
		if(!datatype.equals(values.getDatatype()))
			values = new TimeSeries(datatype);
	}
	
	private static TextField makeUnit(Database database, HorizontalLayout hl, Indicator indicator) {

        if(!(indicator.getDatatype(database) instanceof NumberDatatype)) return null;

		final TextField unit = new TextField("Yksikk�");
		unit.setValue(indicator.getUnit());
		unit.setWidth("100%");
		unit.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		hl.addComponent(unit);
		hl.setComponentAlignment(unit, Alignment.MIDDLE_CENTER);
		hl.setExpandRatio(unit, 1.0f);
		
		return unit;

	}
	
	public static Label makeHistory(Database database, Indicator indicator, boolean forecast) {
		
		Label ta2 = new Label("Historia");
        ta2.setContentMode(ContentMode.HTML);
		ta2.setWidth("100%");
		ta2.setHeight("100%");

        StringBuilder sb = new StringBuilder();
        sb.append("<b>Historia</b><br><br>");
        
        for(Map.Entry<Date, TimeSeriesEntry> entry : indicator.values.list()) {
        	TimeSeriesEntry tse = entry.getValue();
        	Account account = tse.getAccount();
        	Date date = entry.getKey();
        	String user = account != null ? account.getId(database) : "J�rjestelm�";
        	sb.append("<b>");
        	sb.append(user);
        	sb.append("</b> p�ivitti ");
        	sb.append(forecast ? " ennustetta" : " toteumaa");
        	sb.append(" <b>");
        	sb.append(Utils.describeDate(date));
        	sb.append("</b><br><hr>");
        	sb.append("&nbsp;Uusi arvo on <b>");
        	sb.append(forecast ? tse.getForecast() : tse.getValue() + " " + indicator.getUnitAndComment());
        	sb.append("</b><br>");
        	String comment = tse.getComment();
        	if(comment != null)
        		sb.append("<p>" + tse.getComment() + "</p>");
        	sb.append("<br>");
        }
        
        ta2.setValue(sb.toString());
        return ta2;

	}
	
	public static void editIndicator(final Main main, final Base base, final Indicator indicator) {

		final Database database = main.getDatabase();
		
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeightUndefined();
        content.setSpacing(true);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
		hl.setWidth("100%");
		content.addComponent(hl);
        content.setExpandRatio(hl, 0.0f);
        
		final TextField tf = new TextField("Tunniste");
		tf.setValue(indicator.getId(database));
		tf.setWidth("100%");
		tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		hl.addComponent(tf);
        hl.setComponentAlignment(tf, Alignment.MIDDLE_CENTER);
        hl.setExpandRatio(tf, 2.0f);
        
		final TextField tf1 = new TextField("Teksti");
		tf1.setValue(indicator.getText(database));
		tf1.setWidth("100%");
		tf1.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		hl.addComponent(tf1);
        hl.setComponentAlignment(tf1, Alignment.MIDDLE_CENTER);
        hl.setExpandRatio(tf1, 2.0f);

        final TextField unit = makeUnit(database, hl, indicator);

		final TextField tf2 = new TextField();
		tf2.setCaption("Voimassaolo");
		tf2.setValue(Utils.getValidity(database, indicator));
		tf2.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf2.setWidth("100%");
		hl.addComponent(tf2);
        hl.setComponentAlignment(tf2, Alignment.MIDDLE_CENTER);
        hl.setExpandRatio(tf2, 1.0f);

        final TextArea ta = new TextArea("M��ritys");
		ta.setValue(indicator.getText(database));
		ta.setWidth("100%");
		ta.setHeight("100px");
		content.addComponent(ta);
        content.setComponentAlignment(ta, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(ta, 0.0f);
        
        final Label ta2 = makeHistory(database, indicator, main.getUIState().forecastMeters);     
		content.addComponent(ta2);
        content.setComponentAlignment(ta2, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(ta2, 1.0f);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);

        Button ok = new Button("Tallenna", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	indicator.modifyId(main, tf.getValue());
            	Utils.modifyValidity(main, indicator, tf2.getValue());
            	indicator.modifyText(main, tf1.getValue());
            	indicator.modifyDescription(main, ta.getValue());
            	if(unit != null) {
            		indicator.modifyUnit(main, unit.getValue());
            	}
				Updates.update(main, true);
				manageIndicators(main, main.getUIState().currentItem);
            }
            
        });
        buttons.addComponent(ok);

        Button close = new Button("Sulje");
        buttons.addComponent(close);
        
        final Window dialog = Dialogs.makeDialog(main, "650px", "800px", "Muokkaa indikaattoria", null, content, buttons);
        close.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(dialog);
				manageIndicators(main, main.getUIState().currentItem);
            }
            
        });

	}
	
	public void modifyUnit(Main main, String text) {
		
		Account account = main.getAccountDefault();
		modifyUnit(main, account, text);
		
	}

	void modifyUnit(Main main, Account account, String text) {
		
		final Database database = main.getDatabase();

		if(!this.unit.equals(text)) {
			modified(main);
			this.unit = text;
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static String updateIndicatorValue(final Main main, HorizontalLayout hl, final Base base, final Indicator indicator, boolean canWrite) {
		
		final Database database = main.getDatabase();

		Datatype dt = indicator.getDatatype(database);
		if(dt != null) {

			final AbstractField<?> field = dt.getEditor(main, base, indicator, false, null);
			field.setWidth("150px");
			field.setReadOnly(!canWrite);
			hl.addComponent(field);
			hl.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
			Object value = field.getValue();
			return value != null ? value.toString() : "";

		} else {

			Object value = indicator.getValue();
			final String formatted = value != null ? value.toString() : "";

			final TextField tf = new TextField();
			tf.setValue(formatted);
			tf.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = 3547126051252580446L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					try {
						double value = Double.parseDouble(tf.getValue());
						indicator.updateWithComment(main, base, value, main.getUIState().forecastMeters, new AbstractCommentCallback() {
							
							public void canceled() {
								tf.setValue(formatted);
							}
							
						});
					} catch (NumberFormatException e) {
						tf.setComponentError(new UserError("Arvon tulee olla numero"));
					}
				}
			});
			tf.setWidth("150px");
			tf.setReadOnly(!canWrite);
			hl.addComponent(tf);
			hl.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
			return tf.getValue();

		}


	}

	public static void updateIndicators(final Main main, final Base base, boolean canWrite) {
		
		final Database database = main.getDatabase();

		List<IndicatorDescription> descs = new ArrayList<IndicatorDescription>();
		fillIndicatorDescriptions(main, base, "", descs);

		boolean isMap = base instanceof Strategiakartta; 
		
		if(isMap && (!descs.isEmpty() || canWrite )) {
			
			HorizontalLayout indiHeader = new HorizontalLayout();
			indiHeader.setSpacing(true);

			Label header = new Label("Indikaattorit (ennuste)");
			main.propertyCells.add(Utils.excelRow(header.getValue()));
			header.setHeight("32px");
			header.addStyleName(ValoTheme.LABEL_HUGE);
			header.addStyleName(ValoTheme.LABEL_BOLD);
			indiHeader.addComponent(header);
			indiHeader.setComponentAlignment(header, Alignment.BOTTOM_CENTER);

			if(canWrite) {

				final Image editIndicators = new Image(null, new ThemeResource("chart_line_edit.png"));
				editIndicators.setHeight("24px");
				editIndicators.setWidth("24px");
				editIndicators.addClickListener(new MouseEvents.ClickListener() {

					private static final long serialVersionUID = 2661060702097338722L;

					@Override
					public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
						Utils.loseFocus(editIndicators);
						manageIndicators(main, main.getUIState().currentItem);
					}

				});

				indiHeader.addComponent(editIndicators);
				indiHeader.setComponentAlignment(editIndicators, Alignment.BOTTOM_CENTER);

			}

			main.properties.addComponent(indiHeader);
			main.properties.setComponentAlignment(indiHeader, Alignment.MIDDLE_CENTER);

			VerticalLayout indicators = new VerticalLayout();				

			boolean showYears = main.getUIState().time.equals(Property.AIKAVALI_KAIKKI);

			Property time = Property.find(database, Property.AIKAVALI);

			int index = 0;
			for (final IndicatorDescription desc : descs) {
				
				ArrayList<String> excelRow = new ArrayList<String>();
				
				Indicator indicator = desc.indicator;

				final HorizontalLayout hl = new HorizontalLayout();
				hl.addStyleName((((index++)&1) == 0) ? "evenProperty" : "oddProperty");
				hl.setSpacing(true);
				
				Label l = new Label(desc.caption);
				excelRow.add(l.getValue().replace("%nbsp",""));
				l.setContentMode(ContentMode.HTML);
				l.setWidth("450px");
				l.addStyleName("propertyName");
				l.setData(desc);
				hl.addComponent(l);
				hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);

				String value = updateIndicatorValue(main, hl, base, indicator, canWrite);
				excelRow.add(value);
				
				Label unit = new Label(indicator.getUnit());
				unit.setWidth("100px");
				hl.addComponent(unit);
				hl.setComponentAlignment(unit, Alignment.MIDDLE_LEFT);
				excelRow.add(unit.getValue());

				Label comment = new Label(indicator.getValueShortComment());
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

					String years = time.getPropertyValue(indicator);
					if(years == null) years = Property.AIKAVALI_KAIKKI;

					final Label region = new Label(years);
					region.setWidthUndefined();

					excelRow.add(region.getValue());

					hl2.addComponent(region);
					hl2.setComponentAlignment(region, Alignment.MIDDLE_CENTER);
					
				}

				final Image wiki = new Image();
				wiki.setSource(new ThemeResource("table_edit.png"));
				wiki.setHeight("24px");
				wiki.setWidth("24px");
				wiki.addClickListener(new MouseEvents.ClickListener() {

					private static final long serialVersionUID = 2661060702097338722L;

					@Override
					public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
						Wiki.openWiki(main, desc.indicator);
					}

				});

				hl.addComponent(wiki);
				hl.setComponentAlignment(wiki, Alignment.MIDDLE_CENTER);

				indicators.addComponent(hl);
				indicators.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
				
				main.propertyCells.add(excelRow);

			}

			indicators.addLayoutClickListener(new LayoutClickListener() {
				
				private static final long serialVersionUID = 3295743025581923380L;

				private String extractData(Component c) {
					if(c instanceof AbstractComponent) {
						Object data = ((AbstractComponent)c).getData();
						if(data instanceof IndicatorDescription) {
							IndicatorDescription desc = (IndicatorDescription)data;
							return desc.indicator.getDescription(database);
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
			
			main.properties.addComponent(indicators);
			main.properties.setComponentAlignment(indicators, Alignment.MIDDLE_CENTER);

		}

	}

	@Override
	public boolean migrate(Main main) {
		
		Database database = main.getDatabase();

		boolean result = false;
		
		if(description == null) {
			modifyDescription(main, getText(database));
			modifyText(main, getId(database));
			modifyId(main, "");
			result = true;
		}

		if(unit == null) {
			unit = "";
			result = true;
		}
		
		if(values == null && value != Double.NaN) {
			Datatype datatype = Datatype.find(database, NumberDatatype.ID);
			values = new TimeSeries(datatype);
			values.addValue(BigDecimal.valueOf(value), BigDecimal.valueOf(value), null, null, null);
			value = Double.NaN;
			result = true;
		}
		
		result |= super.migrate(main);

		return result;

	}

	@Override
	public Base getOwner(Database database) {
		for(Base b : database.enumerate()) {
			if(b.getIndicators(database).contains(this))
				return b;
		}
		return null;
	}
	
	public static Map<Base,List<Indicator>> indicatorsByParent(Database database, Collection<Indicator> indicators) {
		Map<Base,List<Indicator>> result = new HashMap<Base,List<Indicator>>();
		for(Indicator i : indicators) {
			Base owner = i.getOwner(database);
			List<Indicator> exist = result.get(owner);
			if(exist == null) {
				exist = new ArrayList<Indicator>();
				result.put(owner, exist);
			}
			exist.add(i);
		}
		return result;
	}
	
	public void updateWithComment(final Main main, final Base base, final Object value, final boolean forecast, final CommentCallback callback) {
		
		Dialogs.commentDialog(main, "P�ivitykseen littyv�t lis�tiedot", "Tee muutokset", new CommentCallback() {
			
			@Override
			public void runWithComment(String shortComment, String comment) {
				
				update(main, base, value, forecast, shortComment, comment);
				Updates.update(main, true);

				if(callback != null)
					callback.runWithComment(shortComment, comment);

			}
			
			@Override
			public void canceled() {
				if(callback != null)
					callback.canceled();
			}
			
		});

	}
	
}
