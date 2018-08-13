package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class PiilotaMittarit extends UIStateAction {

	public PiilotaMittarit(Main main, Base base) {
		super("Piilota mittarit", NAKYMA, main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showMeters = false;
	}

}
