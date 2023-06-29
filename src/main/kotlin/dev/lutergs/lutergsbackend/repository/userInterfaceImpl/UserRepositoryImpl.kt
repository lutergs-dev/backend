package dev.lutergs.lutergsbackend.repository.userInterfaceImpl

import dev.lutergs.lutergsbackend.service.user.Email
import dev.lutergs.lutergsbackend.service.user.User
import dev.lutergs.lutergsbackend.service.user.UserRepository
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime

@Document(collection = "user")
class UserEntity {
    @Id
    var id: String? = null
    var email: String? = null
    var createdAt: LocalDateTime? = null

    fun toUser(): User {
        return User(Email.fromFullString(this.email!!), this.createdAt!!)
    }

    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity().apply {
                this.email = user.email.toString()
                this.createdAt = user.createdAt ?: LocalDateTime.now()
            }
        }
    }
}

@Repository
interface UserReactiveMongoRepository: ReactiveMongoRepository<UserEntity, String> {
    fun findDistinctFirstByEmail(email: String): Mono<UserEntity>
}

@Component
class UserRepositoryImpl(
    private val userReactiveMongoRepository: UserReactiveMongoRepository
): UserRepository {
    override fun getUser(email: Email): Mono<User> {
        return this.userReactiveMongoRepository
            .findDistinctFirstByEmail(email.toString())
            .flatMap { Mono.just(it.toUser()) }
    }

    override fun saveUser(user: User): Mono<User> {
        return this.userReactiveMongoRepository
            .findDistinctFirstByEmail(user.email.toString())
            .switchIfEmpty { this.userReactiveMongoRepository.save(UserEntity.fromUser(user)) }
            .flatMap { Mono.just(it.toUser()) }
    }
}