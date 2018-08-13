package fi.semantum.strategia.widget;

import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;

public class ResponsibilitiesDialog {

	private Main main;
	private Base base;
	private ComboBox models;
	private TextArea nameField;
	private boolean isListening = false;
	
	private ValueChangeListener comboListener;
	private String currentField = "";

	public ResponsibilitiesDialog(final Main main, Base b) {

		this.main = main;
		this.base = b;
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout buttons1 = new HorizontalLayout();
		buttons1.setSpacing(true);
		buttons1.setMargin(false);

		HorizontalLayout buttons2 = new HorizontalLayout();
		buttons2.setSpacing(true);
		buttons2.setMargin(false);

		models = new ComboBox();
		models.setWidth("100%");
		models.setNullSelectionAllowed(false);
		models.addStyleName(ValoTheme.COMBOBOX_SMALL);
		models.setCaption("Valitse kentt‰:");
		
		nameField = new TextArea();
		nameField.setSizeFull();
		nameField.setCaption("Kent‰n tiedot:");
		
		comboListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1936269579541506442L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(!isListening) return;
				currentField = (String)models.getValue();
				refresh();
			}
			
		};
		
		models.addValueChangeListener(comboListener);
		
		content.addComponent(models);
		content.setExpandRatio(models, 0.0f);

		content.addComponent(nameField);
		content.setExpandRatio(nameField, 1.0f);

		content.addComponent(buttons1);
		content.setExpandRatio(buttons1, 0.0f);

		refresh();

		final Button save = new Button("Tallenna kentt‰");
		final Button close = new Button("Poistu");
		
		buttons2.addComponent(save);
		buttons2.addComponent(close);

		final Window dialog = Dialogs.makeDialog(main, "450px", "650px", "M‰‰rit‰ vastuut", null, content, buttons2);
		
		save.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5543987996724055368L;

			@Override
			public void buttonClick(ClickEvent event) {

				Database database = main.getDatabase();

				Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
				ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, base);
				if(instance == null) {
					base.denyRelation(database, instanceRelation);
					instance = new ResponsibilityInstance(main.getDatabase());
					Property aika = Property.find(database, Property.AIKAVALI);
					aika.set(main, database, instance, Property.AIKAVALI_KAIKKI);
					base.addRelation(instanceRelation, instance);
				}
				instance.setValue(currentField, nameField.getValue());
				Updates.update(main, true);
				refresh();
				
			}
			
		});

		close.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5543987996724055368L;

			@Override
			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
			}
		});

	}

	private void refresh() {
		
		isListening = false;
		
		Database database = main.getDatabase();

//		Strategiakartta map = database.getMap(base);
//
//		Base currentLevel = map.currentLevel(database);
		
		Set<String> fields = Utils.getResponsibilityFields(database, base);
		
//		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
//		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, currentLevel);
//		if(model == null) return;
//		List<String> fields = model.getFields();
		if(fields.isEmpty()) return;
		
		Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
		ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, base);

		models.removeAllItems();

		for(String field : fields) {
			models.addItem(field);
		}
		
		if(currentField.isEmpty()) {
			currentField = fields.iterator().next();
		}
		
		models.select(currentField);
		
		if(instance != null) {
			String currentValue = instance.getValue(currentField);
			if(currentValue == null) currentValue = "";
			nameField.setValue(currentValue);
		} else {
			nameField.setValue("");
		}

		isListening = true;

	}
	
}
