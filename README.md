# OpenAudible

A desktop application for downloading and managing your audible.com content.

## Getting Started

Import as a standard java Maven project.
You'll need a copy of ffmpeg-- I put a copy in the root directory but can be anywhere if available in your system's PATH.

### Prerequisites
Java 8, Maven, and git

### Getting the source

Clone the [git repo](https://github.com/openaudible/openaudible)

```
git clone https://github.com/openaudible/openaudible.git
```

### Build using Intellij
Import Project:
Select the openaudible/pom.xml file
Click through all of the defaults

### Build using Eclipse
Import Maven Project
Select the openaudible/pom.xml file

### Build from command line (requires maven, java 8 SDK)

cd to the openadible directory..
```
cd openaudible
```

```
mvn compile
mvn package
```
### Running the application from IDE

You need to link to the appropriate SWT library for your OS. Your IDE may do this for you using the Maven platform profile.
For IntelliJ, select RunL Debug... menu
Select Edit Configurations...
+ Add Application
Name: OpenAudible
Main Class: org.openaudible.desktop.Application
VM options: -ea
Mac VM options: -ea -XstartOnFirstThread
Click Debug button


### Running from Command Line

For Windows:
```
java -cp "target\openaudible-jar-with-dependencies.jar;swt\org.eclipse.swt.win32.win32.x86_64-4.6.jar" org.openaudible.desktop.Application
```

For Mac:
```
java -XstartOnFirstThread -cp "./target/openaudible-jar-with-dependencies.jar:./swt/org.eclipse.swt.cocoa.macosx.x86_64-4.6.jar" org.openaudible.desktop.Application
```

Running with -ea to alert you of assertion failures is recommended for debugging. We use a lot of "asserts" to help identify problems.

Notice on Mac, the -XstartOnFirstThread is required to run SWT apps.
Enter that into the VM Arguments on your debugger/run dialog if using an IDE.

You should see the user interface. You may see an error, or a warning about where you can go to preferences and enter your audible account details.

Open the Preferences from the Edit Menu.
Enter your audible user name (email) and password.
Before logging in with the application, go to the Controls: Browser menu and log into your audible account.
This is only required if logging in fails or if the browser cookies expire.

The application will use cookies to expedite logging in-- and bypassing some of the "are you a human" checks.

Errors are logged to an "error.log" file, usually written out to the application directory.

### Installer
A binary installer for Windows, Mac and Linux is to be available, generated with install4j.

### TODO
This is a work in progress. It needs testing and bug reporting for all platforms.
Exporting all your audible books to a web page is in progress.
Exporting to a podcast format is planned
Exporting to a format that supports the best mobile audio book players is the goal.
The UI needs cleaning up, especially for Linux.
Support for multiple audible accounts is needed



## Built With

* [Eclipse SWT](http://www.eclipse.org/swt/) - Standard Widget Toolkit
* [HTML Unit](https://htmlunit.sourceforge.net/) - HTML web page scraping

## Contributing

Please feel free to submit pull requests.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/openaudible/openaudible/tags).

## Authors

See also the list of [contributors](https://github.com/openaudible/openaudible/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details, but may uses code licensed by other licenses.
