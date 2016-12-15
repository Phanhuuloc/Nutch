package org.apache.nutch.parse.tika.model.countrycode;

import java.util.List;

/**
 * Created by laphuoc on 10/11/2016.
 */
public class GeoLocation {
    String status;
    List<Result> results;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
