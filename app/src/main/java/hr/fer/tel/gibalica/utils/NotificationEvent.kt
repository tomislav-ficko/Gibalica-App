package hr.fer.tel.gibalica.utils

data class NotificationEvent(
    val eventType: EventType
)

enum class EventType {
    COUNTER_FINISHED,
    POSE_DETECTED,
    POSE_NOT_DETECTED
}
