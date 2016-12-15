package org.apache.nutch.parse.tika.model;

import org.apache.solr.client.solrj.beans.Field;

/**
 * Created by laphuoc on 9/27/2016.
 */

/**
 * Model for testing
 */
public class Item {
    @Field
    String id;

    @Field
    String title;

    //url
    @Field
    String url;

    // content
    @Field
    String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
