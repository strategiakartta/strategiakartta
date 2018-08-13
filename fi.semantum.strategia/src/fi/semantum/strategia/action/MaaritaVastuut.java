package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.ResponsibilitiesDialog;

public class MaaritaVastuut extends ActionBase<Base> {

	public MaaritaVastuut(Main main, Base p) {
		super("Määritä vastuut", main, p);
	}

	@Override
	public void run() {
		new ResponsibilitiesDialog(main, base);
	}

}
