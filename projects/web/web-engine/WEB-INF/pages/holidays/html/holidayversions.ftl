<#escape x as x?html>
<@page title="Holiday history  - ${holidayDoc.name}">

<@section css="info" if=deleted>
  <p>This holiday has been deleted</p>
</@section>


<#-- SECTION Holiday output -->
<@section title="Holiday">
  <p>
    <@rowout label="Name">${holidayDoc.name}</@rowout>
    <@rowout label="Type">${holidayDescription}</@rowout>
    <@rowout label="Reference">${holidayDoc.uniqueId.value}</@rowout>
  </p>


<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.holidayVersion(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.holidayVersion(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.holiday()}">Latest version - ${holidayDoc.uniqueId.version}</a><br />
    <a href="${uris.holidays()}">Holiday home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
