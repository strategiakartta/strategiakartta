package fi.semantum.strategia.action;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Strategiakartta;

public class PoistaKartta extends ActionBase<Strategiakartta> {

	public PoistaKartta(Main main, Strategiakartta base) {
		super("Poista", main, base);
	}

	@Override
	public void run() {

		if(base.tavoitteet.length > 0 && base.generators.isEmpty()) {

			VerticalLayout la = new VerticalLayout();
			Label l = new Label("Vain tyhjän kartan voi poistaa. Poista ensin jokainen " + base.tavoiteDescription.toLowerCase() +  " ja yritä sen jälkeen uudestaan.");
			l.addStyleName(ValoTheme.LABEL_H3);
			la.addComponent(l);
			Dialogs.errorDialog(main, "Poisto estetty", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);
		
		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa kartan " + desc + " ?", "Poista", "Peruuta", new Runnable() {

			@Override
			public void run() {
				
				Strategiakartta parent = base.getPossibleParent(database);
				database.remove(base);
				if(parent != null)
					main.getUIState().current = parent;
				
				Updates.updateJS(main, true);
			}
			
		});

	}

}
