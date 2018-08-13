package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Wiki;
import fi.semantum.strategia.widget.Base;

public class TaustaAsiakirja extends ActionBase<Base> {

	public TaustaAsiakirja(Main main, Base base) {
		super("Tausta-asiakirja", main, base);
	}

	@Override
	public void run() {

		Wiki.openWiki(main, base);

	}

}
