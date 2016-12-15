package org.apache.nutch.parse.tika.model;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

/**
 * Created by laphuoc on 9/28/2016.
 */
public class ValueBand {
    @Field
    int id;

    @Field
    String raw_name;

    @Field
    String category;

    @Field
    int category_id;

    @Field
    String type;

    @Field
    String language;

    @Field
    String name;

    @Field
    String search_name;

    long created_time;

    @Field
    int num_view;

    @Field
    double point;

    @Field
    List<String> users_subscribed;

    @Field
    long count;

    @Field
    long count_brand;

    boolean isExisted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRaw_name() {
        return raw_name;
    }

    public void setRaw_name(String raw_name) {
        this.raw_name = raw_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearch_name() {
        return search_name;
    }

    public void setSearch_name(String search_name) {
        this.search_name = search_name;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public int getNum_view() {
        return num_view;
    }

    public void setNum_view(int num_view) {
        this.num_view = num_view;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public List<String> getUsers_subscribed() {
        return users_subscribed;
    }

    public void setUsers_subscribed(List<String> users_subscribed) {
        this.users_subscribed = users_subscribed;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount_brand() {
        return count_brand;
    }

    public void setCount_brand(long count_brand) {
        this.count_brand = count_brand;
    }

    public boolean isExisted() {
        return isExisted;
    }

    public void setExisted(boolean existed) {
        isExisted = existed;
    }

    @Override
    public String toString() {
        return "id=" + id + " raw_name=" + raw_name + " name=" + name
                + " category=" + category + " " + category_id;
    }
}
