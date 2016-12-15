package org.apache.nutch.entitybean;

public class ProductionBean {

	String productionId;
	String productionName;
	String displayProductionName;
	String filmId;
	String filmName;
	String productionUrl;
	String extraProductionUrl;
	String languageCode;

	public String getDisplayProductionName() {
		return displayProductionName;
	}

	public void setDisplayProductionName(String displayProductionName) {
		this.displayProductionName = displayProductionName;
	}

	public String getExtraProductionUrl() {
		return extraProductionUrl;
	}

	public void setExtraProductionUrl(String extraProductionUrl) {
		this.extraProductionUrl = extraProductionUrl;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getProductionId() {
		return productionId;
	}

	public void setProductionId(String productionId) {
		this.productionId = productionId;
	}

	public String getProductionName() {
		return productionName;
	}

	public void setProductionName(String productionName) {
		this.productionName = productionName;
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

	public String getProductionUrl() {
		return productionUrl;
	}

	public void setProductionUrl(String productionUrl) {
		this.productionUrl = productionUrl;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return filmName + " ; " + productionName + " ; " + productionUrl;
	}
}
