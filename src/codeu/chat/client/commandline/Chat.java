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

package codeu.chat.client.commandline;

import java.util.Scanner;
import java.util.ArrayList;
import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.client.View;
import codeu.chat.common.ConversationSummary;
import codeu.chat.util.Logger;
import codeu.chat.client.ClientUser;
import codeu.chat.util.Uuid;
import codeu.chat.common.Message;

// Chat - top-level client application.
public final class Chat {

  private final static Logger.Log LOG = Logger.newLog(Chat.class);

  private static final String PROMPT = ">>";

  private final static int PAGE_SIZE = 10;

  private boolean alive = true;

  private final ClientContext clientContext;

  // Constructor - sets up the Chat Application
  public Chat(Controller controller, View view) {
    clientContext = new ClientContext(controller, view);
  }

  // Print help message.
  private static void help() {
    System.out.println("Chat commands:");
    System.out.println("   exit      - exit the program.");
    System.out.println("   help      - this help message.");
    System.out.println("   sign-in <username>  - sign in as user <username>.");
    System.out.println("   sign-out  - sign out current user.");
    System.out.println("   current   - show current user, conversation, message.");
    System.out.println("User commands:");
    System.out.println("   u-add <name> [alias] - add a new user. [Optional Nickname]");
    System.out.println("   u-delete <name> - delete a User");
    System.out.println("   u-set <alias> <UserName> - add a nickname for a user.");
    System.out.println("   u-get-alias <UserName> - get the nickname of chosen user.");
    System.out.println("   u-list-all  - list all users known to system.");
    System.out.println("Conversation commands:");
    System.out.println("   c-add <title>    - add a new conversation.");
    System.out.println("   c-delete <title> - delete the conversation corresponding to the given title.");
    System.out.println("   c-list-all       - list all conversations known to system.");
    System.out.println("   c-select <index> - select conversation from list.");
    System.out.println("Message commands:");
    System.out.println("   m-add <body>     - add a new message to the current conversation.");
    System.out.println("   m-delete <index> - deletes message at a given index.");
    System.out.println("   m-del-last       - deletes the last message.");
    System.out.println("   m-del-all        - delete all messages in the Conversation.");
    System.out.println("   m-list-all       - list all messages in the current conversation.");
    System.out.println("   m-next <index>   - index of next message to view.");
    System.out.println("   m-show <count>   - show next <count> messages.");
    System.out.println("   searchByName <username>  - show all messages from user");
    System.out.println("   searchTag <#hashtagName>  - show all messages with specified hashtag");
 }

  // Prompt for new command.
  private void promptForCommand() {
    System.out.print(PROMPT);
  }

