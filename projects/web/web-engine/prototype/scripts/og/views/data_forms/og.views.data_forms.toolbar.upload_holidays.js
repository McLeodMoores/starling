/*
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.data_forms.toolbar.upload_holidays',
	dependencies: ['og.common.util.ui.dialog'],
	obj: function () {
		return function () {
			og.common.util.ui.dialog({
   			    width: 600,
   			    height: 420,
   			    type: 'input',
   			    title: 'import',
   			    custom: '<iframe id="import" src="import_holidays.ftl" width="100%" height="300" marginheight="0"\
                    marginwidth="0" frameborder="0" />',
                buttons: {
                	'Start Import': function () {
                		$('#import').load(og.views.holidays.search).contents().find('form').submit();
                		$(this).dialog('option', 'buttons', {'Close': function () {
                			$(this).dialog('close').remove();
                		}});
                	},
                	'Cancel': function () {$(this).dialog('close'); }
                }
            });
		};
	}
});