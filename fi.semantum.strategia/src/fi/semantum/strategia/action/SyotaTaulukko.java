package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.TableInputDialog;

public class SyotaTaulukko extends ActionBase<Base> {

	public SyotaTaulukko(Main main, Base p) {
		super("Syötä tiedot taulukkona", main, p);
	}

	@Override
	public void run() {

		new TableInputDialog(main, base);

	}

}
