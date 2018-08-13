package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Baseable;
import fi.semantum.strategia.widget.Database;

public abstract class ActionBase<T extends Baseable> extends Action {

	public static String HAKU = "Siirry hakun‰kym‰‰n";
	public static String NAKYMA = "Muokkaa n‰kym‰‰";
	
	protected Main main;
	protected Database database;
	protected T base;
	
	public ActionBase(String caption, Main main, T base) {
		this(caption, null, main, base);
	}

	@SuppressWarnings("unchecked")
	public ActionBase(String caption, String category, Main main, T base) {
		super(category, caption);
		this.main = main;
		this.database = main.getDatabase();
		this.base = (T)base.getBase();
	}

}
