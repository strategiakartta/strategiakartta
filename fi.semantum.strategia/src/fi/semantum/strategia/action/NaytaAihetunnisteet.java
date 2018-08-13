package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class NaytaAihetunnisteet extends UIStateAction {

	public NaytaAihetunnisteet(Main main, Base base) {
		super("Näytä aihetunnisteet", main, base);
	}

	@Override
	public void modifyState(UIState s) {
		s.showTags = true;
	}

}
