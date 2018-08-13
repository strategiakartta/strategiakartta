package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Base;

public class Ominaisuudet extends ActionBase<Base> {

	public Ominaisuudet(Main main, Base base) {
		super("Ominaisuudet", main, base);
	}

	@Override
	public void run() {

		main.setCurrentItem(base, main.getUIState().currentPosition);
		main.switchToProperties();

	}

}
