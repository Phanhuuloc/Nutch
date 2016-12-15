package org.apache.nutch.entitybean;

public class FilmBean {

	String filmId;
	String filmName;
	String displayFilm;
	String officialUrl;
	String languageCode;

	public String getOfficialUrl() {
		return officialUrl;
	}

	public void setOfficialUrl(String officialUrl) {
		this.officialUrl = officialUrl;
	}

	public String getFilmId() {
		return filmId;
	}

	public void setFilmId(String filmId) {
		this.filmId = filmId;
	}

	public String getFilmName() {
		return filmName;
	}

	public void setFilmName(String filmName) {
		this.filmName = filmName;
	}

	public String getDisplayFilm() {
		return displayFilm;
	}

	public void setDisplayFilm(String displayFilm) {
		this.displayFilm = displayFilm;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	@Override
	public String toString() {
		return filmId + " - " + filmName + " - " + displayFilm + " - " + languageCode;
	}

}
