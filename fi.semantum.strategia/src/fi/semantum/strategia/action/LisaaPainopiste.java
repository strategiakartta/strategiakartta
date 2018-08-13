package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class LisaaPainopiste extends ActionBase<Base> {
	
	private String desc;
	
	public LisaaPainopiste(String desc, Main main, Base base) {
		super("Lis‰‰ " + desc.toLowerCase(), main, base);
		this.desc = desc;
	}

	@Override
	public void run() {

		final Strategiakartta map = database.getMap(base);

		Tavoite goal = (Tavoite)base;
		
		Painopiste pp = Painopiste.create(main, map, goal, "Uusi " + desc);
		Updates.updateJS(main, true);
		
		try {
			Strategiakartta submap = goal.getPossibleImplementationMap(database);
			if(submap != null) {
				Tavoite.createCopy(main, submap, pp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
