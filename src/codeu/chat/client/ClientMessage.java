// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Message;
import codeu.chat.util.Logger;
import codeu.chat.util.Method;
import codeu.chat.util.Uuid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ClientMessage {

  private final static Logger.Log LOG = Logger.newLog(ClientMessage.class);

  private final static int MESSAGE_MAX_COUNT = 100;
  private final static int MESSAGE_FETCH_COUNT = 5;
  private final Controller controller;
  private final View view;

  private Message current = null;

  private final Map<Uuid, Message> messageByUuid = new HashMap<>();

  private Conversation conversationHead;
  private final List<Message> conversationContents = new ArrayList<>();

  private final ClientUser userContext;
  private final ClientConversation conversationContext;

  public ClientMessage(Controller controller, View view, ClientUser userContext,
                       ClientConversation conversationContext) {
    this.controller = controller;
    this.view = view;
    this.userContext = userContext;
    this.conversationContext = conversationContext;
    this.conversationContext.setMessageContext(this);
  }

  // Validate the message body.
  public static boolean isValidBody(String body) {
    boolean clean = true;
    if ((body.length() <= 0) || (body.length() > 1024)) {
      clean = false;
    } else {

      // TODO: check for invalid characters

    }
    return clean;
  }

  public boolean hasCurrent() {
    return (current != null);
  }

  public Message getCurrent() {
    updateMessages(true);
    resetCurrent(true);
    return current;
  }

  public void showCurrent() {
    printMessage(current, userContext);
  }


  public void resetCurrent(boolean replaceAll) {
    updateMessages(replaceAll);
  }

  public int currentMessageCount() {
    return (conversationContents == null) ? 0 : conversationContents.size();
  }

  public List<Message> getConversationContents(ConversationSummary summary) {
    if (conversationHead == null || summary == null || !conversationHead.id.equals(summary.id)) {
      updateMessages(summary, true);
    }
    return conversationContents;
  }

  // For m-add command.
  public void addMessage(Uuid author, Uuid conversation, String body) {
    final boolean validInputs = isValidBody(body) && (author != null) && (conversation != null);

    final Message message = (validInputs) ? controller.newMessage(author, conversation, body) : null;

    if (message == null) {
      System.out.format("Error: message not created - %s.\n",
              (validInputs) ? "server error" : "bad input value");
    } else {
      LOG.info("New message:, Author= %s UUID= %s", author, message.id);
      current = message;
    }
    updateMessages(false);
  }


  // Diplay all messages a user has sent by using the user's username
  public void searchByUser(String user){

    ArrayList<Message>  mess =   controller.searchByUserID(user);

    if(!mess.isEmpty()){
      for(Message m : mess){System.out.println(" Time: " + m.creation + " Content "  + m.content);}
    }
    else System.out.println("User has no messages to display");
  }

  // Display all messages sent containing a specified hashtag
  public ArrayList<Message> searchByTag(String tag){

    return  controller.searchByTag(tag);
  }

  // Delete message, removes last message
  // m-del-last command
  public void deleteMessage() {
    if (currentMessageCount() == 0) {
      System.out.println("Current Conversation has no messages.");

    } else {
      if (controller.deleteMessage(conversationHead.lastMessage, conversationHead.id)) {
        resetCurrent(currentMessageCount() == 1);
        LOG.info("Deleted message: UUID= %s", conversationHead.lastMessage);

      } else {
        LOG.error("Error: Message could not be deleted.");

      }
    }
    updateMessages(true);
  }

  // Delete message, removes message corresponding to given index, (m-delete <index> command)
  // Calls helper method
  public void deleteMessage(String index) {
    if (index.matches("[0-9]+")) {
      int msgIndex = Integer.valueOf(index);

      if (msgIndex == (currentMessageCount() - 1)) {
        // Message to be deleted is the last message in the conversation,
        // so m-del-last command is called
        deleteMessage();

      } else {
        if (msgIndex < currentMessageCount()) {
          Message msg = conversationContents.get(msgIndex);
          deleteMessage(msg);
          updateMessages(true);
          System.out.format("Deleted message: UUID= %s\n", msg.id);
          LOG.info("Deleted message: UUID= %s", msg.id);

        } else {
          System.out.println(" Error: message not found, please enter a valid index.");
          LOG.error(" Error: message not found, please enter a valid index.");
        }
      }
    } else {
      System.out.println("Error: message not deleted, please provide a number index.");
      LOG.error("Error: message not deleted, please provide a number ndex.");

    }
    updateMessages(true);
  }

  // Delete message helper method
  private void deleteMessage(Message msg) {
    if (currentMessageCount() == 0) {
      LOG.error("Error: Message could not be deleted, current Conversation has no messages");

    } else {
        controller.deleteMessage(msg.id, conversationHead.id);
    }
    updateMessages(true);
  }

  // Delete all messages
  public void deleteAllMessages() {
    updateMessages(true);
    if(conversationContents.size() == 0) {
      System.out.println("Current Conversation has no messages.");

    } else {
      for (int i = conversationContents.size() - 1; i > -1; i--) {
        deleteMessage(String.valueOf(i));
      }
    }
    updateMessages(true);
  }

  // For m-list-all command.
  // Show all messages attached to the current conversation. This will balk if the conversation
  // has too many messages (use m-next and m-show instead).
  public void showAllMessages() {
    updateMessages(true);
    if (conversationContents.size() == 0) {
      System.out.println("Current Conversation has no messages.");

    } else {
      for (final Message m : conversationContents) {
        printMessage(m, userContext);
      }
    }
  }

  // For m-next command.
  // Accept an index (within the current stream) that indicates the next message to show.
  // Message 1 is the head of the Conversation's message chain.
  // Message -1 is the tail of the Conversation's message chain.
  public void selectMessage(int index) {
    Method.notImplemented();
  }

  // Processing for m-show command.
  // Accept an int for number of messages to attempt to show (1 by default).
  // Negative values go from newest to oldest.
  public void showMessages(int count) {
    updateMessages(true);
    for (final Message m : conversationContents) {
      printMessage(m, userContext);
    }
  }



  private void showNextMessages(int count) {
    Method.notImplemented();
  }

  private void showPreviousMessages(int count) {
    Method.notImplemented();
  }

  // Determine the next message ID of the current conversation to start pulling.
  // This requires a read of the last read message to determine if the chain has been extended.
  private Uuid getCurrentMessageFetchId(boolean replaceAll) {
    if (replaceAll || conversationContents.isEmpty()) {
      // Fetch/refetch all the messages.
      conversationContents.clear();
      LOG.info("Refetch all messages: replaceAll=%s firstMessage=%s", replaceAll,
              conversationHead.firstMessage);
      return conversationHead.firstMessage;
    } else {
      // Locate last known message. Its next, if any, becomes our starting point.
      return getCurrentTailMessageId();
    }
  }

  private Uuid getCurrentTailMessageId() {
    Uuid nextMessageId = conversationContents.get(conversationContents.size() - 1).id;

    final List<Message> messageTail = new ArrayList<>(view.getMessages(nextMessageId, 1));

    if (messageTail.size() > 0) {
      final Message msg = messageTail.get(0);
      nextMessageId = msg.next;

    } else {
      // fall back.
      LOG.warning("Failed to get tail of messages, starting from %s", nextMessageId);
      conversationContents.clear();
      nextMessageId = conversationHead.firstMessage;

    }
    return nextMessageId;
  }

  // Update the list of messages for the current conversation.
  // Currently rereads the entire message chain.
  public void updateMessages(boolean replaceAll) {
    updateMessages(conversationContext.getCurrent(), replaceAll);

  }

  // Update the list of messages for the given conversation.
  // Currently rereads the entire message chain.
  public void updateMessages(ConversationSummary conversation, boolean replaceAll) {

    if (conversation == null) {
      LOG.error("conversation argument is null - do nothing.");

      return;
    }
    conversationHead = conversationContext.getConversation(conversation.id);



    if (conversationHead == null) {
      LOG.info("ConversationHead is null");

    } else {

      LOG.info("ConversationHead: Title=\"%s\" UUID=%s first=%s last=%s\n",
              conversationHead.title, conversationHead.id, conversationHead.firstMessage,
              conversationHead.lastMessage);

      Uuid nextMessageId = getCurrentMessageFetchId(replaceAll);

      //  Stay in loop until all messages read (up to safety limit)

      while (!nextMessageId.equals(Uuid.NULL) && conversationContents.size() < MESSAGE_MAX_COUNT) {

        for (final Message msg : view.getMessages(nextMessageId, MESSAGE_FETCH_COUNT)) {
          conversationContents.add(msg);

          // Race: message possibly added since conversation fetched.  If that occurs,
          // pretend the newer messages do not exist - they'll get picked up next time).
          if (msg.next.equals(Uuid.NULL) || msg.id.equals(conversationHead.lastMessage)) {
            msg.next = Uuid.NULL;
            break;
          }
        }
        nextMessageId = conversationContents.get(conversationContents.size() - 1).next;
      }


      LOG.info("Retrieved %d messages for conversation %s (%s).\n",
              conversationContents.size(), conversationHead.id, conversationHead.title);



      // Set current to first message of conversation.
      current = (conversationContents.size() > 0) ? conversationContents.get(0) : null;


    }
  }

  // Print Message.  User context is used to map from author UUID to name.
  public static void printMessage(Message m, ClientUser userContext) {
    if (m == null) {
      System.out.println("Null Message!");
    } else {

      // Display author name if available.  Otherwise display the author UUID.
      final String authorName = (userContext == null) ? null : userContext.getName(m.author);

      System.out.format(" Author: %s   Id: %s created: %s\n   Body: %s\n",
              (authorName == null) ? m.author : authorName, m.id, m.creation, m.content);
    }
  }

  // Print Message outside of user context.
  public static void printMessage(Message m) {
    printMessage(m, null);
  }
}
