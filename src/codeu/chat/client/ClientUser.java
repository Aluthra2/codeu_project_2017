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
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class ClientUser {

  private final static Logger.Log LOG = Logger.newLog(ClientUser.class);

  private static final Collection<Uuid> EMPTY = Arrays.asList(new Uuid[0]);
  private final Controller controller;
  private final View view;

  private User current = null;

  private final Map<String, User> userNames = new HashMap<>();
  private final Map<Uuid, User> usersById = new HashMap<>();

  // This is the set of users known to the server, sorted by name.
  private static Store<String, User> usersByName = new Store<>(String.CASE_INSENSITIVE_ORDER);

  public ClientUser(Controller controller, View view) {
    this.controller = controller;
    this.view = view;
  }

  // Validate the username string
  static public boolean isValidName(String userName) {
    boolean clean = true;
    if (userName.length() == 0) {
      clean = false;
    } else {
      //clean = usersByName.containsKey(userName)? false : true;
      // TODO: check for invalid characters - User RegEx(String replacement)

    }
    return clean;
  }

  //Checks for Duplicate User
  public boolean duplicateUser(String uName){
    User user = usersByName.first(uName);
    if (user == null){
      return false;
    } else {
      return true;
    }
  }

  public boolean hasCurrent() {
    return (current != null);
  }

  public User getCurrent() {
    updateUsers();
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


  // Adding a User
  public void addUser(String name) {
    updateUsers();
    final boolean validInputs = isValidName(name);
    final boolean duplicates = duplicateUser(name);
    final boolean validAndNotDuplicate = (!duplicates && validInputs);

    final User user = validAndNotDuplicate ? controller.newUser(name) : null;

    if (user == null) {
      System.out.format("Error: user not created - %s.\n",
          validAndNotDuplicate ? "bad input vale" : ((duplicates) ? "Username already taken" : "Server Failure!"));
    } else {
      LOG.info("New user complete, Name= \"%s\" UUID=%s", user.name, user.id);
      userNames.put(user.name, user);
      updateUsers();
    }
  }


  // Adding a User with a nickName - Overloaded Method
  public void addUser(String name, String nickName) {
    updateUsers();
    final boolean validInputs = isValidName(name);
    final boolean duplicates = duplicateUser(name);
    final boolean validAndNotDuplicate = (!duplicates && validInputs);

    final User user = validAndNotDuplicate ? controller.newUser(name, nickName) : null;

    if (user == null) {
      System.out.format("Error: user not created - %s.\n",
          validAndNotDuplicate ? "bad input vale" : ((duplicates) ? "Username already taken" : "Server Failure!"));
    } else {
      LOG.info("New user complete, Name= \"%s\" UUID=%s", user.name, user.id);
      user.alias = nickName;
      userNames.put(user.name, user);
      updateUsers();
    }
  }

  // Deleting a User
  public void deleteUser(String name){
    if(userNames.containsKey(name)){
      User userObject = userNames.get(name);
      User removeUser = controller.deleteUser(name);
      if(removeUser == null){
        System.out.println("ERROR: NO USER FOUND");
      } else {
        LOG.info("User Deleted, Name=\"%s\" UUID=%s", removeUser.name, removeUser.id);
        userNames.remove(removeUser.name);
        updateUsers();
      }
    } else {
      System.out.println("ERROR: NO USER FOUND");
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

  // Get the nickname of User
  public String getAlias(String name){
    updateUsers();
    try{
      final User user = usersByName.first(name);
      System.out.println(user.alias);
      return user.alias;
    } catch(Exception ex){
      System.out.println("No Such User Exists!");
        return "No Such User Exists!";
  }
}

  // Set the nickaname of a User.
  public void setAlias(String nickname, String uName){
    updateUsers();
    try{
      final User user = usersByName.first(uName);
      boolean result = controller.setAlias(user, nickname);
      if (result){
        LOG.info("New user alias complete, Name= \"%s\" UUID=%s Alias = %s", user.name, user.id, user.alias);
        updateUsers();
      } else {
        LOG.info("Something Went Wrong!");
      }
    } catch(Exception ex){
        System.out.println("No Such User Exists!");
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
