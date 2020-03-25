import * as React from "react";
import EditActionModal from "./EditActionModal";
import SaveModal from "./SaveModal";
import RunTaskModal from "./RunTaskModal";

class MainApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            availableActions: [],
            app: {},
            selectedAction: {userData: {customData: {}}},
            saveDialogVisible: false,
            taskName: "workflow",
            start: false
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
    }

    setStartToFalse() {
        this.setState({
            start: false
        })
    }

    componentDidMount() {
        /*      var client = Stomp.client("ws://localhost:61614/stomp", "v11.stomp");
              var headers = { id:'JUST.FCX', ack: 'client'};
              client.connect("admin", "admin", function () {
                  client.subscribe("/queue/taskexecution_destination",
                                   function (message) {
                                       alert(message);
                                       message.ack();
                                   }, headers);
              });
      */
        let createConnection = function (sourcePort, targetPort) {

            let conn = new draw2d.Connection({
                router: new draw2d.layout.connection.InteractiveManhattanConnectionRouter(),
                color: "#00ff17",
                radius: 20,
                outlineColor: "#30ff30",
                source: sourcePort,
                target: targetPort,
                stroke: 2
            });

            console.log("Source PORT: " + conn.getSource());
            console.log("Target PORT: " + conn.getTarget());

            return conn;

        };

        let app = new example.Application();

        app.view.installEditPolicy(new draw2d.policy.connection.DragConnectionCreatePolicy({
            createConnection: createConnection
        }));

        $.get("/task/actions", (data, status) => {
            let actions = [];
            data.actions.forEach(element => {
                let action;
                if (element.name == "IfAction") {
                    action = new IfAction({x: 550, y: 340, width: 120, height: 120, userData: element});
                } else if (element.name == "StartAction") {
                    action = new StartFigure({x: 550, y: 340, width: 120, height: 120, userData: element});
                } else {
                    action = new BetweenFigure({x: 550, y: 340, width: 120, height: 120, userData: element});
                }

                actions.push(action);

                // Auto add the start action
                if (action.userData.name == "StartAction") {
                    app.view.add(new StartFigure({x: 50, y: 340, width: 120, height: 120, userData: action.userData}))
                }

            })

            // Set state.
            this.setState(prevState => ({
                availableActions: actions
            }));

        });

        this.setState({app: app});
    }

    createNode(actionObject) {
        let action;
        if (actionObject.userData.name == "IfAction") {
            action = new IfAction({x: 550, y: 340, width: 120, height: 120, userData: actionObject.userData});
            action.onDoubleClick = () => this.editModelForFigure();
            action.userData.customData = {
                "scriptLanguage": "VELOCITY"
            };
        } else if (actionObject.userData.name == "StartAction") {
            action = new StartFigure({x: 550, y: 340, width: 120, height: 120, userData: actionObject.userData});
            action.onDoubleClick = () => this.editModelForFigure();
            action.userData.customData = [];

        } else {
            action = new BetweenFigure({x: 550, y: 340, width: 120, height: 120, userData: actionObject.userData});
            action.onDoubleClick = () => this.editModelForFigure();
            action.userData.customData = {
                "scriptLanguage": "VELOCITY"
            };
        }

        return action;
    }

    discardActionSettings() {
        this.setState({selectedAction: {}});
    }

    prepareJSON(resultCallback) {
        let writer = new draw2d.io.json.Writer();
        let outputBody = {name: this.state.taskName, description: "", parameters: [], actions: [], connections: []};
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
                            ports: currentActionPorts, ...json[i].userData.customData,
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

    }

    saveActionSettings() {
        let currentSelection = this.state.app.view.selection.all.data[0];
        currentSelection.userData = this.state.selectedAction.userData;
        console.log(JSON.stringify(currentSelection.userData))
    }

    render() {
        return <div id="container">

            <div className="row h-100 p-0 m-0">

                <div className="col-sm-2 col-md-2 col-lg-2">
                    <img src="images/logo.png" alt="" className="w-100"/>
                    <nav className="nav flex-column">
                        {this.state.availableActions.map(action => (
                            <a href="#" className="nav-link" key={action.userData.name}
                               onClick={() => this.state.app.view.add(this.createNode(action))}>{action.userData.name}</a>
                        ))}
                    </nav>

                    <div><a href="#" onClick={this.downloadWorkflow} className="btn btn-primary">Export Workflow</a>
                    </div>
                    <div><a href="#" onClick={() => $("#saveModal").modal("show")} className="btn btn-primary">Save
                        Workflow</a></div>
                    <div><a href="#" onClick={() => {
                        this.setState({start: true})
                        $("#runTaskModal").modal("show");

                    }} className="btn btn-primary">Run
                        Workflow</a></div>
                </div>
                <div className="col-sm-10 col-md-10 col-lg-10 m-0 p-0">
                    <div id="canvas" className="w-100 h-100"></div>
                </div>
            </div>
            <div id="content">


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
                taskName={this.state.taskName} start={this.state.start} setStartToFalse={this.setStartToFalse}/>

        </div>;
    }

    saveWorkflow() {
        // Validation before sending to API.
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
    }
}

export default MainApp;