package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Strategiakartta;

public class LisaaNakyma extends ActionBase<Strategiakartta> {

	public LisaaNakyma(Main main, Strategiakartta base) {
		super("Lisää näkymä", main, base);
	}

	@Override
	public void run() {

		Utils.addView(main, base);

	}

}
