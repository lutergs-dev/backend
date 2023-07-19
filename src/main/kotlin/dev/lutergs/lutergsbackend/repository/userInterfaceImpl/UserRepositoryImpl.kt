package dev.lutergs.lutergsbackend.repository.userInterfaceImpl

import dev.lutergs.lutergsbackend.service.user.Email
import dev.lutergs.lutergsbackend.service.user.NickName
import dev.lutergs.lutergsbackend.service.user.User
import dev.lutergs.lutergsbackend.service.user.UserRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime

@Table("user_account")
class UserEntity {
    @Id var id: Long? = null
    @Column("email") var email: String? = null
    @Column("nickname") var nickName: String? = null
    @Column("created_at") var createdAt: LocalDateTime? = null

    fun toUser(): User {
        return User(this.id!!, Email.fromFullString(this.email!!), this.createdAt!!, NickName(this.nickName!!))
    }

    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity().apply {
                this.id = user.id
                this.email = user.email.toString()
                this.createdAt = user.createdAt
                this.nickName = user.nickName.value
            }
        }
    }
}

@Repository
interface UserEntityReactiveRepository: ReactiveCrudRepository<UserEntity, Long> {
    fun findDistinctFirstByEmail(email: String): Mono<UserEntity>
    fun findDistinctFirstByNickName(nickName: String): Mono<UserEntity>
}

@Component
class UserRepositoryImpl(
    private val userEntityReactiveRepository: UserEntityReactiveRepository
): UserRepository {
    override fun getUserByEmail(email: Email): Mono<User> {
        return this.userEntityReactiveRepository
            .findDistinctFirstByEmail(email.toString())
            .flatMap { Mono.just(it.toUser()) }
    }

    override fun getUserByNickname(nickname: NickName): Mono<User> {
        return this.userEntityReactiveRepository
            .findDistinctFirstByNickName(nickname.value)
            .flatMap { Mono.just(it.toUser()) }
    }

    override fun checkUserExistsByNickName(nickName: NickName): Mono<Boolean> {
        return this.userEntityReactiveRepository
            .findDistinctFirstByNickName(nickName.value)
            .flatMap { Mono.just(true) }
            .switchIfEmpty { Mono.just(false) }
    }

    override fun saveUser(user: User): Mono<User> {
        return this.userEntityReactiveRepository
            .save(UserEntity.fromUser(user))
            .flatMap { Mono.just(it.toUser()) }
    }

    override fun changeNickName(user: User): Mono<User> {
        return this.userEntityReactiveRepository
            .findDistinctFirstByEmail(user.email.toString())
            .flatMap { Mono.just(it.apply { this.nickName = user.nickName.value }) }
            .flatMap { this.userEntityReactiveRepository.save(it) }
            .flatMap { Mono.just(it.toUser()) }
    }
}