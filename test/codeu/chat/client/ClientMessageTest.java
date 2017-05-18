package codeu.chat.client;

import codeu.chat.common.*;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;
import org.junit.Before;
import org.junit.Test;


public final class ClientMessageTest {
 /*
  ConnectionSource source1 = new ClientConnectionSource("host", 1);
  Controller controller1 = new Controller(source1);

  User user1 = controller1.newUser("Alice");
  User user2 = controller1.newUser("Bob");
  User user3 = controller1.newUser("Carl");

  Conversation convo1 = controller1.newConversation("Hello World", user1.id);
  Conversation convo2 = controller1.newConversation("Goodbye Nothing", user3.id);

  Message msg1 = controller1.newMessage(user1.id, convo1.id, "Hello");
  Message msg2 = controller1.newMessage(user2.id, convo1.id, "World");

  Message msg3 = controller1.newMessage(user3.id, convo2.id, "Goodbye");

  @Test
  public void testAddMessage() {

  }


/*

  @Test
  public void testDeleteMessage() {



  }
*/
}
