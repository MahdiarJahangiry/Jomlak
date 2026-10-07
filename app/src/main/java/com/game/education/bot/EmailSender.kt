package com.game.education.bot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

object EmailSender {

    data class EmailConfig(
        val host: String = "smtp.gmail.com",
        val port: Int = 587,
        val user: String,
        val password: String,
        val to: String
    )

    suspend fun sendReport(
        config: EmailConfig,
        subject: String,
        htmlBody: String,
        plainBody: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", config.host)
                put("mail.smtp.port", config.port.toString())
                put("mail.smtp.ssl.trust", config.host)
                put("mail.smtp.connectiontimeout", "15000")
                put("mail.smtp.timeout", "15000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.user, config.password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.user))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.to))
                this.subject = subject

                val multipart = MimeMultipart("alternative")

                val textPart = MimeBodyPart()
                textPart.setText(plainBody, "UTF-8")
                multipart.addBodyPart(textPart)

                val htmlPart = MimeBodyPart()
                htmlPart.setContent(htmlBody, "text/html; charset=UTF-8")
                multipart.addBodyPart(htmlPart)

                setContent(multipart)
            }

            Transport.send(message)
            Result.success("Email sent successfully to " + config.to)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}