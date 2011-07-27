<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<title><g:layoutTitle default="Messages"/></title>
		<g:layoutHead />
		<g:render template="/css"/>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
		<g:javascript library="jquery" plugin="jquery"/>
		<jqui:resources />
		<g:javascript src="application.js"/>
		<g:javascript src="popup.js" />
		<g:javascript src="/message/move_dropdown.js" />
		<g:javascript src="/message/categorize-dropdown.js" />
    </head>
	<body>
		<div id="container">
			<g:render template="/system_menu"/>
			<g:render template="/tabs"/>
	        <g:render template="/flash"/>
	        <div class="main">
				<g:render template="menu"/>
				<div class="content">
					<div class="content-header">
						<g:if test="${messageSection == 'poll'}">
							<g:render template="poll_header"/>
						</g:if>
						<g:elseif test="${messageSection == 'folder'}">
							<h2 id="message-title">${ownerInstance?.name}</h2>
						</g:elseif>
						<g:else>
							<h2 id="message-title">${messageSection}</h2>
						</g:else>
				        <g:remoteLink controller="quickMessage" action="create" onSuccess="launchWizard('Quick Message', data);" id="quick_message">
							Quick Message
						</g:remoteLink>
					</div>
					<div class="content-body">
						<g:render template="message_list"/>
						<g:layoutBody />
					</div>
					<div class="content-footer">
							<ul id="filter">
								<li>Show:</li>
								<li><g:link action="${messageSection}" params="${params.findAll({it.key != 'starred' && it.key != 'max' && it.key != 'offset'})}">All</g:link></li>
								<li>|</li>
								<li><g:link action="${messageSection}" params="${params.findAll({it.key != 'max' && it.key != 'offset'}) + [starred: true]}" >Starred</g:link></li>
							</ul>
							<div id="page-arrows">
								<g:paginate next="Forward" prev="Back"
									 max="${grailsApplication.config.pagination.max}"
									action="${messageSection}" total="${messageInstanceTotal}" params= "${params.findAll({it.key != 'messageId'})}"/>
							</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
