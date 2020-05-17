# Log Cat Delegate

[![](https://jitpack.io/v/nathan-fiscaletti/logcat-delegate.svg)](https://jitpack.io/#nathan-fiscaletti/logcat-delegate)
[![](https://jitpack.io/v/nathan-fiscaletti/logcat-delegate/month.svg)](https://jitpack.io/#nathan-fiscaletti/logcat-delegate)
[![GitHub license](https://img.shields.io/badge/license-Apache%202.0-blue)](https://github.com/nathan-fiscaletti/logcat-delegate/blob/master/LICENSE)
![Supported SDK](https://img.shields.io/badge/API-14%2b-blue)

This utility allows you to attach a delegate to Log Cat and listen for incoming messages from within your applications Java code.

It achieves this by starting a sub-process for Log Cat and parsing the output. This process will be kept alive if shut down for any reason, until either you kill your application or the delegate is de-registered.

## Add the library to your application

1. In your **project level** `build.gradle` add the repository

    ```gradle
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
    ```
    
2. In your **module level** `build.gradle` add the library (find the version numbers [here](https://github.com/nathan-fiscaletti/logcat-delegate/releases))

    ```gradle
    dependencies {
        ...
        implementation 'com.github.nathan-fiscaletti:logcat-delegate:LATEST_VERSION'
    }
    ```

## Demo Application

There is a demo application that will display a simple example of this library available in the [./demo](./demo) directory.

## Listening for Messages

To listen for log messages you need to implement and register a `LogCatDelegate`.

```java
LogCatDelegate logCatDelegate = new LogCatDelegate() {
    @Override
    public void onNewMessage(LogCatMessage message) {
        // process the message
    }
};
logCatDelegate.register();
```

Registering the delegate will start the child process and begin listening for new messages. Only messages sent AFTER the delegate was registered will be received.

You can later de-register the delegate using the `logCatDelegate.deregister()` and `logCatDelegate.deregisterAsync()` methods.

## Customizing the Log Cat invocation

If you want to pass custom command line parameters to the `logcat` command when it is invoked, override the `getLogCatCommandLineArguments()` method of your `LogcatDelegate` implementation.

```java
LogCatDelegate logCatDelegate = new LogCatDelegate() {
    @Override
    public String getLogCatCommandLineArguments() {
        return "-b all";
    }

    @Override
    public void onNewMessage(LogCatMessage message) {
        // process the message
    }
};
```

Note that the `-v (--format)` argument is not supported as this is used internally for parsing messages. The default command line arguments that are sent are `-b all`.

## Formatting Messages

You can format the `LogCatMessage` instances you receive back from the delegate fairly easily. 

|Method|Description|
|---|---|
|`getFormatted()`|Retrieve a formatted version of the message.|
|`getFormatted(String)`|Retrieve a formatted version of the message with a custom format.|
|`getFormatted(String, String)`|Retrieve a formatted version of the message with a custom format and a custom timestamp format.|

Calling `message.getFormatted(String)` with a format string will format the message as desired. You can use the format specifiers below to choose a format.

|specifier|description|
|---|---|
|`%d`|RFC3339Nano formatted timestamp. See below for information on how to apply a custom timestamp format.|
|`%de`|The timestamp formatted as an epoch timestamp with millisecond precision.|
|`%v`|The message priority as a name.|
|`%vi`|The message priority as a numeric code.|
|`%vc`|The message priority as a single character.|
|`%p`|The Process ID.|
|`%r`|The Thread ID.|
|`%t`|The Tag.|
|`%m`|The Message.|

```java
String formatted = message.getFormatted(
    "%de %p %r %vc %t: %m"
);
```

> The default format when calling `message.getFormatted()` without providing a format is as follows:
> ```
> %de %p %r %vc %t: %m
> ```

If you want to format a message using a custom Date format as well, you can provide a standard `SimpleDateFormat` string as the second argument. This will inform the formatter how it shold format dates when using the `%d` format specifier.

```java
String formatted = message.getFormatted(
    "%de %p %r %vc %t: %m",
    "yyyy-MM-dd'T'HH:mm:ss.SSS"
);
```

If you prefer to handle formatting yourself or need a more complex solution you can retrieve the different elements of the message using it's getter methods.
