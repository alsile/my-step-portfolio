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

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.sps.data.Comments;
import java.util.Date;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns data for my portfolio*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final int COMMENT_LIMIT_DEFAULT = 5;
  private static final String COMMENT_LANGUAGE_CODE_DEFAULT = "en";
  private Comments comments = new Comments(COMMENT_LIMIT_DEFAULT,
                                           new ArrayList<String>(),
                                           COMMENT_LANGUAGE_CODE_DEFAULT);
  private Gson gson = new Gson();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    comments.jsonList.clear();

    for (Entity entity : results.asIterable()) {
      String toDisplay = entity.getProperty("timestamp").toString() + ": ";
      String fromDatastore = (String) entity.getProperty("title");
      Translate translate = TranslateOptions.getDefaultInstance().getService();
      Translation translation =
        translate.translate(fromDatastore,
                            Translate.TranslateOption.targetLanguage(comments.languageCode),
                            Translate.TranslateOption.format("text"));
      toDisplay += translation.getTranslatedText();
      comments.jsonList.add(toDisplay);
    }
    
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(gson.toJson(comments)); 
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("input");
    if (!comment.equals("")) {
      Entity add = new Entity("Task");
      add.setProperty("title", comment);
      add.setProperty("timestamp", new Date());

      datastore.put(add);
    }
    
    String limit = request.getParameter("limit");
    if (!limit.equals("")) {
      comments.limit = Integer.parseInt(request.getParameter("limit"));
    }

    String langCode =  request.getParameter("language");
    if (!langCode.equals("")) {
      comments.languageCode = langCode;
    }

    response.sendRedirect("/index.html");
  }
}
