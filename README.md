# SaltyChatSwitcher
A simple tool to switch between SaltyChat v2 and SaltyChat v3 with just one click.
The program currently only works on windows.  
  
**The [Java Runtime Environment](https://www.java.com/en/download/) (version 8 or higher) is required.**

## How to set up?
Once you start the program for the first time, it automatically enters the setup mode.
You must manually install both versions once again for the setup. (You need the **.ts3_plugin** files for that).
Just read the instructions of the setup and make sure to follow them correctly.

> **IMPORTANT:** When switching versions, uninstall the old version in TeamSpeak and then install the new version you want to install.
It may cause bugs when you just try to "override" the versions.

## How does it work?
The program basically copies all relevant files & folders of the SaltyChat versions in the `sc_switcher` folder and changes the config files of TeamSpeak to enable the plugins.
Once it is set up correctly for both versions, you can switch between them with just one click.
> **TeamSpeak must be closed to switch versions.**

## Something doesn't work?
### It keeps telling me to close TeamSpeak even though it isn't opened?
If you are sure that TeamSpeak is closed, but still get the warning, just press "Proceed anyway".

### The wrong SaltyChat versions are installed when switching?
Try deleting the folder `%APPDATA%\TS3Client\sc_switcher\` and make sure to follow the setup correctly. If it still doesn't work, feel free to create an issue here on GitHub.

---
A huge thanks to Apache for [Commons IO](https://commons.apache.org/proper/commons-io/), which is used in this program.
