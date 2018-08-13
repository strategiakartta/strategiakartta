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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.ResizeEvent;
import com.vaadin.ui.Window.ResizeListener;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.action.Actions;
import fi.semantum.strategia.action.CreatePrincipalMeter;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.custom.PDFButton;
import fi.semantum.strategia.filter.FilterState;
import fi.semantum.strategia.filter.NodeFilter;
import fi.semantum.strategia.filter.SearchFilter;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Browser;
import fi.semantum.strategia.widget.Browser.BrowserListener;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategia.widget.BrowserNodeState;
import fi.semantum.strategia.widget.D3;
import fi.semantum.strategia.widget.D3.D3Listener;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Datatype;
import fi.semantum.strategia.widget.EnumerationDatatype;
import fi.semantum.strategia.widget.Indicator;
import fi.semantum.strategia.widget.MapVis;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.MeterDescription;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;
import fi.semantum.strategia.widget.TimeConfiguration;

@SuppressWarnings("serial")
@Theme("fi_semantum_strategia")
//@PreserveOnRefresh
public class Main extends UI {

	private static String wikiPrefix = "Strategiakartta_";

	public static final String TOTEUTTAA = "Toteuttaa";

	private Database database;
	public Account account;
	public String wikiToken;
	public List<List<String>> propertyCells = new ArrayList<List<String>>();
	public Window mapDialog = null;
	
	public ScheduledExecutorService background = Executors.newScheduledThreadPool(1);

	public UIState uiState = new UIState();

	private static File baseDirectory = null;

	public static File getAppFile(String path) {
		File mf = new File(Main.class.getResource("Main.class").getFile());
		File base = mf.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
		return new File(base, path);
	}
	
	public static File baseDirectory() {
		if(baseDirectory == null) {
			String baseDirectoryPath = System.getenv("strategia_base_directory");
			if(baseDirectoryPath == null)
				baseDirectoryPath = "./strategia-db";
			baseDirectory = new File(baseDirectoryPath);
		}
		return baseDirectory;
	}

	
	public static String getWikiPrefix(Database database) {
		if("database".equals(database.getDatabaseId())) {
			return wikiPrefix;
		} else {
			return wikiPrefix + database.getDatabaseId() + "_";
		}
	}
	
	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public UIState getUIState() {
		return uiState;
	}

	public Account getAccountDefault() {
		if (account != null)
			return account;
		else
			return database.guest;
	}

	public static class TimeInterval {
		
		private static final TimeInterval ALWAYS = new TimeInterval(Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		final public int startYear;
		final public int endYear;

		public TimeInterval(int start, int end) {
			if(start < 2000)
				start = 2000;
			if(end > 2100)
				end = 2100;
			startYear = start;
			endYear = end;
		}

		public boolean isSingle() {
			return startYear == endYear;
		}
		
		public Set<Integer> years() {
			Set<Integer> result = new TreeSet<Integer>();
			for(int i=startYear;i<=endYear;i++)
				 result.add(i);
			return result;
		}
		public boolean contains(String ref) {
			try {
				int year = Integer.parseInt(ref);
				return (year >= startYear) && (year <= endYear);
			} catch (NumberFormatException e) {
				return true;
			}
		}

		public static TimeInterval parse(String s) {
			if (Property.AIKAVALI_KAIKKI.equals(s))
				return ALWAYS;
			if (s == null)
				return ALWAYS;
			int idx = s.lastIndexOf('-');
			if (idx == -1) {
				try {
					int startY = Integer.parseInt(s);
					return new TimeInterval(startY, startY);
				} catch (NumberFormatException e) {
					return ALWAYS;
				}
			}
			String start = s.substring(0, idx);
			String end = s.substring(idx + 1);
			try {
				int startY = Integer.parseInt(start);
				int endY = Integer.parseInt(end);
				return new TimeInterval(startY, endY);
			} catch (NumberFormatException e) {
				return ALWAYS;
			}
		}
		
		public boolean intersects(TimeInterval other) {
			if(other.endYear < startYear) return false;
			if(other.startYear > endYear) return false;
			return true;
		}
		
	}

	public boolean acceptTime(String t) {
		String ref = getUIState().time;
		return Utils.acceptTime(t, ref);
	}

	public Map<String, UIState> fragments = new HashMap<String, UIState>();
	public AbsoluteLayout abs;
	public Button less;
	public Button reportAllButton;
	public Label reportStatus;
	public boolean menuActive = false;

	final D3 js = new D3();
	final D3 js2 = new D3();
	final D3 js3 = new D3();
	Button hallinnoi;
	Button tili;
	Button login;
	Button duplicate;
	Button duplicate2;
	Label modeLabel;
	Button mode;
	Button meterMode;
	PDFButton pdf;
	Button propertyExcelButton;
	final Browser browser_ = new Browser(new BrowserNode[0], new BrowserLink[0], 100, 100);
	VerticalLayout browser;
	VerticalLayout wiki_;
	BrowserFrame wiki;
	String wikiPage;
	Base wikiBase;
	HorizontalSplitPanel hs;
	Panel propertiesPanel;
	public VerticalLayout properties;
	VerticalLayout tags;
	VerticalLayout panelLayout;
	Panel panel;
	VerticalLayout js2Container;
	VerticalLayout gridPanelLayout;
	ComboBox times;
	ComboBox states;
	Button saveState;
	Button more;
	Button hori;
	ComboBox filter;

