package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.TulostavoiteToimenpideFilter;
import fi.semantum.strategia.widget.Base;

public class StrategianToteutus extends ActionBase<Base> {

	public StrategianToteutus(Main main, Base base) {
		super("Strategian toteutus", HAKU, main, base);
	}

	@Override
	public void run() {

		main.getUIState().level = 3;
		main.getUIState().setCurrentFilter(new TulostavoiteToimenpideFilter(main, base));
		
		main.setCurrentItem(base, database.getMap(base));
		main.switchToBrowser();

	}

}
