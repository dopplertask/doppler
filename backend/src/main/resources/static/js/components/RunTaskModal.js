import * as React from "react";

class RunTaskModal extends React.Component {

    constructor(props) {
        super(props);
        this.renderOutput = this.renderOutput.bind(this);
        this.cleanup = this.cleanup.bind(this);
        this.state = {
            taskName: this.props.taskName,
            start: this.props.start,
            executionId: -1
        }
    }

    componentDidMount() {
        this.setState(
            {
                taskName: this.props.taskName,
                start: this.props.start
            })
    }

    componentDidUpdate(prevProps, prevState, snapshot) {

        if (prevProps.taskName != this.props.taskName) {
            this.setState(
                {
                    taskName: this.props.taskName
                })
        }

        if (prevProps.start != this.props.start) {
            this.setState(
                {
                    start: this.props.start
                }, () => {
                    if (this.props.start) {
                        jQuery("#outputDiv").empty();
                        this.renderOutput()
                    }
                })


        }
    }

    renderOutput() {
        if (this.state.start) {
            let runTaskModal = this;
            let json = {
                taskName: this.state.taskName,
                parameters: {"test": "test"}
            }
            $.ajax({
                type: "POST",
                url: "/schedule/task",
                data: JSON.stringify(json),
                contentType: 'application/json',
                success: execution => {
                    runTaskModal.setState({
                        executionId: execution.id
                    }, createStomp => {
                        let client = Stomp.client("ws://localhost:61614/stomp", "v11.stomp");
                        let headers = {
                            id: 'JUST.FCX',
                            ack: 'client',
                            selector: 'executionId=' + runTaskModal.state.executionId
                        };
                        client.connect("admin", "admin", function () {
                            client.subscribe("/queue/taskexecution_destination",
                                function (message) {
                                    let messageBody = JSON.parse(message.body);
                                    jQuery("#outputDiv").append(messageBody.output + "<br>");
                                    message.ack();

                                    if (message.headers["lastMessage"] == "true" && message.headers["executionId"] == runTaskModal.state.executionId) {
                                        client.disconnect();
                                        runTaskModal.cleanup();
                                    }
                                }, headers);
                        });
                    })
                },
                dataType: "json"
            });

        }
    }

    cleanup() {
        this.props.setStartToFalse();
    }

    render() {
        return (<div className="modal fade" id="runTaskModal" tabIndex="-1" role="dialog"
                     aria-labelledby="runTaskModalLabel" aria-hidden="true">
            <div className="modal-dialog modal-lg" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title" id="runTaskModalLabel">Run task</h5>
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div className="modal-body" id="runTaskModalBody">
                        {this.state.taskName}

                        <h4>Task execution output</h4>
                        <code id="outputDiv"></code>


                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" data-dismiss="modal"
                                onClick={this.props.closeSaveDialog}>Close
                        </button>

                    </div>
                </div>
            </div>
        </div>);

    }

}

export default RunTaskModal;