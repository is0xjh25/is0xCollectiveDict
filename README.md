# is0xCollectiveDict
 
## Table of Contents
* [About the Application](#about-the-application)
* [Getting Started](#getting-started)
* [Usage](#usage)
* [Modules and Classes](#modules-and-classes)
* [Excellence](#excellence)
* [Creativity](#creativity)
* [Demo](#demo)
* [Developed By](#developed-by)

## About the Application
_Is0xCollectiveDict_ is a dictionary application which includes the server and the client side. The server is built on multi-threading architecture which has the ability of handling concurrency TCP connection and queries from the users. Every data of the dictionary is stored on MongoDB which is stable and reliable. Moreover, the GUI of the client application is developed by _Swing_ to improve the user experience.

## Getting Started
- Start up the server:
 >In terminal: `java –jar server.jar <port>`<br>
 >Enter the MongoDB Password: `<password>`
 
 Type the first command in the terminal (the port argument is optional). If there is no valid argument, the default port would be `4444`. Type the password for connecting to the database after the promote appears. To run the server, database access is required. Applying for the password or any connection issues, Please contact is0.jimhsiao@gmail.com<br><br>
	<sup><sub>*Even the password is granted, the database connection is still protected by MongoDB Network Access. It means that the IP address needs to be permitted in the MongoDB setting to access the database.</sub></sup>

- Open the graphic user interface on the client side:
 >In terminal: `java –jar client.jar <server-address> <server-port>`
 
 Type the above command in the terminal (the address and the port argument are optional). The address and port arguments are optional. If there is no valid argument, the default IP and port would be `localhost:4444`.

## Usage
- **Connect**<br>
The connection between the client and the server uses TCP. For the server, it can set up the listening socket by choosing its own port. In this task, the server utilizes _ServerSocket_ to ensure the socket is reusable which can handle multi-connection at the same time. On the client side, it is allowed to connect any IP and Port.

- **Search**<br>
It returns the meanings of the word in a text box which is scrollable when the word is recorded in the database. Otherwise, it returns a not found message with a searching suggestion.

- **Add**<br>
Adding a new word to the dictionary if the word is not found and the meaning cannot be empty.

- **Update**<br>
It is designed for editing the meanings of a recorded vocabulary.

- **Remove**<br>
It deletes a word and all its associated meanings from the dictionary. A word removed by one client should not be visible to any of the clients of the dictionary server.

## Modules and Classes
- **Shard**
	- **_Query_**<br>
_Query_ is a helper class that formats all information for input and output stream during the connection. A single query has two types which are request or response. Based on the type of query, it is going to store different data.<br><br>
In a request query, it records:<br>
`Query [Action (connect/search/add/update/remove), Word, Definition]`<br>
In a response query, it records:<br>
`Query [Status (success/error), Code (e.g., 404), Content, Suggestion]`<br><br>
Since the TCP connection works by reading and writing string, _Query_ can analyse the _String_ and transforms the information to the corresponding attributes. Therefore, every _String_ is going to be a _Query_ object, and its attributes can be easily accessed. To some extent, it ensures the format of the communication is regulated and it makes the whole program more reliable.<br><br>
<sup><sub>*Query is zipped as a jar, and it is plugged in the server and the client module as a library.</sub></sup>

- **Server**
	- **_DictionaryServer_**<br>
It is designed for running a server which also supports multi-threading for connecting to different clients. When it receives a query, it will use _handleQuery()_ to analyse the incoming string, and calls out the method in _Dictionary_ that the query is asking for. In addition, the _handleQuery()_ method is synchronized to deal with the potential concurrency issue in multi-threading.

	- **_Dictionary_**<br>
	It handles the connection between the server and the **MongoDB** database. Also, it provides the functionalities of _Search, Add, Update,_ and _Remove_.
	
- **Client**
	- **_DictionaryClient_**<br>
_DictionaryClient_ is the main class for the application on the client side. It is responsible for sending query and handle the response from the server. Also, it starts up the graphic user interface. Most importantly, it stores every data that needs to run the application including, _word, definition, suggestion, message, action, request, response_, etc. The GUI will use the setters and getters in _DictionaryClient_ to keep the data running at the backend and the data displayed on the UI is consistent. Furthermore, since _DictionaryClient_ is staying in the while loop when it is connected to the server, the GUI cannot be re-rendered because _Swing(JFrame)_ is using the Event Dispatch Thread. Therefore, another thread for _DictionaryClient_ is used to ensure the connection block and GUI can execute at the same time.

	- **_Gui_**<br>
It initialises the setting of _JFrame_, and it contains a _PageManager_. All the components will be created at the beginning. To show a certain page, _PageManager_ set the certain components to be visible.

	- **_PageManager_**<br>
_PageManager_ is implemented for rendering various pages. The most important method named _pageControl()_ is introduced to navigate of the pages for the client. Since it controls three sections of a page: _Header_, _MainContent_, and _ButtonGroup_, each page has different headers, main screens, and displayed buttons.

	- **_Header_**<br>
It illustrates two parts: title of the page name, and the textbox for entering or displaying the current word which is controlled by _JTextField().setEditable()_. 

	- **_MainContent_**<br>
In the class, it contains few _JLabels_ with GIF for loading screen, connection page, menu. Also, it possesses a _JTextArea_ associated with _JScrollPane_ to shows the meaning of the word. Since, the definition of the word can be hundreds of words, having a scroll bar to show all the content is necessary. When the server reports back a status of the previous query, instead of using the _JTextArea_, a message and a suggestion will be shown. Based on the status of the response, the text colour of the message can be different. Succeed query shows green, and error colours red.

	- **_ButtonGroup_**<br>
It creates several buttons: _Connect, Search, Update, Add, Remove, Discard, Confirm, Menu, Reconnect_. Every button associates with _addActionListener_, and it calls out the corresponding method when it is clicked. However, sometimes the buttons can be replaced by warning label. For instance, a warning message “Type something, magic will do the rest.” would shows up while the current action is searching for a word and the searching textbox is empty. More details in the _Excellence: Input Checking section_. 

	- **_Footer_**<br>
	_Footer_ is a static element. The reporting button and about me button are always shown in every page. 

## Excellence
- **Input Checking**<br>
Input checking is used by the setting IP and port text field, the searching text field, and the text area for definition. Firstly, it ensures that the input cannot be empty. It means that the query with null content will not be sent. Moreover, the IP and port setting and word searching have their own helper function which are _validIP()_ and _validWord()_ to check if the input is valid or not. If the input is invalid, the warning message will replace the buttons, therefore, the users will not be able to process to the next action unless they correct the input. Every input field has been added a key listener, so the validation keeps checking while a character is entered.

- **Error Message**<br>
When a query is failed to be executed, the server will send back a response with an error message to reveal the error for the client. The error response also includes the status code which can be used in advanced.

- **Suggestion**<br>
Every error response contains the suggestion from the server. It indicates the users what they can do when they receive an error message. For example, when a word is not found, the suggestion will be "Add the word to the dictionary". Therefore, the users not only understand what was going wrong, but also know how to handle with the issue with suggested feedback which makes the application more user friendly.

- **Similar Word Hint**<br>
To solve the case-sensitive problem, searching word uses regex to ignore while the original string is not found. The finding method returns the same word but in different case. The similar word hint can be improved by editing the regex in the future.

- **Formal Database**<br>
The usage of MongoDB database makes the server more extendible since it provides different powerful in-built finding. For instance, the dictionary can let the users to type in the definition to find a word in the next version.

- **Log File**<br>
The design of creating log file makes the debugging much easier. It allows the developer to see what’s is going wrong in the server and the client as well. With the function of reporting problems (more details in _Creativity: Feedback Support_), the client can report the issue with log file, so the program can be upgraded after handling those unexpected errors. A new directory will be created at the first time within the same folder as the jar file at. When the application is executed a log file named with the time will save in the log file directory.

## Creativity
- **Password Promote**<br>
Since the MongoDB is the database in this project, how to store the password for connecting to the database become an interesting problem, especially the server package is going to be a jar file. My solution is creating a password promote while the server starts up, so only the user who has the database password can run the server. Therefore, there is no concern with leaking password in jar file and on GitHub because the password will never be coded in the file. In addition, instead of using _String_ to save the inputting password, _char[]_ is the type to store the password. Since _char[]_ is mutable, the password can be rewrite after use which can hide the actual password, and it makes sure that the it would no longer in the memory and ensures the database security.

- **Connect Page**<br>
_Connect Page_ allows the users to input the server IP and port not only using the command argument but also changing the information via the GUI. To some extents, it improves the user experience.

- **Reconnect Page**<br>
When the server socket is closed, the GUI will redirect to _Reconnect Page_ to ask for reconnect to the server. In this stage, instead of just terminating the application, the users can wait and attempt to reconnect, or they can edit the server IP and port to the other server.

- **Loading Screen**<br>
The loading GIF will be illustrated on the _Waiting Pag_e when a query is sent. The design can let the users notice that the program is processing.

- **Feedback Support**<br>
There is a reporting button on the footer which can email to is0.jimhsiao@gmail.com and the client can attach the client log file, so there is less bug in the next version of the _is0xCollectiveDict_.

## Demo 
- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>

## Developed By
- The application is developed by _[is0xjh25 (Yun-Chi Hsiao)](https://is0xjh25.github.io)_ 
<br/>
<p align="left">
  <img alt="Favicon" src="demo/is0-favicon.png" width="250" >
