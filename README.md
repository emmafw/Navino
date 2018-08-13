# Navino-Android

Navino is a mobile navigation app that allows user to mute directions when they are in areas they know. 
This is done by outlining known areas on the map before starting their directions.
This repository is the Android appication, iOS can be found at https://github.com/emmafw/Navino-ios2

To install this application Android Studio must be installed. Navino was developed in Android Studio 3.1.2
Download this repository and open using Android Studio. 

The user will need to generate a Google Maps API Key from https://developers.google.com/maps/documentation/android-sdk/signup
Paste the key into the AndroidManifest under android:value. 

To download the app to an Android device simply plug the device into the computer and select it from the list of connected devices after clicking run. 

If running Navino on a simulator current location will be a hard coded value which can be updated in the Simulator's extended controls. 
Since current location is hard coded, features such as directions in real time will not work since the current location will not change. 

Once the app is installed the user will be required to sign in using a Google account.


Repositories used for assistance:
https://github.com/NilaxSpaceo/GoogleMapOverlay
