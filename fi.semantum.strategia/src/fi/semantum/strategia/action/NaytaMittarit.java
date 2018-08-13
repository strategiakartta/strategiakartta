package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class NaytaMittarit extends UIStateAction {

	public NaytaMittarit(Main main, Base base) {
		super("N�yt� mittarit", NAKYMA, main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showMeters = true;
	}

}
