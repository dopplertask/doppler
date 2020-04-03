import * as React from "react";
import RunTaskModal from "./RunTaskModal";
import EditActionModal from "./EditActionModal";
import SaveModal from "./SaveModal";
import OpenTaskModal from "./OpenTaskModal";
import TaskSettings from "./TaskSettings";

class MainApp extends React.Component {

    constructor(props) {
        super(props);
        let unsavedTaskNamePrefix = "_" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        this.state = {
            availableActions: [],
            app: {},
            parameters: [],
            selectedAction: {userData: {customData: {}}},
            saveDialogVisible: false,
            taskName: "task" + unsavedTaskNamePrefix,
            start: false,
            saved: false
        }

        this.createNode = this.createNode.bind(this);
        this.prepareJSON = this.prepareJSON.bind(this);
        this.editModelForFigure = this.editModelForFigure.bind(this);
        this.saveActionSettings = this.saveActionSettings.bind(this);
        this.discardActionSettings = this.discardActionSettings.bind(this);
        this.saveWorkflow = this.saveWorkflow.bind(this);
        this.closeSaveDialog = this.closeSaveDialog.bind(this);
        this.handleSaveModalField = this.handleSaveModalField.bind(this);
        this.downloadWorkflow = this.downloadWorkflow.bind(this);
        this.updateFieldData = this.updateFieldData.bind(this);
        this.setStartToFalse = this.setStartToFalse.bind(this);
        this.setStartToTrue = this.setStartToTrue.bind(this);
        this.saveSettings = this.saveSettings.bind(this);
        this.openTask = this.openTask.bind(this);
        this.initApp = this.initApp.bind(this);
        this.newWorkflow = this.newWorkflow.bind(this);

    }

    setStartToFalse() {
        this.setState({
                          start: false
                      })
    }

    setStartToTrue() {
        this.setState({
                          start: true
                      })
    }

    newWorkflow() {
        function createNewTask() {
            let unsavedTaskNamePrefix = "_" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2,
                                                                                                                                 15);
            this.state.app.view.clear();
            this.state.app.view.add(new StartFigure({
                                                        x: 50,
                                                        y: 340,
                                                        width: 120,
                                                        height: 120,
                                                        userData: this.state.availableActions.find(
                                                            availableAction => availableAction.name == "StartAction")
                                                    }));

            this.setState({
                              taskName: "task" + unsavedTaskNamePrefix,
                              parameters: [],
                              saved: false
                          });
        }

