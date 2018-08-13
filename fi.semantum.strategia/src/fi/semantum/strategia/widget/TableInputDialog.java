package fi.semantum.strategia.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Main.TimeInterval;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.action.LisaaTavoite;
import fi.semantum.strategia.widget.Strategiakartta.CharacterInfo;

public class TableInputDialog {

	private Main main;
	private Base base;
	private VerticalLayout content;
	private Panel scroll;
	private boolean isListening = false;
	
	private List<TableInputEntry> data = new ArrayList<TableInputEntry>();
	
//	private String tavoiteDescription;
//	private String painopisteDescription;
	
	private ValueChangeListener comboListener;
	private String currentField = "";
	
	static class TableInputEntry {
		
		public TableInputEntry(TableInputEntry parent, CharacterInfo characterInfo, Base base, String text, String time) {
			this.parent = parent;
			this.characterInfo = characterInfo;
			this.base = base;
			this.text = text;
			this.time = time;
		}
		
		// This is always a goal instance
		public TableInputEntry parent = null;
		// This is the level type
		public CharacterInfo characterInfo;
		// This is normally a goal but leafs are focuses
		public Base base = null;
		public String text = "";
		public String time = "-";
		public boolean removed = false;
		public List<TableInputEntry> painopiste = new ArrayList<TableInputEntry>();
	}
	
	

