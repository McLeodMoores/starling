<#escape x as x?html>
<@page title="Holiday - ${holidayDoc.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This holiday has been deleted</p>
</@section>


<#-- SECTION Holiday output -->
<@section title="Holiday">
  <p>
    <@rowout label="Name">${holidayDoc.name}</@rowout>
    <@rowout label="Type">${holidayDescription}</@rowout>
    <@rowout label="Reference">${holidayDoc.uniqueId.value}, version ${holidayDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
</@subsection>

<#noescape><@xmlEditorScript  xmlValue="${holidayXml}" readOnly=true></@xmlEditorScript></#noescape>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.holidayVersions()}">All versions</a><br />
    <a href="${uris.holiday()}">Latest version - ${latestHolidayDoc.uniqueId.version}</a><br />
    <a href="${uris.holidays()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
