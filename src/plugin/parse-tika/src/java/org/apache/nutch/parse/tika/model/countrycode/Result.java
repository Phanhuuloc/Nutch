package org.apache.nutch.parse.tika.model.countrycode;

import java.util.List;

/**
 * Created by laphuoc on 10/11/2016.
 */
public class Result {
    List<Address> address_components;

    public List<Address> getAddress_components() {
        return address_components;
    }

    public void setAddress_components(List<Address> address_components) {
        this.address_components = address_components;
    }
}
