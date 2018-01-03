# OpenAudible

A desktop application for downloading and managing your audible.com content.

## Download Installer
A binary installer for Windows, Mac and Linux is to be available, generated with install4j.
* **Mac OS**    [OpenAudible_macos.dmg](http://openaudible.org/OpenAudible_macos.dmg)
* **Win 64**    [OpenAudible_windows-x64.exe](http://openaudible.org/OpenAudible_windows-x64.exe)
* **Linux**     [OpenAudible_unix.sh](http://openaudible.org/OpenAudible_unix.sh)

## Features
* Import audible books from your account
* Convert to mp3 with all tags
* Display all your books in searchable
* Export web page/javascript file with all your books

## Screenshot
![Windows Screenshot](http://openaudible.org/images/open_audible_win.png)
Windows User Interface

## Building
OpenAudible is a java application that uses Maven for building.

### Prerequisites
Java 8, Maven, and git. Windows, Mac or Linux Desktop.

Clone the [git repo](https://github.com/openaudible/openaudible)

```
git clone https://github.com/openaudible/openaudible.git
```

#### Build using Intellij
Import Project: <br />
Select the openaudible/pom.xml file <br />
Click through all of the defaults

#### Build using Eclipse
Import... Maven Project<br />
Select the openaudible directory<br />

#### Build from command line (requires maven, java 8 SDK)

```
cd openaudible
mvn compile
mvn package
```
## Running/Debugging

Your IDE should link the platform specific SWT library via the maven profile.

#### IntelliJ
Select menu Run: Debug...  <br />
Select Edit Configurations... <br />
Add Application <br />
Name: OpenAudible <br />
Main Class: org.openaudible.desktop.Application <br />
VM options: -ea <br />
Mac VM options: -ea -XstartOnFirstThread <br />
Click Debug button <br />

#### Windows Command Line
```
java -cp "target\openaudible-jar-with-dependencies.jar;swt\org.eclipse.swt.win32.win32.x86_64-4.6.jar" org.openaudible.desktop.Application
```

#### Mac Command Line
```
java -XstartOnFirstThread -cp "./target/openaudible-jar-with-dependencies.jar:./swt/org.eclipse.swt.cocoa.macosx.x86_64-4.6.jar" org.openaudible.desktop.Application
```
Notice on Mac, the -XstartOnFirstThread is required to run SWT apps.

#### Linux Command Line
```
java -cp "target\openaudible-jar-with-dependencies.jar;swt\org.eclipse.swt.gtk.linux.x86_64-4.6.jar" org.openaudible.desktop.Application
```

#### Notes

Running with -ea to alert you of assertion failures is recommended for debugging. We use a lot of "asserts" to help identify problems.

Enter that into the VM Arguments on your debugger/run dialog if using an IDE.

You should see the user interface. You may see an error, or a warning about where you can go to preferences and enter your audible account details.

Open the Preferences from the Edit Menu. <br />
Enter your audible user name (email) and password. <br />
Before logging in with the application, go to the Controls: Browser menu and log into your audible account. <br />
This is only required if logging in fails or if the browser cookies expire. <br />

The application will use cookies to expedite logging in-- and bypassing some of the "are you a human" checks.

Errors are logged to an "error.log" file, usually written out to the application directory.

## Built With

* [Eclipse SWT](http://www.eclipse.org/swt/) - Standard Widget Toolkit
* [HTML Unit](https://htmlunit.sourceforge.net/) - HTML web page scraping

## Contributing

This is a work in progress. It needs testing and bug reporting for all platforms.

* Exporting to a podcast format is planned
* Exporting to a format that supports the best mobile audio book players is the goal.
* The UI needs cleaning up, especially for Linux.
* Improved "first time setup" and connection needs major improvement
* Support for multiple audible accounts would be nice

Please feel free to submit pull requests.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/openaudible/openaudible/tags).

## Authors

See the list of [contributors](https://github.com/openaudible/openaudible/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details, but may uses code licensed by other licenses.
