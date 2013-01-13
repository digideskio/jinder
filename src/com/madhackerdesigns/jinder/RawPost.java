package com.madhackerdesigns.jinder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;

public class RawPost {
  
  // static and instance fields
  
  private static final String boundary = "---------------------------XXX";
  private static final String requestBodyEnd = String.format("\r\n--%s--\r\n", boundary);
  
  private Connection connection;
  private Long contentLength;
  private String contentType;
  private File file;
  private String requestBodyStart;
  private String url;
  
  // constructors
  
  protected RawPost(Connection connection, String url, File file) {
    this.connection = connection;
    this.url = url;
    this.file = file;
  }
  
  // protected methods
  
  protected Response execute() throws IOException {
    // create http connection
    log(Level.INFO, "Uploading to URL: " + url);
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    
    // set basic authentication
    conn.setRequestProperty("Authorization", "Basic " + encodedAuth());
    
    // setup connection properties
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setUseCaches(false);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    
    // set content length property
    conn.setRequestProperty("Content-Length", contentLength().toString());
    
    // set streaming mode of the connection
    // see http://developer.android.com/reference/java/net/HttpURLConnection.html
    if (contentLength() >= 0 && contentLength() <= Integer.MAX_VALUE) {
      conn.setFixedLengthStreamingMode(contentLength().intValue());
    } else {
      conn.setChunkedStreamingMode(0);
    }
    
    // send the request body
    conn.connect();
    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
    out.writeBytes(requestBodyStart());
    out.flush();
    fileContent().writeTo(out);
    out.flush();
    out.writeBytes(requestBodyEnd);
    out.flush();
    out.close();
    
    // read response from connection
    int code = conn.getResponseCode();
    String message = conn.getResponseMessage();
    log(Level.INFO, String.format("Response: %d %s", code, message));
    return new Response(code, message);
  }
  
  // private methods
  
  private Long contentLength() {
    if (contentLength == null) {
      contentLength = requestBodyStart().length() + file.length() + requestBodyEnd.length();
    }
    return contentLength;
  }
  
  private String contentType() {
    if (contentType == null) {
      contentType = URLConnection.guessContentTypeFromName(file.getName());
      if (contentType == null) { contentType = "application/octet-stream"; }
    }
    return contentType;
  }

  private String encodedAuth() throws IOException {
    String userPass = connection.token() + ":X";
    return Base64.encodeBase64String(StringUtils.getBytesUtf8(userPass));
  }
  
  private InputStreamContent fileContent() throws IOException {
    return new InputStreamContent(contentType(), new FileInputStream(file));
  }
  
  private void log(Level level, String message) {
    connection.log(level, message);
  }
  
  private String requestBodyStart() {
    if (requestBodyStart == null) {
      String dispositionHeader = "Content-Disposition: form-data; name=\"upload\"; filename=\"" + file.getName() + "\"";
      String typeHeader = "Content-Type: " + contentType();
      requestBodyStart = String.format("--%s\r\n%s\r\n%s\r\n\r\n", boundary, dispositionHeader, typeHeader);
    }
    return requestBodyStart;
  }
  
  // internal classes
  
  public class Response {
    
    private int code;
    private String message;
    
    protected Response(int code, String message) {
      this.code = code;
      this.message = message;
    }
    
    public int getResponseCode() {
      return code;
    }
    
    public String getResponseMessage() {
      return message;
    }
    
  }

}
