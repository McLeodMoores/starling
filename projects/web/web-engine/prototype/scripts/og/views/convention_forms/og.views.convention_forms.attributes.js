$.register_module({
	name: 'og.views.convention_forms.Attributes',
	dependencies: ['og.common.util.ui.Form'],
	obj: function () {
		var module = this,
			Block = og.common.util.ui.Block, 
			add_table = '.og.attributes-add-table';
		var Attributes = function (config) {
			var block = this, 
				id = og.common.id('attributes'),
				form = config.form,
				attr_data = config.attributes ? Object.keys(config.attributes).reduce(function (acc, val)) {
					return acc.concat({key: val, value: config.attributes[val]
					}, {}) : {};
				}
			form.Block.call(block, {
				module: 'og.views.convention_forms.convention_attributes_tash',
				extras: {id: id, data: attr_data},
				processor: function (data) {
					var attributes = {},
						path = config.index.split('.'),
						last = path.pop();
					//$('og.attributes-add-table')
				}
			});
			
		}
	}
})