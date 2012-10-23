package frontlinesms2

import net.frontlinesms.messaging.ATDeviceDetector

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.smslib.NotConnectedException

class SmslibFconnection extends Fconnection {
	static passwords = ['pin']
	static configFields = ['name', 'port', 'baud', 'pin', 'imsi', 'serial', 'send', 'receive']
	static defaultValues = ['send':true, 'receive':true]
	static String getShortName() { 'smslib' }
	
	private def camelAddress = {
		def optional = { name, val ->
			return val? "&$name=$val": ''
		}
		"smslib:$port?debugMode=true&baud=$baud${optional('pin', pin)}&allMessages=$allMessages"
	}

	String port
	int baud
	String serial
	String imsi
	String pin // FIXME maybe encode this rather than storing plaintext(?)
	boolean allMessages = true
	boolean send = true
	boolean receive = true

	static constraints = {
		port blank:false
		imsi(nullable: true)
		pin(nullable: true)
		serial(nullable: true)
		send(nullable:true, validator: { val, obj ->
			if(!val) {
				return obj.receive
			}
		})
		receive(nullable:true, validator: { val, obj ->
			if(!val) {
				return obj.send
			}
		})
	}
	
	static namedQueries = {
		findForDetector { ATDeviceDetector d ->
			and {
				or {
					isNull('port')
					eq('port', d.portName)
				}
				or {
					isNull('serial')
					eq('serial', '')
					eq('serial', d.serial)
				}
				or {
					isNull('imsi')
					eq('imsi', '')
					eq('imsi', d.imsi)
				}
			}
		}
	}
	
	List<RouteDefinition> getRouteDefinitions() {
		return new RouteBuilder() {
			@Override void configure() {}
			List getRouteDefinitions() {
				if(getSend() && getReceive()) {
					return [from("seda:out-${SmslibFconnection.this.id}")
							.onException(NotConnectedException)
									.handled(true)
									.beanRef('fconnectionService', 'handleDisconnection')
									.end()
							.beanRef('smslibTranslationService', 'toCmessage')
							.to(camelAddress())
							.routeId("out-modem-${SmslibFconnection.this.id}"),
							from(camelAddress())
									.onException(NotConnectedException)
											.handled(true)
											.beanRef('fconnectionService', 'handleDisconnection')
											.end()
									.to('seda:raw-smslib')
									.routeId("in-${SmslibFconnection.this.id}")]	
				} else if(getReceive()) {
					return [from(camelAddress())
							.onException(NotConnectedException)
									.handled(true)
									.beanRef('fconnectionService', 'handleDisconnection')
									.end()
							.to('seda:raw-smslib')
							.routeId("in-${SmslibFconnection.this.id}")]
				} else {
					return [from("seda:out-${SmslibFconnection.this.id}")
							.onException(NotConnectedException)
									.handled(true)
									.beanRef('fconnectionService', 'handleDisconnection')
									.end()
							.beanRef('smslibTranslationService', 'toCmessage')
							.to(camelAddress())
							.routeId("out-modem-${SmslibFconnection.this.id}")]
				}
				
			}
		}.routeDefinitions
	}
}