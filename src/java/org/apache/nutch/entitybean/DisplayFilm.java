package org.apache.nutch.entitybean;

public class DisplayFilm {
	private Integer film_id;
	private String display_film;
	private String display_content;
	private String display_description;
	private String display_series;
	private String language_code;
	public Integer getFilm_id() {
		return film_id;
	}
	public void setFilm_id(Integer film_id) {
		this.film_id = film_id;
	}
	public String getDisplay_film() {
		return display_film;
	}
	public void setDisplay_film(String display_film) {
		this.display_film = display_film;
	}
	
	public String getDisplay_content() {
		return display_content;
	}
	public void setDisplay_content(String display_content) {
		this.display_content = display_content;
	}
	public String getDisplay_description() {
		return display_description;
	}
	public void setDisplay_description(String display_description) {
		this.display_description = display_description;
	}
	public String getDisplay_series() {
		return display_series;
	}
	public void setDisplay_series(String display_series) {
		this.display_series = display_series;
	}
	public String getLanguage_code() {
		return language_code;
	}
	public void setLanguage_code(String language_code) {
		this.language_code = language_code;
	}
}
