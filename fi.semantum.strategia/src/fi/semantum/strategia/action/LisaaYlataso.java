package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Strategiakartta;

public class LisaaYlataso extends ActionBase<Strategiakartta> {

	public LisaaYlataso(Main main, Strategiakartta base) {
		super("Lis�� yl�tason kartta", main, base);
	}

	@Override
	public void run() {
		Utils.insertRootMap(main, base);
	}

}
