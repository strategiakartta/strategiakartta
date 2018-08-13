package fi.semantum.strategia.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.semantum.strategia.DialogCallback;
import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.UIState;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;

public class ValitseAihetunnisteet extends ActionBase<Strategiakartta> {

	public ValitseAihetunnisteet(Main main, String caption, Strategiakartta base) {
		super(caption, NAKYMA, main, base);
	}

	@Override
	public void run() {
		selectMonitorTagsDialog(main, base, new DialogCallback<Collection<Tag>>() {

			@Override
			public void finished(Collection<Tag> result) {

				UIState s = main.getUIState().duplicate(main);
				s.showTags = true;
				s.shownTags = new ArrayList<Tag>(result);
				main.setFragment(s, true);

				Updates.update(main, true);

			}

			@Override
			public void canceled() {
			}

		});	
	}

	void selectMonitorTagsDialog(final Main main, Strategiakartta map, final DialogCallback<Collection<Tag>> callback) {

		final Database database = main.getDatabase();
		
		HashSet<Tag> monitorTagsSet = new HashSet<Tag>();
		for(Base b : map.getOwners(database)) {
			monitorTagsSet.addAll(b.getMonitorTags(database));
		}
		
		ArrayList<Tag> monitorTags = new ArrayList<Tag>(monitorTagsSet);
		Collections.sort(monitorTags, Base.tagComparator);
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

        final OptionGroup table = new OptionGroup();
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        
        for(Tag t : monitorTags) {
        	table.addItem(t);
        	table.setItemCaption(t, t.getId(database));
        }
        
        content.addComponent(table);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
		Button ok = new Button("Valitse");
        buttons.addComponent(ok);
        buttons.setExpandRatio(ok, 0.0f);

        final Window dialog = Dialogs.makeDialog(main, "300px", "600px", "Valitse näytettävät", "Peruuta", content, buttons);
        
        ok.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1657687721482107951L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
            	main.removeWindow(dialog);
            	callback.finished((Collection<Tag>)table.getValue());
            }
            
        });

	}
	
}
