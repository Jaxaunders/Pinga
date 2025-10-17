
------Pinga ------


App for Device Proximity Awareness

Pinga is an Android safety application designed to enhance personal security through Bluetooth Low Energy (BLE), Wi-Fi, and location-based proximity detection.
Originally developed as part of a cybersecurity research project, Pinga helps users detect nearby devices, identify patterns of interest, and surface potential threats.
The app runs all analysis locally and never uploads anything to the cloud.

Features

-- Bluetooth Scanning -- Detects nearby BLE devices and shows details like signal strength and manufacturer info.
-- Local Device Logging -- Keeps a private record of devices you’ve encountered, stored on your phone only.
-- Forensic Engine -- Groups similar devices together and looks for recurring encounters.
-- Offline Operation -- Works entirely without an internet connection.
-- Privacy-first -- No personal identifiers, accounts, or external databases.

How it Works

Pinga listens for Bluetooth Low Energy advertisements and Wi-Fi networks in your area. It collects basic, non-identifiable information (like RSSI, vendor, and timestamp) and processes it locally.
Over time, the app can highlight devices that keep reappearing around you — potentially indicating a tracker or something worth checking out.

Tech Stack

Language: Kotlin
Platform: Android (min SDK 26)
Libraries: Coroutines, RecyclerView, org.json
IDE: Android Studio

Getting Started

-- Clone the repository:
    git clone https://github.com/jaxaunders/pinga.git
    cd pinga
-- Open the project in Android Studio.
-- Connect an Android device or use an emulator (Bluetooth scans require a real device).
-- Build and run the app.
-- Make sure Bluetooth and Location permissions are granted before scanning.

Current Focus

*** Improving stability of scans on newer Android versions
** Expanding the manufacturer dictionary
** Building better visual summaries of encounters
** Experimenting with local AI-based classification (still early days)


License

This project is released under the MIT License.
Use it, modify it, share it.

Disclaimer

Pinga is a privacy-focused research project. It’s meant to help people feel safer, not to track others.
Don’t use it for surveillance, stalking, or any purpose that invades someone’s privacy.
