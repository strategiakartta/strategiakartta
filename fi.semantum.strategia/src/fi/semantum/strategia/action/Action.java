package fi.semantum.strategia.action;

public abstract class Action implements Runnable {
	
	protected String caption;
	protected String category;
	
	public Action(String caption) {
		this(null, caption);
	}
	
	public Action(String category, String caption) {
		this.category = category;
		this.caption = caption;
	}

	public boolean accept() {
		return true;
	}

	public String getCaption() {
		return caption;
	}
	
}