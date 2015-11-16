**Table of Contents**

- [Whist](#whist)
   - [Features](#features)
   - [About](#about)
   - [Setup instructions](#setup-instructions)
- [High level overview](#high-level-overview)
	- [Code Architecture](#code-architecture)
		- [Controllers](#controllers)
		- [Views](#views)
		- [Models](#models)
	- [AI computer opponents](#ai-computer-opponents)
	- [Multiplayer](#multiplayer)
	- [Software design pattern examples](#software-design-pattern-examples)
- [Code metrics](#code-metrics)
- [Used libraries & code](#used-libraries--code)


# Whist

Hello, I am very pleased to welcome you to the GitHub page of Whist.

Whist is a trick-taking card game with many variations. The one implemented herein is very similar to [Oh Hell](https://en.wikipedia.org/wiki/Oh_Hell).
The Android version is currently in open alpha-testing in the [Google Play Store](goo.gl/3ISVdr). In case the link is not working for you, please [send me](mailto:ntsaoussis@gmail.com) your gmail address and I will add you to the list of testers.


### Features
- Single player against computer opponents
- (_Android only_) Real-time multiplayer using Google Play Games Services (invite friends and/or play with up to 3 random opponents)
- (_Android only_) Achievements and Leaderboards
- Statistics, game settings & variants

Planned future work: tests, iOS app, [app invites & deep links](https://developers.google.com/app-invites/android/), more battery efficient computer opponents, better graphics, in app purchases, and more.

> **Note:** The only reason I have not yet written unit tests is because I was trying to get this out here the soonest possible. However, I definitely plan to do so soon. 

### About

This project started from scratch in **September of 2015** and the biggest part was complete by **mid-October**. Some chunks of code have been iterated over-and-over again during the past years, whenever I found time to experiment with Android development. However, every single line of re-used code has been revisited and (likely) refactored.

--------
> _I never envisioned to make profit from this app --- I just love writing code in my free time, and decided to write an app for the card game I enjoy the most playing with my friends._

--------

I decided to open-source my code in order to showcase my work and get valuable feedback. Below, I present a high-level overview of the app --- feel free to [contact me](mailto:ntsaoussis@gmail.com) or dig in the code for more details. My CV can be found [here](https://uk.linkedin.com/in/ntsaousis).

> **Note:** Everything in here is **100% my own work, except where specifically indicated below**. This includes the UI design and all graphic assets. I am not very proud of my design skills, so please be lenient with the visual aspect of the game.

### Setup instructions

In case you want to build and run the project yourself, you need to:

1. Follow the instructions from [here](https://github.com/libgdx/libgdx/wiki/Gradle-and-Eclipse), to import an existing LibGDX, gradle based project.
2. Implement the function `getStoragePassword()` in [`Config.java`]() (e.g. by simply returning a string).
3. Create your own implementation of card shuffling in [`Dealer.java`]() (e.g. by calling `cards.shuffle()`).
4. Setup Google Games Services as explained [here](https://developers.google.com/games/services/console/enabling).
5. Modify the [ids.xml]() file, with the application ID and achievement/leaderboards IDs you obtained in step (4).
6. Modify the signing configurations in the android [`gradle.build`]() file, with your own keyAlias, keyPassword, storeFile and storePassword (both for the debug and release configurations).

# High level overview

The app is built on top of the [LibGDX](https://github.com/libGDX/libGDX) game development framework and it is written in Java. Currently, it compiles for Android and Desktop,
with an iOS version planned for the future. Platform specific code is placed in the [`android`](), [`desktop`]() and [`ios`]() directories, whereas all platform independent code is
placed in the [`core`]() package. It also uses a customised version (to fit my own needs) of the [StageBuilder](https://github.com/peakgames/libgdx-stagebuilder) library, a project for building LibGDX stages (screens) from xml files. Please have a look at my other [repository](https://github.com/tsanikgr/illustrator-to-android), where I supply an Adobe Illustrator javascript which automatically exports graphical assets and .xml files for use with StageBuilder. Once the Illustrator file is updated, the UI of the app can be updated using two clicks, with no code modification needed whatsoever.

> One of the primary goals when I decided to write this game from scratch, was to create my own library, tools and work-flows. This would allow me to quickly build new games in the future. As such, my top priorities were **code re-usability, maintainability, testability and minimal dependencies**.



## Code Architecture
The structure follows the **Model View Presenter (MVP)** architecture. That said, please do not be confused: all _Presenters_ extend the `Controller` abstract class, and follow the `XxxController` naming convention (after the Model View Controller (MVC) architecture which is very similar).

In the following **UML class diagrams**, many classes are omitted for brevity reasons, including all `XxxModel` classes (many are shown as fields). Moreover, only a selection of the members of each class is shown to save space. 

--------
> **Tip:** You might prefer to navigate the diagrams whilst reading the descriptions below them.

--------

### Controllers
![Diagram 1 - Controllers (Presenters)](https://github.com/tsanikgr/whist/blob/master/uml/overview_controllers_ai.png "Diagram 1 - Controllers (Presenters)")

All controllers inherit from the [`Controller`]() abstract class, which allows the communication between them.

The diagram is color coded as follows:

#### 1. App entry point (green)

The [`AppController`]() class is the app entry point. It implements the [`IAppController`]() interface, which defines methods for the app lifecycle events (e.g. `onCreate()`, `render()`, `onDispose()` etc.).

In addition, [`AppController`]() is a [`CompositeController`](), and is the **root of the controllers object graph**. In other words, all other controllers are its children or grand-children.

> **Note:** The proposed structure scheme makes it **easy to write unit tests**. Each controller can be easily swapped for a stub, allowing the independent testing of each one of them. At the same time, communication between them is easy without dependency injections.

#### 2. First level controllers (blue)

The name of each controller ([`Assets`](), [`Storage`](), [`CardController`]() etc.) and the members of the interfaces they implement pretty much sum up their responsibilities. 

The most interesting one is the composite controller [`ScreenDirector`](). It creates the root of all [`View`]() objects: this is represented by LibGDX's [`Stage`]() object. Moreover, it creates three [`ScreenController`]()s: the [`LoadingController`](), the [`MenuController`]() and the [`GameController`](), and activates the appropriate one according to the state of the app.

#### 3. Screen controllers (red)

These are the **presenters** which handle the creation and the management of their Views.
   - The [`LoadingController`]() updates the [`LoadingView`]()
   - The [`MenuController`]() updates the [`MenuScreen`]() and gets notified about input events.
   - The [`GameController`]() updates the [`GameScreen`]() and gets notified about input & game events.
   
The [`GameController`]() delegates game actions to the concrete implementations the the [`IWhistGameController`]() interface.

#### 4. Game controllers (yellow)

All concrete implementations the the abstract class [`WhistGameController`]() make up the Whist-specific game controllers:

   - [`GameStateController`]()
   - [`Dealer`]()
   - [`PlayerController`]()
   - [`BoardController`]()

> **Note:** The [`GameSimulator`](), [`WhistExecutorService`](), and [`ThreadedGameModelPool`]() are **not** controller objects. They are just there to show how the [`PlayerController`]() implements the bots.

### Views
![Diagram 2 - Views](https://github.com/tsanikgr/whist/blob/master/uml/overview_views.png "Diagram 2 - Views")

Views can be built synchronously or asynchronously. **A view is only built synchronously only when it is required immediately**. Otherwise, as with every other computationally expensive task, most of the work is performed on a background thread. Whenever something needs to run on the UI thread (such as openGL texture binding calls), the command software pattern is used to return to the main thread.

//TODO: work in progress...


### Models

Models expose methods to update and query their internal states, having no business logic (apart from some input validation when updating). They can be serialised to store them or transmit them through network calls. Most of them are placed in the `models` package.

## AI computer opponents

The game wouldn't be complete without a single player mode against computer opponents. I needed a quick hack to implement this functionality, but simple heuristic rules would make the game boring.

> _"Although the rules are extremely simple, there is enormous scope for scientific play."_ [[Wikipedia]](https://en.wikipedia.org/wiki/Whist)

Thus, I implemented a simulation-based algorithm, that allows the computer to play the game with no training or prior input.

Although this algorithm is not suitable for a mobile application (... I guess users expect card games to **use less battery**!), makes the game fun because it is hard to beat. **I challenge you to win the bots!**

#### It is multithreaded

  Uses an `ExecutorService` to create a _fixedThreadPool_ (see [`WhistExecutorService`]()). Synchronisation is achieved using `ReentrantReadWriteLock`s.
  
#### ... and recursive

  The game is simulated recursively. Whenever a recursive call is made, a read lock is obtained to see whether there are any idling threads. If so, the caller tries to obtain a write lock on the number of running threads: if successful, the number of active threads is incremented, and the simulation continues as a new task on the new thread. Otherwise the current thread continues normally.
  
#### ... and uses Pools to reduce the frequency of garbage collections

  Every time a game action is simulated, a new [`SimGameState`]() (Simulation Game State) is created. Instead of creating a new object every time, `SimGameState`s are recycled whenever they are no longer required. To make matters simpler, one pool is used per thread. See [`ThreadedGameModelPool`]().

> _Note:_ Due to the nature of the game, the number of [`SimGameState`]()s space quickly explodes, making it impossible to simulate all possible outcomes. To tackle this problem, some heuristic rules are used to limit the number of simulations per card, when dealing more than 6 to each player.

## Multiplayer

The [Google Play Games Services API](https://developers.google.com/games/services/) was used to implement real-time multiplayer functionality across devices and users. All of the API specific code can be found in the [`google`]() package. It's a high level API, so it was very easy to implement the following:

- Inviting friends to play against them, or playing against random opponents (or a mix of the two)
- Handling invitations and notifying the user
- Handling room life-cycle events
- Creating and matching opponents for different game variants (such as the bet amount)

Interesting bits of my own code can be found in the [`MultiplayerMessage`]() class (e.g. using a `Pool` to recycle messages and multiplexing information into single bytes to conserve network usage) and in the [`Messenger`]() class, whereby an inbox and an outbox are used to properly handle Messages received in the wrong order, or requesting messages that have been dropped to be re-sent. The `BaseGameUtils`, `GameHelper` and `GameHelperUtils` classes where obtained from Google's samples, and where slightly modified to the needs of the game.

## Software design pattern examples

One example for each of the following software design patterns is given below.

* #### Behavioral
    - **Observer**
    
        The [`IStatisticsController`]() is an observable which notifies the attached observers (e.g the [`UserView`](), [`StatisticsView`]() and [`CoinsView`]()) when the [`StatisticsModel`]() changes.

    - **Command**

      Whenever a task finishes on a background thread, this pattern is used to return to the main UI thread. Concrete commands are encapsulated in `Runnable` objects and are submitted for execution using the `Gdx.app.postRunnable()` utility function.
      
    - **Mediator**

      Classes implementing the [`IScreenController`]() interface are concrete mediators: they handle the interaction between UI elements and their corresponding model representations. In other words, `ScreenController`s update the `Screen`s, and are informed by the `Screen`s about user events to update the models (`Screen`s inherit from `EventListener`).
      
    - **Memento**
    
      This pattern is used for game saving & loading. The [`GameController`]() (originator) supplies the [`IWhistGameController`]()s (caretakers) a [`GameModel`]() object to continue from a previously saved game.
            
    - **Strategy**

      Classes implementing the `WhistAiInterface` can be swapped to create different bots. Classes extending `AbstractWhistAi` execute the strategy's logic asynchronously by default.

* #### Structural
    - **Composite**
    
       The object graph of all `Controller`s is formed using the composite pattern. `CompositeController`s, such as the app entry point (`AppController`), delegate work to their child controllers. Also, `Screen`s are composite `View`s.
       
    - **Facade**
    
       `Screen`s are facades to `View`s. Most of the communication between `IScreenController`s and UI elements go through them (in other words screens delegate updates to the appropriate `View`.)
       
    - **Pools**
    
       Pools are used to recycle objects, and hence reduce the frequency of garbage collections. They are used in many places: network messages (`MultiplayerMessage`), simulated game states (`SimGameState`), `Action`s attached to actors etc.

* #### Creational
    - **Factory method**
    
      The abstract class `Screen` provides the methods `buildViewSync(String)`, `buildViewAsync(String)` and `getView(String, Class<T>)`. The subclasses of `Screen` decide which views to instantiate.
    
    - **Builder**
    
       A variation of the builder pattern is used for the circular reveal animations. `Animators` provide an `AnimatorConfig` object, which can be modified to customise the animation. However, `AnimatorConfig` objects are not builders _per se_, since they are members of `Animators` instead of handling their creation. Nevertheless, they separate the representation of `Animators` from their creation.
       
    - **Prototype**
    
      `GameModel`s provide a `copy(GameModel)` member, which returns a clone ...`GameModel`.

# Code metrics

In case you are interested, here are some of the metrics I obtain using the static code analysis tool [SonarCube](http://www.sonarqube.org/).

- Total lines of code: *__22k__*
- Classes: *__234__*
- SQALE Rating: *__A__*
- Technical Debt Ratio: *__1.9%__* (Note that I am using the default SonarCube quality profile which includes in this metric a lot of minor issues (e.g. replacing tabs with white-spaces).)
- Directory tangle index: *__0%__*
- Cycles: *__0__*
- Dependencies to cut: *__0__*
- Complexity: *__4036__*
- Average complexities
	* _per function_: *__2.2__*
	* _per class_: *__17.2__*
	* _per file_: *__22.5__*


# Used libraries & code

As already mentioned, I am using a modified version of the [StageBuilder](https://github.com/peakgames/libgdx-stagebuilder) library. The relevant code is in the [`assets`]() and [`stage_builder`]() packages. In addition, the [`Base64`]() class in the [`Cryptography.java`]() file was obtained from [here](http://migbase64.sourceforge.net/). The `BaseGameUtils`, `GameHelper` and `GameHelperUtils` classes where obtained from [Google's samples](https://github.com/playgameservices/android-basic-samples/tree/master/BasicSamples/libraries/BaseGameUtils/src/main/java/com/google/example/games/basegameutils). The [`LRUCache`]() was obtained from [here](https://github.com/igniterealtime/jxmpp/blob/master/jxmpp-util-cache/src/main/java/org/jxmpp/util/cache/LruCache.java).