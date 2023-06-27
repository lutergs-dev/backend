package dev.lutergs.lutergsbackend.service.user

import reactor.core.publisher.Mono

interface UserRepository {
    fun saveUser(user: User): Mono<User>
    fun getUser(user: User): Mono<User>
    fun deleteUser(user: User): Mono<User>
    fun updateUser(id: String, oldPassword: String, newPassword: String): Mono<User>
}