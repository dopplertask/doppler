
let StartFigure = draw2d.shape.node.Start.extend({

    init : function(attr)
    {
        this._super(attr);
        this.setBackgroundColor("#FFF");
        this.setRadius(12);
        this.setResizeable(false);
        this.setWidth(80);
        this.setHeight(80);
        this.setLabels(attr.labels);
        this.getPorts().each(function (i, port) {
          port.setName(port.getId())
        })
        let label = new draw2d.shape.basic.Label({text: attr.actionName});
        label.setStroke(0);
        this.add(label, new draw2d.layout.locator.CenterLocator(this));
    },

    setLabels: function(amount) {
      this.labels = amount;
      return this;
    },

    getLabels: function getLabels() {
      return this.labels;
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
    onDrop:function(dropTarget, x, y, shiftKey, ctrlKey)
    {
      console.log("onDrop")
    	// Activate a "smart insert" If the user drop this figure on connection
    	//
    	if(dropTarget instanceof draw2d.Connection) {
        let oldSource = dropTarget.getSource();
        let oldTarget = dropTarget.getTarget();

        let insertionSource = this.getOutputPort(0)
        let insertionTarget = this.getInputPort(0)

        // ensure that oldSource ---> insertionTarget.... insertionSource ------>oldTarget
        //
        if (oldSource instanceof draw2d.InputPort){
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
    },

    getPersistentAttributes: function () {
      let memento = this._super();

      // add all decorations to the memento
      //
      memento.labels = this.labels


      return memento
    },

});
