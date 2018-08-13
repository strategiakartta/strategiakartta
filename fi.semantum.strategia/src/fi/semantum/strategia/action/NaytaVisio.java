package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Strategiakartta;

public class NaytaVisio extends ActionBase<Strategiakartta> {

	public NaytaVisio(Main main, Strategiakartta base) {
		super("Näytä visio", NAKYMA, main, base);
	}

	@Override
	final public void run() {
		base.showVision = true;
		Updates.updateJS(main, true);
	}

}