  // Parse and execute a single command.
  private void doOneCommand(Scanner lineScanner) {

    final Scanner tokenScanner = new Scanner(lineScanner.nextLine());
    if (!tokenScanner.hasNext()) {
      return;
    }
    final String token = tokenScanner.next();

    if (token.equals("exit")) {

      alive = false;

    } else if (token.equals("searchByName")) { //Serches for Messages by a specific User

     if (tokenScanner.hasNext()){
       clientContext.message.searchByUser(tokenScanner.nextLine().trim());
     }

   } else if (token.equals("searchTag")){ //Serches for Messages by a specific hashtag.

     if(tokenScanner.hasNext()){
       ArrayList<Message> messagesByTag = clientContext.message.searchByTag(tokenScanner.nextLine().trim());
       for(Message m : messagesByTag) {
         System.out.println("User: " + clientContext.user.getName(m.author) + " Time: " + m.creation + " Content "  + m.content);
       }
     }

    } else if (token.equals("help")) {

      help();

    } else if (token.equals("sign-in")) {

      if (!tokenScanner.hasNext()) {
        System.out.println("ERROR: No user name supplied.");
      } else {
        signInUser(tokenScanner.next());
      }

    } else if (token.equals("sign-out")) {

      if (!clientContext.user.hasCurrent()) {
        System.out.println("ERROR: Not signed in.");
      } else {
        signOutUser();
      }

    } else if (token.equals("current")) {

      showCurrent();

    } else if (token.equals("u-add")) { //Adds a user

      String userName = "";
      String nickName = "";
      if (!tokenScanner.hasNext()) { //Changed from .hasNextLine to .hasNext so 2 commands could be processed. Also eliminates spaces in usernames.
        System.out.println("ERROR: Username not supplied.");
      } else {
        userName = tokenScanner.next();
          if (tokenScanner.hasNext()) {
            nickName = tokenScanner.next();
            addUser(userName, nickName); //Adds a user with a nickName
          } else {
            addUser(userName); //Adds a user with no nickname
          }
      }

    } else if (token.equals("u-delete")){ //Deletes a user.
      if(!tokenScanner.hasNext()){
        System.out.println("ERROR: Username not supplied.");
      } else {
        deleteUser(tokenScanner.next());
      }

    } else if (token.equals("u-set")){ //Sets a nickName for a user.

      String uName = "";
      String nickName = "";

      if(!tokenScanner.hasNext()){
        System.out.println("ERROR: Alias not supplied");
      } else {
        nickName = tokenScanner.next();
      }
      if(!tokenScanner.hasNext()){
        System.out.println("ERROR: User Name not supplied");
      } else {
        uName = tokenScanner.next();
        setAlias(nickName, uName);
      }

    } else if (token.equals("u-get-alias")){ //returns a nickname for a user.
        getAlias(tokenScanner.next());

    } else if (token.equals("u-list-all")) {

      showAllUsers();

    } else if (token.equals("c-add")) {

      if (!clientContext.user.hasCurrent()) {
        System.out.println("ERROR: Not signed in.");
      } else {
        if (!tokenScanner.hasNext()) {
          System.out.println("ERROR: Conversation title not supplied.");
        } else {
          final String title = tokenScanner.nextLine().trim();
          clientContext.conversation.startConversation(title, clientContext.user.getCurrent().id);
        }
      }

    } else if (token.equals("c-delete")) { //Deletes a conversation.

      if (!clientContext.user.hasCurrent()) {
        System.out.println("ERROR: Not signed in.");

      } else if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");

      } else {
        if (!tokenScanner.hasNext()) {
          System.out.println("ERROR: Conversation title not supplied.");
        } else {
          final String title = tokenScanner.nextLine().trim();
          clientContext.conversation.deleteConversation(title);
        }
      }
    }

