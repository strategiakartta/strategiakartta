package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Strategiakartta;

public class AvaaStrategiakartassa extends ActionBase<Base> {

	public AvaaStrategiakartassa(Main main, Base base) {
		super("Avaa strategiakartassa", main, base);
	}

	@Override
	public void run() {

		Strategiakartta map = database.getMap(base);
		
		main.getUIState().current = map;
		Updates.updateJS(main, false);
		main.switchToMap();
		
	}

}
