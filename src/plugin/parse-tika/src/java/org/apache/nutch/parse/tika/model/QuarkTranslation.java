package org.apache.nutch.parse.tika.model;

/**
 * Created by laphuoc on 9/28/2016.
 */
public class QuarkTranslation {
    int id;
    int quark_id;
    String language_code;
    String title;
    String description;
    String manufacturer;
    String country;
    String address_lv1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuark_id() {
        return quark_id;
    }

    public void setQuark_id(int quark_id) {
        this.quark_id = quark_id;
    }

    public String getLanguage_code() {
        return language_code;
    }

    public void setLanguage_code(String language_code) {
        this.language_code = language_code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress_lv1() {
        return address_lv1;
    }

    public void setAddress_lv1(String address_lv1) {
        this.address_lv1 = address_lv1;
    }
}
