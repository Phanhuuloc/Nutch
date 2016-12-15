package org.apache.nutch.parse.tika.model.countrycode;

import java.util.List;

/**
 * Created by laphuoc on 10/11/2016.
 */
public class Address {
    String long_name;
    String short_name;
    List<AddressType> types;

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public List<AddressType> getTypes() {
        return types;
    }

    public void setTypes(List<AddressType> types) {
        this.types = types;
    }
}
