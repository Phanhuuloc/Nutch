package org.apache.nutch.parse.tika.model;

/**
 * Created by laphuoc on 10/24/2016.
 */
public class CrawledBrand {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrawledBrand brand = (CrawledBrand) o;

        return name.equals(brand.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
