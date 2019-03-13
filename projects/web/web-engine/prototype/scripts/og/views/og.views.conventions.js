/*
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.conventions',
	dependencies: [
		'og.api.rest',
		'og.api.text',
		'og.common.masthead.menu',
		'og.common.routes',
		'og.common.search_results.core',
		'og.common.util.history',
		'og.common.util.ui.dialog',
		'og.common.util.ui.message',
		'og.common.util.ui.toolbar',
		'og.views.common.layout',
		'og.views.common.state',
		'og.views.convention_forms.default'
	],
	obj: function () {
		var api = og.api, 
			common = og.common, 
			details = common.details, 
			events = common.events,
			history = common.util.history, 
			masthead = common.masthead, 
			routes = common.routes,
			ui = common.util.ui,
			form_inst, 
			form_state, 
			unsaved_txt = 'You have unsaved changes to', 
			toolbar_action = false,
			search, 
			suppress_update = false, 
			module = this, 
			view, 
			page_name = module.name.split('.').pop(),
			current_type, 
			convention_types = [],
			toolbar_buttons = {
				'new': function () {
					toolbar_action = true;
					ui.dialog({
						type: 'input',
						title: 'Add convention',
						width: 400,
						height: 190,
						fields: [{
								type: 'optselect',
								name: 'Convention Type', 
								id: 'convention_type', 
								options: convention_types,
								value: function () { return current_type; }
						}],
						buttons: {
							'OK': function () {
								var convention_type = ui.dialog({ return_field_value: 'convention_type' });
								$(this).dialog('close');
								routes.go(routes.hash(view.rules.load_new, routes.current().args, {
									add: {convention_type: convention_type}
								}));
							},
							'Cancel': function () {$(this).dialog('close'); }
						}
					});
				},
				'delete': function () {
					toolbar_action = true;
					ui.dialog({
						type: 'confirm',
						title: 'Delete convention?',
						width: 400,
						height: 190,
						message: 'Are you sure you want to permanently delete this convention?',
						buttons: {
							'Delete': function () {
								var args = routes.current().args;
								suppress_update = true;
								form_inst = form_state = null;
								$(this).dialog('close');
								api.rest.conventions.del({
									handler: function (result) {
										if (result.error) {
											return view.error(result.message);
										}
										routes.go(routes.hash(view.rules.load, args, {del: ['id']}));
										setTimeout(function () { view.search(args); });
									},
									id: routes.current().args.id
								});
							},
							'Cancel': function () { $(this).dialog('close'); }
						}
					});
				},
			},
			toolbar = function (options) {
				ui.toolbar(options);
				if (convention_types.length) {
					return;
				}
				$('.OG-tools .og-js-new').addClass('OG-disabled').unbind();
				api.rest.conventions.get({
					meta: true,
					handler: function (result) {
						convention_types = result.data.groups;
						ui.toolbar(options);
					},
					cache_for: 60 * 60 * 1000
				});
			},
			details_page = function (args, config) {
				var rest_options, 
				is_new = !!config, 
				rest_handler = function (result) {
					var details_json = result.data, 
						convention_type, 
						render_type, 
						render_options;
					if (result.error) {
						view.notify(null);
						return view.error(result.message);
					}
					current_type = details_json.template_data.configJSON ? details_json.template_data.configJSON.data['0'][0].split('.').reverse()[0] : details_json.template_data.type;
					convention_type = current_type.toLowerCase();
					if (is_new) {
						if (!result.data) {
							return view.error('No template for: ' + config);
						}
						if (!result.data.template_data.configJSON) {
							result.data.template_data.configJSON = {};
						}
						result.data.template_data.name = 'UNTITLED';
						result.data.template_data.configJSON.name = 'UNTITLED';
					} else {
						history.put({ name: details_json.template_data.name, item: 'history.' + page_name + '.recent', value: routes.current().hash });
					}
					if (og.views.convention_forms[convention_type]) {
						render_type = convention_type;
					} else {
						render_type = 'default';
					}
					render_options = {
						is_new: is_new,
						data: details_json,
						loading: view.notify.partial('saving...'),
						save_new_handler: function (result) {
							var args = routes.current().args;
							view.notify(null);
							if (result.error) {
								return view.error(result.message);
							}
							toolbar_action = true;
							view.search(args);
							routes.go(routes.hash(view.rules.load_item, args, { add: {id: result.meta.id} }));
						},
						save_handler: function (result) {
							var args = routes.current().args;
							view.notify(null);
							if (result.error) {
								return view.error(result.message);
							}
							view.notify('saved');
							setTimeout(function () {
								view.notify(null);
								view.search(args);
								view.details(args);
							}, 300);
						},
						handler: function (form) {
							var json = details_json.template_data,
								error_html = '<section class="OG-box og-box-glass og-box-error OG-shadow-light">This configuration has been deleted</section>';
							if (json.deleted) {
								$('.OG-layout-admin-details-north').html(error_html);
								view.layout.inner.sizePane('north', '0');
								view.layout.inner.open('north');
							} else {
								view.layout.inner.close('north');
								$('.OG-layout-admin-details-north').empty();
							}
							if (is_new || json.deleted) {
								toolbar({
									buttons: [
										{id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
										{id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
										{id: 'save', tooltip: 'Save', handler: form.submit.partial({as_new: true})},
										{id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
										{id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
									],
									location: '.OG-tools'
								});
							} else {
								toolbar({
									buttons: [
										{id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
										{id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
										{id: 'save', tooltip: 'Save', handler: function () {
											suppress_update = true;
											form.submit();
										}},
										{id: 'saveas', tooltip: 'Save as', handler: form.submit.partial({as_new: true})},
										{id: 'delete', tooltip: 'Delete', handler: toolbar_buttons['delete']}
									],
									location: '.OG-tools'
								});
							}
							view.notify(null);
							setTimeout(view.layout.inner.resizeAll);
							form_inst = form;
							form_state = form_inst.compile();
						},
						selector: '.OG-layout-admin-details-center .ui-layout-content',
						type: details_json.template_data.type
					};
					$(render_options.selector).css({'overflow': 'auto'});
					og.views.convention_forms[render_type](render_options);
				};
				view.layout.inner.options.south.onclose = null;
				view.layout.inner.close('south');
				rest_options = {
						dependencies: view.dependencies,
						update: is_new ? (void 0) : view.update,
						handler: rest_handler,
						loading: view.notify.partial({0: 'loading...', 3000: 'still loading...'})
				};
				if (config) {
					rest_options.template = config;
				} else {
					rest_options.id = args.id;
				}
				api.rest.conventions.get(rest_options);
			};
        events.on('hashchange', function () {
            if (!form_inst && !form_state || toolbar_action) {
                toolbar_action = false;
                return void 0;
            }
            var msg = unsaved_txt + ' ' + (form_state ? form_state.data.name : "") + '. \n\n' +
                'OK to discard changes \n' +
                'Cancel to continue editing';
            if (!Object.equals(form_state, form_inst.compile()) && !window.confirm(msg)) {
                return false;
            }
            form_inst = form_state = null;
            return true;
        });
        events.on('unload', function () {
            if (!form_inst && !form_state) {
                return true;
            }
            if (!Object.equals(form_state, form_inst.compile())) {
                return false;
            }
            form_inst = form_state = null;
            return true;
        });
        var build_menu = function (list) {
            var menu_html = '<select class="og-js-type-filter" style="width: 80px"><option value="">Type</option>';
            list.forEach(function (entry) {
                menu_html += '<optgroup label="' + entry.group + '">';
                entry.types.forEach(function (type) {
                    menu_html += '<option value="' + type.value + '">' + type.name + '</option>';
                });
                menu_html += '</optgroup>';
            });
            menu_html += "</select>";
            return menu_html;
        };
		return view = $.extend(view = new og.views.common.Core(page_name), {
			default_details: function () {
				og.views.common.default_details(page_name, view.name, null, toolbar.partial(view.options.toolbar['default']));
			},
			details: details_page,
			load: function (args) {
				view.layout = og.views.common.layout;
				view.check_state({ 
					args: args, 
					conditions: [
						{ new_page: function (args) {
							view.search(args);
							masthead.menu.set_tab(page_name);
						} }
					]});
				if (!args.id && !args.convention_type) {
					view.default_details();
				}
			},
            load_filter: function (args) {
                view.filter = function () {
                    var filter_name = view.options.slickgrid.columns[0].name;
                    if (!filter_name || filter_name === 'loading') {// wait until type filter is populated
                        return setTimeout(view.filter, 500);
                    }
                    search.filter();
                };
                view.check_state({args: args, conditions: [{new_value: 'id', method: function (args) {
                    view[args.id ? 'load_item' : 'load'](args);
                }}]});
                view.filter();
            },
			load_new: function (args) {
				view.check_state({args: args, conditions: [{new_page: view.load}]});
				view.details(args, args.convention_type);
			},
			options: {
				slickgrid: {
					'selector': '.OG-js-search', 'page_type': page_name,
					'columns': [
						{id: 'type', toolTip: 'type', name: null, field: 'type', width: 100},
						{id: 'name', field: 'name', width: 300, cssClass: 'og-link', toolTip: 'name',
							name: '<input type="text" placeholder="Name" class="og-js-name-filter" style="width: 280px">'}
					]
				},
				toolbar: {
					'default': {
						buttons: [
							{id: 'new', tooltip: 'New', handler: toolbar_buttons['new']},
                            {id: 'import', tooltip: 'Import', enabled: 'OG-disabled'},
                            {id: 'save', tooltip: 'Save', enabled: 'OG-disabled'},
                            {id: 'saveas', tooltip: 'Save as', enabled: 'OG-disabled'},
							{id: 'delete', tooltip: 'Delete', enabled: 'OG-disabled'}
						],
						location: '.OG-tools'
					}
				}
			},
		    rules: $.extend(view.rules(['name', 'type']), {
		    	load_new: {
		    		route: '/' + page_name + '/new/:convention_type/name:?/type:?',
		    		method: module.name + '.load_new'
		    	}
		    }),
            search: function (args) {
                if (!search) {
                    search = common.search_results.core();
                }
                if (view.options.slickgrid.columns[0].name === 'loading') {
                    return setTimeout(view.search.partial(args), 500);
                }
                if (view.options.slickgrid.columns[0].name === null) {
                    return api.rest.conventions.get({
                        meta: true,
                        dependencies: [], // if the page changes, cancel this request
                        handler: function (result) {
                            if (result.error) {
                                return view.error(result.message);
                            }
                            view.options.slickgrid.columns[0].name = build_menu(result.data.groups);
                            view.search(args);
                        },
                        loading: function () {
                            view.options.slickgrid.columns[0].name = 'loading';
                            ui.message({location: '.OG-js-search', message: {0: 'loading...', 3000: 'still loading...'}});
                        },
                        cache_for: 15 * 1000
                    });
                }
                search.load(view.options.slickgrid);
            },
            update: function (delivery) {
                view.search(routes.current().args);
                if (suppress_update) {
                    return suppress_update = false;
                }
                ui.dialog({
                    type: 'confirm',
                    width: 400,
                    height: 190,
                    title: delivery.reset ? 'The connection has been reset!' : 'This item has been updated!',
                    message: 'Would you like to refresh this page (all changes will be lost) or continue working?',
                    buttons: {
                        'Refresh': function () {
                            $(this).dialog('close');
                            view.details(routes.current().args);
                        },
                        'Continue Working': function () {
                            $(this).dialog('close');
                            og.api.rest.conventions.get({ id: routes.current().args.id, update: view.update, dependencies: view.dependencies });
                        }
                    }
                });
            }
		});
	}
})