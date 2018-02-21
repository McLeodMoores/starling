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
	],
	obj: function () {
		var api = og.api, common = og.common, details = common.details, events = common.events,
			history = common.util.history, masthead = common.masthead, routes = common.routes,
			ui = common.util.ui,
			form_inst, form_state, unsaved_txt = 'You have unsaved changes to', toolbar_action = false,
			search, suppress_update = false, module = this, view, page_name = module.name.split('.').pop(),
			current_type, convention_types = [],
			toolbar_buttons = {
				'new': function () {
					toolbar_action = true;
					ui.dialog({
						type: 'input',
						title: 'Add convention',
						width: 400,
						height: 190,
						fields: [{type: 'optselect', name: 'Convention Type', id: 'convention_type', options: convention_types,
							value: function () { return current_type; }}],
						buttons: {
							'OK': function () {
								var convention_type = ui.dialog({ return_field_value: 'convention_type'});
								$(this).dialog('close');
								routes.go(routes.hash(view.rules.load_new, routes.current().args, {
									add: {convention_type: convention_type}
								}));
							},
							'Cancel': function () {$(this).dialog('close'); }
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
			details_page = function (args, new_convention_type) {
			
		    };
		return view = $.extend(view = new og.views.common.Core(page_name), {
			details: details_page,
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
							{id: 'new', tooltip: 'New', handler: toolbar_buttons['new']}
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
		    })
		});
	}
})