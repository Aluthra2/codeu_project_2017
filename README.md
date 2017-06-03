
# CODEU CHAT SERVER | README


## DISCLAIMER

CODEU is a program created by Google to develop the skills of future software
engineers. This project is not an official Google Product. This project is a
playground for those looking to develop their coding and software engineering
skills.


## ENVIRONMENT

All instructions here are relative to a LINUX environment. There will be some
differences if you are working on a non-LINUX system. We will not support any
other development environment.

This project was built using JAVA 7. It is recommended that you install
JAVA&nbsp;7 when working with this project.


## GETTING STARTED

  1. To build the project:
       ```
       $ sh clean.sh
       $ sh make.sh
       ```

  2. To test the project:
       ```
       $ sh test.sh
       ```

  3. To run the project you will need to run both the client and the server. Run
     the following two commands in separate shells: (Command Line Client is the client we chose to work with)

       ```
       $ sh run_server.sh 100.101 ABABAB 2007 bin
       $ sh run_client.sh localhost 2007
       ```

All running images write informational and exceptional events to log files.
The default setting for log messages is "INFO". You may change this to get
more or fewer messages, and you are encouraged to add more LOG statements
to the code. The logging is implemented in `codeu.chat.util.Logger.java`,
which is built on top of `java.util.logging.Logger`, which you can refer to
for more information.

In addition to your team's client and server, the project also includes a
Relay Server and a script that runs it (`run_relay.sh`).
This is not needed to get started with the project.

## New Features added to project

  *If 'help' is written on the client the commands come up for the new features*

  New User Functionality
  1. Deleting user (u-delete [username])
     The client that created the user can also delete the user by passing in this command with the correct username.

  2. Nicknames on startup (u-add [username] {alias})
     When a user is created, a nickname can be assigned to that user on the command line.

  3. Nicknames set at a later time (u-set [alias] [username])
     Set a nickname for any user at any time using the above command.

  4. Nickname retrieval (u-get-alias [username])
     Returns the nickname of a given user.

  5. Handling Duplicate Usernames
     The same username cannot be used for multiple people. Protections are set up for this now.

  New Conversation Functionality
  1. Full Conversations can now be deleted (c-delete [title])
     Conversations can be deleted provided the title of the Conversation.

  New Messages Functionality
  1. Deleting a Message (m-delete [index])
     Can now delete any specific message provided the index of the message.

  2. Deleting the last message in the Conversation (m-del-last)
     Deletes the last message in the Conversation.

  3. Deleting all messages in the conversation but not the conversation itself. (m-del-all)
     Deletes all messages in the current conversation but not the conversation itself.

  4. Search for Messages from a specific user (searchByName [username])
     Returns all messages from the specific user.

  5. Search for Messages from a specific hashtag. (searchTag [#hashtagName])
     Returns all messages with that specific hashtag.

All Features are synced across clients.


## Finding your way around the project

All the source files (except test-related source files) are in
`./src/codeu/chat`.  The test source files are in `./test/codeu/chat`. If you
use the supplied scripts to build the project, the `.class` files will be placed
in `./bin`. There is a `./third_party` directory that holds the jar files for
JUnit (a Java testing framework). Your environment may or may not already have
this installed. The supplied scripts use the version in `./third_party`.

Finally, there are some high-level design documents in the project Wiki. Please
review them as they can help you find your way around the sources.



## Source Directories

The major project components have been separated into their own packages. The
main packages/directories under `src/codeu/chat` are:

### codeu.chat.client

Classes for building the two clients (`codeu.chat.ClientMain` and
`codeu.chat.SimpleGuiClientMain`).

### codeu.chat.server

Classes for building the server (`codeu.chat.ServerMain`).

### codeu.chat.relay

Classes for building the Relay Server (`codeu.chat.RelayMain`). The Relay Server
is not needed to get started.

### codeu.chat.common

Classes that are shared by the clients and servers.

### codeu.chat.util

Some basic infrastructure classes used throughout the project.
