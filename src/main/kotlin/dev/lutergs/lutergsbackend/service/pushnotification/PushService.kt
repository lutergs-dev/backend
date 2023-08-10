package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.controller.*
import dev.lutergs.lutergsbackend.repository.pushRepositoryImpl.Response
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PushService(
    private val pushRepository: PushRepository,
    private val newTopicMakeRequestRepository: NewTopicMakeRequestRepository
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
            // Not showing TEST topic
            .filter { it.name != "testTopic" }
    }

    fun getSubscribedTopics(auth: String): Mono<List<Topic>> {
        return this.pushRepository.findSubscriptionByAuth(auth, true)
            .flatMap { Mono.just(it.topics!!) }
    }

    fun subscribeToTopic(subscriptionAuth: String, topicUUID: String): Mono<Boolean> {
        return Mono.zip(
            this.pushRepository.findSubscriptionByAuth(subscriptionAuth, false),
            this.pushRepository.findTopicByUUID(topicUUID)
        ).flatMap {
            val (subscription, topic) = Pair(it.t1, it.t2)
            this.pushRepository.subscribeToTopic(subscription, topic)
        }
    }

    fun unsubscribeToTopic(subscriptionAuth: String, topicUUID: String): Mono<Boolean> {
        return Mono.zip(
            this.pushRepository.findSubscriptionByAuth(subscriptionAuth, false),
            this.pushRepository.findTopicByUUID(topicUUID)
        ).flatMap {
            val (subscription, topic) = Pair(it.t1, it.t2)
            this.pushRepository.unsubscribeToTopic(subscription, topic)
        }
    }

    fun triggerTopic(triggerTopicRequest: TriggerTopicRequest): Flux<Response> {
        println(triggerTopicRequest)
        return this.pushRepository.findTopicByUUID(topicUUID = triggerTopicRequest.topicUUID)
            .flatMapMany { this.pushRepository.sendTopicMessage(it, triggerTopicRequest.toPushMessage()) }
    }

    fun newTopicMakeRequest(newTopicMakeRequest: NewTopicMakeRequest): Mono<Boolean> {
        return this.newTopicMakeRequestRepository.request(newTopicMakeRequest.description)
    }
}