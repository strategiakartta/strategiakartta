package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Tavoite;

public class LisaaToteutuskartta extends ActionBase<Tavoite> {

	public LisaaToteutuskartta(Main main, Tavoite goal) {
		super("Lis�� toteuttava alakartta", main, goal);
	}

	@Override
	public void run() {

		Utils.addImplementationMap(main, base);

	}

}
