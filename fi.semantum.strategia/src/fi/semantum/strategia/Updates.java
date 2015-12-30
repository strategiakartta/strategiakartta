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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.FilterState.ReportCell;
import fi.semantum.strategia.Main.TreeVisitor1;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Indicator;
import fi.semantum.strategia.widget.MapVis;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;
import fi.semantum.strategia.widget.Tavoite;


public class Updates {

	public static void update(Main main, boolean needSave) {
		update(main, false, needSave);
	}

	public static void update(Main main, boolean setPositions, boolean needSave) {

		updateStates(main);
		updateFilter(main);
		if(main.getUIState().tabState == UIState.PROPERTIES)
			updateProperties(main);
		updateJS(main, setPositions, needSave);
		
		main.setTabState(main.getUIState(), main.getUIState().tabState);
		
	}

	private static void updateStates(Main main) {

		ComboBox combo = main.states;
		combo.removeAllItems();
		combo.setInputPrompt("valitse tallennettu näkymä");
		
		for(UIState state : main.getAccountDefault().uiStates) {
			combo.addItem(state.name);
		}

	}

	private static void updateFilter(Main main) {

		List<NodeFilter> availableFilters = main.availableFilters;
		UIState uiState = main.getUIState();
		Account account = main.account;
		ComboBox filter = main.filter;

		main.availableFilters.clear();
		main.filter.removeAllItems();

		NodeFilter current = uiState.getCurrentFilter();
		if(current != null)
			availableFilters.add(current);
		
		availableFilters.add(new TulostavoiteToimenpideFilter(main, uiState.currentItem, uiState.currentPosition));

		if (uiState.currentItem instanceof Strategiakartta) {

			availableFilters.add(new MeterFilter(main,
					uiState.currentItem, uiState.currentPosition));
			availableFilters.add(new TulostavoiteFilter(main,
					uiState.currentItem, uiState.currentPosition));

		} else if (uiState.currentItem instanceof Tavoite) {

			availableFilters.add(new MeterFilter(main,
					uiState.currentItem, uiState.currentPosition));
			availableFilters.add(new TulostavoiteFilter(main,
					uiState.currentItem, uiState.currentPosition));

		} else if (uiState.currentItem instanceof Painopiste) {

			availableFilters.add(new MeterFilter(main,
					uiState.currentItem, uiState.currentPosition));
			availableFilters.add(new TulostavoiteFilter(main,
					uiState.currentItem, uiState.currentPosition));

		}

		availableFilters.add(new ChangeFilter(main));

		if(account != null)
			availableFilters.add(new AccountFilter(main, account));
		
		for (NodeFilter f : availableFilters) {
			filter.addItem(f.toString());
		}

		String filterName = uiState.currentFilterName;
		if(filterName == null)
			filterName = "";

		NodeFilter newFilter = null;
		
		main.filterListenerActive = false;
		for(NodeFilter f : availableFilters) {
			if(f.toString().equals(filterName))
				newFilter = f;
		}
		
		if(newFilter == null) {
			newFilter = new TulostavoiteToimenpideFilter(main, uiState.current, uiState.currentPosition);
			filter.select(filter.getNullSelectionItemId());
			filter.setInputPrompt("valitse hakuehto");
		} else {
			filter.select(newFilter.toString());
		}

		main.filterListenerActive = true;

		uiState.setCurrentFilter(newFilter);

	}
	
	private static void updateProperties(final Main main) {

		final Base base = main.getUIState().currentItem; 
		
		boolean canWrite = main.canWrite(base);
		
		main.propertyCells = new ArrayList<List<String>>();
				
		main.properties.removeAllComponents();
		main.properties.setId("map");

		if (base == null)
			return;

		Property.updateProperties(main, base, canWrite);
		
		Indicator.updateIndicators(main, base, canWrite);
		
		Meter.updateMeters(main, canWrite);
		
		Tag.updateRelatedTags(main, canWrite);
		
		Tag.updateMonitoredTags(main, canWrite);
		
		final Button palaa = new Button("Palaa takaisin", new Button.ClickListener() {

			private static final long serialVersionUID = -6097534468072862126L;

			public void buttonClick(ClickEvent event) {
				main.applyFragment(main.backFragment, true);
			}

		});
		
		main.properties.addComponent(palaa);
		main.properties.setComponentAlignment(palaa, Alignment.BOTTOM_CENTER);

	}

