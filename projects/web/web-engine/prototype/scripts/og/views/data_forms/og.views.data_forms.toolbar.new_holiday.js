/*
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.data_forms.toolbar.new_holiday',
	dependencies: [
		'og.api.rest',
		'og.common.routes',
		'og.common.util.ui.dialog'
	],
	obj: function () {	
		var api = og.api, common = og.common, routes = common.routes, ui = common.util.ui, view,
        create_holiday = function () {
    		$(this).dialog('close');
    		api.rest.holidays.put({
    			handler: function (result) {
    				var args = routes.current().args, rule = view.rules.load_item;
    				if (result.error) return view.error(result.message);
    				view.search(args);
                    routes.go(routes.hash(rule, args, {add: {id: result.meta.id}, del: ['version']}));
/*                    if (result.meta.id)
                        return routes.go(routes.hash(rule, args, {add: {id: result.meta.id}}));
                    routes.go(routes.hash(view.rules.load, args));*/
                },                
    			holiday_type: ui.dialog({return_field_value: 'holiday_type'}),
    			identifier: ui.dialog({return_field_value: 'identifier'}),
    			weekend_type: ui.dialog({return_field_value: 'weekend_type'})
    		});
    	};
		return function () {
			var identifierHint = function (select) {
				console.log(select.value);
			};
			og.common.util.ui.dialog({
				width: 400, 
				height: 350,
				type: 'input',
				title: 'new',
				fields: [
                    {type: 'select', name: 'Holiday Type', id: 'holiday_type',
                        options: [
                            {name: 'CURRENCY', value: 'CURRENCY'},
                            {name: 'BANK', value: 'BANK'},
                            {name: 'SETTLEMENT', value: 'SETTLEMENT'},
                            {name: 'TRADING', value: 'TRADING'},
                            {name: 'CUSTOM', value: 'CUSTOM'},
                        ]
                    },
                    {type: 'input', name: 'Identifier', id: 'identifier'},
                    {type: 'select', name: 'Weekend Type', id: 'weekend_type',
                    	options: [
                    		{name: 'SATURDAY/SUNDAY', value: 'SATURDAY_SUNDAY'},
                    		{name: 'FRIDAY/SATURDAY', value: 'FRIDAY_SATURDAY'},
                    		{name: 'THURSDAY/FRIDAY', value: 'THURSDAY_FRIDAY'}
                    	]
                    }
				],
				buttons: {
					'OK': create_holiday,
					'Cancel': function () {$(this).dialog('close'); }
				},
				
			});
		};
	}
});