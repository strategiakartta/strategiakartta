package fi.semantum.strategia;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Utils.CommentCallback;

public class Dialogs {

	public static Window makeDialog(final Main main, String caption, String back, Component content_, HorizontalLayout buttons) {
		return makeDialog(main, main.dialogWidth(), main.dialogHeight(), caption, back, content_, buttons);
	}

	public static Window makeDialog(final Main main, String w, String h, String caption, String back, Component content_, HorizontalLayout buttons) {

		if(buttons == null) {
			buttons = new HorizontalLayout();
	        buttons.setSpacing(true);
	        buttons.setMargin(true);
		}

        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth(w);
        subwindow.setHeight(h);
        subwindow.setResizable(true);
		
		if(back != null) {
	        Button close = new Button(back, new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1992235622970234624L;
	
	            public void buttonClick(ClickEvent event) {
	    			main.removeWindow(subwindow);
	            }
	            
	        });
	        buttons.addComponent(close);
		}
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Panel content = new Panel();
        content.setSizeFull();
        content.addStyleName(ValoTheme.PANEL_BORDERLESS);
        content.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
        content.setContent(content_);
        
        winLayout.addComponent(content);
        winLayout.setExpandRatio(content, 1.0f);
        winLayout.setComponentAlignment(content, Alignment.BOTTOM_CENTER);

        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
		main.addWindow(subwindow);
		
		return subwindow;
		
	}

	public static Window errorDialog(final Main main, String caption, Component content_) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth(main.dialogWidth());
        subwindow.setHeight(main.dialogHeight());
        subwindow.setResizable(false);

		Button close = new Button("Jatka", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });
        
        buttons.addComponent(close);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Panel content = new Panel();
        content.addStyleName(ValoTheme.PANEL_BORDERLESS);
        content.setSizeFull();
        
        content.setContent(content_);
        
        winLayout.addComponent(content);
        winLayout.setExpandRatio(content, 1.0f);
        winLayout.setComponentAlignment(content, Alignment.BOTTOM_CENTER);

        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);

		main.addWindow(subwindow);
		
		return subwindow;
		
	}

	public static Window confirmDialog(final Main main, String text, String ok, String cancel, final Runnable runnable) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window("Vahvistus", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("150px");
        subwindow.setResizable(true);

		Button okButton = new Button(ok, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
    			runnable.run();
            }
            
        });
        
		Button cancelButton = new Button(cancel, new Button.ClickListener() {
			
			private static final long serialVersionUID = -5227602164819268383L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });

		buttons.addComponent(okButton);
		buttons.addComponent(cancelButton);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Label l = new Label(text);
        winLayout.addComponent(l);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
		main.addWindow(subwindow);
		
		return subwindow;
		
	}

	public static Window infoDialog(final Main main, String caption, String text, final Runnable runnable) {

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("150px");
        subwindow.setResizable(false);
        
		Button okButton = new Button("Jatka", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
    			if(runnable != null)
    				runnable.run();
            }
            
        });

		buttons.addComponent(okButton);
		
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        Label l = new Label(text);
        l.addStyleName(ValoTheme.LABEL_LARGE);
        winLayout.addComponent(l);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
        //main.closeDialog();

		main.addWindow(subwindow);
		
		return subwindow;
		
	}	
	
	public static Window commentDialog(final Main main, String caption, String okCaption, final CommentCallback runnable) {

        final Window subwindow = new Window(caption, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("550px");
        subwindow.setHeight("450px");
        
        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);
        winLayout.setSizeFull();
        
        final TextField s = new TextField("");
        s.setCaption("Anna lyhyt selite n�ytett�v�ksi arvon yhteydess�");
        s.addStyleName(ValoTheme.TEXTFIELD_TINY);
        s.setWidth("100%");
        winLayout.addComponent(s);
        winLayout.setExpandRatio(s, 0.0f);
        
        final TextArea l = new TextArea("");
        l.setCaption("P�ivitykseen liittyvi� lis�tietoja");
        l.addStyleName(ValoTheme.TEXTAREA_TINY);
        l.setSizeFull();
        winLayout.addComponent(l);
        winLayout.setExpandRatio(l, 1.0f);

		Button okButton = new Button(okCaption, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
            	String shortComment = s.getValue();
            	String comment = l.getValue();
    			main.removeWindow(subwindow);
    			if(runnable != null)
    				runnable.runWithComment(shortComment, comment);
            }
            
        });

		Button cancelButton = new Button("Peruuta", new Button.ClickListener() {
        	
			private static final long serialVersionUID = -1059166655868073563L;

            public void buttonClick(ClickEvent event) {
    			main.removeWindow(subwindow);
            }
            
        });

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		
		buttons.addComponent(okButton);
		buttons.addComponent(cancelButton);
        
        winLayout.addComponent(buttons);
        winLayout.setExpandRatio(buttons, 0.0f);
        winLayout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
        
		main.addWindow(subwindow);
		
		return subwindow;
		
	}

	
}
