package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.widget.Base;

public class UIStateAction extends ActionBase<Base> {

	public UIStateAction(String text, Main main, Base base) {
		super(text, main, base);
	}

	public UIStateAction(String text, String category, Main main, Base base) {
		super(text, category, main, base);
	}

	public void modifyState(UIState s) {
		
	}
	
	@Override
	final public void run() {
		UIState s = main.getUIState().duplicate(main);
		modifyState(s);
		main.setFragment(s, true);
	}

}
