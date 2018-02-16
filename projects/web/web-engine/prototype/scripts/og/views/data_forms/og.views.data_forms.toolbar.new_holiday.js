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
		var api = og.api, common = og.common, routes = common.routes, ui = common.util.ui,
        create_holiday = function () {
    		$(this).dialog('close');
    		api.rest.holidays.put({
    			handler: function (result) {
    				var args = routes.current().args, rule = view.rules.load_item;
    				if (result.error) return view.error(result.message);
    				view.search(args);
                    if (result.meta.id)
                        return routes.go(routes.hash(rule, args, {add: {id: result.meta.id}}));
                    routes.go(routes.hash(view.rules.load, args));
                },                                
    			holiday_type: ui.dialog({return_field_value: 'holidayType'}),
    			identifier: ui.dialog({return_field_value: 'identifier'}),
    			weekend_type: ui.dialog({return_field_value: 'weekendType'})
    		});
    	};
		return function () {
			og.common.util.ui.dialog({
				width: 400, 
				height: 350,
				type: 'input',
				title: 'new',
				custom: '<iframe id="new" src="new_holiday.ftl" width="100%" height="250" marginheight="0" marginwidth="0" frameborder="0"/>',
				buttons: {
					'OK': create_holiday,
					'Cancel': function () {$(this).dialog('close'); }
				},
				
			});
		};
	}
});