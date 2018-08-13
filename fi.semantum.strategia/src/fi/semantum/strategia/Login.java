package fi.semantum.strategia;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.widget.Account;
import fi.semantum.strategia.widget.Database;

public class Login {

	public static void login(final Main main) {

        final Window subwindow = new Window("Anna k‰ytt‰j‰tunnus ja salasana", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("295px");
        subwindow.setResizable(false);

        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        final TextField tf = new TextField();
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.setCaption("K‰ytt‰j‰tunnus:");
        tf.setId("loginUsernameField");
        winLayout.addComponent(tf);

        final PasswordField pf = new PasswordField();
        pf.setCaption("Salasana:");
        pf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf.setWidth("100%");
        pf.setId("loginPasswordField");
        winLayout.addComponent(pf);

        final Label l = new Label("V‰‰r‰ k‰ytt‰j‰tunnus tai salasana");
        l.addStyleName(ValoTheme.LABEL_FAILURE);
        l.addStyleName(ValoTheme.LABEL_TINY);
        l.setVisible(false);
        winLayout.addComponent(l);

        pf.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = -2708082203576343391L;

        	@Override
			public void valueChange(ValueChangeEvent event) {
        		doLogin(main, subwindow, l, tf.getValue(), pf.getValue());
			}
		});

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

//        Button auto = new Button("Autologin", new Button.ClickListener() {
//        	
//			private static final long serialVersionUID = -5148036024457593062L;
//
//			public void buttonClick(ClickEvent event) {
//				for(Account acc : Account.enumerate(main.getDatabase())) {
//					if(acc.isAdmin())
//						doLogin(main, subwindow, l, acc.hash, acc);
//				}
//            }
//            
//        });
//
//        hl.addComponent(auto);

        Button ok = new Button("Kirjaudu", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5148036024457593062L;

			public void buttonClick(ClickEvent event) {
        		doLogin(main, subwindow, l, tf.getValue(), pf.getValue());
            }
            
        });
        
        hl.addComponent(ok);

        Button close = new Button("Peruuta", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5719853213838228457L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
            
        });
        
        hl.addComponent(close);
        
        winLayout.addComponent(hl);
        winLayout.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
		main.addWindow(subwindow);
		
		tf.setCursorPosition(tf.getValue().length());

	}

	static void doLogin(Main main, Window subwindow, Label l, String usr, String pass) {

		Database database = main.getDatabase();
    	String hash = Utils.hash(pass);
    	Account acc = Account.find(database, usr);
    	doLogin(main, subwindow, l, hash, acc);

	}

	static void doLogin(Main main, Window subwindow, Label l, String hash, Account acc) {

		Database database = main.getDatabase();
    	if(acc != null) {
    		if(hash.equals(acc.getHash())) {
            	main.removeWindow(subwindow);
            	main.account = acc;
            	main.hallinnoi.setVisible(acc.isAdmin());
            	main.tili.setVisible(true);
            	Updates.update(main, false);
            	main.login.setCaption("Kirjaudu ulos " + acc.getId(database));
            	main.saveState.setEnabled(true);
    			return;
    		}
    	}
    	l.setVisible(true);

	}
	
}
