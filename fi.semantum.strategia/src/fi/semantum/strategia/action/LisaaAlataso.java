package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Strategiakartta;

public class LisaaAlataso extends ActionBase<Strategiakartta> {

	public LisaaAlataso(Main main, Strategiakartta base) {
		super("Lisää alatason kartta", main, base);
	}

	@Override
	public void run() {

		Utils.addMap(main, base);

	}

}
