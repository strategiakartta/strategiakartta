package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Strategiakartta;

public class PaivitaNakyma extends ActionBase<Strategiakartta> {

	public PaivitaNakyma(Main main, Strategiakartta base) {
		super("P�ivit� n�kym�", main, base);
	}

	@Override
	public void run() {

		base.generate(main);
		Updates.updateJS(main, false);
		
	}

}
