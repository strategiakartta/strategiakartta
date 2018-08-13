package fi.semantum.strategia.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponsibilityInstance extends Base {

	private static final long serialVersionUID = 7674413338143454559L;
	
	private Map<String,String> values = new HashMap<String, String>();
	
	public ResponsibilityInstance(Database database) {
		super(UUID.randomUUID().toString(), "", "");
		database.register(this);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static List<ResponsibilityInstance> enumerate(Database database) {

		ArrayList<ResponsibilityInstance> result = new ArrayList<ResponsibilityInstance>();
		for (Base b : database.objects.values()) {
			if (b instanceof ResponsibilityInstance)
				result.add((ResponsibilityInstance) b);
		}
		return result;

	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	public void setValue(String key, String value) {
		values.put(key, value);
	}
	
}
