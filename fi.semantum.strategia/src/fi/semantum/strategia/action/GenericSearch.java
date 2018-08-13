package fi.semantum.strategia.action;

import java.util.ArrayList;
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.GenericImplementationFilter;
import fi.semantum.strategia.widget.Base;

public class GenericSearch extends ActionBase<Base> {

	public GenericSearch(Main main, Base base) {
		super("Yleinen haku", HAKU, main, base);
	}

	@Override
	public void run() {

		List<String> cols = new ArrayList<String>();
		cols.add("Strateginen tavoite");
		cols.add("Painopiste");
		
		main.getUIState().level = 3;
		main.getUIState().setCurrentFilter(new GenericImplementationFilter(main, base, cols));
		
		main.setCurrentItem(base, database.getMap(base));
		main.switchToBrowser();

	}

}
