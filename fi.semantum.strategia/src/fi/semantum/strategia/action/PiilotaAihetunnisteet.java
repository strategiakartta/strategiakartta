package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class PiilotaAihetunnisteet extends UIStateAction {

	public PiilotaAihetunnisteet(Main main, Base base) {
		super("Piilota aihetunnisteet", NAKYMA, main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showTags = false;
	}

}
