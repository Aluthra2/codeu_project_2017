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

import java.util.Comparator;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.Logger;
import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Model {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);


  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) { return 0; }

      if (a == null && b != null) { return -1; }

      if (a != null && b == null) { return 1; }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };

  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;

  private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  protected final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Conversation> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Time, Conversation> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Conversation> conversationByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);
  protected final HashMap<String, ArrayList<Message>> messageByUserID = new HashMap<>();
  protected final HashMap<String, ArrayList<Message>> tags = new HashMap<>();

  private final Uuid.Generator userGenerations = new LinearUuidGenerator(null, 1, Integer.MAX_VALUE);
  private Uuid currentUserGeneration = userGenerations.make();

  public void add(User user) {
    currentUserGeneration = userGenerations.make();

    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
  }

  public void remove(User user){
    userById.remove(user.id);
    userByTime.remove(user.creation);
    userByText.remove(user.name);
  }

  public StoreAccessor<Uuid, User> userById() {
    return userById;
  }

  public StoreAccessor<Time, User> userByTime() {
    return userByTime;
  }

  public StoreAccessor<String, User> userByText() {
    return userByText;
  }

  public Uuid userGeneration() {
    return currentUserGeneration;
  }

  public void add(Conversation conversation) {
    conversationById.insert(conversation.id, conversation);
    conversationByTime.insert(conversation.creation, conversation);
    conversationByText.insert(conversation.title, conversation);
  }

  public StoreAccessor<Uuid, Conversation> conversationById() {
    return conversationById;
  }

  public void delete(Conversation conversation) {
    if(conversationById.contains(conversation.id)) {
      conversationById.delete(conversation.id);
      System.out.println(conversationById.all().toString());
    }

    if(conversationByTime.contains(conversation.creation)) {
      conversationByTime.delete(conversation.creation);

    }

    if(conversationByText.contains(conversation.title)) {
      conversationByText.delete(conversation.title);

    }
  }


  public StoreAccessor<Time, Conversation> conversationByTime() {
    return conversationByTime;
  }

  public StoreAccessor<String, Conversation> conversationByText() {
    return conversationByText;
  }

  public void add(Message message) {
    messageById.insert(message.id, message);
    messageByTime.insert(message.creation, message);
    messageByText.insert(message.content, message);
    if(messageByUserID.containsKey(message.author.toString())){
        messageByUserID.get(message.author.toString()).add(message);
    }
    else{

	ArrayList<Message> a = new ArrayList<>();
	a.add(message);
	messageByUserID.put(message.author.toString(), a);
	}

    Pattern hashtag = Pattern.compile("(#\\w+)\\b");
    Matcher tagCheck = hashtag.matcher(message.content);
    while(tagCheck.find()){
  
        if(tags.containsKey(tagCheck.group(1))){
	   tags.get(tagCheck.group(1)).add(message);

	}else{
	   ArrayList<Message> tagMessages = new ArrayList<>();
	   tagMessages.add(message);
	   tags.put(tagCheck.group(1), tagMessages);

	}         
    }
   
  }

   

  public void delete(Message message) {
    if(messageById.contains(message.id)) {
      messageById.delete(message.id);
    }

    if(messageByTime.contains(message.creation)) {
      messageByTime.delete(message.creation);
    }

    if(messageByText.contains(message.content)) {
      messageByText.delete(message.content);
    }

    if(messageByUserID.containsKey(message.author.toString())) {
      ArrayList<Message> a = new ArrayList<>();
      a = messageByUserID.get(message.author.toString());
      for(Message m : a){
        if(m.id == message.id){
           a.remove(m);
           break;
        }
      }
    }

    Pattern hashtag = Pattern.compile("(#\\w+)\\b");
    Matcher tagCheck = hashtag.matcher(message.content);
    while(tagCheck.find()){

      if(tags.containsKey(tagCheck.group(1))){
         tags.get(tagCheck.group(1)).remove(message);

      }
     }

  }
  public StoreAccessor<Uuid, Message> messageById() {
    return messageById;
  }

  public StoreAccessor<Time, Message> messageByTime() {
    return messageByTime;
  }

  public StoreAccessor<String, Message> messageByText() {
    return messageByText;
  }
}
