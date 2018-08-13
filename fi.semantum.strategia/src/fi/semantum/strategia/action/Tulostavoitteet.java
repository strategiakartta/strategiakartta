package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.TulostavoiteFilter;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Strategiakartta;

public class Tulostavoitteet extends ActionBase<Base> {

	public Tulostavoitteet(Main main, Base base) {
		super("Tulostavoitteet", HAKU, main, base);
	}

	@Override
	public void run() {

		Strategiakartta map = database.getMap(base);
		main.getUIState().currentPosition = map;

		main.getUIState().setCurrentFilter(new TulostavoiteFilter(main, base));
		main.getUIState().level = 10;
		
		main.setCurrentItem(base, main.getUIState().currentPosition);
		main.switchToBrowser();
		
	}

}
