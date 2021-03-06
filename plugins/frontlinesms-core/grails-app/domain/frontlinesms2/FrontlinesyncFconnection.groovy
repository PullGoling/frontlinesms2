package frontlinesms2

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.apache.camel.Exchange

import frontlinesms2.api.*

@FrontlineApiAnnotations(apiUrl="frontlinesync")
class FrontlinesyncFconnection extends Fconnection implements FrontlineApi {
	static final checkIntervalOptions = [1, 5, 15, 30, 60, 120, 0]
	static String getShortName() { 'frontlinesync' }
	static final passwords = []
	static final configFields = ['info-setup': ['secret'], 'info-name':['name']]

	def frontlinesyncService
	def appSettingsService
	def grailsLinkGenerator
	def urlHelperService
	def dispatchRouterService

	Date lastConnectionTime
	boolean sendEnabled = false
	boolean receiveEnabled = false
	boolean missedCallEnabled = false
	boolean configSynced = false
	boolean hasDispatches = false
	int checkInterval = 15
	String secret

	static constraints = {
		lastConnectionTime nullable:true
	}

	def apiProcess(controller) {
		frontlinesyncService.apiProcess(this, controller)
	}

	boolean isApiEnabled() { return this.sendEnabled || this.receiveEnabled }

	def getCustomStatus() {
		lastConnectionTime ? (this.enabled ? ConnectionStatus.CONNECTED : ConnectionStatus.DISABLED) : ConnectionStatus.CONNECTING
	}

	List<RouteDefinition> getRouteDefinitions() {
		def routeDefinitions = new RouteBuilder() {
			@Override void configure() {}
			List getRouteDefinitions() {
				def definitions = []
				if(isSendEnabled()) {
					definitions << from("seda:out-${FrontlinesyncFconnection.this.id}")
							.setHeader('fconnection-id', simple(FrontlinesyncFconnection.this.id.toString()))
							.beanRef('frontlinesyncService', 'processSend')
							.routeId("out-internet-${FrontlinesyncFconnection.this.id}")
				}
				return definitions
			}
		}.routeDefinitions
		return routeDefinitions
	}

	def getIndexOfCurrentCheckFrequency() {
		return checkIntervalOptions.indexOf(checkInterval)
	}

	String getFullApiUrl(request) {
		return apiEnabled? "${urlHelperService.getBaseUrl(request)}" :''
	}

	def removeDispatchesFromQueue() {
		QueuedDispatch.deleteAll(this)
		if(this.hasDispatches) {
			this.hasDispatches = false
			this.save()
		}
	}

	def addToQueuedDispatches(d) {
		QueuedDispatch.create(this, d)
		this.hasDispatches = true
	}

	def getQueuedDispatches() {
		QueuedDispatch.getDispatches(this)
	}

	def updateDispatch(Exchange x) {
		// Dispatch is already in PENDING state so no need to change the status
	}
}

