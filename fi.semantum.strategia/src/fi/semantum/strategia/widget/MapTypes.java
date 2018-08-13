package fi.semantum.strategia.widget;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;

public class MapTypes {

	public static String canRemove(Database database, Base level) {
    	for(Strategiakartta map : Strategiakartta.enumerate(database)) {
    		ObjectType t = map.getLevelType(database);
    		if(t.equals(level)) return "Tyyppi on käytössä kartassa: " + map.getText(database);
    	}
    	return "";
	}
	
	public static void selectMapType(final Main main, final Strategiakartta map) {
        
		final Database database = main.getDatabase();
		
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        final ListSelect list = new ListSelect();
        list.setSizeFull();
        list.setNullSelectionAllowed(false);
        list.setMultiSelect(false);
        list.addStyleName(ValoTheme.TABLE_SMALL);
        list.addStyleName(ValoTheme.TABLE_COMPACT);

        final Button aseta = new Button("Aseta");
        aseta.setEnabled(false);

        final Button poista = new Button("Poista");
        poista.setEnabled(false);

        final Button hallinnoi = new Button("Muokkaa");
        
		final Base currentLevel = map.currentLevel(database);
		for(Base b : Strategiakartta.availableLevels(database)) {
			list.addItem(b.uuid);
			if(currentLevel != null && currentLevel.uuid == b.uuid) {
				list.setItemCaption(b.uuid, b.text + " (nykyinen)");
				list.select(b.uuid);
			} else {
				list.setItemCaption(b.uuid, b.text);
			}
		}
		
		ValueChangeListener listener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			boolean isCurrent(Object property) {
				if(currentLevel == null) return false;
				return currentLevel.uuid == property;
			}
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				
				Object property = event.getProperty();
				aseta.setEnabled(!isCurrent(property));
				
            	Base level = database.find((String)list.getValue());
				poista.setEnabled(canRemove(database, level).equals(""));
				
			}
	    };
		
		list.addValueChangeListener(listener);
        
        content.addComponent(list);
        content.setExpandRatio(list, 1.0f);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        buttons.addComponent(aseta);
        buttons.addComponent(hallinnoi);
        buttons.addComponent(poista);
        
        final Window dialog = Dialogs.makeDialog(main, "450px", "450px", "Valitse kartan tyyppi", "Palaa", content, buttons);
        aseta.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	Base newLevel = database.find((String)list.getValue());
            	map.setCurrentLevel(main, newLevel);
				Updates.updateJS(main, true);
            	map.prepare(main);
            	main.removeWindow(dialog);
            }
            
        });
        hallinnoi.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = -8878089006366962329L;

			public void buttonClick(ClickEvent event) {
            	Base newLevel = database.find((String)list.getValue());
            	main.removeWindow(dialog);
            	manageMapTypes(main, map, newLevel);
            }
            
        });
        poista.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	
            	Base level = database.find((String)list.getValue());
            	String error = canRemove(database, level);
            	if(!error.equals("")) {
            		return;
            	}
            	
            	level.remove(database);
				Updates.updateJS(main, true);
            	map.prepare(main);
            	list.removeItem(level.uuid);
            	
            }
            
        });

	}

	public static void manageMapTypes(final Main main, Strategiakartta returnMap, Base initialSelection) {
		ManageMapsDialog.create(main, returnMap, initialSelection);
	}
	
}
