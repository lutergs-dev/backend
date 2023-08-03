package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.controller.NewTopicRequest
import dev.lutergs.lutergsbackend.controller.SubscriptionRequest
import dev.lutergs.lutergsbackend.controller.TriggerTopicRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PushService(
    private val pushRepository: PushRepository
) {

    fun saveSubscription(subscriptionRequest: SubscriptionRequest): Mono<Subscription> {
        return subscriptionRequest.toSubscription()
            .let { this.pushRepository.saveNewSubscription(it) }
    }

    fun saveTopic(topicRequest: NewTopicRequest): Mono<Topic> {
        return topicRequest.toTopic()
            .let { this.pushRepository.saveNewTopic(it) }
    }

    fun getTopics(): Flux<Topic> {
        return this.pushRepository.getTopics()
    }

    fun triggerTopic(triggerTopicRequest: TriggerTopicRequest): Mono<List<Int>> {
        return this.pushRepository.findTopicByUUID(topicUUID = triggerTopicRequest.topicUUID)
            .flatMap { this.pushRepository.sendTopicMessage(it, triggerTopicRequest.message) }
    }
}