package com.madhackerdesigns.jinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import com.google.api.client.json.JsonFactory;
import com.madhackerdesigns.jinder.models.Message;

public abstract class Listener implements Runnable {
  
  private Connection connection;
  private boolean listening = true;
  private BufferedReader reader;
  private Room room;
  
  // public methods
  
  public abstract void handleNewMessage(Message message);

  @Override
  public void run() {
    connection.logger().log(Level.INFO, "Starting new Listener thread for " + room.name + "...");
    try {
      connectAndListenToMessages();
    } catch (IOException e) {
      connection.logger().log(Level.WARNING, "Got disconnected from " + room.name + "!");
    }
  }
  
  // protected methods
  
  protected void connectAndListenToMessages() throws IOException {
    String nextLine = readNextLine();
    while (listening && notEmpty(nextLine)) {
      parseMessageFrom(nextLine);
      nextLine = readNextLine();
    }
  }
  
  protected void setConnection(Connection connection) {
    this.connection = connection;
  }
  
  protected void setRoom(Room room) {
    this.room = room;
  }
  
  protected void stop() {
    listening = false;
  }
  
  // private methods

  private void parseMessageFrom(String nextLine) throws IOException {
    Message message = jsonFactory().createJsonParser(nextLine).parseAndClose(Message.class, null);
    handleNewMessage(message);
  }
  
  private String readNextLine() throws IOException {
    return reader().readLine().trim();
  }
  
  private JsonFactory jsonFactory() {
    return connection.jsonFactory();
  }
  
  private boolean notEmpty(String string) {
    return string != null && ! string.equals("");
  }
  
  private BufferedReader reader() throws IOException {
    if (reader == null) {
      InputStream stream = connection.getStreamForRoom(room.id).getContent();
      reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    }
    return reader;
  }

}
