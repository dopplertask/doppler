function makeid(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

let signs = new draw2d.shape.icon.Svg(10, 10);
signs.setWidth(30);
signs.setHeight(30);
signs.createSet = function () {
    return this.canvas.paper.path("M507.31 84.69L464 41.37c-6-6-14.14-9.37-22.63-9.37H288V16c0-8.84-7.16-16-16-16h-32c-8.84 0-16 7.16-16 16v16H56c-13.25 0-24 10.75-24 24v80c0 13.25 10.75 24 24 24h385.37c8.49 0 16.62-3.37 22.63-9.37l43.31-43.31c6.25-6.26 6.25-16.38 0-22.63zM224 496c0 8.84 7.16 16 16 16h32c8.84 0 16-7.16 16-16V384h-64v112zm232-272H288v-32h-64v32H70.63c-8.49 0-16.62 3.37-22.63 9.37L4.69 276.69c-6.25 6.25-6.25 16.38 0 22.63L48 342.63c6 6 14.14 9.37 22.63 9.37H456c13.25 0 24-10.75 24-24v-80c0-13.25-10.75-24-24-24z");
}
signs.onDoubleClick = function () {
    this.getParent().onDoubleClick();
}

let IfAction = draw2d.shape.node.Between.extend({


    init: function (attr) {
        this._super(attr);
        this.setBackgroundColor("#FFF");
        this.setRadius(12);
        this.setResizeable(false);
        this.setWidth(80);
        this.setHeight(80);
        this.createPort("output");
        this.getPorts().each(function (i, port) {
            port.setName(port.getId())
        })
        console.log(this.getPorts());
        let label = new draw2d.shape.basic.Label({text: "IfAction"});
        label.setStroke(0);
        this.add(label, new draw2d.layout.locator.BottomLocator(this));
        this.add(signs, new draw2d.layout.locator.CenterLocator(this));
    },

    /**
     * @method
     * Called if the user drop this element onto the dropTarget.
     *
     * In this Example we create a "smart insert" of an existing connection.
     * COOL and fast network editing.
     *
     * @param {draw2d.Figure} dropTarget The drop target.
     * @param {Number} x the x coordinate of the drop
     * @param {Number} y the y coordinate of the drop
     * @param {Boolean} shiftKey true if the shift key has been pressed during this event
     * @param {Boolean} ctrlKey true if the ctrl key has been pressed during the event
     * @private
     **/
    onDrop: function (dropTarget, x, y, shiftKey, ctrlKey) {
        console.log("onDrop")
        // Activate a "smart insert" If the user drop this figure on connection
        //
        if (dropTarget instanceof draw2d.Connection) {
            let oldSource = dropTarget.getSource();
            let oldTarget = dropTarget.getTarget();

            let insertionSource = this.getOutputPort(0)
            let insertionTarget = this.getInputPort(0)

            // ensure that oldSource ---> insertionTarget.... insertionSource ------>oldTarget
            //
            if (oldSource instanceof draw2d.InputPort) {
                oldSource = dropTarget.getTarget();
                oldTarget = dropTarget.getSource();
            }

            let stack = this.getCanvas().getCommandStack();

            let cmd = new draw2d.command.CommandReconnect(dropTarget);
            cmd.setNewPorts(oldSource, insertionTarget);
            stack.execute(cmd);

            let additionalConnection = createConnection();
            cmd = new draw2d.command.CommandConnect(oldTarget, insertionSource);
            cmd.setConnection(additionalConnection);
            stack.execute(cmd);
        }
    }

});
