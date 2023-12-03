package dev.lutergs.lutergsbackend.infra.configuration

import dev.lutergs.lutergsbackend.domain.push.SubscriberReadRepository
import dev.lutergs.lutergsbackend.domain.push.SubscriberWriteRepository
import dev.lutergs.lutergsbackend.domain.push.TopicReadRepository
import dev.lutergs.lutergsbackend.domain.push.TopicWriteRepository
import dev.lutergs.lutergsbackend.domain.push.subscriber.SubscriberAggregate
import dev.lutergs.lutergsbackend.domain.push.subscriber.SubscriberProjection
import dev.lutergs.lutergsbackend.domain.push.topic.TopicAggregate
import dev.lutergs.lutergsbackend.domain.push.topic.TopicProjection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectionAndAggregateConfig {

    @Bean
    fun subscriberProjection(
        subscriberReadRepository: SubscriberReadRepository
    ): SubscriberProjection {
        return SubscriberProjection(subscriberReadRepository)
    }

    @Bean
    fun subscriberAggregate(
        subscriberWriteRepository: SubscriberWriteRepository
    ): SubscriberAggregate {
        return SubscriberAggregate(subscriberWriteRepository)
    }

    @Bean
    fun topicProjection(
        topicReadRepository: TopicReadRepository
    ): TopicProjection {
        return TopicProjection(topicReadRepository)
    }

    @Bean
    fun topicAggregate(
        topicWriteRepository: TopicWriteRepository
    ): TopicAggregate {
        return TopicAggregate(topicWriteRepository)
    }
}