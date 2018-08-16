$.register_module({
	name: 'og.views.convention_forms.Attributes',
	dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, 
        	Block = og.common.util.ui.Block, 
        	add_table = '.og-add-attr-table',
            template = Handlebars.compile('<tr class="og-js-attr-row">\
            		<td class="attr_key">{{{key}}}</td>\
                    <td class="attr_val">{{{value}}}</td>\
                    <td><a class="OG-link-remove og-js-rem-attribute" href="#">remove</a></td>\
            		</tr>');
        var Attributes = function (config) {
            var block = this, 
            	id = og.common.id('attributes'), 
            	form = config.form;   
			if (config.attributes) {
				if (config.attributes.length) { 
					attr_data = Object.keys(config.attributes).reduce(function (acc, val) {
   	              		return acc.concat({ key: config.attributes[val]['Key'], value: config.attributes[val]['Value'] });
    	               }, []);
				} else { // only one attribute so hasn't been translated to an array
					attr_data = [{ key: config.attributes['Key'], value: config.attributes['Value'] }];
				}
			} else {
				attr_data = {};
			}
//            attr_data = (config.attributes ? 
//             	Object.keys(config.attributes).reduce(function (acc, val) {
//              		return acc.concat({ key: config.attributes[val]['Key'], value: config.attributes[val]['Value'] });
//               	}, []) : {});
            form.Block.call(block, {
                module: 'og.views.forms.convention-attributes_tash',
                extras: { id: id, data: attr_data },
                processor: function (data) {
                    var attributes = [], 
                    	path = config.index.split('.'), 
                    	last = path.pop();
                    $('.og-js-attr-row').each(function (i, e) {
                        attributes[i] = { 'Key': $(e).find('.attr_key').html().trim(), 'Value': $(e).find('.attr_val').html().trim() };
                    });
                    path.reduce(function (acc, val) { return acc[val]; }, data)[last] = attributes;
                }
            });
            block.on('click', '#' + id + ' ' + add_table + ' .og-js-rem-attribute', function (event) {
            	event.preventDefault();
                $(event.target).parent().parent().remove();
            }).on('click', '#' + id + ' .og-js-add-attribute', function (event) {
                event.preventDefault();
                var row = $(event.target).parent().parent(), 
                    key = row.find('.attr_key').val(),
                    value = row.find('.attr_val').val();
                if (!key || !value) {
                    return;
                }
                $(add_table).prepend(template({key: key, value: value}));
                row.find('[class^=attr_]').val('');
                $('.attr_key').focus();
            });
        };
        Attributes.prototype = new Block(null, { module: 'og.views.forms.convention-attributes_tash' }); // inherit Block prototype
        return Attributes;
    }
});