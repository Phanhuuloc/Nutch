package org.apache.nutch.parse.tika.model;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 * Created by laphuoc on 9/28/2016.
 */
public class Quark {
    @Field
    int id;

    @Field
    String quark_type;

    @Field
    String link_external;

    @Field
    List<String> photos;

    @Field
    boolean has_photo;

    @Field
    int user_id;

    @Field
    int brand_id;

    @Field
    String language;

    @Field
    List<String> list_languages;

    String country_code;

    @Field
    String country_code_en;

    @Field
    String title_en;

    @Field
    String description_en;

    @Field
    String manufacturer_en;

    @Field
    String country_en;

    @Field
    String address_lv1_en;

    @Field
    long created_time;

    @Field
    long updated_time;

    @Field
    boolean delete_flag;

    @Field
    boolean status;

    @Field
    String price;

    @Field
    List<String>  value_bands_en;

    @Field
    List<String> value_bands_data_en;
    
    @Field
    List<String> vb_raw_name_en;
    
    @Field
    List<String> vb_category_en;
    
    @Field
    String model_name_en;

    String crawl_data;

    boolean isExisted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuark_type() {
        return quark_type;
    }

    public void setQuark_type(String quark_type) {
        this.quark_type = quark_type;
    }

    public String getLink_external() {
        return link_external;
    }

    public void setLink_external(String link_external) {
        this.link_external = link_external;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public boolean getHas_photo() {
        return has_photo;
    }

    public void setHas_photo(boolean has_photo) {
        this.has_photo = has_photo;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getBrand_id() {
        return brand_id;
    }

    public void setBrand_id(int brand_id) {
        this.brand_id = brand_id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getList_languages() {
        return list_languages;
    }

    public void setList_languages(List<String> list_languages) {
        this.list_languages = list_languages;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getCountry_code_en() {
        return country_code_en;
    }

    public void setCountry_code_en(String country_code_en) {
        this.country_code_en = country_code_en;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDescription_en() {
        return description_en;
    }

    public void setDescription_en(String description_en) {
        this.description_en = description_en;
    }

    public String getManufacturer_en() {
        return manufacturer_en;
    }

    public void setManufacturer_en(String manufacturer_en) {
        this.manufacturer_en = manufacturer_en;
    }

    public String getCountry_en() {
        return country_en;
    }

    public void setCountry_en(String country_en) {
        this.country_en = country_en;
    }

    public String getAddress_lv1_en() {
        return address_lv1_en;
    }

    public void setAddress_lv1_en(String address_lv1_en) {
        this.address_lv1_en = address_lv1_en;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public long getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(long updated_time) {
        this.updated_time = updated_time;
    }

    public boolean getDelete_flag() {
        return delete_flag;
    }

    public void setDelete_flag(boolean delete_flag) {
        this.delete_flag = delete_flag;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<String>  getValue_bands_en() {
        return value_bands_en;
    }

    public void setValue_bands_en(List<String>  value_bands_en) {
        this.value_bands_en = value_bands_en;
    }

    public List<String> getValue_bands_data_en() {
        return value_bands_data_en;
    }

    public void setValue_bands_data_en(List<String> value_bands_data_en) {
        this.value_bands_data_en = value_bands_data_en;
    }

    public String getCrawl_data() {
        return crawl_data;
    }

    public void setCrawl_data(String crawl_data) {
        this.crawl_data = crawl_data;
    }

    public String getModel_name_en() {
        return model_name_en;
    }

    public void setModel_name_en(String model_name_en) {
        this.model_name_en = model_name_en;
    }

    public boolean isExisted() {
        return isExisted;
    }

    public void setExisted(boolean existed) {
        isExisted = existed;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

	public List<String> getVb_raw_name_en() {
		return this.vb_raw_name_en;
	}

	public void setVb_raw_name_en(List<String> vb_raw_name_en) {
		this.vb_raw_name_en = vb_raw_name_en;
	}

	public List<String> getVb_category_en() {
		return this.vb_category_en;
	}

	public void setVb_category_en(List<String> vb_category_en) {
		this.vb_category_en = vb_category_en;
	}
}
