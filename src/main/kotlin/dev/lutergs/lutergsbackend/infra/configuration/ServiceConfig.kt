package dev.lutergs.lutergsbackend.infra.configuration

import dev.lutergs.lutergsbackend.domain.guestbook.GuestbookRepository
import dev.lutergs.lutergsbackend.domain.image.ImageRepository
import dev.lutergs.lutergsbackend.domain.page.PageRepository
import dev.lutergs.lutergsbackend.domain.push.NewTopicMakeRequester
import dev.lutergs.lutergsbackend.domain.push.PushMessageSender
import dev.lutergs.lutergsbackend.domain.push.subscriber.SubscriberAggregate
import dev.lutergs.lutergsbackend.domain.push.subscriber.SubscriberProjection
import dev.lutergs.lutergsbackend.domain.push.topic.TopicAggregate
import dev.lutergs.lutergsbackend.domain.push.topic.TopicProjection
import dev.lutergs.lutergsbackend.domain.user.OAuthRequester
import dev.lutergs.lutergsbackend.domain.user.TokenGenerator
import dev.lutergs.lutergsbackend.domain.user.UserRepository
import dev.lutergs.lutergsbackend.service.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig {

    @Bean
    fun guestbookService(guestbookRepository: GuestbookRepository): GuestbookService {
        return GuestbookService(guestbookRepository)
    }

    @Bean
    fun imageService(imageRepository: ImageRepository): ImageService {
        return ImageService(imageRepository)
    }

    @Bean
    fun pageService(pageRepository: PageRepository, userService: UserService): PageService {
        return PageService(pageRepository, userService)
    }

    @Bean
    fun pushService(
        subscriberProjection: SubscriberProjection,
        subscriberAggregate: SubscriberAggregate,
        topicProjection: TopicProjection,
        topicAggregate: TopicAggregate,
        pushMessageSender: PushMessageSender,
        newTopicMakeRequester: NewTopicMakeRequester
    ): PushService {
        return PushService(
            subscriberProjection,
            subscriberAggregate,
            topicProjection,
            topicAggregate,
            pushMessageSender,
            newTopicMakeRequester
        )
    }

    @Bean
    fun userService(
        oAuthRequester: OAuthRequester,
        userRepository: UserRepository,
        tokenGenerator: TokenGenerator
    ): UserService {
        return UserService(oAuthRequester, userRepository, tokenGenerator)
    }
}