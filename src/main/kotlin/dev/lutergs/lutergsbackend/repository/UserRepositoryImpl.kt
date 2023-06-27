package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.user.User
import dev.lutergs.lutergsbackend.service.user.UserRepository
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Document(collection = "user")
class UserEntity {
    @Id
    var id: String? = null
    var username: String? = null
    var password: String? = null
    var createdAt: LocalDateTime? = null

    fun toUser(): User {
        return User(id = this.username!!, password = this.password!!, createdAt = this.createdAt!!)
    }

    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity().apply {
                this.username = user.id
                this.password = user.password
                this.createdAt = user.createdAt
            }
        }
    }
}

@Repository
interface UserReactiveMongoRepository: ReactiveMongoRepository<UserEntity, String> {
    fun findByUsername(username: String): Flux<UserEntity>
    fun findDistinctFirstByUsername(username: String): Mono<UserEntity>
}

@Component
class UserRepositoryImpl(
    private val userReactiveMongoRepository: UserReactiveMongoRepository
): UserRepository {
    override fun saveUser(user: User): Mono<User> {
        return this.userReactiveMongoRepository.findDistinctFirstByUsername(user.id)
            .switchIfEmpty(this.userReactiveMongoRepository.save(UserEntity.fromUser(user)))
            .flatMap { Mono.just(it.toUser()) }
    }

    override fun getUser(user: User): Mono<User> {
        TODO("Not yet implemented")
    }

    override fun deleteUser(user: User): Mono<User> {
        TODO("Not yet implemented")
    }

    override fun updateUser(id: String, oldPassword: String, newPassword: String): Mono<User> {
        TODO("Not yet implemented")
    }
}