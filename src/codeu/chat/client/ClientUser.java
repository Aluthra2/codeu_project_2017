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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import codeu.chat.common.User;
import codeu.chat.common.Uuid;
import codeu.chat.util.Logger;
import codeu.chat.util.store.Store;

public final class ClientUser {

  private final static Logger.Log LOG = Logger.newLog(ClientUser.class);

  private static final Collection<Uuid> EMPTY = Arrays.asList(new Uuid[0]);
  private final Controller controller;
  private final View view;

  private User current = null;

  private final Map<Uuid, User> usersById = new HashMap<>();

  // This is the set of users known to the server, sorted by name.
  private static Store<String, User> usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

  public ClientUser(Controller controller, View view) {
    this.controller = controller;
    this.view = view;
  }

  // Validate the username string
  static public boolean isValidName(String userName) { //Check for Duplicates
    boolean clean = true;
    if (userName.length() == 0) {
      clean = false;
    } else {
      //clean = usersByName.containsKey(userName)? false : true;
      // TODO: check for invalid characters - User RegEx(String replacement)

    }
    return clean;
  }

  public boolean hasCurrent() {
    return (current != null);
  }

  public User getCurrent() {
    return current;
  }

  public boolean signInUser(String name) {
    updateUsers();

    final User prev = current;
    if (name != null) {
      final User newCurrent = usersByName.first(name);
      if (newCurrent != null) {
        current = newCurrent;
      }
    }
    return (prev != current);
  }

  public boolean signOutUser() {
    boolean hadCurrent = hasCurrent();
    current = null;
    return hadCurrent;
  }

  public void showCurrent() {
    printUser(current);
  }

//Set it up so that it works if an alias is entered!
  public void addUser(String name) {
    final boolean validInputs = isValidName(name);

    final User user = (validInputs) ? controller.newUser(name) : null;

    if (user == null) {
      System.out.format("Error: user not created - %s.\n",
          (validInputs) ? "server failure" : "bad input value");
    } else {
      LOG.info("New user complete, Name= \"%s\" UUID=%s", user.name, user.id);
      updateUsers();
    }
  }

  //Deleting from Map not System yet - Figure out how to delete from System
  public void deleteUser(String name){
    if(usersById.containsValue(name)){
      for(Map.Entry<Uuid, User> entry: usersById.entrySet()){
        Uuid id = entry.getKey();
        User user = entry.getValue();
        if(user.name == name){
          usersById.remove(id);
        //  usersByName.remove(user.name);
          }
        }
      }
    }


  public void showAllUsers() {
    updateUsers();
    for (final User u : usersByName.all()) {
      printUser(u);
    }
  }

  public User lookup(Uuid id) {
    return (usersById.containsKey(id)) ? usersById.get(id) : null;
  }

  public String getName(Uuid id) {
    final User user = lookup(id);
    if (user == null) {
      LOG.warning("userContext.lookup() failed on ID: %s", id);
      return null;
    } else {
      return user.name;
    }
  }

//Set it up so that it works for any user not just current user
  public String getAlias(){
    final User user = getCurrent();
    if (user != null){
      return ("NULL");
    } else {
      return user.alias;
    }
  }

//Set it up so that it works for any user not just current user
  public void setAlias(String nickname){
    final User user = getCurrent();
    if (user != null){
      user.alias = nickname;
      LOG.info("New user alias complete, Name= \"%s\" UUID=%s Alias = %s", user.name, user.id, user.alias);
    }
  }

  public Iterable<User> getUsers() {
    return usersByName.all();
  }

  public void updateUsers() {
    usersById.clear();
    usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

    for (final User user : view.getUsersExcluding(EMPTY)) {
      usersById.put(user.id, user);
      usersByName.insert(user.name, user);
    }
  }

 public void updateUsers(Collection<Uuid> deletion) {
    usersById.clear();
    usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

    for (final User user : view.getUsersExcluding(deletion)) {
      usersById.put(user.id, user);
      usersByName.insert(user.name, user);
    }
  }


  public static String getUserInfoString(User user) {
    return (user == null) ? "Null user" :
        String.format(" User: %s\n   Id: %s\n Created: %s\n Alias: %s\n", user.name, user.id, user.creation, user.alias);
  }

  public String showUserInfo(String uname) {
    return getUserInfoString(usersByName.first(uname));
  }

  // Move to User's toString()
  public static void printUser(User user) {
    System.out.println(getUserInfoString(user));
  }
}
