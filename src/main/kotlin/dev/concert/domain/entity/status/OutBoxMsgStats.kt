package dev.concert.domain.entity.status

enum class OutBoxMsgStats {
    INIT,
    SEND_SUCCESS,
    SEND_FAIL,
}