	public static void updateMap(Main main) {

		if(main.getUIState().current == null) return;
		
		main.getUIState().current.prepare(main);
		
		MapVis vis = new MapVis(main, main.getUIState().current, true);
		vis.fixRows();
		main.js.update(vis, main.windowWidth, Account.canWrite(main, main.getUIState().current));
		
		if(main.getUIState().reference != null) {
			vis = new MapVis(main, main.getUIState().reference, false);
			vis.fixRows();
			main.js2Container.setVisible(true);
			if(main.mapDialog != null) {
				main.js2.update(vis, (int)main.mapDialog.getWidth(), Account.canWrite(main, main.getUIState().reference));
			} else {
				main.js2.update(vis, main.windowWidth, Account.canWrite(main, main.getUIState().reference));
			}
		} else{
			main.js2.update(null, main.windowWidth, false);
			main.js2Container.setVisible(false);
		}

	}

	
	public static void updateBrowser(Main main, boolean setPositions) {

		final Database database = main.getDatabase();

		TreeVisitor1 treeVisitor1 = main.new TreeVisitor1();
		main.getUIState().getCurrentFilter().reset(treeVisitor1.filterState);
		treeVisitor1.visit(database.getRoot());
		
		treeVisitor1.filterState.process(main.getUIState().getCurrentFilter(), setPositions);

		ArrayList<BrowserNode> ns = new ArrayList<BrowserNode>();
		ns.addAll(treeVisitor1.filterState.nodes);
		
		BrowserNode[] nodes = ns.toArray(new BrowserNode[ns.size()]);
		Collections.sort(treeVisitor1.filterState.links, new Comparator<BrowserLink>() {

			@Override
			public int compare(BrowserLink o1, BrowserLink o2) {
				
				int result = Double.compare(o2.weight, o1.weight);
				if(result != 0) return result;
				
				result = Integer.compare(o2.source, o1.source);
				if(result != 0) return result;
				
				return Integer.compare(o2.target, o1.target);
				
			}
			
		});
		
		main.browser_.update(nodes, treeVisitor1.filterState.links
				.toArray(new BrowserLink[treeVisitor1.filterState.links.size()]),
				main.windowWidth, main.windowHeight, setPositions);

		updateQueryGrid(main, treeVisitor1.filterState);

	}

	public static void updateJS(Main main, boolean needSave) {
		
		updateJS(main, false, needSave);
		
	}

	public static void updateJS(Main main, boolean setPositions, boolean needSave) {

		boolean needMap = main.getUIState().tabState == UIState.MAP;
		boolean needBrowser = main.getUIState().tabState == UIState.BROWSER;
		updateJS(main, needMap, needBrowser, setPositions, needSave);
		
	}

	public static void updateJS(Main main, boolean needMap, boolean needBrowser, boolean setPositions, boolean needSave) {

		if (needSave)
			main.getDatabase().save();

		if (needMap)
			updateMap(main);
		
		if (needBrowser || setPositions)
			updateBrowser(main, setPositions);
		
	}
	
