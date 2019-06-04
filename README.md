# doppler
Doppler is a revolutionary open-source software that allows you to automate tasks easily. Whether it’s a bunch of bash scripts or just starting your car remotely, you can automate it. You can build, run, reuse and share automations with anyone around the globe.

## Build status
[![Build Status](https://travis-ci.org/dopplertask/doppler.svg?branch=master)](https://travis-ci.org/dopplertask/doppler)

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
      "actionType": "PrintAction",
      "fields": {
        "message": "This is an example task."
      }
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
  "automationId": 13,
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

#### TimedWait
##### Variables
* seconds: Amount of seconds to wait

#### LinkedTaskAction
##### Variables
* linkedTaskId: Id of another task

## Authors

* **Feras Wilson** 
* **Jack Kourie** 

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details