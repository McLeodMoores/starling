<#include "modules/common/og.common.header.ftl">
<title>Import</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css">
    body {background: #fff;}
    div {margin-bottom: 9px}
    small {font-size: 10px;}
    input[type=text] {width: 200px}
    td {vertical-align: top;}
    table {width: 100%;}
</style>
<script type="text/javascript">
  window.onload = function () {document.getElementsByTagName('input')[0].focus();}
</script>
</head>
<body>
	<form action="/jax/holidayupload" enctype="multipart/form-data" method="post">
		<table>
        	<tr>
          		<td>
            		<div>
	  		            <label>
            	        	CSV/XLS Upload:<br/>
                  			<input type="file" name="file"><br/>
              			</label>
              			<small><a href="/prototype/data/example-holidays.csv">Example CSV Format</a></small>
            		</div>
            		<div>
            			<label>
            				Weekend: <br/>
            				<select name="weekendType">
            					<option value="SATURDAY_SUNDAY">SATURDAY/SUNDAY</option>
            					<option value="FRIDAY_SATURDAY">FRIDAY/SATURDAY</option>
            					<option value="THURSDAY_FRIDAY">THURSDAY/FRIDAY</option>
            				</select>
            		<div>
                		<label>
                			Holiday Type: <br/>
                			<select name="holidayType">
    	            			<option value="CURRENCY">CURRENCY</option>
	                			<option value="BANK">BANK</option>
        	        			<option value="CUSTOM">CUSTOM</option>
            	    			<option value="SETTLEMENT">SETTLEMENT</option>
                				<option value="TRADING">TRADING</option>
              				</select>
            			</label>
          			</div>
            		<div>
              			<label>
                  			Identifier: <br /><input type="text" name="id"><br/>
              			</label>
            		</div>
          			<div>
          				<label>
          					Date Format: <br/>
              				<select name="dateFormat">
                				<option value="ISO">ISO Format (yyyy-MM-dd or yyyyMMdd)</option>
                				<option value="US">US Format (MM-dd-yyyy or MM/dd/yyyy)</option>
                				<option value="UK">UK Format (dd-MM-yyyy or dd/MM/yyyy)</option>
              				</select>          					
          				</label>
          			</div>
        		</td>
      		</tr>
    	</table>
  	</form>
</body>
</html>
