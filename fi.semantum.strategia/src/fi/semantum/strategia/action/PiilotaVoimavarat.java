package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class PiilotaVoimavarat extends UIStateAction {

	public PiilotaVoimavarat(Main main, Base base) {
		super("Piilota voimavarat", main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showVoimavarat = false;
	}

}