        if (!this.state.saved && confirm("You have unsaved changes. Do you want to continue?")) {
            createNewTask.call(this);
        } else if (this.state.saved) {
            createNewTask.call(this);
        }
    }

    componentDidMount() {
        let app = this.initApp();

        this.setState({app: app}, callback => {
            $.get("/task/actions", (data, status) => {
                let actions = [];
                data.actions.forEach(element => {
                    element.customData = [];
                    element.propertyInformationList.map(pi => {
                        if (pi.type === "MAP") {
                            element.customData[pi.name] = []
                        } else if (pi.type === "BOOLEAN") {
                            if (pi.defaultValue == "true") {
                                element.customData[pi.name] = true || "";
                            } else {
                                element.customData[pi.name] = false || "";
                            }
                        } else {
                            element.customData[pi.name] = pi.defaultValue || "";
                        }
                    })

                    actions.push(element);

                    // Auto add the start action
                    if (element.name == "StartAction") {
                        this.state.app.view.add(
                            new StartFigure({x: 50, y: 340, width: 120, height: 120, userData: element}))
                    }

                })

                // Set state.
                this.setState(prevState => ({
                    availableActions: actions
                }));

            });

        });

    }

    initApp() {
        let mainApp = this;
        var createConnection = function (sourcePort, targetPort) {

            let conn = new draw2d.Connection({
                                                 router: new draw2d.layout.connection.InteractiveManhattanConnectionRouter(),
                                                 color: "#334455",
                                                 radius: 20,
                                                 outlineColor: "#334455",
                                                 source: sourcePort,
                                                 target: targetPort,
                                                 stroke: 2
                                             });

            return conn;

        };

        let app = new example.Application();

        app.view.installEditPolicy(new draw2d.policy.connection.DragConnectionCreatePolicy({
                                                                                               createConnection: createConnection
                                                                                           }));
        app.view.getCommandStack().on("change", function (e) {
            if (e.isPostChangeEvent()) {
                mainApp.setState({saved: false})
            }
        });
        return app;
    }

    createNode(actionName) {
        let action;
        if (actionName == "IfAction") {
            action = new IfAction({
                                      x: 550,
                                      y: 340,
                                      width: 120,
                                      height: 120,
                                      userData: this.state.availableActions.find(
                                          availableAction => availableAction.name == actionName)
                                  });
            action.onDoubleClick = () => this.editModelForFigure();
        } else if (actionName == "StartAction") {
            action = new StartFigure({
                                         x: 550,
                                         y: 340,
                                         width: 120,
                                         height: 120,
                                         userData: this.state.availableActions.find(
                                             availableAction => availableAction.name == actionName)
                                     });
            action.onDoubleClick = () => this.editModelForFigure();
        } else {
            action = new BetweenFigure({
                                           x: 550,
                                           y: 340,
                                           width: 120,
                                           height: 120,
                                           userData: this.state.availableActions.find(
                                               availableAction => availableAction.name == actionName)
                                       });
            action.onDoubleClick = () => this.editModelForFigure();
        }

        return action;
    }

    discardActionSettings() {
        this.setState({selectedAction: {}});
    }

    prepareJSON(resultCallback) {
        let writer = new draw2d.io.json.Writer();
        let outputBody = {
            name: this.state.taskName,
            description: "",
            parameters: this.state.parameters,
            actions: [],
            connections: []
        };
        return new Promise(resolve => {
            writer.marshal(this.state.app.view, function (json) {
                for (var i = 0; i < json.length; i++) {
                    if (json[i].type == "draw2d.shape.node.Start") {
                        let currentActionPorts = [];
                        for (let j = 0; j < json[i].ports.length; j++) {
                            let decidedPortType = json[i].ports[j].type == "draw2d.OutputPort" ? "OUTPUT" : "INPUT";
                            currentActionPorts.push({externalId: json[i].ports[j].name, portType: decidedPortType})
                        }

                        outputBody.actions.push({
                                                    "@type": "StartAction",
                                                    ports: currentActionPorts,
                                                    guiXPos: json[i].x,
                                                    guiYPos: json[i].y
                                                });
                    }
                    if (json[i].type == "draw2d.shape.node.Between") {
                        let currentActionPorts = [];
                        for (let j = 0; j < json[i].ports.length; j++) {
                            let decidedPortType = json[i].ports[j].type == "draw2d.OutputPort" ? "OUTPUT" : "INPUT";
                            currentActionPorts.push({externalId: json[i].ports[j].name, portType: decidedPortType})
                        }

                        outputBody.actions.push({
                                                    "@type": json[i].userData.name,
                                                    ...json[i].userData.customData,
                                                    ports: currentActionPorts,
                                                    guiXPos: json[i].x,
                                                    guiYPos: json[i].y
                                                });
                    }
                    if (json[i].type == "draw2d.Connection") {
                        outputBody.connections.push(
                            {source: {externalId: json[i].source.port}, target: {externalId: json[i].target.port}});
                    }
                }

                resultCallback(JSON.stringify(outputBody, null, 2));
            });
        });
    }

    downloadWorkflow() {

        this.prepareJSON(json => {

            let element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(JSON.stringify(json, null, 2)));
            element.setAttribute('download', "workflow.json");

            element.style.display = 'none';
            document.body.appendChild(element);

            element.click();

            document.body.removeChild(element);

        });

    }

    openTask(checksum) {
        let mainApp = this;
        $.ajax({
                   type: "GET",
                   url: "/task/" + checksum + "/checksum",
                   contentType: 'application/json',
                   success: task => {
                       this.state.app.view.clear();

                       task.actions.forEach((action, i) => {
                           let generatedAction = this.createNode(action["@type"]);
                           generatedAction.userData.customData = action;
                           generatedAction.x = action.guiXPos;
                           generatedAction.y = action.guiYPos;
                           let inputPortIndex = 0;
                           let outputPortIndex = 0;

                           action.ports.forEach((port, i) => {
                               if (port.portType == "INPUT") {
                                   generatedAction.getInputPort(inputPortIndex).setId(port.externalId);
                                   generatedAction.getInputPort(inputPortIndex).setName(port.externalId);
                                   inputPortIndex++;
                               } else {
                                   generatedAction.getOutputPort(outputPortIndex).setId(port.externalId);
                                   generatedAction.getOutputPort(outputPortIndex).setName(port.externalId);
                                   outputPortIndex++;
                               }
                           })

                           this.state.app.view.add(generatedAction);
                       })

                       // Very inefficient
                       // TODO: Improve performance
                       task.connections.forEach(connection => {
                           let conVisual = new draw2d.Connection({
                                                                     router: new draw2d.layout.connection.InteractiveManhattanConnectionRouter(),
                                                                     color: "#334455",
                                                                     radius: 20,
                                                                     outlineColor: "#334455",
                                                                     stroke: 2
                                                                 });

                           this.state.app.view.figures.data.forEach(figure => {
                               figure.outputPorts.data.forEach(outputPort => {
                                                                   if (outputPort.name == connection.source.externalId) {
                                                                       conVisual.setSource(outputPort);
                                                                   }
                                                               }
                               )
                               figure.inputPorts.data.forEach(inputPort => {
                                                                  if (inputPort.name == connection.target.externalId) {
                                                                      conVisual.setTarget(inputPort);
                                                                  }
                                                              }
                               )
                           })

                           this.state.app.view.add(conVisual);
                       })

                       mainApp.setState({
                                            taskName: task.name,
                                            parameters: task.parameters,
                                            saved: true
                                        });
                   }
                   ,
                   dataType: "json"
               }
        )
        ;
    }

    editModelForFigure() {
        this.setState({selectedAction: {}});
        let currentSelection = this.state.app.view.selection.all.data[0];
        this.setState({selectedAction: currentSelection});
        $("#actionEditModal").modal("show");
    }

    handleSaveModalField(event) {
        const target = event.target;
        let value = target.value;

        this.setState(prevState => ({
            taskName: value
        }));

        this.setState({saved: false});

    }

    saveActionSettings() {
        let currentSelection = this.state.app.view.selection.all.data[0];
        currentSelection.userData = this.state.selectedAction.userData;
        console.log(JSON.stringify(currentSelection.userData));
        this.setState({saved: false});
    }

    render() {
        let runBtn;
        if (this.state.saved) {
            runBtn = (<div><a href="#" onClick={() => {
                $("#runTaskModal").modal("show");

            }} className="btn btn-primary btn-block">Run
                Workflow</a></div>);
        } else {
            runBtn = (<div><a href="#" onClick={() => {
                alert("Please save the workflow first");
            }} className="btn btn-primary disabled btn-block">Run
                Workflow</a></div>);
        }
        return <div id="container">


            <div id="wrapper">

                <div id="sidebar-wrapper">
                    <nav id="spy">
                        <ul className="sidebar-nav nav">
                            <li className="sidebar-brand">
                                <span className="fa fa-home solo">Add action</span>
                            </li>
                            {this.state.availableActions.map(action => (
                                <li><a href="#" key={action.name}
                                       onClick={() => {
                                           this.setState({saved: false});
                                           this.state.app.view.add(this.createNode(action.name))
                                       }}><span className="fa fa-anchor solo">{action.name}</span></a></li>
                            ))}
                        </ul>
                    </nav>
                </div>

                <div id="page-content-wrapper">

                    <div className="row h-100 p-0 m-0">

                        <div className="col-sm-2 col-md-2 col-lg-2">
                            <nav id="spy">

                                <ul className="sidebar-nav nav">
                                    <li className="sidebar-brand">
                                        <span className="fa fa-home solo"><img src="images/logo.png" alt=""
                                        /></span>
                                    </li>
                                    <li><a href="#" onClick={this.newWorkflow}
                                           className="fa fa-home solo">New task</a>
                                    </li>
                                    <li><a href="#" onClick={() => $("#taskSettingsModal").modal("show")}
                                           className="fa fa-home solo">Task parameters</a>
                                    </li>
                                    <li><a href="#" onClick={this.downloadWorkflow} className="fa fa-home solo">Export
                                        Workflow</a>
                                    </li>
                                    <li><a href="#" onClick={() => $("#saveModal").modal("show")}
                                           className="fa fa-home solo">Save
                                        Workflow</a></li>
                                    <li><a href="#" onClick={() => $("#openTaskModal").modal("show")}
                                           className="fa fa-home solo">Open
                                        Workflow</a>
                                    </li>
                                    <li>
                                        <a href="#" onClick={() => $("#wrapper").toggleClass("active")}
                                           className="fa fa-home solo">Show/hide
                                            actions</a>
                                    </li>
                                </ul>
                                {runBtn}
                            </nav>

                            <br/>
                            Current task:<br/>
                            {!this.state.saved ? ("[UNSAVED]") : ("")} {this.state.taskName}
                        </div>
                        <div className="col-sm-10 col-md-10 col-lg-10 m-0 p-0">
                            <div id="canvas" className="w-100 h-100"></div>
                        </div>

                    </div>
                    <div id="content">


                    </div>
                </div>

            </div>


            <EditActionModal updateFieldData={this.updateFieldData}
                             saveActionSettings={this.saveActionSettings}
                             selectedAction={this.state.selectedAction}
                             discardActionSettings={this.discardActionSettings}/>

            <SaveModal
                saveWorkflow={this.saveWorkflow} closeSaveDialog={this.closeSaveDialog}
                taskName={this.state.taskName}
                handleSaveModalField={this.handleSaveModalField}/>

            <RunTaskModal
                taskName={this.state.taskName} start={this.state.start} setStartToFalse={this.setStartToFalse}
                parameters={this.state.parameters} setStartToTrue={this.setStartToTrue}/>

            <OpenTaskModal
                openTask={this.openTask} saved={this.state.saved}/>

            <TaskSettings parameters={this.state.parameters}
                          saveSettings={this.saveSettings}
            />

        </div>
            ;
    }

    saveSettings(parameters) {
        this.setState({
                          parameters: parameters
                      })

    }

    saveWorkflow() {
        // Validation before sending to API.
        let startActions = 0;
        this.state.app.view.figures.data.forEach(figure => {
            if (figure instanceof StartFigure) {
                startActions++;
            }
        })

        // Validate
        if (startActions > 1) {
            alert("Only one start action is allowed.");
            return;
        }
        console.log("Save workflow API call to backend")

        this.prepareJSON(json => {
            console.log(json);
            $.ajax({
                       type: "POST",
                       url: "/task",
                       data: json,
                       contentType: 'application/json',
                       success: success => {
                           console.log(success)
                           this.setState({saved: true})
                       },
                       dataType: "json"
                   });
        });

    }

    closeSaveDialog() {
        this.setState({saveDialogVisible: false});
    }

    updateFieldData(id, value) {
        this.setState(prevState => ({
            selectedAction: {
                ...prevState.selectedAction,
                userData: {
                    ...prevState.selectedAction.userData,
                    customData: {
                        ...prevState.selectedAction.userData.customData,
                        [id]: value
                    }
                }
            }
        }));
        this.setState({saved: false})
    }

}

export default MainApp;