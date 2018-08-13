package fi.semantum.strategia.action;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class PoistaTavoite extends ActionBase<Base> {

	private String desc;
	
	public PoistaTavoite(String desc, Main main, Base base) {
		super("Poista", main, base);
		this.desc = desc;
	}

	@Override
	public void run() {
		
		final Strategiakartta map = database.getMap(base);

		Tavoite t = (Tavoite)base;
		if(t.painopisteet.length > 0) {

			VerticalLayout la = new VerticalLayout();
			Label l = new Label("Vain tyhj‰n m‰‰rityksen voi poistaa. Poista ensin jokainen " + desc.toLowerCase() +  " ja yrit‰ sen j‰lkeen uudestaan.");
			l.addStyleName(ValoTheme.LABEL_H3);
			la.addComponent(l);
			Dialogs.errorDialog(main, "Poisto estetty", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);

		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + desc + " ?", "Poista", "Peruuta", new Runnable() {

			@Override
			public void run() {
				
				Tavoite t = (Tavoite)base;
				t.remove(database);
				map.fixRows();
				Updates.updateJS(main, true);
			}
			
		});


	}

}
