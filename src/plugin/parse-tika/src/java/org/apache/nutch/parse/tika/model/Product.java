package org.apache.nutch.parse.tika.model;

import java.util.List;

public class Product {
	private Long id;
	private String rawHtml;
	private String name;
	private String imageUrl;
	private String price;
	private String unit;
	private String company;
	private String address;
	private String phone;
	private String valueBandname;
	private String link_external;
	private String model_name_en;
    private String description;
	private String companyLogo;
	private String country_code;
	private String country;
	private String categoryName;
//    private List<String> multipleImageUrls;
	public Product() {
		super();
	}

	public Product(Long id, String rawHtml, String name, String imageUrl, String price, String unit, String company,
			String address) {
		super();
		this.id = id;
		this.rawHtml = rawHtml;
		this.name = name;
		this.imageUrl = imageUrl;
		this.price = price;
		this.unit = unit;
		this.company = company;
		this.address = address;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public String getRawHtml() {
		return rawHtml;
	}

	public void setRawHtml(String rawHtml) {
		this.rawHtml = rawHtml;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getValueBandname() {
		return valueBandname;
	}

	public void setValueBandname(String valueBandname) {
		this.valueBandname = valueBandname;
	}

	public String getLink_external() {
		return link_external;
	}

	public void setLink_external(String link_external) {
		this.link_external = link_external;
	}

	public String getModel_name_en() {
		return model_name_en;
	}

	public void setModel_name_en(String model_name_en) {
		this.model_name_en = model_name_en;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

	public String getCountry_code() {
		return country_code;
	}

	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

   /* public List<String> getMultipleImageUrls() {
        return multipleImageUrls;
    }

    public void setMultipleImageUrls(List<String> multipleImageUrls) {
        this.multipleImageUrls = multipleImageUrls;
    }*/

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
