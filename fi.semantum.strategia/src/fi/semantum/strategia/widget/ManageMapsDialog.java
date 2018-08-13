package fi.semantum.strategia.widget;

import java.util.Collection;

import org.vaadin.elements.ElementIntegration;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.colorpicker.ColorChangeEvent;
import com.vaadin.ui.components.colorpicker.ColorChangeListener;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;

public class ManageMapsDialog {

	private CheckBox linkCheckbox;
	private CheckBox tavoiteSubmapCheckbox;
	private ComboBox tavoiteSubmapSelection;
	private ColorPicker backgroundButton;
	private ColorPicker foregroundButton;
	private TextField idTextField;
	private TextField goalTextField;
	private TextField focusTextField;
	private ComboBox list;
	private Label example;
	private ValueChangeListener listener;
	private ValueChangeListener idTextFieldListener;
	private ValueChangeListener goalTextListener;
	private ValueChangeListener focusTextListener;
	private ColorChangeListener foregroundListener;
	private ColorChangeListener backgroundListener;
	private ValueChangeListener linkCheckboxListener;
	private ValueChangeListener tavoiteSubmapCheckboxListener;
	private ValueChangeListener tavoiteSubmapSelectionListener;
	private Strategiakartta returnMap;
	private Base currentSelection;
	
	private String currentText;
	private String focusColor;
	private String focusTextColor;
	private String goalDescription;
	private String focusDescription;
	private String tavoiteSubmapType = "";
	private boolean linkWithParent = true;
	private boolean tavoiteSubmap = false;
	
