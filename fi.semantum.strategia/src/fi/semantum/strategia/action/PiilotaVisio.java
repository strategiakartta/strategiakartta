package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Strategiakartta;

public class PiilotaVisio extends ActionBase<Strategiakartta> {

	public PiilotaVisio(Main main, Strategiakartta base) {
		super("Piilota visio", NAKYMA, main, base);
	}

	@Override
	final public void run() {
		base.showVision = false;
		Updates.updateJS(main, true);
	}

}
