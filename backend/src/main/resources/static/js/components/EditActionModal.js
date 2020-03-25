import * as React from "react";
import Editor from "./Editor";

class EditActionModal extends React.Component {

    constructor(props) {
        super(props);
        this.renderFields = this.renderFields.bind(this);
        this.valueChange = this.valueChange.bind(this);
        this.editorValueChange = this.editorValueChange.bind(this);
        this.state = {
            customData: this.props.selectedAction.userData.customData
        }

        console.log(this.props.selectedAction.userData.customData)
    }

    valueChange(event) {
        const target = event.target;
        let value = target.value;
        if (target.type == "checkbox") {
            value = target.checked;
        }

        this.props.updateFieldData(target.id, value);
    }

    editorValueChange(name, value) {

        this.props.updateFieldData(name, value)
    }


    renderFields() {
        let fields = [];
        if (this.props.selectedAction.userData != undefined && this.props.selectedAction.userData.propertyInformationList != undefined) {
            this.props.selectedAction.userData.propertyInformationList.map(property => {
                let temp;
                switch (property.type) {
                    case "STRING":
                        temp = <div className="form-group" key={property.name}><label
                            htmlFor={property.name}>{property.displayName}</label>
                            <Editor id={property.name} onChange={(value) => {

                                this.editorValueChange(property.name, value)
                            }}
                                    simple={true}
                                    value={this.props.selectedAction.userData.customData[property.name] || property.defaultValue || ""}
                                    scriptLanguage={this.props.selectedAction.userData.customData["scriptLanguage"].toLowerCase() || "velocity"}/>
                        </div>;
                        break;
                    case "MULTILINE":
                        temp = <div className="form-group" key={property.name}><label
                            htmlFor={property.name}>{property.displayName}</label>

                            <Editor id={property.name} onChange={(value) => {

                                this.editorValueChange(property.name, value)
                            }}
                                    value={this.props.selectedAction.userData.customData[property.name] || property.defaultValue || ""}
                                    scriptLanguage={this.props.selectedAction.userData.customData["scriptLanguage"].toLowerCase() || "velocity"}/>
                        </div>;
                        break;
                    case "NUMBER":
                        temp = <div className="form-group" key={property.name}><label
                            htmlFor={property.name}>{property.displayName}</label> <input className="form-control"
                                                                                          type="number"
                                                                                          id={property.name}
                                                                                          value={this.props.selectedAction.userData.customData[property.name] || property.defaultValue || ""}
                                                                                          onChange={this.valueChange}/>
                        </div>;
                        break;
                    case "BOOLEAN":
                        temp =
                            <div className="form-group" key={property.name}><label
                                htmlFor={property.name}>{property.displayName}</label> <input type="checkbox"
                                                                                              id={property.name}
                                                                                              checked={this.props.selectedAction.userData.customData[property.name] || property.defaultValue || false}
                                                                                              onChange={this.valueChange}/>
                            </div>;
                        break;
                    case "DROPDOWN":
                        temp =
                            <div className="form-group" key={property.name}><label
                                htmlFor={property.name}>{property.displayName}</label>
                                <select className="form-control" id={property.name}
                                        onChange={this.valueChange}
                                        value={this.props.selectedAction.userData.customData[property.name] || "VELOCITY"}>
                                    {property.options.map(selectOption => {
                                        return (<option key={selectOption.name}
                                                        checked={this.props.selectedAction.userData.customData[property.name] || property.defaultValue || false}
                                                        value={selectOption.name}>{selectOption.displayName}</option>)
                                    })}
                                </select>
                            </div>;
                        break;
                }

                fields.push(temp);
            })
        }
        return fields;
    }

    render() {

        return (<div className="modal fade" id="actionEditModal" tabIndex="-1" role="dialog"
                     aria-labelledby="actionEditModalLabel" aria-hidden="true">
            <div className="modal-dialog" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title" id="actionEditModalLabel">Modal title</h5>
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div className="modal-body" id="actionEditModalBody">
                        {this.renderFields()}
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" data-dismiss="modal"
                                onClick={this.props.discardActionSettings}>Close
                        </button>
                        <button type="button" data-dismiss="modal" className="btn btn-primary"
                                onClick={this.props.saveActionSettings}>Save
                            changes
                        </button>
                    </div>
                </div>
            </div>
        </div>);

    }


}

export default EditActionModal;