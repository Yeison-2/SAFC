package com.safc.caficultura.config

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MailConfiguration {

    private val emailUser = ""
    private val emailPassword = ""

    private fun getMailSession(): Session {
        val props = Properties().apply {
            put("mail.transport.protocol", "smtp")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailUser, emailPassword)
            }
        })
    }

    suspend fun sendEmail(to: String, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            try {
                val session = getMailSession()
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(emailUser))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    setSubject(subject)
                    setText(body)
                }
                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
                // En una app real podrías querer manejar este error de forma más visible
            }
        }
    }
}
