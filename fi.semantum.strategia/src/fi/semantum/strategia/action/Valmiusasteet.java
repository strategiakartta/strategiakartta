package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.MeterFilter;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class Valmiusasteet extends ActionBase<Base> {

	public Valmiusasteet(Main main, Base base) {
		super("Valmiusasteet", HAKU, main, base);
	}

	@Override
	public void run() {

		Strategiakartta map = database.getMap(base);
		main.getUIState().currentPosition = map;

		main.getUIState().setCurrentFilter(new MeterFilter(main, base));
		if(base instanceof Tavoite) {
			main.getUIState().level = 4;
			main.less.setEnabled(true);
		} else {
			main.getUIState().level = 2;
			main.less.setEnabled(false);
		}
		
		main.setCurrentItem(base, main.getUIState().currentPosition);
		main.switchToBrowser();
	}

}
