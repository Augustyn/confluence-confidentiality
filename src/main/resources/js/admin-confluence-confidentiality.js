/* global AJS:false, Confluence:false */
AJS.toInit($ => {
    const capitalize = str => str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    const url = `${AJS.contextPath()}/rest/confluence-confidentiality/1.0/administer/confidentiality/${AJS.Meta.get("space-key")}`;

    function ConfidentialityTable() {

        this.show = () => new AJS.RestfulTable({
            el: $("#space-confidentiality-table"),
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
            noEntriesMessage: "No confidentiality options.",
            deleteConfirmation: true,
            deleteConfirmationCallback: (model) => {
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
                })
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
            ]
        });
        this.clear = () => {
            AJS.$('#space-confidentiality-table').html('<span/>');
        };
    }

    const toggle = document.getElementById('toggle-confidentiality');
    toggle.tooltipOn = 'Confidentiality for the space is Enabled';
    toggle.tooltipOff = 'Confidentiality for the space is Disabled';

    toggle.addEventListener('change', () => {
        toggle.busy = true;
        $.ajax({
            url: `${AJS.contextPath()}/rest/confluence-confidentiality/1.0/administer/confidentiality/enabled/${AJS.Meta.get("space-key")}`,
            value: toggle.checked,
            type: "PUT",
            dataType: "json",
            contentType: "application/json"
        }).done((responseMessage, respStatus, resp) => {
            toggle.checked = responseMessage;
            const success = AJS.flag({
                type: 'success',
                title: `Success: ${resp.status} ${resp.statusText}`,
                body: `Changed confidentiality for space "${AJS.Meta.get("space-key")}", "${AJS.Meta.get("space-name")}" to: ${responseMessage}`
            });
            setTimeout(function () {
                success.close();
            }, 5000);
            const table = new ConfidentialityTable();
            toggle.checked ? table.show() : table.clear();
        }).error(resp => {
            toggle.checked = !toggle.checked;
            const fail = AJS.flag({
                type: 'error',
                title: `Failed: ${resp.status}`,
                body: `Failed to change confidentiality state for space ${AJS.Meta.get("space-key")}
                Message: ${resp.statusText}`
            });
            setTimeout(function () {
                fail.close();
            }, 5000);
        }).complete(() => {
            toggle.busy = false;
        });
    });

    const nameReadView = AJS.RestfulTable.CustomReadView.extend({
        render: (self) => {
            const value = (self.value) || "";
            return $("<span />").text(capitalize(value));
        }
    });

    AJS.$(document).bind(AJS.RestfulTable.Events.SERVER_ERROR, (resp, req) => {
        AJS.flag({
            type: 'error',
            title: `Error: ${resp.status}`,
            body: `Error while processing request. Please refresh page, to see current table status.`
        });
    });

    if (toggle && toggle.checked) {
        new ConfidentialityTable().show();
    }
});
