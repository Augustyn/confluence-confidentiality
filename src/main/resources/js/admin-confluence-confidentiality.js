/* global AJS:false, Confluence:false */
AJS.toInit($ => {
    const capitalize = str => str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    const url = `${AJS.contextPath()}/rest/confluence-confidentiality/1.0/administer/confidentiality/${AJS.Meta.get("space-key")}`;

    const nameReadView = AJS.RestfulTable.CustomReadView.extend({
        render: function (self) {
            const value = (self.value) || "";
            return $("<span />").text(capitalize(value));
        }
    });

    new AJS.RestfulTable({
        el: jQuery("#space-confidentiality-table"),
        id: 'space-confidentiality-table',
        submitAccessKey: 'S',
        autoFocus: false,
        allowReorder: false, //to be fixed later on.
        createPosition: 'bottom',
        loadingMsg: "Loading, please wait",
        resources: {
            all: url, // resource to get all confidentiality options
            self: url // resource to get single confidentiality url/{id}
        },
        columns: [
            {
                id: "confidentiality",
                header: "<strong>Space confidentiality list:</strong>",
                allowEdit: true,
                fieldName: 'confidentiality',
                emptyText: 'Type to add new confidentiality',
                readView: nameReadView
                // editView: editGroupView
            }
        ],
        deleteConfirmationCallback: function (model) {
            AJS.$("#restful-table-model")[0].innerHTML = `Are you really sure you want to remove confidentiality option: '${model.id}'?`;
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
