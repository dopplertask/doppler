# doppler
Doppler is a revolutionary open-source software that allows you to automate tasks easily. Whether it’s a bunch of bash scripts or just starting your car remotely, you can automate it. You can build, run, reuse and share automations with anyone around the globe.

On top of all of this life-simplifying project, we  are striving to make an climate friendly software that is fast, easy and consumes as little resources as possible.

## Build status
[![Build Status](https://travis-ci.org/dopplertask/doppler.svg?branch=master)](https://travis-ci.org/dopplertask/doppler)

## Simple install
Using snap:
```snap install dopplertask```
## Prerequisites

Install JDK 11 and gradle.

To run as a project:
```gradle clean build bootRun```

## Usage

### Example
#### Add a task

Below is an example of a Dopplerfile:
```
{
  "name": "Test Task",
  "actions": [
    {
      "@type": "PrintAction",
      "message": "This is an example task."
    }
  ]
}
```

This file is used to add tasks to the system. You can do that by sending a request to the REST API:

```curl -X POST http://localhost:8090/task -H "Content-Type: application/json" -d @add_task.json```

#### Run a task


To run a task, send the JSON with the task id and parameters to the REST API:

```
{
  "taskName": "doppler-example",
  "parameters": {
  }
}
``` 

Example of the call:

```curl -X POST http://localhost:8090/schedule/task -H "Content-Type: application/json" -d @run_task.json```


### Actions
#### PrintAction
##### Variables
* message: A message to be printed

#### SSHAction
Connects to a machine via SSH and executes a command
##### Variables
* hostname: hostname to connect to
* username
* password
* command: A command to execute once connected

#### SecureCopyAction
##### Variables
* hostname: hostname to connect to
* username
* password
* sourceFilename: location of the file to be transferred
* destinationFilename: location of where the file will be placed in the remote host

#### HttpAction
##### Variables
* url
* method: GET, POST, PUT, DELETE
* body
* headers: Key-value list of headers

#### MySQLAction
Executes a MySQL statement, like a SELECT statement.
##### Variables
* hostname: hostname to connect to
* port (Optional)
* username
* password
* database
* timezone
* command

#### TimedWait
##### Variables
* seconds: Amount of seconds to wait

#### LinkedTaskAction
##### Variables
* taskName: name of another task

#### BrowseWebAction
Starts a browser and executes a list of UI Actions.
##### Variables
* url: URL to naviate to.
* headless: If set to false, it will show the web browser window. Default is true.
* actionList: A list of actions to perform. 

A UI Action contains the following fields:

* fieldName: Name of the field to control. Not required when using the actions WAIT OR ACCEPT_ALERT.
* findByType: Determines how to find the field. Possible values: ID, NAME, XPATH, CSS. Not required when using the actions WAIT OR ACCEPT_ALERT.
* action: Action to perform. 

| Action        |Description                   |
| ------------- |------------------------------|
|PRESS          |Clicks on the requested field.|
|WRITE          |Writes in the requested field. Useful if the field is an input text or textarea.|
|SELECT         |Selects an item from the requested select list / dropdown based on name. |
|WAIT           |Waits a certain amount of time. Amount of time is expressed in milliseconds.|
|ACCEPT_ALERT   |Closes an alert / confirm box.|

* value: Required only if used with the actions WRITE, SELECT and WAIT.

#### ReadFileAction
Reads a file from disk.
##### Variables
* filename: name of a file. Using ~ in the beginning of the filename will point to the home directory eg. ~/Downloads/testfile.txt 

#### SetVariableAction
Sets or modifies a variable for the current execution.
##### Variables
* setVariableList: A list of key-value pairs of variables.

#### ExecuteCommandAction
Executes a command on the current machine.
##### Variables
* command: Command to execute.


### Retry
All actions have retry mechanisms to allow you to retry an action.
#### Variables
* retries: Amount of retries.
* failOn: Any input will cause the current action to be marked as failure.
* continueOnFailure: Lets the action continue on failure, ignoring any retry.

## Docker
To run the built docker image:
```docker run -p 8090:8090 -p 61617:61617 dopplertask/doppler-engine ```

To rebuild the docker image:
```docker build -t dopplertask/doppler-engine .```

## Authors

* **Feras Wilson** 
* **Jack Kourie** 

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details