	private static void updateQueryGrid(final Main main, final FilterState state) {
		
		main.gridPanelLayout.removeAllComponents();
		main.gridPanelLayout.setMargin(false);
		
		final List<String> keys = state.reportColumns;
		if(keys.isEmpty()) {
			Label l = new Label("Kysely ei tuottanut yhtään tulosta.");
			l.addStyleName(ValoTheme.LABEL_BOLD);
			l.addStyleName(ValoTheme.LABEL_HUGE);
			main.gridPanelLayout.addComponent(l);
			return;
		}
		
		final IndexedContainer container = new IndexedContainer();
		
		for(String key : keys) {
			container.addContainerProperty(key, String.class, "");
		}
		
		rows: for(Map<String,ReportCell> map : state.report) {
			Object item = container.addItem();
			for(String key : keys)
				if(map.get(key) == null)
					continue rows;
			
			for(Map.Entry<String,ReportCell> entry : map.entrySet()) {
				@SuppressWarnings("unchecked")
				com.vaadin.data.Property<String> p = container.getContainerProperty(item, entry.getKey());
				p.setValue(entry.getValue().id);
			}
			
		}
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		
		final TextField filter = new TextField();
		filter.addStyleName(ValoTheme.TEXTFIELD_TINY);
		filter.setInputPrompt("rajaa hakutuloksia - kirjoitetun sanan tulee löytyä rivin teksteistä");
		filter.setWidth("100%");
		
		final Image clipboard = new Image();
		clipboard.setSource(new ThemeResource("page_white_excel.png"));
		clipboard.setHeight("24px");
		clipboard.setWidth("24px");
		
		hl.addComponent(filter);
		hl.setExpandRatio(filter, 1.0f);
		hl.setComponentAlignment(filter, Alignment.BOTTOM_CENTER);
		hl.addComponent(clipboard);
		hl.setComponentAlignment(clipboard, Alignment.BOTTOM_CENTER);
		hl.setExpandRatio(clipboard, 0.0f);
		
		main.gridPanelLayout.addComponent(hl);
		main.gridPanelLayout.setExpandRatio(hl, 0f);
		
		filter.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 3033918399018888150L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				container.removeAllContainerFilters();
				container.addContainerFilter(new QueryFilter(filter.getValue(), true, false));
			}
		});
		
		AbsoluteLayout abs = new AbsoluteLayout();
		abs.setSizeFull();
		
		final Grid queryGrid = new Grid(container);
		queryGrid.setSelectionMode(SelectionMode.NONE);
		queryGrid.setHeightMode(HeightMode.CSS);
		queryGrid.setHeight("100%");
		queryGrid.setWidth("100%");
		
		for(String key : keys) {
			Column col = queryGrid.getColumn(key);
			col.setExpandRatio(1);
		}
		
		abs.addComponent(queryGrid);
		
		OnDemandFileDownloader dl = new OnDemandFileDownloader(new OnDemandStreamSource() {
			
			private static final long serialVersionUID = 981769438054780731L;

			File f; 
			Date date = new Date();

			@Override
			public InputStream getStream() {
				
				String uuid = UUID.randomUUID().toString();
				f = new File("printing", uuid+".xlsx"); 
				
				Workbook w = new XSSFWorkbook();
				Sheet sheet = w.createSheet("Sheet1");
				Row header = sheet.createRow(0);
				for(int i=0;i<keys.size();i++) {
					Cell cell = header.createCell(i);
					cell.setCellValue(keys.get(i));
				}

				int row = 1;
				rows: for(Map<String,ReportCell> map : state.report) {
					for(String key : keys)
						if(map.get(key) == null)
							continue rows;
					
					Row r = sheet.createRow(row++);
					int column = 0;
					for(int i=0;i<keys.size();i++) {
						Cell cell = r.createCell(column++);
						ReportCell rc = map.get(keys.get(i));
						if(rc.id.equals(rc.caption)) {
							cell.setCellValue(rc.id);
						} else {
							cell.setCellValue(rc.caption);
						}
					}
					
				}
				
		    	try {
		    		FileOutputStream s = new FileOutputStream(f);
		    		w.write(s);
		    		s.close();
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
				
				try {
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				throw new IllegalStateException();
				
			}
			
			@Override
			public void onRequest() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public long getFileSize() {
				return f.length();
			}
			
			@Override
			public String getFileName() {
				return "Strategiakartta_" + Utils.dateString(date) + ".xlsx";
			}
			
		});
		
		dl.getResource().setCacheTime(0);
		dl.extend(clipboard);

		main.gridPanelLayout.addComponent(abs);
		main.gridPanelLayout.setExpandRatio(abs, 1f);
		
	}
	
	public static void updateTags(final Main main) {

		final Database database = main.getDatabase();

		main.tags.removeAllComponents();
		main.tags.setMargin(true);

		ArrayList<Tag> sorted = new ArrayList<Tag>(Tag.enumerate(database)); 
		Collections.sort(sorted, new Comparator<Tag>() {

			@Override
			public int compare(Tag arg0, Tag arg1) {
				return arg0.getId(database).compareTo(arg1.getId(database));
			}
			
		});
		
		for (final Tag t : sorted) {

			final HorizontalLayout hl = new HorizontalLayout();
			hl.setSpacing(true);
			Label l = new Label(t.getId(database));
			l.setSizeUndefined();
			l.addStyleName(ValoTheme.LABEL_HUGE);

			hl.addComponent(l);
			hl.setComponentAlignment(l, Alignment.BOTTOM_LEFT);

			final Image select = new Image("", new ThemeResource("cursor.png"));
			select.setHeight("24px");
			select.setWidth("24px");
			select.setDescription("Valitse");
			select.addClickListener(new MouseEvents.ClickListener() {

				private static final long serialVersionUID = 3734678948272593793L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					main.setCurrentItem(t, main.getUIState().currentPosition);
					Utils.loseFocus(select);
				}
				
			});
			hl.addComponent(select);
			hl.setComponentAlignment(select, Alignment.BOTTOM_LEFT);
			
			final Image edit = new Image("", new ThemeResource("table_edit.png"));
			edit.setHeight("24px");
			edit.setWidth("24px");
			edit.setDescription("Muokkaa");
			edit.addClickListener(new MouseEvents.ClickListener() {

				private static final long serialVersionUID = -3792353723974454702L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					Utils.editTextAndId(main, "Muokkaa aihetunnistetta", t);
					updateTags(main);
				}
				
			});
			hl.addComponent(edit);
			hl.setComponentAlignment(edit, Alignment.BOTTOM_LEFT);

			main.tags.addComponent(hl);
			main.tags.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);

			Label l2 = new Label(t.getText(database));
			l2.addStyleName(ValoTheme.LABEL_LIGHT);
			l2.setSizeUndefined();
			main.tags.addComponent(l2);
			main.tags.setComponentAlignment(l2, Alignment.MIDDLE_CENTER);

		}

	}
	
}
