# Lottery service connector

## Implementation

```ruby
implementation 'com.github.makeabledk:lottery-service-connector-android:latest.version.here'
```

## Setup

Add package query to manifest:

```xml
<manifest>

    <queries>
        <package android:name="dk.makeable.varelotteriet" />
    </queries>
    
    <application
    ...
```

## Usage

Please see sample for implementatoin details [here](https://github.com/makeabledk/lottery-service-connector-android/blob/develop/app/src/main/java/dk/makeable/lotteryserviceconnector/MainActivity.kt).