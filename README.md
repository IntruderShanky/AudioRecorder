# AudioRecorder
![](https://jitpack.io/v/IntruderShanky/AudioRecorder.svg)

For Personal Use

# Usage
Step 1. Add the JitPack repository to your build file
```groovy
allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
```
Step 2. Add the dependency
```groovy
dependencies {
  compile 'com.github.IntruderShanky:AudioRecorder:1.1'
 }
 ```

# Implementation
Add Manifest Permission
```
  <uses-permission android:name="android.permission.RECORD_AUDIO" />
```
Request Runtime permission if required (API>23)

```java
  RecorderDialog dialog = RecorderDialog.getInstance("output_path.mp3");
  dialog.show(getFragmentManager(), "Audio Recorder");
  
   dialog.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onRecorded(File file) {
                
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onDialogDismiss() {

            }

            @Override
            public void onPositiveButtonClick() {

            }
        });
```
