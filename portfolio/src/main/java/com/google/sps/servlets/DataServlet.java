// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns data for my portfolio*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private int commentLimit = 5; // default limit = 5
  private String commentLanguageCode = "en"; // default lang code is english
  private Gson gson = new Gson();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private class Comment {
    public String message;
    public String score;

    public Comment(String message, String score) {
      this.message = message;
      this.score = score;
    }
  }

  private class CommentData {
    public List<Comment> comments;
    public int commentLimit;
    public String languageCode;

    public CommentData(List<Comment> comments, int commentLimit, String languageCode) {
      this.comments = comments;
      this.commentLimit = commentLimit;
      this.languageCode = languageCode;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comments").addSort("timestamp", SortDirection.DESCENDING);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(commentLimit));
    CommentData commentData = new CommentData(new ArrayList<Comment>(), commentLimit, commentLanguageCode);

    for (Entity entity : results) {
      String toDisplay = entity.getProperty("timestamp").toString() + ": ";
      String fromDatastore = (String) entity.getProperty("message");
      Translate translate = TranslateOptions.getDefaultInstance().getService();
      Translation translation =
        translate.translate(fromDatastore,
                            Translate.TranslateOption.targetLanguage(commentLanguageCode),
                            Translate.TranslateOption.format("text"));
      toDisplay += translation.getTranslatedText();
      commentData.comments.add(new Comment(toDisplay, "")); // TODO: Add setiment score with comments
    }

    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(gson.toJson(commentData)); 
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("input");
    if (!comment.equals("")) {
      Entity add = new Entity("Comments");
      add.setProperty("message", comment);
      add.setProperty("timestamp", new Date());

      datastore.put(add);
    }

    String limit = request.getParameter("limit");
    if (!limit.equals(null)) {
      commentLimit = Integer.parseInt(limit);
    }

    String langCode =  request.getParameter("language");
    if (!langCode.equals("")) {
      commentLanguageCode = langCode;
    }

    response.sendRedirect("/index.html");
  }
}
