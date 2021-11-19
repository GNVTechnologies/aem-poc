package com.adobe.aem.guides.poc.core.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


public class AdaptiveFormsPostRequestWrapper extends HttpServletRequestWrapper {

  public AdaptiveFormsPostRequestWrapper(HttpServletRequest request) throws IOException
  {
    //So that other request method behave just like before
    super(request);
  }

  /*@Override
  public String getMethod() {
    return "GET";
  }
  */

}