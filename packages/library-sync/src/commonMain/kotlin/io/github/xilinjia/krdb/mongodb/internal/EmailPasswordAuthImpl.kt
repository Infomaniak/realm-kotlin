package io.github.xilinjia.krdb.mongodb.internal

import io.github.xilinjia.krdb.internal.interop.RealmAppPointer
import io.github.xilinjia.krdb.internal.interop.RealmInterop
import io.github.xilinjia.krdb.internal.util.Validation
import io.github.xilinjia.krdb.internal.util.use
import io.github.xilinjia.krdb.mongodb.auth.EmailPasswordAuth
import kotlinx.coroutines.channels.Channel
import org.mongodb.kbson.BsonValue
import org.mongodb.kbson.serialization.Bson

internal class EmailPasswordAuthImpl(private val app: RealmAppPointer) : EmailPasswordAuth {

    override suspend fun registerUser(email: String, password: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_register_email(
                app,
                Validation.checkEmpty(email, "email"),
                Validation.checkEmpty(password, "password"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun confirmUser(token: String, tokenId: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_confirm_user(
                app,
                Validation.checkEmpty(token, "token"),
                Validation.checkEmpty(tokenId, "tokenId"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun resendConfirmationEmail(email: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_resend_confirmation_email(
                app,
                Validation.checkEmpty(email, "email"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun retryCustomConfirmation(email: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_retry_custom_confirmation(
                app,
                Validation.checkEmpty(email, "email"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun sendResetPasswordEmail(email: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_send_reset_password_email(
                app,
                Validation.checkEmpty(email, "email"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun callResetPasswordFunction(email: String, newPassword: String, vararg args: Any?) {
        Channel<Result<Unit>>(1).use { channel ->
            BsonEncoder.encodeToBsonValue(args.asList()).let { bsonValue: BsonValue ->
                RealmInterop.realm_app_call_reset_password_function(
                    app,
                    Validation.checkEmpty(email, "email"),
                    Validation.checkEmpty(newPassword, "newPassword"),
                    Bson.toJson(bsonValue),
                    channelResultCallback<Unit, Unit>(channel) {
                        // No-op
                    }
                )
            }
            return channel.receive()
                .getOrThrow()
        }
    }

    override suspend fun resetPassword(token: String, tokenId: String, newPassword: String) {
        Channel<Result<Unit>>(1).use { channel ->
            RealmInterop.realm_app_email_password_provider_client_reset_password(
                app,
                Validation.checkEmpty(newPassword, "newPassword"),
                Validation.checkEmpty(token, "token"),
                Validation.checkEmpty(tokenId, "tokenId"),
                channelResultCallback<Unit, Unit>(channel) {
                    // No-op
                }
            )
            return channel.receive()
                .getOrThrow()
        }
    }
}
