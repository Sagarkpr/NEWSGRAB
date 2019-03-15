package com.grabnews.app.DatabaseModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.grabnews.app.News.Source;

import io.realm.RealmObject;

public class NewsSchema extends RealmObject{

    private String author;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String pbDate;
    private String content;
    private String id;
    private String name;

    public NewsSchema() {

    }

    public NewsSchema(String id, String name, String author, String title, String description, String url, String imageUrl, String pbDate, String content) {

        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.pbDate = pbDate;
        this.content = content;
        this.id=id;
        this.name=name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPbDate() {
        return pbDate;
    }

    public void setPbDate(String pbDate) {
        this.pbDate = pbDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}