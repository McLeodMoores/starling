/*
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.convention_forms.iborindexconvention',
	dependencies: [
		'og.api.rest',
		'og.common.util.ui'
	],
	obj: function () {
		var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, Form = ui.Form;
		var constructor = function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
            loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
            orig_name = config.data.template_data.name,
            resource_id = config.data.template_data.object_id,
            save_new_handler = config.save_new_handler, save_handler = config.save_handler,
            master = config.data.template_data.configJSON.data, convention_type = config.type,
			form = new Form({
				module: 'og.views.forms.ibor-index-convention_tash',
				data: master,
				selector: selector
			}),
			form_id = '#' + form.id;
            form.dom();
		};
		return constructor;
	}
})