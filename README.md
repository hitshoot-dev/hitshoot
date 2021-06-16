# hitshoot
An alternative BitChute frontend/proxy with interesting features

## Requirements
 - Java 8 or higher
 - Java Development Kit (JDK) 8 or higher (if building from source)
 - PostgreSQL (in the future, not at the moment though)

## Compiling
Clone this repository, and once inside of it, run the comment `./gradlew build` (Linux, MacOS) or `gradlew.bat` (Windows).
Once the build finishes, copy `app/build/libs/app-all.jar` to wherever you want to install the application.

## Setting up
(These commands assume your application jar is named `app.jar`, if you have it under a different name, change the commands accordingly) 

The first thing you need to do is generate the configuration file. You can do this by running

```shell
java -jar app.jar --recreate-config
```

There should now be a configuration file called `config.yml` in the application directory. Edit it to your liking.

Once you want to run the application, simply run

```shell
java -jar app.jar
```

## Updating
To determine your current version, run `java -jar app.jar --help`. The version will be listed at the bottom of the message.

Recompile (or download a new release), and replace the old jar with the new one.
Unless otherwise noted on the release, all you need to do is restart the server, and everything should be good to go.

Sometimes between releases there will be changes the configuration file structure.
You don't need to do anything in most cases, since the application automatically performs the necessary changes.