	ManageMapsDialog(final Main main, Strategiakartta returnMap, Base initialSelection) {
		
		final Database database = main.getDatabase();

		this.returnMap = returnMap;
		currentSelection = initialSelection;
		currentText = currentSelection.getText(database);
		
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        list = new ComboBox();
        list.setCaption("Valitse karttatyyppi");
        list.setWidth("100%");
        list.setNullSelectionAllowed(false);
        list.addStyleName(ValoTheme.TABLE_SMALL);
        list.addStyleName(ValoTheme.TABLE_COMPACT);
        list.setTextInputAllowed(false);

        final Button apply = new Button("Tee muutokset");
        final Button add = new Button("Lis‰‰ tyyppi", new Button.ClickListener() {

			private static final long serialVersionUID = 7600747402942335542L;

			public void buttonClick(ClickEvent event) {
				
				Property typeProperty = Property.find(database, Property.TYPE);
		        ObjectType levelType = ObjectType.find(database, ObjectType.LEVEL_TYPE);
				
				Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
				Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
				Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
				Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
				Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
				Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);

				ObjectType newType = ObjectType.create(database, "", "Uusi karttatyyppi");
				newType.properties.add(typeProperty.make(levelType.uuid));
				characterDescriptionP.set(main, newType, "Painopiste");
				goalDescriptionP.set(main, newType, "");
				characterColorP.set(main, newType, database.createHue());
				characterTextColorP.set(main, newType, "#000");
				linkWithParentP.set(main, newType, "true");
				goalSubmapP.set(main, newType, "false");
				Updates.updateJS(main, true);
				initialize(database);

            }
            
        });
        final Button vastuut = new Button("Vastuumalli", new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {

				new ResponsibilityModelsDialog(main, currentSelection);

            }
            
        });
        final Button close = new Button("Sulje");

		listener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String uuid = (String)list.getValue();
				currentSelection = database.find(uuid);
				initialize(database);
				update();
			}
			
	    };
        
        content.addComponent(list);
        content.setExpandRatio(list, 0.0f);

        idTextField = new TextField();
        idTextField.setCaption("Karttatyypin nimi");
        idTextField.setWidth("100%");
        idTextFieldListener = new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				currentText = idTextField.getValue();
				update();
			}
			
	    };
	    idTextField.addValueChangeListener(idTextFieldListener);

        content.addComponent(idTextField);
        content.setExpandRatio(idTextField, 0.0f);

        
        goalTextField = new TextField();
        goalTextField.setCaption("Valitse m‰‰re ulommille laatikoille");
        goalTextField.setWidth("100%");
		goalTextListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				goalDescription = goalTextField.getValue();
				update();
			}
			
	    };
	    goalTextField.addValueChangeListener(goalTextListener);

        content.addComponent(goalTextField);
        content.setExpandRatio(goalTextField, 0.0f);

        
        focusTextField = new TextField();
        focusTextField.setCaption("Valitse m‰‰re uusille sis‰laatikoille");
        focusTextField.setWidth("100%");
		focusTextListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				focusDescription = focusTextField.getValue();
				update();
			}
			
	    };
	    focusTextField.addValueChangeListener(focusTextListener);

        content.addComponent(focusTextField);
        content.setExpandRatio(focusTextField, 0.0f);
        
        HorizontalLayout tavoite = new HorizontalLayout();
        tavoite.setWidth("100%");
        tavoite.setSpacing(true);
        tavoite.setMargin(false);
        
        foregroundButton = new ColorPicker("Valitse tekstin v‰ri");
        foregroundButton.setCaption("Valitse tekstin v‰ri");
        foregroundButton.setWidth("100%");
        foregroundListener = new ColorChangeListener() {
        	
			private static final long serialVersionUID = 2195620409108279819L;

			@Override
            public void colorChanged(ColorChangeEvent event) {
                focusTextColor = toCSS(event.getColor());
                update();
            }
        };
        foregroundButton.addColorChangeListener(foregroundListener);
        tavoite.addComponent(foregroundButton);
        tavoite.setExpandRatio(foregroundButton, 1f);

        backgroundButton = new ColorPicker("Valitse taustav‰ri");
        backgroundButton.setCaption("Valitse taustav‰ri");
        backgroundButton.setWidth("100%");
	    backgroundListener = new ColorChangeListener() {
        	
			private static final long serialVersionUID = 5960028504533354038L;

			@Override
            public void colorChanged(ColorChangeEvent event) {
                focusColor = toCSS(event.getColor());
                update();
            }
        };
        backgroundButton.addColorChangeListener(backgroundListener);
        tavoite.addComponent(backgroundButton);
        tavoite.setExpandRatio(backgroundButton, 1f);
        
        content.addComponent(tavoite);
        content.setExpandRatio(tavoite, 0.0f);
        
        linkCheckbox = new CheckBox("Kartta linkittyy yl‰tason karttaan");
        linkCheckbox.setWidth("100%");
        linkCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				linkWithParent = linkCheckbox.getValue();
			}
			
        };
        linkCheckbox.addValueChangeListener(linkCheckboxListener);
        
        content.addComponent(linkCheckbox);
        content.setExpandRatio(linkCheckbox, 0.0f);

        tavoiteSubmapCheckbox = new CheckBox("Alikartat kytkeytyv‰t ulompiin laatikoihin");
        tavoiteSubmapCheckbox.setWidth("100%");
        tavoiteSubmapCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tavoiteSubmap = tavoiteSubmapCheckbox.getValue();
			}
			
        };
        tavoiteSubmapCheckbox.addValueChangeListener(tavoiteSubmapCheckboxListener);
        
        content.addComponent(tavoiteSubmapCheckbox);
        content.setExpandRatio(tavoiteSubmapCheckbox, 0.0f);

        tavoiteSubmapSelection = new ComboBox();
        tavoiteSubmapSelection.setWidth("100%");
		Collection<Base> subs = Strategiakartta.availableLevels(database);
		for(Base b : subs) {
			tavoiteSubmapSelection.addItem(b.uuid);
			tavoiteSubmapSelection.setItemCaption(b.uuid, b.getText(database));
			tavoiteSubmapSelection.select(b.uuid);
		}
        tavoiteSubmapSelectionListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tavoiteSubmapType = (String)tavoiteSubmapSelection.getValue();
			}
			
        };
        tavoiteSubmapSelection.addValueChangeListener(tavoiteSubmapSelectionListener);
        
        content.addComponent(tavoiteSubmapSelection);
        content.setExpandRatio(tavoiteSubmapSelection, 0.0f);

        
        
        example = new Label("<div style=\"width:100%;vertical-align:middle;\">Sisemm‰n laatikon teksti</div>");
        example.setContentMode(ContentMode.HTML);
        example.setSizeFull();
        content.addComponent(example);
        content.setExpandRatio(example, 1.0f);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        buttons.addComponent(apply);
        buttons.addComponent(add);
        buttons.addComponent(vastuut);
        buttons.addComponent(close);
        
        initialize(database);
        update();

		list.addValueChangeListener(listener);
        
		final Window dialog = Dialogs.makeDialog(main, "550px", "650px", "Hallinnoi karttatyyppej‰", null, content, buttons);
		close.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7345507553621833858L;

			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
				if(ManageMapsDialog.this.returnMap != null)
					MapTypes.selectMapType(main, ManageMapsDialog.this.returnMap);
            }
            
        });
		apply.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7345507553621833858L;

			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
				apply(main);
				if(ManageMapsDialog.this.returnMap != null)
					ManageMapsDialog.this.returnMap.prepare(main);
            }
            
        });
        
	}
	
	void initialize(Database database) {

		Collection<Base> available = Strategiakartta.availableLevels(database);
		if(currentSelection == null) {
			if(!available.isEmpty())
				currentSelection = available.iterator().next();
			else
				currentSelection = null;
		}
		
		list.removeValueChangeListener(listener);
		list.removeAllItems();
		for(Base b : available) {
			list.addItem(b.uuid);
			if(currentSelection != null) {
				if(currentSelection.uuid == b.uuid)
					list.select(b.uuid);
			}
			list.setItemCaption(b.uuid, b.text);
		}
		list.addValueChangeListener(listener);
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		if(currentSelection != null) {
			currentText = currentSelection.getText(database);
			focusDescription = characterDescriptionP.getPropertyValue(currentSelection);
			goalDescription = goalDescriptionP.getPropertyValue(currentSelection);
			focusColor = characterColorP.getPropertyValue(currentSelection);
			if(focusColor == null) focusColor = "#CA6446";
			focusTextColor = characterTextColorP.getPropertyValue(currentSelection);
			String linkValue = linkWithParentP.getPropertyValue(currentSelection);
			if(linkValue == null) linkWithParent = true;
			else if("false".equals(linkValue)) linkWithParent = false;
			else linkWithParent = true;
			String goalSubmapValue = goalSubmapP.getPropertyValue(currentSelection);
			if(goalSubmapValue == null) tavoiteSubmap = false;
			else if("true".equals(goalSubmapValue)) tavoiteSubmap = true;
			else tavoiteSubmap = false;
			Collection<Base> subTypes = currentSelection.getRelatedObjects(database, goalSubmapTypeR);
			if(subTypes.size() == 1) {
				tavoiteSubmapType = subTypes.iterator().next().uuid;
			}
		}		
	}
	
	public String parseCSSDigit(int value) {
		String result = Integer.toHexString(value);
		if(result.length() == 1) result = "0" + result;
		return result;
	}
	
	public String toCSS(Color color) {
        return "#" + parseCSSDigit(color.getRed()) + parseCSSDigit(color.getGreen()) + parseCSSDigit(color.getBlue());
	}
	
	public Color toColor(String css) {
		if(css.length() == 4) {
			return new Color(Integer.parseInt(css.substring(1,2), 16), Integer.parseInt(css.substring(2,3), 16), Integer.parseInt(css.substring(3,4), 16));
		} else if (css.length() == 7) {
			return new Color(Integer.parseInt(css.substring(1,3), 16), Integer.parseInt(css.substring(3,5), 16), Integer.parseInt(css.substring(5,7), 16));
		} else throw new IllegalArgumentException("Invalid css color: '" + css + "'");
	}

	public void update() {

		if(focusColor != null) {
			Color back = toColor(focusColor);
			backgroundButton.removeColorChangeListener(backgroundListener);
			backgroundButton.setColor(back);
			backgroundButton.addColorChangeListener(backgroundListener);
		}
		
		if(focusTextColor != null) {
			Color fore = toColor(focusTextColor);
			foregroundButton.removeColorChangeListener(foregroundListener);
			foregroundButton.setColor(fore);
			foregroundButton.addColorChangeListener(foregroundListener);
		}

		if(focusColor != null && focusTextColor != null)
			ElementIntegration.getRoot(example).setAttribute("style", "color:" + focusTextColor + ";background-color:" + focusColor + ";width:100%;height:100%;text-align:center;vertical-align:middle;line-height:100px;font-size:26px;");

		if(currentText != null) {
	        idTextField.removeValueChangeListener(idTextFieldListener);
	        idTextField.setValue(currentText);
	        idTextField.addValueChangeListener(idTextFieldListener);
		}

		if(goalDescription != null) {
	        goalTextField.removeValueChangeListener(goalTextListener);
	        goalTextField.setValue(goalDescription);
	        goalTextField.addValueChangeListener(goalTextListener);
		}

		if(focusDescription != null) {
	        focusTextField.removeValueChangeListener(focusTextListener);
	        focusTextField.setValue(focusDescription);
	        focusTextField.addValueChangeListener(focusTextListener);
		}
	
		linkCheckbox.removeValueChangeListener(linkCheckboxListener);
		linkCheckbox.setValue(linkWithParent);
		linkCheckbox.addValueChangeListener(linkCheckboxListener);

		tavoiteSubmapCheckbox.removeValueChangeListener(tavoiteSubmapCheckboxListener);
		tavoiteSubmapCheckbox.setValue(tavoiteSubmap);
		tavoiteSubmapCheckbox.addValueChangeListener(tavoiteSubmapCheckboxListener);

		tavoiteSubmapSelection.removeValueChangeListener(tavoiteSubmapSelectionListener);
		tavoiteSubmapSelection.select(tavoiteSubmapType);
		tavoiteSubmapSelection.addValueChangeListener(tavoiteSubmapSelectionListener);
		
	}
	
	public void apply(Main main) {
		
		Database database = main.getDatabase();
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Relation goalSubmapTypeP = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		if(currentSelection != null) {
			currentSelection.modifyText(main, currentText);
			goalDescriptionP.set(main, currentSelection, goalDescription);
			characterDescriptionP.set(main, currentSelection, focusDescription);
			characterColorP.set(main, currentSelection, focusColor);
			characterTextColorP.set(main, currentSelection, focusTextColor);
			linkWithParentP.set(main, currentSelection, linkWithParent ? "true" : "false");
			goalSubmapP.set(main, currentSelection, tavoiteSubmap ? "true" : "false");
			Base b = database.find(tavoiteSubmapType);
			if(b != null) {
				currentSelection.denyRelation(database, goalSubmapTypeP);
				currentSelection.addRelation(goalSubmapTypeP, b);
			}
			Updates.updateJS(main, true);
			initialize(database);
		}	
		
	}
	
	public static ManageMapsDialog create(Main main, Strategiakartta returnMap, Base initialSelection) {
		return new ManageMapsDialog(main, returnMap, initialSelection);
	}
	
}
