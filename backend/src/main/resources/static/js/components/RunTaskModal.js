import * as React from "react";

class RunTaskModal extends React.Component {

    constructor(props) {
        super(props);
        this.renderOutput = this.renderOutput.bind(this);
        this.cleanup = this.cleanup.bind(this);
        this.state = {
            taskName: this.props.taskName,
            start: this.props.start
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
            let json = {
                taskName: this.state.taskName,
                parameters: {"test": "test"}
            }
            $.ajax({
                type: "POST",
                url: "/schedule/task",
                data: JSON.stringify(json),
                contentType: 'application/json',
                success: success => {
                    console.log(success)
                },
                dataType: "json"
            });

            let client = Stomp.client("ws://localhost:61614/stomp", "v11.stomp");
            let headers = {id: 'JUST.FCX', ack: 'client'};
            let runTaskModal = this;
            client.connect("admin", "admin", function () {
                client.subscribe("/queue/taskexecution_destination",
                    function (message) {
                        let messageBody = JSON.parse(message.body);
                        jQuery("#outputDiv").append(messageBody.output + "<br>");
                        message.ack();

                        if (message.headers["lastMessage"] == "true") {
                            client.disconnect();
                            runTaskModal.cleanup();
                        }
                    }, headers);
            });
        }
    }

    cleanup() {
        this.props.setStartToFalse();
    }

    render() {
        return (<div className="modal fade" id="runTaskModal" tabIndex="-1" role="dialog"
                     aria-labelledby="runTaskModalLabel" aria-hidden="true">
            <div className="modal-dialog" role="document">
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
                        <div id="outputDiv"></div>


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