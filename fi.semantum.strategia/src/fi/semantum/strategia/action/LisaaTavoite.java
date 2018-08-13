package fi.semantum.strategia.action;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

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

public class LisaaTavoite extends ActionBase<Strategiakartta> {
	
	public LisaaTavoite(Main main, Strategiakartta base) {
		super("Lis‰‰ " + base.tavoiteDescription.toLowerCase(), main, base);
	}

	@Override
	public void run() {

		selectGoalType(main, base, new Consumer<String>() {
			
			@Override
			public void accept(String uuid) {
				if(uuid != null) {
					Base copy = database.find(uuid);
					Tavoite.createCopy(main, main.uiState.current, copy);
				} else {
					Tavoite.create(database, main.uiState.current, "Oma " + base.tavoiteDescription.toLowerCase());
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

		Button ok = new Button("Lis‰‰ oma " + map.tavoiteDescription.toLowerCase());
        
        hl1.addComponent(ok);
        hl1.setExpandRatio(ok, 0.0f);

		Label uusiOma = new Label("Omat m‰‰ritykset eiv‰t perustu ylempien tasojen m‰‰rityksiin");
		uusiOma.addStyleName(ValoTheme.LABEL_TINY);
		
        hl1.addComponent(uusiOma);
        hl1.setComponentAlignment(uusiOma, Alignment.MIDDLE_LEFT);
        hl1.setExpandRatio(uusiOma, 1.0f);

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

        final Window dialog = Dialogs.makeDialog(main, "M‰‰rit‰ uusi " + map.tavoiteDescription.toLowerCase(), "Peruuta", content, buttons);

        if(parent_ != null) {
        
        	Base implementedGoal = map.getImplemented(database);
        	
        	final ListSelect table = new ListSelect();
	        table.setSizeFull();
	        table.setNullSelectionAllowed(false);
	        table.setMultiSelect(false);
	        table.setCaption("Lis‰‰ " + map.tavoiteDescription.toLowerCase() + " perustuen ylemm‰n tason (" + parent_.getId(database) + ") m‰‰ritykseen. Valitse m‰‰ritys listasta:");
	        for(Linkki l : map.parents) {
	            Strategiakartta parent = database.find(l.uuid);
	            for(Tavoite t : parent.tavoitteet) {
	            	
	            	if(implementedGoal != null) {
	            		if(!implementedGoal.equals(t))
	            			continue;
	            	}
	            	
	            	for(Painopiste p : t.painopisteet) {
	            		if(alreadyImplemented.contains(p)) continue;
	            		table.addItem(p.uuid);
	            		String desc = p.getText(database);
	            		String id = p.getId(database);
	            		if(desc.isEmpty()) desc = id;
	            		else if(!id.isEmpty()) desc = id + " : " + desc;
	            		table.setItemCaption(p.uuid, desc);
	            	}
	            }
	        }
	        
	       final Button copy = new Button("Lis‰‰ toteuttava " + map.tavoiteDescription.toLowerCase());
	        
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

            copy.addClickListener(new Button.ClickListener() {
            	
				private static final long serialVersionUID = 7193540167093776902L;

				// inline click-listener
                public void buttonClick(ClickEvent event) {
                	Object selected = table.getValue();
                	if(selected == null) return;
                	main.removeWindow(dialog);
                	callback.accept((String)selected);
                }
                
            });
	        
        }

        ok.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 6054297133724400131L;

			// inline click-listener
            public void buttonClick(ClickEvent event) {
            	main.removeWindow(dialog);
            	callback.accept(null);
            }
            
        });

	}

}
