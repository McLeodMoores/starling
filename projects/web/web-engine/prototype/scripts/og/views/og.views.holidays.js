/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.holidays',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.routes',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.versions',
        'og.common.gadgets.manager'
    ],
    obj: function () {
        var api = og.api, common = og.common, gadgets = common.gadgets, gadgets_manager = common.gadgets.manager,
            details = common.details, history = common.util.history,
            routes = common.routes, ui = common.util.ui, module = this,
            page_name = module.name.split('.').pop(), json = {},
            view, holiday_name,
            toolbar_buttons = {
        		'import': og.views.data_forms.toolbar.upload_holidays,
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete holidays?',
                        width: 400, height: 190,
                        message: 'Are you sure you want to permanently delete <strong style="white-space: nowrap">' + holiday_name + '</strong>?',
                        buttons: {
                            'Delete': function () {
                                var args = routes.current().args, rest_options = {
                                    id: args.id,
                                    handler: function (result) {
                                        var args = routes.current().args, rule = view.rules.load;
                                        if (result.error) return view.error(result.message);
                                        routes.go(routes.hash(rule, args));
                                        setTimeout(function () {view.search(args); });
                                    }
                                };
                                $(this).dialog('close');
                                api.rest.holidays.del(rest_options);                            
                            },
                            'Cancel': function () {$(this).dialog('close'); }
                        }
                    });
                }
            },
            details_page = function (args, config) {
                var show_loading = !(config || {}).hide_loading, rest_options;
                view.layout.inner.options.south.onclose = null;
                view.layout.inner.close('south');
                rest_options = {
                    dependencies: view.dependencies,
                    update: view.update,
                    id: args.id,
                    loading: function () {if (show_loading) view.notify({0: 'loading...', 3000: 'still loading...'});}
                };
                $.when(api.rest.holidays.get(rest_options), api.text({module: module.name}))
                    .then(function (result, template) {
                        if (result.error) return view.notify(null), view.error(result.message);
                        var json = result.data, error_html = '\
                                <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                    These holiday data have been deleted\
                                </section>\
                            ',
                            $html = $.tmpl(template, json.template_data);
                        history.put({
                            name: holiday_name = json.template_data.name,
                            item: 'history.' + page_name + '.recent',
                            value: routes.current().hash
                        });
                        $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                        $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                        ui.toolbar(view.options.toolbar.active);
                        if (json.template_data && json.template_data.deleted) {
                            $('.OG-layout-admin-details-north').html(error_html);
                            view.layout.inner.sizePane('north', '0');
                            view.layout.inner.open('north');
                            $('.OG-tools .og-js-delete').addClass('OG-disabled').unbind();
                        } else {
                            view.layout.inner.close('north'), $('.OG-layout-admin-details-north').empty();
                            $('.OG-holiday .og-calendar').datepicker({
                                numberOfMonths: [4, 3],                     // Layout configuration
                                showCurrentAtPos: new Date().getMonth(),    // Makes the first month January
                                showButtonPanel: false,                     // Turns off default buttons
                                stepMonths: 12,                             // Pagination moves 1 year at a time
                                firstDay: 1,                                // Start the week on Monday
                                displayOnly: true,                          // This is an OG custom configuration
                                specialDates: json.dates                    // This is an OG custom configuration
                            });
                            if (show_loading) view.notify(null);
                            details.calendar_ui_changes(json.dates);
                            setTimeout(view.layout.inner.resizeAll);
                        }
                    });
            };
        return view = $.extend(view = new og.views.common.Core(page_name), {
            details: details_page,
            options: {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': page_name,
                    'columns': [
                        {
                            id: 'name', field: 'name', width: 100, cssClass: 'og-link', toolTip: 'name',
                            name:
                                '<input type="text" placeholder="name" class="og-js-name-filter" style="width: 80px;">'
                        },
                        {
                            id: 'type', field: 'type', width: 200, toolTip: 'type',
                            name: '<select class="og-js-type-filter" style="width: 180px">\
                                     <option value="">Type</option>\
                                     <option>CURRENCY</option>\
                                     <option>BANK</option>\
                                     <option>SETTLEMENT</option>\
                                     <option>TRADING</option>\
                                   </select>'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
//                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', handler: toolbar_buttons['import']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-tools'
                    },
                    active: {
                        buttons: [
                            {id: 'new', tooltip: 'New', enabled: 'OG-disabled'},
//                            {id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', handler: toolbar_buttons['import']},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
                            {id: 'delete', tooltip: 'Delete', divider: true, handler: toolbar_buttons['delete']},
//                            {id: 'versions', label: 'versions', handler: toolbar_buttons['versions']}
                        ],
                        location: '.OG-tools'
                    }
                }
            },
            rules: view.rules(['name', 'type'], ['sync', 'version'])
        });
    }
});