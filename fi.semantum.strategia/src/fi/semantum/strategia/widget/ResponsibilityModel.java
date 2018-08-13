package fi.semantum.strategia.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResponsibilityModel extends Base {

	private static final long serialVersionUID = 7674413338143454559L;
	
	private List<String> fields = new ArrayList<String>();
	
	public ResponsibilityModel(Database database, String text) {
		super(UUID.randomUUID().toString(), "", text);
		database.register(this);
		fields.add("Vastuuhenkilö");
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static List<ResponsibilityModel> enumerate(Database database) {

		ArrayList<ResponsibilityModel> result = new ArrayList<ResponsibilityModel>();
		for (Base b : database.objects.values()) {
			if (b instanceof ResponsibilityModel)
				result.add((ResponsibilityModel) b);
		}
		return result;

	}
	
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	public List<String> getFields() {
		return fields;
	}
	
}
