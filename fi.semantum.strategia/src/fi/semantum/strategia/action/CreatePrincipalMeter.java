package fi.semantum.strategia.action;

import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Datatype;
import fi.semantum.strategia.widget.EnumerationDatatype;
import fi.semantum.strategia.widget.Indicator;
import fi.semantum.strategia.widget.Meter;

public class CreatePrincipalMeter extends ActionBase<Base> {

	public CreatePrincipalMeter(Main main, Base base) {
		super("Aseta arvio", main, base);
	}

	private Datatype findType() {
        List<Datatype> types = Datatype.enumerate(database);
        for(Datatype dt : types) {
        	if(dt instanceof EnumerationDatatype) {
        		String id = dt.getId(database);
        		if("Toteuma".equals(id)) return dt;
        	}
        }
        return null;
	}
	
	public static void perform(Main main, Base base) {
		new CreatePrincipalMeter(main, base).run(); 
	}
	
	@Override
	public void run() {

		Datatype dt = findType();
		if(dt == null) return;
		
		Indicator ind = Indicator.create(database, "", dt);
		ind.update(main, base, dt.getDefaultValue(), false, "", "Alkuarvo");
		ind.update(main, base, dt.getDefaultForecast(), true, "", "Alkuarvo");
		Meter m = Meter.addIndicatorMeter(main, base, ind, Utils.getValidity(database, base));
		m.isPrincipal = true;
		
		Updates.updateJS(main, true);
		
	}

}
