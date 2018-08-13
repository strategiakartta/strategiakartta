package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class VaihdaToteutusmittareihin extends UIStateAction {

	public VaihdaToteutusmittareihin(Main main, Base base) {
		super("Vaihda toteutusmittareihin", main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.useImplementationMeters = true;
	}

}
