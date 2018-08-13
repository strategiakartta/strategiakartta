package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Base;

public class PoistaRajaus extends ActionBase<Base> {

	public PoistaRajaus(Main main, Base base) {
		super("Poista rajaus", main, base);
	}

	@Override
	public void run() {
		main.removeRequiredItem(base);
	}

}