	boolean filterListenerActive = true;
	ValueChangeListener filterListener = new ValueChangeListener() {

		public void valueChange(ValueChangeEvent event) {

			if (!filterListenerActive)
				return;

			Object value = filter.getValue();
			if (value == null)
				return;

			String selected = value.toString();

			for (NodeFilter f : availableFilters) {
				if (f.toString().equals(selected)) {
					uiState.setCurrentFilter(f);
					f.refresh();
					switchToBrowser();
					Updates.updateJS(Main.this, false);
					return;
				}
			}

		}

	};

	public int stateCounter = 1;

	ValueChangeListener statesListener = new ValueChangeListener() {

		public void valueChange(ValueChangeEvent event) {

			Object value = states.getValue();
			if (value == null)
				return;

			String selected = value.toString();
			for (UIState state : account.uiStates) {
				if (state.name.equals(selected)) {
					setFragment(state.duplicate("s" + stateCounter++), true, true);
					return;
				}
			}

		}

	};

	ValueChangeListener timesListener = new ValueChangeListener() {

		public void valueChange(ValueChangeEvent event) {

			String value = (String) times.getValue();
			if (value == null)
				return;

			UIState state = uiState.duplicate(Main.this);
			state.time = value;
			setFragment(state, true);
			return;

		}

	};

	int windowWidth;
	int windowHeight;

	private void setWindowWidth(int newWidth, int newHeight) {
		windowWidth = newWidth;
		windowHeight = newHeight;
	}

	FileResource redResource;
	FileResource greenResource;
	FileResource blackResource;
	FileResource mapMagnify;

	String backFragment = "";

	public void setFragment(UIState state, boolean update) {
		setFragment(state, false, update);
	}

	public void setFragment(UIState state, boolean setPositions, boolean update) {
		if (uiState.name.equals(state.name))
			return;
		backFragment = uiState.name;
		fragments.put(state.name, state);
		getPage().setUriFragment(state.name);
		uiState = state;
		if (update || setPositions)
			Updates.update(this, setPositions, false);
	}

	public void applyFragment(String uuid, boolean update) {
		if (uiState.name.equals(uuid))
			return;
		backFragment = uiState.name;
		UIState state = fragments.get(uuid);
		if (state == null)
			return;
		uiState = state;
		if (update)
			Updates.update(this, false);
	}

	static class MapListener implements D3Listener {

		final private Main main;
		final private boolean isReference;

		public MapListener(Main main, boolean isReference) {
			this.main = main;
			this.isReference = isReference;
		}

		private Strategiakartta getMap() {
			if (isReference) {
				return main.getUIState().reference;
			} else {
				return main.getUIState().current;
			}
		}

		private Tavoite getGoal(int index) {
			Strategiakartta map = getMap();
			if (index == map.tavoitteet.length)
				return map.voimavarat;
			else
				return map.tavoitteet[index];
		}

		@Override
		public void select(double x, double y) {
			Actions.selectAction(main, x, y, null, getMap());
		}

		@Override
		public void navigate(String kartta) {
			Database database = main.getDatabase();
			Strategiakartta k = database.find(kartta);
			if (k != null) {
				UIState s = main.getUIState().duplicate(main);
				s.current = k;
				main.setFragment(s, true);
			}
		}

