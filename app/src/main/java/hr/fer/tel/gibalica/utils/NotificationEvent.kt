package hr.fer.tel.gibalica.utils

data class NotificationEvent(
    val cause: CounterCause,
    val eventType: EventType
)

enum class EventType {
    COUNTER_FINISHED
}
