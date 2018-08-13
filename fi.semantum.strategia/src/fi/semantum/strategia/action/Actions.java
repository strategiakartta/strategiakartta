package fi.semantum.strategia.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import elemental.events.KeyboardEvent.KeyCode;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.ObjectType;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Property;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class Actions {

	public static void selectAction(final Main main, Double x, Double y, final Base container, final Base base) {

		// Do not open a new menu if old one is already showing
		if(main.menuActive)
			return;
		
		boolean viewOnly = !main.getUIState().input;

		final Database database = main.getDatabase();

		List<Action> actions = new ArrayList<Action>();
		if(base instanceof Meter) return;
		
		Strategiakartta baseMap = database.getMap(base);
		boolean generated = baseMap != null ? !baseMap.generators.isEmpty() : false;

		if(!generated) {
			
			if(base instanceof Strategiakartta) {
				actions.add(new GenericSearch(main, base));
//				actions.add(new StrategianToteutus(main, base));
//				actions.add(new Valmiusasteet(main, base));
//				actions.add(new Tulostavoitteet(main, base));
			}
			
			if(!viewOnly) actions.add(new TaustaAsiakirja(main, base));
			if(!viewOnly) actions.add(new Ominaisuudet(main, base));
			
		}
		
		if(main.getUIState().tabState != 0) {
			actions.add(new AvaaStrategiakartassa(main, base));
		}

		if(base instanceof Tavoite) {
			
			Tavoite goal = (Tavoite)base;

			if(!generated) {
				if(!viewOnly) actions.add(new Voimavarat(main, base));
			}
			
			final String desc = goal.getFocusDescription(database);

			if(Account.canWrite(main, base) && !generated && !viewOnly) {

				Strategiakartta map = database.getMap(base);
				if(map.linkGoalsToSubmaps(database)) {
					try {
						if(goal.getPossibleImplementationMap(database) == null) {
							actions.add(new LisaaToteutuskartta(main, goal));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				actions.add(new LisaaPainopiste(desc, main, base));

				MoveUp moveUp = new MoveUp(main, (Tavoite)base);
				if(moveUp.accept(goal)) actions.add(moveUp);
				MoveDown moveDown = new MoveDown(main, (Tavoite)base);
				if(moveDown.accept(goal)) actions.add(moveDown);
				
				actions.add(new PoistaTavoite(desc, main, base));
				
				if(Utils.getPossibleImplemented(database, base) == null)
					actions.add(new AsetaToteuttamaan(main, base));

			}
			
		}
		
		if(base instanceof Painopiste) {
			
			if(Account.canWrite(main, base) && !generated && !viewOnly) {

				if(base.getMeters(database).isEmpty())
					actions.add(new CreatePrincipalMeter(main, base));

				actions.add(new DefineImplementors(main, base));
				actions.add(new MaaritaVastuut(main, base));

				final Tavoite t = database.getTavoite((Painopiste)base);
				int pos = t.findPainopiste((Painopiste)base);
				if(pos > 0) {
					actions.add(new MoveUp(main, (Painopiste)base));
				}
				if(pos < t.painopisteet.length - 1) {
					actions.add(new MoveDown(main, (Painopiste)base));
				}
				if(container != null) {
					actions.add(new PoistaPainopiste(main, base, container));
				}
				
			}
			
		}

		if(base instanceof Strategiakartta) {
			
			final Strategiakartta map = (Strategiakartta)base;
			
			if(!map.generators.isEmpty()) {
				actions.add(new PaivitaNakyma(main, map));
			}
			
			if(Account.canWrite(main, base) && !generated && !viewOnly) {
				actions.add(new SyotaTaulukko(main, base));
				actions.add(new LisaaTavoite(main, map));
				actions.add(new LisaaAlataso(main, map));
				actions.add(new LisaaNakyma(main, map));
				Base parent = map.getPossibleParent(database);
				if(parent == null) {
					actions.add(new LisaaYlataso(main, map));	
				}
				actions.add(new PoistaKartta(main, map));
				if(map.showVision) {
					actions.add(new PiilotaVisio(main, map));
				} else {
					actions.add(new NaytaVisio(main, map));
				}
				actions.add(new KartanTyyppi(main, map));
				actions.add(new MaaritaVastuut(main, base));
			}
			
//			if(main.getUIState().tabState == 1) {
//				actions.add(new RajaaAlle(main, map));
//			}

			if(main.getUIState().showTags) {
				actions.add(new PiilotaAihetunnisteet(main, base));
				if(base instanceof Strategiakartta) {
					actions.add(new ValitseAihetunnisteet(main, "Valitse aihetunnisteet", (Strategiakartta)base));
				}
			} else {
				actions.add(new ValitseAihetunnisteet(main, "Näytä aihetunnisteet", (Strategiakartta)base));
			}
			
			if(main.getUIState().showMeters) {
				actions.add(new PiilotaMittarit(main, base));
//				if(main.getUIState().useImplementationMeters) {
//					actions.add(new VaihdaKayttajanMittareihin(main, base));
//				} else {
//					actions.add(new VaihdaToteutusmittareihin(main, base));
//				}
			} else {
				actions.add(new NaytaMittarit(main, base));
			}
			
		}
		
		if(main.getUIState().tabState == 1) {
			actions.add(new RajaaAlle(main, base));
			actions.add(new PoistaRajaus(main, base));
		}
		
		if(actions.size() == 0) return;
		if(actions.size() == 1) {
			actions.get(0).run();
			return;
		}
		
		VerticalLayout menu = new VerticalLayout();
		final PopupView openerButton = new PopupView("", menu);
		menu.setWidth("350px");
		
		openerButton.addPopupVisibilityListener(new PopupVisibilityListener() {
			
			private static final long serialVersionUID = -841548194737021404L;

			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(!event.isPopupVisible()) {
					main.background.schedule(new Runnable() {
						@Override
						public void run() {
							main.getUI().access(new Runnable() {
								@Override
								public void run() {
									main.menuActive = false;
								}
							});
						}
					}, 1L, TimeUnit.SECONDS);
				}
			}
			
		});
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);
		
		if(desc.length() > 100) desc = desc.substring(0, 99) + " ...";
		
		Label header = new Label(desc);
		header.addStyleName(ValoTheme.LABEL_LARGE);
		header.addStyleName("menuHeader");
		header.setWidth("350px");
		header.setHeightUndefined();
		menu.addComponent(header);
		menu.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

		Label header2 = new Label("valitse toiminto");
		header2.addStyleName(ValoTheme.LABEL_LIGHT);
		header2.addStyleName(ValoTheme.LABEL_SMALL);
		header2.setSizeUndefined();
		menu.addComponent(header2);
		menu.setComponentAlignment(header2, Alignment.MIDDLE_CENTER);

		Map<String, List<Action>> categories = new HashMap<String, List<Action>>();
		
		for(Action a : actions) {
			String category = a.category;
			if(category != null) {
				List<Action> acts = categories.get(category);
				if(acts == null) {
					acts = new ArrayList<Action>();
					categories.put(category, acts);
					
				}
				acts.add(a);
			} else {
				addAction(main, Collections.singleton(openerButton), menu, a.getCaption(), a);
			}
		}
		
		for(Map.Entry<String,List<Action>> entries : categories.entrySet()) {
			addCategory(main, openerButton, menu, x.intValue(), y.intValue(), entries.getKey(), entries.getValue());
		}
		
		openerButton.setHideOnMouseOut(false);
		openerButton.setPopupVisible(true);
		
		main.abs.addComponent(openerButton, "left: " + x.intValue() + "px; top: " + y.intValue() + "px");
		main.menuActive = true;

	}
	
	private static void openSubmenu(Main main, Collection<PopupView> parents, int x, int y, List<Action> actions) {
		
		VerticalLayout content = new VerticalLayout();
		final PopupView subMenu = new PopupView("", content);
		
		content.setWidth("350px");
		
		for(Action a : actions) {
			ArrayList<PopupView> menus = new ArrayList<PopupView>(parents);
			menus.add(subMenu);
			addAction(main, menus, content, a.getCaption(), a);
		}
		
		subMenu.setHideOnMouseOut(false);
		subMenu.setPopupVisible(true);
		
		int middlePos = main.getUI().getPage().getBrowserWindowWidth() / 2;
		if(x > middlePos) {
			main.abs.addComponent(subMenu, "left: " + (x-350) + "px; top: " + y + "px");
		} else {
			main.abs.addComponent(subMenu, "left: " + (x+350) + "px; top: " + y + "px");
		}

	}
	
	private static void addAction(final Main main, final Collection<PopupView> menus, final VerticalLayout menu, String caption, final Action r) {

		Button b1 = new Button(caption);
		b1.addStyleName(ValoTheme.BUTTON_QUIET);
		b1.addStyleName(ValoTheme.BUTTON_TINY);
		b1.setWidth("100%");
		b1.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 7150528981216406326L;

			@Override
			public void buttonClick(ClickEvent event) {
				r.run();
				clearMenu(main, menus);
			}
			
		});
		if("Peruuta".equals(caption)) {
			b1.setClickShortcut(KeyCode.ESC);
		}
		menu.addComponent(b1);

	}
	
	private static void clearMenu(Main main, Collection<PopupView> menus) {
		for(PopupView menu : menus)
			main.abs.removeComponent(menu);
		main.menuActive = false;
	}
	
	private static void addCategory(final Main main, final PopupView openerButton, final VerticalLayout menu, final int x, final int y, String caption, final List<Action> actions) {

		Button b1 = new Button(caption);
		b1.addStyleName(ValoTheme.BUTTON_QUIET);
		b1.addStyleName(ValoTheme.BUTTON_TINY);
		b1.setWidth("100%");
		b1.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1044839440293207852L;

			@Override
			public void buttonClick(ClickEvent event) {
				openSubmenu(main, Collections.singleton(openerButton), x, y, actions);
			}
			
		});
		menu.addComponent(b1);

	}

	static ObjectType getFocusType(Database database, Strategiakartta map) {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);

		if(focusTypeUUID == null) return ObjectType.find(database, ObjectType.PAINOPISTE); 
		
		return database.find(focusTypeUUID);

	}
	
}
