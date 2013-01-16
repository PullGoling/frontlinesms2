package frontlinesms2

import spock.lang.*
import grails.plugin.spock.*
import frontlinesms2.*

@TestFor(CustomActivity)
@Mock([JoinActionStep, StepProperty, ReplyActionStep, LeaveActionStep])
class CustomActivitySpec extends Specification {
	def "a custom activity can have one step"() {
		given:
			def joinStep = new JoinActionStep(stepProperties:[new StepProperty(key:"group", value:"football")])
			joinStep.save(failOnError:true)
			def customActivity = new CustomActivity(name:"Custom Activity")
			customActivity.addToSteps(joinStep)
			customActivity.save(flush:true, failOnError:true)
		expect:
			CustomActivity.count()			
	}

	def "a custom activity can have many steps of different kinds"() {
		given:
			def joinStep = new JoinActionStep(stepProperties:[new StepProperty(key:"group", value:"football")])
			def replyStep = new ReplyActionStep(stepProperties: [new StepProperty(key:"autoreplyText", value:"autoreply :)")])

			def customActivity = new CustomActivity(name:"Custom Activity")
			customActivity.addToSteps(joinStep)
			customActivity.addToSteps(replyStep)
			customActivity.save(flush:true)
		expect:
			CustomActivity.count()	
	}

	def "custom activity steps should be ordered according to the order of addition"() {
		given:
			def joinStep = new JoinActionStep(stepProperties:[new StepProperty(key:"group", value:"football")])
			def replyStep = new ReplyActionStep(stepProperties: [new StepProperty(key:"autoreplyText", value:"autoreply :)")])
			def leaveStep = new LeaveActionStep(stepProperties: [new StepProperty(key:"group", value:"Friends")])

			def customActivity = new CustomActivity(name:"Custom Activity")
			customActivity.addToSteps(joinStep)
			customActivity.addToSteps(replyStep)
			customActivity.addToSteps(leaveStep)
			customActivity.save(flush:true)
		expect:
			CustomActivity.list()[0].steps == [joinStep, replyStep, leaveStep]
	}
}