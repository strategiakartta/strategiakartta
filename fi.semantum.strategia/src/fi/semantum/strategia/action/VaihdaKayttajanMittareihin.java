package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class VaihdaKayttajanMittareihin extends UIStateAction {

	public VaihdaKayttajanMittareihin(Main main, Base base) {
		super("Vaihda k�ytt�j�n mittareihin", main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.useImplementationMeters = false;
	}

}
