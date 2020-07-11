package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

public class Comments {
  
  public int limit;
  public List<String> jsonList;

  public Comments(int limit, List<String> jsonList) {
    this.limit = limit;
    this.jsonList = jsonList;
  }
}