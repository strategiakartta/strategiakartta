package fi.semantum.strategia.action;

import java.util.Collection;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Relation;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class PoistaPainopiste extends ActionBase<Base> {

	private Base container;
	
	public PoistaPainopiste(Main main, Base base, Base container) {
		super("Poista", main, base);
		this.container = container;
	}

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
			Dialogs.errorDialog(main, "M‰‰ritys on k‰ytˆss‰, eik‰ sit‰ voida poistaa", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);

		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + desc + " ?", "Poista", "Peruuta", new Runnable() {

			@Override
			public void run() {
				
				Painopiste p = (Painopiste)base;
				Strategiakartta map = database.getMap(base);
				int refs = 0;
				if(map != null) {
					for(Tavoite t : map.tavoitteet) {
						for(Painopiste p2 : t.painopisteet) {
							if(p2.equals(base)) refs++;
						}
					}
				}
				
				Tavoite t = (Tavoite)container;
				if(refs == 1) p.remove(database);
				else t.removePainopiste(database, p);
				if(map != null)
					map.fixRows();
				Updates.updateJS(main, true);
				
			}
			
		});


	}

}
