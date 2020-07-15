package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

public class Comments {
  
  public int limit;
  public List<String> jsonList;
  public String languageCode;

  public Comments(int limit, List<String> jsonList, String languageCode) {
    this.limit = limit;
    this.jsonList = jsonList;
    this.languageCode = languageCode;
  }
}