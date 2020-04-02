import * as React from "react";

class Editor extends React.Component {
    constructor(props) {
        super(props);
        this.valueChange = this.valueChange.bind(this);
        this.containerId = `editor_${props.id}`
        this.scriptLanguage = this.props.scriptLanguage;
    }

    valueChange(value) {
        this.props.onChange(value)
    }

    componentDidMount() {

        ace.config.set("basePath", "/");
        this.editor = window.ace.edit(this.containerId);

        this.editor.$blockScrolling = Infinity; // option suggested in the log by Ace Editor to disable the returning warning

        this.editor.session.setMode("ace/mode/" + this.scriptLanguage);

        this.editor.setAutoScrollEditorIntoView(true);
        this.editor.setShowPrintMargin(false);
        this.editor.setFontSize(16);

        this.editor.setOptions({
                                   enableBasicAutocompletion: true,
                                   enableSnippets: true,
                                   enableLiveAutocompletion: false
                               });

        this.editor.setOption("enableSnippets", true);
        this.editor.setOption("maxLines", this.props.simple ? 1 : 30);
        this.editor.setOption("minLines", this.props.simple ? 1 : 5);

        this.editor.setValue(this.props.value, -1);

        this.editor.getSession().on("change", () => this.valueChange(this.editor.getValue()));

    }

    render() {
        return <div id={this.containerId} style={{
            margin: "0.5em",
            marginLeft: 0,
            border: "1px solid lightgrey", width: "100%"
        }}/>
    }
}

export default Editor
