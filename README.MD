# Lottery service connector

## Implementation

[![](https://jitpack.io/v/makeabledk/lottery-service-connector-android.svg)](https://jitpack.io/#makeabledk/lottery-service-connector-android)


```ruby
implementation 'com.github.makeabledk:lottery-service-connector-android:latest.version.here'
```

## Setup

Add package query to manifest of LL main screen app:

```xml
<manifest>

    <queries>
        <package android:name="dk.makeable.varelotteriet.mainscreenserviceapp" />
    </queries>
    
    <application
    ...
```

## Usage

Please see sample for implementatoin details [here](https://github.com/makeabledk/lottery-service-connector-android/blob/develop/app/src/main/java/dk/makeable/lotteryserviceconnector/MainActivity.kt).
