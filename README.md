# Welcome to streaming test environment


## How to test 📱
1) Download stable release [.apk](http://raw.githubusercontent.com/GoodDamn/MediaStreamingAndroid/main/test_apps/0.2.1.apk)
2) Install application
3) Grant video recording permission
4) Write your IP-address destination (you can test once with 127.0.0.1 individually). You need to stream to peer client which has the same application.
5) 'Start recording' button means that you can receive live stream video from another client. Click on it.
6) Buttons (0, 1 and etc) below are your available camera ID's for live streaming.
7) Select any camera (0, 1 and etc) and click on it

If you have any issues with live streaming, please [write it](https://github.com/GoodDamn/MediaStreamingAndroid/issues)


## How to build a project 🛠️
1) Download latest Android Studio IDE
2) Clone repository
3) Select folder with Android Studio IDE and open it
4) Wait till gradle building will be completed

### How to change camera capturing settings 📸
Press LShift twice and type `CaptureRequest.kt`. Inside the `default()` function you can change parameters via set() method. See [documentation](https://developer.android.com/reference/android/hardware/camera2/CaptureRequest) with available options

### How to change camera streaming settings 🎥
Press LShift twice and type `MediaFormat.kt`. Inside the `default()` function you can change parameters via setInteger() method. See [documentation](https://developer.android.com/reference/android/media/MediaFormat) with available options
