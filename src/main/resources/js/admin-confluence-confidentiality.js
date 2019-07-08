/* global AJS:false, Confluence:false */
$(function () {
    AJS.toInit($ => {
        const capitalize = str => str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        const url = `${AJS.contextPath()}/rest/confluence-confidentiality/1.0/administer/confidentiality/${AJS.Meta.get("space-key")}`;

        AJS.$(document).on('click', '#save-button', function () {
            var that = this;
            that.preventDefault();
            if (!that.isBusy()) {
                that.busy();
                setTimeout(function () {
                    that.idle();
                }, 2000);
            }
        });

        //may be useful, for user authentication
        const remoteUser = AJS.Meta.get("remote-user-key");
        const remoteUserKey = AJS.Meta.get("remote-user-key");
        const baseUrl = AJS.Meta.get("base-url");
        const urlContext = AJS.Meta.get("context-path");
        const spaceKey = AJS.Meta.get("space-key");
        const spaceName = AJS.Meta.get("space-name");
        const altUserToken = AJS.Meta.get("atl-token");
        const confidentialityEnabled = AJS.$.find('#confidentiality-enabled').checked;

        const editGroupView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                const $select = $("<select name='group' class='select'>" +
                    "<option value='Friends'>Friends</option>" +
                    "<option value='Family'>Family</option>" +
                    "<option value='Work'>Work</option>" +
                    "</select>");

                $select.val(self.value); // select currently selected
                return $select;
            }
        });

        const nameReadView = AJS.RestfulTable.CustomReadView.extend({
            render: function (self) {
                const value = (self.value) || "";
                return $("<strong />").text(capitalize(value));
            }
        });

        const checkboxEditView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                console.log(self);
                const $select = $("<input type='checkbox' class='ajs-restfultable-input-" + self.name + "' />" +
                    "<input type='hidden' name='" + self.name + "'/>");
                return $select;
            }
        });

        const auiEvents = ["ROW_ADDED", "REORDER_SUCCESS", "ROW_REMOVED", "EDIT_ROW"];
        _.each(auiEvents, function(eventName){
            $(AJS).one(AJS.RestfulTable.Events[eventName], function(){
                AJS.messages.info("#message-area", {
                    id: eventName,
                    title: "test",
                    body: eventName + " fired on AJS. Used for testing AJS events."
                });
            });
        });

        const table = new AJS.RestfulTable({
            el: jQuery("#space-confidentiality-table"),
            id: 'space-confidentiality-table',
            submitAccessKey: 'S',
            autoFocus: false,
            allowReorder: true,
            createPosition: 'bottom',
            loadingMsg: "Loading, please wait",
            resources: {
                all: url, // resource to get all confidentiality options
                self: url // resource to get single confidentiality url/{id}
            },
            columns: [
                {
                    id: "confidentiality",
                    header: "Confidentiality",
                    allowEdit: true,
                    fieldName: 'confidentiality',
                    emptyText: 'Type to add new confidentiality',
                    readView: nameReadView
                    // editView: editGroupView
                }
            ],
            deleteConfirmationCallback: function (model) {
                AJS.$("#restful-table-model")[0].innerHTML = "Remove confidentiality option: '" + model.id + "' ?";
                AJS.dialog2("#delete-confirmation-dialog").show();
                return new Promise(function (resolve, reject) {
                    AJS.$("#dialog-submit-button").on('click', function (e) {
                        resolve();
                        e.preventDefault();
                        AJS.dialog2("#delete-confirmation-dialog").hide();
                    });
                    AJS.$(".aui-dialog2-header-close, #warning-dialog-cancel").on('click', function (e) {
                        reject();
                        e.preventDefault();
                        AJS.dialog2("#delete-confirmation-dialog").hide();
                    });
                });
            }
        });
    });
})(AJS.$);
