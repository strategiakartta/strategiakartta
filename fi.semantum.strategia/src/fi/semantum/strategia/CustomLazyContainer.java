package fi.semantum.strategia;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

import fi.semantum.strategia.Utils.TagCombo;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Tag;

class CustomLazyContainer extends IndexedContainer {
	
	private static final long serialVersionUID = -4520139213434183947L;
	
	private Database database;
	private String filterString;
	private TagCombo combo;
	
	final private List<Tag> tags;

	public CustomLazyContainer(Database database, TagCombo combo, List<Tag> tags) {
		this.database = database;
		this.tags = tags;
		this.combo = combo;
		addContainerProperty("id", String.class, "");
		doFilter();
	}
	
	public String getFilterString() {
		return filterString;
	}

	@Override
	public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
		if (filter == null)
		{
			removeAllItems();
			filterString = null;
			return;
		}

		removeAllItems();

		if (filter instanceof SimpleStringFilter)
		{
			String newFilterString = combo.customFilterString;

			if (newFilterString == null)
				return;

			if (newFilterString.equals(filterString))
				return;

			filterString = newFilterString;

			if (filterString.length() < 1)
				return;

			doFilter();
			super.addContainerFilter(filter);
		}
	}

	@SuppressWarnings("unchecked")
	private void doFilter() {
		for(Tag t : tags) {
			Item item = addItem(t.getId(database));
			item.getItemProperty("id").setValue(t.getId(database));
		}
		if(filterString != null) {
			Item item = addItem(filterString);
			if(item != null)
				item.getItemProperty("id").setValue(filterString);
		}
	}

}