package fi.semantum.strategia.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class DefineImplementors extends ActionBase<Base> {

	public DefineImplementors(Main main, Base base) {
		super("Määritä toteuttavat alatason kartat", main, base);
	}

	@Override
	public void run() {

		VerticalLayout content = new VerticalLayout();
		content.setSpacing(true);

		Label desc = new Label("Valitse listasta kaikki määritystä toteuttavat alatason kartat.");
		desc.addStyleName(ValoTheme.LABEL_TINY);

		content.addComponent(desc);
		content.setComponentAlignment(desc, Alignment.MIDDLE_LEFT);
		content.setExpandRatio(desc, 0.0f);

		final OptionGroup table = new OptionGroup();
		table.setNullSelectionAllowed(true);
		table.setMultiSelect(true);
		table.setCaption("Alatason kartat");

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
							table.setItemCaption(o, baseCaption + " (LISÄTÄÄN)");
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

		Button copy = new Button("Määritä");

		buttons.addComponent(copy);

		final Window dialog = Dialogs.makeDialog(main, "Määritä toteuttajat", "Peruuta", content, buttons);
		
		copy.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -2067411013142475955L;

			public void buttonClick(ClickEvent event) {

				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;

				Set<Base> denied = new HashSet<Base>();

				for(Object o : table.getItemIds()) {
					Strategiakartta child = database.find((String)o);
					if(selected.contains(o)) {
						if(!currentlyImplemented.contains(o)) {
							Tavoite.createCopy(main, child, base);
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

				main.removeWindow(dialog);

				if(!denied.isEmpty()) {

					VerticalLayout la = new VerticalLayout();
					Label l = new Label("Seuraavat määritykset ovat käytössä, eikä niitä voida poistaa:");
					l.addStyleName(ValoTheme.LABEL_H3);
					la.addComponent(l);
					for(Base b : denied) {
						Strategiakartta map = database.getMap(b);
						Label l2 = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + b.getId(database) + " - " + b.getText(database) + " (" + map.getId(database) + ")");
						l2.setContentMode(ContentMode.HTML);
						la.addComponent(l2);
					}

					Dialogs.errorDialog(main, "Poistaminen ei onnistu", la);

				}

			}

		});


	}

}
