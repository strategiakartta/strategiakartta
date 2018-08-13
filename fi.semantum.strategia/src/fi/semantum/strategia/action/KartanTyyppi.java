package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.MapTypes;
import fi.semantum.strategia.widget.Strategiakartta;

public class KartanTyyppi extends ActionBase<Strategiakartta> {

	public KartanTyyppi(Main main, Strategiakartta base) {
		super("Kartan tyyppi", main, base);
	}

	@Override
	public void run() {
		MapTypes.selectMapType(main, base);
	}

}
