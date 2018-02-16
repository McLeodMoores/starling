<#include "modules/common/og.common.header.ftl">
<title>New</title>
${ogStyle.print('og_all.css', 'all', false)}
<style type="text/css">
    body {background: #fff;}
    div {margin-bottom: 9px}
    input[type=text] {width: 200px}
    td {vertical-align: top;}
    table {width: 100%;}
    small {font-size: 10px;}
</style>
</style>
<script type="text/javascript">
    window.onload = function () {document.getElementsByTagName('input')[0].focus();}
    function identifierHint (select) {
        document.getElementById("identifierHint").innerHTML = text(select.value);
        function text(value) {
            switch (value) {
                case "CURRENCY": return "Allowed scheme types: CurrencyISO";
                case "BANK": case "SETTLEMENT": case "TRADING": return "Allowed scheme types: CurrencyISO, ISO_COUNTRY_ALPHA2, ISO_COUNTRY_ALPHA3, FINANCIAL_REGION";
                case "CUSTOM": return "Allowed scheme types: Any";
                default: return "";
            }
        };
    }
</script>
</head>
<body>
    <table>
          <tr>
              <td>
                <div>
                    <label>
                        Holiday Type: <br/>
                        <select onchange="javascript:identifierHint(this);" id="holidayType">
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
                       Identifier: <br/><input type="text" id="identifier"><br/>
                    </label>
                    <small>
                       <label id="identifierHint">Allowed scheme types: CurrencyISO
                       </label>
                    </small>   
                </div>
                <div>
                  <label>
                    Weekend: <br/>
                    <select id="weekendType">
                      <option value="SATURDAY_SUNDAY">SATURDAY/SUNDAY</option>
                      <option value="FRIDAY_SATURDAY">FRIDAY/SATURDAY</option>
                      <option value="THURSDAY_FRIDAY">THURSDAY/FRIDAY</option>
                    </select>
                  </label>
                </div>
            </td>
          </tr>
      </table>
</body>
</html>
