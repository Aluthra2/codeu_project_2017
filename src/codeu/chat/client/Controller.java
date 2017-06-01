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
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import java.util.ArrayList;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean deleteMessage(Uuid msg, Uuid conversation) {
    boolean success = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), msg);
      Uuid.SERIALIZER.write(connection.out(), conversation);


      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_MESSAGE_RESPONSE) {
        success = true;
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      LOG.error(ex, "Exception during call on server.");
    }

    return success;
  }

  @Override
  public User newUser(String name) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      Serializers.STRING.write(connection.out(), "Not Entered");
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  public User newUser(String name, String nickName) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      Serializers.STRING.write(connection.out(), nickName);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User deleteUser(String name){
    User response = null;

    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("Delete User: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("deleteUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  public boolean setAlias(User user, String nickName){
    User response = null;

    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.NICKNAME_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user.id);
      Serializers.STRING.write(connection.out(), nickName);
      LOG.info("setAlias: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NICKNAME_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("setAlias: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response != null;
  }

  @Override
  public Conversation newConversation(String title, Uuid owner)  {

    Conversation response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean deleteConversation(Uuid conversation) {
    boolean success = true;

    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_CONVERSATION_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), conversation);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_CONVERSATION_RESPONSE) {
        success = true;

      } else {
        System.out.println("Response from server failed.");
        LOG.error("Response from server failed.");

      }
    } catch (Exception ex) {
      System.out.println("Exception during call on server.");
      LOG.error(ex, "Exception during call on server.");

    }

    return success;
  }
  
  //send to the server the user's name and receive the  messages by the user. This method returns the array of messages. 
  public ArrayList<Message> searchByUserID(String author){
    final ArrayList <Message> messagesbyuserid = new ArrayList<>();
    try (final Connection connection = source.connect()){
      Serializers.INTEGER.write(connection.out(), NetworkCode.SEARCHREQUEST);
      Serializers.STRING.write(connection.out(), author);
		
      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.SEARCHRESPONSE){
         messagesbyuserid.addAll(Serializers.collection(Message.SERIALIZER).read(connection.in()));
      }
	
     }catch(Exception ex){ System.out.println("ERROR: Exception during call on server. Check log for details.");}	

     return messagesbyuserid;
   }

  //This method sends a tag to the server and receives all the messages with this tag from the server.
  //This method returns an array of these messages 
  public ArrayList<Message> searchByTag(String tag){
    final ArrayList <Message> messagesByTag = new ArrayList<>();
    try (final Connection connection = source.connect()){
      Serializers.INTEGER.write(connection.out(), NetworkCode.TAGREQUEST);
      Serializers.STRING.write(connection.out(), tag);


      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.TAGRESPONSE){
         messagesByTag.addAll(Serializers.collection(Message.SERIALIZER).read(connection.in()));
      }
              
    }catch(Exception ex){ System.out.println("ERROR: Exception during call on server. Check log for details.");}

    return messagesByTag;
  }

}