    else if (token.equals("c-list-all")) {

      clientContext.conversation.showAllConversations();



    } else if (token.equals("c-select")) {

      selectConversation(lineScanner);

    } else if (token.equals("m-add")) {

      if (!clientContext.user.hasCurrent()) {
        System.out.println("ERROR: Not signed in.");
      } else if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        if (!tokenScanner.hasNext()) {
          System.out.println("ERROR: Message body not supplied.");
        } else {
          clientContext.message.addMessage(clientContext.user.getCurrent().id,
              clientContext.conversation.getCurrentId(),
              tokenScanner.nextLine().trim());
        }
      }

    } else if (token.equals("m-delete")) { //Deletes a message at a specific index (Starts at 0).
      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        if (!tokenScanner.hasNext()) {
          System.out.println("ERROR: Message index not supplied.");
        } else {
          clientContext.message.deleteMessage(tokenScanner.nextLine().trim());
        }
      }

    } else if (token.equals("m-del-last")) { //Deletes the last message.
      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        clientContext.message.deleteMessage();
      }

    } else if (token.equals("m-del-all")) { //Deletes all messages but not the conversation.
      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        clientContext.message.deleteAllMessages();
      }
    }

    else if (token.equals("m-list-all")) {

      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        clientContext.message.showAllMessages();
      }

    } else if (token.equals("m-next")) {

      // TODO: Implement m-next command to jump to an index in the message chain.
      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else if (!tokenScanner.hasNextInt()) {
        System.out.println("Command requires an integer message index.");
      } else {
        clientContext.message.selectMessage(tokenScanner.nextInt());
      }

    } else if (token.equals("m-show")) {

      // TODO: Implement m-show command to show N messages (currently just show all)
      if (!clientContext.conversation.hasCurrent()) {
        System.out.println("ERROR: No conversation selected.");
      } else {
        final int count = (tokenScanner.hasNextInt()) ? tokenScanner.nextInt() : 1;
        clientContext.message.showMessages(count);
      }

    } else {

      System.out.format("Command not recognized: %s\n", token);
      System.out.format("Command line rejected: %s%s\n", token,
              (tokenScanner.hasNext()) ? tokenScanner.nextLine() : "");
      System.out.println("Type \"help\" for help.");
    }
    tokenScanner.close();
  }

  // Sign in a user.
  private void signInUser(String name) {
    if (!clientContext.user.signInUser(name)) {
      System.out.println("Error: sign in failed (invalid name?)");
    }
  }

  // Sign out a user.
  private void signOutUser() {
    if (!clientContext.user.signOutUser()) {
      System.out.println("Error: sign out failed (not signed in?)");
    }
  }

  // Helper for showCurrent() - show message info.
  private void showCurrentMessage() {
    if (clientContext.conversation.currentMessageCount() == 0) {
      System.out.println(" -- no messages in conversation --");
    } else {
      System.out.format(" conversation has %d messages.\n",
              clientContext.conversation.currentMessageCount());
      if (!clientContext.message.hasCurrent()) {
        System.out.println(" -- no current message --");
      } else {
        System.out.println("\nCurrent Message:");
        clientContext.message.showCurrent();
      }
    }
  }

  // Show current user, conversation, message, if any
  private void showCurrent() {
    boolean displayed = false;
    if (clientContext.user.hasCurrent()) {
      System.out.println("User:");
      clientContext.user.showCurrent();
      System.out.println();
      displayed = true;
    }

    if (clientContext.conversation.hasCurrent()) {
      System.out.println("Conversation:");
      clientContext.conversation.showCurrent();

      showCurrentMessage();

      System.out.println();
      displayed = true;
    }

    if (!displayed) {
      System.out.println("No current user or conversation.");
    }
  }

  // Display current user.
  private void showCurrentUser() {
    if (clientContext.user.hasCurrent()) {
      clientContext.user.showCurrent();
    } else {
      System.out.println("No current user.");
    }
  }

  // Display current conversation.
  private void showCurrentConversation() {
    if (clientContext.conversation.hasCurrent()) {
      clientContext.conversation.showCurrent();
    } else {
      System.out.println(" No current conversation.");
    }
  }

  // Add a new user.
  private void addUser(String name) {
    clientContext.user.addUser(name);
  }

  // Overloaded method
  // Add a new user with a nickname
  private void addUser(String name, String nickname) {
    clientContext.user.addUser(name, nickname);
  }

  // Delete a User
  private void deleteUser(String name){
    clientContext.user.deleteUser(name);
  }

  // Get Alias of user
  private void getAlias(String name) {
    clientContext.user.getAlias(name);
  }

  // Set Alias of a user
  private void setAlias(String nickname, String id){
    clientContext.user.setAlias(nickname, id);

  }

  // Display all users known to server.
  private void showAllUsers() {
    clientContext.user.showAllUsers();
  }

  // Handles command
  public boolean handleCommand(Scanner lineScanner) {

    try {
      promptForCommand();
      doOneCommand(lineScanner);
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during command processing. Check log for details.");
      LOG.error(ex, "Exception during command processing");
    }

    // "alive" may have been set to false while executing a command. Return
    // the result to signal if the user wants to keep going.

    return alive;
  }

  // Selects Conversation
  public void selectConversation(Scanner lineScanner) {

    clientContext.conversation.updateAllConversations(false);
    final int selectionSize = clientContext.conversation.conversationsCount();
    System.out.format("Selection contains %d entries.\n", selectionSize);

    final ConversationSummary previous = clientContext.conversation.getCurrent();
    ConversationSummary newCurrent = null;

    if (selectionSize == 0) {
      System.out.println("Nothing to select.");
    } else {
      final ListNavigator<ConversationSummary> navigator =
              new ListNavigator<ConversationSummary>(
                      clientContext.conversation.getConversationSummaries(),
                      lineScanner, PAGE_SIZE);
      if (navigator.chooseFromList()) {
        newCurrent = navigator.getSelectedChoice();
        clientContext.message.resetCurrent(newCurrent != previous);
        System.out.format("OK. Conversation \"%s\" selected.\n", newCurrent.title);
      } else {
        System.out.println("OK. Current Conversation is unchanged.");
      }
    }
    if (newCurrent != previous) {
      clientContext.conversation.setCurrent(newCurrent);
      clientContext.conversation.updateAllConversations(true);
    }
  }
}
