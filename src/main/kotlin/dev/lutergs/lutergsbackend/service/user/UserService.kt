package dev.lutergs.lutergsbackend.service.user

import dev.lutergs.lutergsbackend.controller.ChangeUserPasswordDto
import dev.lutergs.lutergsbackend.controller.RawUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    @Value("\${custom.encrypt.salt}") private val salt: String
) {

    fun createUser(rawUser: RawUser): Mono<Boolean> {
        User.fromRawUser(rawUser, this.salt).also { println(it) }
        return Mono.just(true)
    }

    fun verifyUser(rawUser: RawUser): Mono<Boolean> {
        return verifyUser(User.fromRawUser(rawUser, salt))
    }

    fun verifyUser(user: User): Mono<Boolean> {
        return this.userRepository.getUser(user)
            .flatMap { dbSavedUser -> Mono.just(user.password == dbSavedUser.password) }
            .defaultIfEmpty(false)
    }

    fun deleteUser(rawUser: RawUser): Mono<Boolean> {
        return User.fromRawUser(rawUser, salt)
            .let { user -> this.verifyUser(user)
                .flatMap { isVerified ->
                    if (isVerified) this.userRepository.deleteUser(user).flatMap { Mono.just(true) }
                    else Mono.just(false)
                }
            }
    }

    fun changePassword(changeUserPasswordDto: ChangeUserPasswordDto): Mono<Boolean> {
        return this.verifyUser(RawUser(id = changeUserPasswordDto.id, password = changeUserPasswordDto.oldPassword))
            .flatMap { isValid ->
                if (isValid) this.userRepository
                    .updateUser(id = changeUserPasswordDto.id, oldPassword = changeUserPasswordDto.newPassword, newPassword = changeUserPasswordDto.newPassword)
                    .flatMap { Mono.just(true) }
                else Mono.just(false)
            }
    }
}