		@Override
		public void drill(int tavoite) {
			Tavoite goal = getGoal(tavoite);
			try {
				Strategiakartta k = goal.getPossibleImplementationMap(main.getDatabase());
				if (k != null) {
					UIState s = main.getUIState().duplicate(main);
					s.current = k;
					main.setFragment(s, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void navi(double x, double y, int tavoite) {
			Actions.selectAction(main, x, y, getMap(), getGoal(tavoite));
//			if(main.getUIState().input) {
//			} else {
//				Tavoite goal = getGoal(tavoite);
//				Strategiakartta k = goal.getImplementingSubmap(main.getDatabase());
//				if (k != null) {
//					UIState s = main.getUIState().duplicate(main);
//					s.current = k;
//					main.setFragment(s, true);
//				}
//			}
		}

		@Override
		public void navi2(double x, double y, int tavoite, int painopiste) {
			Actions.selectAction(main, x, y, getGoal(tavoite), getGoal(tavoite).painopisteet[painopiste]);
		}

		@Override
		public void editHeader() {
			Utils.editTextAndId(main, "Muokkaa strategiakartan nimeä", getMap());
		}

		@Override
		public void editVisio() {

			Strategiakartta map = getMap();

			final Window subwindow = new Window("Muokkaa visiota", new VerticalLayout());
			subwindow.setModal(true);
			subwindow.setWidth("400px");
			subwindow.setHeight("500px");
			subwindow.setResizable(false);

			VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
			winLayout.setMargin(true);
			winLayout.setSpacing(true);

			final TextArea tf = new TextArea();
			tf.setValue(map.visio);
			tf.setWidth("360px");
			tf.setHeight("390px");
			winLayout.addComponent(tf);

			Button save = new Button("Tallenna", new Button.ClickListener() {

				public void buttonClick(ClickEvent event) {
					String value = tf.getValue();
					main.removeWindow(subwindow);
					getMap().visio = value;
					Updates.updateJS(main, true);
				}
			});

			Button discard = new Button("Peru muutokset", new Button.ClickListener() {

				private static final long serialVersionUID = -784522457615993823L;

				public void buttonClick(ClickEvent event) {
					Updates.updateJS(main, true);
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

			tf.setCursorPosition(tf.getValue().length());

		}
		
		private void editMeter(Base b, Meter m) {
			
			Database database = main.getDatabase();
			
			boolean canWrite = main.canWrite(getMap());

			Indicator indicator = m.getPossibleIndicator(database);
			if (canWrite && indicator != null && main.getUIState().input) {

				Datatype dt = indicator.getDatatype(database);
				if (dt instanceof EnumerationDatatype) {
					Utils.setUserMeter(main, b, m);
					return;
				}

			}

			String id = m.getCaption(database);
			String exp = m.describe(database, main.getUIState().forecastMeters);
			Indicator i = m.getPossibleIndicator(database);
			if (i != null) {
				String shortComment = i.getValueShortComment();
				if (!shortComment.isEmpty())
					exp += ", " + shortComment;
			}

			String content = "<div style=\"width: 800px; border: 2px solid; padding: 5px\">";
			/*content += "<div style=\"text-align: center; white-space:normal; font-size: 22px; padding: 5px\">" + id
					+ "</div>";*/
			content += "<div style=\"text-align: center; white-space:normal; font-size: 24px; padding: 15px\">" + exp
					+ "</div>";
			content += "</div>";

			Notification n = new Notification(content, Notification.Type.HUMANIZED_MESSAGE);
			n.setHtmlContentAllowed(true);
			n.show(Page.getCurrent());
			
		}

		@Override
		public void selectMeter(int tavoite, int painopiste, int index, String link) {

			Base b = null;
			if (painopiste == -1) {
				b = getGoal(tavoite);
			} else {
				b = getGoal(tavoite).painopisteet[painopiste];
			}

			if (b == null)
				return;
			
			Database database = main.getDatabase();
			
			if(main.getUIState().useImplementationMeters) {
				
				boolean forecast = main.getUIState().forecastMeters;
				
				Base linked = database.find(link);
				if(linked != null) {
					UIState s = main.getUIState().duplicate(main);
					s.current = (Strategiakartta)linked;
					main.setFragment(s, true);
					return;
				}

				boolean canWrite = main.canWrite(getMap());
				if(canWrite) {
					if(b instanceof Painopiste) {
						Painopiste p = (Painopiste)b;
						List<Meter> descs = p.getImplementationMeters(main, forecast);
						if(descs.isEmpty()) {
							Meter m = p.getPrincipalMeter(main, "", forecast);
							if(m != null) descs = Collections.singletonList(m);
						}
						if(index < descs.size()) {
							Meter m = descs.get(index);
							if(!m.isTransient()) {
								editMeter(b, m);
							}
						}
					}
				}
				
			} else {

				List<MeterDescription> descs = Meter.makeMeterDescriptions(main, b, true);
				Meter m = descs.get(index).meter;
				editMeter(b, m);
				
			}

		}

		@Override
		public void editTavoite(final int tavoite) {

			Database database = main.getDatabase();

			Tavoite t = getGoal(tavoite);
			if (t.isCopy(database))
				return;

			Utils.editTextAndId(main, "Muokkaa tavoitteen määritystä", t);

		}

		@Override
		public void editPainopiste(final int tavoite, final int painopiste) {

			Database database = main.getDatabase();

			Painopiste p = getGoal(tavoite).painopisteet[painopiste];
			if (p.isCopy(database))
				return;

			Utils.editTextAndId(main, "Muokkaa painopisteen määritystä", p);

		}

		@Override
		public void removeTavoite(int index) {
			getMap().removeTavoite(index);
			Updates.updateJS(main, true);
		}

		@Override
		public void displayInfo(final int tavoite, final int painopiste) {

			Database database = main.getDatabase();
			
			Painopiste p = getGoal(tavoite).painopisteet[painopiste];

			Map<String,String> resp = Utils.getResponsibilityMap(database, p);
			if(resp.isEmpty()) return;
			
			String content = "<div style=\"width: 825px; padding: 5px\">";
			content +="<div style=\"text-align:center;margin:10px\">Vastuut</div>";
			content +="<table style=\"border: 1px solid black;border-collapse: collapse\">";
			for(String field : resp.keySet()) {
				content += "<tr>";
				content += "<td style=\"\"><div style=\"margin: 5px;width: 250px\">" + field + "</div></fd>";
				String value = resp.get(field);
				content += "<td style=\"border: 1px solid black;\"><div style=\"margin: 5px;width: 550px;white-space:normal\">" + value + "</div></fd>";
				content += "</tr>";	
			}
			content += "</table>";
			content += "</div>";

			Notification n = new Notification(content, Notification.Type.HUMANIZED_MESSAGE);
			n.setHtmlContentAllowed(true);
			n.show(Page.getCurrent());

		}
		
		@Override
		public void displayMeter(final int tavoite, final int painopiste) {

			Painopiste p = getGoal(tavoite).painopisteet[painopiste];
			
			// The unique leaf box
			Base leaf = main.hasLeaf(p);
			
			for(Meter m : leaf.getMetersActive(main)) {
				if(m.isPrincipal) {
					Utils.setUserMeter(main, leaf, m);
					return;
				}
			}
			
			CreatePrincipalMeter.perform(main, leaf);

			for(Meter m : leaf.getMetersActive(main)) {
				if(m.isPrincipal) {
					Utils.setUserMeter(main, leaf, m);
					return;
				}
			}

		}

	};
	
	private String validatePathInfo(String pathInfo) {
		if(pathInfo.isEmpty()) return "database";
		if(pathInfo.contains("/")) return "database";
		if(pathInfo.length() > 32) return "database";
		for(int i=0;i<pathInfo.length();i++) {
			char c = pathInfo.charAt(i);
			if(!Character.isJavaIdentifierPart(c)) return "database";
		}
		return pathInfo;
	}
	
//	private String resolveDatabase(so) {
//		String pathInfo = request.getPathInfo();
//		if(pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
//		if(pathInfo.endsWith("/")) pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
//		return validatePathInfo(pathInfo);
//	}
	

	@Override
	protected void refresh(VaadinRequest request) {
		super.refresh(request);
	}
	

	@Override
	protected void init(VaadinRequest request) {

		getPage().addUriFragmentChangedListener(new UriFragmentChangedListener() {
			public void uriFragmentChanged(UriFragmentChangedEvent source) {
				applyFragment(source.getUriFragment(), true);
			}
		});
		
		String pathInfo = request.getPathInfo();
		if(pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
		if(pathInfo.endsWith("/")) pathInfo = pathInfo.substring(0, pathInfo.length() - 1);

		String databaseId = validatePathInfo(pathInfo);

		setWindowWidth(Page.getCurrent().getBrowserWindowWidth(), Page.getCurrent().getBrowserWindowHeight());

		// Find the application directory
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

		// Image as a file resource
		redResource = new FileResource(new File(basepath + "/WEB-INF/images/bullet_red.png"));
		greenResource = new FileResource(new File(basepath + "/WEB-INF/images/bullet_green.png"));
		blackResource = new FileResource(new File(basepath + "/WEB-INF/images/bullet_black.png"));
		mapMagnify = new FileResource(new File(basepath + "/WEB-INF/images/map_magnify.png"));

		abs = new AbsoluteLayout();

		final VerticalLayout vs = new VerticalLayout();
		vs.setSizeFull();

		abs.addComponent(vs);
		setContent(abs);

		// This will set the login cookie
		Wiki.login(this);

		// Make sure that the printing directory exists
		new File(Main.baseDirectory(), "printing").mkdirs();

		database = Database.load(this, databaseId);
		database.getOrCreateTag("Tavoite");
		database.getOrCreateTag("Painopiste");
		
		for(Strategiakartta map : Strategiakartta.enumerate(database)) {
			Strategiakartta parent = map.getPossibleParent(database);
			if(parent == null)
				uiState.setCurrentMap(parent);
		}
		
		if (uiState.getCurrentMap() == null)
			uiState.setCurrentMap(database.getRoot());

		uiState.currentPosition = uiState.getCurrentMap();

		uiState.currentItem = uiState.getCurrentMap();

		setPollInterval(10000);

		addPollListener(new PollListener() {

			@Override
			public void poll(PollEvent event) {

				if (database.checkChanges()) {
					String curr = uiState.getCurrentMap().uuid;
					database = Database.load(Main.this, database.getDatabaseId());
					uiState.setCurrentMap((Strategiakartta) database.find(curr));
					Updates.updateJS(Main.this, false);
				}

			}

		});

		js.addListener(new MapListener(this, false));
		js2.addListener(new MapListener(this, true));

		browser_.addListener(new BrowserListener() {

			@Override
			public void select(double x, double y, String uuid) {
				Base b = database.find(uuid);
				Actions.selectAction(Main.this, x, y, null, b);
			}

			@Override
			public void save(String name, Map<String, BrowserNodeState> states) {

				UIState state = getUIState().duplicate(name);
				state.browserStates = states;

				account.uiStates.add(state);

				Updates.update(Main.this, true);

			}

		});

		Page.getCurrent().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {

			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event) {
				setWindowWidth(event.getWidth(), event.getHeight());
				Updates.updateJS(Main.this, false);
			}
		});
		
		modeLabel = new Label("Katselutila");
		modeLabel.setWidth("95px");
		modeLabel.addStyleName("viewMode");
		
		mode = new Button();
		mode.setDescription("Siirry tiedon syöttötilaan");
		mode.setIcon(FontAwesome.EYE);
		mode.addStyleName(ValoTheme.BUTTON_TINY);

		mode.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				
				if("Siirry tiedon syöttötilaan".equals(mode.getDescription())) {
					
					mode.setDescription("Siirry katselutilaan");
					mode.setIcon(FontAwesome.PENCIL);
					modeLabel.setValue("Syöttötila");
					modeLabel.removeStyleName("viewMode");
					modeLabel.addStyleName("editMode");

					UIState s = uiState.duplicate(Main.this);
					s.input = true;
					setFragment(s, true);
					
				} else {
					
					mode.setDescription("Siirry tiedon syöttötilaan");
					mode.setIcon(FontAwesome.EYE);
					modeLabel.setValue("Katselutila");
					modeLabel.removeStyleName("editMode");
					modeLabel.addStyleName("viewMode");

					UIState s = uiState.duplicate(Main.this);
					s.input = false;
					setFragment(s, true);
					
				}

			}

		});

		meterMode = new Button();
		meterMode.setDescription("Vaihda toteumamittareihin");
		meterMode.setCaption("Ennuste");
		meterMode.addStyleName(ValoTheme.BUTTON_TINY);

		meterMode.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				
				if("Vaihda toteumamittareihin".equals(meterMode.getDescription())) {
					
					meterMode.setDescription("Vaihda ennustemittareihin");
					meterMode.setCaption("Toteuma");

					UIState s = uiState.duplicate(Main.this);
					s.setActualMeters();
					setFragment(s, true);
					
				} else {

					meterMode.setDescription("Vaihda toteumamittareihin");
					meterMode.setCaption("Ennuste");

					UIState s = uiState.duplicate(Main.this);
					s.setForecastMeters();
					setFragment(s, true);
					
				}

			}

		});
		
