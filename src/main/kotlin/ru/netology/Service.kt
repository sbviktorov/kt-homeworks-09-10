package ru.netology

import ru.netology.exceptions.QuantityOfChatsExceeded

object Service {
    var chatId: Int = 0
    var messageId: Int = 0
    var chatList = mutableListOf<Chat>()
    var messageList = listOf<Message>()
    var deletedMessages = listOf<Int>()
    var deletedChats = listOf<Int>()

    fun send(
        userId: Int,
        opponentId: Int,
        message: String
    ) {
        chatList.filter { chat ->
            chat.usersId.contains(userId) && chat.usersId.contains(opponentId) && !deletedChats.contains(chat.chatId)
        }//должен найтись 1 активный чат, если чатов нет - создаем чат, если больше 1 - что-то пошло не так
            .let {
                if (it.count() > 1) {
                    throw QuantityOfChatsExceeded(it.count())
                } else it
            }
            .firstOrNull()
            ?.let {
                it.message += Message(userId, opponentId, it.chatId, messageId, message)
                if (it.latestUnreadMessageIndex == null || it.message[it?.latestUnreadMessageIndex!!].authorId != userId) {
                    it.latestUnreadMessageIndex = it.message.indexOf(it.message.last())
                }
                messageId++
            } ?: addChat(userId, opponentId, message)
    }

    private fun addChat(
        userId: Int,
        opponentId: Int,
        message: String
    ) {
        val addingMessage = Message(userId, opponentId, chatId, messageId, message)
        chatList += Chat(
            chatId,
            listOf(userId, opponentId),
            mutableListOf(
                addingMessage
            )
        )
        chatList[chatId].latestUnreadMessageIndex = chatList[chatId].message.indexOf(addingMessage)
        chatId++
        messageId++
    }

    fun getChat(userId: Int): List<Chat> {//возвращает список чатов пользователя или "Нет сообщений"
        val chatsForUser = chatList.filter { it.usersId.contains(userId) && !deletedChats.contains(it.chatId) }
        if (chatsForUser.isEmpty()) {
            println("Нет сообщений")
        }
        return chatsForUser
    }

    fun getUnreadChatsCount(userId: Int): Int {
        return getChat(userId)
            .filter { it.latestUnreadMessageIndex != null }
            .count {
                (it.message[it.latestUnreadMessageIndex!!].authorId != userId)
            }
    }

    fun getNewMessagesByChat(userId: Int, chatId: Int, quantity: Int = 20): List<Message> {
        deletedChats.firstOrNull { it == chatId }// c if (deletedChats.contains(chatId)) читабельнее
            ?.let {
                println("Нет сообщений")
                return listOf<Message>()
            } ?: if (chatList[chatId].latestUnreadMessageIndex == null) {
            println("Нет новых сообщений")
            return listOf<Message>()
        }
        val listOfNewMessages = chatList[chatId].message.filter {
            it.messageId >= chatList[chatId].message[chatList[chatId].latestUnreadMessageIndex!!].messageId
                    && !deletedMessages.contains(it.messageId)
                    && it.authorId != userId
        }
            .take(quantity)

        chatList[chatId].latestUnreadMessageIndex =
            chatList[chatId].latestUnreadMessageIndex?.plus(listOfNewMessages.size)
        if (chatList[chatId].latestUnreadMessageIndex!! > chatList[chatId].message.size - 1) {
            chatList[chatId].latestUnreadMessageIndex = null
        }
        if (listOfNewMessages.isEmpty()) {
            println("Нет новых сообщений")
        }
        return listOfNewMessages
    }

    fun deleteMessage(userId: Int, chatId: Int, messageId: Int): Boolean { //удаление сообщений
        var result = false
        try {
            chatList[chatId].message
                .firstOrNull { it.messageId == messageId && it.authorId == userId }
                ?.let {
                    deletedMessages += messageId
                    println("Удаление сообщения id=$messageId прошло успешно")
                    result = true
                } ?: println("Такое сообщение не найдено.")
            chatList[chatId].message
                .filter { !deletedMessages.contains(it.messageId) }
                .let {
                    if (it.isEmpty()) {
                        deleteChat(userId, chatId)
                    }
                }
        } catch (_: IndexOutOfBoundsException) {
        }
        return result
    }

    fun deleteChat(userId: Int, chatId: Int): Boolean {
        try {
            if (chatList[chatId].usersId.contains(userId) && !deletedChats.contains(chatList[chatId].chatId)) {
                chatList[chatId].message
                    .filter { !deletedMessages.contains(it.messageId) }
                    .forEach { deletedMessages += it.messageId }
                deletedChats += chatId
                println("Удаление чата id=$chatId прошло успешно")
                return true
            }
        } catch (_: IndexOutOfBoundsException) {
        }
        println("Такой чат не найден.")
        return false
    }

    fun reset() {
        chatId = 0
        messageId = 0
        chatList = mutableListOf<Chat>()
        messageList = listOf<Message>()
        deletedMessages = listOf<Int>()
        deletedChats = listOf<Int>()
    }
}