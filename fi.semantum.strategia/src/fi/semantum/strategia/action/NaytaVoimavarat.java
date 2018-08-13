package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class NaytaVoimavarat extends UIStateAction {

	public NaytaVoimavarat(Main main, Base base) {
		super("Näytä voimavarat", main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showVoimavarat = true;
	}

}
