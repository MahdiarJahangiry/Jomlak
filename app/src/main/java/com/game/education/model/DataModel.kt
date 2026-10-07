package com.game.education.model


data class AddCoin(
    val amount: Int,
    val description: String,
    val reference: String
)

data class UpdateEmail(
    val email: String
)

data class Verify(
    val code: String
)

data class Sentence(
    val sentence_id: Int
)