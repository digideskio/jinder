package com.madhackerdesigns.jinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.madhackerdesigns.jinder.helpers.MockTransport;
import com.madhackerdesigns.jinder.models.Account;
import com.madhackerdesigns.jinder.models.User;

public class CampfireTest extends JinderTest {
  
  private static Campfire campfire;

  @BeforeClass
  public static void loadNewCampfire() {
    campfire = new Campfire("test", "mytoken");
  }
  
  @Test
  public void returnsTheAvailableRooms() throws IOException {
    setRoomsFixture();
    List<Room> rooms = campfire.rooms();
    assertEquals(2, rooms.size());
  }
  
  @Test
  public void setsTheRoomNameAndId() throws IOException {
    setRoomsFixture();
    Room room = campfire.rooms().get(0);
    assertEquals("Room 1", room.name);
    assertEquals(80749, room.id);
  }
  
  @Test
  public void returnsRoomWhenMatchFoundById() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomById(80749);
    assertEquals("Room 1", room.name);
  }
  
  @Test
  public void returnsNullWhenMatchNotFoundById() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomById(123);
    assertNull(room);
  }
  
  @Test
  public void returnsRoomWhenMatchFoundByName() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomByName("Room 1");
    assertEquals(80749, room.id);
  }
  
  @Test
  public void returnsNullWhenMatchNotFoundByName() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomByName("asdf");
    assertNull(room);
  }
  
  @Test
  public void returnsRoomWhenMatchFoundByGuestHash() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomByGuestHash("4c8fb");
    assertEquals("Room 2", room.name);
  }
  
  @Test
  public void returnsNullWhenMatchNotFoundByGuestHash() throws IOException {
    setRoomsFixture();
    Room room = campfire.findRoomByGuestHash("asdf");
    assertNull(room);
  }
  
  @Test
  public void returnsSortedListOfUsersInAllRooms() throws IOException {
    MockTransport mockTransport = new MockTransport();
    mockTransport.addResponse("GET", "/rooms.json", 200, fixture("rooms.json"));
    mockTransport.addResponse("GET", "/room/80749.json", 200, fixture("room_80749.json"));
    mockTransport.addResponse("GET", "/room/80751.json", 200, fixture("room_80751.json"));
    campfire.setHttpTransport(mockTransport);
    SortedSet<User> users = campfire.users();
    assertEquals(3, users.size());
    assertEquals("Jane Doe", users.first().name);
    assertEquals("John Doe", users.last().name);
  }
  
  @Test
  public void returnsCurrentUserInfoWhenMeRequested() throws IOException {
    campfire.setHttpTransport(new MockTransport("GET", "/users/me.json", 200, fixture("me.json")));
    User me = campfire.me();
    assertEquals("John Doe", me.name);
  }
  
  @Test
  public void fetchesAccountInformation() throws IOException {
    campfire.setHttpTransport(new MockTransport("GET", "/account.json", 200, fixture("account.json")));
    Account account = campfire.account();
    assertEquals("Your Company", account.name);
  }
  
  // private helpers
  
  private void setRoomsFixture() throws IOException {
    campfire.setHttpTransport(new MockTransport("GET", "/rooms.json", 200, fixture("rooms.json")));
  }

}
