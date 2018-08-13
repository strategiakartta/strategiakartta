package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Strategiakartta;

public class RajaaAlle extends ActionBase<Base> {

	public RajaaAlle(Main main, Base base) {
		super("Rajaa tarkastelu tämän alle", main, base);
	}

	@Override
	public void run() {
		if(base instanceof Strategiakartta)
			main.setCurrentItem(main.getUIState().currentItem, (Strategiakartta)base);
		main.addRequiredItem(base);
	}

}
