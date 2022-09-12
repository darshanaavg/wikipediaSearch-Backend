package wikipedia.search.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class searchRequest {
	
	@NotEmpty@NotNull
	private String text;
	@NotNull
	private int from;
	@NotNull
	private int size;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
