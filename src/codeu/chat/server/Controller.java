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

package codeu.chat.server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

<<<<<<< HEAD
import codeu.chat.common.*;
=======
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
>>>>>>> Project_Review
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public Conversation newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final Conversation foundConversation = model.conversationById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
              Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
                      message.id :
                      foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;

      if (!foundConversation.users.contains(foundUser)) {
        foundConversation.users.add(foundUser.id);
      }
    }

    return message;
  }

  @Override
  public boolean deleteMessage(Uuid msg, Uuid conversation) {
    final Message foundMessage = model.messageById().first(msg);
    final Conversation foundConversation = model.conversationById().first(conversation);
    final User foundUser = model.userById().first(foundMessage.author);

    Iterator<Message> iteratorBefore;
    Iterator<Message> iteratorAfter;

    boolean success = true;

<<<<<<< HEAD

    if (foundMessage != null && foundUser != null && foundConversation != null) {
      model.delete(foundMessage);

      if (Uuids.equals(foundConversation.lastMessage, msg)) {
        // This message was the conversation's last one

        if (Uuids.equals(foundConversation.firstMessage, msg)) {
          // This message was the conversation's only message now the conversation will have no
          // messages in it, therefore the first and last message are NULL
          foundConversation.firstMessage = Uuids.NULL;
          foundConversation.lastMessage = Uuids.NULL;
=======
      LOG.info("foundConversation.lastMessage: " + foundConversation.lastMessage);
      LOG.info("msg: " + msg);
      LOG.info("Uuids.equals(foundConversation.lastMessage, msg): " + Uuid.equals(foundConversation.lastMessage, msg));

      LOG.info("foundConversation.firstMessage: " + foundConversation.firstMessage);
      LOG.info("msg: " + msg);
      LOG.info("Uuids.equals(foundConversation.firstMessage, msg): " + Uuid.equals(foundConversation.firstMessage, msg));

      if (Uuid.equals(foundConversation.lastMessage, msg)) {

        // The deleted message was the last one, change the previous message's next field to NULL
        LOG.info("Entered if-branch where Uuids.equals(foundConversation.lastMessage, msg) is TRUE");

        if (Uuid.equals(foundConversation.firstMessage, msg)) {
          LOG.info("Within if-branch that means there was only one message in the conversation");
          // If the deleted message was the last one, and it's previous field was NULL
          // the deleted message was the only message in the conversation

          foundConversation.lastMessage = Uuid.NULL;
          model.delete(foundMessage);
          LOG.info("Message deleted: %s", msg);
>>>>>>> Project_Review

          LOG.info("Message deleted: %s", msg);

        } else {
          // This message was the conversation's last message, but not the first one
          // Update the last message value to penultimate message,
          // and pointer of the penultimate message to null
          iteratorBefore = model.messageByTime().before(foundMessage.creation).iterator();
          final Message newLastMessage = findNewLastMessage(iteratorBefore);

          newLastMessage.next = Uuids.NULL;
          foundConversation.lastMessage = newLastMessage.id;

          LOG.info("Message deleted: %s", msg);

        }

<<<<<<< HEAD
      } else {
=======
      } else if (Uuid.equals(foundConversation.firstMessage, msg)) {
        LOG.info("Within if-branch that means the deleted message was the first one, and not the only one");
        System.out.println("Null?: foundMessage.next" + Uuid.equals(foundMessage.next, Uuid.NULL));
>>>>>>> Project_Review

        if (Uuids.equals(foundConversation.firstMessage, msg)) {
          // This message was the first message in a conversation that has other messages
          // Update the first message value to the second message,
          // update the pointer of the second message
          iteratorAfter = model.messageByTime().after(foundMessage.creation).iterator();
          final Message newFirstMessage = findNewFirstMessage(iteratorAfter);

          newFirstMessage.previous = Uuids.NULL;
          foundConversation.firstMessage = newFirstMessage.id;

          LOG.info("Message deleted: %s", msg);

        } else {
          // This message was not the first, nor the last message and there are more messages
          // Update the pointers
          iteratorBefore = model.messageByTime().before(foundMessage.creation).iterator();
          iteratorAfter = model.messageByTime().after(foundMessage.creation).iterator();
          final Message newPrevMessage = findNewLastMessage(iteratorBefore);
          final Message newNextMessage = findNewFirstMessage(iteratorAfter);

          newNextMessage.previous = newPrevMessage.id;
          newPrevMessage.next = newNextMessage.id;
          LOG.info("Message deleted: %s", msg);

        }
      }

    } else {
      success = false;
      LOG.info("Error: Message not deleted: %s", msg);
    }

    return success;
  }

  private Message findNewFirstMessage(Iterator<Message> iterator) {
    // Given an iterator of all the messages that came after the message
    // to be deleted, which was the first message in the conversation,
    // this method finds and returns the second message in the conversation
    Message first = null;

    if(iterator.hasNext()) {
      first = iterator.next();
    }
    return  first;
  }

  private Message findNewLastMessage(Iterator<Message> iterator) {
    // Given an iterator of all the messages that came before the message
    // to be deleted, which was the last message in the conversation,
    // this method finds and returns the second-to-last message in the conversation
    Message last = null;

    while (iterator.hasNext()) {
      last = iterator.next();

    }

    return last;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime);
      model.add(user);

      LOG.info(
              "newUser success (user.id=%s user.name=%s user.time=%s)",
              id,
              name,
              creationTime);

    } else {

      LOG.info(
              "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
              id,
              name,
              creationTime);
    }

    return user;
  }

  @Override
  public Conversation newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    Conversation conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new Conversation(id, owner, creationTime, title);
      model.add(conversation);

      LOG.info("Conversation added: " + conversation.id);
    }

    return conversation;
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

      // Assuming that "randomUuid" is actually well implemented, this
      // loop should never be needed, but just incase make sure that the
      // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
            model.conversationById().first(id) != null ||
            model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

}
