package com.grabnews.app.News;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

public class News {
   @SerializedName("status")
   @Expose
   private String status;

   @SerializedName("totalResult")
   @Expose
   private int totalResult;

   @SerializedName("articles")
   @Expose
   private List<NewsData> article;

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public int getTotalResult() {
      return totalResult;
   }

   public void setTotalResult(int totalResult) {
      this.totalResult = totalResult;
   }

   public List<NewsData> getArticle() {
      return article;
   }

   public void setArticle(List<NewsData> article) {
      this.article = article;
   }
}
