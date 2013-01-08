package com.madhackerdesigns.jinder.models;

import java.util.List;

import com.google.api.client.util.Key;
import com.madhackerdesigns.jinder.Room;

public class RoomList {

  // Campfire API data model: Rooms
  
  @Key private List<Room> rooms;
  
  // public methods
  
  public List<Room> rooms() {
    return rooms;
  }
  
}
