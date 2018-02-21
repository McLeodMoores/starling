/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.masthead.menu',
    dependencies: [],
    obj: function () {
        return {
            init: function () {
                var config_link = '.OG-masthead .og-configs', config = '.OG-masthead .og-config',
                	conventions_link = '.OG-masthead .og-conventions', conventions = '.OG-masthead .og-convention',
                    data_link = '.OG-masthead .og-datas', data = '.OG-masthead .og-data', 
                    active = 'og-active-menu', hovering = false, common = og.views.common, config_menu_html, convention_menu_html;
                var hide_config = function () {
                    $(config).hide();
                    $(config_link).removeClass(active);
                    common.layout.main.resetOverflow('north');
                };
                var hide_conventions = function () {
                	$(conventions).hide();
                	$(conventions_link).removeClass(active);
                	common.layout.main.resetOverflow('north');
                }
                var hide_data = function () {
                    $(data).hide();
                    $(data_link).removeClass(active);
                    common.layout.main.resetOverflow('north');
                };
                var hide_all = function () {
                    hide_data();
                    hide_config();
                    hide_conventions();
                };
                var build_config_menu = function (list) {
                    var config_menu_html = "<td>";
                    list.forEach(function (entry) {
                        config_menu_html += '<header>' + entry.group + '</header><ul>';
                        entry.types.forEach(function (type) {
                            config_menu_html += '<li><a href="admin.ftl#/configs/filter=true/type=' + type.value + '">'
                                + type.name + '</a></li>';
                        });
                        config_menu_html += '</ul>';
                    });
                    config_menu_html += "</td>";
                    return config_menu_html;
                };
                var build_convention_menu = function (list) {
                	var convention_menu_html = "<td>";
                    list.forEach(function (entry) {
                        convention_menu_html += '<header>' + entry.group + '</header><ul>';
                        entry.types.forEach(function (type) {
                            convention_menu_html += '<li><a href="admin.ftl#/conventions/filter=true/type=' + type.value + '">'
                                + type.name + '</a></li>';
                        });
                        convention_menu_html += '</ul>';
                    });
                    convention_menu_html += "</td>";
                    return convention_menu_html;
                };
                $(config_link + ', ' + config)
                    .hover(function () {
                        hide_data();
                        hide_conventions();
                        common.layout.main.allowOverflow('north');
                        $(config).show();
                        $(config_link).addClass(active);
                        hovering = true;
                    },
                        function () {
                            hovering = false;
                            setTimeout(function () {
                                if (hovering === false) {
                                    hide_config();
                                }
                            }, 500);
                        })
                    .on('click', 'a', function () {hide_config(); });
                $(conventions_link + ', ' + conventions)
                	.hover(function () {
                		hide_config();
                		hide_data();
                		common.layout.main.allowOverflow('north');
                    	$(conventions).show();
                    	$(conventions_link).addClass(active);
                    	hovering = true;
                	},
                		function () {
                        	hovering = false;
                        	setTimeout(function () {
                        		if (hovering === false) {
                        			hide_conventions();
                        		}
                        	}, 500);
                    	})
                    .on('click', 'a', function () {hide_conventions(); });
                $(data_link + ', ' + data)
                    .hover(function () {
                        hide_config();
                        hide_conventions();
                        common.layout.main.allowOverflow('north');
                        $(data).show();
                        $(data_link).addClass(active);
                        hovering = true;
                    },
                        function () {
                            hovering = false;
                            setTimeout(function () {
                                if (hovering === false) {
                                    hide_data();
                                }
                            }, 500);
                        })
                    .on('click', 'a', function () {hide_data(); });

                $(config_link).on('click', function () {hide_all(); });
                $(data_link).on('click', function () {hide_all(); });
                $(conventions_link).on('click', function() {hide_all(); });
                og.api.rest.configs.get({meta: true, cache_for: 15 * 1000}).pipe(function (result) {
                    // remove the first half of groups and assign to 'left', the 'right' will then what is left
                    var groups = result.data.groups, left = groups.splice(0, Math.floor(groups.length / 2));
                    config_menu_html = '<table><tr>';
                    config_menu_html += build_config_menu(left);
                    config_menu_html += build_config_menu(groups);
                    config_menu_html += '</tr></table>';
                    $(config).html(config_menu_html);
                });
                og.api.rest.conventions.get({meta: true, cache_for: 15 * 1000}).pipe(function (result) {
                    // remove the first half of groups and assign to 'left', the 'right' will then what is left
                    var groups = result.data.groups, left = groups.splice(0, Math.floor(groups.length / 2));
                    convention_menu_html = '<table><tr>';
                    convention_menu_html += build_convention_menu(left);
                    convention_menu_html += build_convention_menu(groups);
                    convention_menu_html += '</tr></table>';
                    $(conventions).html(convention_menu_html);                	
                });
            },
            set_tab: function (name) {
                $('.OG-masthead a').removeClass('og-active');
                $('.OG-masthead .og-' + name).addClass('og-active');
            }
        };
    }
});