		pdf = new PDFButton();
		pdf.setDescription("Tallenna kartta PDF-muodossa");
		pdf.setIcon(FontAwesome.PRINT);
		pdf.addStyleName(ValoTheme.BUTTON_TINY);

		pdf.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				Utils.print(Main.this);

			}

		});
		
		propertyExcelButton = new Button();
		propertyExcelButton.setDescription("Tallenna tiedot Excel-tiedostona");
		propertyExcelButton.setIcon(FontAwesome.PRINT);
		propertyExcelButton.addStyleName(ValoTheme.BUTTON_TINY);
		
		OnDemandFileDownloader dl = new OnDemandFileDownloader(new OnDemandStreamSource() {
			
			private static final long serialVersionUID = 981769438054780731L;

			File f; 
			Date date = new Date();

			@Override
			public InputStream getStream() {
				
				String uuid = UUID.randomUUID().toString();
				File printing = new File(Main.baseDirectory(), "printing");
				f = new File(printing, uuid+".xlsx"); 
				
				Workbook w = new XSSFWorkbook();
				Sheet sheet = w.createSheet("Sheet1");
				int row = 1;
				for(List<String> cells : propertyCells) {
					Row r = sheet.createRow(row++);
					for(int i=0;i<cells.size();i++) {
						String value = cells.get(i);
						r.createCell(i).setCellValue(value);
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
		dl.extend(propertyExcelButton);

		states = new ComboBox();
		states.setWidth("250px");
		states.addStyleName(ValoTheme.COMBOBOX_TINY);
		states.setInvalidAllowed(false);
		states.setNullSelectionAllowed(false);

		states.addValueChangeListener(statesListener);

		saveState = new Button();
		saveState.setEnabled(false);
		saveState.setDescription("Tallenna nykyinen näkymä");
		saveState.setIcon(FontAwesome.BOOKMARK);
		saveState.addStyleName(ValoTheme.BUTTON_TINY);
		saveState.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Utils.saveCurrentState(Main.this);
			}

		});

		class SearchTextField extends TextField {
			public boolean hasFocus = false;
		}

		final SearchTextField search = new SearchTextField();
		search.setWidth("100%");
		search.addStyleName(ValoTheme.TEXTFIELD_TINY);
		search.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
		search.setInputPrompt("hae vapaasanahaulla valitun asian alta");
		search.setId("searchTextField");
		search.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {

				if (!search.hasFocus)
					return;

				String text = search.getValue().toLowerCase();
				try {

					Map<String, String> content = new HashMap<String, String>();
					List<String> hits = Lucene.search(database.getDatabaseId(), text + "*");
					for (String uuid : hits) {
						Base b = database.find(uuid);
						if (b != null) {
							String report = "";
							Map<String, String> map = b.searchMap(database);
							for (Map.Entry<String, String> e : map.entrySet()) {
								if (e.getValue().contains(text)) {
									if (!report.isEmpty())
										report += ", ";
									report += e.getKey();
								}
							}
							if (!report.isEmpty())
								content.put(uuid, report);
						}
					}

					uiState.setCurrentFilter(new SearchFilter(Main.this, content));

					Updates.updateJS(Main.this, false);

					switchToBrowser();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		search.addFocusListener(new FocusListener() {

			@Override
			public void focus(FocusEvent event) {
				search.hasFocus = true;
			}
		});
		search.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				search.hasFocus = false;
			}
		});

		hallinnoi = new Button("Hallinnoi");
		hallinnoi.setWidthUndefined();
		hallinnoi.setVisible(false);
		hallinnoi.addStyleName(ValoTheme.BUTTON_TINY);
		hallinnoi.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (account != null) {
					if (account.isAdmin()) {
						Utils.manage(Main.this);
					}
				}
			}

		});

		tili = new Button("Käyttäjätili");
		tili.setWidthUndefined();
		tili.setVisible(false);
		tili.addStyleName(ValoTheme.BUTTON_TINY);
		tili.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (account != null) {
					Utils.modifyAccount(Main.this);
				}
			}

		});

		duplicate = new Button("Avaa ikkunassa");
		duplicate2 = new Button("Avaa alas");

		duplicate.setWidthUndefined();
		duplicate.addStyleName(ValoTheme.BUTTON_TINY);
		duplicate.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				MapVis model = js2.getModel();
				if (model == null) {

					UIState s = uiState.duplicate(Main.this);
					s.reference = s.current;

					mapDialog = new Window(s.reference.getText(database), new VerticalLayout());
					mapDialog.setWidth(dialogWidth());
					mapDialog.setHeight(dialogHeight());
					mapDialog.setResizable(true);
					mapDialog.setContent(js2Container);
					mapDialog.setVisible(true);
					mapDialog.setResizeLazy(false);
					mapDialog.addCloseListener(new CloseListener() {

						@Override
						public void windowClose(CloseEvent e) {

							duplicate.setCaption("Avaa ikkunassa");
							duplicate2.setVisible(true);

							UIState s = uiState.duplicate(Main.this);
							mapDialog.close();
							mapDialog = null;
							s.reference = null;
							setFragment(s, true);

						}

					});
					mapDialog.addResizeListener(new ResizeListener() {

						@Override
						public void windowResized(ResizeEvent e) {
							Updates.updateJS(Main.this, false);
						}

					});

					setFragment(s, true);

					addWindow(mapDialog);

					duplicate.setCaption("Sulje referenssi");
					duplicate2.setVisible(false);

				} else {

					UIState s = uiState.duplicate(Main.this);
					if (mapDialog != null) {
						mapDialog.close();
						mapDialog = null;
					}

					panelLayout.removeComponent(js2Container);

					s.reference = null;
					setFragment(s, true);

					duplicate.setCaption("Avaa ikkunassa");
					duplicate2.setVisible(true);

				}

			}

		});

		duplicate2.setWidthUndefined();
		duplicate2.addStyleName(ValoTheme.BUTTON_TINY);
		duplicate2.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				MapVis model = js2.getModel();
				assert (model == null);

				UIState s = uiState.duplicate(Main.this);
				s.reference = s.current;
				setFragment(s, true);

				panelLayout.addComponent(js2Container);

				duplicate.setCaption("Sulje referenssi");
				duplicate2.setVisible(false);

			}

		});

		login = new Button("Kirjaudu");
		login.setWidthUndefined();
		login.addStyleName(ValoTheme.BUTTON_TINY);
		login.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (account != null) {
					account = null;
					hallinnoi.setVisible(false);
					tili.setVisible(false);
					Updates.update(Main.this, true);
					login.setCaption("Kirjaudu");
				} else {
					Login.login(Main.this);
				}
			}

		});

		times = new ComboBox();
		times.setWidth("130px");
		times.addStyleName(ValoTheme.COMBOBOX_SMALL);
		times.addItem(Property.AIKAVALI_KAIKKI);
		times.addItem("2016");
		times.addItem("2017");
		times.addItem("2018");
		times.addItem("2019");
		times.select("2016");
		times.setInvalidAllowed(false);
		times.setNullSelectionAllowed(false);

		times.addValueChangeListener(timesListener);

		final HorizontalLayout hl0 = new HorizontalLayout();
		hl0.setWidth("100%");
		hl0.setHeight("32px");
		hl0.setSpacing(true);

		hl0.addComponent(pdf);
		hl0.setComponentAlignment(pdf, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(pdf, 0.0f);

		hl0.addComponent(propertyExcelButton);
		hl0.setComponentAlignment(propertyExcelButton, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(propertyExcelButton, 0.0f);

		hl0.addComponent(states);
		hl0.setComponentAlignment(states, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(states, 0.0f);

		hl0.addComponent(saveState);
		hl0.setComponentAlignment(saveState, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(saveState, 0.0f);

		hl0.addComponent(times);
		hl0.setComponentAlignment(times, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(times, 0.0f);

		hl0.addComponent(search);
		hl0.setComponentAlignment(search, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(search, 1.0f);

		hl0.addComponent(modeLabel);
		hl0.setComponentAlignment(modeLabel, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(modeLabel, 0.0f);

		hl0.addComponent(mode);
		hl0.setComponentAlignment(mode, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(mode, 0.0f);

		hl0.addComponent(meterMode);
		hl0.setComponentAlignment(meterMode, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(meterMode, 0.0f);

		hl0.addComponent(hallinnoi);
		hl0.setComponentAlignment(hallinnoi, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(hallinnoi, 0.0f);

		hl0.addComponent(tili);
		hl0.setComponentAlignment(tili, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(tili, 0.0f);

		hl0.addComponent(duplicate);
		hl0.setComponentAlignment(duplicate, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(duplicate, 0.0f);

		hl0.addComponent(duplicate2);
		hl0.setComponentAlignment(duplicate2, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(duplicate2, 0.0f);

		hl0.addComponent(login);
		hl0.setComponentAlignment(login, Alignment.MIDDLE_LEFT);
		hl0.setExpandRatio(login, 0.0f);

		propertiesPanel = new Panel();
		propertiesPanel.setSizeFull();

		properties = new VerticalLayout();
		properties.setSpacing(true);
		properties.setMargin(true);

		propertiesPanel.setContent(properties);
		propertiesPanel.setVisible(false);

		tags = new VerticalLayout();
		tags.setSpacing(true);
		Updates.updateTags(this);

		AbsoluteLayout tabs = new AbsoluteLayout();
		tabs.setSizeFull();

		{
			panel = new Panel();
			panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
			panel.setSizeFull();
			panel.setId("mapContainer1");
			panelLayout = new VerticalLayout();
			panelLayout.addComponent(js);
			panelLayout.setHeight("100%");

			js2Container = new VerticalLayout();
			js2Container.setHeight("100%");
			js2Container.addComponent(new Label("<hr />", ContentMode.HTML));
			js2Container.addComponent(js2);
			
			panel.setContent(panelLayout);
			tabs.addComponent(panel);
		}

		wiki = new BrowserFrame();
		wiki.setSource(new ExternalResource(Wiki.wikiAddress() + "/"));
		wiki.setWidth("100%");
		wiki.setHeight("100%");

		{

			wiki_ = new VerticalLayout();
			wiki_.setSizeFull();
			Button b = new Button("Palaa sovellukseen");
			b.setWidth("100%");
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					applyFragment(backFragment, true);
					String content = Wiki.get(wikiPage);
					if (content == null)
						return;
					int first = content.indexOf("<rev contentformat");
					if (first == -1)
						return;
					content = content.substring(first);
					int term = content.indexOf(">");
					content = content.substring(term + 1);
					int end = content.indexOf("</rev>");
					content = content.substring(0, end);
					if (wikiBase.modifyMarkup(Main.this, content)) {
						Updates.update(Main.this, true);
					}
				}
			});
			wiki_.addComponent(b);
			wiki_.addComponent(wiki);
			wiki_.setVisible(false);

			wiki_.setExpandRatio(b, 0.0f);
			wiki_.setExpandRatio(wiki, 1.0f);

			tabs.addComponent(wiki_);

		}

		hs = new HorizontalSplitPanel();
		hs.setSplitPosition(0, Unit.PIXELS);
		hs.setHeight("100%");
		hs.setWidth("100%");

		browser = new VerticalLayout();
		browser.setSizeFull();

		HorizontalLayout browserWidgets = new HorizontalLayout();
		browserWidgets.setWidth("100%");

		hori = new Button();
		hori.setDescription("Näytä asiat taulukkona");
		hori.setEnabled(true);
		hori.setIcon(FontAwesome.ARROW_RIGHT);
		hori.addStyleName(ValoTheme.BUTTON_TINY);
		hori.addClickListener(new ClickListener() {

			boolean right = false;

			@Override
			public void buttonClick(ClickEvent event) {
				if (right) {
					hs.setSplitPosition(0, Unit.PIXELS);
					hori.setIcon(FontAwesome.ARROW_RIGHT);
					hori.setDescription("Näytä asiat taulukkona");
					right = false;
				} else {
					hs.setSplitPosition(windowWidth / 2, Unit.PIXELS);
					hori.setIcon(FontAwesome.ARROW_LEFT);
					hori.setDescription("Piilota taulukko");
					right = true;
				}
			}

		});

		more = new Button();
		more.setDescription("Laajenna näytettävien asioiden joukkoa");
		more.setIcon(FontAwesome.PLUS_SQUARE);
		more.addStyleName(ValoTheme.BUTTON_TINY);
		more.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				uiState.level++;
				Updates.updateJS(Main.this, false);
				if (uiState.level >= 2)
					less.setEnabled(true);
			}

		});

		less = new Button();
		less.setDescription("Supista näytettävien asioiden joukkoa");
		less.setIcon(FontAwesome.MINUS_SQUARE);
		less.addStyleName(ValoTheme.BUTTON_TINY);
		less.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (uiState.level > 1) {
					uiState.level--;
					Updates.updateJS(Main.this, false);
				}
				if (uiState.level <= 1)
					less.setEnabled(false);
			}

		});

		reportAllButton = new Button();
		reportAllButton.setCaption("Näkyvät tulokset");
		reportAllButton.addStyleName(ValoTheme.BUTTON_TINY);
		reportAllButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (uiState.reportAll) {
					reportAllButton.setCaption("Näkyvät tulokset");
					uiState.reportAll = false;
				} else {
					reportAllButton.setCaption("Kaikki tulokset");
					uiState.reportAll = true;
				}
				Updates.updateJS(Main.this, false);
			}

		});
		
		reportStatus = new Label("0 tulosta.");
		reportStatus.setWidth("100px");

		filter = new ComboBox();
		filter.setWidth("100%");
		filter.addStyleName(ValoTheme.COMBOBOX_SMALL);
		filter.setInvalidAllowed(false);
		filter.setNullSelectionAllowed(false);

		filter.addValueChangeListener(filterListener);

		browserWidgets.addComponent(hori);
		browserWidgets.setComponentAlignment(hori, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(hori, 0.0f);

		browserWidgets.addComponent(more);
		browserWidgets.setComponentAlignment(more, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(more, 0.0f);

		browserWidgets.addComponent(less);
		browserWidgets.setComponentAlignment(less, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(less, 0.0f);

		browserWidgets.addComponent(reportAllButton);
		browserWidgets.setComponentAlignment(reportAllButton, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(reportAllButton, 0.0f);

		browserWidgets.addComponent(reportStatus);
		browserWidgets.setComponentAlignment(reportStatus, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(reportStatus, 0.0f);

		browserWidgets.addComponent(filter);
		browserWidgets.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(filter, 1.0f);

		browser.addComponent(browserWidgets);
		browser.addComponent(hs);

		browser.setExpandRatio(browserWidgets, 0.0f);
		browser.setExpandRatio(hs, 1.0f);

		browser.setVisible(false);

		tabs.addComponent(browser);

		{
			gridPanelLayout = new VerticalLayout();
			gridPanelLayout.setMargin(false);
			gridPanelLayout.setSpacing(false);
			gridPanelLayout.setSizeFull();
			hs.addComponent(gridPanelLayout);
		}

		CssLayout browserLayout = new CssLayout();

		browserLayout.setSizeFull();

		browserLayout.addComponent(browser_);

		hs.addComponent(browserLayout);

		tabs.addComponent(propertiesPanel);

		vs.addComponent(hl0);
		vs.addComponent(tabs);

		vs.setExpandRatio(hl0, 0.0f);
		vs.setExpandRatio(tabs, 1.0f);

		// Ground state
		fragments.put("", uiState);

		setCurrentItem(uiState.currentItem, (Strategiakartta) uiState.currentItem);

	}

	List<NodeFilter> availableFilters = new ArrayList<NodeFilter>();

	void setTabState(UIState uiState, int state) {

		panel.setVisible(false);
		browser.setVisible(false);
		propertiesPanel.setVisible(false);
		wiki_.setVisible(false);
		pdf.setVisible(false);
		propertyExcelButton.setVisible(false);

		uiState.tabState = state;

		if (uiState.tabState == UIState.MAP) {
			panel.setVisible(true);
			pdf.setVisible(true);
		} else if (uiState.tabState == UIState.BROWSER) {
			browser.setVisible(true);
		} else if (uiState.tabState == UIState.WIKI) {
			wiki_.setVisible(true);
		} else if (uiState.tabState == UIState.PROPERTIES) {
			propertiesPanel.setVisible(true);
			propertyExcelButton.setVisible(true);
		}

	}

	public void switchToMap() {
		setTabState(uiState, UIState.MAP);
		Updates.update(this, false);
	}

	public void switchToBrowser() {
		setTabState(uiState, UIState.BROWSER);
		Updates.update(this, false);
	}

	void switchToWiki() {
		setTabState(uiState, UIState.WIKI);
		Updates.update(this, false);
	}

	public void switchToProperties() {
		setTabState(uiState, UIState.PROPERTIES);
		Updates.update(this, false);
	}

	public class TreeVisitor1 implements MapVisitor {

		FilterState filterState = new FilterState(Main.this, windowWidth, windowHeight);
		LinkedList<Base> path = new LinkedList<Base>();

		@Override
		public void visit(Base b) {

			path.add(b);

			Collection<Base> bases = uiState.getCurrentFilter().traverse(path, filterState);
			for (Base b2 : bases) {
				b2.accept(this);
			}

			uiState.getCurrentFilter().accept(path, filterState);

			path.removeLast();

		}

	}

	public void setCurrentItem(Base item, Strategiakartta position) {

		UIState state = uiState.duplicate(Main.this);
		state.currentItem = item;
		state.currentPosition = position;
		setFragment(state, true);

	}

	public void addRequiredItem(Base item) {

		UIState state = uiState.duplicate(Main.this);
		if(state.requiredItems == null) state.requiredItems = new HashSet<Base>();
		state.requiredItems.add(item);
		setFragment(state, true);

	}

	public void removeRequiredItem(Base item) {

		UIState state = uiState.duplicate(Main.this);
		if(state.requiredItems == null) state.requiredItems = new HashSet<Base>();
		state.requiredItems.remove(item);
		setFragment(state, true);

	}

	
	public String dialogWidth() {
		return dialogWidth(0.5);
	}

	public String dialogWidth(double ratio) {
		return (ratio * windowWidth) + "px";
	}

	public String dialogHeight() {
		return dialogHeight(0.5);
	}
	
	public String dialogHeight(double ratio) {
		return (ratio * windowHeight) + "px";
	}

	public boolean canWrite(Base b) {
		Account account = getAccountDefault();
		return account.canWrite(database, b);
	}
	
	public Base hasLeaf(Base b) {
		return b.hasLeaf(database, getUIState().time);
	}

	public boolean isLeaf(Base b) {
		return b.isLeaf(database, getUIState().time);
	}
	
	public boolean canWriteWithSelectedTime() {
		String currentTime = getUIState().time; 
		return TimeConfiguration.getInstance(database).canWrite(currentTime);
	}
	
}