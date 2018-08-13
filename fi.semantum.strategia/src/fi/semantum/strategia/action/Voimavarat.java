package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Wiki;
import fi.semantum.strategia.widget.Base;

public class Voimavarat extends ActionBase<Base> {

	public Voimavarat(Main main, Base base) {
		super("Voimavarat", main, base);
	}

	@Override
	public void run() {

		Wiki.openWiki(main, base);

	}

}