	public TableInputDialog(final Main main, Base b) {

		scroll = new Panel();
		scroll.setSizeFull();

		content = new VerticalLayout();
		content.setWidth("100%");
		scroll.setContent(content);

		HorizontalLayout buttons1 = new HorizontalLayout();
		buttons1.setSpacing(true);
		buttons1.setMargin(false);

		HorizontalLayout buttons2 = new HorizontalLayout();
		buttons2.setSpacing(true);
		buttons2.setMargin(false);

		this.main = main;
		this.base = b;

		readData();
		populate();

		final Button save = new Button("Tallenna");
		final Button close = new Button("Poistu");
		
		buttons2.addComponent(save);
		buttons2.addComponent(close);
		
		final Window dialog = Dialogs.makeDialog(main, main.dialogWidth(0.8), main.dialogHeight(0.8), "Syötä tiedot taulukkona", null, scroll, buttons2);
		
		save.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5543987996724055368L;

			public void apply(TableInputEntry entry) {
				Database database = main.getDatabase();
				if(entry.base != null) {
					if(entry.removed) {
						if(entry.base instanceof Tavoite) {
							Base imp = entry.base.getPossibleImplemented(database);
							if(imp != null) {
								if(imp instanceof Painopiste) {
									Tavoite t2 = ((Painopiste)imp).getGoal(database);
									if(t2.hasImplementationSubmap(database)) {
										imp.removeRecursive(database);
										return;
									}
								}
							}
						}
						entry.base.removeRecursive(database);
						return;
					}
					entry.base.modifyText(main, entry.text);
				} else {
					// This was already removed => apply nothing
					if(entry.removed)
						return;
					if(entry.parent == null) {
						// In this case use the root map
						Tavoite t = Tavoite.create(database, base.getMap(database), entry.text);
						try {
							t.ensureImplementationMap(main);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						entry.base = t;
					} else {
						Tavoite t = (Tavoite)entry.parent.base;
						Painopiste p = Painopiste.create(main, t.getMap(database), t, entry.text);
						try {
							t.ensureImplementationMap(main);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						entry.base = p.getPossibleImplementationGoal(database);
						if(entry.base == null) entry.base = p;
					}
				}
				final Property aika = Property.find(database, Property.AIKAVALI);
				aika.set(main, entry.base, entry.time);
				for(TableInputEntry c : entry.painopiste)
					apply(c);
			}
			
			@Override
			public void buttonClick(ClickEvent event) {

				for(int i=0;i<data.size();i++) {
					TableInputEntry entry = data.get(i);
					apply(entry);
				}
				
				Updates.update(main, true);
				populate();
				
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
	
	private void readData() {

		Database database = main.getDatabase();

		Strategiakartta map = database.getMap(base);
		
		data.clear();
		
		for(int i=0;i<map.tavoitteet.length;i++) {
			TableInputEntry entry = readRec(null, map.tavoitteet[i]);
			if(entry != null)
				data.add(entry);
		}
		
	}

	private TableInputEntry readRec(TableInputEntry parent, Tavoite t) {
		
		Database database = main.getDatabase();
		
		final Property aika = Property.find(database, Property.AIKAVALI);
		String time = aika.getPropertyValue(t);
		
		if(!main.acceptTime(time)) return null;

		CharacterInfo ci = t.getMap(database).getCharacterInfo(database);
		
		TableInputEntry entry = new TableInputEntry(parent, ci, t, t.getText(database), time);
		
		for(int j=0;j<t.painopisteet.length;j++) {
			Painopiste p = t.painopisteet[j];
			Tavoite t2 = p.getPossibleImplementationGoal(database);
			if(t2 != null) {
				if(t.hasImplementationSubmap(database)) {
					TableInputEntry childEntry = readRec(entry, t2); 
					if(childEntry != null) entry.painopiste.add(childEntry);
				} else {
					if(main.acceptTime(aika.getPropertyValue(p)))
						entry.painopiste.add(new TableInputEntry(entry, null, p, p.getText(database), entry.time));
				}
			} else {
				if(main.acceptTime(aika.getPropertyValue(p)))
					entry.painopiste.add(new TableInputEntry(entry, null, p, p.getText(database), entry.time));
			}
		}

		
		return entry;
		
	}
	
	private void createEditor(final VerticalLayout vl, final TableInputEntry entry, int level, int i, String desc, boolean readOnly) {
		
		final HorizontalLayout hlz = new HorizontalLayout();
		hlz.setWidth("100%");
		vl.addComponent(hlz);

		final Label l = new Label(desc);
		l.setWidth("250px");
		hlz.addComponent(l);
		
		final TextArea tf = new TextArea();
		tf.setValue(entry.text);
		tf.setWidth("100%");
		tf.setHeight("60px");
		//tf.addStyleName("inputTableLevel" + level);
		tf.addStyleName("tiny");
		if(readOnly) {
			tf.setReadOnly(true);
		} else {
			tf.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					entry.text = tf.getValue();
				}

			});
		}
		vl.addComponent(tf);

		final ComboBox times = new ComboBox();
		times.setWidth("200px");
		times.addItem(Property.AIKAVALI_KAIKKI);
		TimeConfiguration tc = TimeConfiguration.getInstance(main.getDatabase());
		TimeInterval ti = TimeInterval.parse(tc.getRange());
		for(int year : ti.years()) {
			times.addItem(Integer.toString(year));
		}
		times.setInvalidAllowed(false);
		times.setNullSelectionAllowed(false);
		times.addStyleName("tiny");
		times.select(entry.time);

		if(readOnly) {
			times.setReadOnly(true);
		} else {
			times.addValueChangeListener(new ValueChangeListener() {
				private static final long serialVersionUID = -2224460993765643089L;
				@Override
				public void valueChange(ValueChangeEvent event) {
					String value = (String)times.getValue();
					entry.time = value;
				}
			});
		}
		
		hlz.addComponent(times);
		hlz.setExpandRatio(times, 1.0f);

		if(!readOnly) {
			
			Button b = new Button("Poista");
			b.addStyleName(ValoTheme.BUTTON_SMALL);
			b.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 6267511776781878309L;

				@Override
				public void buttonClick(ClickEvent event) {
					vl.removeAllComponents();
					vl.setVisible(false);
					entry.removed = true;
				}
			});
			
			hlz.addComponent(b);
			hlz.setExpandRatio(b, 0.0f);
			
		}

	}
	
	private void level(VerticalLayout layout, final TableInputEntry entry, final int level, int i) {
		
		final Database database = main.getDatabase();

		final VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");

		layout.addComponent(vl);
		
		String description = entry.characterInfo != null ? entry.characterInfo.goalDescription : entry.parent.characterInfo.focusDescription;
		
		boolean readOnly = false;
		if(entry.base != null) {
			if(!main.canWrite(entry.base))
				readOnly = true;
		}

		if(!main.canWriteWithSelectedTime())
			readOnly = true;

		createEditor(vl, entry, level, i, description, readOnly);
		
		final HorizontalLayout hl = new HorizontalLayout();
		vl.addComponent(hl);
		hl.setWidth("100%");
		
		final VerticalLayout vl2x = new VerticalLayout();
		vl2x.setWidth("30px");
		hl.addComponent(vl2x);
		hl.setExpandRatio(vl2x, 0);
		
		final VerticalLayout vl2 = new VerticalLayout();
		vl2.setWidth("100%");
		hl.addComponent(vl2);
		hl.setExpandRatio(vl2, 1);

		if(entry.characterInfo != null) {

			vl2.addStyleName("inputTableGroup");

			final VerticalLayout vl3 = new VerticalLayout();
			vl3.setWidth("100%");
			vl2.addComponent(vl3);

			for(int j=0;j<entry.painopiste.size();j++) {
				TableInputEntry p = entry.painopiste.get(j);
				if(p.removed) continue;
				level(vl3, p, level+1, j);
			}

			if(!readOnly) {

				Button b = new Button("Lisää " + entry.characterInfo.focusDescription);
				b.addStyleName("tiny");
				vl2.addComponent(b);
				b.addClickListener(new ClickListener() {

					private static final long serialVersionUID = 3351797676087829282L;

					@Override
					public void buttonClick(ClickEvent event) {
						final int j = entry.painopiste.size();
						String newTime = entry.time;
						TimeInterval ti = TimeInterval.parse(entry.time);
						if(!ti.isSingle()) {
							// OK parent does not force time -> let's try current time if that is singular
							TimeInterval ti2 = TimeInterval.parse(main.getUIState().time);
							if(ti2.isSingle()) {
								newTime = main.getUIState().time;
							}
						}
						entry.painopiste.add(new TableInputEntry(entry, entry.characterInfo.getGoalSubmapInfo(database), null, "Uusi " + entry.characterInfo.focusDescription, newTime));
						TableInputEntry p = entry.painopiste.get(j);
						level(vl3, p, level+1,j);
					}

				});
				
			}

		}

	}

	private void populate() {
		
		isListening = false;
		
		content.removeAllComponents();
		
		final VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.addStyleName("inputTableGroup");

		final VerticalLayout vl2 = new VerticalLayout();
		vl2.setWidth("100%");
		vl.addComponent(vl2);
		
		content.addComponent(vl);
		
		for(int i=0;i<data.size();i++) {
			final TableInputEntry entry = data.get(i);
			if(entry.removed) continue;
			level(vl2,entry, 0, i);
		}
		
		Button b = new Button("Lisää " + base.getMap(main.getDatabase()).getCharacterInfo(main.getDatabase()).goalDescription);
		b.addStyleName("tiny");
		vl.addComponent(b);
		b.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 3351797676087829282L;

			@Override
			public void buttonClick(ClickEvent event) {
			
				LisaaTavoite.selectGoalType(main, base.getMap(main.getDatabase()), new Consumer<String>() {
					
					@Override
					public void accept(String uuid) {
						if(uuid != null) {
							
							Database database = main.getDatabase();
							Base copy = database.find(uuid);
							Tavoite t = Tavoite.createCopy(main, main.uiState.current, copy);
							try {
								t.ensureImplementationMap(main);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Updates.updateJS(main, true);

							final Property aika = Property.find(database, Property.AIKAVALI);
							String a = aika.getPropertyValue(t);

							final int j = data.size();
							data.add(new TableInputEntry(null, t.getMap(database).getCharacterInfo(database), t, t.getText(database), a));
							final TableInputEntry entry = data.get(j);
							level(vl2, entry, 0, j);

						}
					}
					
				});
				
			}
			
		});
		

		isListening = true;

	}
	
}
