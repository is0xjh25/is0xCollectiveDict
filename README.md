# is0xCollectiveDict
 
## Table of Contents
* [About the Application](#about-the-application)
* [Getting Started](#getting-started)
* [Usage](#usage)
* [Demo](#demo)
* [Developed By](#developed-by)

## About the Application

## Getting Started
- Start up the server:
 >Type in terminal: `java –jar DictionaryServer.jar <port> <dictionary-file>`
- Open the graphic user interface on the client side:
 >Type in terminal: `java –jar DictionaryClient.jar <server-address> <server-port>`

## Usage
- **Login**
  - ***Login***: Require username and password and the authentication is verifed by JWT which would be stored in browser cookie.
  - ***Forgot Password***: Send a rescue password to user's email.
  - ***Register***: Requrie username, email and password.
- **Profile**
  - ***View***: Display account information.
  - ***Edit***: Email and password. Yet, username is unchangable.
  - ***Log Out***: See you next time!
- **Vote**
  - ***1st Vote***: Vote by intuition.
  - ***Read Posts***: Have a look on others' point of views
  - ***2nd Vote***: Only this vote would be counted.
  - ***Share Opinion***: Publish your thought and let people see it.
- **Post**
  - ***Post***: Publish your thought and let people see it.
  - ***Delete Post***: Delete your post, then make another one (maybe)?
- **Upvote**
  - ***Upvote***: Support others' posts.
  - ***Undo Upvote***: Misclick? Let's get the upvote back.
- **History**
  - ***Search By Date***: The result would show the vote started in last 90 days of the selected date.
  - ***Search By Keyword***: Keyword can be a part for vote's name or it can be one of the categories.
  
## Demo 
- **Login**
<p align="center">
  <img alt="Login Page" src="demo/login.png" width="500">
</p>


## Developed By
- The application is developed by _[is0xjh25 (Yun-Chi Hsiao)](https://is0xjh25.github.io)_ 
<br/>
<p align="left">
  <img alt="Favicon" src="demo/is0-favicon.png" width="250" >
