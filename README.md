# OpenAudible

A desktop application for downloading and managing your audible.com content.

## Getting Started

Import as a standard java Maven project.
You'll need a copy of ffmpeg-- I put a copy in the root directory.

### Prerequisites
Java 8, Maven, and git

### Installing from source

Clone the [git repo](https://github.com/openaudible/openaudible)

```
git clone https://github.com/openaudible/openaudible.git
```

cd to that directory.. and build the openaudible project with your IDE.

In the Maven Properties, there are 3 profiles for Win, Mac, and Linux. Linux is untested. The profile points the linker to the appropriate swt library, which has the desktop widget native libraries.

```
mvn install
```

Then run the jar. You need to link to the appropriate SWT library for your OS.

You should see the user interface where you can go to preferences and enter your audible account details.

If you get any errors logging in, it might help to open the Audible web browser from within the application and log in there.

The application will use cookies to expedite logging in-- and bypassing some of the "are you a human" checks.

### Installer
A binary installer for Windows, Mac and Linux is to be available, generated with install4j.


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
