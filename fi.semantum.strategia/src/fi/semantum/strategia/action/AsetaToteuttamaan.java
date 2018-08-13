package fi.semantum.strategia.action;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Linkki;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Pair;
import fi.semantum.strategia.widget.Relation;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class AsetaToteuttamaan extends ActionBase<Base> {

	public AsetaToteuttamaan(Main main, Base base) {
		super("Aseta toteuttamaan", main, base);
	}

	@Override
	public void run() {
		
		Strategiakartta map = database.getMap(base);
		selectGoalType(main, map, new Consumer<String>() {
			
			@Override
			public void accept(String uuid) {
				if(uuid != null) {
					Base painopiste = database.find(uuid);
					base.addRelation(Relation.find(database, Relation.IMPLEMENTS), painopiste);
				}
				Updates.updateJS(main, true);
			}
			
		});


	}

	public static void selectGoalType(final Main main, Strategiakartta map, final Consumer<String> callback) {

		final Database database = main.getDatabase();

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(true);

        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        Set<Base> alreadyImplemented = new HashSet<Base>();
    	Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
        for(Tavoite t : map.tavoitteet) {
        	Pair p = implementsRelation.getPossibleRelation(t);
        	if(p != null) {
        		Base targetPP = database.find(p.second);
        		alreadyImplemented.add(targetPP);
        	}
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
        Strategiakartta parent_ = map.getPossibleParent(database); 

        final Window dialog = Dialogs.makeDialog(main, "Valitse toteutettava aihe", "Peruuta", content, buttons);

        if(parent_ != null) {
        
	        final ListSelect table = new ListSelect();
	        table.setSizeFull();
	        table.setNullSelectionAllowed(false);
	        table.setMultiSelect(false);
	        table.setCaption("Valitse listasta:");
	        for(Linkki l : map.parents) {
	            Strategiakartta parent = database.find(l.uuid);
	            for(Tavoite t : parent.tavoitteet) {
	            	for(Painopiste p : t.painopisteet) {
	            		if(alreadyImplemented.contains(p)) continue;
	            		table.addItem(p.uuid);
	            		table.setItemCaption(p.uuid, p.getId(database) + ": " + p.getText(database));
	            	}
	            }
	        }
	        
	        final Button copy = new Button("M‰‰rit‰ toteuttajaksi", new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1L;
	
				// inline click-listener
	            public void buttonClick(ClickEvent event) {
	            	Object selected = table.getValue();
	            	if(selected == null) return;
	            	main.removeWindow(dialog);
	            	callback.accept((String)selected);
	            }
	            
	        });
	        
	        ValueChangeListener l = new ValueChangeListener() {
				
				private static final long serialVersionUID = 192004471077387400L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
	
					Object selection = table.getValue();
					if(selection == null) {
						copy.setEnabled(false);
					} else {
						copy.setEnabled(true);
					}
					
				}
				
			};
	        
			l.valueChange(null);
			
	        table.addValueChangeListener(l);
	        
	        content.addComponent(table);
	        content.setExpandRatio(table, 1.0f);

	        copy.setEnabled(false);
	        
	        buttons.addComponent(copy);

        }

	}
	
}
